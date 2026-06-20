package ic2.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ITerraformingBP
{
	double getConsume(ItemStack var1);

	int getRange(ItemStack var1);

	boolean canInsert(ItemStack var1, Player var2, Level var3, BlockPos var4);

	boolean terraform(ItemStack var1, Level var2, BlockPos var3);
}
