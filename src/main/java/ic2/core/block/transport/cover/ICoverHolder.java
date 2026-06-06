package ic2.core.block.transport.cover;

import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICoverHolder
{
	Set<CoverProperty> getCoverProperties();

	boolean canPlaceCover(World var1, BlockPos var2, EnumFacing var3, ItemStack var4);

	void placeCover(World var1, BlockPos var2, EnumFacing var3, ItemStack var4);

	boolean canRemoveCover(World var1, BlockPos var2, EnumFacing var3);

	void removeCover(World var1, BlockPos var2, EnumFacing var3);
}
