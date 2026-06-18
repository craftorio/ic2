package ic2.core.energy;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Direction;
import org.apache.logging.log4j.Level;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.ops.MatrixIO;

public class EnergyCalculatorEjml implements IEnergyCalculator
{
	public static final boolean useLinearTransferModel = ConfigUtil.getBool(MainConfig.get(), "misc/useLinearTransferModel");

	private static final boolean debugGrid = System.getProperty("ic2.energynet.debuggrid") != null;
	private static final boolean debugGridVerbose = debugGrid && "verbose".equals(System.getProperty("ic2.energynet.debuggrid"));

	private static boolean verifyGrid()
	{
		return Util.hasAssertions();
	}

	@Override
	public void handleGridChange(Grid grid)
	{
		getData(grid).invalidate();
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
		data.prepareCalculation();
		return !data.activeSources.isEmpty() && !data.activeSinks.isEmpty();
	}

	@Override
	public void runAsyncStep(Grid grid)
	{
		GridData data = getData(grid);
		if (data.activeSources.isEmpty() || data.activeSinks.isEmpty())
		{
			return;
		}

		data.calculate();
	}

	@Override
	public NodeStats getNodeStats(Tile tile)
	{
		double in = 0.0;
		double out = 0.0;
		double voltage = 0.0;

		for (Node node : tile.getNodes())
		{
			Grid grid = node.getGrid();
			if (grid != null)
			{
				GridData data = grid.getData();
				if (data != null)
				{
					NodeStats stats = data.lastStats.get(node);
					if (stats != null)
					{
						in += stats.getEnergyIn();
						out += stats.getEnergyOut();
						voltage = Math.max(voltage, stats.getVoltage());
					}
				}
			}
		}

		return new NodeStats(in, out, voltage);
	}

	@Override
	public void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat)
	{
		Grid grid = node.getGrid();
		if (grid == null) return;
		GridData data = getData(grid);
		EjmlNode ejmlNode = data.nodeMap.get(node);
		if (ejmlNode == null)
		{
			chat.printf("%s(optimized away)%n", prefix);
			return;
		}

		chat.printf("%svoltage: %.4f V%n", prefix, ejmlNode.voltage);
		chat.printf("%scurrent in: %.4f A, out: %.4f A%n", prefix, ejmlNode.currentIn, ejmlNode.currentOut);
		if (ejmlNode.nodeType == NodeType.Source)
		{
			chat.printf("%samount: %.4f EU%n", prefix, ejmlNode.amount);
		}
	}

	private static GridData getData(Grid grid)
	{
		GridData ret = grid.getData();
		if (ret == null)
		{
			ret = new GridData(grid);
			grid.setData(ret);
		}

		return ret;
	}

	static class EjmlNode
	{
		final Node enetNode;
		final NodeType nodeType;
		int tier;
		double amount;
		double resistance;
		double voltage = Double.NaN;
		double currentIn;
		double currentOut;
		final double maxCurrent;
		EjmlNode parent;
		final List<EjmlLink> links = new ArrayList<>();

		EjmlNode(Node enetNode)
		{
			this.enetNode = enetNode;
			this.nodeType = enetNode.getType();
			IEnergyTile mainTile = enetNode.getTile().getMainTile();
			switch (this.nodeType)
			{
				case Source:
					IEnergySource source = (IEnergySource) mainTile;
					this.tier = source.getSourceTier();
					this.amount = source.getOfferedEnergy();
					this.maxCurrent = Double.MAX_VALUE;
					break;
				case Sink:
					IEnergySink sink = (IEnergySink) mainTile;
					this.tier = sink.getSinkTier();
					this.amount = sink.getDemandedEnergy();
					this.maxCurrent = Double.MAX_VALUE;
					break;
				case Conductor:
					this.tier = 0;
					this.amount = 0.0;
					this.maxCurrent = ((IEnergyConductor) mainTile).getConductorBreakdownEnergy();
					break;
				default:
					throw new RuntimeException("invalid nodetype: " + this.nodeType);
			}

			if (this.amount < 0.0) this.amount = 0.0;
		}

		EjmlNode(EjmlNode node)
		{
			this.enetNode = node.enetNode;
			this.nodeType = node.nodeType;
			this.tier = node.tier;
			this.amount = node.amount;
			this.maxCurrent = node.maxCurrent;
			this.parent = node;

			for (EjmlLink link : node.links)
			{
				this.links.add(new EjmlLink(link));
			}
		}

		EjmlNode getTop()
		{
			return this.parent != null ? this.parent.getTop() : this;
		}

		double getInnerLoss()
		{
			return switch (this.nodeType)
			{
				case Source, Sink -> 0.0;
				case Conductor -> ((IEnergyConductor) this.enetNode.getTile().getMainTile()).getConductionLoss();
			};
		}

		EjmlLink getConnectionTo(EjmlNode node)
		{
			for (EjmlLink link : this.links)
			{
				if (link.getNeighbor(this) == node)
				{
					return link;
				}
			}

			return null;
		}

		void resetCurrents()
		{
			this.getTop().currentIn = 0.0;
			this.getTop().currentOut = 0.0;
		}

		void addCurrent(double current)
		{
			if (current >= 0.0)
			{
				this.getTop().currentIn += current;
			} else
			{
				this.getTop().currentOut -= current;
			}
		}

		@Override
		public String toString()
		{
			String type = switch (this.nodeType)
			{
				case Source -> "E";
				case Sink -> "A";
				case Conductor -> "C";
			};

			return this.enetNode.getTile().getMainTile().getClass().getSimpleName().replace("TileEntity", "") + "|" + type + "|" + this.tier + "|" + this.enetNode.uid;
		}
	}

	static class EjmlLink
	{
		EjmlNode nodeA;
		EjmlNode nodeB;
		Direction dirFromA;
		Direction dirFromB;
		double loss;
		List<EjmlNode> skippedNodes = new ArrayList<>();

		EjmlLink(EjmlNode nodeA, EjmlNode nodeB, double loss, Direction dirFromA, Direction dirFromB)
		{
			this.nodeA = nodeA;
			this.nodeB = nodeB;
			this.loss = loss;
			this.dirFromA = dirFromA;
			this.dirFromB = dirFromB;
		}

		EjmlLink(EjmlLink link)
		{
			this(link.nodeA, link.nodeB, link.loss, link.dirFromA, link.dirFromB);
			this.skippedNodes.addAll(link.skippedNodes);
		}

		EjmlNode getNeighbor(EjmlNode node)
		{
			return this.nodeA == node ? this.nodeB : this.nodeA;
		}

		EjmlNode getNeighbor(int uid)
		{
			return this.nodeA.enetNode.uid == uid ? this.nodeB : this.nodeA;
		}

		void replaceNode(EjmlNode oldNode, EjmlNode newNode)
		{
			if (this.nodeA == oldNode)
			{
				this.nodeA = newNode;
			} else
			{
				if (this.nodeB != oldNode)
				{
					throw new IllegalArgumentException("Node " + oldNode + " isn't in " + this + ".");
				}

				this.nodeB = newNode;
			}
		}

		Direction getDirFrom(EjmlNode node)
		{
			if (this.nodeA == node)
			{
				return this.dirFromA;
			}
			else
			{
				return this.nodeB == node ? this.dirFromB : null;
			}
		}

		void updateCurrent()
		{
			assert !Double.isNaN(this.nodeA.getTop().voltage);
			assert !Double.isNaN(this.nodeB.getTop().voltage);
			double currentAB = (this.nodeA.getTop().voltage - this.nodeB.getTop().voltage) / this.loss;
			this.nodeA.addCurrent(-currentAB);
			this.nodeB.addCurrent(currentAB);
		}

		@Override
		public String toString()
		{
			return "EjmlLink:" + this.nodeA + "@" + this.dirFromA + "->" + this.nodeB + "@" + this.dirFromB;
		}
	}

	static class GridData
	{
		final Grid grid;
		final Map<Node, EjmlNode> nodeMap = new IdentityHashMap<>();
		final List<EjmlNode> allNodes = new ArrayList<>();
		final Set<Integer> activeSources = new HashSet<>();
		final Set<Integer> activeSinks = new HashSet<>();
		final EjmlStructureCache cache = new EjmlStructureCache();
		EjmlStructureCache.Data lastData;
		final Map<Node, NodeStats> lastStats = new IdentityHashMap<>();
		boolean hasNonZeroVoltages;

		GridData(Grid grid)
		{
			this.grid = grid;
			buildGraph();
		}

		void invalidate()
		{
			this.nodeMap.clear();
			this.allNodes.clear();
			this.activeSources.clear();
			this.activeSinks.clear();
			this.cache.clear();
			this.lastStats.clear();
			this.hasNonZeroVoltages = false;
			buildGraph();
		}

		private void buildGraph()
		{
			for (Node enetNode : this.grid.getNodes())
			{
				EjmlNode node = new EjmlNode(enetNode);
				this.nodeMap.put(enetNode, node);
				this.allNodes.add(node);
			}

			for (EjmlNode node : this.allNodes)
			{
				for (NodeLink enetLink : node.enetNode.getLinks())
				{
					Node enetNeighbor = enetLink.getNeighbor(node.enetNode);
					EjmlNode neighbor = this.nodeMap.get(enetNeighbor);
					if (neighbor != null)
					{
						boolean alreadyConnected = false;
						for (EjmlLink existing : node.links)
						{
							if (existing.getNeighbor(node) == neighbor)
							{
								alreadyConnected = true;
								break;
							}
						}

						if (!alreadyConnected)
						{
							double loss = (node.getInnerLoss() + neighbor.getInnerLoss()) / 2.0;
							Direction dirFromA = enetLink.getDirFrom(node.enetNode);
							Direction dirFromB = dirFromA != null ? dirFromA.getOpposite() : null;
							EjmlLink link = new EjmlLink(node, neighbor, loss, dirFromA, dirFromB);
							node.links.add(link);
							neighbor.links.add(link);
						}
					}
				}
			}
		}

		void prepareCalculation()
		{
			if (!this.activeSources.isEmpty()) this.activeSources.clear();
			if (!this.activeSinks.isEmpty()) this.activeSinks.clear();

			List<EjmlNode> dynamicTierNodes = new ArrayList<>();
			int maxSourceTier = 0;

			for (EjmlNode node : this.allNodes)
			{
				switch (node.nodeType)
				{
					case Source:
						IEnergySource source = (IEnergySource) node.enetNode.getTile().getMainTile();
						node.amount = source.getOfferedEnergy();
						if (node.amount > 0.0)
						{
							this.activeSources.add(node.enetNode.uid);
							maxSourceTier = Math.max(node.tier, maxSourceTier);
						} else
						{
							node.amount = 0.0;
						}
						break;
					case Sink:
						IEnergySink sink = (IEnergySink) node.enetNode.getTile().getMainTile();
						node.amount = sink.getDemandedEnergy();
						if (node.amount > 0.0)
						{
							this.activeSinks.add(node.enetNode.uid);
							if (node.tier == Integer.MAX_VALUE)
							{
								dynamicTierNodes.add(node);
							}
						} else
						{
							node.amount = 0.0;
						}
						break;
					case Conductor:
						node.amount = 0.0;
						break;
				}
			}

			for (EjmlNode node : dynamicTierNodes)
			{
				node.getTop().tier = maxSourceTier;
			}
		}

		void calculate()
		{
			if (!this.activeSources.isEmpty() && !this.activeSinks.isEmpty())
			{
				// Draw energy from sources
				for (int nodeId : this.activeSources)
				{
					EjmlNode node = findNode(nodeId);
					if (node == null) continue;
					int shareCount = 1;

					for (Node sharedEnetNode : node.enetNode.getTile().getNodes())
					{
						if (sharedEnetNode.uid != nodeId && sharedEnetNode.getType() == NodeType.Source)
						{
							Grid otherGrid = sharedEnetNode.getGrid();
							if (otherGrid != null)
							{
								GridData otherData = otherGrid.getData();
								if (otherData != null && !otherData.activeSinks.isEmpty())
								{
									shareCount++;
								}
							}
						}
					}

					node.amount /= shareCount;
					IEnergySource source = (IEnergySource) node.enetNode.getTile().getMainTile();
					source.drawEnergy(node.amount);
				}

				EjmlStructureCache.Data data = calculateDistribution();
				calculateEffects(data);
				this.hasNonZeroVoltages = true;
			} else
			{
				for (EjmlNode node : this.allNodes)
				{
					node.voltage = 0.0;
					node.resetCurrents();
				}

				this.hasNonZeroVoltages = false;
			}

			this.activeSources.clear();
			this.activeSinks.clear();

			// Build stats
			this.lastStats.clear();
			for (EjmlNode node : this.allNodes)
			{
				if (node.nodeType == NodeType.Source || node.nodeType == NodeType.Sink)
				{
					double in, out, voltage;
					if (useLinearTransferModel)
					{
						in = node.currentIn * node.voltage;
						out = node.currentOut * node.voltage;
					} else
					{
						in = node.currentIn;
						out = node.currentOut;
					}
					voltage = node.voltage;
					this.lastStats.put(node.enetNode, new NodeStats(in, out, voltage));
				}
			}
		}

		private EjmlNode findNode(int uid)
		{
			for (EjmlNode node : this.allNodes)
			{
				if (node.enetNode.uid == uid) return node;
			}

			return null;
		}

		private EjmlStructureCache.Data calculateDistribution()
		{
			long time = System.nanoTime();
			EjmlStructureCache.Data data = this.cache.get(this.activeSources, this.activeSinks);
			this.lastData = data;
			if (!data.isInitialized)
			{
				copyForOptimize(data);
				optimize(data);
				determineEmittingNodes(data);
				int size = data.activeNodes.size();
				data.networkMatrix = new DMatrixRMaj(size, size);
				data.sourceMatrix = new DMatrixRMaj(size, 1);
				data.resultMatrix = new DMatrixRMaj(size, 1);
				data.solver = LinearSolverFactory_DDRM.symmPosDef(size);
				if (!useLinearTransferModel)
				{
					populateNetworkMatrix(data);
					initializeSolver(data);
					if (!data.solver.modifiesA())
					{
						data.networkMatrix = null;
					}
				}

				data.isInitialized = true;
			}

			if (useLinearTransferModel)
			{
				populateNetworkMatrix(data);
				initializeSolver(data);
			}

			populateSourceMatrix(data);
			if (debugGridVerbose)
			{
				dumpMatrix(IC2.log.getPrintStream(LogCategory.EnergyNet, Level.TRACE), true, false, data);
			}

			data.solver.solve(data.sourceMatrix, data.resultMatrix);
			if (debugGridVerbose)
			{
				dumpMatrix(IC2.log.getPrintStream(LogCategory.EnergyNet, Level.TRACE), false, true, data);
			}

			if (debugGrid)
			{
				time = System.nanoTime() - time;
				IC2.log.debug(LogCategory.EnergyNet, "Grid %d: The distribution calculation took %d us.", this.grid.hashCode(), time / 1000L);
			}

			return data;
		}

		private void calculateEffects(EjmlStructureCache.Data data)
		{
			for (EjmlNode node : this.allNodes)
			{
				node.voltage = Double.NaN;
				node.resetCurrents();
			}

			for (int row = 0; row < data.activeNodes.size(); row++)
			{
				EjmlNode node = data.activeNodes.get(row);
				node.voltage = data.resultMatrix.get(row);
				switch (node.nodeType)
				{
					case Source:
					{
						double current;
						if (useLinearTransferModel)
						{
							current = data.sourceMatrix.get(row) - node.voltage / node.resistance;
							double actualAmount = current * node.voltage;
							node.amount = actualAmount - node.amount;
						} else
						{
							node.amount = 0.0;
						}
						break;
					}
					case Sink:
					{
						double current;
						if (useLinearTransferModel)
						{
							current = node.voltage / node.resistance;
							node.amount = node.voltage * current;
						} else
						{
							current = node.voltage;
							node.amount = current;
						}
						if (node.amount > 0.0)
						{
							IEnergySink sink = (IEnergySink) node.enetNode.getTile().getMainTile();
							double demand = sink.getDemandedEnergy();
							if (demand > 0.0)
							{
								sink.injectEnergy(null, Math.min(node.amount, demand), node.voltage);
							}
						}
						break;
					}
					case Conductor:
						break;
				}
			}

			Set<EjmlLink> visitedLinks = verifyGrid() ? new HashSet<>() : null;

			for (EjmlNode node : data.activeNodes)
			{
				for (EjmlLink link : node.links)
				{
					if (link.nodeA == node)
					{
						EjmlNode nodeA = link.nodeA.getTop();
						EjmlNode nodeB = link.nodeB.getTop();
						double totalLoss = link.loss;

						for (EjmlNode skipped : link.skippedNodes)
						{
							assert skipped.nodeType == NodeType.Conductor;
							skipped = skipped.getTop();
							EjmlLink link2 = nodeA.getConnectionTo(skipped);
							assert link2 != null;
							assert !verifyGrid() || visitedLinks.add(link2);

							skipped.voltage = Util.lerp(nodeA.voltage, nodeB.voltage, link2.loss / totalLoss);
							link2.updateCurrent();
							nodeA = skipped;
							totalLoss -= link2.loss;
						}

						nodeA.getConnectionTo(nodeB).updateCurrent();
					}
				}
			}
		}

		private void copyForOptimize(EjmlStructureCache.Data data)
		{
			data.optimizedNodes = new HashMap<>();

			for (EjmlNode node : this.allNodes)
			{
				if (node.amount > 0.0 || node.nodeType == NodeType.Conductor)
				{
					data.optimizedNodes.put(node.enetNode.uid, new EjmlNode(node));
				}
			}

			for (EjmlNode node : data.optimizedNodes.values())
			{
				ListIterator<EjmlLink> it = node.links.listIterator();

				while (it.hasNext())
				{
					EjmlLink link = it.next();
					EjmlNode neighbor = link.getNeighbor(node.enetNode.uid);
					if ((neighbor.nodeType == NodeType.Sink || neighbor.nodeType == NodeType.Source) && neighbor.amount <= 0.0)
					{
						it.remove();
					} else if (link.nodeA.enetNode.uid == node.enetNode.uid)
					{
						link.nodeA = data.optimizedNodes.get(link.nodeA.enetNode.uid);
						link.nodeB = data.optimizedNodes.get(link.nodeB.enetNode.uid);
						List<EjmlNode> newSkippedNodes = new ArrayList<>();

						for (EjmlNode skippedNode : link.skippedNodes)
						{
							newSkippedNodes.add(data.optimizedNodes.get(skippedNode.enetNode.uid));
						}

						link.skippedNodes = newSkippedNodes;
					} else
					{
						assert link.nodeB.enetNode.uid == node.enetNode.uid;
						boolean foundReverseLink = false;
						for (EjmlLink reverseLink : data.optimizedNodes.get(link.nodeA.enetNode.uid).links)
						{
							if (reverseLink.nodeB.enetNode.uid == node.enetNode.uid && !node.links.contains(reverseLink))
							{
								foundReverseLink = true;
								it.set(reverseLink);
								break;
							}
						}

						assert foundReverseLink || node.links.contains(link);
					}
				}
			}
		}

		private void optimize(EjmlStructureCache.Data data)
		{
			int removed;
			do
			{
				removed = 0;
				Iterator<EjmlNode> it = data.optimizedNodes.values().iterator();

				while (it.hasNext())
				{
					EjmlNode node = it.next();
					if (node.nodeType == NodeType.Conductor)
					{
						if (node.links.size() < 2)
						{
							it.remove();
							removed++;

							for (EjmlLink link : node.links)
							{
								boolean found = false;
								Iterator<EjmlLink> it2 = link.getNeighbor(node).links.iterator();

								while (it2.hasNext())
								{
									if (it2.next() == link)
									{
										it2.remove();
										found = true;
										break;
									}
								}

								assert found;
							}
						} else if (node.links.size() == 2)
						{
							it.remove();
							removed++;
							EjmlLink linkA = node.links.get(0);
							EjmlLink linkB = node.links.get(1);
							EjmlNode neighborA = linkA.getNeighbor(node);
							EjmlNode neighborB = linkB.getNeighbor(node);
							if (neighborA != neighborB)
							{
								linkA.loss = linkA.loss + linkB.loss;
								if (linkA.nodeA == node)
								{
									linkA.nodeA = neighborB;
									linkA.dirFromA = linkB.getDirFrom(neighborB);
									if (linkB.nodeA == node)
									{
										assert linkB.nodeB == neighborB;
										Collections.reverse(linkB.skippedNodes);
									} else
									{
										assert linkB.nodeB == node && linkB.nodeA == neighborB;
									}

									linkB.skippedNodes.add(node);
									linkB.skippedNodes.addAll(linkA.skippedNodes);
									linkA.skippedNodes = linkB.skippedNodes;
								} else
								{
									linkA.nodeB = neighborB;
									linkA.dirFromB = linkB.getDirFrom(neighborB);
									if (linkB.nodeB == node)
									{
										assert linkB.nodeA == neighborB;
										Collections.reverse(linkB.skippedNodes);
									} else
									{
										assert linkB.nodeA == node && linkB.nodeB == neighborB;
									}

									linkA.skippedNodes.add(node);
									linkA.skippedNodes.addAll(linkB.skippedNodes);
								}

								assert linkA.nodeA != linkA.nodeB;
								boolean found = false;
								ListIterator<EjmlLink> it2 = neighborB.links.listIterator();

								while (it2.hasNext())
								{
									if (it2.next() == linkB)
									{
										found = true;
										it2.set(linkA);
										break;
									}
								}

								assert found;
							} else
							{
								neighborA.links.remove(linkA);
								neighborB.links.remove(linkB);
							}
						}
					}
				}
			} while (removed > 0);
		}

		private static void determineEmittingNodes(EjmlStructureCache.Data data)
		{
			data.activeNodes = new ArrayList<>();
			data.activeNodes.addAll(data.optimizedNodes.values());
		}

		private static void populateNetworkMatrix(EjmlStructureCache.Data data)
		{
			for (int row = 0; row < data.activeNodes.size(); row++)
			{
				EjmlNode node = data.activeNodes.get(row);

				for (int col = 0; col < data.activeNodes.size(); col++)
				{
					double value = 0.0;
					if (row != col)
					{
						EjmlNode possibleNeighbor = data.activeNodes.get(col);

						for (EjmlLink link : node.links)
						{
							EjmlNode neighbor = link.getNeighbor(node);
							if (neighbor != node && neighbor == possibleNeighbor)
							{
								value -= 1.0 / link.loss;
							}
						}
					} else
					{
						for (EjmlLink link : node.links)
						{
							if (link.getNeighbor(node) != node)
							{
								value += 1.0 / link.loss;
							}
						}

						if (useLinearTransferModel)
						{
							if (node.nodeType == NodeType.Source)
							{
								double openCircuitVoltage = EnergyNet.instance.getPowerFromTier(node.tier);
								double resistance = Util.square(openCircuitVoltage) / (node.amount * 4.0);
								assert resistance > 0.0;
								value += 1.0 / resistance;
								node.resistance = resistance;
							} else if (node.nodeType == NodeType.Sink)
							{
								double resistance = EnergyNet.instance.getPowerFromTier(node.tier);
								assert resistance > 0.0;
								value += 1.0 / resistance;
								node.resistance = resistance;
							}
						} else if (node.nodeType == NodeType.Sink)
						{
							value++;
						}
					}

					data.networkMatrix.set(row, col, value);
				}
			}
		}

		private void populateSourceMatrix(EjmlStructureCache.Data data)
		{
			for (int row = 0; row < data.activeNodes.size(); row++)
			{
				EjmlNode node = data.activeNodes.get(row);
				double input = 0.0;
				if (node.nodeType == NodeType.Source)
				{
					if (useLinearTransferModel)
					{
						double openCircuitVoltage = EnergyNet.instance.getPowerFromTier(node.tier);
						input = openCircuitVoltage / node.resistance;
					} else
					{
						input = node.amount;
					}

					assert input > 0.0;
				}

				data.sourceMatrix.set(row, 0, input);
			}
		}

		private static void initializeSolver(EjmlStructureCache.Data data)
		{
			if (!data.solver.setA(data.networkMatrix))
			{
				int size = data.networkMatrix.numCols;
				if (data.solver.modifiesA())
				{
					populateNetworkMatrix(data);
				}

				data.solver = LinearSolverFactory_DDRM.linear(size);
				if (!data.solver.setA(data.networkMatrix))
				{
					if (data.solver.modifiesA())
					{
						populateNetworkMatrix(data);
					}

					EigenDecomposition_F64<DMatrixRMaj> ed = DecompositionFactory_DDRM.eig(size, false);
					if (ed.decompose(data.networkMatrix))
					{
						int complex = size;
						int nonPositive = size;
						StringBuilder sb = new StringBuilder("Eigen values: ");

						for (int i = 0; i < size; i++)
						{
							Complex_F64 ev = ed.getEigenvalue(i);
							if (ev.isReal()) complex--;
							if (ev.real > 0.0) nonPositive--;
							if (i != 0) sb.append(", ");
							sb.append(ev);
						}

						IC2.log.info(LogCategory.EnergyNet, sb.toString());
						IC2.log.info(LogCategory.EnergyNet, "Total: %d, complex: %d, non positive: %d", size, complex, nonPositive);
					} else
					{
						IC2.log.info(LogCategory.EnergyNet, "Unable to compute the eigen values.");
					}

					if (ed.inputModified())
					{
						populateNetworkMatrix(data);
					}

					throw new RuntimeException("Can't decompose network matrix.");
				}
			}
		}

		// Debug helpers
		void dumpNodeInfo(PrintStream ps, EjmlNode node)
		{
			ps.println("Node " + node + " info:");
			ps.println(" type: " + node.nodeType);
			ps.println(" voltage: " + node.voltage + " V");
			ps.println(" current in: " + node.currentIn + " A, out: " + node.currentOut + " A");
			ps.println(node.links.size() + " neighbor links:");

			for (EjmlLink link : node.links)
			{
				ps.println(" " + link.getNeighbor(node) + " " + link.loss + " " + link.skippedNodes);
			}
		}

		void dumpMatrix(PrintStream ps, boolean dumpNodesNetSrcMatrices, boolean dumpResultMatrix, EjmlStructureCache.Data data)
		{
			if (data == null)
			{
				ps.println("Matrices unavailable");
			} else if (dumpNodesNetSrcMatrices || dumpResultMatrix)
			{
				if (dumpNodesNetSrcMatrices)
				{
					ps.println("Emitting node indices:");
					for (int i = 0; i < data.activeNodes.size(); i++)
					{
						EjmlNode node = data.activeNodes.get(i);
						ps.println(i + " " + node + " (amount=" + node.amount + ", tier=" + node.tier + ")");
					}

					ps.println("Network matrix:");
					printMatrix(data.networkMatrix, ps);
					ps.println("Source matrix:");
					printMatrix(data.sourceMatrix, ps);
				}

				if (dumpResultMatrix)
				{
					ps.println("Result matrix:");
					printMatrix(data.resultMatrix, ps);
				}
			}
		}

		void dumpStats(PrintStream ps)
		{
			ps.println("Grid " + this.grid.hashCode() + " info:");
			ps.println(this.allNodes.size() + " nodes");
			ps.printf("%d entries in cache, hit rate %.2f%%", this.cache.size(), 100.0 * this.cache.hits / Math.max(1, this.cache.hits + this.cache.misses));
			ps.println();
		}

		void dumpGraph(boolean waitForFinish)
		{
			EjmlStructureCache.Data data = this.lastData;

			for (int i = 0; i < 2 && (i != 1 || data != null && data.isInitialized && data.optimizedNodes != null); i++)
			{

				try (FileWriter out = new FileWriter("ejml_graph_" + this.grid.hashCode() + "_" + (i == 0 ? "raw" : "optimized") + ".txt"))
				{
					try
					{
						out.write("graph nodes {\n  overlap=false;\n");
						Collection<EjmlNode> nodesToDump = (i == 0 ? this.allNodes : data.optimizedNodes.values());
						Set<EjmlNode> dumpedConnections = new HashSet<>();

						for (EjmlNode node : nodesToDump)
						{
							out.write("  \"" + node + "\";\n");

							for (EjmlLink link : node.links)
							{
								EjmlNode neighbor = link.getNeighbor(node);
								if (!dumpedConnections.contains(neighbor))
								{
									out.write("  \"" + node + "\" -- \"" + neighbor + "\" [label=\"" + link.loss + "\"];\n");
								}
							}

							dumpedConnections.add(node);
						}

						out.write("}\n");
					} catch (IOException e)
					{
						IC2.log.debug(LogCategory.EnergyNet, e, "Graph saving failed.");
					}
				} catch (IOException ignored)
				{
				}
			}
		}

		private static void printMatrix(DMatrixRMaj matrix, PrintStream ps)
		{
			if (matrix == null)
			{
				ps.println("null");
			} else
			{
				boolean isZero = true;
				for (int i = 0; i < matrix.numRows; i++)
				{
					for (int j = 0; j < matrix.numCols; j++)
					{
						if (matrix.get(i, j) != 0.0)
						{
							isZero = false;
							break;
						}
					}
				}

				if (isZero)
				{
					ps.println(matrix.numRows + "x" + matrix.numCols + ", all zero");
				} else
				{
					MatrixIO.print(ps, matrix, "%.6f");
				}
			}
		}
	}

	static class EjmlStructureCache
	{
		private static final int MAX_SIZE = 32;
		final Map<Key, Data> entries = new HashMap<>();
		int hits;
		int misses;

		Data get(Set<Integer> activeSources, Set<Integer> activeSinks)
		{
			Key key = new Key(activeSources, activeSinks);
			Data ret = this.entries.get(key);
			if (ret == null)
			{
				ret = new Data();
				this.add(key, ret);
				this.misses++;
			} else
			{
				this.hits++;
			}

			ret.queries++;
			return ret;
		}

		void clear()
		{
			this.entries.clear();
		}

		int size()
		{
			return this.entries.size();
		}

		private void add(Key key, Data data)
		{
			if (this.entries.size() >= MAX_SIZE)
			{
				int min = Integer.MAX_VALUE;
				Key minKey = null;
				for (Map.Entry<Key, Data> entry : this.entries.entrySet())
				{
					if (entry.getValue().queries < min)
					{
						min = entry.getValue().queries;
						minKey = entry.getKey();
					}
				}

				this.entries.remove(minKey);
			}

			this.entries.put(new Key(key), data);
		}

		static class Data
		{
			boolean isInitialized;
			Map<Integer, EjmlNode> optimizedNodes;
			List<EjmlNode> activeNodes;
			DMatrixRMaj networkMatrix;
			DMatrixRMaj sourceMatrix;
			DMatrixRMaj resultMatrix;
			LinearSolverDense<DMatrixRMaj> solver;
			int queries;
		}

		static class Key
		{
			final Set<Integer> activeSources;
			final Set<Integer> activeSinks;
			final int hashCode;

			Key(Set<Integer> activeSources, Set<Integer> activeSinks)
			{
				this.activeSources = new HashSet<>(activeSources);
				this.activeSinks = new HashSet<>(activeSinks);
				this.hashCode = this.activeSources.hashCode() * 31 + this.activeSinks.hashCode();
			}

			Key(Key key)
			{
				this.activeSources = new HashSet<>(key.activeSources);
				this.activeSinks = new HashSet<>(key.activeSinks);
				this.hashCode = key.hashCode;
			}

			@Override
			public int hashCode()
			{
				return this.hashCode;
			}

			@Override
			public boolean equals(Object o)
			{
				if (!(o instanceof Key key)) return false;
				return key.activeSources.equals(this.activeSources) && key.activeSinks.equals(this.activeSinks);
			}
		}
	}
}
