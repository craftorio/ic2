package ic2.core.energy.grid;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Tile {
   private final IEnergyTile mainTile;
   final List<IEnergyTile> subTiles;
   final List<Node> nodes = new ArrayList<>();
   private boolean disabled;
   private double amount;
   private int packetCount;

   Tile(EnergyNetLocal enet, IEnergyTile mainTile, List<IEnergyTile> subTiles) {
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

   void addExtraNode(Node node) {
      node.setExtraNode(true);
      this.nodes.add(node);
   }

   boolean removeExtraNode(Node node) {
      boolean canBeRemoved = false;
      if (node.isExtraNode()) {
         canBeRemoved = true;
      } else {
         for (Node otherNode : this.nodes) {
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

   IEnergyTile getSubTileAt(BlockPos pos) {
      for (IEnergyTile subTile : this.subTiles) {
         if (EnergyNet.instance.getPos(subTile).equals(pos)) {
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

   public void setAmount(double amount) {
      this.amount = amount;
   }

   public int getPacketCount() {
      return this.packetCount;
   }

   public void setSourceData(double amount, int packetCount) {
      this.amount = amount;
      this.packetCount = packetCount;
   }

   @Override
   public String toString() {
      String ret = getTeClassName(this.mainTile);
      World world = EnergyNet.instance.getWorld(this.mainTile);
      MinecraftServer server = world.getMinecraftServer();
      if (server != null && server.isCallingFromMinecraftThread()) {
         BlockPos pos = EnergyNet.instance.getPos(this.mainTile);
         if (world.isBlockLoaded(pos)) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
               ret = ret + "|" + getTeClassName(te);
            } else {
               ret = ret + "|" + world.getBlockState(pos);
            }
         }
      }

      return ret;
   }

   private static String getTeClassName(Object o) {
      return o.getClass().getSimpleName().replace("TileEntity", "");
   }
}
