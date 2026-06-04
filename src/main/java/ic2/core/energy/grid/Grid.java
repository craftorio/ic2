// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import java.io.IOException;
import java.util.HashSet;
import java.io.FileWriter;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import java.io.PrintStream;
import net.minecraft.util.math.BlockPos;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;
import java.util.Queue;
import java.util.IdentityHashMap;
import java.util.Collections;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Grid
{
    private final int uid;
    private final EnergyNetLocal enet;
    private final Map<Integer, Node> nodes;
    private boolean dirty;
    private Object data;
    
    Grid(final EnergyNetLocal enet) {
        this.nodes = new HashMap<Integer, Node>();
        this.uid = enet.allocateGridId();
        (this.enet = enet).addGrid(this);
    }
    
    public EnergyNetLocal getEnergyNet() {
        return this.enet;
    }
    
    public Node getNode(final int id) {
        return this.nodes.get(id);
    }
    
    public Collection<Node> getNodes() {
        return this.nodes.values();
    }
    
    public boolean clearDirty() {
        if (!this.dirty) {
            return false;
        }
        this.dirty = false;
        return true;
    }
    
    public <T> T getData() {
        return (T)this.data;
    }
    
    public void setData(final Object data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "Grid " + this.uid;
    }
    
    void add(final Node node, final Collection<Node> neighbors) {
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Add %s to %s neighbors: %s.", this.uid, node, this, neighbors);
        }
        this.invalidate();
        assert !(!neighbors.isEmpty());
        assert !(!node.isExtraNode());
        assert node.links.isEmpty();
        this.add(node);
        for (final Node neighbor : neighbors) {
            assert neighbor != node;
            assert this.nodes.containsKey(neighbor.uid);
            final double loss = (node.getInnerLoss() + neighbor.getInnerLoss()) / 2.0;
            final NodeLink link = new NodeLink(node, neighbor, loss);
            node.links.add(link);
            neighbor.links.add(link);
        }
    }
    
    void remove(final Node node) {
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Remove Node %s from %s with %d nodes.", this.uid, node, this, this.nodes.size());
        }
        this.invalidate();
        final Iterator<NodeLink> it = node.links.iterator();
        while (it.hasNext()) {
            final NodeLink link = it.next();
            final Node neighbor = link.getNeighbor(node);
            boolean found = false;
            final Iterator<NodeLink> it2 = neighbor.links.iterator();
            while (it2.hasNext()) {
                if (it2.next() == link) {
                    it2.remove();
                    found = true;
                    break;
                }
            }
            assert found;
            if (!neighbor.links.isEmpty() || !neighbor.tile.removeExtraNode(neighbor)) {
                continue;
            }
            if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.debug(LogCategory.EnergyNet, "%d Removing isolated extra node %s.", this.uid, neighbor);
            }
            assert neighbor.getType() != NodeType.Conductor;
            it.remove();
            this.nodes.remove(neighbor.uid);
            neighbor.clearGrid();
        }
        this.nodes.remove(node.uid);
        node.clearGrid();
        final int linkCount = node.links.size();
        if (linkCount == 0) {
            assert this.nodes.isEmpty();
            this.enet.removeGrid(this);
        }
        else if (linkCount > 1 && node.nodeType == NodeType.Conductor) {
            final Set<Node>[] nodeTable = new Set[linkCount];
            final int[] mapping = new int[linkCount];
            int gridCount = 0;
            final Queue<Node> nodesToCheck = new ArrayDeque<Node>();
        Label_0680:
            for (int i = 0; i < linkCount; ++i) {
                final Node neighbor2 = node.links.get(i).getNeighbor(node);
                if (neighbor2.getType() != NodeType.Conductor) {
                    if (neighbor2.links.isEmpty()) {
                        nodeTable[i] = Collections.singleton(neighbor2);
                        ++gridCount;
                    }
                    else {
                        mapping[i] = -1;
                    }
                }
                else {
                    for (int j = 0; j < i; ++j) {
                        final Set<Node> nodes = nodeTable[j];
                        if (nodes != null && nodes.contains(neighbor2)) {
                            mapping[i] = j;
                            continue Label_0680;
                        }
                    }
                    final Set<Node> connectedNodes = Collections.newSetFromMap(new IdentityHashMap<Node, Boolean>());
                    nodesToCheck.add(neighbor2);
                    connectedNodes.add(neighbor2);
                    Node cNode;
                    while ((cNode = nodesToCheck.poll()) != null) {
                        for (final NodeLink link2 : cNode.links) {
                            final Node nNode = link2.getNeighbor(cNode);
                            if (connectedNodes.add(nNode) && nNode.getType() == NodeType.Conductor) {
                                nodesToCheck.add(nNode);
                            }
                        }
                    }
                    assert !connectedNodes.contains(node);
                    nodeTable[i] = connectedNodes;
                    ++gridCount;
                }
            }
            assert gridCount > 0;
            if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.debug(LogCategory.EnergyNet, "%d Neighbor connectivity (%d links, %d new grids):", this.uid, linkCount, gridCount);
                for (int i = 0; i < linkCount; ++i) {
                    final Set<Node> nodes2 = nodeTable[i];
                    if (nodes2 != null) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d %d: %s: %s (%d).", this.uid, i, node.links.get(i).getNeighbor(node), nodes2, nodes2.size());
                    }
                    else {
                        IC2.log.debug(LogCategory.EnergyNet, "%d %d: %s contained in %d.", this.uid, i, node.links.get(i).getNeighbor(node), mapping[i]);
                    }
                }
            }
            if (gridCount <= 1) {
                return;
            }
            for (int i = 1; i < linkCount; ++i) {
                final Set<Node> connectedNodes2 = nodeTable[i];
                if (connectedNodes2 != null) {
                    final Grid grid = new Grid(this.enet);
                    if (EnergyNetSettings.logGridUpdatesVerbose) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d Moving %d nodes from net %d to new grid %d.", this.uid, connectedNodes2.size(), i, grid.uid);
                    }
                    for (final Node cNode2 : connectedNodes2) {
                        boolean needsExtraNode = false;
                        if (!cNode2.links.isEmpty() && cNode2.nodeType != NodeType.Conductor) {
                            for (final Set<Node> nodes3 : nodeTable) {
                                if (nodes3 != null && nodes3.contains(cNode2)) {
                                    needsExtraNode = true;
                                    break;
                                }
                            }
                        }
                        if (needsExtraNode) {
                            final Node extraNode = new Node(this.enet.allocateNodeId(), cNode2.tile, cNode2.nodeType);
                            if (EnergyNetSettings.logGridUpdatesVerbose) {
                                IC2.log.debug(LogCategory.EnergyNet, "%s Create extra Node %d for %s in grid %d.", this.uid, extraNode.uid, cNode2, grid.uid);
                            }
                            cNode2.tile.addExtraNode(extraNode);
                            final Iterator<NodeLink> it3 = cNode2.links.iterator();
                            while (it3.hasNext()) {
                                final NodeLink link3 = it3.next();
                                final Node neighbor3 = link3.getNeighbor(cNode2);
                                if (connectedNodes2.contains(neighbor3)) {
                                    assert neighbor3.nodeType == NodeType.Conductor;
                                    link3.replaceNode(cNode2, extraNode);
                                    extraNode.links.add(link3);
                                    it3.remove();
                                }
                            }
                            assert !extraNode.links.isEmpty();
                            grid.add(extraNode);
                            assert extraNode.getGrid() != null;
                            continue;
                        }
                        else {
                            if (EnergyNetSettings.logGridUpdatesVerbose) {
                                IC2.log.debug(LogCategory.EnergyNet, "%d Move Node %s to grid %d.", this.uid, cNode2, grid.uid);
                            }
                            assert this.nodes.containsKey(cNode2.uid);
                            this.nodes.remove(cNode2.uid);
                            cNode2.clearGrid();
                            grid.add(cNode2);
                            assert cNode2.getGrid() != null;
                            continue;
                        }
                    }
                }
            }
        }
    }
    
    void merge(final Grid grid, final Map<Node, Node> nodeReplacements) {
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Merge %s -> %s.", this.uid, grid, this);
        }
        assert this.enet.hasGrid(grid);
        this.invalidate();
        for (final Node node : grid.nodes.values()) {
            boolean found = false;
            if (node.nodeType != NodeType.Conductor) {
                for (final Node node2 : node.tile.nodes) {
                    if (node2.nodeType == node.nodeType && node2.getGrid() == this) {
                        if (EnergyNetSettings.logGridUpdatesVerbose) {
                            IC2.log.debug(LogCategory.EnergyNet, "%d Merge Node %s -> %s.", this.uid, node, node2);
                        }
                        found = true;
                        for (final NodeLink link : node.links) {
                            link.replaceNode(node, node2);
                            node2.links.add(link);
                        }
                        node2.tile.removeExtraNode(node);
                        nodeReplacements.put(node, node2);
                        break;
                    }
                }
            }
            if (!found) {
                if (EnergyNetSettings.logGridUpdatesVerbose) {
                    IC2.log.debug(LogCategory.EnergyNet, "%d Add Node %s.", this.uid, node);
                }
                node.clearGrid();
                this.add(node);
                assert node.getGrid() != null;
                continue;
            }
        }
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "Remove %s.", grid);
        }
        this.enet.removeGrid(grid);
    }
    
    private void add(final Node node) {
        node.setGrid(this);
        final Node prev = this.nodes.put(node.uid, node);
        if (prev != null) {
            throw new IllegalStateException("duplicate node uid, new " + node + ", old " + prev);
        }
    }
    
    private void invalidate() {
        this.dirty = true;
    }
    
    GridInfo getInfo() {
        int complexNodes = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (final Node node : this.nodes.values()) {
            if (node.links.size() > 2) {
                ++complexNodes;
            }
            for (final IEnergyTile tile : node.tile.subTiles) {
                final BlockPos pos = EnergyNet.instance.getPos(tile);
                if (pos.getX() < minX) {
                    minX = pos.getX();
                }
                if (pos.getY() < minY) {
                    minY = pos.getY();
                }
                if (pos.getZ() < minZ) {
                    minZ = pos.getZ();
                }
                if (pos.getX() > maxX) {
                    maxX = pos.getX();
                }
                if (pos.getY() > maxY) {
                    maxY = pos.getY();
                }
                if (pos.getZ() > maxZ) {
                    maxZ = pos.getZ();
                }
            }
        }
        return new GridInfo(this.uid, this.nodes.size(), complexNodes, minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    void dumpInfo(final String prefix, final PrintStream console, final PrintStream chat) {
        chat.printf("%sGrid %d info:%n", prefix, this.uid);
        chat.printf("%s %d nodes%n", prefix, this.nodes.size());
    }
    
    void dumpNodeInfo(final Node node, final String prefix, final PrintStream console, final PrintStream chat) {
        final IEnergyTile ioTile = node.getTile().getMainTile();
        chat.printf("%sNode %s info:%n", prefix, node);
        chat.printf("%s pos: %s%n", prefix, Util.formatPosition((IBlockAccess)EnergyNet.instance.getWorld(ioTile), EnergyNet.instance.getPos(ioTile)));
        chat.printf("%s type: %s%n", prefix, node.nodeType);
        switch (node.nodeType) {
            case Sink: {
                final IEnergySink sink = (IEnergySink)ioTile;
                chat.printf("%s demanded: %.2f%n", prefix, sink.getDemandedEnergy());
                chat.printf("%s tier: %d%n", prefix, sink.getSinkTier());
                break;
            }
            case Source: {
                final IEnergySource source = (IEnergySource)ioTile;
                chat.printf("%s offered: %.2f%n", prefix, source.getOfferedEnergy());
                chat.printf("%s tier: %d%n", prefix, source.getSourceTier());
                break;
            }
        }
        chat.printf("%s %d neighbor links:%n", prefix, node.links.size());
        for (final NodeLink link : node.links) {
            chat.printf("%s  %s %.4f %s%n", prefix, link.getNeighbor(node), link.loss, link.skippedNodes);
        }
        EnergyNetGlobal.getCalculator().dumpNodeInfo(node, prefix + " ", console, chat);
    }
    
    void dumpGraph() {
        FileWriter out = null;
        try {
            out = new FileWriter("graph_" + this.uid + "_raw.txt");
            out.write("graph nodes {\n  overlap=false;\n");
            final Collection<Node> nodesToDump = this.nodes.values();
            final Set<Node> dumpedConnections = new HashSet<Node>();
            for (final Node node : nodesToDump) {
                out.write("  \"" + node + "\";\n");
                for (final NodeLink link : node.links) {
                    final Node neighbor = link.getNeighbor(node);
                    if (!dumpedConnections.contains(neighbor)) {
                        out.write("  \"" + node + "\" -- \"" + neighbor + "\" [label=\"" + link.loss + "\"];\n");
                    }
                }
                dumpedConnections.add(node);
            }
            out.write("}\n");
        }
        catch (final IOException e) {
            IC2.log.debug(LogCategory.EnergyNet, e, "Graph saving failed.");
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            }
            catch (final IOException ex) {}
        }
    }
}
