package ic2.core.audio;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;

@SideOnly(Side.CLIENT)
final class AudioSourceClient extends AudioSource implements Comparable<AudioSourceClient>
{
	private final SoundSystem soundSystem;
	private final String sourceName;
	private boolean valid = false;
	private boolean culled = false;
	private final String initialSoundFile;
	private final boolean loop;
	private final boolean prioritized;
	private final Reference<Object> obj;
	private AudioPosition position;
	private final PositionSpec positionSpec;
	private float configuredVolume;
	private float realVolume;
	private boolean isPlaying = false;

	AudioSourceClient(
		SoundSystem soundSystem,
		String sourceName,
		Object obj,
		PositionSpec positionSpec,
		String initialSoundFile,
		boolean loop,
		boolean prioritized,
		float volume
	)
	{
		this.soundSystem = soundSystem;
		this.sourceName = sourceName;
		this.initialSoundFile = initialSoundFile;
		this.loop = loop;
		this.prioritized = prioritized;
		this.obj = new WeakReference<>(obj);
		this.position = AudioPosition.getFrom(obj, positionSpec);
		this.positionSpec = positionSpec;
		this.configuredVolume = volume;
	}

	void setup()
	{
		if (this.valid)
		{
			throw new IllegalStateException("already initialized");
		}

		URL url = AudioManagerClient.getSourceURL(this.initialSoundFile);
		if (url == null)
		{
			IC2.log.warn(LogCategory.Audio, "Invalid sound file: %s.", this.initialSoundFile);
		} else
		{
			this.soundSystem
				.newSource(
					this.prioritized,
					this.sourceName,
					url,
					this.initialSoundFile,
					this.loop,
					this.position.x,
					this.position.y,
					this.position.z,
					0,
					((AudioManagerClient) IC2.audioManager).fadingDistance * Math.max(this.configuredVolume, 1.0F)
				);
			this.valid = true;
			this.setVolume(this.configuredVolume);
		}
	}

	public int compareTo(AudioSourceClient x)
	{
		return this.culled ? (int) ((this.realVolume * 0.9F - x.realVolume) * 128.0F) : (int) ((this.realVolume - x.realVolume) * 128.0F);
	}

	@Override
	public void remove()
	{
		if (this.check())
		{
			this.stop();
			this.soundSystem.removeSource(this.sourceName);
			this.setInvalid();
		}
	}

	boolean isValid()
	{
		return this.valid;
	}

	void setInvalid()
	{
		this.valid = false;
	}

	@Override
	public void play()
	{
		if (this.check())
		{
			if (!this.isPlaying)
			{
				this.isPlaying = true;
				if (!this.culled)
				{
					this.soundSystem.play(this.sourceName);
				}
			}
		}
	}

	@Override
	public void pause()
	{
		if (this.check())
		{
			if (this.isPlaying && !this.culled)
			{
				this.isPlaying = false;
				this.soundSystem.pause(this.sourceName);
			}
		}
	}

	@Override
	public void stop()
	{
		if (this.check() && this.isPlaying)
		{
			this.isPlaying = false;
			if (!this.culled)
			{
				this.soundSystem.stop(this.sourceName);
			}
		}
	}

	@Override
	public void flush()
	{
		if (this.check())
		{
			if (this.isPlaying && !this.culled)
			{
				this.soundSystem.flush(this.sourceName);
			}
		}
	}

	@Override
	public void cull()
	{
		if (this.check() && !this.culled)
		{
			this.soundSystem.cull(this.sourceName);
			this.culled = true;
		}
	}

	@Override
	public void activate()
	{
		if (this.check() && this.culled)
		{
			this.soundSystem.activate(this.sourceName);
			this.culled = false;
			if (this.isPlaying)
			{
				this.isPlaying = false;
				this.play();
			}
		}
	}

	@Override
	public float getVolume()
	{
		return !this.check() ? 0.0F : this.soundSystem.getVolume(this.sourceName);
	}

	@Override
	public float getRealVolume()
	{
		return this.realVolume;
	}

	@Override
	public void setVolume(float volume)
	{
		if (this.check())
		{
			this.configuredVolume = volume;
			this.soundSystem.setVolume(this.sourceName, IC2.audioManager.getMasterVolume() * Math.min(volume, 1.0F));
		}
	}

	@Override
	public void setPitch(float pitch)
	{
		if (this.check())
		{
			this.soundSystem.setPitch(this.sourceName, pitch);
		}
	}

	@Override
	public void updatePosition()
	{
		if (this.check())
		{
			this.position = AudioPosition.getFrom(this.obj.get(), this.positionSpec);
			if (this.position != null)
			{
				this.soundSystem.setPosition(this.sourceName, this.position.x, this.position.y, this.position.z);
			}
		}
	}

	@Override
	public void updateVolume(EntityPlayer player)
	{
		if (this.check() && this.isPlaying)
		{
			float maxDistance = ((AudioManagerClient) IC2.audioManager).fadingDistance * Math.max(this.configuredVolume, 1.0F);
			float rolloffFactor = 1.0F;
			float referenceDistance = 1.0F;
			World world = player.getEntityWorld();
			float x = (float) player.posX;
			float y = (float) player.posY;
			float z = (float) player.posZ;
			float distance;
			if (this.position != null && this.position.getWorld() == world)
			{
				float deltaX = this.position.x - x;
				float deltaY = this.position.y - y;
				float deltaZ = this.position.z - z;
				distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
			} else
			{
				distance = Float.POSITIVE_INFINITY;
			}

			if (distance > maxDistance)
			{
				this.realVolume = 0.0F;
				this.cull();
			} else
			{
				if (distance < referenceDistance)
				{
					distance = referenceDistance;
				}

				float gain = 1.0F - rolloffFactor * (distance - referenceDistance) / (maxDistance - referenceDistance);
				float newRealVolume = gain * this.configuredVolume * IC2.audioManager.getMasterVolume();
				float dx = (this.position.x - x) / distance;
				float dy = (this.position.y - y) / distance;
				float dz = (this.position.z - z) / distance;
				if (newRealVolume > 0.1)
				{
					for (int i = 0; i < distance; i++)
					{
						BlockPos pos = new BlockPos(Util.roundToNegInf(x), Util.roundToNegInf(y), Util.roundToNegInf(z));
						IBlockState state = world.getBlockState(pos);
						Block block = state.getBlock();
						if (!block.isAir(state, world, pos))
						{
							if (block.isNormalCube(state, world, pos))
							{
								newRealVolume *= 0.6F;
							} else
							{
								newRealVolume *= 0.8F;
							}
						}

						x += dx;
						y += dy;
						z += dz;
					}
				}

				if (Math.abs(this.realVolume / newRealVolume - 1.0F) > 0.06)
				{
					this.soundSystem.setVolume(this.sourceName, Math.min(newRealVolume, 1.0F));
				}

				this.realVolume = newRealVolume;
			}
		} else
		{
			this.realVolume = 0.0F;
		}
	}

	private boolean check()
	{
		if (this.valid && IC2.audioManager.valid())
		{
			return true;
		}

		this.valid = false;
		return false;
	}
}
