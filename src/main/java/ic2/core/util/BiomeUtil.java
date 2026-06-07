package ic2.core.util;

import ic2.core.IC2;
import ic2.core.proxy.EnvProxy;

import java.lang.reflect.Field;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
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
	private static final Field field_ChunkSection_biomeContainer = ReflectionUtil.getField(LevelChunkSection.class, "biomeContainer", "field_34556", "f_187995_");

	public static Biome getBiome(Level world, ResourceKey<Biome> key)
	{
		return (Biome) world.m_5962_().m_175515_(Registry.f_122885_).m_6246_(key);
	}

	public static Holder<Biome> getOriginalBiome(LevelReader world, BlockPos pos)
	{
		return world.m_203675_(pos.getX(), pos.getY(), pos.getZ());
	}

	public static Holder<Biome> getBiome(LevelReader world, BlockPos pos)
	{
		return world.m_204166_(pos);
	}

	public static void setBiome(LevelReader world, BlockPos pos, Biome biome)
	{
		Objects.requireNonNull(biome, "null biome");
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		ChunkAccess chunk = world.m_46819_(x, z, ChunkStatus.f_62317_);

		for (LevelChunkSection section : chunk.m_7103_())
		{
			PalettedContainer<Biome> biomeContainer = ReflectionUtil.getFieldValue(field_ChunkSection_biomeContainer, section);

			for (int y = 0; y < 3; y++)
			{
				biomeContainer.m_156470_(x >> 2, y, z >> 2, biome);
			}
		}
	}

	public static void setBiomeAndNotify(Level world, BlockPos pos, Biome biome)
	{
		setBiome(world, pos, biome);
		ChunkSource chunkManager = world.m_7726_();
		if (chunkManager instanceof ServerChunkCache)
		{
			LevelChunk chunk = world.m_46745_(pos);
			ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
				chunk, ((ServerLevel) world).m_7726_().m_7827_(), null, null, true
			);
			((ServerChunkCache) chunkManager).f_8325_.m_183262_(chunk.m_7697_(), false).forEach(player -> player.f_8906_.m_9829_(packet));
			chunk.m_8092_(true);
		} else
		{
			assert !world.isClientSide : "Can't notify a server of a client side biome change";
			world.m_46745_(pos).m_8092_(true);
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
