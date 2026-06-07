package ic2.core.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class RepeatablePositionedSoundInstance extends PositionedSoundInstance
{
	public RepeatablePositionedSoundInstance(SoundEvent sound, SoundSource category, float volume, float pitch, BlockPos pos)
	{
		super(sound, category, volume, pitch, pos);
		this.f_119578_ = true;
	}
}
