package ic2.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IEnhancedOverlayProvider
{
	boolean providesEnhancedOverlay(Level var1, BlockPos var2, Direction var3, Player var4, ItemStack var5);
}
