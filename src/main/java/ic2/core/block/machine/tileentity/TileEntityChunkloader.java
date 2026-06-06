package ic2.core.block.machine.tileentity;

import com.google.common.collect.ImmutableSet;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ChunkLoaderLogic;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.block.machine.gui.GuiChunkLoader;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityChunkloader extends TileEntityInventory implements INetworkClientTileEntityEventListener, IHasGui, IUpgradableBlock
{
	private ForgeChunkManager.Ticket ticket;
	private final Set<ChunkPos> loadedChunks = new HashSet<>();
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlotDischarge dischargeSlot;
	public final Energy energy;
	private static final int defaultTier = 1;
	private static final int defaultEnergyStorage = 2500;
	private final double euPerChunk = ConfigUtil.getFloat(MainConfig.get(), "balance/euPerChunk");

	public TileEntityChunkloader()
	{
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
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("loadedChunks", 4);
		this.loadedChunks.clear();

		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagLong currentNBT = (NBTTagLong) list.get(i);
			long value = currentNBT.getLong();
			this.loadedChunks.add(ChunkLoaderLogic.deserialize(value));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();
		nbt.setTag("loadedChunks", list);

		for (ChunkPos chunk : this.loadedChunks)
		{
			list.appendTag(new NBTTagLong(ChunkLoaderLogic.serialize(chunk)));
		}

		return nbt;
	}

	@Override
	public void setActive(boolean active)
	{
		World world = this.getWorld();
		if (!world.isRemote && this.getActive() != active)
		{
			if (active)
			{
				if (this.ticket != null)
				{
					throw new IllegalStateException("Cannot activate ChunkLoader: " + this.pos + " " + this.ticket);
				}

				this.ticket = ChunkLoaderLogic.getInstance().createTicket(world, this.pos);

				for (ChunkPos coords : this.loadedChunks)
				{
					ChunkLoaderLogic.getInstance().addChunkToTicket(this.ticket, coords);
				}
			} else
			{
				if (this.ticket == null)
				{
					throw new IllegalStateException("Cannot deactivate ChunkLoader: " + this.pos + " " + this.ticket);
				}

				ChunkLoaderLogic.getInstance().removeTicket(this.ticket);
				this.ticket = null;
			}
		}

		super.setActive(active);
	}

	@Override
	public void onLoaded()
	{
		super.onLoaded();
		World world = this.getWorld();
		if (!world.isRemote)
		{
			this.ticket = ChunkLoaderLogic.getInstance().getTicket(world, this.pos, false);
			if (this.ticket != null)
			{
				this.loadedChunks.clear();
				this.loadedChunks.addAll(this.ticket.getChunkList());
			}

			super.setActive(this.ticket != null);
			this.setOverclockRates();
		}
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
		this.ticket = null;
	}

	@Override
	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		super.onPlaced(stack, placer, facing);
		this.loadedChunks.add(ChunkLoaderLogic.getChunkCoords(this.pos));
	}

	@Override
	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return this.getWorld().isRemote ? true : IC2.platform.launchGui(player, this);
	}

	@Override
	public ContainerBase<TileEntityChunkloader> getGuiContainer(EntityPlayer player)
	{
		return new ContainerChunkLoader(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiChunkLoader(new ContainerChunkLoader(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	public void addChunkToLoaded(ChunkPos chunk)
	{
		if (this.getWorld().isRemote)
		{
			new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
		} else if (!this.isChunkInRange(chunk))
		{
			IC2.log.warn(LogCategory.Block, "Trying to add a Chunk to loaded, however the chunk is too far away. Aborting.");
		} else
		{
			if (this.getLoadedChunks().size() < ChunkLoaderLogic.getInstance().getMaxChunksPerTicket())
			{
				if (this.ticket != null)
				{
					ChunkLoaderLogic.getInstance().addChunkToTicket(this.ticket, chunk);
				}

				this.loadedChunks.add(chunk);
				this.markDirty();
			}
		}
	}

	public void removeChunkFromLoaded(ChunkPos chunk)
	{
		if (this.getWorld().isRemote)
		{
			new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
		} else if (!ChunkLoaderLogic.getChunkCoords(this.pos).equals(chunk))
		{
			if (this.ticket != null)
			{
				ChunkLoaderLogic.getInstance().removeChunkFromTicket(this.ticket, chunk);
			}

			this.loadedChunks.remove(chunk);
			this.markDirty();
		}
	}

	public ImmutableSet<ChunkPos> getLoadedChunks()
	{
		return ImmutableSet.copyOf(this.loadedChunks);
	}

	public boolean isChunkInRange(ChunkPos chunk)
	{
		ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.pos);
		return Math.abs(chunk.x - mainChunk.x) <= 4 && Math.abs(chunk.z - mainChunk.z) <= 4;
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		int x = (event & 15) - 8;
		int z = (event >> 4 & 15) - 8;
		ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.pos);
		ChunkPos chunk = new ChunkPos(mainChunk.x + x, mainChunk.z + z);
		if (this.isChunkInRange(chunk))
		{
			if (this.getLoadedChunks().contains(chunk))
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
		if (this.ticket != null)
		{
			ChunkLoaderLogic.getInstance().removeTicket(this.ticket);
			this.ticket = null;
		}
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
	public void markDirty()
	{
		super.markDirty();
		if (IC2.platform.isSimulating())
		{
			this.setOverclockRates();
		}
	}
}
