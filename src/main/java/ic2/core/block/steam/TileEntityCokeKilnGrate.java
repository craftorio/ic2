package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;

import java.util.Collections;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCokeKilnGrate extends TileEntityInventory implements IHasGui
{
	protected final Fluids fluidsComponent = this.addComponent(new Fluids(this));
	@GuiSynced
	protected final Fluids.InternalFluidTank fluidTank;

	public TileEntityCokeKilnGrate(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.COKE_KILN_GRATE, pos, state);
		this.fluidTank = this.fluidsComponent.addTank("fluidTank", 64000, InvSlot.Access.O, InvSlot.InvSide.ANY);
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.fluidsComponent.changeConnectivity(this.fluidTank, Collections.emptyList(), Collections.singleton(this.getFacing()));
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, net.minecraft.world.entity.player.Inventory inventory, ic2.core.network.GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}
}
