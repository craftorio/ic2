// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.TickHandler;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergySink;
import java.util.Collections;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.core.energy.grid.NodeType;
import ic2.core.energy.grid.GridInfo;
import net.minecraft.util.math.Vec3i;
import java.util.Collection;
import java.util.Arrays;
import ic2.api.energy.tile.IMetaDelegate;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import java.util.ListIterator;
import java.util.Iterator;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.Util;
import ic2.api.energy.EnergyNet;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.io.PrintStream;
import ic2.core.energy.grid.Node;
import ic2.api.energy.NodeStats;
import java.util.WeakHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.util.math.BlockPos;
import java.util.Map;
import java.util.List;
import java.util.Set;
import net.minecraft.world.World;
import ic2.core.energy.grid.IEnergyCalculator;

public final class EnergyNetLocal implements IEnergyCalculator
{
    public static final boolean useLinearTransferModel;
    public static final double nonConductorResistance = 0.2;
    public static final double sourceResistanceFactor = 0.0625;
    public static final double sinkResistanceFactor = 1.0;
    public static final double sourceCurrent = 17.0;
    public static final boolean enableCache = true;
    private static int nextGridUid;
    private static int nextNodeUid;
    private final World world;
    protected final Set<Grid> grids;
    protected List<Change> changes;
    private final Map<BlockPos, Tile> registeredTiles;
    private final Map<IEnergyTile, Integer> pendingAdds;
    private final Set<Tile> removedTiles;
    private boolean locked;
    private static final long logSuppressionTimeout = 300000000000L;
    private final Map<String, Long> recentLogs;
    
    public EnergyNetLocal() {
        this.grids = new HashSet<Grid>();
        this.changes = new ArrayList<Change>();
        this.registeredTiles = new HashMap<BlockPos, Tile>();
        this.pendingAdds = new WeakHashMap<IEnergyTile, Integer>();
        this.removedTiles = new HashSet<Tile>();
        this.locked = false;
        this.recentLogs = new HashMap<String, Long>();
        this.world = null;
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void handleGridChange(final ic2.core.energy.grid.Grid grid) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean runSyncStep(final ic2.core.energy.grid.EnergyNetLocal enet) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean runSyncStep(final ic2.core.energy.grid.Grid grid) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void runAsyncStep(final ic2.core.energy.grid.Grid grid) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public NodeStats getNodeStats(final ic2.core.energy.grid.Tile tile) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void dumpNodeInfo(final Node node, final String prefix, final PrintStream console, final PrintStream chat) {
        throw new UnsupportedOperationException();
    }
    
    protected void addTile(final IEnergyTile mainTile) {
        this.addTile(mainTile, 0);
    }
    
    protected void addTile(final IEnergyTile mainTile, final int retry) {
        if (EnergyNetGlobal.debugTileManagement) {
            IC2.log.debug(LogCategory.EnergyNet, "EnergyNet.addTile(%s, %d), world=%s, chunk=%s, this=%s", mainTile, retry, EnergyNet.instance.getWorld(mainTile), EnergyNet.instance.getWorld(mainTile).getChunkFromBlockCoords(EnergyNet.instance.getPos(mainTile)), this);
        }
        if (EnergyNetGlobal.checkApi && !Util.checkInterfaces(mainTile.getClass())) {
            IC2.log.warn(LogCategory.EnergyNet, "EnergyNet.addTile: %s doesn't implement its advertised interfaces completely.", mainTile);
        }
        if (mainTile instanceof TileEntity && ((TileEntity)mainTile).isInvalid()) {
            this.logWarn("EnergyNet.addTile: " + mainTile + " is invalid (TileEntity.isInvalid()), aborting");
            return;
        }
        if (this.world != DimensionManager.getWorld(this.world.provider.getDimension())) {
            this.logDebug("EnergyNet.addTile: " + mainTile + " is in an unloaded world, aborting");
            return;
        }
        if (this.locked) {
            this.logDebug("EnergyNet.addTileEntity: adding " + mainTile + " while locked, postponing.");
            this.pendingAdds.put(mainTile, retry);
            return;
        }
        final Tile tile = new Tile(this, mainTile);
        if (EnergyNetGlobal.debugTileManagement) {
            final List<String> posStrings = new ArrayList<String>(tile.subTiles.size());
            for (final IEnergyTile subTile : tile.subTiles) {
                posStrings.add(String.format("%s (%s)", subTile, EnergyNet.instance.getPos(subTile)));
            }
            IC2.log.debug(LogCategory.EnergyNet, "positions: %s", posStrings);
        }
        final ListIterator<IEnergyTile> it = tile.subTiles.listIterator();
        while (it.hasNext()) {
            IEnergyTile subTile2 = it.next();
            final BlockPos pos = EnergyNet.instance.getPos(subTile2).toImmutable();
            Tile conflicting = this.registeredTiles.get(pos);
            boolean abort = false;
            if (conflicting != null) {
                if (mainTile == conflicting.mainTile) {
                    this.logDebug("EnergyNet.addTileEntity: " + subTile2 + " (" + mainTile + ") is already added using the same position, aborting");
                }
                else if (retry < 2) {
                    this.pendingAdds.put(mainTile, retry + 1);
                }
                else if ((conflicting.mainTile instanceof TileEntity && ((TileEntity)mainTile).isInvalid()) || EnergyNetGlobal.replaceConflicting) {
                    this.logDebug("EnergyNet.addTileEntity: " + subTile2 + " (" + mainTile + ") is conflicting with " + conflicting.mainTile + " (invalid=" + (conflicting.mainTile instanceof TileEntity && ((TileEntity)conflicting.mainTile).isInvalid()) + ") using the same position, which is abandoned (prev. te not removed), replacing");
                    this.removeTile(conflicting.mainTile);
                    conflicting = null;
                }
                else {
                    this.logWarn("EnergyNet.addTileEntity: " + subTile2 + " (" + mainTile + ") is still conflicting with " + conflicting.mainTile + " using the same position (overlapping), aborting");
                }
                if (conflicting != null) {
                    abort = true;
                }
            }
            if (!abort && !this.world.isBlockLoaded(pos)) {
                if (retry < 1) {
                    this.logWarn("EnergyNet.addTileEntity: " + subTile2 + " (" + mainTile + ") was added too early, postponing");
                    this.pendingAdds.put(mainTile, retry + 1);
                }
                else {
                    this.logWarn("EnergyNet.addTileEntity: " + subTile2 + " (" + mainTile + ") unloaded, aborting");
                }
                abort = true;
            }
            if (abort) {
                it.previous();
                while (it.hasPrevious()) {
                    subTile2 = it.previous();
                    this.registeredTiles.remove(EnergyNet.instance.getPos(subTile2));
                }
                return;
            }
            this.registeredTiles.put(pos, tile);
            this.notifyLoadedNeighbors(pos, tile.subTiles);
        }
        this.addTileToGrids(tile);
        if (EnergyNetGlobal.verifyGrid()) {
            for (final ic2.core.energy.Node node : tile.nodes) {
                assert node.getGrid() != null;
            }
        }
    }
    
    private void notifyLoadedNeighbors(final BlockPos pos, final List<IEnergyTile> excluded) {
        final Set<BlockPos> excludedPositions = new HashSet<BlockPos>(excluded.size());
        for (final IEnergyTile subTile : excluded) {
            excludedPositions.add(EnergyNet.instance.getPos(subTile).toImmutable());
        }
        final Block block = this.world.getBlockState(pos).getBlock();
        final int ocx = pos.getX() >> 4;
        final int ocz = pos.getZ() >> 4;
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final BlockPos cPos = pos.offset(dir);
            if (!excludedPositions.contains(cPos)) {
                final int ccx = cPos.getX() >> 4;
                final int ccz = cPos.getZ() >> 4;
                if (dir.getAxis().isVertical() || (ccx == ocx && ccz == ocz) || this.world.isBlockLoaded(cPos)) {
                    this.world.getBlockState(cPos).neighborChanged(this.world, cPos, block, pos);
                }
            }
        }
    }
    
    protected void removeTile(final IEnergyTile mainTile) {
        if (this.locked) {
            throw new IllegalStateException("removeTile isn't allowed from this context");
        }
        if (EnergyNetGlobal.debugTileManagement) {
            IC2.log.debug(LogCategory.EnergyNet, "EnergyNet.removeTile(%s), world=%s, chunk=%s, this=%s", mainTile, EnergyNet.instance.getWorld(mainTile), EnergyNet.instance.getWorld(mainTile).getChunkFromBlockCoords(EnergyNet.instance.getPos(mainTile)), this);
        }
        List<IEnergyTile> subTiles;
        if (mainTile instanceof IMetaDelegate) {
            subTiles = ((IMetaDelegate)mainTile).getSubTiles();
        }
        else {
            subTiles = Arrays.asList(mainTile);
        }
        final boolean wasPending = this.pendingAdds.remove(mainTile) != null;
        if (EnergyNetGlobal.debugTileManagement) {
            final List<String> posStrings = new ArrayList<String>(subTiles.size());
            for (final IEnergyTile subTile : subTiles) {
                posStrings.add(String.format("%s (%s)", subTile, EnergyNet.instance.getPos(subTile)));
            }
            IC2.log.debug(LogCategory.EnergyNet, "positions: %s", posStrings);
        }
        boolean removed = false;
        for (final IEnergyTile subTile : subTiles) {
            final BlockPos pos = EnergyNet.instance.getPos(subTile);
            final Tile tile = this.registeredTiles.get(pos);
            if (tile == null) {
                if (wasPending) {
                    continue;
                }
                this.logDebug("EnergyNet.removeTileEntity: " + subTile + " (" + mainTile + ") wasn't found (added), skipping");
            }
            else if (tile.mainTile != mainTile) {
                this.logWarn("EnergyNet.removeTileEntity: " + subTile + " (" + mainTile + ") doesn't match the registered tile " + tile.mainTile + ", skipping");
            }
            else {
                if (!removed) {
                    assert new HashSet(subTiles).equals(new HashSet(tile.subTiles));
                    this.removeTileFromGrids(tile);
                    removed = true;
                    this.removedTiles.add(tile);
                }
                this.registeredTiles.remove(pos);
                if (!this.world.isBlockLoaded(pos)) {
                    continue;
                }
                this.notifyLoadedNeighbors(pos, tile.subTiles);
            }
        }
    }
    
    protected double getTotalEnergyEmitted(final TileEntity tileEntity) {
        final BlockPos coords = new BlockPos((Vec3i)tileEntity.getPos());
        final Tile tile = this.registeredTiles.get(coords);
        if (tile == null) {
            this.logWarn("EnergyNet.getTotalEnergyEmitted: " + tileEntity + " is not added to the enet, aborting");
            return 0.0;
        }
        double ret = 0.0;
        final Iterable<NodeStats> stats = tile.getStats();
        for (final NodeStats stat : stats) {
            ret += stat.getEnergyOut();
        }
        return ret;
    }
    
    protected double getTotalEnergySunken(final TileEntity tileEntity) {
        final BlockPos coords = new BlockPos((Vec3i)tileEntity.getPos());
        final Tile tile = this.registeredTiles.get(coords);
        if (tile == null) {
            this.logWarn("EnergyNet.getTotalEnergySunken: " + tileEntity + " is not added to the enet, aborting");
            return 0.0;
        }
        double ret = 0.0;
        final Iterable<NodeStats> stats = tile.getStats();
        for (final NodeStats stat : stats) {
            ret += stat.getEnergyIn();
        }
        return ret;
    }
    
    protected NodeStats getNodeStats(final IEnergyTile energyTile) {
        final BlockPos coords = EnergyNet.instance.getPos(energyTile);
        final Tile tile = this.registeredTiles.get(coords);
        if (tile == null) {
            this.logWarn("EnergyNet.getTotalEnergySunken: " + energyTile + " is not added to the enet");
            return new NodeStats(0.0, 0.0, 0.0);
        }
        double in = 0.0;
        double out = 0.0;
        double voltage = 0.0;
        final Iterable<NodeStats> stats = tile.getStats();
        for (final NodeStats stat : stats) {
            in += stat.getEnergyIn();
            out += stat.getEnergyOut();
            voltage = Math.max(voltage, stat.getVoltage());
        }
        return new NodeStats(in, out, voltage);
    }
    
    protected Tile getTile(final BlockPos pos) {
        return this.registeredTiles.get(pos);
    }
    
    public boolean dumpDebugInfo(final PrintStream console, final PrintStream chat, final BlockPos pos) {
        final Tile tile = this.registeredTiles.get(pos);
        if (tile == null) {
            return false;
        }
        chat.println("Tile " + tile + " info:");
        chat.println(" main: " + tile.mainTile);
        chat.println(" sub: " + tile.subTiles);
        chat.println(" nodes: " + tile.nodes.size());
        final Set<Grid> processedGrids = new HashSet<Grid>();
        for (final ic2.core.energy.Node node : tile.nodes) {
            final Grid grid = node.getGrid();
            if (processedGrids.add(grid)) {
                grid.dumpNodeInfo(chat, true, node);
                grid.dumpStats(chat, true);
                grid.dumpMatrix(console, true, true, true);
                console.println("dumping graph for " + grid);
                grid.dumpGraph(true);
            }
        }
        return true;
    }
    
    public List<GridInfo> getGridInfos() {
        final List<GridInfo> ret = new ArrayList<GridInfo>();
        for (final Grid grid : this.grids) {
            ret.add(grid.getInfo());
        }
        return ret;
    }
    
    protected void onTickEnd() {
        if (!IC2.platform.isSimulating()) {
            return;
        }
        this.locked = true;
        for (final Grid grid : this.grids) {
            grid.finishCalculation();
            grid.updateStats();
        }
        this.locked = false;
        this.processChanges();
        if (!this.pendingAdds.isEmpty()) {
            final List<Map.Entry<IEnergyTile, Integer>> pending = new ArrayList<Map.Entry<IEnergyTile, Integer>>(this.pendingAdds.entrySet());
            this.pendingAdds.clear();
            for (final Map.Entry<IEnergyTile, Integer> entry : pending) {
                this.addTile(entry.getKey(), entry.getValue());
            }
        }
        this.locked = true;
        for (final Grid grid : this.grids) {
            grid.prepareCalculation();
        }
        final List<Runnable> tasks = new ArrayList<Runnable>();
        for (final Grid grid2 : this.grids) {
            final Runnable task = grid2.startCalculation();
            if (task != null) {
                tasks.add(task);
            }
        }
        IC2.getInstance().threadPool.executeAll(tasks);
        this.locked = false;
    }
    
    protected void addChange(final ic2.core.energy.Node node, final EnumFacing dir, final double amount, final double voltage) {
        this.changes.add(new Change(node, dir, amount, voltage));
    }
    
    protected static int getNextGridUid() {
        return EnergyNetLocal.nextGridUid++;
    }
    
    protected static int getNextNodeUid() {
        return EnergyNetLocal.nextNodeUid++;
    }
    
    private void addTileToGrids(final Tile tile) {
        final List<ic2.core.energy.Node> extraNodes = new ArrayList<ic2.core.energy.Node>();
        for (final ic2.core.energy.Node node : tile.nodes) {
            if (EnergyNetGlobal.debugGrid) {
                IC2.log.debug(LogCategory.EnergyNet, "Adding node %s.", node);
            }
            final List<ic2.core.energy.Node> neighbors = new ArrayList<ic2.core.energy.Node>();
            for (final IEnergyTile subTile : tile.subTiles) {
                for (final EnumFacing dir : EnumFacing.VALUES) {
                    final BlockPos coords = EnergyNet.instance.getPos(subTile).offset(dir);
                    final Tile neighborTile = this.registeredTiles.get(coords);
                    if (neighborTile != null) {
                        if (neighborTile != node.tile) {
                            for (final ic2.core.energy.Node neighbor : neighborTile.nodes) {
                                if (neighbor.isExtraNode()) {
                                    continue;
                                }
                                boolean canEmit = false;
                                if ((node.nodeType == NodeType.Source || node.nodeType == NodeType.Conductor) && neighbor.nodeType != NodeType.Source) {
                                    final IEnergyEmitter emitter = (IEnergyEmitter)((subTile instanceof IEnergyEmitter) ? subTile : node.tile.mainTile);
                                    final IEnergyTile neighborSubTe = neighborTile.getSubTileAt(coords);
                                    final IEnergyAcceptor acceptor = (IEnergyAcceptor)((neighborSubTe instanceof IEnergyAcceptor) ? neighborSubTe : neighbor.tile.mainTile);
                                    canEmit = (emitter.emitsEnergyTo((IEnergyAcceptor)neighbor.tile.mainTile, dir) && acceptor.acceptsEnergyFrom((IEnergyEmitter)node.tile.mainTile, dir.getOpposite()));
                                }
                                boolean canAccept = false;
                                if (!canEmit && (node.nodeType == NodeType.Sink || node.nodeType == NodeType.Conductor) && neighbor.nodeType != NodeType.Sink) {
                                    final IEnergyAcceptor acceptor2 = (IEnergyAcceptor)((subTile instanceof IEnergyAcceptor) ? subTile : node.tile.mainTile);
                                    final IEnergyTile neighborSubTe2 = neighborTile.getSubTileAt(coords);
                                    final IEnergyEmitter emitter2 = (IEnergyEmitter)((neighborSubTe2 instanceof IEnergyEmitter) ? neighborSubTe2 : neighbor.tile.mainTile);
                                    canAccept = (acceptor2.acceptsEnergyFrom((IEnergyEmitter)neighbor.tile.mainTile, dir) && emitter2.emitsEnergyTo((IEnergyAcceptor)node.tile.mainTile, dir.getOpposite()));
                                }
                                if (!canEmit && !canAccept) {
                                    continue;
                                }
                                neighbors.add(neighbor);
                            }
                        }
                    }
                }
            }
            if (neighbors.isEmpty()) {
                if (EnergyNetGlobal.debugGrid) {
                    IC2.log.debug(LogCategory.EnergyNet, "Creating new grid for %s.", node);
                }
                final Grid grid = new Grid(this);
                grid.add(node, neighbors);
            }
            else {
                switch (node.nodeType) {
                    case Conductor: {
                        Grid grid = null;
                        for (final ic2.core.energy.Node neighbor2 : neighbors) {
                            if (neighbor2.nodeType == NodeType.Conductor || neighbor2.links.isEmpty()) {
                                if (EnergyNetGlobal.debugGrid) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Using %s for %s with neighbors %s.", neighbor2.getGrid(), node, neighbors);
                                }
                                grid = neighbor2.getGrid();
                                break;
                            }
                        }
                        if (grid == null) {
                            if (EnergyNetGlobal.debugGrid) {
                                IC2.log.debug(LogCategory.EnergyNet, "Creating new grid for %s with neighbors %s.", node, neighbors);
                            }
                            grid = new Grid(this);
                        }
                        final Map<ic2.core.energy.Node, ic2.core.energy.Node> neighborReplacements = new HashMap<ic2.core.energy.Node, ic2.core.energy.Node>();
                        ListIterator<ic2.core.energy.Node> it = neighbors.listIterator();
                        while (it.hasNext()) {
                            ic2.core.energy.Node neighbor3 = it.next();
                            if (neighbor3.getGrid() == grid) {
                                continue;
                            }
                            if (neighbor3.nodeType != NodeType.Conductor && !neighbor3.links.isEmpty()) {
                                boolean found = false;
                                for (int i = 0; i < it.previousIndex(); ++i) {
                                    final ic2.core.energy.Node neighbor4 = neighbors.get(i);
                                    if (neighbor4.tile == neighbor3.tile && neighbor4.nodeType == neighbor3.nodeType && neighbor4.getGrid() == grid) {
                                        if (EnergyNetGlobal.debugGrid) {
                                            IC2.log.debug(LogCategory.EnergyNet, "Using neighbor node %s instead of %s.", neighbor4, neighbors);
                                        }
                                        found = true;
                                        it.set(neighbor4);
                                        break;
                                    }
                                }
                                if (found) {
                                    continue;
                                }
                                if (EnergyNetGlobal.debugGrid) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for neighbor %s.", neighbor3);
                                }
                                neighbor3 = new ic2.core.energy.Node(this, neighbor3.tile, neighbor3.nodeType);
                                neighbor3.tile.addExtraNode(neighbor3);
                                grid.add(neighbor3, (Collection<ic2.core.energy.Node>)Collections.emptyList());
                                it.set(neighbor3);
                                assert neighbor3.getGrid() != null;
                                continue;
                            }
                            else {
                                grid.merge(neighbor3.getGrid(), neighborReplacements);
                            }
                        }
                        it = neighbors.listIterator();
                        while (it.hasNext()) {
                            ic2.core.energy.Node neighbor3 = it.next();
                            final ic2.core.energy.Node replacement = neighborReplacements.get(neighbor3);
                            if (replacement != null) {
                                neighbor3 = replacement;
                                it.set(replacement);
                            }
                            assert neighbor3.getGrid() == grid;
                        }
                        grid.add(node, neighbors);
                        assert node.getGrid() != null;
                        continue;
                    }
                    case Sink:
                    case Source: {
                        final List<List<ic2.core.energy.Node>> neighborGroups = new ArrayList<List<ic2.core.energy.Node>>();
                        for (final ic2.core.energy.Node neighbor2 : neighbors) {
                            boolean found2 = false;
                            if (node.nodeType == NodeType.Conductor) {
                                for (final List<ic2.core.energy.Node> nodeList : neighborGroups) {
                                    final ic2.core.energy.Node neighbor4 = nodeList.get(0);
                                    if (neighbor4.nodeType == NodeType.Conductor && neighbor4.getGrid() == neighbor2.getGrid()) {
                                        nodeList.add(neighbor2);
                                        found2 = true;
                                        break;
                                    }
                                }
                            }
                            if (!found2) {
                                final List<ic2.core.energy.Node> nodeList2 = new ArrayList<ic2.core.energy.Node>();
                                nodeList2.add(neighbor2);
                                neighborGroups.add(nodeList2);
                            }
                        }
                        if (EnergyNetGlobal.debugGrid) {
                            IC2.log.debug(LogCategory.EnergyNet, "Neighbor groups detected for %s: %s.", node, neighborGroups);
                        }
                        assert !neighborGroups.isEmpty();
                        for (int j = 0; j < neighborGroups.size(); ++j) {
                            final List<ic2.core.energy.Node> nodeList3 = neighborGroups.get(j);
                            ic2.core.energy.Node neighbor3 = nodeList3.get(0);
                            if (neighbor3.nodeType != NodeType.Conductor && !neighbor3.links.isEmpty()) {
                                assert nodeList3.size() == 1;
                                if (EnergyNetGlobal.debugGrid) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for neighbor %s.", neighbor3);
                                }
                                neighbor3 = new ic2.core.energy.Node(this, neighbor3.tile, neighbor3.nodeType);
                                neighbor3.tile.addExtraNode(neighbor3);
                                new Grid(this).add(neighbor3, (Collection<ic2.core.energy.Node>)Collections.emptyList());
                                nodeList3.set(0, neighbor3);
                                assert neighbor3.getGrid() != null;
                            }
                            ic2.core.energy.Node currentNode;
                            if (j == 0) {
                                currentNode = node;
                            }
                            else {
                                if (EnergyNetGlobal.debugGrid) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for %s.", node);
                                }
                                currentNode = new ic2.core.energy.Node(this, tile, node.nodeType);
                                currentNode.setExtraNode(true);
                                extraNodes.add(currentNode);
                            }
                            neighbor3.getGrid().add(currentNode, nodeList3);
                            assert currentNode.getGrid() != null;
                        }
                        continue;
                    }
                }
            }
        }
        for (final ic2.core.energy.Node node : extraNodes) {
            tile.addExtraNode(node);
        }
    }
    
    private void removeTileFromGrids(final Tile tile) {
        for (final ic2.core.energy.Node node : tile.nodes) {
            node.getGrid().remove(node);
        }
    }
    
    private void processChanges() {
        for (final Tile tile : this.removedTiles) {
            final Iterator<Change> it = this.changes.iterator();
            while (it.hasNext()) {
                final Change change = it.next();
                if (change.node.tile != tile) {
                    continue;
                }
                final Tile replacement = this.registeredTiles.get(EnergyNet.instance.getPos(change.node.tile.mainTile));
                boolean validReplacement = false;
                if (replacement != null) {
                    for (final ic2.core.energy.Node node : replacement.nodes) {
                        if (node.nodeType == change.node.nodeType && node.getGrid() == change.node.getGrid()) {
                            if (EnergyNetGlobal.debugGrid) {
                                IC2.log.debug(LogCategory.EnergyNet, "Redirecting change %s to replacement node %s.", change, node);
                            }
                            change.node = node;
                            validReplacement = true;
                            break;
                        }
                    }
                }
                if (validReplacement) {
                    continue;
                }
                it.remove();
                final List<Change> sameGridSourceChanges = new ArrayList<Change>();
                for (final Change change2 : this.changes) {
                    if (change2.node.nodeType == NodeType.Source && change.node.getGrid() == change2.node.getGrid()) {
                        sameGridSourceChanges.add(change2);
                    }
                }
                if (EnergyNetGlobal.debugGrid) {
                    IC2.log.debug(LogCategory.EnergyNet, "Redistributing change %s to remaining source nodes %s.", change, sameGridSourceChanges);
                }
                for (final Change change2 : sameGridSourceChanges) {
                    change2.setAmount(change2.getAmount() - Math.abs(change.getAmount()) / sameGridSourceChanges.size());
                }
            }
        }
        this.removedTiles.clear();
        for (final Change change3 : this.changes) {
            if (change3.node.nodeType == NodeType.Sink) {
                assert change3.getAmount() > 0.0;
                final IEnergySink sink = (IEnergySink)change3.node.tile.mainTile;
                final double returned = sink.injectEnergy(change3.dir, change3.getAmount(), change3.getVoltage());
                if (EnergyNetGlobal.debugGrid) {
                    IC2.log.debug(LogCategory.EnergyNet, "Applied change %s, %f EU returned.", change3, returned);
                }
                if (returned <= 0.0) {
                    continue;
                }
                final List<Change> sameGridSourceChanges2 = new ArrayList<Change>();
                for (final Change change4 : this.changes) {
                    if (change4.node.nodeType == NodeType.Source && change3.node.getGrid() == change4.node.getGrid()) {
                        sameGridSourceChanges2.add(change4);
                    }
                }
                if (EnergyNetGlobal.debugGrid) {
                    IC2.log.debug(LogCategory.EnergyNet, "Redistributing returned amount to source nodes %s.", sameGridSourceChanges2);
                }
                for (final Change change4 : sameGridSourceChanges2) {
                    change4.setAmount(change4.getAmount() - returned / sameGridSourceChanges2.size());
                }
            }
        }
        for (final Change change3 : this.changes) {
            if (change3.node.nodeType == NodeType.Source) {
                assert change3.getAmount() <= 0.0;
                if (change3.getAmount() >= 0.0) {
                    continue;
                }
                final IEnergySource source = (IEnergySource)change3.node.tile.mainTile;
                source.drawEnergy(change3.getAmount());
                if (!EnergyNetGlobal.debugGrid) {
                    continue;
                }
                IC2.log.debug(LogCategory.EnergyNet, "Applied change %s.", change3);
            }
        }
        this.changes.clear();
    }
    
    private void logDebug(final String msg) {
        if (!this.shouldLog(msg)) {
            return;
        }
        IC2.log.debug(LogCategory.EnergyNet, msg);
        if (EnergyNetGlobal.debugTileManagement) {
            IC2.log.debug(LogCategory.EnergyNet, new Throwable(), "stack trace");
            if (TickHandler.getLastDebugTrace() != null) {
                IC2.log.debug(LogCategory.EnergyNet, TickHandler.getLastDebugTrace(), "parent stack trace");
            }
        }
    }
    
    private void logWarn(final String msg) {
        if (!this.shouldLog(msg)) {
            return;
        }
        IC2.log.warn(LogCategory.EnergyNet, msg);
        if (EnergyNetGlobal.debugTileManagement) {
            IC2.log.debug(LogCategory.EnergyNet, new Throwable(), "stack trace");
            if (TickHandler.getLastDebugTrace() != null) {
                IC2.log.debug(LogCategory.EnergyNet, TickHandler.getLastDebugTrace(), "parent stack trace");
            }
        }
    }
    
    private boolean shouldLog(String msg) {
        if (EnergyNetGlobal.logAll) {
            return true;
        }
        this.cleanRecentLogs();
        msg = msg.replaceAll("@[0-9a-f]+", "@x");
        final long time = System.nanoTime();
        final Long lastLog = this.recentLogs.put(msg, time);
        return lastLog == null || lastLog < time - 300000000000L;
    }
    
    private void cleanRecentLogs() {
        if (this.recentLogs.size() < 100) {
            return;
        }
        final long minTime = System.nanoTime() - 300000000000L;
        final Iterator<Long> it = this.recentLogs.values().iterator();
        while (it.hasNext()) {
            final long recTime = it.next();
            if (recTime < minTime) {
                it.remove();
            }
        }
    }
    
    static {
        useLinearTransferModel = ConfigUtil.getBool(MainConfig.get(), "misc/useLinearTransferModel");
        EnergyNetLocal.nextGridUid = 0;
        EnergyNetLocal.nextNodeUid = 0;
    }
}
