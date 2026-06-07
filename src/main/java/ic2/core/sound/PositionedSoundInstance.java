package ic2.core.sound;

import ic2.core.IC2;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class PositionedSoundInstance extends SimpleSoundInstance implements ListenableSoundInstance
{
	private Runnable onFinish = null;

	public PositionedSoundInstance(SoundEvent sound, SoundSource category, float volume, float pitch, BlockPos pos)
	{
		super(sound, category, volume, pitch, IC2.random, pos);
	}

	@Override
	public void addOnFinishListener(Runnable then)
	{
		this.onFinish = this.onFinish == null ? then : () ->
		{
			this.onFinish.run();
			then.run();
		};
	}

	@Override
	public void onFinish(Runnable then)
	{
		this.onFinish = null;
	}

	@Override
	public void clearOnFinishListener()
	{
		this.onFinish = null;
	}

	@Override
	public void finish()
	{
		if (this.onFinish != null)
		{
			this.onFinish.run();
		}
	}
}
