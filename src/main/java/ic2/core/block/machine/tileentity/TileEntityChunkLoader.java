package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ChunkLoaderLogic;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.init.IC2Config;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.LogCategory;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChunkLoader extends TileEntityInventory implements INetworkClientTileEntityEventListener, IHasGui, IUpgradableBlock
{
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlotDischarge dischargeSlot;
	public final Energy energy;
	private final LongSet loadedChunks = new LongOpenHashSet();
	private final double euPerChunk = IC2Config.balance.euPerChunk.get();

	public TileEntityChunkLoader(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CHUNK_LOADER, pos, state);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.IO, 1, true, InvSlot.InvSide.ANY);
		this.energy = this.addComponent(Energy.asBasicSink(this, 2500.0, 1).addManagedSlot(this.dischargeSlot));
	}

	@Override
	public void updateEntityServer()
	{
		super.updateEntityServer();
		boolean active = this.energy.useEnergy(this.getLoadedChunks().size() * this.euPerChunk);
		if (active != this.getActive())
		{
			this.setActive(active);
		}

		this.upgradeSlot.tick();
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		ListTag list = nbt.getList("loadedChunks", 4);
		this.loadedChunks.clear();

		for (net.minecraft.nbt.Tag tag : list)
		{
			this.loadedChunks.add(((LongTag) tag).getAsLong());
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		ListTag list = new ListTag();
		nbt.put("loadedChunks", list);

		for (long pos : this.loadedChunks)
		{
			list.add(LongTag.valueOf(pos));
		}
	}

	@Override
	public void setActive(boolean active)
	{
		Level world = this.getLevel();
		if (!world.isClientSide && this.getActive() != active)
		{
			if (active)
			{
				ChunkLoaderLogic.addChunkLoader((ServerLevel) world, this.worldPosition, this.loadedChunks);
			} else
			{
				ChunkLoaderLogic.removeChunkLoader((ServerLevel) world, this.worldPosition);
			}
		}

		super.setActive(active);
	}

	@Override
	public void onLoaded()
	{
		super.onLoaded();
		Level world = this.getLevel();
		if (!world.isClientSide)
		{
			this.setOverclockRates();
			if (this.getActive())
			{
				ChunkLoaderLogic.addChunkLoader((ServerLevel) world, this.worldPosition, this.loadedChunks);
			}
		}
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		this.loadedChunks.add(ChunkPos.asLong(this.worldPosition));
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerChunkLoader(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerChunkLoader(syncId, inventory, this);
	}

	public void addChunkToLoaded(ChunkPos chunk)
	{
		if (this.level.isClientSide)
		{
			new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
		} else if (!this.isChunkInRange(chunk))
		{
			IC2.log.warn(LogCategory.Block, "Trying to add a Chunk to loaded, however the chunk is too far away. Aborting.");
		} else
		{
			if (this.loadedChunks.add(chunk.toLong()))
			{
				ChunkLoaderLogic.updateChunkLoader((ServerLevel) this.level, this.worldPosition, this.loadedChunks);
				this.setChanged();
			}
		}
	}

	public void removeChunkFromLoaded(ChunkPos chunk)
	{
		if (this.level.isClientSide)
		{
			new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
		} else if (ChunkPos.asLong(this.worldPosition) != chunk.toLong())
		{
			if (this.loadedChunks.remove(chunk.toLong()))
			{
				ChunkLoaderLogic.updateChunkLoader((ServerLevel) this.level, this.worldPosition, this.loadedChunks);
				this.setChanged();
			}
		}
	}

	public LongSet getLoadedChunks()
	{
		return this.loadedChunks;
	}

	public boolean isChunkInRange(ChunkPos chunk)
	{
		ChunkPos mainChunk = new ChunkPos(this.worldPosition);
		return Math.abs(chunk.x - mainChunk.x) <= 4 && Math.abs(chunk.z - mainChunk.z) <= 4;
	}

	public int getMaxChunks()
	{
		return 9;
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		int x = (event & 15) - 8;
		int z = (event >> 4 & 15) - 8;
		ChunkPos mainChunk = new ChunkPos(this.worldPosition);
		ChunkPos chunk = new ChunkPos(mainChunk.x + x, mainChunk.z + z);
		if (this.isChunkInRange(chunk))
		{
			if (this.getLoadedChunks().contains(chunk.toLong()))
			{
				this.removeChunkFromLoaded(chunk);
			} else
			{
				this.addChunkToLoaded(chunk);
			}
		}
	}

	@Override
	protected void onBlockBreak()
	{
		super.onBlockBreak();
		ChunkLoaderLogic.removeChunkLoader((ServerLevel) this.level, this.worldPosition);
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.Transformer);
	}

	public void setOverclockRates()
	{
		this.upgradeSlot.onChanged();
		int tier = this.upgradeSlot.getTier(1);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(2500, 0, 0));
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (IC2.sideProxy.isSimulating())
		{
			this.setOverclockRates();
		}
	}
}
