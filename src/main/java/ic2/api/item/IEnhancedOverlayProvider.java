package ic2.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEnhancedOverlayProvider {
   boolean providesEnhancedOverlay(World var1, BlockPos var2, EnumFacing var3, EntityPlayer var4, ItemStack var5);
}
