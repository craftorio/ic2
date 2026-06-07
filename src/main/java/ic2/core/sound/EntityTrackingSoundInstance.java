package ic2.core.sound;

import ic2.core.IC2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class EntityTrackingSoundInstance extends EntityBoundSoundInstance implements ListenableSoundInstance
{
	protected Entity entity;
	protected SoundEvent soundEvent;
	protected net.minecraft.client.sounds.SoundManager vanillaManager;
	private Runnable onFinish = null;

	public EntityTrackingSoundInstance(SoundEvent sound, SoundSource category, float volume, float pitch, Entity entity)
	{
		super(sound, category, volume, pitch, entity, IC2.random.m_188505_());
		this.vanillaManager = Minecraft.m_91087_().m_91106_();
		this.f_119578_ = true;
		this.entity = entity;
		this.soundEvent = sound;
	}

	public void playOnce()
	{
		this.entity.m_5496_(this.soundEvent, this.f_119573_, this.f_119574_);
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
		this.onFinish = then;
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
