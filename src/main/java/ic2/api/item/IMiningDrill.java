package ic2.api.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMiningDrill
{
	int energyUse(ItemStack paramItemStack, World paramWorld, BlockPos paramBlockPos, IBlockState paramIBlockState);

	int breakTime(ItemStack paramItemStack, World paramWorld, BlockPos paramBlockPos, IBlockState paramIBlockState);

	boolean breakBlock(ItemStack paramItemStack, World paramWorld, BlockPos paramBlockPos, IBlockState paramIBlockState);

	default boolean tryUsePower(ItemStack drill, double amount)
	{
		return ElectricItem.manager.use(drill, amount, null);
	}
}
