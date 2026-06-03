package ic2.core.energy.grid;

import ic2.api.energy.tile.IEnergyConductor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Node {
  final int uid;
  
  final Tile tile;
  
  final NodeType nodeType;
  
  Node(int uid, Tile tile, NodeType nodeType) {
    if (tile == null)
      throw new NullPointerException("null tile"); 
    if (nodeType == null)
      throw new NullPointerException("null node type"); 
    assert nodeType != NodeType.Conductor || tile.getMainTile() instanceof IEnergyConductor;
    assert nodeType != NodeType.Sink || tile.getMainTile() instanceof ic2.api.energy.tile.IEnergySink;
    assert nodeType != NodeType.Source || tile.getMainTile() instanceof ic2.api.energy.tile.IEnergySource;
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
  
  void setExtraNode(boolean isExtraNode) {
    if (this.nodeType == NodeType.Conductor)
      throw new IllegalStateException("A conductor can't be an extra node."); 
    this.isExtraNode = isExtraNode;
  }
  
  public Grid getGrid() {
    return this.grid;
  }
  
  void setGrid(Grid grid) {
    if (grid == null)
      throw new NullPointerException("null grid"); 
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
  
  public NodeLink getLinkTo(Node node) {
    for (NodeLink link : this.links) {
      if (link.getNeighbor(this) == node)
        return link; 
    } 
    return null;
  }
  
  double getInnerLoss() {
    switch (this.nodeType) {
      case Source:
        return 0.002D;
      case Sink:
        return 0.002D;
      case Conductor:
        return ((IEnergyConductor)this.tile.getMainTile()).getConductionLoss();
    } 
    throw new RuntimeException("invalid nodetype: " + this.nodeType);
  }
  
  public String toString() {
    String type = null;
    switch (this.nodeType) {
      case Conductor:
        type = "C";
        break;
      case Sink:
        type = "A";
        break;
      case Source:
        type = "E";
        break;
    } 
    return this.tile + "|" + type + "|" + this.uid;
  }
  
  private boolean isExtraNode = false;
  
  private Grid grid;
  
  List<NodeLink> links = new ArrayList<>();
}
