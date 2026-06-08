package ic2.core.util;

import ic2.core.IC2;
import ic2.core.proxy.EnvProxy;

import java.lang.reflect.Field;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

public final class BiomeUtil
{
	private static final Field field_ChunkSection_biomeContainer = ReflectionUtil.getField(LevelChunkSection.class, "biomeContainer", "field_34556", "biomes");

	public static Biome getBiome(Level world, ResourceKey<Biome> key)
	{
		return (Biome) world.registryAccess().registryOrThrow(Registries.BIOME).get(key);
	}

	public static Holder<Biome> getOriginalBiome(LevelReader world, BlockPos pos)
	{
		return world.getUncachedNoiseBiome(pos.getX(), pos.getY(), pos.getZ());
	}

	public static Holder<Biome> getBiome(LevelReader world, BlockPos pos)
	{
		return world.getBiome(pos);
	}

	public static void setBiome(LevelReader world, BlockPos pos, Biome biome)
	{
		Objects.requireNonNull(biome, "null biome");
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		ChunkAccess chunk = world.getChunk(x, z, ChunkStatus.BIOMES);

		for (LevelChunkSection section : chunk.getSections())
		{
			PalettedContainer<Biome> biomeContainer = ReflectionUtil.getFieldValue(field_ChunkSection_biomeContainer, section);

			for (int y = 0; y < 3; y++)
			{
				biomeContainer.set(x >> 2, y, z >> 2, biome);
			}
		}
	}

	public static void setBiomeAndNotify(Level world, BlockPos pos, Biome biome)
	{
		setBiome(world, pos, biome);
		ChunkSource chunkManager = world.getChunkSource();
		if (chunkManager instanceof ServerChunkCache)
		{
			LevelChunk chunk = world.getChunkAt(pos);
			ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
				chunk, ((ServerLevel) world).getChunkSource().getLightEngine(), null, null
			);
			((ServerChunkCache) chunkManager).chunkMap.getPlayers(chunk.getPos(), false).forEach(player -> player.connection.send(packet));
			chunk.setUnsaved(true);
		} else
		{
			assert !world.isClientSide : "Can't notify a server of a client side biome change";
			world.getChunkAt(pos).setUnsaved(true);
		}
	}

	public static int getBiomeTemperature(Level world, BlockPos pos)
	{
		Holder<Biome> biome = getBiome(world, pos);
		if (IC2.envProxy.biomeHasType(biome, EnvProxy.BiomeType.HOT))
		{
			return 45;
		} else
		{
			return IC2.envProxy.biomeHasType(biome, EnvProxy.BiomeType.COLD) ? 0 : 25;
		}
	}
}
