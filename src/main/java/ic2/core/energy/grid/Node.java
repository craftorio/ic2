// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import java.util.Iterator;
import java.util.Collection;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyConductor;
import java.util.ArrayList;
import java.util.List;

public class Node
{
    final int uid;
    final Tile tile;
    final NodeType nodeType;
    private boolean isExtraNode;
    private Grid grid;
    List<NodeLink> links;
    
    Node(final int uid, final Tile tile, final NodeType nodeType) {
        this.isExtraNode = false;
        this.links = new ArrayList<NodeLink>();
        if (tile == null) {
            throw new NullPointerException("null tile");
        }
        if (nodeType == null) {
            throw new NullPointerException("null node type");
        }
        assert !(!(tile.getMainTile() instanceof IEnergyConductor));
        assert !(!(tile.getMainTile() instanceof IEnergySink));
        assert !(!(tile.getMainTile() instanceof IEnergySource));
        this.uid = uid;
        this.tile = tile;
        this.nodeType = nodeType;
    }
    
    public Tile getTile() {
        return this.tile;
    }
    
    public NodeType getType() {
        return this.nodeType;
    }
    
    boolean isExtraNode() {
        return this.isExtraNode;
    }
    
    void setExtraNode(final boolean isExtraNode) {
        if (this.nodeType == NodeType.Conductor) {
            throw new IllegalStateException("A conductor can't be an extra node.");
        }
        this.isExtraNode = isExtraNode;
    }
    
    public Grid getGrid() {
        return this.grid;
    }
    
    void setGrid(final Grid grid) {
        if (grid == null) {
            throw new NullPointerException("null grid");
        }
        assert this.grid == null;
        this.grid = grid;
    }
    
    void clearGrid() {
        assert this.grid != null;
        this.grid = null;
    }
    
    public Collection<NodeLink> getLinks() {
        return this.links;
    }
    
    public NodeLink getLinkTo(final Node node) {
        for (final NodeLink link : this.links) {
            if (link.getNeighbor(this) == node) {
                return link;
            }
        }
        return null;
    }
    
    double getInnerLoss() {
        switch (this.nodeType) {
            case Source: {
                return 0.002;
            }
            case Sink: {
                return 0.002;
            }
            case Conductor: {
                return ((IEnergyConductor)this.tile.getMainTile()).getConductionLoss();
            }
            default: {
                throw new RuntimeException("invalid nodetype: " + this.nodeType);
            }
        }
    }
    
    @Override
    public String toString() {
        String type = null;
        switch (this.nodeType) {
            case Conductor: {
                type = "C";
                break;
            }
            case Sink: {
                type = "A";
                break;
            }
            case Source: {
                type = "E";
                break;
            }
        }
        return this.tile + "|" + type + "|" + this.uid;
    }
}
