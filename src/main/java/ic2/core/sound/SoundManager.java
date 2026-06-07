package ic2.core.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public class SoundManager
{
	public Sound createSound(Object obj, SoundEvent soundEvent, SoundSource soundCategory, LivingEntity entity, float volume, float pitch)
	{
		return null;
	}

	public Sound createSound(Object obj, SoundEvent soundEvent, SoundSource soundCategory, BlockPos pos, float volume, float pitch)
	{
		return null;
	}

	public void playOnce(SoundEvent soundEvent, SoundSource soundCategory, float volume, float pitch, LivingEntity entity)
	{
	}

	public void pauseAll()
	{
	}

	public void resumeAll()
	{
	}

	public void stopAll()
	{
	}

	public SoundManagerClient.WeakObject stopAll(Object obj)
	{
		return null;
	}

	public void removeAllSound(Object obj)
	{
	}

	public void removeSound(Object obj, Sound sound)
	{
	}

	public void tick()
	{
	}
}
