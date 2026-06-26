package ic2.core.block.tileentity;

import ic2.core.IC2;
import ic2.core.sound.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityBase extends TileEntityInventory
{
	protected Sound loopingSound;
	protected Sound subLoopingSound;
	protected Sound startSound;
	protected Sound stopSound;
	protected Sound interruptSound;
	private boolean clientLastActive;

	public TileEntityBase(BlockEntityType<? extends TileEntityInventory> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		// Looping sound playback is driven client-side; on a dedicated server IC2.soundManager is a no-op
		// so loopingSound is null and this restart logic never ran. updateEntityClient now handles persistence.
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		if (this.loopingSound != null)
		{
			if (this.getActive() && !this.loopingSound.isPlaying())
			{
				this.loopingSound.play();
			} else if (!this.getActive() && this.loopingSound.isPlaying())
			{
				this.loopingSound.stop();
			}
		}
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if (field.equals("active") && this.level != null && this.level.isClientSide)
		{
			boolean nowActive = this.getActive();
			if (nowActive != this.clientLastActive)
			{
				this.clientLastActive = nowActive;
				if (nowActive)
				{
					this.startPlaySound(false);
				} else
				{
					this.stopStartSound();
					this.stopLoopingSound();
					this.playStopSound();
				}
			}
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (this.hasSound())
		{
			IC2.soundManager.removeAllSound(this);
			this.clearSound();
		}

		super.onUnloaded();
	}

	@Override
	protected void onLoaded()
	{
		this.initSound();
		super.onLoaded();
	}

	public void setActiveState(boolean active, boolean playSubSound)
	{
		if (active)
		{
			this.activate(playSubSound);
		} else
		{
			this.shutdown(false);
		}
	}

	public void activate(boolean playSubSound)
	{
		if (!this.getActive())
		{
			this.teBlock.setActive(this.level, this.worldPosition, this.getBlockState(), true);
			if (this.level != null && this.level.isClientSide)
			{
				this.startPlaySound(playSubSound);
			}
		}
	}

	public void shutdown(boolean isInterrupted)
	{
		if (this.getActive())
		{
			this.teBlock.setActive(this.level, this.worldPosition, this.getBlockState(), false);
			if (this.level != null && this.level.isClientSide)
			{
				this.stopStartSound();
				this.stopLoopingSound();
				if (isInterrupted)
				{
					this.playInterruptSound();
				} else
				{
					this.playStopSound();
				}
			}
		}
	}

	public void startPlaySound(boolean playSubSound)
	{
		if (this.startSound != null)
		{
			if (this.loopingSound != null)
			{
				this.startSound.onFinish(() -> this.playLoopingSound(playSubSound));
			}

			this.startSound.playOnce();
		} else
		{
			this.playLoopingSound(playSubSound);
		}
	}

	public void playLoopingSound(boolean playSubSound)
	{
		if (this.loopingSound != null)
		{
			this.loopingSound.play();
			if (playSubSound && this.subLoopingSound != null)
			{
				this.subLoopingSound.play();
			}
		}
	}

	public void stopLoopingSound()
	{
		if (this.loopingSound != null)
		{
			this.loopingSound.stop();
		}

		if (this.subLoopingSound != null)
		{
			this.subLoopingSound.stop();
		}

	}

	public void stopStartSound()
	{
		if (this.startSound != null)
		{
			this.startSound.stop();
		}
	}

	public void playStopSound()
	{
		if (this.stopSound != null)
		{
			this.stopSound.playOnce();
		}
	}

	public void playInterruptSound()
	{
		if (this.interruptSound != null)
		{
			this.interruptSound.playOnce();
		}
	}

	protected boolean hasSound()
	{
		return this.startSound != null || this.loopingSound != null || this.stopSound != null || this.interruptSound != null;
	}

	protected void initSound()
	{
		this.updateStartSound();
		this.updateLoopingSound();
		this.updateSubLoopingSound();
		this.updateInterruptSound();
		this.updateStopSound();
	}

	protected void updateStartSound()
	{
		SoundEvent startSoundEvent = this.getStartSoundEvent();
		if (startSoundEvent != null && this.startSound == null)
		{
			this.startSound = IC2.soundManager.createSound(this, startSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
		}
	}

	protected void updateStopSound()
	{
		SoundEvent stopSoundEvent = this.getStopSoundEvent();
		if (stopSoundEvent != null && this.stopSound == null)
		{
			this.stopSound = IC2.soundManager.createSound(this, stopSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
		}
	}

	protected void updateLoopingSound()
	{
		SoundEvent loopingSoundEvent = this.getLoopingSoundEvent();
		if (loopingSoundEvent != null && this.loopingSound == null)
		{
			this.loopingSound = IC2.soundManager.createSound(this, loopingSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
		}
	}

	protected void updateSubLoopingSound()
	{
		SoundEvent loopingSoundEvent = this.getSubLoopingSoundEvent();
		if (loopingSoundEvent != null && this.subLoopingSound == null)
		{
			this.subLoopingSound = IC2.soundManager.createSound(this, loopingSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
		}
	}

	protected void updateInterruptSound()
	{
		SoundEvent interruptSoundEvent = this.getInterruptSoundEvent();
		if (interruptSoundEvent != null && this.interruptSound == null)
		{
			this.interruptSound = IC2.soundManager.createSound(this, interruptSoundEvent, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
		}
	}

	protected void clearSound()
	{
		this.startSound = null;
		this.loopingSound = null;
		this.subLoopingSound = null;
		this.stopSound = null;
		this.interruptSound = null;
	}
	protected boolean isLoopingSoundIdling()
	{
		return this.loopingSound != null && !this.loopingSound.isPlaying();
	}

	public SoundEvent getStartSoundEvent()
	{
		return null;
	}

	public SoundEvent getLoopingSoundEvent()
	{
		return null;
	}

	public SoundEvent getSubLoopingSoundEvent()
	{
		return null;
	}

	public SoundEvent getStopSoundEvent()
	{
		return null;
	}

	public SoundEvent getInterruptSoundEvent()
	{
		return null;
	}
}
