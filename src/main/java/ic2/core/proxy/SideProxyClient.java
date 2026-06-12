package ic2.core.proxy;

import ic2.api.tile.IRotorProvider;
import ic2.core.IC2;
import ic2.core.block.generator.gui.GuiSolarGenerator;
import ic2.core.block.heatgenerator.gui.GuiElectricHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiFluidHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiRTHeatGenerator;
import ic2.core.block.kineticgenerator.gui.GuiElectricKineticGenertor;
import ic2.core.block.kineticgenerator.gui.GuiSteamKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import ic2.core.block.machine.gui.GuiAdvMiner;
import ic2.core.block.machine.gui.GuiBatchCrafter;
import ic2.core.block.machine.gui.GuiCanner;
import ic2.core.block.machine.gui.GuiChunkLoader;
import ic2.core.block.machine.gui.GuiCondenser;
import ic2.core.block.machine.gui.GuiCropHarvester;
import ic2.core.block.machine.gui.GuiCropmatron;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.block.machine.gui.GuiFermenter;
import ic2.core.block.machine.gui.GuiFluidBottler;
import ic2.core.block.machine.gui.GuiFluidDistributor;
import ic2.core.block.machine.gui.GuiFluidRegulator;
import ic2.core.block.machine.gui.GuiIndustrialWorkbench;
import ic2.core.block.machine.gui.GuiItemBuffer;
import ic2.core.block.machine.gui.GuiLiquidHeatExchanger;
import ic2.core.block.machine.gui.GuiMagnetizer;
import ic2.core.block.machine.gui.GuiMatter;
import ic2.core.block.machine.gui.GuiMetalFormer;
import ic2.core.block.machine.gui.GuiMiner;
import ic2.core.block.machine.gui.GuiPatternStorage;
import ic2.core.block.machine.gui.GuiReplicator;
import ic2.core.block.machine.gui.GuiScanner;
import ic2.core.block.machine.gui.GuiSolarDestiller;
import ic2.core.block.machine.gui.GuiSortingMachine;
import ic2.core.block.machine.gui.GuiSteamGenerator;
import ic2.core.block.machine.gui.GuiWeightedFluidDistributor;
import ic2.core.block.machine.gui.GuiWeightedItemDistributor;
import ic2.core.block.personal.GuiEnergyOMatClosed;
import ic2.core.block.personal.GuiEnergyOMatOpen;
import ic2.core.block.personal.GuiTradeOMatClosed;
import ic2.core.block.personal.GuiTradeOMatOpen;
import ic2.core.block.reactor.gui.GuiNuclearReactor;
import ic2.core.block.renderer.KineticGeneratorRenderer;
import ic2.core.block.wiring.GuiChargepadBlock;
import ic2.core.block.wiring.GuiElectricBlock;
import ic2.core.block.wiring.GuiTransformer;
import ic2.core.entity.render.BoatEntityRenderer;
import ic2.core.entity.render.ExplosiveBlockRenderer;
import ic2.core.entity.render.LaserBulletEntityRenderer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.item.tool.GuiToolMeter;
import ic2.core.item.tool.GuiToolScanner;
import ic2.core.item.tool.GuiCropAnalyzer;
import ic2.core.item.tool.GuiToolbox;
import ic2.core.item.upgrade.AdvancedUpgradeScreenFactory;
import ic2.core.item.upgrade.HandHeldOre;
import ic2.core.item.upgrade.HandHeldValueConfig;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Entities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.sound.SoundManager;
import ic2.core.sound.SoundManagerClient;
import ic2.core.util.Keyboard;
import ic2.core.util.KeyboardClient;
import ic2.core.util.Util;

import java.io.File;
import java.util.Objects;

import ic2.forge.ClientEnvProxyForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class SideProxyClient implements SideProxy
{
	public static final ClientEnvProxy envProxy = new ClientEnvProxyForge();
	public static final Minecraft mc = Minecraft.getInstance();
	public static final Keyboard keyboard = new KeyboardClient();
	private static final SoundManager soundManager = new SoundManagerClient();
	@Override
	public void preInit()
	{
		envProxy.registerScreen(Ic2ScreenHandlers.DYNAMIC_BE, DynamicGui::create);
		envProxy.registerScreen(Ic2ScreenHandlers.DYNAMIC_ITEM, DynamicGui::create);
		envProxy.registerScreen(Ic2ScreenHandlers.ELECTRIC_HEAT_GENERATOR, GuiElectricHeatGenerator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.FLUID_HEAT_GENERATOR, GuiFluidHeatGenerator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.RT_HEAT_GENERATOR, GuiRTHeatGenerator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ELECTRIC_KINETIC_GENERATOR, GuiElectricKineticGenertor::new);
		envProxy.registerScreen(Ic2ScreenHandlers.STEAM_KINETIC_GENERATOR, GuiSteamKineticGenerator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.STIRLING_KINETIC_GENERATOR, GuiStirlingKineticGenerator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.WATER_KINETIC_GENERATOR, GuiWaterKineticGenerator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.WIND_KINETIC_GENERATOR, GuiWindKineticGenerator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.NUCLEAR_REACTOR, GuiNuclearReactor::new);
		envProxy.registerScreen(Ic2ScreenHandlers.CONDENSER, GuiCondenser::new);
		envProxy.registerScreen(Ic2ScreenHandlers.FLUID_BOTTLER, GuiFluidBottler::new);
		envProxy.registerScreen(Ic2ScreenHandlers.FLUID_DISTRIBUTOR, GuiFluidDistributor::new);
		envProxy.registerScreen(Ic2ScreenHandlers.FLUID_REGULATOR, GuiFluidRegulator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.LIQUID_HEAT_EXCHANGER, GuiLiquidHeatExchanger::new);
		envProxy.registerScreen(Ic2ScreenHandlers.SOLAR_DISTILLER, GuiSolarDestiller::new);
		envProxy.registerScreen(Ic2ScreenHandlers.STEAM_GENERATOR, GuiSteamGenerator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ITEM_BUFFER, GuiItemBuffer::new);
		envProxy.registerScreen(Ic2ScreenHandlers.MAGNETIZER, GuiMagnetizer::new);
		envProxy.registerScreen(Ic2ScreenHandlers.SORTING_MACHINE, GuiSortingMachine::new);
		envProxy.registerScreen(Ic2ScreenHandlers.CANNER, GuiCanner::new);
		envProxy.registerScreen(Ic2ScreenHandlers.FERMENTER, GuiFermenter::new);
		envProxy.registerScreen(Ic2ScreenHandlers.METAL_FORMER, GuiMetalFormer::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ADVANCED_MINER, GuiAdvMiner::new);
		envProxy.registerScreen(Ic2ScreenHandlers.CROP_HARVESTER, GuiCropHarvester::new);
		envProxy.registerScreen(Ic2ScreenHandlers.CROPMATRON, GuiCropmatron::new);
		envProxy.registerScreen(Ic2ScreenHandlers.MINER, GuiMiner::new);
		envProxy.registerScreen(Ic2ScreenHandlers.MATTER_GENERATOR, GuiMatter::new);
		envProxy.registerScreen(Ic2ScreenHandlers.PATTERN_STORAGE, GuiPatternStorage::new);
		envProxy.registerScreen(Ic2ScreenHandlers.REPLICATOR, GuiReplicator::new);
		envProxy.registerScreen(Ic2ScreenHandlers.UU_SCANNER, GuiScanner::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ENERGY_O_MAT_CLOSED, GuiEnergyOMatClosed::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ENERGY_O_MAT_OPEN, GuiEnergyOMatOpen::new);
		envProxy.registerScreen(Ic2ScreenHandlers.TRADE_O_MAT_CLOSED, GuiTradeOMatClosed::new);
		envProxy.registerScreen(Ic2ScreenHandlers.TRADE_O_MAT_OPEN, GuiTradeOMatOpen::new);
		envProxy.registerScreen(Ic2ScreenHandlers.CHARGEPAD, GuiChargepadBlock::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ENERGY_STORAGE, GuiElectricBlock::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ELECTROLYZER, GuiElectrolyzer::new);
		envProxy.registerScreen(Ic2ScreenHandlers.TRANSFORMER, GuiTransformer::new);
		envProxy.registerScreen(Ic2ScreenHandlers.CHUNK_LOADER, GuiChunkLoader::new);
		envProxy.registerScreen(Ic2ScreenHandlers.WEIGHTED_FLUID_DISTRIBUTOR, GuiWeightedFluidDistributor::new);
		envProxy.registerScreen(Ic2ScreenHandlers.WEIGHTED_ITEM_DISTRIBUTOR, GuiWeightedItemDistributor::new);
		envProxy.registerScreen(Ic2ScreenHandlers.INDUSTRIAL_WORKBENCH, GuiIndustrialWorkbench::new);
		envProxy.registerScreen(Ic2ScreenHandlers.BATCH_CRAFTER, GuiBatchCrafter::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ADVANCED_UPGRADE, new AdvancedUpgradeScreenFactory());
		envProxy.registerScreen(Ic2ScreenHandlers.ADVANCED_UPGRADE_EDIT_ORE, HandHeldOre.GuiEditOre::new);
		envProxy.registerScreen(Ic2ScreenHandlers.ADVANCED_UPGRADE_VALUE_CONFIG, HandHeldValueConfig.GuiValueConfig::new);
		envProxy.registerScreen(Ic2ScreenHandlers.SCANNER, GuiToolScanner::new);
		envProxy.registerScreen(Ic2ScreenHandlers.TOOL_BOX, GuiToolbox::new);
		envProxy.registerScreen(Ic2ScreenHandlers.METER, GuiToolMeter::new);
		envProxy.registerScreen(Ic2ScreenHandlers.CROP_ANALYZER, GuiCropAnalyzer::new);
		envProxy.registerScreen(Ic2ScreenHandlers.SOLAR_GENERATOR, GuiSolarGenerator::new);
		envProxy.registerColorProvider((state, world, post, tintIndex) -> 6723908, Ic2Blocks.RUBBER_LEAVES);
		envProxy.registerColorProvider((var1, var2) -> 6723908, Ic2Items.RUBBER_LEAVES);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.WOODEN_SCAFFOLD, Ic2Blocks.IRON_SCAFFOLD);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.FOAM);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.REINFORCED_GLASS);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.REINFORCED_DOOR);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.COPPER_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.INSULATED_COPPER_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.TIN_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.INSULATED_TIN_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.IRON_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.INSULATED_IRON_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DOUBLE_INSULATED_IRON_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.TRIPLE_INSULATED_IRON_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.GOLD_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.INSULATED_GOLD_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DOUBLE_INSULATED_GOLD_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.GLASS_FIBRE_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DETECTOR_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.SPLITTER_CABLE);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.CROP_STICK);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.WEED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.WHEAT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.CARROTS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.POTATO_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.BEETROOTS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.PUMPKIN_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.MELON_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DANDELION_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.POPPY_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.BLACKTHORN_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.TULIP_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.CYAZINT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.VENOMILIA_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.REED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.STICKY_REED_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.COCOA_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.FLAX_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.RED_MUSHROOM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.BROWN_MUSHROOM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.NETHER_WART_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.TERRA_WART_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.OAK_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.SPRUCE_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.BIRCH_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.JUNGLE_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.ACACIA_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DARK_OAK_SAPLING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.FERRU_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.CYPRIUM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.STAGNIUM_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.PLUMBISCUS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.AURELIA_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.SHINING_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.RED_WHEAT_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.COFFEE_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.HOPS_CROP);
		envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.EATING_PLANT_CROP);
		this.registerRotorProvider(Ic2BlockEntities.WIND_KINETIC_GENERATOR);
		this.registerRotorProvider(Ic2BlockEntities.WATER_KINETIC_GENERATOR);
		this.registerRotorProvider(Ic2BlockEntities.WIND_GENERATOR);
		this.registerRotorProvider(Ic2BlockEntities.WATER_GENERATOR);
		envProxy.registerEntityRenderer(Ic2Entities.ITNT, ExplosiveBlockRenderer::new);
		envProxy.registerEntityRenderer(Ic2Entities.NUKE, ExplosiveBlockRenderer::new);
		envProxy.registerEntityRenderer(Ic2Entities.LASER_BULLET, LaserBulletEntityRenderer::new);
		envProxy.registerEntityRenderer(Ic2Entities.RUBBER_BOAT, context -> new BoatEntityRenderer(context, false, "ic2"));
		envProxy.registerEntityRenderer(Ic2Entities.ELECTRIC_BOAT, context -> new BoatEntityRenderer(context, false, "ic2"));
		envProxy.registerEntityRenderer(Ic2Entities.CARBON_BOAT, context -> new BoatEntityRenderer(context, false, "ic2"));
		envProxy.registerBlockEntityRenderer(Ic2BlockEntities.SIGN, SignRenderer::new);
	}

	@Override
	public void onPostInit()
	{
	}

	@Override
	public boolean isSimulating()
	{
		return !this.isRendering();
	}

	@Override
	public SoundManager getSoundManager()
	{
		return soundManager;
	}

	@Override
	public Keyboard getKeyboard()
	{
		return keyboard;
	}

	@Override
	public boolean isRendering()
	{
		return Minecraft.getInstance().isSameThread();
	}

	@Override
	public void requestTick(boolean simulating, Runnable runnable)
	{
		if (simulating)
		{
			MinecraftServer server = mc.getSingleplayerServer();
			if (server == null)
			{
				throw new IllegalStateException("server unavailable");
			}

			server.execute(runnable);
		} else
		{
			mc.execute(runnable);
		}
	}

	@Override
	public void onServerAvailable(MinecraftServer server)
	{
	}

	@Override
	public void displayError(String error, Object... args)
	{
		SideProxyServer.displayError0(error, args);
	}

	@Override
	public void displayError(Exception e, String error, Object... args)
	{
		SideProxyServer.displayError(this, e, error, args);
	}

	@Override
	public Player getPlayerInstance()
	{
		return mc.player;
	}

	@Override
	public Level getWorld(MinecraftServer server, ResourceLocation dimId)
	{
		if (server == null)
		{
			Level ret = mc.level;
			if (ret != null && dimId.equals(Util.getDimId(ret)))
			{
				return ret;
			}
		} else
		{
			for (Level world : server.getAllLevels())
			{
				if (dimId.equals(Util.getDimId(world)))
				{
					return world;
				}
			}
		}

		return null;
	}

	@Override
	public Level getPlayerWorld()
	{
		return mc.level;
	}

	@Override
	public RecipeManager getRecipeManager()
	{
		MinecraftServer server = mc.getSingleplayerServer();
		return server != null ? server.getRecipeManager() : Objects.requireNonNull(mc.getConnection()).getRecipeManager();
	}

	@Override
	public File getMinecraftDir()
	{
		return envProxy.getMinecraftDir();
	}

	@Deprecated
	@Override
	public void playSoundSp(SoundEvent soundEvent, SoundSource soundCategory, float volume, float pitch)
	{
		IC2.soundManager.playOnce(soundEvent, soundCategory, volume, pitch, this.getPlayerInstance());
	}

	@Override
	public void playSoundOnce(Entity entity, SoundEvent soundEvent, float volume, float pitch)
	{
		entity.playSound(soundEvent, volume, pitch);
	}

	@Override
	public void messagePlayer(Player player, String translatable, Object... args)
	{
		if (player == null) player = mc.player;
		if (player != null) player.displayClientMessage(Component.translatable(translatable, args.length > 0 ? SideProxyServer.getMessageComponents(args) : new Object[0]), false);
	}

	@Override
	public <T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> type)
	{
		envProxy.registerBer(type, KineticGeneratorRenderer::new);
	}
	
}
