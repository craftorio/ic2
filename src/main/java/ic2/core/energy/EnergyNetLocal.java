package ic2.core.energy;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.core.IC2;
import ic2.core.TickHandler;
import ic2.core.energy.grid.GridInfo;
import ic2.core.energy.grid.IEnergyCalculator;
import ic2.core.energy.grid.NodeType;
import ic2.core.init.MainConfig;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public final class EnergyNetLocal implements IEnergyCalculator
{
	public static final boolean useLinearTransferModel = ConfigUtil.getBool(MainConfig.get(), "misc/useLinearTransferModel");
	public static final double nonConductorResistance = 0.2;
	public static final double sourceResistanceFactor = 0.0625;
	public static final double sinkResistanceFactor = 1.0;
	public static final double sourceCurrent = 17.0;
	public static final boolean enableCache = true;
	private static int nextGridUid = 0;
	private static int nextNodeUid = 0;
	private final World world;
	protected final Set<Grid> grids = new HashSet<>();
	protected final List<Change> changes = new ArrayList<>();
	private final Map<BlockPos, Tile> registeredTiles = new HashMap<>();
	private final Map<IEnergyTile, Integer> pendingAdds = new WeakHashMap<>();
	private final Set<Tile> removedTiles = new HashSet<>();
	private boolean locked = false;
	private static final long logSuppressionTimeout = 300000000000L;
	private final Map<String, Long> recentLogs = new HashMap<>();

	public EnergyNetLocal()
	{
		this.world = null;
		throw new UnsupportedOperationException();
	}

	@Override
	public void handleGridChange(ic2.core.energy.grid.Grid grid)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean runSyncStep(ic2.core.energy.grid.EnergyNetLocal enet)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean runSyncStep(ic2.core.energy.grid.Grid grid)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void runAsyncStep(ic2.core.energy.grid.Grid grid)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NodeStats getNodeStats(ic2.core.energy.grid.Tile tile)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void dumpNodeInfo(ic2.core.energy.grid.Node node, String prefix, PrintStream console, PrintStream chat)
	{
		throw new UnsupportedOperationException();
	}

	protected void addTile(IEnergyTile mainTile)
	{
		this.addTile(mainTile, 0);
	}

	protected void addTile(IEnergyTile mainTile, int retry)
	{
		if (EnergyNetGlobal.debugTileManagement)
		{
			IC2.log
				.debug(
					LogCategory.EnergyNet,
					"EnergyNet.addTile(%s, %d), world=%s, chunk=%s, this=%s",
					mainTile,
					retry,
					EnergyNet.instance.getWorld(mainTile),
					EnergyNet.instance.getWorld(mainTile).getChunkFromBlockCoords(EnergyNet.instance.getPos(mainTile)),
					this
				);
		}

		if (EnergyNetGlobal.checkApi && !Util.checkInterfaces(mainTile.getClass()))
		{
			IC2.log.warn(LogCategory.EnergyNet, "EnergyNet.addTile: %s doesn't implement its advertised interfaces completely.", mainTile);
		}

		if (mainTile instanceof TileEntity && ((TileEntity) mainTile).isInvalid())
		{
			this.logWarn("EnergyNet.addTile: " + mainTile + " is invalid (TileEntity.isInvalid()), aborting");
		} else if (this.world != DimensionManager.getWorld(this.world.provider.getDimension()))
		{
			this.logDebug("EnergyNet.addTile: " + mainTile + " is in an unloaded world, aborting");
		} else if (this.locked)
		{
			this.logDebug("EnergyNet.addTileEntity: adding " + mainTile + " while locked, postponing.");
			this.pendingAdds.put(mainTile, retry);
		} else
		{
			Tile tile = new Tile(this, mainTile);
			if (EnergyNetGlobal.debugTileManagement)
			{
				List<String> posStrings = new ArrayList<>(tile.subTiles.size());

				for (IEnergyTile subTile : tile.subTiles)
				{
					posStrings.add(String.format("%s (%s)", subTile, EnergyNet.instance.getPos(subTile)));
				}

				IC2.log.debug(LogCategory.EnergyNet, "positions: %s", posStrings);
			}

			ListIterator<IEnergyTile> it = tile.subTiles.listIterator();

			while (it.hasNext())
			{
				IEnergyTile subTile = it.next();
				BlockPos pos = EnergyNet.instance.getPos(subTile).toImmutable();
				Tile conflicting = this.registeredTiles.get(pos);
				boolean abort = false;
				if (conflicting != null)
				{
					if (mainTile == conflicting.mainTile)
					{
						this.logDebug("EnergyNet.addTileEntity: " + subTile + " (" + mainTile + ") is already added using the same position, aborting");
					} else if (retry < 2)
					{
						this.pendingAdds.put(mainTile, retry + 1);
					} else if ((!(conflicting.mainTile instanceof TileEntity) || !((TileEntity) mainTile).isInvalid()) && !EnergyNetGlobal.replaceConflicting)
					{
						this.logWarn(
							"EnergyNet.addTileEntity: "
								+ subTile
								+ " ("
								+ mainTile
								+ ") is still conflicting with "
								+ conflicting.mainTile
								+ " using the same position (overlapping), aborting"
						);
					} else
					{
						this.logDebug(
							"EnergyNet.addTileEntity: "
								+ subTile
								+ " ("
								+ mainTile
								+ ") is conflicting with "
								+ conflicting.mainTile
								+ " (invalid="
								+ (conflicting.mainTile instanceof TileEntity && ((TileEntity) conflicting.mainTile).isInvalid())
								+ ") using the same position, which is abandoned (prev. te not removed), replacing"
						);
						this.removeTile(conflicting.mainTile);
						conflicting = null;
					}

					if (conflicting != null)
					{
						abort = true;
					}
				}

				if (!abort && !this.world.isBlockLoaded(pos))
				{
					if (retry < 1)
					{
						this.logWarn("EnergyNet.addTileEntity: " + subTile + " (" + mainTile + ") was added too early, postponing");
						this.pendingAdds.put(mainTile, retry + 1);
					} else
					{
						this.logWarn("EnergyNet.addTileEntity: " + subTile + " (" + mainTile + ") unloaded, aborting");
					}

					abort = true;
				}

				if (abort)
				{
					it.previous();

					while (it.hasPrevious())
					{
						subTile = it.previous();
						this.registeredTiles.remove(EnergyNet.instance.getPos(subTile));
					}

					return;
				}

				this.registeredTiles.put(pos, tile);
				this.notifyLoadedNeighbors(pos, tile.subTiles);
			}

			this.addTileToGrids(tile);
			if (EnergyNetGlobal.verifyGrid())
			{
				for (Node node : tile.nodes)
				{
					assert node.getGrid() != null;
				}
			}
		}
	}

	private void notifyLoadedNeighbors(BlockPos pos, List<IEnergyTile> excluded)
	{
		Set<BlockPos> excludedPositions = new HashSet<>(excluded.size());

		for (IEnergyTile subTile : excluded)
		{
			excludedPositions.add(EnergyNet.instance.getPos(subTile).toImmutable());
		}

		Block block = this.world.getBlockState(pos).getBlock();
		int ocx = pos.getX() >> 4;
		int ocz = pos.getZ() >> 4;

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			BlockPos cPos = pos.offset(dir);
			if (!excludedPositions.contains(cPos))
			{
				int ccx = cPos.getX() >> 4;
				int ccz = cPos.getZ() >> 4;
				if (dir.getAxis().isVertical() || ccx == ocx && ccz == ocz || this.world.isBlockLoaded(cPos))
				{
					this.world.getBlockState(cPos).neighborChanged(this.world, cPos, block, pos);
				}
			}
		}
	}

	protected void removeTile(IEnergyTile mainTile)
	{
		if (this.locked)
		{
			throw new IllegalStateException("removeTile isn't allowed from this context");
		}

		if (EnergyNetGlobal.debugTileManagement)
		{
			IC2.log
				.debug(
					LogCategory.EnergyNet,
					"EnergyNet.removeTile(%s), world=%s, chunk=%s, this=%s",
					mainTile,
					EnergyNet.instance.getWorld(mainTile),
					EnergyNet.instance.getWorld(mainTile).getChunkFromBlockCoords(EnergyNet.instance.getPos(mainTile)),
					this
				);
		}

		List<IEnergyTile> subTiles;
		if (mainTile instanceof IMetaDelegate)
		{
			subTiles = ((IMetaDelegate) mainTile).getSubTiles();
		} else
		{
			subTiles = Arrays.asList(mainTile);
		}

		boolean wasPending = this.pendingAdds.remove(mainTile) != null;
		if (EnergyNetGlobal.debugTileManagement)
		{
			List<String> posStrings = new ArrayList<>(subTiles.size());

			for (IEnergyTile subTile : subTiles)
			{
				posStrings.add(String.format("%s (%s)", subTile, EnergyNet.instance.getPos(subTile)));
			}

			IC2.log.debug(LogCategory.EnergyNet, "positions: %s", posStrings);
		}

		boolean removed = false;

		for (IEnergyTile subTile : subTiles)
		{
			BlockPos pos = EnergyNet.instance.getPos(subTile);
			Tile tile = this.registeredTiles.get(pos);
			if (tile == null)
			{
				if (!wasPending)
				{
					this.logDebug("EnergyNet.removeTileEntity: " + subTile + " (" + mainTile + ") wasn't found (added), skipping");
				}
			} else if (tile.mainTile != mainTile)
			{
				this.logWarn("EnergyNet.removeTileEntity: " + subTile + " (" + mainTile + ") doesn't match the registered tile " + tile.mainTile + ", skipping");
			} else
			{
				if (!removed)
				{
					assert new HashSet<>(subTiles).equals(new HashSet<>(tile.subTiles));
					this.removeTileFromGrids(tile);
					removed = true;
					this.removedTiles.add(tile);
				}

				this.registeredTiles.remove(pos);
				if (this.world.isBlockLoaded(pos))
				{
					this.notifyLoadedNeighbors(pos, tile.subTiles);
				}
			}
		}
	}

	protected double getTotalEnergyEmitted(TileEntity tileEntity)
	{
		BlockPos coords = new BlockPos(tileEntity.getPos());
		Tile tile = this.registeredTiles.get(coords);
		if (tile == null)
		{
			this.logWarn("EnergyNet.getTotalEnergyEmitted: " + tileEntity + " is not added to the enet, aborting");
			return 0.0;
		}

		double ret = 0.0;

		for (NodeStats stat : tile.getStats())
		{
			ret += stat.getEnergyOut();
		}

		return ret;
	}

	protected double getTotalEnergySunken(TileEntity tileEntity)
	{
		BlockPos coords = new BlockPos(tileEntity.getPos());
		Tile tile = this.registeredTiles.get(coords);
		if (tile == null)
		{
			this.logWarn("EnergyNet.getTotalEnergySunken: " + tileEntity + " is not added to the enet, aborting");
			return 0.0;
		}

		double ret = 0.0;

		for (NodeStats stat : tile.getStats())
		{
			ret += stat.getEnergyIn();
		}

		return ret;
	}

	protected NodeStats getNodeStats(IEnergyTile energyTile)
	{
		BlockPos coords = EnergyNet.instance.getPos(energyTile);
		Tile tile = this.registeredTiles.get(coords);
		if (tile == null)
		{
			this.logWarn("EnergyNet.getTotalEnergySunken: " + energyTile + " is not added to the enet");
			return new NodeStats(0.0, 0.0, 0.0);
		}

		double in = 0.0;
		double out = 0.0;
		double voltage = 0.0;

		for (NodeStats stat : tile.getStats())
		{
			in += stat.getEnergyIn();
			out += stat.getEnergyOut();
			voltage = Math.max(voltage, stat.getVoltage());
		}

		return new NodeStats(in, out, voltage);
	}

	protected Tile getTile(BlockPos pos)
	{
		return this.registeredTiles.get(pos);
	}

	public boolean dumpDebugInfo(PrintStream console, PrintStream chat, BlockPos pos)
	{
		Tile tile = this.registeredTiles.get(pos);
		if (tile == null)
		{
			return false;
		}

		chat.println("Tile " + tile + " info:");
		chat.println(" main: " + tile.mainTile);
		chat.println(" sub: " + tile.subTiles);
		chat.println(" nodes: " + tile.nodes.size());
		Set<Grid> processedGrids = new HashSet<>();

		for (Node node : tile.nodes)
		{
			Grid grid = node.getGrid();
			if (processedGrids.add(grid))
			{
				grid.dumpNodeInfo(chat, true, node);
				grid.dumpStats(chat, true);
				grid.dumpMatrix(console, true, true, true);
				console.println("dumping graph for " + grid);
				grid.dumpGraph(true);
			}
		}

		return true;
	}

	public List<GridInfo> getGridInfos()
	{
		List<GridInfo> ret = new ArrayList<>();

		for (Grid grid : this.grids)
		{
			ret.add(grid.getInfo());
		}

		return ret;
	}

	protected void onTickEnd()
	{
		if (IC2.platform.isSimulating())
		{
			this.locked = true;

			for (Grid grid : this.grids)
			{
				grid.finishCalculation();
				grid.updateStats();
			}

			this.locked = false;
			this.processChanges();
			if (!this.pendingAdds.isEmpty())
			{
				List<Entry<IEnergyTile, Integer>> pending = new ArrayList<>(this.pendingAdds.entrySet());
				this.pendingAdds.clear();

				for (Entry<IEnergyTile, Integer> entry : pending)
				{
					this.addTile(entry.getKey(), entry.getValue());
				}
			}

			this.locked = true;

			for (Grid grid : this.grids)
			{
				grid.prepareCalculation();
			}

			List<Runnable> tasks = new ArrayList<>();

			for (Grid grid : this.grids)
			{
				Runnable task = grid.startCalculation();
				if (task != null)
				{
					tasks.add(task);
				}
			}

			IC2.getInstance().threadPool.executeAll(tasks);
			this.locked = false;
		}
	}

	protected void addChange(Node node, EnumFacing dir, double amount, double voltage)
	{
		this.changes.add(new Change(node, dir, amount, voltage));
	}

	protected static int getNextGridUid()
	{
		return nextGridUid++;
	}

	protected static int getNextNodeUid()
	{
		return nextNodeUid++;
	}

	private void addTileToGrids(Tile tile)
	{
		List<Node> extraNodes = new ArrayList<>();

		for (Node node : tile.nodes)
		{
			if (EnergyNetGlobal.debugGrid)
			{
				IC2.log.debug(LogCategory.EnergyNet, "Adding node %s.", node);
			}

			List<Node> neighbors = new ArrayList<>();

			for (IEnergyTile subTile : tile.subTiles)
			{
				for (EnumFacing dir : EnumFacing.VALUES)
				{
					BlockPos coords = EnergyNet.instance.getPos(subTile).offset(dir);
					Tile neighborTile = this.registeredTiles.get(coords);
					if (neighborTile != null && neighborTile != node.tile)
					{
						for (Node neighbor : neighborTile.nodes)
						{
							if (!neighbor.isExtraNode())
							{
								boolean canEmit = false;
								if ((node.nodeType == NodeType.Source || node.nodeType == NodeType.Conductor) && neighbor.nodeType != NodeType.Source)
								{
									IEnergyEmitter emitter = (IEnergyEmitter) (subTile instanceof IEnergyEmitter ? subTile : node.tile.mainTile);
									IEnergyTile neighborSubTe = neighborTile.getSubTileAt(coords);
									IEnergyAcceptor acceptor = (IEnergyAcceptor) (neighborSubTe instanceof IEnergyAcceptor ? neighborSubTe : neighbor.tile.mainTile);
									canEmit = emitter.emitsEnergyTo((IEnergyAcceptor) neighbor.tile.mainTile, dir)
										&& acceptor.acceptsEnergyFrom((IEnergyEmitter) node.tile.mainTile, dir.getOpposite());
								}

								boolean canAccept = false;
								if (!canEmit && (node.nodeType == NodeType.Sink || node.nodeType == NodeType.Conductor) && neighbor.nodeType != NodeType.Sink)
								{
									IEnergyAcceptor acceptor = (IEnergyAcceptor) (subTile instanceof IEnergyAcceptor ? subTile : node.tile.mainTile);
									IEnergyTile neighborSubTe = neighborTile.getSubTileAt(coords);
									IEnergyEmitter emitter = (IEnergyEmitter) (neighborSubTe instanceof IEnergyEmitter ? neighborSubTe : neighbor.tile.mainTile);
									canAccept = acceptor.acceptsEnergyFrom((IEnergyEmitter) neighbor.tile.mainTile, dir)
										&& emitter.emitsEnergyTo((IEnergyAcceptor) node.tile.mainTile, dir.getOpposite());
								}

								if (canEmit || canAccept)
								{
									neighbors.add(neighbor);
								}
							}
						}
					}
				}
			}

			if (neighbors.isEmpty())
			{
				if (EnergyNetGlobal.debugGrid)
				{
					IC2.log.debug(LogCategory.EnergyNet, "Creating new grid for %s.", node);
				}

				Grid grid = new Grid(this);
				grid.add(node, neighbors);
			} else
			{
				switch (node.nodeType)
				{
					case Conductor:
						Grid grid = null;

						for (Node neighbor : neighbors)
						{
							if (neighbor.nodeType == NodeType.Conductor || neighbor.links.isEmpty())
							{
								if (EnergyNetGlobal.debugGrid)
								{
									IC2.log.debug(LogCategory.EnergyNet, "Using %s for %s with neighbors %s.", neighbor.getGrid(), node, neighbors);
								}

								grid = neighbor.getGrid();
								break;
							}
						}

						if (grid == null)
						{
							if (EnergyNetGlobal.debugGrid)
							{
								IC2.log.debug(LogCategory.EnergyNet, "Creating new grid for %s with neighbors %s.", node, neighbors);
							}

							grid = new Grid(this);
						}

						Map<Node, Node> neighborReplacements = new HashMap<>();
						ListIterator<Node> it = neighbors.listIterator();

						while (it.hasNext())
						{
							Node neighbor = it.next();
							if (neighbor.getGrid() != grid)
							{
								if (neighbor.nodeType != NodeType.Conductor && !neighbor.links.isEmpty())
								{
									boolean found = false;

									for (int i = 0; i < it.previousIndex(); i++)
									{
										Node neighbor2 = neighbors.get(i);
										if (neighbor2.tile == neighbor.tile && neighbor2.nodeType == neighbor.nodeType && neighbor2.getGrid() == grid)
										{
											if (EnergyNetGlobal.debugGrid)
											{
												IC2.log.debug(LogCategory.EnergyNet, "Using neighbor node %s instead of %s.", neighbor2, neighbors);
											}

											found = true;
											it.set(neighbor2);
											break;
										}
									}

									if (!found)
									{
										if (EnergyNetGlobal.debugGrid)
										{
											IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for neighbor %s.", neighbor);
										}

										neighbor = new Node(this, neighbor.tile, neighbor.nodeType);
										neighbor.tile.addExtraNode(neighbor);
										grid.add(neighbor, Collections.emptyList());
										it.set(neighbor);
										assert neighbor.getGrid() != null;
									}
								} else
								{
									grid.merge(neighbor.getGrid(), neighborReplacements);
								}
							}
						}

						it = neighbors.listIterator();

						while (it.hasNext())
						{
							Node neighbor = it.next();
							Node replacement = neighborReplacements.get(neighbor);
							if (replacement != null)
							{
								neighbor = replacement;
								it.set(replacement);
							}

							assert neighbor.getGrid() == grid;
						}

						grid.add(node, neighbors);
						assert node.getGrid() != null;
						break;
					case Sink:
					case Source:
						List<List<Node>> neighborGroups = new ArrayList<>();

						for (Node neighbor : neighbors)
						{
							boolean found = false;
							if (node.nodeType == NodeType.Conductor)
							{
								for (List<Node> nodeList : neighborGroups)
								{
									Node neighbor2 = nodeList.get(0);
									if (neighbor2.nodeType == NodeType.Conductor && neighbor2.getGrid() == neighbor.getGrid())
									{
										nodeList.add(neighbor);
										found = true;
										break;
									}
								}
							}

							if (!found)
							{
								List<Node> nodeList = new ArrayList<>();
								nodeList.add(neighbor);
								neighborGroups.add(nodeList);
							}
						}

						if (EnergyNetGlobal.debugGrid)
						{
							IC2.log.debug(LogCategory.EnergyNet, "Neighbor groups detected for %s: %s.", node, neighborGroups);
						}

						assert !neighborGroups.isEmpty();

						for (int i = 0; i < neighborGroups.size(); i++)
						{
							List<Node> nodeList = neighborGroups.get(i);
							Node neighbor = nodeList.get(0);
							if (neighbor.nodeType != NodeType.Conductor && !neighbor.links.isEmpty())
							{
								assert nodeList.size() == 1;
								if (EnergyNetGlobal.debugGrid)
								{
									IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for neighbor %s.", neighbor);
								}

								neighbor = new Node(this, neighbor.tile, neighbor.nodeType);
								neighbor.tile.addExtraNode(neighbor);
								new Grid(this).add(neighbor, Collections.emptyList());
								nodeList.set(0, neighbor);
								assert neighbor.getGrid() != null;
							}

							Node currentNode;
							if (i == 0)
							{
								currentNode = node;
							} else
							{
								if (EnergyNetGlobal.debugGrid)
								{
									IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for %s.", node);
								}

								currentNode = new Node(this, tile, node.nodeType);
								currentNode.setExtraNode(true);
								extraNodes.add(currentNode);
							}

							neighbor.getGrid().add(currentNode, nodeList);
							assert currentNode.getGrid() != null;
						}
				}
			}
		}

		for (Node node : extraNodes)
		{
			tile.addExtraNode(node);
		}
	}

	private void removeTileFromGrids(Tile tile)
	{
		for (Node node : tile.nodes)
		{
			node.getGrid().remove(node);
		}
	}

	private void processChanges()
	{
		for (Tile tile : this.removedTiles)
		{
			Iterator<Change> it = this.changes.iterator();

			while (it.hasNext())
			{
				Change change = it.next();
				if (change.node.tile == tile)
				{
					Tile replacement = this.registeredTiles.get(EnergyNet.instance.getPos(change.node.tile.mainTile));
					boolean validReplacement = false;
					if (replacement != null)
					{
						for (Node node : replacement.nodes)
						{
							if (node.nodeType == change.node.nodeType && node.getGrid() == change.node.getGrid())
							{
								if (EnergyNetGlobal.debugGrid)
								{
									IC2.log.debug(LogCategory.EnergyNet, "Redirecting change %s to replacement node %s.", change, node);
								}

								change.node = node;
								validReplacement = true;
								break;
							}
						}
					}

					if (!validReplacement)
					{
						it.remove();
						List<Change> sameGridSourceChanges = new ArrayList<>();

						for (Change change2 : this.changes)
						{
							if (change2.node.nodeType == NodeType.Source && change.node.getGrid() == change2.node.getGrid())
							{
								sameGridSourceChanges.add(change2);
							}
						}

						if (EnergyNetGlobal.debugGrid)
						{
							IC2.log.debug(LogCategory.EnergyNet, "Redistributing change %s to remaining source nodes %s.", change, sameGridSourceChanges);
						}

						for (Change change2 : sameGridSourceChanges)
						{
							change2.setAmount(change2.getAmount() - Math.abs(change.getAmount()) / sameGridSourceChanges.size());
						}
					}
				}
			}
		}

		this.removedTiles.clear();

		for (Change change : this.changes)
		{
			if (change.node.nodeType == NodeType.Sink)
			{
				assert change.getAmount() > 0.0;
				IEnergySink sink = (IEnergySink) change.node.tile.mainTile;
				double returned = sink.injectEnergy(change.dir, change.getAmount(), change.getVoltage());
				if (EnergyNetGlobal.debugGrid)
				{
					IC2.log.debug(LogCategory.EnergyNet, "Applied change %s, %f EU returned.", change, returned);
				}

				if (returned > 0.0)
				{
					List<Change> sameGridSourceChanges = new ArrayList<>();

					for (Change change2 : this.changes)
					{
						if (change2.node.nodeType == NodeType.Source && change.node.getGrid() == change2.node.getGrid())
						{
							sameGridSourceChanges.add(change2);
						}
					}

					if (EnergyNetGlobal.debugGrid)
					{
						IC2.log.debug(LogCategory.EnergyNet, "Redistributing returned amount to source nodes %s.", sameGridSourceChanges);
					}

					for (Change change2 : sameGridSourceChanges)
					{
						change2.setAmount(change2.getAmount() - returned / sameGridSourceChanges.size());
					}
				}
			}
		}

		for (Change change : this.changes)
		{
			if (change.node.nodeType == NodeType.Source)
			{
				assert change.getAmount() <= 0.0;
				if (!(change.getAmount() >= 0.0))
				{
					IEnergySource source = (IEnergySource) change.node.tile.mainTile;
					source.drawEnergy(change.getAmount());
					if (EnergyNetGlobal.debugGrid)
					{
						IC2.log.debug(LogCategory.EnergyNet, "Applied change %s.", change);
					}
				}
			}
		}

		this.changes.clear();
	}

	private void logDebug(String msg)
	{
		if (this.shouldLog(msg))
		{
			IC2.log.debug(LogCategory.EnergyNet, msg);
			if (EnergyNetGlobal.debugTileManagement)
			{
				IC2.log.debug(LogCategory.EnergyNet, new Throwable(), "stack trace");
				if (TickHandler.getLastDebugTrace() != null)
				{
					IC2.log.debug(LogCategory.EnergyNet, TickHandler.getLastDebugTrace(), "parent stack trace");
				}
			}
		}
	}

	private void logWarn(String msg)
	{
		if (this.shouldLog(msg))
		{
			IC2.log.warn(LogCategory.EnergyNet, msg);
			if (EnergyNetGlobal.debugTileManagement)
			{
				IC2.log.debug(LogCategory.EnergyNet, new Throwable(), "stack trace");
				if (TickHandler.getLastDebugTrace() != null)
				{
					IC2.log.debug(LogCategory.EnergyNet, TickHandler.getLastDebugTrace(), "parent stack trace");
				}
			}
		}
	}

	private boolean shouldLog(String msg)
	{
		if (EnergyNetGlobal.logAll)
		{
			return true;
		}

		this.cleanRecentLogs();
		msg = msg.replaceAll("@[0-9a-f]+", "@x");
		long time = System.nanoTime();
		Long lastLog = this.recentLogs.put(msg, time);
		return lastLog == null || lastLog < time - 300000000000L;
	}

	private void cleanRecentLogs()
	{
		if (this.recentLogs.size() >= 100)
		{
			long minTime = System.nanoTime() - 300000000000L;
			Iterator<Long> it = this.recentLogs.values().iterator();

			while (it.hasNext())
			{
				long recTime = it.next();
				if (recTime < minTime)
				{
					it.remove();
				}
			}
		}
	}
}
