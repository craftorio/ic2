package ic2.core.item.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemIC2Door extends ItemBlockIC2
{
	public ItemIC2Door(Block block)
	{
		super(block);
		this.setMaxStackSize(8);
	}

	public boolean placeBlockAt(
		ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState
	)
	{
		ItemDoor.placeDoor(world, pos, EnumFacing.fromAngle(player.rotationYaw), this.block, false);
		return true;
	}
}
