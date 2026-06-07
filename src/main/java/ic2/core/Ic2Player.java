package ic2.core;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import ic2.core.util.Util;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Ic2Player
{
	public static Player get(Level world)
	{
		return world instanceof ServerLevel ? IC2.envProxy.createFakePlayer((ServerLevel) world, getGameProfile(Util.getDimId(world))) : null;
	}

	private static GameProfile getGameProfile(ResourceLocation dim)
	{
		String name = "[IC2 " + dim + "]";
		UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8));
		return new GameProfile(uuid, name);
	}
}
