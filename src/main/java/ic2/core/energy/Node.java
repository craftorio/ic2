package ic2.core.energy;

import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.core.IC2;
import ic2.core.energy.grid.NodeType;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.List;

class Node {
  final int uid;
  
  final Tile tile;
  
  final NodeType nodeType;
  
  private final Node parent;
  
  Node(EnergyNetLocal energyNet, Tile tile1, NodeType nodeType1) {
    if (energyNet == null)
      throw new NullPointerException("The energyNet parameter must not be null."); 
    if (tile1 == null)
      throw new NullPointerException("The tile parameter must not be null."); 
    assert nodeType1 != NodeType.Conductor || tile1.mainTile instanceof IEnergyConductor;
    assert nodeType1 != NodeType.Sink || tile1.mainTile instanceof ic2.api.energy.tile.IEnergySink;
    assert nodeType1 != NodeType.Source || tile1.mainTile instanceof ic2.api.energy.tile.IEnergySource;
    this.uid = EnergyNetLocal.getNextNodeUid();
    this.tile = tile1;
    this.nodeType = nodeType1;
    this.parent = null;
  }
  
  Node(Node node) {
    this.uid = node.uid;
    this.tile = node.tile;
    this.nodeType = node.nodeType;
    this.parent = node;
    assert this.nodeType != NodeType.Conductor || this.tile.mainTile instanceof IEnergyConductor;
    assert this.nodeType != NodeType.Sink || this.tile.mainTile instanceof ic2.api.energy.tile.IEnergySink;
    assert this.nodeType != NodeType.Source || this.tile.mainTile instanceof ic2.api.energy.tile.IEnergySource;
    for (NodeLink link : node.links) {
      assert (link.getNeighbor(node)).links.contains(link);
      this.links.add(new NodeLink(link));
    } 
  }
  
  double getInnerLoss() {
    switch (this.nodeType) {
      case Source:
        return 0.4D;
      case Sink:
        return 0.4D;
      case Conductor:
        return ((IEnergyConductor)this.tile.mainTile).getConductionLoss();
    } 
    throw new RuntimeException("invalid nodetype: " + this.nodeType);
  }
  
  boolean isExtraNode() {
    return (getTop()).isExtraNode;
  }
  
  void setExtraNode(boolean isExtraNode) {
    if (this.nodeType == NodeType.Conductor)
      throw new IllegalStateException("A conductor can't be an extra node."); 
    (getTop()).isExtraNode = isExtraNode;
  }
  
  int getTier() {
    return (getTop()).tier;
  }
  
  void setTier(int tier) {
    if (tier < 0 || Double.isNaN(tier)) {
      assert false;
      if (EnergyNetGlobal.debugGrid)
        IC2.log.warn(LogCategory.EnergyNet, "Node %s / te %s is using the invalid tier %d.", new Object[] { this, this.tile.mainTile, Integer.valueOf(tier) }); 
      tier = 0;
    } else if (tier > 20 && (tier != Integer.MAX_VALUE || this.nodeType != NodeType.Sink)) {
      if (Util.inDev())
        IC2.log.debug(LogCategory.EnergyNet, "Restricting node %s to tier 20, requested %d.", new Object[] { this, Integer.valueOf(tier) }); 
      tier = 20;
    } 
    (getTop()).tier = tier;
  }
  
  double getAmount() {
    return (getTop()).amount;
  }
  
  void setAmount(double amount) {
    (getTop()).amount = amount;
  }
  
  double getResistance() {
    return (getTop()).resistance;
  }
  
  void setResistance(double resistance) {
    (getTop()).resistance = resistance;
  }
  
  double getVoltage() {
    return (getTop()).voltage;
  }
  
  void setVoltage(double voltage) {
    (getTop()).voltage = voltage;
  }
  
  double getMaxCurrent() {
    return this.tile.maxCurrent;
  }
  
  void resetCurrents() {
    (getTop()).currentIn = 0.0D;
    (getTop()).currentOut = 0.0D;
  }
  
  void addCurrent(double current) {
    if (current >= 0.0D) {
      (getTop()).currentIn += current;
    } else {
      (getTop()).currentOut += -current;
    } 
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
    return this.tile.mainTile.getClass().getSimpleName().replace("TileEntity", "") + "|" + type + "|" + this.tier + "|" + this.uid;
  }
  
  Node getTop() {
    if (this.parent != null)
      return this.parent.getTop(); 
    return this;
  }
  
  NodeLink getConnectionTo(Node node) {
    for (NodeLink link : this.links) {
      if (link.getNeighbor(this) == node)
        return link; 
    } 
    return null;
  }
  
  NodeStats getStats() {
    return this.lastNodeStats;
  }
  
  void updateStats() {
    if (EnergyNetLocal.useLinearTransferModel) {
      this.lastNodeStats.set(this.currentIn * this.voltage, this.currentOut * this.voltage, this.voltage);
    } else {
      this.lastNodeStats.set(this.currentIn, this.currentOut, this.voltage);
    } 
  }
  
  Grid getGrid() {
    return (getTop()).grid;
  }
  
  void setGrid(Grid grid) {
    if (grid == null)
      throw new NullPointerException("null grid"); 
    assert (getTop()).grid == null;
    (getTop()).grid = grid;
  }
  
  void clearGrid() {
    assert (getTop()).grid != null;
    (getTop()).grid = null;
  }
  
  private boolean isExtraNode = false;
  
  private int tier;
  
  private double amount;
  
  private double resistance;
  
  private double voltage;
  
  private double currentIn;
  
  private double currentOut;
  
  private Grid grid;
  
  List<NodeLink> links = new ArrayList<>();
  
  private final MutableNodeStats lastNodeStats = new MutableNodeStats();
}
