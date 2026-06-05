package ic2.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITerraformingBP {
   double getConsume(ItemStack var1);

   int getRange(ItemStack var1);

   boolean canInsert(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4);

   boolean terraform(ItemStack var1, World var2, BlockPos var3);
}
