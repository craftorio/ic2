// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy;

import ic2.api.energy.NodeStats;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Iterator;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyConductor;
import java.util.ArrayList;
import java.util.List;
import ic2.core.energy.grid.NodeType;

class Node
{
    final int uid;
    final Tile tile;
    final NodeType nodeType;
    private final Node parent;
    private boolean isExtraNode;
    private int tier;
    private double amount;
    private double resistance;
    private double voltage;
    private double currentIn;
    private double currentOut;
    private Grid grid;
    List<NodeLink> links;
    private final MutableNodeStats lastNodeStats;
    
    Node(final EnergyNetLocal energyNet, final Tile tile1, final NodeType nodeType1) {
        this.isExtraNode = false;
        this.links = new ArrayList<NodeLink>();
        this.lastNodeStats = new MutableNodeStats();
        if (energyNet == null) {
            throw new NullPointerException("The energyNet parameter must not be null.");
        }
        if (tile1 == null) {
            throw new NullPointerException("The tile parameter must not be null.");
        }
        assert !(!(tile1.mainTile instanceof IEnergyConductor));
        assert !(!(tile1.mainTile instanceof IEnergySink));
        assert !(!(tile1.mainTile instanceof IEnergySource));
        this.uid = EnergyNetLocal.getNextNodeUid();
        this.tile = tile1;
        this.nodeType = nodeType1;
        this.parent = null;
    }
    
    Node(final Node node) {
        this.isExtraNode = false;
        this.links = new ArrayList<NodeLink>();
        this.lastNodeStats = new MutableNodeStats();
        this.uid = node.uid;
        this.tile = node.tile;
        this.nodeType = node.nodeType;
        this.parent = node;
        assert !(!(this.tile.mainTile instanceof IEnergyConductor));
        assert !(!(this.tile.mainTile instanceof IEnergySink));
        assert !(!(this.tile.mainTile instanceof IEnergySource));
        for (final NodeLink link : node.links) {
            assert link.getNeighbor(node).links.contains(link);
            this.links.add(new NodeLink(link));
        }
    }
    
    double getInnerLoss() {
        switch (this.nodeType) {
            case Source: {
                return 0.4;
            }
            case Sink: {
                return 0.4;
            }
            case Conductor: {
                return ((IEnergyConductor)this.tile.mainTile).getConductionLoss();
            }
            default: {
                throw new RuntimeException("invalid nodetype: " + this.nodeType);
            }
        }
    }
    
    boolean isExtraNode() {
        return this.getTop().isExtraNode;
    }
    
    void setExtraNode(final boolean isExtraNode) {
        if (this.nodeType == NodeType.Conductor) {
            throw new IllegalStateException("A conductor can't be an extra node.");
        }
        this.getTop().isExtraNode = isExtraNode;
    }
    
    int getTier() {
        return this.getTop().tier;
    }
    
    void setTier(int tier) {
        if (tier < 0 || Double.isNaN(tier)) {
            assert false;
            if (EnergyNetGlobal.debugGrid) {
                IC2.log.warn(LogCategory.EnergyNet, "Node %s / te %s is using the invalid tier %d.", this, this.tile.mainTile, tier);
            }
            tier = 0;
        }
        else if (tier > 20 && (tier != Integer.MAX_VALUE || this.nodeType != NodeType.Sink)) {
            if (Util.inDev()) {
                IC2.log.debug(LogCategory.EnergyNet, "Restricting node %s to tier 20, requested %d.", this, tier);
            }
            tier = 20;
        }
        this.getTop().tier = tier;
    }
    
    double getAmount() {
        return this.getTop().amount;
    }
    
    void setAmount(final double amount) {
        this.getTop().amount = amount;
    }
    
    double getResistance() {
        return this.getTop().resistance;
    }
    
    void setResistance(final double resistance) {
        this.getTop().resistance = resistance;
    }
    
    double getVoltage() {
        return this.getTop().voltage;
    }
    
    void setVoltage(final double voltage) {
        this.getTop().voltage = voltage;
    }
    
    double getMaxCurrent() {
        return this.tile.maxCurrent;
    }
    
    void resetCurrents() {
        this.getTop().currentIn = 0.0;
        this.getTop().currentOut = 0.0;
    }
    
    void addCurrent(final double current) {
        if (current >= 0.0) {
            final Node top = this.getTop();
            top.currentIn += current;
        }
        else {
            final Node top2 = this.getTop();
            top2.currentOut += -current;
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
        return this.tile.mainTile.getClass().getSimpleName().replace("TileEntity", "") + "|" + type + "|" + this.tier + "|" + this.uid;
    }
    
    Node getTop() {
        if (this.parent != null) {
            return this.parent.getTop();
        }
        return this;
    }
    
    NodeLink getConnectionTo(final Node node) {
        for (final NodeLink link : this.links) {
            if (link.getNeighbor(this) == node) {
                return link;
            }
        }
        return null;
    }
    
    NodeStats getStats() {
        return this.lastNodeStats;
    }
    
    void updateStats() {
        if (EnergyNetLocal.useLinearTransferModel) {
            this.lastNodeStats.set(this.currentIn * this.voltage, this.currentOut * this.voltage, this.voltage);
        }
        else {
            this.lastNodeStats.set(this.currentIn, this.currentOut, this.voltage);
        }
    }
    
    Grid getGrid() {
        return this.getTop().grid;
    }
    
    void setGrid(final Grid grid) {
        if (grid == null) {
            throw new NullPointerException("null grid");
        }
        assert this.getTop().grid == null;
        this.getTop().grid = grid;
    }
    
    void clearGrid() {
        assert this.getTop().grid != null;
        this.getTop().grid = null;
    }
}
