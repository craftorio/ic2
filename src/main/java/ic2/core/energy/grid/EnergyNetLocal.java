// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import net.minecraft.util.EnumFacing;
import net.minecraft.block.Block;
import ic2.core.ref.BlockName;
import java.io.PrintStream;
import ic2.api.energy.NodeStats;
import java.util.Collection;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.api.energy.EnergyNet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import java.util.Set;
import ic2.api.energy.tile.IEnergyTile;
import java.util.Map;
import java.util.Queue;
import net.minecraft.world.World;

public class EnergyNetLocal
{
    static final GridChange QUEUE_DELAY_CHANGE;
    private final World world;
    private final Queue<GridChange> gridChangesQueue;
    private final Map<IEnergyTile, GridChange> gridAdditionsMap;
    private final Set<BlockPos> positionsToNotify;
    private final GridUpdater updater;
    int nextNodeId;
    int nextGridId;
    final Map<IEnergyTile, Tile> registeredIoTiles;
    final Map<BlockPos, Tile> registeredTiles;
    final Set<Tile> sources;
    private final List<Grid> grids;
    
    public static EnergyNetLocal create(final World world) {
        return new EnergyNetLocal(world);
    }
    
    private EnergyNetLocal(final World world) {
        this.gridChangesQueue = new ArrayDeque<GridChange>();
        this.gridAdditionsMap = new IdentityHashMap<IEnergyTile, GridChange>();
        this.positionsToNotify = new HashSet<BlockPos>();
        this.updater = new GridUpdater(this);
        this.registeredIoTiles = new IdentityHashMap<IEnergyTile, Tile>();
        this.registeredTiles = new HashMap<BlockPos, Tile>();
        this.sources = Collections.newSetFromMap(new IdentityHashMap<Tile, Boolean>());
        this.grids = new ArrayList<Grid>();
        this.world = world;
        for (int i = 0; i < 1; ++i) {
            this.gridChangesQueue.add(EnergyNetLocal.QUEUE_DELAY_CHANGE);
        }
    }
    
    IEnergyTile getIoTile(final BlockPos pos) {
        final Tile tile = this.getTile(pos);
        if (tile != null) {
            return tile.getMainTile();
        }
        IEnergyTile ret = null;
        for (final GridChange change : this.gridChangesQueue) {
            if (change == EnergyNetLocal.QUEUE_DELAY_CHANGE) {
                continue;
            }
            if (!change.pos.equals((Object)pos)) {
                continue;
            }
            ret = ((change.type == GridChange.Type.REMOVAL) ? null : change.ioTile);
        }
        return ret;
    }
    
    IEnergyTile getSubTile(final BlockPos pos) {
        final Tile tile = this.getTile(pos);
        if (tile != null) {
            return tile.getSubTileAt(pos);
        }
        IEnergyTile ret = null;
        for (final GridChange change : this.gridChangesQueue) {
            if (change == EnergyNetLocal.QUEUE_DELAY_CHANGE) {
                continue;
            }
            final Iterable<IEnergyTile> subTiles = (change.subTiles != null) ? change.subTiles : Collections.singletonList(change.ioTile);
            for (final IEnergyTile subtile : subTiles) {
                if (EnergyNet.instance.getPos(subtile).equals((Object)pos)) {
                    ret = ((change.type == GridChange.Type.REMOVAL) ? null : change.ioTile);
                    break;
                }
            }
        }
        return ret;
    }
    
    public Tile getTile(final BlockPos pos) {
        if (this.updater.isInChangeStep()) {
            this.updater.awaitCompletion();
        }
        return this.registeredTiles.get(pos);
    }
    
    void addTile(final IEnergyTile ioTile, final BlockPos pos) {
        final GridChange change = new GridChange(GridChange.Type.ADDITION, pos, ioTile);
        final GridChange prev;
        if ((prev = this.gridAdditionsMap.put(ioTile, change)) != null) {
            this.gridAdditionsMap.put(ioTile, prev);
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s was attempted to be queued twice for addition.", Util.toString(ioTile, (IBlockAccess)this.getWorld(), pos));
            }
        }
        else {
            this.gridChangesQueue.add(change);
        }
    }
    
    void removeTile(final IEnergyTile ioTile, final BlockPos pos) {
        final GridChange addition = this.gridAdditionsMap.remove(ioTile);
        if (addition != null) {
            if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.debug(LogCategory.EnergyNet, "Removing tile %s by cancelling a pending addition.", Util.toString(ioTile, (IBlockAccess)this.getWorld(), pos));
            }
            this.gridChangesQueue.remove(addition);
        }
        else {
            this.gridChangesQueue.add(new GridChange(GridChange.Type.REMOVAL, pos, ioTile));
            final Tile tile = this.registeredIoTiles.get(ioTile);
            if (tile != null) {
                tile.setDisabled();
                if (EnergyNetSettings.logGridUpdatesVerbose) {
                    IC2.log.debug(LogCategory.EnergyNet, "Disabled tile %s.", Util.toString(ioTile, (IBlockAccess)this.getWorld(), pos));
                }
            }
            else if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.warn(LogCategory.EnergyNet, "Missing tile %s.", Util.toString(ioTile, (IBlockAccess)this.getWorld(), pos));
            }
        }
    }
    
    public Collection<Tile> getSources() {
        return this.sources;
    }
    
    NodeStats getNodeStats(final IEnergyTile ioTile) {
        this.updater.awaitCompletion();
        final Tile tile = this.registeredIoTiles.get(ioTile);
        if (tile == null) {
            return null;
        }
        return EnergyNetGlobal.getCalculator().getNodeStats(tile);
    }
    
    public Collection<GridInfo> getGridInfos() {
        if (this.updater.isInChangeStep()) {
            this.updater.awaitCompletion();
        }
        final List<GridInfo> ret = new ArrayList<GridInfo>();
        for (final Grid grid : this.grids) {
            ret.add(grid.getInfo());
        }
        return ret;
    }
    
    boolean dumpDebugInfo(final BlockPos pos, final PrintStream console, final PrintStream chat) {
        this.updater.awaitCompletion();
        final Tile tile = this.registeredTiles.get(pos);
        if (tile == null) {
            return false;
        }
        chat.println("Tile " + tile + " info:");
        chat.println(" disabled: " + tile.isDisabled());
        chat.println(" main: " + tile.getMainTile());
        chat.println(" sub: " + tile.subTiles);
        chat.println(" nodes: " + tile.nodes.size());
        final Set<Grid> processedGrids = new HashSet<Grid>();
        for (final Node node : tile.nodes) {
            final Grid grid = node.getGrid();
            if (processedGrids.add(grid)) {
                grid.dumpNodeInfo(node, " ", console, chat);
                grid.dumpInfo(" ", console, chat);
                grid.dumpGraph();
            }
        }
        return true;
    }
    
    void onTickStart() {
        if (this.updater.isInChangeStep()) {
            this.updater.awaitCompletion();
            if (!this.positionsToNotify.isEmpty()) {
                final Block block = BlockName.te.getInstance();
                for (final BlockPos pos : this.positionsToNotify) {
                    if (!this.world.isBlockLoaded(pos)) {
                        continue;
                    }
                    this.world.getBlockState(pos).neighborChanged(this.world, pos, block, pos);
                }
                this.positionsToNotify.clear();
            }
            this.updater.startTransferCalc();
        }
    }
    
    void onTickEnd() {
        this.updater.awaitCompletion();
        if (!this.gridChangesQueue.isEmpty() && this.gridChangesQueue.peek() != EnergyNetLocal.QUEUE_DELAY_CHANGE) {
            this.updater.startChangeCalc(this.gridChangesQueue, this.gridAdditionsMap);
        }
        else {
            this.gridChangesQueue.poll();
            this.updater.startTransferCalc();
        }
        this.gridChangesQueue.add(EnergyNetLocal.QUEUE_DELAY_CHANGE);
        assert this.gridChangesQueue.size() >= 1;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    int allocateNodeId() {
        return this.nextNodeId++;
    }
    
    int allocateGridId() {
        return this.nextGridId++;
    }
    
    void addPositionToNotify(final BlockPos pos) {
        this.positionsToNotify.add(pos);
        for (final EnumFacing facing : EnumFacing.VALUES) {
            this.positionsToNotify.add(pos.offset(facing));
        }
    }
    
    boolean hasGrid(final Grid grid) {
        return this.grids.contains(grid);
    }
    
    boolean hasGrids() {
        return !this.grids.isEmpty();
    }
    
    Collection<Grid> getGrids() {
        return this.grids;
    }
    
    void addGrid(final Grid grid) {
        assert !this.hasGrid(grid);
        this.grids.add(grid);
    }
    
    void removeGrid(final Grid grid) {
        final boolean removed = this.grids.remove(grid);
        assert removed;
    }
    
    void shuffleGrids() {
        Collections.shuffle(this.grids);
    }
    
    static {
        QUEUE_DELAY_CHANGE = new GridChange(null, null, null);
    }
}
