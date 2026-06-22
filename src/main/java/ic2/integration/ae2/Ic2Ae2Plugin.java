package ic2.integration.ae2;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.info.ILocatable;
import ic2.api.info.Info;
import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public final class Ic2Ae2Plugin
{
	public static final double EU_TO_AE_RATIO = 2.0;
	public static final ResourceLocation ENERGY_ACCEPTOR_ID = ResourceLocation.fromNamespaceAndPath("ae2", "energy_acceptor");

	private static final Map<Level, Map<BlockPos, Ae2EnergySink>> sinks = new HashMap<>();
	private static final Map<Level, Queue<ChunkPos>> pendingChunks = new HashMap<>();
	private static final int POSITIONS_PER_TICK = 32;

	private static boolean ae2GridApiAvailable;
	private static Method ae2InjectMethod;
	private static Method ae2GetEnergyDemandMethod;
	private static Method ae2GetExposedNodeMethod;
	private static Method ae2GetEnergyServiceMethod;
	private static Object ae2ActionableModulate;

	static
	{
		try
		{
			Class<?> gridHelperClass = Class.forName("appeng.api.networking.GridHelper");
			Class<?> energyServiceClass = Class.forName("appeng.api.networking.energy.IEnergyService");
			Class<?> actionableClass = Class.forName("appeng.api.config.Actionable");
			Class<?> iGridClass = Class.forName("appeng.api.networking.IGrid");

			ae2GetExposedNodeMethod = gridHelperClass.getMethod("getExposedNode", Level.class, BlockPos.class, Direction.class);
			ae2InjectMethod = energyServiceClass.getMethod("injectPower", double.class, actionableClass);
			ae2GetEnergyDemandMethod = energyServiceClass.getMethod("getEnergyDemand", double.class);
			ae2GetEnergyServiceMethod = iGridClass.getMethod("getEnergyService");
			ae2ActionableModulate = actionableClass.getField("MODULATE").get(null);

			ae2GridApiAvailable = true;
			IC2.log.debug(LogCategory.EnergyNet, "AE2 grid API available for direct AE injection.");
		} catch (Exception e)
		{
			ae2GridApiAvailable = false;
			IC2.log.debug(LogCategory.EnergyNet, "AE2 grid API not available, falling back to IEnergyStorage.");
		}
	}

	public static void init()
	{
	}

	private static boolean isEnergyAcceptor(LevelAccessor world, BlockPos pos)
	{
		return ENERGY_ACCEPTOR_ID.equals(ForgeRegistries.BLOCKS.getKey(world.getBlockState(pos).getBlock()));
	}

	private static void registerSink(Level world, BlockPos pos)
	{
		if (world.isClientSide || !Info.isIc2Available()) return;

		Map<BlockPos, Ae2EnergySink> worldSinks = sinks.computeIfAbsent(world, k -> new HashMap<>());
		if (worldSinks.containsKey(pos)) return;

		Ae2EnergySink sink = new Ae2EnergySink(world, pos);
		worldSinks.put(pos, sink);
		EnergyNet.instance.addLocatableTile(sink);

		IC2.log.debug(LogCategory.EnergyNet, "Registered AE2 energy_acceptor sink at %s in %s", pos, world.dimension().location());
	}

	private static void unregisterSink(Level world, BlockPos pos)
	{
		if (world.isClientSide || !Info.isIc2Available()) return;

		Map<BlockPos, Ae2EnergySink> worldSinks = sinks.get(world);
		if (worldSinks == null) return;

		Ae2EnergySink sink = worldSinks.remove(pos);
		if (sink != null)
		{
			EnergyNet.instance.removeTile(sink);
			IC2.log.debug(LogCategory.EnergyNet, "Unregistered AE2 energy_acceptor sink at %s in %s", pos, world.dimension().location());
		}
	}

	private static void unregisterAllInWorld(Level world)
	{
		Map<BlockPos, Ae2EnergySink> worldSinks = sinks.remove(world);
		pendingChunks.remove(world);
		if (worldSinks == null) return;

		for (Ae2EnergySink sink : worldSinks.values())
		{
			EnergyNet.instance.removeTile(sink);
		}

		IC2.log.debug(LogCategory.EnergyNet, "Unregistered all AE2 energy_acceptor sinks in %s", world.dimension().location());
	}

	private static void processPendingChunks(ServerLevel world)
	{
		Queue<ChunkPos> queue = pendingChunks.get(world);
		if (queue == null || queue.isEmpty()) return;

		int processed = 0;
		while (processed < POSITIONS_PER_TICK && !queue.isEmpty())
		{
			ChunkPos cp = queue.poll();
			int chunkX = cp.x;
			int chunkZ = cp.z;
			BlockPos checkPos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
			if (!world.isLoaded(checkPos)) continue;

			LevelChunk chunk = world.getChunk(chunkX, chunkZ);

			for (BlockPos pos : chunk.getBlockEntities().keySet())
			{
				if (isEnergyAcceptor(world, pos))
				{
					registerSink(world, pos);
				}
			}

			processed++;
		}
	}

	public static final class ForgeEventHandler
	{
		@SubscribeEvent
		public void onBlockPlace(BlockEvent.EntityPlaceEvent event)
		{
			if (event.getEntity() == null) return;
			Level world = event.getEntity().level();
			if (world.isClientSide) return;

			BlockPos pos = event.getPos();
			if (isEnergyAcceptor(world, pos))
			{
				registerSink(world, pos);
			}
		}

		@SubscribeEvent
		public void onBlockBreak(BlockEvent.BreakEvent event)
		{
			Level world = (Level) event.getLevel();
			if (world.isClientSide) return;

			BlockPos pos = event.getPos();
			if (isEnergyAcceptor(world, pos))
			{
				unregisterSink(world, pos);
			}
		}

		@SubscribeEvent
		public void onChunkLoad(ChunkEvent.Load event)
		{
			if (!(event.getChunk() instanceof LevelChunk chunk)) return;
			Level world = chunk.getLevel();
			if (world.isClientSide) return;

			pendingChunks.computeIfAbsent(world, k -> new ArrayDeque<>()).add(chunk.getPos());
		}

		@SubscribeEvent
		public void onChunkUnload(ChunkEvent.Unload event)
		{
			if (!(event.getChunk() instanceof LevelChunk chunk)) return;
			Level world = chunk.getLevel();
			if (world.isClientSide) return;

			Map<BlockPos, Ae2EnergySink> worldSinks = sinks.get(world);
			if (worldSinks != null)
			{
				int chunkX = chunk.getPos().x;
				int chunkZ = chunk.getPos().z;
				Iterator<Map.Entry<BlockPos, Ae2EnergySink>> it = worldSinks.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry<BlockPos, Ae2EnergySink> entry = it.next();
					BlockPos pos = entry.getKey();
					if ((pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ)
					{
						EnergyNet.instance.removeTile(entry.getValue());
						it.remove();
					}
				}
			}
		}

		@SubscribeEvent
		public void onWorldTick(TickEvent.LevelTickEvent event)
		{
			if (event.phase != TickEvent.Phase.START) return;
			if (event.level.isClientSide()) return;
			if (!(event.level instanceof ServerLevel serverLevel)) return;

			processPendingChunks(serverLevel);
		}

		@SubscribeEvent
		public void onWorldUnload(LevelEvent.Unload event)
		{
			if (event.getLevel() instanceof Level world && !world.isClientSide)
			{
				unregisterAllInWorld(world);
			}
		}
	}

	private record Ae2EnergySink(Level world, BlockPos pos) implements ILocatable, IEnergySink
	{

		@Nullable
		private static IEnergyStorage getFeStorage(BlockEntity be)
		{
			IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
			if (storage != null && storage.canReceive()) return storage;

			for (Direction dir : Direction.values())
			{
				storage = be.getCapability(ForgeCapabilities.ENERGY, dir).orElse(null);
				if (storage != null && storage.canReceive()) return storage;
			}

			return null;
		}

		@Nullable
		private Object getGridNode()
		{
			BlockEntity be = world.getBlockEntity(pos);
			if (be == null) return null;

			try
			{
				Method getMainNode = be.getClass().getMethod("getMainNode");
				Object managedNode = getMainNode.invoke(be);
				if (managedNode != null)
				{
					Object node = managedNode.getClass().getMethod("getNode").invoke(managedNode);
					if (node != null) return node;
				}
			} catch (Exception ignored)
			{
			}

			for (Direction dir : Direction.values())
			{
				try
				{
					Object node = ae2GetExposedNodeMethod.invoke(null, world, pos, dir);
					if (node != null) return node;
				} catch (Exception ignored)
				{
				}
			}

			return null;
		}

		@Nullable
		private Object getEnergyGrid(Object gridNode)
		{
			try
			{
				Object grid = gridNode.getClass().getMethod("getGrid").invoke(gridNode);
				if (grid != null)
				{
					return ae2GetEnergyServiceMethod.invoke(grid);
				}
			} catch (Exception ignored)
			{
			}

			return null;
		}

		private double injectAePower(double aeAmount)
		{
			Object gridNode = getGridNode();
			if (gridNode == null) return aeAmount;

			Object energyService = getEnergyGrid(gridNode);
			if (energyService == null) return aeAmount;

			try
			{
				return (double) ae2InjectMethod.invoke(energyService, aeAmount, ae2ActionableModulate);
			} catch (Exception e)
			{
				IC2.log.debug(LogCategory.EnergyNet, "Failed to inject AE power: %s", e.getMessage());
			}

			return aeAmount;
		}

		private double injectFePower(double euAmount)
		{
			BlockEntity be = world.getBlockEntity(pos);
			if (be == null) return euAmount;

			IEnergyStorage storage = getFeStorage(be);
			if (storage == null) return euAmount;

			int feToSend = (int) Math.ceil(euAmount * EU_TO_AE_RATIO);
			if (feToSend <= 0) return euAmount;

			int feAccepted = storage.receiveEnergy(feToSend, false);
			double euAccepted = Math.min(feAccepted / EU_TO_AE_RATIO, euAmount);

			return euAmount - euAccepted;
		}

		private double getAeDemand()
		{
			Object gridNode = getGridNode();
			if (gridNode == null) return 0.0;

			Object energyService = getEnergyGrid(gridNode);
			if (energyService == null) return 0.0;

			try
			{
				double aeDemand = (double) ae2GetEnergyDemandMethod.invoke(energyService, Double.MAX_VALUE);
				return aeDemand / EU_TO_AE_RATIO;
			} catch (Exception ignored)
			{
			}

			return 0.0;
		}

		private double getFeDemand()
		{
			BlockEntity be = world.getBlockEntity(pos);
			if (be == null) return 0.0;

			IEnergyStorage storage = getFeStorage(be);
			if (storage == null) return 0.0;

			int feFree = storage.getMaxEnergyStored() - storage.getEnergyStored();
			return feFree / EU_TO_AE_RATIO;
		}

		@Override
		public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction direction)
		{
			return true;
		}

		@Override
		public double getDemandedEnergy()
		{
			if (ae2GridApiAvailable)
			{
				double demand = getAeDemand();
				if (demand > 0.0) return demand;
			}

			return getFeDemand();
		}

		@Override
		public int getSinkTier()
		{
			return 4;
		}

		@Override
		public double injectEnergy(Direction directionFrom, double amount, double voltage)
		{
			if (ae2GridApiAvailable)
			{
				double aeAmount = amount * EU_TO_AE_RATIO;
				double leftoverAe = injectAePower(aeAmount);
				double acceptedAe = aeAmount - leftoverAe;
				if (acceptedAe > 0.0)
				{
					return amount - acceptedAe / EU_TO_AE_RATIO;
				}
			}

			return injectFePower(amount);
		}

		@Override
		public Level getWorldObj()
		{
			return world;
		}

		@Override
		public BlockPos getPosition()
		{
			return pos;
		}
	}
}
