package ic2.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IMiningDrill
{
	int energyUse(ItemStack var1, Level var2, BlockPos var3, BlockState var4);

	int breakTime(ItemStack var1, Level var2, BlockPos var3, BlockState var4);

	boolean breakBlock(ItemStack var1, Level var2, BlockPos var3, BlockState var4);

	default boolean tryUsePower(ItemStack drill, double amount)
	{
		return ElectricItem.manager.use(drill, amount, null);
	}
}
