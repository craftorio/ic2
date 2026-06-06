package ic2.core;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IHitSoundOverride
{
	@SideOnly(Side.CLIENT)
	String getHitSoundForBlock(EntityPlayerSP var1, World var2, BlockPos var3, ItemStack var4);

	@SideOnly(Side.CLIENT)
	String getBreakSoundForBlock(EntityPlayerSP var1, World var2, BlockPos var3, ItemStack var4);
}
