package ic2.core.sound;

import ic2.core.IC2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public class EntityTrackingSoundInstance extends EntityBoundSoundInstance implements ListenableSoundInstance
{
	protected Entity entity;
	protected SoundEvent soundEvent;
	protected net.minecraft.client.sounds.SoundManager vanillaManager;
	private Runnable onFinish = null;
	private Item sourceItem = null;

	public EntityTrackingSoundInstance(SoundEvent sound, SoundSource category, float volume, float pitch, Entity entity)
	{
		super(sound, category, volume, pitch, entity, IC2.random.nextLong());
		this.vanillaManager = Minecraft.getInstance().getSoundManager();
		this.looping = true;
		this.entity = entity;
		this.soundEvent = sound;
	}

	public void setSourceItem(Item item)
	{
		this.sourceItem = item;
	}

	@Override
	public void tick()
	{
		super.tick();
		if (this.sourceItem != null && this.entity instanceof LivingEntity livingEntity)
		{
			if (livingEntity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() != this.sourceItem
					&& livingEntity.getItemBySlot(EquipmentSlot.OFFHAND).getItem() != this.sourceItem)
			{
				this.stop();
			}
		}
	}

	public void playOnce()
	{
		this.entity.playSound(this.soundEvent, this.volume, this.pitch);
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
