package ic2.core.block.comp;

import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.network.GrowingBuffer;

import java.io.DataInput;
import java.io.IOException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public abstract class TileEntityComponent
{
	protected final Ic2TileEntity parent;

	public TileEntityComponent(Ic2TileEntity parent)
	{
		this.parent = parent;
	}

	public Ic2TileEntity getParent()
	{
		return this.parent;
	}

	public void readFromNbt(CompoundTag nbt)
	{
	}

	public CompoundTag writeToNbt()
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

	public void onContainerUpdate(ServerPlayer player)
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

	protected void setNetworkUpdate(ServerPlayer player, GrowingBuffer data)
	{
		IC2.network.get(true).sendComponentUpdate(this.parent, Components.getId((Class<? extends TileEntityComponent>) this.getClass()), player, data);
	}
}
