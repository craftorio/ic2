package ic2.forge;

import ic2.core.event.EventHandler;
import ic2.core.network.Ic2NetworkPayload;
import ic2.core.network.NetworkManager;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.fluid.FluidBeBridge;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.loot.Ic2LootNbtProviderTypes;
import ic2.core.ref.Ic2Fluids;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.fml.ModContainer;

@Mod("ic2")
public final class FmlMod {

    private List<Runnable> toRunAfterRegistryInit = new ArrayList<>();

    public static FmlMod instance;

    private static final AtomicInteger loadState = new AtomicInteger();

    private final ModContainer modContainer;

    public FmlMod(ModContainer modContainer) {
        instance = this;
        this.modContainer = modContainer;
        IEventBus modEventBus = modContainer.getEventBus();
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
        Ic2LootModifier.lootModifiersRegistry.register(modEventBus);
        if (FMLLoader.getDist().isClient()) {
            modEventBus.register(new ClientModEventHandlerForge());
        }
        Ic2Fluids.init();
    }

    @SubscribeEvent
    public void load(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new EventHandlerForge());
        if (FMLLoader.getDist().isClient()) {
            NeoForge.EVENT_BUS.register(new ClientEventHandlerForge());
        }
        if (!loadState.compareAndSet(1, 2)) {
            throw new IllegalStateException();
        }
        EventHandler.onInit();
    }

    @SubscribeEvent
    public void init(FMLLoadCompleteEvent event) {
        if (!loadState.compareAndSet(2, 3)) {
            throw new IllegalStateException();
        }
        EventHandler.onInitLate();
    }

    @SubscribeEvent
    public void registerFluidTypes(RegisterEvent event) {
        if (event.getRegistryKey() == NeoForgeRegistries.Keys.FLUID_TYPES) {
            EnvFluidHandlerForge.registerPendingFluidTypes();
        }
    }

    @SubscribeEvent
    public void registerLootNbtProviders(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.LOOT_NBT_PROVIDER_TYPE) {
            Ic2LootNbtProviderTypes.init();
        }
    }

    @SubscribeEvent
    public void registerFluids(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.FLUID) {
            EnvFluidHandlerForge.registerPendingFluids();
        }
    }

    @SubscribeEvent
    public void registerItems(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.ITEM) {
            EnvProxyForge.registerPendingItems();
        }
    }

    @SubscribeEvent
    public void registerBlocks(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.BLOCK) {
            if (!loadState.compareAndSet(0, 1)) {
                throw new IllegalStateException();
            }
            EventHandler.onInitEarly();
        }
    }

    @SubscribeEvent
    public void registerGameEvents(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.SOUND_EVENT) {
            EventHandler.onInitGameEvents();
        }
    }

    @SubscribeEvent
    public void registerLate(RegisterEvent event) {
        if (event.getRegistryKey() == NeoForgeRegistries.Keys.HOLDER_SET_TYPES) {
            for (Runnable runnable : this.toRunAfterRegistryInit) {
                runnable.run();
            }
            this.toRunAfterRegistryInit = null;
        }
    }

    void runAfterRegistryInit(Runnable runnable) {
        if (loadState.get() > 1) {
            runnable.run();
        } else {
            this.toRunAfterRegistryInit.add(runnable);
        }
    }

    @SubscribeEvent
    public void registerFeatures(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.CONFIGURED_FEATURE) {
            for (EnvProxyForge.ConfiguredFeatureRegistration<?, ?> reg : EnvProxyForge.configuredFeatureRegistrations) {
                ConfiguredFeature cf = new ConfiguredFeature(reg.feature(), reg.config());
                event.register(Registries.CONFIGURED_FEATURE, reg.id(), () -> cf);
            }
        }
    }

    @SubscribeEvent
    public void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playBidirectional(Ic2NetworkPayload.TYPE, Ic2NetworkPayload.STREAM_CODEC, ForgeNetworkHandler.HANDLER);
    }

    @SubscribeEvent
    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("ic2")) {
                event.registerBlock(Capabilities.FluidHandler.BLOCK,
                    (level, pos, state, be, side) -> {
                        if (be instanceof Ic2TileEntity) {
                            if (be instanceof FluidBeBridge bridge) {
                                Ic2FluidBlock fb = bridge.getFluidBlock();
                                if (fb != null && fb.isFluidBlock(null, null, null, be)) {
                                    return new BlockFluidCapImpl(fb, be).getCapability(level, pos, state, be, side);
                                }
                            }
                            return new LazyBlockFluidCapImpl(be).getCapability(level, pos, state, be, side);
                        }
                        return null;
                    },
                    block);

                event.registerBlock(Capabilities.ItemHandler.BLOCK,
                    (level, pos, state, be, side) -> {
                        if (be instanceof WorldlyContainer wc) {
                            return new SidedInvWrapper(wc, side);
                        }
                        if (be instanceof Container cont) {
                            return new InvWrapper(cont);
                        }
                        return null;
                    },
                    block);
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof Ic2FluidItem) {
                event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, unused) -> new ItemFluidCapImpl(stack),
                    item);
            }
        }
    }
}
