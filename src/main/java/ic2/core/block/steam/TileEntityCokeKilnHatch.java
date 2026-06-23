package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.ref.Ic2BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCokeKilnHatch extends TileEntityInventory implements IHasGui
{
	protected final InvSlot inventory = new InvSlot(this, "inventory", InvSlot.Access.I, 1, InvSlot.InvSide.ANY);

	public TileEntityCokeKilnHatch(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.COKE_KILN_HATCH, pos, state);
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction side)
	{
		return side != this.getFacing() ? false : super.canPlaceItemThroughFace(index, stack, side);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side)
	{
		return false;
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
