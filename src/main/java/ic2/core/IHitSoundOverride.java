package ic2.core;


import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHitSoundOverride
{
	@OnlyIn(Dist.CLIENT)
	SoundEvent getHitSoundForBlock(LocalPlayer var1, Level var2, BlockPos var3, ItemStack var4);

	@OnlyIn(Dist.CLIENT)
	SoundEvent getBreakSoundForBlock(LocalPlayer var1, Level var2, BlockPos var3, ItemStack var4);
}
