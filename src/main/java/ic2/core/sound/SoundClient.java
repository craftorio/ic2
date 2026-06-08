package ic2.core.sound;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public class SoundClient extends Sound
{
	private RepeatablePositionedSoundInstance repeatInstance = null;
	private PositionedSoundInstance onceInstance = null;
	private EntityTrackingSoundInstance entityTrackingInstance = null;
	private final List<SoundInstance> startedSoundList = new ArrayList<>();
	private boolean isStarted = false;
	public net.minecraft.client.sounds.SoundManager vanillaManager = Minecraft.getInstance().getSoundManager();

	protected SoundClient()
	{
	}

	public SoundClient(SoundEvent soundEvent, SoundSource soundCategory, BlockPos pos, float volume, float pitch)
	{
		this();
		this.repeatInstance = new RepeatablePositionedSoundInstance(soundEvent, soundCategory, volume, pitch, pos);
		this.onceInstance = new PositionedSoundInstance(soundEvent, soundCategory, volume, pitch, pos);
	}

	public SoundClient(SoundEvent soundEvent, SoundSource soundCategory, LivingEntity entity, float volume, float pitch)
	{
		this();
		this.entityTrackingInstance = new EntityTrackingSoundInstance(soundEvent, soundCategory, volume, pitch, entity);
	}

	@Override
	public void play()
	{
		super.play();
		if (this.repeatInstance != null && !this.vanillaManager.isActive(this.repeatInstance))
		{
			if (this.vanillaManager.isActive(this.onceInstance))
			{
				this.vanillaManager.stop(this.onceInstance);
			}

			this.vanillaManager.play(this.repeatInstance);
			this.startedSoundList.add(this.repeatInstance);
		}

		if (this.entityTrackingInstance != null && !this.vanillaManager.isActive(this.entityTrackingInstance))
		{
			this.vanillaManager.play(this.entityTrackingInstance);
			this.startedSoundList.add(this.entityTrackingInstance);
		}

		this.isStarted = true;
	}

	@Override
	public void playOnce()
	{
		super.playOnce();
		if (this.onceInstance != null)
		{
			this.vanillaManager.play(this.onceInstance);
			this.startedSoundList.add(this.onceInstance);
		}

		if (this.entityTrackingInstance != null)
		{
			this.entityTrackingInstance.playOnce();
			this.startedSoundList.add(this.entityTrackingInstance);
		}

		this.isStarted = true;
	}

	@Override
	public void stop()
	{
		super.stop();
		this.isStarted = false;
		this.vanillaManager.stop(this.repeatInstance);
		this.vanillaManager.stop(this.onceInstance);
		this.vanillaManager.stop(this.entityTrackingInstance);
	}

	private boolean isPlayingSound(SoundInstance instance)
	{
		return instance != null && this.vanillaManager.isActive(instance);
	}

	@Override
	public boolean isPlaying()
	{
		return this.isPlayingSound(this.onceInstance) || this.isPlayingSound(this.repeatInstance) || this.isPlayingSound(this.entityTrackingInstance);
	}

	private void addOnFinishListener(ListenableSoundInstance instance, Runnable then)
	{
		if (instance != null)
		{
			instance.addOnFinishListener(then);
		}
	}

	private void onFinishSound(ListenableSoundInstance instance, Runnable then)
	{
		if (instance != null)
		{
			instance.onFinish(then);
		}
	}

	private void checkSoundFinished(SoundInstance instance)
	{
		if (instance instanceof ListenableSoundInstance listenableSoundInstance)
		{
			if (!this.vanillaManager.isActive(instance) && this.startedSoundList.contains(instance))
			{
				listenableSoundInstance.finish();
				this.startedSoundList.remove(instance);
			}
		}
	}

	@Override
	public void addOnFinishListener(Runnable then)
	{
		this.addOnFinishListener(this.onceInstance, then);
		this.addOnFinishListener(this.repeatInstance, then);
		this.addOnFinishListener(this.entityTrackingInstance, then);
	}

	@Override
	public void onFinish(Runnable then)
	{
		this.onFinishSound(this.onceInstance, then);
		this.onFinishSound(this.repeatInstance, then);
		this.onFinishSound(this.entityTrackingInstance, then);
	}

	@Override
	public void tick()
	{
		super.tick();
		if (this.isStarted)
		{
			this.checkSoundFinished(this.onceInstance);
			this.checkSoundFinished(this.repeatInstance);
			this.checkSoundFinished(this.entityTrackingInstance);
			if (this.startedSoundList.isEmpty())
			{
				this.isStarted = false;
			}
		}
	}
}
