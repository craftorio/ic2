package ic2.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITerraformingBP
{
	double getConsume(ItemStack paramItemStack);

	int getRange(ItemStack paramItemStack);

	boolean canInsert(ItemStack paramItemStack, EntityPlayer paramEntityPlayer, World paramWorld, BlockPos paramBlockPos);

	boolean terraform(ItemStack paramItemStack, World paramWorld, BlockPos paramBlockPos);
}
