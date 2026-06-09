package ic2.core.energy.leg;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.energy.tile.IOverloadHandler;
import ic2.core.IC2;
import ic2.core.Ic2DamageSource;
import ic2.core.Ic2Explosion;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.energy.grid.EnergyNetSettings;
import ic2.core.energy.grid.Grid;
import ic2.core.energy.grid.IEnergyCalculator;
import ic2.core.energy.grid.Node;
import ic2.core.energy.grid.NodeLink;
import ic2.core.energy.grid.NodeType;
import ic2.core.energy.grid.Tile;
import ic2.core.init.MainConfig;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableDouble;

public class EnergyCalculatorLeg implements IEnergyCalculator
{
	@Override
	public void handleGridChange(Grid grid)
	{
		long startTime = 0L;
		EnergyCalculatorLeg.GridData data = getData(grid);
		updateCache(grid, data);
	}

	@Override
	public boolean runSyncStep(EnergyNetLocal enet)
	{
		boolean foundAny = false;

		for (Tile tile : enet.getSources())
		{
			IEnergySource source = (IEnergySource) tile.getMainTile();
			int packets = 1;
			IMultiEnergySource multiSource;
			double amount;
			if (!tile.isDisabled()
				&& (amount = source.getOfferedEnergy()) > 0.0
				&& (
				!(source instanceof IMultiEnergySource)
					|| !(multiSource = (IMultiEnergySource) source).sendMultipleEnergyPackets()
					|| (packets = multiSource.getMultipleEnergyPacketAmount()) > 0
			))
			{
				int tier = source.getSourceTier();
				if (tier < 0)
				{
					if (EnergyNetSettings.logGridCalculationIssues)
					{
						IC2.log
							.warn(
								LogCategory.EnergyNet,
								"Tile %s reported an invalid tier (%d).",
								Util.toString(source, enet.getWorld(), EnergyNet.instance.getPos(source)),
								tier
							);
					}

					tile.setSourceData(0.0, 0);
				} else
				{
					foundAny = true;
					double power = EnergyNet.instance.getPowerFromTier(tier);
					amount = Math.min(amount, power * packets);
					tile.setSourceData(amount, packets);
				}
			} else
			{
				tile.setSourceData(0.0, 0);
			}
		}

		return foundAny;
	}

	@Override
	public boolean runSyncStep(Grid grid)
	{
		long startTime = 0L;
		if (runCalculation(grid, getData(grid)))
		{
		}

		return false;
	}

	@Override
	public void runAsyncStep(Grid grid)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NodeStats getNodeStats(Tile tile)
	{
		double in = 0.0;
		double out = 0.0;
		double max = 0.0;

		for (Node node : tile.getNodes())
		{
			EnergyCalculatorLeg.GridData data = node.getGrid().getData();
			if (data != null && data.active)
			{
				int calcId = data.currentCalcId;
				Collection<EnergyPath> paths = getPaths(node, data);
				double sum = 0.0;

				for (EnergyPath path : paths)
				{
					if (path.lastCalcId == calcId)
					{
						sum += path.energySupplied;
						max = Math.max(path.maxPacketConducted, max);
					}
				}

				if (node.getType() == NodeType.Source)
				{
					out += sum;
				} else if (node.getType() == NodeType.Sink)
				{
					in += sum;
				} else
				{
					in += sum;
					out += sum;
				}
			}
		}

		return new NodeStats(in, out, EnergyNet.instance.getTierFromPower(max));
	}

	private static Collection<EnergyPath> getPaths(Node node, EnergyCalculatorLeg.GridData data)
	{
		if (node.getType() == NodeType.Source)
		{
			List<EnergyPath> ret = data.energySourceToEnergyPathMap.get(node);
			if (ret == null)
			{
				ret = Collections.emptyList();
			}

			return ret;
		} else
		{
			List<EnergyPath> ret = data.pathCache.get(node);
			if (ret != null)
			{
				return ret;
			}

			ret = new ArrayList<>();

			for (List<EnergyPath> paths : data.energySourceToEnergyPathMap.values())
			{
				for (EnergyPath path : paths)
				{
					if (node.getType() == NodeType.Sink)
					{
						if (path.target == node)
						{
							ret.add(path);
						}
					} else if (path.conductors.contains(node))
					{
						ret.add(path);
					}
				}
			}

			data.pathCache.put(node, ret);
			return ret;
		}
	}

	@Override
	public void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat)
	{
		EnergyCalculatorLeg.GridData data = getData(node.getGrid());
		Collection<EnergyPath> paths = getPaths(node, data);
		switch (node.getType())
		{
			case Source:
				chat.printf("%s%d connected sink nodes%n", prefix, paths.size());
				break;
			case Sink:
				chat.printf("%s%d connected source nodes%n", prefix, paths.size());
				break;
			case Conductor:
				chat.printf("%s%d paths across this conductor%n", prefix, paths.size());
		}

		double sum = 0.0;
		double max = 0.0;
		int calcId = data.currentCalcId;
		int pathsToPrint = 8;
		int n = 0;

		for (EnergyPath path : paths)
		{
			boolean printPathEnergy = false;
			if (n < 8)
			{
				switch (node.getType())
				{
					case Source:
						chat.printf("%s %s", prefix, path.target);
						break;
					case Sink:
						chat.printf("%s %s", prefix, path.source);
						break;
					case Conductor:
						chat.printf("%s %s -> %s", prefix, path.source, path.target);
				}

				printPathEnergy = true;
			} else if (n == 8)
			{
				chat.printf("%s ... (%d more)%n", paths.size() - 8);
			}

			n++;
			if (path.lastCalcId != calcId)
			{
				if (printPathEnergy)
				{
					chat.println(" (idle)");
				}
			} else
			{
				if (printPathEnergy)
				{
					chat.printf(" (%.2f EU, max packet %.2f EU)%n", path.energySupplied, path.maxPacketConducted);
				}

				sum += path.energySupplied;
				max = Math.max(path.maxPacketConducted, max);
			}
		}

		chat.printf("%slast tick: %.2f EU, max packet %.2f EU%n", prefix, sum, max);
	}

	private static void updateCache(Grid grid, EnergyCalculatorLeg.GridData data)
	{
		data.active = false;
		data.energySourceToEnergyPathMap.clear();
		data.activeSources.clear();
		data.activeSinks.clear();
		data.pathCache.clear();
		data.currentCalcId = -1;
		Collection<Node> nodes = grid.getNodes();
		if (nodes.size() >= 2)
		{
			List<Node> sources = new ArrayList<>();
			int sinkCount = 0;

			for (Node node : nodes)
			{
				if (node.getType() == NodeType.Source)
				{
					sources.add(node);
				} else if (node.getType() == NodeType.Sink)
				{
					sinkCount++;
				}
			}

			if (!sources.isEmpty() && sinkCount != 0)
			{
				Map<Node, Node> path = new IdentityHashMap<>();
				final Map<Node, Double> lossMap = new IdentityHashMap<>();
				Queue<Node> queue;
				if (sources.size() <= 2048)
				{
					queue = new PriorityQueue<>(nodes.size(), new Comparator<Node>()
					{
						public int compare(Node a, Node b)
						{
							return lossMap.get(a).compareTo(lossMap.get(b));
						}
					});
				} else
				{
					queue = new ArrayDeque<>(nodes.size());
				}

				Map<IEnergyTile, EnergyPath> paths = new LinkedHashMap<>();

				for (Node srcNode : sources)
				{
					lossMap.put(srcNode, 0.0);
					queue.add(srcNode);

					Node node;
					while ((node = queue.poll()) != null)
					{
						if (node.getType() == NodeType.Sink)
						{
							double loss = lossMap.get(node);
							IEnergyTile tile = node.getTile().getMainTile();
							EnergyPath prev = paths.get(tile);
							if (prev == null || !(prev.loss <= loss))
							{
								if (EnergyNetSettings.roundLossDown)
								{
									loss = Math.floor(loss);
								}

								paths.put(tile, new EnergyPath(srcNode, node, reconstructPath(srcNode, node, path), loss));
								if (paths.size() == sinkCount)
								{
									break;
								}
							}
						} else if (node.getType() == NodeType.Conductor || node == srcNode)
						{
							double loss = lossMap.get(node);

							for (NodeLink link : node.getLinks())
							{
								Node neighbor = link.getNeighbor(node);
								if (neighbor.getType() == NodeType.Source)
								{
									List<EnergyPath> srcPaths = data.energySourceToEnergyPathMap.get(neighbor);
									if (srcPaths != null)
									{
										if (srcPaths.isEmpty())
										{
											break;
										}

										loss -= link.getLoss();
										List<Node> pathToHere = null;

										for (EnergyPath cPath : srcPaths)
										{
											double cLoss = loss + cPath.loss;
											IEnergyTile tile = cPath.target.getTile().getMainTile();
											EnergyPath prev = paths.get(tile);
											if (prev == null || !(prev.loss <= cLoss))
											{
												if (EnergyNetSettings.roundLossDown)
												{
													cLoss = Math.floor(cLoss);
												}

												if (pathToHere == null)
												{
													pathToHere = reconstructPath(srcNode, node, path);
												}

												List<Node> conductors = new ArrayList<>(pathToHere.size() + cPath.conductors.size());
												conductors.addAll(pathToHere);
												conductors.addAll(cPath.conductors);
												paths.put(tile, new EnergyPath(srcNode, cPath.target, conductors, cLoss));
											}
										}
										break;
									}
								} else
								{
									double newLoss = loss + link.getLoss();
									Double prevLoss = lossMap.get(neighbor);
									if (prevLoss == null || prevLoss > newLoss)
									{
										if (prevLoss != null)
										{
											queue.remove(neighbor);
										}

										lossMap.put(neighbor, newLoss);
										path.put(neighbor, node);
										queue.add(neighbor);
									}
								}
							}
						}
					}

					if (!paths.isEmpty())
					{
						data.energySourceToEnergyPathMap.put(srcNode, new ArrayList<>(paths.values()));
					}

					lossMap.clear();
					path.clear();
					paths.clear();
					queue.clear();
				}

				if (!data.energySourceToEnergyPathMap.isEmpty())
				{
					data.active = true;
				}
			}
		}
	}

	private static List<Node> reconstructPath(Node srcNode, Node dstNode, Map<Node, Node> path)
	{
		List<Node> ret = new ArrayList<>();
		Node node = dstNode;

		while ((node = path.get(node)) != srcNode)
		{
			assert node != null;
			ret.add(node);
		}

		Collections.reverse(ret);
		return ret;
	}

	private static boolean runCalculation(Grid grid, EnergyCalculatorLeg.GridData data)
	{
		if (!data.active)
		{
			return false;
		}

		List<Node> activeSources = data.activeSources;
		Map<Node, MutableDouble> activeSinks = data.activeSinks;
		activeSources.clear();
		activeSinks.clear();
		int calcId = ++data.currentCalcId;

		for (Node node : grid.getNodes())
		{
			Tile tile = node.getTile();
			if (!tile.isDisabled())
			{
				if (node.getType() == NodeType.Source && data.energySourceToEnergyPathMap.containsKey(node) && tile.getAmount() > 0.0)
				{
					activeSources.add(node);
				} else
				{
					double amount;
					if (node.getType() == NodeType.Sink && (amount = ((IEnergySink) tile.getMainTile()).getDemandedEnergy()) > 0.0)
					{
						activeSinks.put(node, new MutableDouble(amount));
					}
				}
			}
		}

		if (!activeSources.isEmpty() && !activeSinks.isEmpty())
		{
			Level world = grid.getEnergyNet().getWorld();
			RandomSource rand = world.random;
			boolean shufflePaths = (world.getGameTime() & 3L) != 0L;
			int sourcesOffset;
			if (activeSources.size() > 1)
			{
				sourcesOffset = rand.nextInt(activeSources.size());
			} else
			{
				sourcesOffset = 0;
			}

			for (int i = sourcesOffset; i < activeSources.size() && !activeSinks.isEmpty(); i++)
			{
				distribute(activeSources.get(i), data, shufflePaths, calcId, rand);
			}

			for (int i = 0; i < sourcesOffset && !activeSinks.isEmpty(); i++)
			{
				distribute(activeSources.get(i), data, shufflePaths, calcId, rand);
			}

			if (!data.eventPaths.isEmpty())
			{
				applyCableEffects(data.eventPaths, grid.getEnergyNet().getWorld());
				data.eventPaths.clear();
			}

			return true;
		} else
		{
			return false;
		}
	}

	private static void distribute(Node srcNode, EnergyCalculatorLeg.GridData data, boolean shufflePaths, int calcId, RandomSource rand)
	{
		Tile tile = srcNode.getTile();
		int packetCount = tile.getPacketCount();
		assert packetCount > 0;
		List<EnergyPath> paths = data.energySourceToEnergyPathMap.get(srcNode);
		int pathOffset;
		if (paths.size() > 1 && shufflePaths)
		{
			pathOffset = rand.nextInt(paths.size());
		} else
		{
			pathOffset = 0;
		}

		double totalOffer = tile.getAmount();
		assert totalOffer > 0.0;
		double offer;
		if (packetCount == 1)
		{
			offer = distributeSingle(totalOffer, tile, paths, pathOffset, data, calcId);
		} else
		{
			offer = distributeMultiple(totalOffer, tile, paths, pathOffset, data, calcId, packetCount);
		}

		double used = totalOffer - Math.max(0.0, offer);
		if (used > 0.0)
		{
			tile.setAmount(offer);
			((IEnergySource) tile.getMainTile()).drawEnergy(used);
		}
	}

	private static double distributeSingle(double offer, Tile tile, List<EnergyPath> paths, int pathOffset, EnergyCalculatorLeg.GridData data, int calcId)
	{
		for (int i = pathOffset; i < paths.size(); i++)
		{
			offer -= emit(paths.get(i), offer, data, calcId);
			if (offer <= 0.0)
			{
				break;
			}
		}

		for (int i = 0; i < pathOffset && offer > 0.0; i++)
		{
			offer -= emit(paths.get(i), offer, data, calcId);
		}

		return offer;
	}

	private static double distributeMultiple(
		double offer, Tile tile, List<EnergyPath> paths, int pathOffset, EnergyCalculatorLeg.GridData data, int calcId, int packetCount
	)
	{
		IEnergySource source = (IEnergySource) tile.getMainTile();
		double power = EnergyNet.instance.getPowerFromTier(source.getSourceTier());

		do
		{
			double cOffer = Math.min(offer, power);
			double used = cOffer - distributeSingle(cOffer, tile, paths, pathOffset, data, calcId);
			if (used <= 0.0)
			{
				break;
			}

			offer -= used;
		} while (--packetCount > 0 && offer > 0.0);

		return offer;
	}

	private static double emit(EnergyPath path, double offer, EnergyCalculatorLeg.GridData data, int calcId)
	{
		Tile targetTile = path.target.getTile();
		if (targetTile.isDisabled())
		{
			return 0.0;
		}

		double injectAmount = offer - path.loss;
		if (injectAmount <= 0.0)
		{
			return 0.0;
		}

		MutableDouble sinkDemand = data.activeSinks.get(path.target);
		if (sinkDemand == null)
		{
			return 0.0;
		}

		IEnergySink sink = (IEnergySink) targetTile.getMainTile();
		double amount = Math.min(injectAmount, sinkDemand.doubleValue());
		double rejected = sink.injectEnergy(path.targetDirection, amount, EnergyNet.instance.getTierFromPower(amount));
		if (rejected >= amount)
		{
			return 0.0;
		}

		double effectiveAmount = Math.max(0.0, amount - rejected + path.loss);
		if (path.lastCalcId != calcId)
		{
			path.lastCalcId = calcId;
			path.energySupplied = 0.0;
			path.maxPacketConducted = 0.0;
		}

		path.energySupplied += amount - rejected;
		path.maxPacketConducted = Math.max(effectiveAmount, path.maxPacketConducted);
		if (effectiveAmount > path.minEffectEnergy || amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier()))
		{
			data.eventPaths.add(path);
		}

		if (amount >= sinkDemand.doubleValue() || rejected > 0.0)
		{
			data.activeSinks.remove(path.target);
		}

		return effectiveAmount;
	}

	private static void applyCableEffects(Collection<EnergyPath> eventPaths, Level world)
	{
		if (MainConfig.get().get("misc/enableEnetCableMeltdown").getBool())
		{
			Set<Tile> cablesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
			Set<Tile> cablesToStrip = Collections.newSetFromMap(new IdentityHashMap<>());
			Map<Tile, MutableDouble> sinksToExplode = new IdentityHashMap<>();
			Map<LivingEntity, MutableDouble> shockEnergyMap = new IdentityHashMap<>();

			for (EnergyPath path : eventPaths)
			{
				double amount = path.maxPacketConducted;
				boolean conductorOverload = false;
				if (amount > path.minConductorBreakdownEnergy || amount > path.minInsulationBreakdownEnergy)
				{
					conductorOverload = true;

					for (Node node : path.conductors)
					{
						Tile tile = node.getTile();
						IEnergyConductor conductor = (IEnergyConductor) tile.getMainTile();
						if (amount > conductor.getConductorBreakdownEnergy())
						{
							cablesToRemove.add(tile);
						} else if (amount > conductor.getInsulationBreakdownEnergy())
						{
							cablesToStrip.add(tile);
						}
					}
				}

				if (amount > path.minInsulationEnergyAbsorption)
				{
					List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(
						LivingEntity.class,
						new AABB(path.minX - 1, path.minY - 1, path.minZ - 1, path.maxX + 2, path.maxY + 2, path.maxZ + 2),
						EntitySelector.LIVING_ENTITY_STILL_ALIVE
					);
					if (!nearbyEntities.isEmpty())
					{
						Map<LivingEntity, MutableDouble> localShockEnergyMap = new IdentityHashMap<>();

						for (Node node : path.conductors)
						{
							Tile tile = node.getTile();
							IEnergyConductor conductor = (IEnergyConductor) tile.getMainTile();
							if (!(amount <= conductor.getInsulationEnergyAbsorption()))
							{
								int shockEnergy = (int) (amount - conductor.getInsulationEnergyAbsorption());

								for (IEnergyTile subTile : tile.getSubTiles())
								{
									BlockPos pos = EnergyNet.instance.getPos(subTile);

									for (LivingEntity entity : nearbyEntities)
									{
										MutableDouble prev = localShockEnergyMap.get(entity);
										if ((prev == null || !(prev.doubleValue() >= shockEnergy))
											&& entity.getBoundingBox()
											.intersects(
												new AABB(
													pos.getX() - 1,
													pos.getY() - 1,
													pos.getZ() - 1,
													pos.getX() + 2,
													pos.getY() + 2,
													pos.getZ() + 2
												)
											))
										{
											if (prev == null)
											{
												localShockEnergyMap.put(entity, new MutableDouble(shockEnergy));
											} else
											{
												prev.setValue(shockEnergy);
											}
										}
									}
								}
							}
						}

						for (Entry<LivingEntity, MutableDouble> entry : localShockEnergyMap.entrySet())
						{
							MutableDouble prev = shockEnergyMap.get(entry.getKey());
							if (prev == null)
							{
								shockEnergyMap.put(entry.getKey(), entry.getValue());
							} else
							{
								prev.add(entry.getValue().doubleValue());
							}
						}
					}
				}

				Tile sinkTile = path.target.getTile();
				IEnergySink sink = (IEnergySink) sinkTile.getMainTile();
				if (conductorOverload || amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier()))
				{
					MutableDouble prev = sinksToExplode.get(sinkTile);
					if (prev == null)
					{
						sinksToExplode.put(sinkTile, new MutableDouble(amount));
					} else if (prev.doubleValue() < amount)
					{
						prev.setValue(amount);
					}
				}
			}

			cablesToStrip.removeAll(cablesToRemove);

			for (Tile tile : cablesToRemove)
			{
				((IEnergyConductor) tile.getMainTile()).removeConductor();
			}

			for (Tile tile : cablesToStrip)
			{
				((IEnergyConductor) tile.getMainTile()).removeInsulation();
			}

			for (Entry<Tile, MutableDouble> entry : sinksToExplode.entrySet())
			{
				explodeTile(world, entry.getKey(), entry.getValue().doubleValue());
			}

			for (Entry<LivingEntity, MutableDouble> entry : shockEnergyMap.entrySet())
			{
				LivingEntity target = entry.getKey();
				int damage = (int) Math.ceil(entry.getValue().doubleValue() / 64.0);
				if (target.isAlive() && damage > 0)
				{	
					if (Ic2DamageSource.electricity == null)
					{
						Ic2DamageSource.init(target.level().registryAccess());
					}
					target.hurt(Ic2DamageSource.electricity, damage);
				}
			}
		}
	}

	private static EnergyCalculatorLeg.GridData getData(Grid grid)
	{
		EnergyCalculatorLeg.GridData ret = grid.getData();
		if (ret == null)
		{
			ret = new EnergyCalculatorLeg.GridData();
			grid.setData(ret);
		}

		return ret;
	}

	private static void explodeTile(Level world, Tile tile, double maxPower)
	{
		if (MainConfig.get().get("misc/enableEnetExplosions").getBool())
		{
			int tier = EnergyNet.instance.getTierFromPower(maxPower);

			for (IEnergyTile subTile : tile.getSubTiles())
			{
				IEnergySink mainTile = (IEnergySink) tile.getMainTile();
				BlockPos pos = EnergyNet.instance.getPos(subTile);
				BlockEntity realTe = world.getBlockEntity(pos);
				if (!(mainTile instanceof IOverloadHandler handler && handler.onOverload(tier))
					&& !(realTe instanceof IOverloadHandler handler2 && handler2.onOverload(tier)))
				{
					float power = 2.5F;
					if (mainTile instanceof IExplosionPowerOverride override)
					{
						if (!override.shouldExplode())
						{
							continue;
						}

						power = override.getExplosionPower(tier, power);
					} else if (realTe instanceof IExplosionPowerOverride override)
					{
						if (!override.shouldExplode())
						{
							continue;
						}

						power = override.getExplosionPower(tier, power);
					}

					Player closestPlayer = world.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20.0, false);
					world.removeBlock(pos, false);
					Ic2Explosion explosion = new Ic2Explosion(
						world, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, 0.75F, Ic2Explosion.Type.Electrical
					);
					explosion.doExplosion();
				}
			}
		}
	}

	private static class GridData
	{
		boolean active;
		final Map<Node, List<EnergyPath>> energySourceToEnergyPathMap = new IdentityHashMap<>();
		final List<Node> activeSources = new ArrayList<>();
		final Map<Node, MutableDouble> activeSinks = new IdentityHashMap<>();
		final Set<EnergyPath> eventPaths = Collections.newSetFromMap(new IdentityHashMap<>());
		final Map<Node, List<EnergyPath>> pathCache = new IdentityHashMap<>();
		int currentCalcId = -1;
	}
}
