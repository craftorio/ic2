package ic2.core.block.comp;

import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.network.GrowingBuffer;

import java.io.DataInput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

public abstract class TileEntityComponent
{
	protected final TileEntityBlock parent;

	public TileEntityComponent(TileEntityBlock parent)
	{
		this.parent = parent;
	}

	public TileEntityBlock getParent()
	{
		return this.parent;
	}

	public void readFromNbt(NBTTagCompound nbt)
	{
	}

	public NBTTagCompound writeToNbt()
	{
		return null;
	}

	public void onLoaded()
	{
	}

	public void onUnloaded()
	{
	}

	public void onNeighborChange(Block srcBlock, BlockPos srcPos)
	{
	}

	public void onContainerUpdate(EntityPlayerMP player)
	{
	}

	public void onNetworkUpdate(DataInput is) throws IOException
	{
	}

	public boolean enableWorldTick()
	{
		return false;
	}

	public void onWorldTick()
	{
	}

	protected void setNetworkUpdate(EntityPlayerMP player, GrowingBuffer data)
	{
		IC2.network.get(true).sendComponentUpdate(this.parent, Components.getId((Class<? extends TileEntityComponent>) this.getClass()), player, data);
	}

	public Collection<? extends Capability<?>> getProvidedCapabilities(EnumFacing side)
	{
		return Collections.emptySet();
	}

	public <T> T getCapability(Capability<T> cap, EnumFacing side)
	{
		return null;
	}
}
