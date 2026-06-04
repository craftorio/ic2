// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import java.util.ListIterator;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import net.minecraft.util.EnumFacing;
import java.util.Iterator;
import ic2.api.energy.IEnergyNetEventReceiver;
import ic2.api.energy.tile.IEnergySource;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Arrays;
import java.util.Collection;
import ic2.api.energy.tile.IEnergyTile;
import java.util.ArrayList;
import ic2.api.energy.tile.IMetaDelegate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.api.energy.EnergyNet;

class ChangeHandler
{
    static boolean prepareSync(final EnergyNetLocal enet, final GridChange change) {
        final World world = enet.getWorld();
        final GridChange.Type type = change.type;
        final IEnergyTile ioTile = change.ioTile;
        final BlockPos pos = change.pos;
        if (EnergyNet.instance.getWorld(ioTile) != world) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s had the wrong world in grid update (%s)", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos), type);
            }
            return false;
        }
        if (type != GridChange.Type.REMOVAL && !EnergyNet.instance.getPos(ioTile).equals((Object)pos)) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s has the wrong position in grid update (%s)", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos), type);
            }
            return false;
        }
        if (type != GridChange.Type.REMOVAL && !world.isBlockLoaded(pos)) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s was unloaded in grid update (%s)", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos), type);
            }
            return false;
        }
        if (type != GridChange.Type.REMOVAL && ioTile instanceof TileEntity && ((TileEntity)ioTile).isInvalid()) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s was invalidated in grid update (%s)", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos), type);
            }
            return false;
        }
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "Considering tile %s for grid update (%s)", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos), type);
        }
        if (type == GridChange.Type.ADDITION) {
            if (ioTile instanceof IMetaDelegate) {
                change.subTiles = new ArrayList<IEnergyTile>(((IMetaDelegate)ioTile).getSubTiles());
                if (change.subTiles.isEmpty()) {
                    throw new RuntimeException(String.format("Tile %s must return at least 1 sub tile for IMetaDelegate.getSubTiles().", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos)));
                }
            }
            else {
                change.subTiles = Arrays.asList(ioTile);
            }
        }
        return true;
    }
    
    static void applyAddition(final EnergyNetLocal enet, final IEnergyTile ioTile, final BlockPos pos, final List<IEnergyTile> subTiles, final Collection<GridChange> pendingChanges) {
        if (enet.registeredIoTiles.containsKey(ioTile)) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s is already registered", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos));
            }
            return;
        }
        for (final IEnergyTile subTile : subTiles) {
            final BlockPos subPos = EnergyNet.instance.getPos(subTile);
            final Tile prev;
            if ((prev = enet.registeredTiles.get(subPos)) != null) {
                final IEnergyTile prevIoTile = prev.getMainTile();
                boolean found = false;
                final Iterator<GridChange> it = pendingChanges.iterator();
                while (it.hasNext()) {
                    final GridChange change = it.next();
                    if (change.type == GridChange.Type.REMOVAL && change.ioTile == prevIoTile) {
                        if (EnergyNetSettings.logGridUpdatesVerbose) {
                            IC2.log.debug(LogCategory.EnergyNet, "Expediting pending removal of %s due to addition conflict.", Util.toString(change.ioTile, (IBlockAccess)enet.getWorld(), change.pos));
                        }
                        found = true;
                        it.remove();
                        applyRemoval(enet, change.ioTile, change.pos);
                        assert !enet.registeredTiles.containsKey(subPos);
                        break;
                    }
                    else {
                        if (change.type == GridChange.Type.ADDITION && change.ioTile == prevIoTile) {
                            break;
                        }
                        continue;
                    }
                }
                if (!found) {
                    if (EnergyNetSettings.logGridUpdateIssues) {
                        IC2.log.warn(LogCategory.EnergyNet, "Tile %s, sub tile %s addition is conflicting with a previous registration at the same location: %s.", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos), Util.toString(subTile, (IBlockAccess)enet.getWorld(), subPos), prevIoTile);
                    }
                    return;
                }
                continue;
            }
        }
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "Adding tile %s.", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos));
        }
        final Tile tile = new Tile(enet, ioTile, subTiles);
        enet.registeredIoTiles.put(ioTile, tile);
        if (ioTile instanceof IEnergySource) {
            enet.sources.add(tile);
        }
        for (final IEnergyTile subTile2 : subTiles) {
            final BlockPos subPos2 = EnergyNet.instance.getPos(subTile2);
            enet.registeredTiles.put(subPos2, tile);
            enet.addPositionToNotify(subPos2);
        }
        addTileToGrids(enet, tile);
        for (final IEnergyNetEventReceiver receiver : EnergyNetGlobal.getEventReceivers()) {
            receiver.onAdd(ioTile);
        }
    }
    
    private static void addTileToGrids(final EnergyNetLocal enet, final Tile tile) {
        final List<Node> extraNodes = new ArrayList<Node>();
        final IEnergyTile ioTile = tile.getMainTile();
        for (final Node node : tile.nodes) {
            if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.debug(LogCategory.EnergyNet, "Adding node %s.", node);
            }
            final List<Node> neighbors = new ArrayList<Node>();
            for (final IEnergyTile subTile : tile.subTiles) {
                for (final EnumFacing dir : EnumFacing.VALUES) {
                    final BlockPos coords = EnergyNet.instance.getPos(subTile).offset(dir);
                    final Tile neighborTile = enet.registeredTiles.get(coords);
                    if (neighborTile != null) {
                        if (neighborTile != node.tile) {
                            for (final Node neighbor : neighborTile.nodes) {
                                if (neighbor.isExtraNode()) {
                                    continue;
                                }
                                final IEnergyTile neighborIoTile = neighbor.tile.getMainTile();
                                boolean canEmit = false;
                                if ((node.nodeType == NodeType.Source || node.nodeType == NodeType.Conductor) && neighbor.nodeType != NodeType.Source) {
                                    final IEnergyEmitter emitter = (IEnergyEmitter)((subTile instanceof IEnergyEmitter) ? subTile : ioTile);
                                    final IEnergyTile neighborSubTe = neighborTile.getSubTileAt(coords);
                                    final IEnergyAcceptor acceptor = (IEnergyAcceptor)((neighborSubTe instanceof IEnergyAcceptor) ? neighborSubTe : neighborIoTile);
                                    canEmit = (emitter.emitsEnergyTo((IEnergyAcceptor)neighborIoTile, dir) && acceptor.acceptsEnergyFrom((IEnergyEmitter)ioTile, dir.getOpposite()));
                                }
                                boolean canAccept = false;
                                if (!canEmit && (node.nodeType == NodeType.Sink || node.nodeType == NodeType.Conductor) && neighbor.nodeType != NodeType.Sink) {
                                    final IEnergyAcceptor acceptor2 = (IEnergyAcceptor)((subTile instanceof IEnergyAcceptor) ? subTile : ioTile);
                                    final IEnergyTile neighborSubTe2 = neighborTile.getSubTileAt(coords);
                                    final IEnergyEmitter emitter2 = (IEnergyEmitter)((neighborSubTe2 instanceof IEnergyEmitter) ? neighborSubTe2 : neighborIoTile);
                                    canAccept = (acceptor2.acceptsEnergyFrom((IEnergyEmitter)neighborIoTile, dir) && emitter2.emitsEnergyTo((IEnergyAcceptor)ioTile, dir.getOpposite()));
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
                if (EnergyNetSettings.logGridUpdatesVerbose) {
                    IC2.log.debug(LogCategory.EnergyNet, "Creating new grid for %s.", node);
                }
                final Grid grid = new Grid(enet);
                grid.add(node, neighbors);
            }
            else {
                switch (node.nodeType) {
                    case Conductor: {
                        Grid grid = null;
                        for (final Node neighbor2 : neighbors) {
                            if (neighbor2.nodeType == NodeType.Conductor || neighbor2.links.isEmpty()) {
                                if (EnergyNetSettings.logGridUpdatesVerbose) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Using %s for %s with neighbors %s.", neighbor2.getGrid(), node, neighbors);
                                }
                                grid = neighbor2.getGrid();
                                break;
                            }
                        }
                        if (grid == null) {
                            if (EnergyNetSettings.logGridUpdatesVerbose) {
                                IC2.log.debug(LogCategory.EnergyNet, "Creating new grid for %s with neighbors %s.", node, neighbors);
                            }
                            grid = new Grid(enet);
                        }
                        final Map<Node, Node> neighborReplacements = new HashMap<Node, Node>();
                        ListIterator<Node> it = neighbors.listIterator();
                        while (it.hasNext()) {
                            Node neighbor3 = it.next();
                            if (neighbor3.getGrid() == grid) {
                                continue;
                            }
                            if (neighbor3.nodeType != NodeType.Conductor && !neighbor3.links.isEmpty()) {
                                boolean found = false;
                                for (int i = 0; i < it.previousIndex(); ++i) {
                                    final Node neighbor4 = neighbors.get(i);
                                    if (neighbor4.tile == neighbor3.tile && neighbor4.nodeType == neighbor3.nodeType && neighbor4.getGrid() == grid) {
                                        if (EnergyNetSettings.logGridUpdatesVerbose) {
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
                                if (EnergyNetSettings.logGridUpdatesVerbose) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for neighbor %s.", neighbor3);
                                }
                                neighbor3 = new Node(enet.allocateNodeId(), neighbor3.tile, neighbor3.nodeType);
                                neighbor3.tile.addExtraNode(neighbor3);
                                grid.add(neighbor3, (Collection<Node>)Collections.emptyList());
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
                            Node neighbor3 = it.next();
                            final Node replacement = neighborReplacements.get(neighbor3);
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
                        final List<List<Node>> neighborGroups = new ArrayList<List<Node>>();
                        for (final Node neighbor2 : neighbors) {
                            boolean found2 = false;
                            if (node.nodeType == NodeType.Conductor) {
                                for (final List<Node> nodeList : neighborGroups) {
                                    final Node neighbor4 = nodeList.get(0);
                                    if (neighbor4.nodeType == NodeType.Conductor && neighbor4.getGrid() == neighbor2.getGrid()) {
                                        nodeList.add(neighbor2);
                                        found2 = true;
                                        break;
                                    }
                                }
                            }
                            if (!found2) {
                                final List<Node> nodeList2 = new ArrayList<Node>();
                                nodeList2.add(neighbor2);
                                neighborGroups.add(nodeList2);
                            }
                        }
                        if (EnergyNetSettings.logGridUpdatesVerbose) {
                            IC2.log.debug(LogCategory.EnergyNet, "Neighbor groups detected for %s: %s.", node, neighborGroups);
                        }
                        assert !neighborGroups.isEmpty();
                        for (int j = 0; j < neighborGroups.size(); ++j) {
                            final List<Node> nodeList3 = neighborGroups.get(j);
                            Node neighbor3 = nodeList3.get(0);
                            if (neighbor3.nodeType != NodeType.Conductor && !neighbor3.links.isEmpty()) {
                                assert nodeList3.size() == 1;
                                if (EnergyNetSettings.logGridUpdatesVerbose) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for neighbor %s.", neighbor3);
                                }
                                neighbor3 = new Node(enet.allocateNodeId(), neighbor3.tile, neighbor3.nodeType);
                                neighbor3.tile.addExtraNode(neighbor3);
                                new Grid(enet).add(neighbor3, (Collection<Node>)Collections.emptyList());
                                nodeList3.set(0, neighbor3);
                                assert neighbor3.getGrid() != null;
                            }
                            Node currentNode;
                            if (j == 0) {
                                currentNode = node;
                            }
                            else {
                                if (EnergyNetSettings.logGridUpdatesVerbose) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for %s.", node);
                                }
                                currentNode = new Node(enet.allocateNodeId(), tile, node.nodeType);
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
        for (final Node node : extraNodes) {
            tile.addExtraNode(node);
        }
    }
    
    static void applyRemoval(final EnergyNetLocal enet, final IEnergyTile ioTile, final BlockPos pos) {
        final Tile tile = enet.registeredIoTiles.remove(ioTile);
        if (tile == null) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s removal without registration", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos));
            }
            return;
        }
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "Removing tile %s.", Util.toString(ioTile, (IBlockAccess)enet.getWorld(), pos));
        }
        assert tile.getMainTile() == ioTile;
        if (ioTile instanceof IEnergySource) {
            enet.sources.remove(tile);
        }
        for (final IEnergyTile subTile : tile.subTiles) {
            final BlockPos subPos = EnergyNet.instance.getPos(subTile);
            enet.registeredTiles.remove(subPos);
            enet.addPositionToNotify(subPos);
        }
        removeTileFromGrids(tile);
        for (final IEnergyNetEventReceiver receiver : EnergyNetGlobal.getEventReceivers()) {
            receiver.onRemove(ioTile);
        }
    }
    
    private static void removeTileFromGrids(final Tile tile) {
        for (final Node node : tile.nodes) {
            node.getGrid().remove(node);
        }
    }
}
