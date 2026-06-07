package ic2.core.ref;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.generator.container.ContainerSolarGenerator;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import ic2.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerWindKineticGenerator;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.block.machine.container.ContainerClassicCropmatron;
import ic2.core.block.machine.container.ContainerCondenser;
import ic2.core.block.machine.container.ContainerCropHarvester;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.block.machine.container.ContainerFermenter;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.block.machine.container.ContainerFluidRegulator;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.machine.container.ContainerItemBuffer;
import ic2.core.block.machine.container.ContainerLiquidHeatExchanger;
import ic2.core.block.machine.container.ContainerMagnetizer;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.block.machine.container.ContainerPatternStorage;
import ic2.core.block.machine.container.ContainerReplicator;
import ic2.core.block.machine.container.ContainerScanner;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;
import ic2.core.block.machine.container.ContainerWeightedItemDistributor;
import ic2.core.block.personal.ContainerEnergyOMatClosed;
import ic2.core.block.personal.ContainerEnergyOMatOpen;
import ic2.core.block.personal.ContainerTradeOMatClosed;
import ic2.core.block.personal.ContainerTradeOMatOpen;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.block.wiring.ContainerChargepadBlock;
import ic2.core.block.wiring.ContainerElectricBlock;
import ic2.core.block.wiring.ContainerTransformer;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.item.tool.ContainerMeter;
import ic2.core.item.tool.ContainerToolScanner;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.item.upgrade.HandHeldAdvancedUpgrade;
import ic2.core.item.upgrade.HandHeldOre;
import ic2.core.item.upgrade.HandHeldValueConfig;
import ic2.core.network.DataEncoder;
import ic2.core.network.GrowingBuffer;
import ic2.core.proxy.EnvProxy;
import ic2.core.util.Util;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.function.BiFunction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class Ic2ScreenHandlers
{
	private static final EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>> MANAGED_CLIENT_BE_HANDLER = createManagedBeClientHandler();
	private static final EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>> MANAGED_CLIENT_ITEM_HANDLER = createManagedItemClientHandler();
	public static final MenuType<DynamicContainer<TileEntityInventory>> DYNAMIC_BE = registerManagedBe("dynamic_be");
	public static final MenuType<DynamicContainer<HandHeldInventory>> DYNAMIC_ITEM = registerManagedItem("dynamic_item");
	public static final MenuType<ContainerSolarGenerator> SOLAR_GENERATOR = registerManagedBe("solar_generator");
	public static final MenuType<ContainerElectricHeatGenerator> ELECTRIC_HEAT_GENERATOR = registerManagedBe("electric_heat_generator");
	public static final MenuType<ContainerFluidHeatGenerator> FLUID_HEAT_GENERATOR = registerManagedBe("fluid_heat_generator");
	public static final MenuType<ContainerRTHeatGenerator> RT_HEAT_GENERATOR = registerManagedBe("rt_heat_generator");
	public static final MenuType<ContainerElectricKineticGenerator> ELECTRIC_KINETIC_GENERATOR = registerManagedBe("electric_kinetic_generator");
	public static final MenuType<ContainerSteamKineticGenerator> STEAM_KINETIC_GENERATOR = registerManagedBe("steam_kinetic_generator");
	public static final MenuType<ContainerStirlingKineticGenerator> STIRLING_KINETIC_GENERATOR = registerManagedBe("stirling_kinetic_generator");
	public static final MenuType<ContainerWaterKineticGenerator> WATER_KINETIC_GENERATOR = registerManagedBe("water_kinetic_generator");
	public static final MenuType<ContainerWindKineticGenerator> WIND_KINETIC_GENERATOR = registerManagedBe("wind_kinetic_generator");
	public static final MenuType<ContainerNuclearReactor> NUCLEAR_REACTOR = registerManagedBe("nuclear_reactor");
	public static final MenuType<ContainerCondenser> CONDENSER = registerManagedBe("condenser");
	public static final MenuType<ContainerFluidBottler> FLUID_BOTTLER = registerManagedBe("fluid_bottler");
	public static final MenuType<ContainerFluidDistributor> FLUID_DISTRIBUTOR = registerManagedBe("fluid_distributor");
	public static final MenuType<ContainerFluidRegulator> FLUID_REGULATOR = registerManagedBe("fluid_regulator");
	public static final MenuType<ContainerLiquidHeatExchanger> LIQUID_HEAT_EXCHANGER = registerManagedBe("liquid_heat_exchanger");
	public static final MenuType<ContainerSolarDestiller> SOLAR_DISTILLER = registerManagedBe("solar_distiller");
	public static final MenuType<ContainerSteamGenerator> STEAM_GENERATOR = registerManagedBe("steam_generator");
	public static final MenuType<ContainerItemBuffer> ITEM_BUFFER = registerManagedBe("item_buffer");
	public static final MenuType<ContainerMagnetizer> MAGNETIZER = registerManagedBe("magnetizer");
	public static final MenuType<ContainerSortingMachine> SORTING_MACHINE = registerManagedBe("sorting_machine");
	public static final MenuType<ContainerCanner> CANNER = registerManagedBe("canner");
	public static final MenuType<ContainerClassicCanner> CLASSIC_CANNER = registerManagedBe("classic_canner");
	public static final MenuType<ContainerFermenter> FERMENTER = registerManagedBe("fermenter");
	public static final MenuType<ContainerMetalFormer> METAL_FORMER = registerManagedBe("metal_former");
	public static final MenuType<ContainerAdvMiner> ADVANCED_MINER = registerManagedBe("advanced_miner");
	public static final MenuType<ContainerCropHarvester> CROP_HARVESTER = registerManagedBe("crop_harvester");
	public static final MenuType<ContainerCropmatron> CROPMATRON = registerManagedBe("cropmatron");
	public static final MenuType<ContainerClassicCropmatron> CLASSIC_CROPMATRON = registerManagedBe("classic_cropmatron");
	public static final MenuType<ContainerMiner> MINER = registerManagedBe("miner");
	public static final MenuType<ContainerMatter> MATTER_GENERATOR = registerManagedBe("matter_generator");
	public static final MenuType<ContainerPatternStorage> PATTERN_STORAGE = registerManagedBe("pattern_storage");
	public static final MenuType<ContainerReplicator> REPLICATOR = registerManagedBe("replicator");
	public static final MenuType<ContainerScanner> UU_SCANNER = registerManagedBe("uu_scanner");
	public static final MenuType<ContainerEnergyOMatClosed> ENERGY_O_MAT_CLOSED = registerManagedBe("energy_o_mat_closed");
	public static final MenuType<ContainerEnergyOMatOpen> ENERGY_O_MAT_OPEN = registerManagedBe("energy_o_mat_open");
	public static final MenuType<ContainerTradeOMatClosed> TRADE_O_MAT_CLOSED = registerManagedBe("trade_o_mat_closed");
	public static final MenuType<ContainerTradeOMatOpen> TRADE_O_MAT_OPEN = registerManagedBe("trade_o_mat_open");
	public static final MenuType<ContainerChargepadBlock> CHARGEPAD = registerManagedBe("chargepad");
	public static final MenuType<ContainerElectricBlock> ENERGY_STORAGE = registerManagedBe("energy_storage");
	public static final MenuType<ContainerElectrolyzer> ELECTROLYZER = registerManagedBe("electrolyzer");
	public static final MenuType<ContainerTransformer> TRANSFORMER = registerManagedBe("transformer");
	public static final MenuType<ContainerChunkLoader> CHUNK_LOADER = registerManagedBe("chunk_loader");
	public static final MenuType<ContainerWeightedFluidDistributor> WEIGHTED_FLUID_DISTRIBUTOR = registerManagedBe("weighted_fluid_distributor");
	public static final MenuType<ContainerWeightedItemDistributor> WEIGHTED_ITEM_DISTRIBUTOR = registerManagedBe("weighted_item_distributor");
	public static final MenuType<ContainerIndustrialWorkbench> INDUSTRIAL_WORKBENCH = registerManagedBe("industrial_workbench");
	public static final MenuType<ContainerBatchCrafter> BATCH_CRAFTER = registerManagedBe("batch_crafter");
	public static final MenuType<DynamicContainer<HandHeldAdvancedUpgrade>> ADVANCED_UPGRADE = registerManagedItem("advanced_upgrade");
	public static final MenuType<HandHeldOre.ContainerEditOre> ADVANCED_UPGRADE_EDIT_ORE = registerManagedItem("advanced_upgrade/edit_ore");
	public static final MenuType<HandHeldValueConfig.ContainerValueConfig> ADVANCED_UPGRADE_VALUE_CONFIG = registerManagedItem("advanced_upgrade/value_config");
	public static final MenuType<ContainerToolScanner> SCANNER = registerManagedItem("scanner");
	public static final MenuType<ContainerMeter> METER = registerManagedItem("meter");
	public static final MenuType<ContainerToolbox> TOOL_BOX = registerManagedItem("tool_box");

	public static void init()
	{
	}

	private static <T extends AbstractContainerMenu> MenuType<T> register(String name, BiFunction<Integer, Inventory, T> factory)
	{
		return IC2.envProxy.registerScreenHandler(IC2.getIdentifier(name), factory);
	}

	private static <T extends AbstractContainerMenu> MenuType<T> registerExtended(String name, EnvProxy.ExtendedClientScreenHandlerFactory<T> factory)
	{
		return IC2.envProxy.registerExtendedScreenHandler(IC2.getIdentifier(name), factory);
	}

	public static <T extends AbstractContainerMenu> MenuType<T> registerManagedBe(String name)
	{
		return registerExtended(name, MANAGED_CLIENT_BE_HANDLER);
	}

	private static <T extends AbstractContainerMenu> MenuType<T> registerManagedItem(String name)
	{
		return registerExtended(name, MANAGED_CLIENT_ITEM_HANDLER);
	}

	private static EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>> createManagedBeClientHandler()
	{
		return new EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>>()
		{
			public ContainerBase<?> create(int syncId, Inventory inventory, ByteBuf data)
			{
				GrowingBuffer buffer = GrowingBuffer.wrap(data);

				try
				{
					BlockEntity be = DataEncoder.getValue(DataEncoder.decode(buffer, DataEncoder.EncodedType.TileEntity), null);
					return !(be instanceof IHasGui provider) ? null : provider.createClientScreenHandler(syncId, inventory, buffer);
				} catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
	}

	public static void writeManagedBeData(BlockEntity be, GrowingBuffer buffer) throws IOException
	{
		DataEncoder.encode(buffer, be, false);
	}

	private static EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>> createManagedItemClientHandler()
	{
		return new EnvProxy.ExtendedClientScreenHandlerFactory<ContainerBase<?>>()
		{
			public ContainerBase<?> create(int syncId, Inventory inventory, ByteBuf data)
			{
				GrowingBuffer buffer = GrowingBuffer.wrap(data);
				int currentItemPosition = buffer.readInt();
				boolean subGui = buffer.readBoolean();
				int subGuiId = subGui ? buffer.readVarInt() : 0;
				Player player = IC2.sideProxy.getPlayerInstance();
				InteractionHand hand;
				ItemStack currentItem;
				if (currentItemPosition < 0)
				{
					int handOrdinal = -currentItemPosition;
					if (handOrdinal >= Util.HANDS.length)
					{
						return null;
					}

					hand = Util.HANDS[handOrdinal];
					currentItem = player.m_21120_(hand);
				} else
				{
					if (currentItemPosition != player.getInventory().f_35977_)
					{
						return null;
					}

					hand = InteractionHand.MAIN_HAND;
					currentItem = player.getInventory().m_36056_();
				}

				if (currentItem == null)
				{
					return null;
				} else if (!(currentItem.getItem() instanceof IHandHeldInventory item))
				{
					return null;
				} else
				{
					IHasGui provider;
					if (subGui && item instanceof IHandHeldSubInventory)
					{
						provider = ((IHandHeldSubInventory) item).getSubInventory(player, hand, currentItem, subGuiId);
					} else
					{
						provider = item.getInventory(player, hand, currentItem);
					}

					return provider.createClientScreenHandler(syncId, inventory, buffer);
				}
			}
		};
	}

	public static void writeManagedItemData(Player player, InteractionHand hand, Integer subGuiId, GrowingBuffer buffer)
	{
		Item item = player.m_21120_(hand).getItem();
		assert item instanceof IHandHeldInventory;
		int slot;
		if (hand == InteractionHand.MAIN_HAND)
		{
			slot = player.getInventory().f_35977_;
		} else
		{
			slot = -hand.ordinal();
		}

		buffer.writeInt(slot);
		boolean subInv = subGuiId != null && item instanceof IHandHeldSubInventory;
		buffer.writeBoolean(subInv);
		if (subInv)
		{
			buffer.writeVarInt(subGuiId);
		}
	}
}
