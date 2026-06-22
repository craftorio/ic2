package ic2.core.sound;

import ic2.core.IHitSoundOverride;
import ic2.core.proxy.SideProxyClient;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class SoundManagerClient extends SoundManager
{
	private final Map<SoundManagerClient.WeakObject, List<SoundClient>> objectToSoundMap = new ConcurrentHashMap<>();

	public static SoundInstance onSoundPlayed(SoundInstance sound)
	{
		if (sound == null) return null;
		SoundSource category = sound.getSource();
		String name = sound.getLocation().getPath();
		if (category == SoundSource.BLOCKS && name.endsWith(".hit") || category == SoundSource.BLOCKS && name.endsWith(".break"))
		{
			LocalPlayer player = Minecraft.getInstance().player;
			ItemStack stack = null;
			if (player != null)
			{
				stack = player.getInventory().getSelected();
			}

			if (stack != null && stack.getItem() instanceof IHitSoundOverride hitSoundOverride)
			{
				Level world = player.getCommandSenderWorld();
				BlockHitResult mop = getMovingObjectPositionFromPlayer(world, player);
				BlockPos pos = new BlockPos((int) sound.getX(), (int) sound.getY(), (int) sound.getZ());
				if (mop.getType() == Type.BLOCK && pos.equals(mop.getBlockPos()))
				{
					SoundEvent replaceSound;
					if (name.endsWith(".hit"))
					{
						replaceSound = hitSoundOverride.getHitSoundForBlock(player, world, pos, stack);
					} else
					{
						replaceSound = hitSoundOverride.getBreakSoundForBlock(player, world, pos, stack);
					}

					if (replaceSound != null)
					{
						sound = null;
						world.playSound(player, pos, replaceSound, category, 1.0F, 1.0F);
					}
				}
			}
		}

		return sound;
	}

	private static BlockHitResult getMovingObjectPositionFromPlayer(Level worldIn, Player playerIn)
	{
		float f = playerIn.getXRot();
		float f1 = playerIn.getYRot();
		double d0 = playerIn.getX();
		double d1 = playerIn.getY() + playerIn.getEyeHeight(playerIn.getPose());
		double d2 = playerIn.getZ();
		Vec3 vec3 = new Vec3(d0, d1, d2);
		float f2 = Mth.cos(-f1 * (float) (Math.PI / 180.0) - (float) Math.PI);
		float f3 = Mth.sin(-f1 * (float) (Math.PI / 180.0) - (float) Math.PI);
		float f4 = -Mth.cos(-f * (float) (Math.PI / 180.0));
		float f5 = Mth.sin(-f * (float) (Math.PI / 180.0));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = 5.0;
		Vec3 vec31 = vec3.add(f6 * d3, f5 * d3, f7 * d3);
		return worldIn.clip(new ClipContext(vec3, vec31, Block.OUTLINE, Fluid.NONE, playerIn));
	}

	@Override
	public Sound createSound(Object obj, SoundEvent soundEvent, SoundSource soundCategory, LivingEntity entity, float volume, float pitch)
	{
		super.createSound(obj, soundEvent, soundCategory, entity, volume, pitch);
		return registerSound(obj, new SoundClient(soundEvent, soundCategory, entity, volume, pitch));
	}

	@Override
	public Sound createSound(Object obj, SoundEvent soundEvent, SoundSource soundCategory, BlockPos pos, float volume, float pitch)
	{
		super.createSound(obj, soundEvent, soundCategory, pos, volume, pitch);
		return registerSound(obj, new SoundClient(soundEvent, soundCategory, pos, volume, pitch));
	}

	private Sound registerSound(Object obj, SoundClient soundClient)
	{
		this.objectToSoundMap.computeIfAbsent(new SoundManagerClient.WeakObject(obj), k -> new CopyOnWriteArrayList<>()).add(soundClient);
		return soundClient;
	}

	@Override
	public void playOnce(SoundEvent soundEvent, SoundSource soundCategory, float volume, float pitch, LivingEntity entity)
	{
		super.playOnce(soundEvent, soundCategory, volume, pitch, entity);
		new EntityTrackingSoundInstance(soundEvent, soundCategory, volume, pitch, entity).playOnce();
	}

	@Override
	public void pauseAll()
	{
		super.pauseAll();
		SideProxyClient.mc.getSoundManager().pause();
	}

	@Override
	public void resumeAll()
	{
		super.resumeAll();
		SideProxyClient.mc.getSoundManager().resume();
	}

	@Override
	public void stopAll()
	{
		super.stopAll();
		SideProxyClient.mc.getSoundManager().stop();
	}

	@Override
	public SoundManagerClient.WeakObject stopAll(Object obj)
	{
		super.stopAll(obj);
		SoundManagerClient.WeakObject weakObject = new SoundManagerClient.WeakObject(obj);
		List<SoundClient> list = this.objectToSoundMap.get(weakObject);
		if (list == null)
		{
			return null;
		}

		list.forEach(SoundClient::stop);
		return weakObject;
	}

	@Override
	public void removeAllSound(Object obj)
	{
		super.removeAllSound(obj);
		SoundManagerClient.WeakObject weakObject = this.stopAll(obj);
		if (weakObject != null)
		{
			this.objectToSoundMap.remove(weakObject);
		}
	}

	@Override
	public void removeSound(Object obj, Sound sound)
	{
		super.removeSound(obj, sound);
		sound.stop();
		SoundManagerClient.WeakObject weakObject = new SoundManagerClient.WeakObject(obj);
		this.objectToSoundMap.computeIfPresent(weakObject, (k, list) ->
		{
			list.remove((SoundClient) sound);
			return list;
		});
	}

	@Override
	public void tick()
	{
		super.tick();
		this.objectToSoundMap.forEach((object, soundClientList) -> soundClientList.forEach(SoundClient::tick));
	}

	public static class WeakObject extends WeakReference<Object>
	{
		private final int cachedHashCode;

		public WeakObject(Object referent)
		{
			super(referent);
			this.cachedHashCode = referent.hashCode();
		}

		@Override
		public boolean equals(Object object)
		{
			if (object instanceof SoundManagerClient.WeakObject)
			{
				Object thisRef = this.get();
				return thisRef != null && thisRef == ((SoundManagerClient.WeakObject) object).get();
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return this.cachedHashCode;
		}
	}
}
