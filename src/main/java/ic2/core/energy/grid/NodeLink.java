package ic2.core.energy.grid;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class NodeLink {
  Node nodeA;
  
  Node nodeB;
  
  EnumFacing dirFromA;
  
  EnumFacing dirFromB;
  
  double loss;
  
  NodeLink(Node nodeA, Node nodeB, double loss) {
    this(nodeA, nodeB, loss, null, null);
    calculateDirections();
  }
  
  NodeLink(NodeLink link) {
    this(link.nodeA, link.nodeB, link.loss, link.dirFromA, link.dirFromB);
    this.skippedNodes.addAll(link.skippedNodes);
  }
  
  private NodeLink(Node nodeA1, Node nodeB1, double loss1, EnumFacing dirFromA, EnumFacing dirFromB) {
    assert nodeA1 != nodeB1;
    this.nodeA = nodeA1;
    this.nodeB = nodeB1;
    this.loss = loss1;
    this.dirFromA = dirFromA;
    this.dirFromB = dirFromB;
  }
  
  public Node getNeighbor(Node node) {
    if (this.nodeA == node)
      return this.nodeB; 
    return this.nodeA;
  }
  
  Node getNeighbor(int uid) {
    if (this.nodeA.uid == uid)
      return this.nodeB; 
    return this.nodeA;
  }
  
  public double getLoss() {
    return this.loss;
  }
  
  void replaceNode(Node oldNode, Node newNode) {
    if (this.nodeA == oldNode) {
      this.nodeA = newNode;
    } else if (this.nodeB == oldNode) {
      this.nodeB = newNode;
    } else {
      throw new IllegalArgumentException("Node " + oldNode + " isn't in " + this + ".");
    } 
  }
  
  public EnumFacing getDirFrom(Node node) {
    if (this.nodeA == node)
      return this.dirFromA; 
    if (this.nodeB == node)
      return this.dirFromB; 
    return null;
  }
  
  public String toString() {
    return "NodeLink:" + this.nodeA + "@" + this.dirFromA + "->" + this.nodeB + "@" + this.dirFromB;
  }
  
  private void calculateDirections() {
    for (IEnergyTile posA : this.nodeA.tile.subTiles) {
      for (IEnergyTile posB : this.nodeB.tile.subTiles) {
        BlockPos delta = EnergyNet.instance.getPos(posA).func_177973_b((Vec3i)EnergyNet.instance.getPos(posB));
        for (EnumFacing dir : EnumFacing.field_82609_l) {
          if (dir.func_82601_c() == delta.func_177958_n() && dir
            .func_96559_d() == delta.func_177956_o() && dir
            .func_82599_e() == delta.func_177952_p()) {
            this.dirFromA = dir;
            this.dirFromB = dir.func_176734_d();
            return;
          } 
        } 
      } 
    } 
    assert false;
    this.dirFromA = null;
    this.dirFromB = null;
  }
  
  List<Node> skippedNodes = new ArrayList<>();
}
