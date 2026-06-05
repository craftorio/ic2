package ic2.api.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMiningDrill {
   int energyUse(ItemStack var1, World var2, BlockPos var3, IBlockState var4);

   int breakTime(ItemStack var1, World var2, BlockPos var3, IBlockState var4);

   boolean breakBlock(ItemStack var1, World var2, BlockPos var3, IBlockState var4);

   default boolean tryUsePower(ItemStack drill, double amount) {
      return ElectricItem.manager.use(drill, amount, null);
   }
}
