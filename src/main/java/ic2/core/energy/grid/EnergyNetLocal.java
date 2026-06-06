package ic2.core.energy.grid;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.IC2;
import ic2.core.ref.BlockName;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyNetLocal
{
	static final GridChange QUEUE_DELAY_CHANGE = new GridChange(null, null, null);
	private final World world;
	private final Queue<GridChange> gridChangesQueue = new ArrayDeque<>();
	private final Map<IEnergyTile, GridChange> gridAdditionsMap = new IdentityHashMap<>();
	private final Set<BlockPos> positionsToNotify = new HashSet<>();
	private final GridUpdater updater = new GridUpdater(this);
	int nextNodeId;
	int nextGridId;
	final Map<IEnergyTile, Tile> registeredIoTiles = new IdentityHashMap<>();
	final Map<BlockPos, Tile> registeredTiles = new HashMap<>();
	final Set<Tile> sources = Collections.newSetFromMap(new IdentityHashMap<>());
	private final List<Grid> grids = new ArrayList<>();

	public static EnergyNetLocal create(World world)
	{
		return new EnergyNetLocal(world);
	}

	private EnergyNetLocal(World world)
	{
		this.world = world;

		for (int i = 0; i < 1; i++)
		{
			this.gridChangesQueue.add(QUEUE_DELAY_CHANGE);
		}
	}

	IEnergyTile getIoTile(BlockPos pos)
	{
		Tile tile = this.getTile(pos);
		if (tile != null)
		{
			return tile.getMainTile();
		}

		IEnergyTile ret = null;

		for (GridChange change : this.gridChangesQueue)
		{
			if (change != QUEUE_DELAY_CHANGE && change.pos.equals(pos))
			{
				ret = change.type == GridChange.Type.REMOVAL ? null : change.ioTile;
			}
		}

		return ret;
	}

	IEnergyTile getSubTile(BlockPos pos)
	{
		Tile tile = this.getTile(pos);
		if (tile != null)
		{
			return tile.getSubTileAt(pos);
		}

		IEnergyTile ret = null;

		for (GridChange change : this.gridChangesQueue)
		{
			if (change != QUEUE_DELAY_CHANGE)
			{
				for (IEnergyTile subtile : change.subTiles != null ? change.subTiles : Collections.singletonList(change.ioTile))
				{
					if (EnergyNet.instance.getPos(subtile).equals(pos))
					{
						ret = change.type == GridChange.Type.REMOVAL ? null : change.ioTile;
						break;
					}
				}
			}
		}

		return ret;
	}

	public Tile getTile(BlockPos pos)
	{
		if (this.updater.isInChangeStep())
		{
			this.updater.awaitCompletion();
		}

		return this.registeredTiles.get(pos);
	}

	void addTile(IEnergyTile ioTile, BlockPos pos)
	{
		GridChange change = new GridChange(GridChange.Type.ADDITION, pos, ioTile);
		GridChange prev;
		if ((prev = this.gridAdditionsMap.put(ioTile, change)) != null)
		{
			this.gridAdditionsMap.put(ioTile, prev);
			if (EnergyNetSettings.logGridUpdateIssues)
			{
				IC2.log.warn(LogCategory.EnergyNet, "Tile %s was attempted to be queued twice for addition.", Util.toString(ioTile, this.getWorld(), pos));
			}
		} else
		{
			this.gridChangesQueue.add(change);
		}
	}

	void removeTile(IEnergyTile ioTile, BlockPos pos)
	{
		GridChange addition = this.gridAdditionsMap.remove(ioTile);
		if (addition != null)
		{
			if (EnergyNetSettings.logGridUpdatesVerbose)
			{
				IC2.log.debug(LogCategory.EnergyNet, "Removing tile %s by cancelling a pending addition.", Util.toString(ioTile, this.getWorld(), pos));
			}

			this.gridChangesQueue.remove(addition);
		} else
		{
			this.gridChangesQueue.add(new GridChange(GridChange.Type.REMOVAL, pos, ioTile));
			Tile tile = this.registeredIoTiles.get(ioTile);
			if (tile != null)
			{
				tile.setDisabled();
				if (EnergyNetSettings.logGridUpdatesVerbose)
				{
					IC2.log.debug(LogCategory.EnergyNet, "Disabled tile %s.", Util.toString(ioTile, this.getWorld(), pos));
				}
			} else if (EnergyNetSettings.logGridUpdatesVerbose)
			{
				IC2.log.warn(LogCategory.EnergyNet, "Missing tile %s.", Util.toString(ioTile, this.getWorld(), pos));
			}
		}
	}

	public Collection<Tile> getSources()
	{
		return this.sources;
	}

	NodeStats getNodeStats(IEnergyTile ioTile)
	{
		this.updater.awaitCompletion();
		Tile tile = this.registeredIoTiles.get(ioTile);
		return tile == null ? null : EnergyNetGlobal.getCalculator().getNodeStats(tile);
	}

	public Collection<GridInfo> getGridInfos()
	{
		if (this.updater.isInChangeStep())
		{
			this.updater.awaitCompletion();
		}

		List<GridInfo> ret = new ArrayList<>();

		for (Grid grid : this.grids)
		{
			ret.add(grid.getInfo());
		}

		return ret;
	}

	boolean dumpDebugInfo(BlockPos pos, PrintStream console, PrintStream chat)
	{
		this.updater.awaitCompletion();
		Tile tile = this.registeredTiles.get(pos);
		if (tile == null)
		{
			return false;
		}

		chat.println("Tile " + tile + " info:");
		chat.println(" disabled: " + tile.isDisabled());
		chat.println(" main: " + tile.getMainTile());
		chat.println(" sub: " + tile.subTiles);
		chat.println(" nodes: " + tile.nodes.size());
		Set<Grid> processedGrids = new HashSet<>();

		for (Node node : tile.nodes)
		{
			Grid grid = node.getGrid();
			if (processedGrids.add(grid))
			{
				grid.dumpNodeInfo(node, " ", console, chat);
				grid.dumpInfo(" ", console, chat);
				grid.dumpGraph();
			}
		}

		return true;
	}

	void onTickStart()
	{
		if (this.updater.isInChangeStep())
		{
			this.updater.awaitCompletion();
			if (!this.positionsToNotify.isEmpty())
			{
				Block block = BlockName.te.getInstance();

				for (BlockPos pos : this.positionsToNotify)
				{
					if (this.world.isBlockLoaded(pos))
					{
						this.world.getBlockState(pos).neighborChanged(this.world, pos, block, pos);
					}
				}

				this.positionsToNotify.clear();
			}

			this.updater.startTransferCalc();
		}
	}

	void onTickEnd()
	{
		this.updater.awaitCompletion();
		if (!this.gridChangesQueue.isEmpty() && this.gridChangesQueue.peek() != QUEUE_DELAY_CHANGE)
		{
			this.updater.startChangeCalc(this.gridChangesQueue, this.gridAdditionsMap);
		} else
		{
			this.gridChangesQueue.poll();
			this.updater.startTransferCalc();
		}

		this.gridChangesQueue.add(QUEUE_DELAY_CHANGE);
		assert this.gridChangesQueue.size() >= 1;
	}

	public World getWorld()
	{
		return this.world;
	}

	int allocateNodeId()
	{
		return this.nextNodeId++;
	}

	int allocateGridId()
	{
		return this.nextGridId++;
	}

	void addPositionToNotify(BlockPos pos)
	{
		this.positionsToNotify.add(pos);

		for (EnumFacing facing : EnumFacing.VALUES)
		{
			this.positionsToNotify.add(pos.offset(facing));
		}
	}

	boolean hasGrid(Grid grid)
	{
		return this.grids.contains(grid);
	}

	boolean hasGrids()
	{
		return !this.grids.isEmpty();
	}

	Collection<Grid> getGrids()
	{
		return this.grids;
	}

	void addGrid(Grid grid)
	{
		assert !this.hasGrid(grid);
		this.grids.add(grid);
	}

	void removeGrid(Grid grid)
	{
		boolean removed = this.grids.remove(grid);
		assert removed;
	}

	void shuffleGrids()
	{
		Collections.shuffle(this.grids);
	}
}
