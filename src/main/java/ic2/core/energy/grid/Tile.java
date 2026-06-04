// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import ic2.api.energy.EnergyNet;
import net.minecraft.util.math.BlockPos;
import java.util.Iterator;
import java.util.Collection;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import java.util.ArrayList;
import java.util.List;
import ic2.api.energy.tile.IEnergyTile;

public class Tile
{
    private final IEnergyTile mainTile;
    final List<IEnergyTile> subTiles;
    final List<Node> nodes;
    private boolean disabled;
    private double amount;
    private int packetCount;
    
    Tile(final EnergyNetLocal enet, final IEnergyTile mainTile, final List<IEnergyTile> subTiles) {
        this.nodes = new ArrayList<Node>();
        this.mainTile = mainTile;
        this.subTiles = subTiles;
        if (mainTile instanceof IEnergySource) {
            this.nodes.add(new Node(enet.allocateNodeId(), this, NodeType.Source));
        }
        if (mainTile instanceof IEnergySink) {
            this.nodes.add(new Node(enet.allocateNodeId(), this, NodeType.Sink));
        }
        if (mainTile instanceof IEnergyConductor) {
            this.nodes.add(new Node(enet.allocateNodeId(), this, NodeType.Conductor));
        }
    }
    
    public IEnergyTile getMainTile() {
        return this.mainTile;
    }
    
    public Collection<Node> getNodes() {
        return this.nodes;
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
        }
        return canBeRemoved;
    }
    
    public Collection<IEnergyTile> getSubTiles() {
        return this.subTiles;
    }
    
    IEnergyTile getSubTileAt(final BlockPos pos) {
        for (final IEnergyTile subTile : this.subTiles) {
            if (EnergyNet.instance.getPos(subTile).equals((Object)pos)) {
                return subTile;
            }
        }
        return null;
    }
    
    void setDisabled() {
        this.disabled = true;
    }
    
    public boolean isDisabled() {
        return this.disabled;
    }
    
    public double getAmount() {
        return this.amount;
    }
    
    public void setAmount(final double amount) {
        this.amount = amount;
    }
    
    public int getPacketCount() {
        return this.packetCount;
    }
    
    public void setSourceData(final double amount, final int packetCount) {
        this.amount = amount;
        this.packetCount = packetCount;
    }
    
    @Override
    public String toString() {
        String ret = getTeClassName(this.mainTile);
        final World world = EnergyNet.instance.getWorld(this.mainTile);
        final MinecraftServer server = world.getMinecraftServer();
        if (server != null && server.isCallingFromMinecraftThread()) {
            final BlockPos pos = EnergyNet.instance.getPos(this.mainTile);
            if (world.isBlockLoaded(pos)) {
                final TileEntity te = world.getTileEntity(pos);
                if (te != null) {
                    ret = ret + "|" + getTeClassName(te);
                }
                else {
                    ret = ret + "|" + world.getBlockState(pos);
                }
            }
        }
        return ret;
    }
    
    private static String getTeClassName(final Object o) {
        return o.getClass().getSimpleName().replace("TileEntity", "");
    }
}
