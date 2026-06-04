package ic2.core.audio;

import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.init.MainConfig;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

@SideOnly(Side.CLIENT)
public final class AudioManagerClient extends AudioManager
{
	public static class WeakObject extends WeakReference<Object>
	{
		public WeakObject(Object referent)
		{
			super(referent);
		}

		public boolean equals(Object object)
		{
			if (object instanceof WeakObject)
				return (((WeakObject) object).get() == get());
			return (get() == object);
		}

		public int hashCode()
		{
			Object object = get();
			if (object == null)
				return 0;
			return object.hashCode();
		}
	}

	public void initialize()
	{
		this.enabled = ConfigUtil.getBool(MainConfig.get(), "audio/enabled");
		this.masterVolume = ConfigUtil.getFloat(MainConfig.get(), "audio/volume");
		this.fadingDistance = ConfigUtil.getFloat(MainConfig.get(), "audio/fadeDistance");
		this.maxSourceCount = ConfigUtil.getInt(MainConfig.get(), "audio/maxSourceCount");
		if (this.maxSourceCount <= 6)
		{
			IC2.log.info(LogCategory.Audio, "The audio source limit is too low to enable IC2 sounds.");
			this.enabled = false;
		}
		if (!this.enabled)
		{
			IC2.log.debug(LogCategory.Audio, "Sounds disabled.");
			return;
		}
		IC2.log.debug(LogCategory.Audio, "Using %d audio sources.", this.maxSourceCount);
		SoundSystemConfig.setNumberStreamingChannels(4);
		SoundSystemConfig.setNumberNormalChannels(this.maxSourceCount - 4);
		this.soundManagerLoaded = ReflectionUtil.getField(SoundManager.class, boolean.class);
		if (this.soundManagerLoaded == null)
		{
			IC2.log.warn(LogCategory.Audio, "Can't find SoundManager.loaded, IC2 audio disabled.");
			this.enabled = false;
			return;
		}
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(AudioConfigHandler.class);
	}

	@SubscribeEvent
	public void onSoundSetup(SoundLoadEvent event)
	{
		if (!this.enabled)
			return;
		for (List<AudioSourceClient> sources : this.objectToAudioSourceMap.values())
		{
			for (AudioSourceClient source : sources)
			{
				if (source.isValid())
					source.setInvalid();
			}
		}
		this.objectToAudioSourceMap.clear();
		Thread thread = this.initThread;
		if (thread != null)
		{
			thread.interrupt();
			try
			{
				thread.join();
			}
            catch (InterruptedException interruptedException)
			{
              
			}
		}
		IC2.log.debug(LogCategory.Audio, "IC2 audio starting.");
		this.soundSystem = null;
		this.soundManager = getSoundManager();
		this.initThread = new Thread(() ->
		{
			try
			{
				while (!Thread.currentThread().isInterrupted())
				{
					boolean loaded;
					try
					{
						loaded = AudioManagerClient.this.soundManagerLoaded.getBoolean(AudioManagerClient.this.soundManager);
					} catch (Exception e)
					{
						throw new RuntimeException(e);
					}
					if (loaded)
					{
						AudioManagerClient.this.soundSystem = AudioManagerClient.getSoundSystem(AudioManagerClient.this.soundManager);
						if (AudioManagerClient.this.soundSystem == null)
						{
							IC2.log.warn(LogCategory.Audio, "IC2 audio unavailable.");
							AudioManagerClient.this.enabled = false;
							break;
						}
						IC2.log.debug(LogCategory.Audio, "IC2 audio ready.");
						break;
					}
					Thread.sleep(100L);
				}
			} catch (InterruptedException loaded)
			{
              
            }
			AudioManagerClient.this.initThread = null;
		}, "IC2 audio init thread");
		this.initThread.setDaemon(true);
		this.initThread.start();
	}

	private static SoundManager getSoundManager()
	{
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		return ReflectionUtil.getValue(handler, SoundManager.class);
	}

	private static SoundSystem getSoundSystem(SoundManager soundManager)
	{
		try
		{
			return ReflectionUtil.getValueRecursive(soundManager, SoundSystem.class, false);
		} catch (NoSuchFieldException e)
		{
			return null;
		}
	}

	public void onTick()
	{
		if (!this.enabled || !valid())
			return;
		assert IC2.platform.isRendering();
		IC2.platform.profilerStartSection("UpdateSourceVolume");
		EntityPlayer player = IC2.platform.getPlayerInstance();
		if (player == null)
		{
			for (List<AudioSourceClient> sources : this.objectToAudioSourceMap.values())
				removeSources(sources);
			this.objectToAudioSourceMap.clear();
			this.singleSoundQueue.clear();
		} else
		{
			boolean isPaused = Minecraft.getMinecraft().isGamePaused();
			if (!isPaused && !this.singleSoundQueue.isEmpty())
			{
				IC2.platform.profilerStartSection("SoundQueuing");
				for (Iterator<Map.Entry<String, FutureSound>> iterator = this.singleSoundQueue.entrySet().iterator(); iterator.hasNext(); )
				{
					Map.Entry<String, FutureSound> entry = iterator.next();
					if (!this.soundSystem.playing(entry.getKey()))
					{
						(entry.getValue()).onFinish();
						iterator.remove();
						continue;
					}
					if ((entry.getValue()).isCancelled())
					{
						removeSource(entry.getKey());
						iterator.remove();
					}
				}
				synchronized (SoundSystemConfig.THREAD_SYNC)
				{
					IC2.platform.profilerEndSection();
				}
			}
			Iterator<Map.Entry<WeakObject, List<AudioSourceClient>>> it;
			for (it = this.objectToAudioSourceMap.entrySet().iterator(); it.hasNext(); )
			{
				Map.Entry<WeakObject, List<AudioSourceClient>> entry = it.next();
				if ((entry.getKey()).get() == null)
				{
					it.remove();
					removeSources(entry.getValue());
					continue;
				}
				for (AudioSource audioSource : entry.getValue())
				{
					if (!this.wasPaused)
						audioSource.updateVolume(player);
					if (audioSource.getRealVolume() > 0.0F)
						this.validAudioSources.add(audioSource);
				}
			}
			IC2.platform.profilerEndStartSection("Culling");
			if (!isPaused)
			{
				if (this.wasPaused)
				{
					for (it = (Iterator) this.validAudioSources.iterator(); it.hasNext(); )
					{
						AudioSource source = (AudioSource) it.next();
						source.play();
					}
					this.wasPaused = false;
				}
				for (int i = 0; !this.validAudioSources.isEmpty(); i++)
				{
					AudioSource source = this.validAudioSources.poll();
					if (i < this.maxSourceCount)
					{
						source.activate();
					} else
					{
						source.cull();
					}
				}
			} else if (isPaused != this.wasPaused)
			{
				this.wasPaused = true;
				while (!this.validAudioSources.isEmpty())
					this.validAudioSources.poll().pause();
			} else
			{
				assert isPaused;
				assert this.wasPaused;
				this.validAudioSources.clear();
			}
		}
		IC2.platform.profilerEndSection();
	}

	public AudioSource createSource(Object obj, String initialSoundFile)
	{
		return createSource(obj, PositionSpec.Center, initialSoundFile, false, false, getDefaultVolume());
	}

	public AudioSource createSource(Object obj, PositionSpec positionSpec, String initialSoundFile, boolean loop, boolean priorized, float volume)
	{
		if (!this.enabled)
			return null;
		if (!valid())
			return null;
		assert IC2.platform.isRendering();
		String sourceName = getSourceName(this.nextId);
		this.nextId++;
		AudioSourceClient audioSource = new AudioSourceClient(this.soundSystem, sourceName, obj, positionSpec, initialSoundFile, loop, priorized, volume);
		audioSource.setup();
		WeakObject key = new WeakObject(obj);
		List<AudioSourceClient> sources = this.objectToAudioSourceMap.get(key);
		if (sources == null)
		{
			sources = new ArrayList<>();
			this.objectToAudioSourceMap.put(key, sources);
		}
		sources.add(audioSource);
		return audioSource;
	}

	static URL getSourceURL(String soundFile)
	{
		int colonIndex = soundFile.indexOf(':');
		if (colonIndex > -1)
			return AudioSource.class.getClassLoader().getResource("assets/" + soundFile.substring(0, colonIndex) + "/sounds/" + soundFile.substring(++colonIndex));
		return AudioSource.class.getClassLoader().getResource("ic2/sounds/" + soundFile);
	}

	public void removeSources(Object obj)
	{
		WeakObject key;
		if (!valid())
			return;
		assert IC2.platform.isRendering();
		if (obj instanceof WeakObject)
		{
			key = (WeakObject) obj;
		} else
		{
			key = new WeakObject(obj);
		}
		List<AudioSourceClient> sources = this.objectToAudioSourceMap.remove(key);
		if (sources == null)
			return;
		removeSources(sources);
	}

	private static void removeSources(List<AudioSourceClient> sources)
	{
		for (AudioSourceClient audioSource : sources)
			audioSource.remove();
	}

	public void playOnce(Object obj, String soundFile)
	{
		playOnce(obj, PositionSpec.Center, soundFile, true, getDefaultVolume());
	}

	public String playOnce(Object obj, PositionSpec positionSpec, String soundFile, boolean priorized, float volume)
	{
		if (!this.enabled)
			return null;
		if (!valid())
			return null;
		assert IC2.platform.isRendering();
		AudioPosition position = AudioPosition.getFrom(obj, positionSpec);
		if (position == null)
			return null;
		URL url = getSourceURL(soundFile);
		if (url == null)
		{
			IC2.log.warn(LogCategory.Audio, "Invalid sound file: %s.", soundFile);
			return null;
		}
		String sourceName = this.soundSystem.quickPlay(priorized, url, soundFile, false, position.x, position.y, position.z, 2, this.fadingDistance * Math.max(volume, 1.0F));
		this.soundSystem.setVolume(sourceName, this.masterVolume * Math.min(volume, 1.0F));
		return sourceName;
	}

	public void chainSource(String source, FutureSound onFinish)
	{
		this.singleSoundQueue.put(source, onFinish);
	}

	public void removeSource(String source)
	{
		if (source != null)
		{
			this.soundSystem.stop(source);
			this.soundSystem.removeSource(source);
		}
	}

	public float getDefaultVolume()
	{
		return 1.2F;
	}

	public float getMasterVolume()
	{
		return this.masterVolume;
	}

	protected boolean valid()
	{
		try
		{
			return (this.soundSystem != null && this.soundManager != null && this.soundManagerLoaded.getBoolean(this.soundManager));
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@SubscribeEvent
	public void onSoundPlayed(PlaySoundEvent event)
	{
		SoundCategory category = event.getSound().getCategory();
		if ((category == SoundCategory.NEUTRAL && event.getName().endsWith(".hit")) || (category == SoundCategory.BLOCKS && event
			.getName().endsWith(".break")))
		{
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			ItemStack stack = player.inventory.getCurrentItem();
			if (stack != null && stack.getItem() instanceof IHitSoundOverride)
			{
				World world = player.getEntityWorld();
				RayTraceResult mop = getMovingObjectPositionFromPlayer(world, player, false);
				BlockPos pos = new BlockPos(event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF());
				if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && pos.equals(mop.getBlockPos()))
				{
					String replace;
					if (event.getSound().getCategory() == SoundCategory.NEUTRAL)
					{
						replace = ((IHitSoundOverride) stack.getItem()).getHitSoundForBlock(player, world, pos, stack);
					} else
					{
						replace = ((IHitSoundOverride) stack.getItem()).getBreakSoundForBlock(player, world, pos, stack);
					}
					if (replace != null)
					{
						event.setResultSound(null);
						if (!replace.isEmpty())
							IC2.platform.playSoundSp(replace, 1.0F, 1.0F);
					}
				}
			}
		}
	}

	private static RayTraceResult getMovingObjectPositionFromPlayer(World worldIn, EntityPlayer playerIn, boolean useLiquids)
	{
		float f = playerIn.rotationPitch;
		float f1 = playerIn.rotationYaw;
		double d0 = playerIn.posX;
		double d1 = playerIn.posY + playerIn.getEyeHeight();
		double d2 = playerIn.posZ;
		Vec3d vec3 = new Vec3d(d0, d1, d2);
		float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
		float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
		float f4 = -MathHelper.cos(-f * 0.017453292F);
		float f5 = MathHelper.sin(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = 5.0D;
		Vec3d vec31 = vec3.addVector(f6 * d3, f5 * d3, f7 * d3);
		return worldIn.rayTraceBlocks(vec3, vec31, useLiquids, !useLiquids, false);
	}

	private static String getSourceName(int id)
	{
		return "asm_snd" + id;
	}

	public float fadingDistance = 16.0F;

	private boolean enabled = true;

	private boolean wasPaused = false;

	private int maxSourceCount = 32;

	private final int streamingSourceCount = 4;

	private SoundManager soundManager;

	private Field soundManagerLoaded;

	private volatile Thread initThread;

	private SoundSystem soundSystem = null;

	float masterVolume = 0.5F;

	private int nextId = 0;

	private final Map<WeakObject, List<AudioSourceClient>> objectToAudioSourceMap = new HashMap<>();

	private final Queue<AudioSource> validAudioSources = new PriorityQueue<>();

	private final Map<String, FutureSound> singleSoundQueue = new HashMap<>();
}
