package ic2.core.energy.grid;

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
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableDouble;

public class EnergyCalculatorUnified implements IEnergyCalculator
{
	@Override
	public void handleGridChange(Grid grid)
	{
		GridData data = getData(grid);
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
			if (!tile.isDisabled() && (amount = source.getOfferedEnergy()) > 0.0 && (!(source instanceof IMultiEnergySource) || !(multiSource = (IMultiEnergySource) source).sendMultipleEnergyPackets() || (packets = multiSource.getMultipleEnergyPacketAmount()) > 0))
			{
				int tier = source.getSourceTier();
				if (tier < 0)
				{
					if (EnergyNetSettings.logGridCalculationIssues)
					{
						IC2.log.warn(LogCategory.EnergyNet, "Tile %s reported an invalid tier (%d).", Util.toString(source, enet.getWorld(), EnergyNet.instance.getPos(source)), tier);
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
		GridData data = getData(grid);
		return runCalculation(grid, data);
	}

	@Override
	public void runAsyncStep(Grid grid)
	{
		GridData data = getData(grid);
		if (data.active)
		{
			runCalculation(grid, data);
		}
	}

	@Override
	public NodeStats getNodeStats(Tile tile)
	{
		double in = 0.0;
		double out = 0.0;
		double max = 0.0;

		for (Node node : tile.getNodes())
		{
			GridData data = node.getGrid().getData();
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

		return new NodeStats(in, out, max);
	}

	private static Collection<EnergyPath> getPaths(Node node, GridData data)
	{
		List<EnergyPath> ret;
		if (node.getType() == NodeType.Source)
		{
			ret = data.energySourceToEnergyPathMap.get(node);
			if (ret == null)
			{
				ret = Collections.emptyList();
			}
		} else
		{
			ret = data.pathCache.get(node);
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
		}
		return ret;
	}

	@Override
	public void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat)
	{
		GridData data = getData(node.getGrid());
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
				chat.printf("%d more %n", paths.size() - 8);
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

		chat.printf("%s last tick: %.2f EU, max packet %.2f EU%n", prefix, sum, max);
	}

	private static void updateCache(Grid grid, GridData data)
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
				OptimizedGraph optGraph = buildOptimizedGraph(nodes);
				Map<Node, Node> parentMap = new IdentityHashMap<>();
				Map<Node, OptLink> incomingLinkMap = new IdentityHashMap<>();
				final Map<Node, Double> lossMap = new IdentityHashMap<>();
				Queue<Node> queue;
				if (sources.size() <= EnergyNetSettings.bfsThreshold)
				{
					queue = new PriorityQueue<>(nodes.size(), Comparator.comparing(lossMap::get));
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

								paths.put(tile, new EnergyPath(srcNode, node, reconstructPathWithLinks(srcNode, node, parentMap, incomingLinkMap), loss));
								if (paths.size() == sinkCount)
								{
									break;
								}
							}
						} else if (node.getType() == NodeType.Conductor || node == srcNode)
						{
							double loss = lossMap.get(node);

							List<OptLink> optLinks = optGraph.nodeToLinks.get(node);
							if (optLinks != null)
							{
								for (OptLink optLink : optLinks)
								{
									processLink(optLink, node, srcNode, loss, lossMap, parentMap, incomingLinkMap, queue, paths, data, sinkCount);
								}
							}
						}
					}

					if (!paths.isEmpty())
					{
						data.energySourceToEnergyPathMap.put(srcNode, new ArrayList<>(paths.values()));
					}

					lossMap.clear();
					parentMap.clear();
					incomingLinkMap.clear();
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

	private static void processLink(NodeLink link, Node node, Node srcNode, double loss, Map<Node, Double> lossMap, Map<Node, Node> parentMap, Queue<Node> queue, Map<IEnergyTile, EnergyPath> paths, GridData data, int sinkCount)
	{
		Node neighbor = link.getNeighbor(node);
		if (neighbor.getType() == NodeType.Source)
		{
			List<EnergyPath> srcPaths = data.energySourceToEnergyPathMap.get(neighbor);
			if (srcPaths != null)
			{
				if (srcPaths.isEmpty())
				{
					return;
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
							pathToHere = reconstructPath(srcNode, node, parentMap);
						}

						List<Node> conductors = new ArrayList<>(pathToHere.size() + cPath.conductors.size());
						conductors.addAll(pathToHere);
						conductors.addAll(cPath.conductors);
						paths.put(tile, new EnergyPath(srcNode, cPath.target, conductors, cLoss));
					}
				}
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
				parentMap.put(neighbor, node);
				queue.add(neighbor);
			}
		}
	}

	private static void processLink(OptLink optLink, Node node, Node srcNode, double loss, Map<Node, Double> lossMap, Map<Node, Node> parentMap, Map<Node, OptLink> incomingLinkMap, Queue<Node> queue, Map<IEnergyTile, EnergyPath> paths, GridData data, int sinkCount)
	{
		Node neighbor = optLink.getNeighbor(node);
		if (neighbor.getType() == NodeType.Source)
		{
			List<EnergyPath> srcPaths = data.energySourceToEnergyPathMap.get(neighbor);
			if (srcPaths != null)
			{
				if (srcPaths.isEmpty())
				{
					return;
				}

				loss -= optLink.loss;
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
							pathToHere = reconstructPathWithLinks(srcNode, node, parentMap, incomingLinkMap);
						}

						List<Node> conductors = new ArrayList<>(pathToHere.size() + optLink.skippedNodes.size() + cPath.conductors.size());
						conductors.addAll(pathToHere);
						conductors.addAll(optLink.skippedNodes);
						conductors.addAll(cPath.conductors);
						Direction targetDir = cPath.conductors.isEmpty() ? cPath.targetDirection : null;
						paths.put(tile, new EnergyPath(srcNode, cPath.target, conductors, cLoss, targetDir));
					}
				}
			}
		} else
		{
			double newLoss = loss + optLink.loss;
			Double prevLoss = lossMap.get(neighbor);
			if (prevLoss == null || prevLoss > newLoss)
			{
				if (prevLoss != null)
				{
					queue.remove(neighbor);
				}

				lossMap.put(neighbor, newLoss);
				parentMap.put(neighbor, node);
				incomingLinkMap.put(neighbor, optLink);
				queue.add(neighbor);
			}
		}
	}

	private static OptimizedGraph buildOptimizedGraph(Collection<Node> nodes)
	{
		Map<Node, List<OptLink>> nodeToLinks = new IdentityHashMap<>();

		for (Node node : nodes)
		{
			nodeToLinks.put(node, new ArrayList<>());
		}

		for (Node node : nodes)
		{
			for (NodeLink link : node.getLinks())
			{
				if (link.nodeA == node)
				{
					OptLink optLink = new OptLink(link.nodeA, link.nodeB, link.getLoss(), new ArrayList<>());
					nodeToLinks.get(link.nodeA).add(optLink);
					nodeToLinks.get(link.nodeB).add(optLink);
				}
			}
		}

		boolean changed;
		do
		{
			changed = false;
			java.util.Iterator<java.util.Map.Entry<Node, List<OptLink>>> it = nodeToLinks.entrySet().iterator();

			while (it.hasNext())
			{
				java.util.Map.Entry<Node, List<OptLink>> entry = it.next();
				Node node = entry.getKey();
				if (node.getType() != NodeType.Conductor) continue;

				List<OptLink> links = entry.getValue();
				if (links.isEmpty())
				{
					it.remove();
					changed = true;
				} else if (links.size() == 1)
				{
					OptLink link = links.get(0);
					Node neighbor = link.getNeighbor(node);
					List<OptLink> neighborLinks = nodeToLinks.get(neighbor);
					if (neighborLinks != null)
					{
						neighborLinks.remove(link);
					}
					it.remove();
					changed = true;
				} else if (links.size() == 2)
				{
					OptLink link1 = links.get(0);
					OptLink link2 = links.get(1);
					Node neighbor1 = link1.getNeighbor(node);
					Node neighbor2 = link2.getNeighbor(node);

					List<OptLink> links1 = nodeToLinks.get(neighbor1);
					List<OptLink> links2 = nodeToLinks.get(neighbor2);
					if (links1 != null) links1.remove(link1);
					if (links2 != null) links2.remove(link2);

					if (neighbor1 != neighbor2)
					{
						List<Node> skipped = new ArrayList<>(link1.skippedNodes);
						skipped.add(node);
						skipped.addAll(link2.skippedNodes);

						double combinedLoss = link1.loss + link2.loss;
						OptLink merged = new OptLink(neighbor1, neighbor2, combinedLoss, skipped);
						if (links1 != null) links1.add(merged);
						if (links2 != null) links2.add(merged);
					}

					it.remove();
					changed = true;
				}
			}
		} while (changed);

		return new OptimizedGraph(nodeToLinks);
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

	private static List<Node> reconstructPathWithLinks(Node srcNode, Node dstNode, Map<Node, Node> parentMap, Map<Node, OptLink> incomingLinkMap)
	{
		List<Node> ret = new ArrayList<>();
		Node node = dstNode;

		while (true)
		{
			Node parent = parentMap.get(node);
			if (parent == null) break;

			OptLink link = incomingLinkMap.get(node);
			if (link != null)
			{
				List<Node> skipped;
				if (link.nodeA == parent)
				{
					skipped = new ArrayList<>(link.skippedNodes);
					Collections.reverse(skipped);
				}
				else
				{
					skipped = link.skippedNodes;
				}
				ret.addAll(skipped);
			}

			if (parent == srcNode) break;
			ret.add(parent);
			node = parent;
		}

		Collections.reverse(ret);
		return ret;
	}

	private static boolean runCalculation(Grid grid, GridData data)
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

	private static void distribute(Node srcNode, GridData data, boolean shufflePaths, int calcId, RandomSource rand)
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
			offer = distributeSingle(totalOffer, paths, pathOffset, data, calcId);
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

	private static double distributeSingle(double offer, List<EnergyPath> paths, int pathOffset, GridData data, int calcId)
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

	private static double distributeMultiple(double offer, Tile tile, List<EnergyPath> paths, int pathOffset, GridData data, int calcId, int packetCount)
	{
		IEnergySource source = (IEnergySource) tile.getMainTile();
		double power = EnergyNet.instance.getPowerFromTier(source.getSourceTier());

		do
		{
			double cOffer = Math.min(offer, power);
			double used = cOffer - distributeSingle(cOffer, paths, pathOffset, data, calcId);
			if (used <= 0.0)
			{
				break;
			}

			offer -= used;
		} while (--packetCount > 0 && offer > 0.0);

		return offer;
	}

	private static double emit(EnergyPath path, double offer, GridData data, int calcId)
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
				if (amount > path.minConductorBreakdownEnergy || amount > path.minInsulationBreakdownEnergy)
				{
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
					List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, new AABB(path.minX - 1, path.minY - 1, path.minZ - 1, path.maxX + 2, path.maxY + 2, path.maxZ + 2), EntitySelector.LIVING_ENTITY_STILL_ALIVE);
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
										if ((prev == null || !(prev.doubleValue() >= shockEnergy)) && entity.getBoundingBox().intersects(new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2)))
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
				if (amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier()))
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
				if (!(mainTile instanceof IOverloadHandler handler && handler.onOverload(tier)) && !(realTe instanceof IOverloadHandler handler2 && handler2.onOverload(tier)))
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

					world.removeBlock(pos, false);
					Ic2Explosion explosion = new Ic2Explosion(world, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, 0.75F, Ic2Explosion.Type.Electrical);
					explosion.doExplosion();
				}
			}
		}
	}

	private static GridData getData(Grid grid)
	{
		GridData ret = grid.getData();
		if (ret == null)
		{
			ret = new GridData();
			grid.setData(ret);
		}

		return ret;
	}

	private record OptLink(Node nodeA, Node nodeB, double loss, List<Node> skippedNodes)
	{
		Node getNeighbor(Node node)
		{
			return nodeA == node ? nodeB : nodeA;
		}
	}

	private record OptimizedGraph(Map<Node, List<OptLink>> nodeToLinks)
	{
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
