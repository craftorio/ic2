// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy;

import ic2.api.energy.NodeStats;
import ic2.api.energy.EnergyNet;
import net.minecraft.util.math.BlockPos;
import java.util.Iterator;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.core.energy.grid.NodeType;
import ic2.api.energy.tile.IEnergySource;
import java.util.Arrays;
import java.util.Collection;
import ic2.api.energy.tile.IMetaDelegate;
import java.util.ArrayList;
import java.util.List;
import ic2.api.energy.tile.IEnergyTile;

class Tile
{
    final IEnergyTile mainTile;
    final List<IEnergyTile> subTiles;
    final List<Node> nodes;
    final double maxCurrent;
    
    Tile(final EnergyNetLocal energyNet, final IEnergyTile mainTile) {
        this.nodes = new ArrayList<Node>();
        this.mainTile = mainTile;
        if (mainTile instanceof IMetaDelegate) {
            this.subTiles = new ArrayList<IEnergyTile>(((IMetaDelegate)mainTile).getSubTiles());
            if (this.subTiles.isEmpty()) {
                throw new RuntimeException("Tile " + mainTile + " must return at least 1 sub tile for IMetaDelegate.getSubTiles().");
            }
        }
        else {
            this.subTiles = Arrays.asList(mainTile);
        }
        if (mainTile instanceof IEnergySource) {
            this.nodes.add(new Node(energyNet, this, NodeType.Source));
        }
        if (mainTile instanceof IEnergySink) {
            this.nodes.add(new Node(energyNet, this, NodeType.Sink));
        }
        if (mainTile instanceof IEnergyConductor) {
            this.nodes.add(new Node(energyNet, this, NodeType.Conductor));
            this.maxCurrent = ((IEnergyConductor)mainTile).getConductorBreakdownEnergy();
        }
        else {
            this.maxCurrent = Double.MAX_VALUE;
        }
    }
    
    void addExtraNode(final Node node) {
        node.setExtraNode(true);
        this.nodes.add(node);
    }
    
    boolean removeExtraNode(final Node node) {
        boolean canBeRemoved = false;
        if (node.isExtraNode()) {
            canBeRemoved = true;
        }
        else {
            for (final Node otherNode : this.nodes) {
                if (otherNode != node && otherNode.nodeType == node.nodeType && otherNode.isExtraNode()) {
                    otherNode.setExtraNode(false);
                    canBeRemoved = true;
                    break;
                }
            }
        }
        if (canBeRemoved) {
            this.nodes.remove(node);
            return true;
        }
        return false;
    }
    
    IEnergyTile getSubTileAt(final BlockPos pos) {
        for (final IEnergyTile subTile : this.subTiles) {
            if (EnergyNet.instance.getPos(subTile).equals((Object)pos)) {
                return subTile;
            }
        }
        return null;
    }
    
    Iterable<NodeStats> getStats() {
        final List<NodeStats> ret = new ArrayList<NodeStats>(this.nodes.size());
        for (final Node node : this.nodes) {
            ret.add(node.getStats());
        }
        return ret;
    }
}
