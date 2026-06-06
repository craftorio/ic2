package ic2.core.item.block;

import ic2.core.block.BlockSheet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockSheet extends ItemBlockMulti
{
	public ItemBlockSheet(Block block)
	{
		super(block);
	}

	public boolean placeBlockAt(
		ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState
	)
	{
		return !((BlockSheet) this.block).canReplace(world, pos, side, stack)
			? false
			: super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
	}
}
