package ic2.core.block.machine.tileentity;

import com.google.common.collect.ImmutableSet;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ChunkLoaderLogic;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.block.machine.gui.GuiChunkLoader;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
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
		this.energy = (Energy) addComponent((TileEntityComponent) Energy.asBasicSink(this, 2500.0D, 1).addManagedSlot(this.dischargeSlot));
	}

	public void updateEntityServer()
	{
		super.updateEntityServer();
		boolean active = this.energy.useEnergy(getLoadedChunks().size() * this.euPerChunk);
		if (active != getActive())
			setActive(active);
		this.upgradeSlot.tick();
	}

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

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();
		nbt.setTag("loadedChunks", list);
		for (ChunkPos chunk : this.loadedChunks)
			list.appendTag(new NBTTagLong(ChunkLoaderLogic.serialize(chunk)));
		return nbt;
	}

	public void setActive(boolean active)
	{
		World world = getWorld();
		if (!world.isRemote && getActive() != active)
			if (active)
			{
				if (this.ticket != null)
					throw new IllegalStateException("Cannot activate ChunkLoader: " + this.pos + " " + this.ticket);
				this.ticket = ChunkLoaderLogic.getInstance().createTicket(world, this.pos);
				for (ChunkPos coords : this.loadedChunks)
					ChunkLoaderLogic.getInstance().addChunkToTicket(this.ticket, coords);
			} else
			{
				if (this.ticket == null)
					throw new IllegalStateException("Cannot deactivate ChunkLoader: " + this.pos + " " + this.ticket);
				ChunkLoaderLogic.getInstance().removeTicket(this.ticket);
				this.ticket = null;
			}
		super.setActive(active);
	}

	public void onLoaded()
	{
		super.onLoaded();
		World world = getWorld();
		if (!world.isRemote)
		{
			this.ticket = ChunkLoaderLogic.getInstance().getTicket(world, this.pos, false);
			if (this.ticket != null)
			{
				this.loadedChunks.clear();
				this.loadedChunks.addAll(this.ticket.getChunkList());
			}
			super.setActive((this.ticket != null));
			setOverclockRates();
		}
	}

	protected void onUnloaded()
	{
		super.onUnloaded();
		this.ticket = null;
	}

	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		super.onPlaced(stack, placer, facing);
		this.loadedChunks.add(ChunkLoaderLogic.getChunkCoords(this.pos));
	}

	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if ((getWorld()).isRemote)
			return true;
		return IC2.platform.launchGui(player, this);
	}

	public ContainerBase<TileEntityChunkloader> getGuiContainer(EntityPlayer player)
	{
		return new ContainerChunkLoader(player, this);
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiChunkLoader(new ContainerChunkLoader(player, this));
	}

	public void onGuiClosed(EntityPlayer player)
	{
	}

	public void addChunkToLoaded(ChunkPos chunk)
	{
		if ((getWorld()).isRemote)
		{
			(new RuntimeException("Something tried to change the ChunkLoaderState on the client.")).printStackTrace();
			return;
		}
		if (!isChunkInRange(chunk))
		{
			IC2.log.warn(LogCategory.Block, "Trying to add a Chunk to loaded, however the chunk is too far away. Aborting.");
			return;
		}
		if (getLoadedChunks().size() < ChunkLoaderLogic.getInstance().getMaxChunksPerTicket())
		{
			if (this.ticket != null)
				ChunkLoaderLogic.getInstance().addChunkToTicket(this.ticket, chunk);
			this.loadedChunks.add(chunk);
			markDirty();
		}
	}

	public void removeChunkFromLoaded(ChunkPos chunk)
	{
		if ((getWorld()).isRemote)
		{
			(new RuntimeException("Something tried to change the ChunkLoaderState on the client.")).printStackTrace();
			return;
		}
		if (ChunkLoaderLogic.getChunkCoords(this.pos).equals(chunk))
			return;
		if (this.ticket != null)
			ChunkLoaderLogic.getInstance().removeChunkFromTicket(this.ticket, chunk);
		this.loadedChunks.remove(chunk);
		markDirty();
	}

	public ImmutableSet<ChunkPos> getLoadedChunks()
	{
		return ImmutableSet.copyOf(this.loadedChunks);
	}

	public boolean isChunkInRange(ChunkPos chunk)
	{
		ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.pos);
		return (Math.abs(chunk.x - mainChunk.x) <= 4 && Math.abs(chunk.z - mainChunk.z) <= 4);
	}

	public void onNetworkEvent(EntityPlayer player, int event)
	{
		int x = (event & 0xF) - 8;
		int z = (event >> 4 & 0xF) - 8;
		ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.pos);
		ChunkPos chunk = new ChunkPos(mainChunk.x + x, mainChunk.z + z);
		if (isChunkInRange(chunk))
		{
			if (getLoadedChunks().contains(chunk))
			{
				removeChunkFromLoaded(chunk);
			} else
			{
				addChunkToLoaded(chunk);
			}
		}
	}

	protected void onBlockBreak()
	{
		super.onBlockBreak();
		if (this.ticket != null)
		{
			ChunkLoaderLogic.getInstance().removeTicket(this.ticket);
			this.ticket = null;
		}
	}

	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

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

	public void markDirty()
	{
		super.markDirty();
		if (IC2.platform.isSimulating())
			setOverclockRates();
	}
}
