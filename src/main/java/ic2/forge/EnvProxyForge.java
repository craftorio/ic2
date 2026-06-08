package ic2.forge;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import ic2.api.energy.ProfileEvent;
import ic2.api.event.ExplosionEvent;
import ic2.api.event.RetextureEvent;
import ic2.core.IC2;
import ic2.core.Ic2ItemGroupType;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.item.EnvItemHandler;
import ic2.core.ref.Ic2Items;
import ic2.core.network.GrowingBuffer;
import ic2.core.proxy.EnvProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
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
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class EnvProxyForge implements EnvProxy
{
	static final DeferredRegister<BlockEntityType<?>> blockEntityRegistry = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "ic2");
	static final DeferredRegister<MenuType<?>> screenHandlerRegistry = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "ic2");
	static final DeferredRegister<EntityType<?>> entityRegistry = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "ic2");
	static final DeferredRegister<MobEffect> statusEffectRegistry = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "ic2");
	static List<Runnable> pendingItemRegistrations = new ArrayList<>();
	static List<Runnable> configuredFeatureRegistrations = new ArrayList<>();
	static List<EnvProxyForge.PlacedFeatureRegistration<?>> placedFeatureRegistrations = new ArrayList<>();
	static List<EnvProxyForge.PlacementModifierTypeRegistration> placementModifierTypeRegistrations = new ArrayList<>();
	static final DeferredRegister<FoliagePlacerType<?>> foliagePlacerRegistry = DeferredRegister.create(ForgeRegistries.FOLIAGE_PLACER_TYPES, "ic2");
	static final DeferredRegister<RecipeType<?>> recipeTypeRegistry = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "ic2");
	static final DeferredRegister<RecipeSerializer<?>> recipeSerializerRegistry = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "ic2");
	static HashMap<Item, Integer> burnTimeRecord = new HashMap<>();
	private static final boolean isClient = FMLEnvironment.dist.isClient();

	@Override
	public boolean isClientEnv()
	{
		return isClient;
	}

	@Override
	public boolean isFabricEnv()
	{
		return false;
	}

	@Override
	public boolean isForgeEnv()
	{
		return true;
	}

	@Override
	public MinecraftServer getServer()
	{
		return ServerLifecycleHooks.getCurrentServer();
	}

	@Override
	public void registerBlock(ResourceLocation id, Block block)
	{
		ForgeRegistries.BLOCKS.register(id, block);
	}

	@Override
	public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(ResourceLocation id, BiFunction<BlockPos, BlockState, T> factory, Block... blocks)
	{
		BlockEntityType<T> type = Builder.of(factory::apply, blocks).build(null);
		blockEntityRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public <T extends AbstractContainerMenu> MenuType<T> registerScreenHandler(ResourceLocation id, BiFunction<Integer, Inventory, T> factory)
	{
		MenuType<T> type = new MenuType<>(factory::apply, FeatureFlags.DEFAULT_FLAGS);
		screenHandlerRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public <T extends AbstractContainerMenu> MenuType<T> registerExtendedScreenHandler(
		ResourceLocation id, EnvProxy.ExtendedClientScreenHandlerFactory<T> factory
	)
	{
		MenuType<T> type = IForgeMenuType.create(factory::create);
		screenHandlerRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public void registerItem(ResourceLocation id, Item item)
	{
		ForgeRegistries.ITEMS.register(id, item);
	}

	@Override
	public void registerEntity(ResourceLocation id, EntityType<?> type)
	{
		entityRegistry.register(id.getPath(), () -> type);
	}

	@Override
	public WoodType registerSignType(String name)
	{
		return WoodType.register(new WoodType(name, BlockSetType.OAK));
	}

	@Override
	public void registerStatusEffect(ResourceLocation id, MobEffect effect)
	{
		statusEffectRegistry.register(id.getPath(), () -> effect);
	}

	@Override
	public void registerFlammableBlock(Block block, int burn, int spread)
	{
	}

	@Override
	public SoundEvent registerSoundEvent(String id)
	{
		ResourceLocation identifier = IC2.getIdentifier(id);
		SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(identifier);
		ForgeRegistries.SOUND_EVENTS.register(identifier, soundEvent);
		return soundEvent;
	}

	@Override
	public GameEvent registerGameEvent(String id, int range)
	{
		ResourceLocation identifier = IC2.getIdentifier(id);
		return (GameEvent) Registry.register(BuiltInRegistries.GAME_EVENT, identifier, new GameEvent(identifier.toString(), range));
	}

	@Override
	public <FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> registerConfiguredFeature(
		ResourceLocation id, F feature, FC config
	)
	{
		CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> ret = new CompletableFuture<>();
		configuredFeatureRegistrations.add(() -> ret.complete(null));
		return ret;
	}

	@Override
	public <FC extends FeatureConfiguration> void registerPlacedFeature(
		ResourceLocation id, CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> feature, List<PlacementModifier> modifiers
	)
	{
		placedFeatureRegistrations.add(new EnvProxyForge.PlacedFeatureRegistration<>(id, feature, modifiers, new CompletableFuture<>()));
	}

	@Override
	public void attachPlacedFeatureToBiome(ResourceLocation id, EnvProxy.BiomeSelector selector, Decoration step)
	{
	}

	@Override
	public void registerPlacementModifierType(ResourceLocation id, PlacementModifierType<?> type)
	{
		placementModifierTypeRegistrations.add(new EnvProxyForge.PlacementModifierTypeRegistration(id, type));
	}

	@Override
	public <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(ResourceLocation id, Codec<T> codec)
	{
		FoliagePlacerType<T> type = new FoliagePlacerType(codec);
		foliagePlacerRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public <T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation id)
	{
		RecipeType<T> type = RecipeType.simple(id);
		recipeTypeRegistry.register(id.getPath(), () -> type);
		return type;
	}

	@Override
	public void registerRecipeSerializer(ResourceLocation id, RecipeSerializer<?> serializer)
	{
		recipeSerializerRegistry.register(id.getPath(), () -> serializer);
	}

	@Override
	public void runAfterRegistryInit(Runnable runnable)
	{
		FmlMod.instance.runAfterRegistryInit(runnable);
	}

	@Override
	public CreativeModeTab createItemGroup(ResourceLocation id, Supplier<ItemStack> iconSupplier, Ic2ItemGroupType groupType)
	{
		return CreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + id.getNamespace() + "." + id.getPath()))
			.icon(iconSupplier)
			.displayItems((params, output) ->
			{
				for (Supplier<Item> itemSupplier : Ic2Items.CREATIVE_TAB_ITEMS.get(groupType))
				{
					output.accept(new ItemStack(itemSupplier.get()));
				}
			})
			.build();
	}

	@Override
	public EnvFluidHandler createFluidStackHandler()
	{
		if (this.isClientEnv())
		{
			try
			{
				return (EnvFluidHandler) Class.forName("ic2.forge.ClientEnvFluidHandlerForge").getConstructor().newInstance();
			} catch (ReflectiveOperationException e)
			{
				throw new RuntimeException(e);
			}
		} else
		{
			return new EnvFluidHandlerForge();
		}
	}

	@Override
	public EnvItemHandler createItemHandler()
	{
		return new EnvItemHandlerForge();
	}

	@Override
	public float getBlastResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion)
	{
		return state.getBlock().getExplosionResistance(state, world, pos, explosion);
	}

	@Override
	public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation)
	{
		return state.rotate(world, pos, rotation);
	}

	@Override
	public boolean hasRecipeRemainder(ItemStack stack)
	{
		return stack.getItem().hasCraftingRemainingItem(stack);
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack)
	{
		return stack.getItem().getCraftingRemainingItem(stack);
	}

	@Override
	public void registerBurnTime(ItemLike stack, int value)
	{
		burnTimeRecord.put(stack.asItem(), value);
	}

	@Override
	public int getBurnTime(ItemStack stack)
	{
		return ForgeHooks.getBurnTime(stack, null);
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
		NetworkHooks.openScreen((ServerPlayer) player, factory, data::writeTo);
		return true;
	}

	@Override
	public boolean isFakePlayer(Player entity)
	{
		return entity instanceof FakePlayer;
	}

	@Override
	public Player createFakePlayer(ServerLevel world, GameProfile profile)
	{
		return FakePlayerFactory.get(world, profile);
	}

	@Override
	public void announceProfileLoad(Set<String> loaded, String active)
	{
		MinecraftForge.EVENT_BUS.post(new ProfileEvent.Load(loaded, active));
	}

	@Override
	public void announceProfileSwitch(String from, String to)
	{
		MinecraftForge.EVENT_BUS.post(new ProfileEvent.Switch(from, to));
	}

	@Override
	public boolean announceRetexture(
		Level world,
		BlockPos pos,
		BlockState state,
		Direction side,
		Player player,
		BlockState refState,
		String refVariant,
		Direction refSide,
		int[] refColorMultipliers
	)
	{
		RetextureEvent event = new RetextureEvent(world, pos, state, side, player, refState, refVariant, refSide, refColorMultipliers);
		MinecraftForge.EVENT_BUS.post(event);
		return event.applied;
	}

	@Override
	public boolean announceExplosion(Level world, Entity entity, Vec3 pos, double power, LivingEntity igniter, int radiationRange, double rangeLimit)
	{
		ExplosionEvent event = new ExplosionEvent(world, entity, pos, power, igniter, radiationRange, rangeLimit);
		return !MinecraftForge.EVENT_BUS.post(event);
	}

	static void registerPendingItems()
	{
		for (Runnable r : pendingItemRegistrations)
		{
			r.run();
		}

		pendingItemRegistrations.clear();
	}

	record PlacedFeatureRegistration<FC extends FeatureConfiguration>(
		ResourceLocation id,
		CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> feature,
		List<PlacementModifier> modifiers,
		CompletableFuture<Holder<PlacedFeature>> placedFeature
	)
	{
	}

	record PlacementModifierTypeRegistration(ResourceLocation id, PlacementModifierType<?> type)
	{
	}
}
