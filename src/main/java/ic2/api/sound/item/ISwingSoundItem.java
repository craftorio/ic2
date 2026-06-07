package ic2.api.sound.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public interface ISwingSoundItem
{
	SoundEvent getSwingSound(LivingEntity var1, InteractionHand var2);
}
