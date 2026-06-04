// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import net.minecraft.util.math.BlockPos;
import java.util.Iterator;
import net.minecraft.util.math.Vec3i;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.util.EnumFacing;

public class NodeLink
{
    Node nodeA;
    Node nodeB;
    EnumFacing dirFromA;
    EnumFacing dirFromB;
    double loss;
    List<Node> skippedNodes;
    
    NodeLink(final Node nodeA, final Node nodeB, final double loss) {
        this(nodeA, nodeB, loss, null, null);
        this.calculateDirections();
    }
    
    NodeLink(final NodeLink link) {
        this(link.nodeA, link.nodeB, link.loss, link.dirFromA, link.dirFromB);
        this.skippedNodes.addAll(link.skippedNodes);
    }
    
    private NodeLink(final Node nodeA1, final Node nodeB1, final double loss1, final EnumFacing dirFromA, final EnumFacing dirFromB) {
        this.skippedNodes = new ArrayList<Node>();
        assert nodeA1 != nodeB1;
        this.nodeA = nodeA1;
        this.nodeB = nodeB1;
        this.loss = loss1;
        this.dirFromA = dirFromA;
        this.dirFromB = dirFromB;
    }
    
    public Node getNeighbor(final Node node) {
        if (this.nodeA == node) {
            return this.nodeB;
        }
        return this.nodeA;
    }
    
    Node getNeighbor(final int uid) {
        if (this.nodeA.uid == uid) {
            return this.nodeB;
        }
        return this.nodeA;
    }
    
    public double getLoss() {
        return this.loss;
    }
    
    void replaceNode(final Node oldNode, final Node newNode) {
        if (this.nodeA == oldNode) {
            this.nodeA = newNode;
        }
        else {
            if (this.nodeB != oldNode) {
                throw new IllegalArgumentException("Node " + oldNode + " isn't in " + this + ".");
            }
            this.nodeB = newNode;
        }
    }
    
    public EnumFacing getDirFrom(final Node node) {
        if (this.nodeA == node) {
            return this.dirFromA;
        }
        if (this.nodeB == node) {
            return this.dirFromB;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "NodeLink:" + this.nodeA + "@" + this.dirFromA + "->" + this.nodeB + "@" + this.dirFromB;
    }
    
    private void calculateDirections() {
        for (final IEnergyTile posA : this.nodeA.tile.subTiles) {
            for (final IEnergyTile posB : this.nodeB.tile.subTiles) {
                final BlockPos delta = EnergyNet.instance.getPos(posA).subtract((Vec3i)EnergyNet.instance.getPos(posB));
                for (final EnumFacing dir : EnumFacing.VALUES) {
                    if (dir.getFrontOffsetX() == delta.getX() && dir.getFrontOffsetY() == delta.getY() && dir.getFrontOffsetZ() == delta.getZ()) {
                        this.dirFromA = dir;
                        this.dirFromB = dir.getOpposite();
                        return;
                    }
                }
            }
        }
        assert false;
        this.dirFromA = null;
        this.dirFromB = null;
    }
}
