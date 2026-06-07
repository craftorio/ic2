package ic2.core.proxy;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.item.EnvItemHandler;
import ic2.core.network.GrowingBuffer;
import io.netty.buffer.ByteBuf;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.phys.Vec3;

public interface EnvProxy
{
	boolean isClientEnv();

	boolean isFabricEnv();

	boolean isForgeEnv();

	MinecraftServer getServer();

	void registerBlock(ResourceLocation var1, Block var2);

	<T extends BlockEntity> BlockEntityType<T> registerBlockEntity(ResourceLocation var1, BiFunction<BlockPos, BlockState, T> var2, Block... var3);

	<T extends AbstractContainerMenu> MenuType<T> registerScreenHandler(ResourceLocation var1, BiFunction<Integer, Inventory, T> var2);

	<T extends AbstractContainerMenu> MenuType<T> registerExtendedScreenHandler(ResourceLocation var1, EnvProxy.ExtendedClientScreenHandlerFactory<T> var2);

	void registerItem(ResourceLocation var1, Item var2);

	void registerEntity(ResourceLocation var1, EntityType<?> var2);

	WoodType registerSignType(String var1);

	void registerStatusEffect(ResourceLocation var1, MobEffect var2);

	void registerFlammableBlock(Block var1, int var2, int var3);

	SoundEvent registerSoundEvent(String var1);

	GameEvent registerGameEvent(String var1, int var2);

	<FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> registerConfiguredFeature(
		ResourceLocation var1, F var2, FC var3
	);

	<FC extends FeatureConfiguration> void registerPlacedFeature(
		ResourceLocation var1, CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> var2, List<PlacementModifier> var3
	);

	void attachPlacedFeatureToBiome(ResourceLocation var1, EnvProxy.BiomeSelector var2, Decoration var3);

	void registerPlacementModifierType(ResourceLocation var1, PlacementModifierType<?> var2);

	<T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(ResourceLocation var1, Codec<T> var2);

	<T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation var1);

	void registerRecipeSerializer(ResourceLocation var1, RecipeSerializer<?> var2);

	void runAfterRegistryInit(Runnable var1);

	CreativeModeTab createItemGroup(ResourceLocation var1, Supplier<ItemStack> var2);

	EnvFluidHandler createFluidStackHandler();

	EnvItemHandler createItemHandler();

	float getBlastResistance(BlockState var1, BlockGetter var2, BlockPos var3, Explosion var4);

	BlockState rotate(BlockState var1, LevelAccessor var2, BlockPos var3, Rotation var4);

	boolean hasRecipeRemainder(ItemStack var1);

	ItemStack getRecipeRemainder(ItemStack var1);

	void registerBurnTime(ItemLike var1, int var2);

	int getBurnTime(ItemStack var1);

	boolean biomeHasType(Holder<Biome> var1, EnvProxy.BiomeType var2);

	Collection<EnvProxy.BiomeType> getBiomeTypes(Holder<Biome> var1);

	void announceProfileLoad(Set<String> var1, String var2);

	void announceProfileSwitch(String var1, String var2);

	boolean announceRetexture(Level var1, BlockPos var2, BlockState var3, Direction var4, Player var5, BlockState var6, String var7, Direction var8, int[] var9);

	boolean openHandledScreen(Player var1, MenuProvider var2, GrowingBuffer var3);

	boolean isFakePlayer(Player var1);

	Player createFakePlayer(ServerLevel var1, GameProfile var2);

	boolean announceExplosion(Level var1, Entity var2, Vec3 var3, double var4, LivingEntity var6, int var7, double var8);

	enum BiomeSelector
	{
		OVERWORLD,
		SWAMP,
		JUNGLE,
		FOREST;
	}

	enum BiomeType
	{
		COLD,
		HOT,
		DRY,
		WET,
		DENSE,
		SPARSE,
		RARE,
		PLATEAU,
		MODIFIED,
		END,
		NETHER,
		OVERWORLD,
		BEACH,
		CONIFEROUS,
		DEAD,
		FOREST,
		HILLS,
		JUNGLE,
		LUSH,
		MAGICAL,
		MESA,
		MOUNTAIN,
		MUSHROOM,
		OCEAN,
		PLAINS,
		RIVER,
		SANDY,
		SAVANNA,
		SNOWY,
		SPOOKY,
		SWAMP,
		VOID,
		WASTELAND,
		WATER;
	}

	interface ExtendedClientScreenHandlerFactory<T>
	{
		T create(int var1, Inventory var2, ByteBuf var3);
	}
}
