package ic2.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import ic2.core.Ic2ItemGroupType;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.item.EnvItemHandler;
import ic2.core.network.GrowingBuffer;
import ic2.core.proxy.EnvProxy;

import java.util.Collection;
import java.util.Collections;
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

public final class EnvProxyFabric implements EnvProxy
{
	public void init()
	{
	}

	@Override
	public boolean isClientEnv()
	{
		try
		{
			Class.forName("net.minecraft.client.Minecraft");
			return true;
		}
		catch (ClassNotFoundException e)
		{
			return false;
		}
	}

	@Override
	public boolean isFabricEnv()
	{
		return true;
	}

	@Override
	public boolean isForgeEnv()
	{
		return false;
	}

	@Override
	public MinecraftServer getServer()
	{
		return null;
	}

	@Override
	public void registerBlock(ResourceLocation id, Block block)
	{
	}

	@Override
	public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(ResourceLocation id, BiFunction<BlockPos, BlockState, T> factory, Block... blocks)
	{
		return null;
	}

	@Override
	public <T extends AbstractContainerMenu> MenuType<T> registerScreenHandler(ResourceLocation id, BiFunction<Integer, Inventory, T> factory)
	{
		return null;
	}

	@Override
	public <T extends AbstractContainerMenu> MenuType<T> registerExtendedScreenHandler(ResourceLocation id, EnvProxy.ExtendedClientScreenHandlerFactory<T> factory)
	{
		return null;
	}

	@Override
	public void registerItem(ResourceLocation id, Item item)
	{
	}

	@Override
	public void registerEntity(ResourceLocation id, EntityType<?> type)
	{
	}

	@Override
	public WoodType registerSignType(String name)
	{
		return null;
	}

	@Override
	public void registerStatusEffect(ResourceLocation id, MobEffect effect)
	{
	}

	@Override
	public void registerFlammableBlock(Block block, int burn, int spread)
	{
	}

	@Override
	public SoundEvent registerSoundEvent(String id)
	{
		return null;
	}

	@Override
	public GameEvent registerGameEvent(String id, int range)
	{
		return null;
	}

	@Override
	public <FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> registerConfiguredFeature(ResourceLocation id, F feature, FC config)
	{
		return null;
	}

	@Override
	public <FC extends FeatureConfiguration> void registerPlacedFeature(ResourceLocation id, CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> feature, List<PlacementModifier> modifiers)
	{
	}

	@Override
	public void attachPlacedFeatureToBiome(ResourceLocation id, EnvProxy.BiomeSelector selector, Decoration step)
	{
	}

	@Override
	public void registerPlacementModifierType(ResourceLocation id, PlacementModifierType<?> type)
	{
	}

	@Override
	public <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(ResourceLocation id, Codec<T> codec)
	{
		return null;
	}

	@Override
	public <T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation id)
	{
		return null;
	}

	@Override
	public void registerRecipeSerializer(ResourceLocation id, RecipeSerializer<?> serializer)
	{
	}

	@Override
	public void runAfterRegistryInit(Runnable runnable)
	{
	}

	@Override
	public CreativeModeTab createItemGroup(ResourceLocation id, Supplier<ItemStack> iconSupplier, Ic2ItemGroupType groupType)
	{
		return null;
	}

	@Override
	public EnvFluidHandler createFluidStackHandler()
	{
		return null;
	}

	@Override
	public EnvItemHandler createItemHandler()
	{
		return null;
	}

	@Override
	public float getBlastResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion)
	{
		return 0.0F;
	}

	@Override
	public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation)
	{
		return state;
	}

	@Override
	public boolean hasRecipeRemainder(ItemStack stack)
	{
		return false;
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void registerBurnTime(ItemLike stack, int value)
	{
	}

	@Override
	public int getBurnTime(ItemStack stack)
	{
		return 0;
	}

	@Override
	public boolean biomeHasType(Holder<Biome> biome, EnvProxy.BiomeType type)
	{
		return false;
	}

	@Override
	public Collection<EnvProxy.BiomeType> getBiomeTypes(Holder<Biome> biome)
	{
		return Collections.emptySet();
	}

	@Override
	public boolean openHandledScreen(Player player, MenuProvider factory, GrowingBuffer data)
	{
		return false;
	}

	@Override
	public boolean isFakePlayer(Player entity)
	{
		return false;
	}

	@Override
	public Player createFakePlayer(ServerLevel world, GameProfile profile)
	{
		return null;
	}

	@Override
	public void announceProfileLoad(Set<String> loaded, String active)
	{
	}

	@Override
	public void announceProfileSwitch(String from, String to)
	{
	}

	@Override
	public boolean announceRetexture(Level world, BlockPos pos, BlockState state, Direction side, Player player, BlockState refState, String refVariant, Direction refSide, int[] refColorMultipliers)
	{
		return false;
	}

	@Override
	public boolean announceExplosion(Level world, Entity entity, Vec3 pos, double power, LivingEntity igniter, int radiationRange, double rangeLimit)
	{
		return false;
	}
}
