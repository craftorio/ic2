package ic2.forge;

import ic2.core.event.EventHandler;
import ic2.core.network.NetworkManager;
import ic2.core.ref.Ic2Fluids;
import ic2.data.Ic2DataGenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod("ic2")
public final class FmlMod
{
	private List<Runnable> toRunAfterRegistryInit = new ArrayList<>();
	public static FmlMod instance;
	public static ExistingFileHelper existingFileHelper;
	private static final AtomicInteger loadState = new AtomicInteger();
	private final FMLJavaModLoadingContext ctx;

	public FmlMod(FMLJavaModLoadingContext ctx)
	{
		instance = this;
		this.ctx = ctx;
		IEventBus modEventBus = this.ctx.getModEventBus();
		modEventBus.register(this);
		EnvProxyForge.blockEntityRegistry.register(modEventBus);
		EnvProxyForge.creativeTabRegistry.register(modEventBus);
		EnvProxyForge.entityRegistry.register(modEventBus);
		EnvProxyForge.screenHandlerRegistry.register(modEventBus);
		EnvProxyForge.statusEffectRegistry.register(modEventBus);
		EnvProxyForge.foliagePlacerRegistry.register(modEventBus);
		EnvProxyForge.recipeTypeRegistry.register(modEventBus);
		EnvProxyForge.recipeSerializerRegistry.register(modEventBus);
		EnvFluidHandlerForge.fluidRegistry.register(modEventBus);
		EnvFluidHandlerForge.fluidTypeRegistry.register(modEventBus);
		if (FMLEnvironment.dist.isClient())
		{
			modEventBus.register(new ClientModEventHandlerForge());
		}

		Ic2Fluids.init();
	}


	@SubscribeEvent
	public void load(FMLCommonSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
		if (FMLEnvironment.dist.isClient())
		{
			MinecraftForge.EVENT_BUS.register(new ClientEventHandlerForge());
		}

		NetworkRegistry.newEventChannel(NetworkManager.channelId, () -> "0", v -> true, v -> true).registerObject(new ForgeNetworkHandler());
		this.ctx
			.registerExtensionPoint(
				DisplayTest.class,
				() -> new DisplayTest(
					() -> "OH, NO!",
					(in, net) -> true
				)
			);
		if (!loadState.compareAndSet(1, 2))
		{
			throw new IllegalStateException();
		}

		EventHandler.onInit();
	}

	@SubscribeEvent
	public void init(FMLLoadCompleteEvent event)
	{
		if (!loadState.compareAndSet(2, 3))
		{
			throw new IllegalStateException();
		}

		EventHandler.onInitLate();
	}

	@SubscribeEvent
	public void initData(GatherDataEvent event)
	{
		existingFileHelper = event.getExistingFileHelper();
		Ic2DataGenerators.setup(event.getGenerator(), event.getLookupProvider(), event.getExistingFileHelper());
	}

	@SubscribeEvent
	public void registerFluidTypes(RegisterEvent event)
	{
		if (event.getRegistryKey() == ForgeRegistries.Keys.FLUID_TYPES)
		{
			EnvFluidHandlerForge.registerPendingFluidTypes();
		}
	}

	@SubscribeEvent
	public void registerFluids(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.FLUID)
		{
			EnvFluidHandlerForge.registerPendingFluids();
		}
	}

	@SubscribeEvent
	public void registerItems(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.ITEM)
		{
			EnvProxyForge.registerPendingItems();
		}
	}

	@SubscribeEvent
	public void registerBlocks(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.BLOCK)
		{
			if (!loadState.compareAndSet(0, 1))
			{
				throw new IllegalStateException();
			}

			EventHandler.onInitEarly();
		}
	}

	@SubscribeEvent
	public void registerGameEvents(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.SOUND_EVENT)
		{
			EventHandler.onInitGameEvents();
		}
	}

	@SubscribeEvent
	public void registerLate(RegisterEvent event)
	{
		if (event.getRegistryKey() == ForgeRegistries.Keys.HOLDER_SET_TYPES)
		{
			for (Runnable runnable : this.toRunAfterRegistryInit)
			{
				runnable.run();
			}

			this.toRunAfterRegistryInit = null;
		}
	}

	void runAfterRegistryInit(Runnable runnable)
	{
		if (loadState.get() > 1)
		{
			runnable.run();
		} else
		{
			this.toRunAfterRegistryInit.add(runnable);
		}
	}

	@SubscribeEvent
	public void registerFeatures(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registries.CONFIGURED_FEATURE)
		{
			for (EnvProxyForge.ConfiguredFeatureRegistration<?, ?> reg : EnvProxyForge.configuredFeatureRegistrations)
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				ConfiguredFeature<?, ?> cf = new ConfiguredFeature(reg.feature(), reg.config());
				event.register(Registries.CONFIGURED_FEATURE, reg.id(), () -> cf);
			}
		} else if (event.getRegistryKey() == Registries.FEATURE)
		{
			for (EnvProxyForge.PlacedFeatureRegistration<?> reg : EnvProxyForge.placedFeatureRegistrations)
			{
				ResourceKey<PlacedFeature> placedKey = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse(reg.id().toString()));
				// TODO: Fix for 1.20.1 - reg.placedFeature().complete(Registry.registerForHolder(ForgeRegistries.PLACED_FEATURES, placedKey, new PlacedFeature(reg.feature().join().value(), reg.modifiers())));
			}

			for (EnvProxyForge.PlacementModifierTypeRegistration reg : EnvProxyForge.placementModifierTypeRegistrations)
			{
				// TODO: Fix for 1.20.1 - Registry.register(ForgeRegistries.PLACEMENT_MODIFIER_TYPES, reg.id(), reg.type());
			}
		}
	}
}
