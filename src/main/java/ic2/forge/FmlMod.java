package ic2.forge;

import ic2.core.event.EventHandler;
import ic2.core.network.NetworkManager;
import ic2.data.Ic2DataGenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
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

	public FmlMod()
	{
		instance = this;
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.register(this);
		EnvProxyForge.blockEntityRegistry.register(modEventBus);
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
		ModLoadingContext.get()
			.registerExtensionPoint(
				DisplayTest.class,
				() -> new DisplayTest(
					() -> "OHNOES\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31\ud83d\ude31",
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
		Ic2DataGenerators.setup(event.getGenerator());
	}

	@SubscribeEvent
	public void registerBlocks(RegisterEvent event)
	{
		if (event.getRegistryKey() == Registry.f_122901_)
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
		if (event.getRegistryKey() == Registry.f_122898_)
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
		if (event.getRegistryKey() == Registry.f_122838_)
		{
			for (Runnable reg : EnvProxyForge.configuredFeatureRegistrations)
			{
				reg.run();
			}

			for (EnvProxyForge.PlacedFeatureRegistration<?> reg : EnvProxyForge.placedFeatureRegistrations)
			{
				reg.placedFeature().complete(PlacementUtils.m_206509_(reg.id().toString(), reg.feature().join(), reg.modifiers()));
			}

			for (EnvProxyForge.PlacementModifierTypeRegistration reg : EnvProxyForge.placementModifierTypeRegistrations)
			{
				Registry.m_122965_(Registry.f_194570_, reg.id(), reg.type());
			}
		}
	}
}
