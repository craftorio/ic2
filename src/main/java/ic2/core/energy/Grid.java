package ic2.core.energy;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.IC2;
import ic2.core.energy.grid.GridInfo;
import ic2.core.energy.grid.NodeType;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.ops.MatrixIO;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

class Grid {
  private final int uid;
  
  private final EnergyNetLocal energyNet;
  
  private final Map<Integer, Node> nodes;
  
  private boolean hasNonZeroVoltages;
  
  private boolean lastVoltagesNeedUpdate;
  
  private final Set<Integer> activeSources;
  
  private final Set<Integer> activeSinks;
  
  private final StructureCache cache;
  
  private Future<Iterable<Node>> calculation;
  
  private StructureCache.Data lastData;
  
  private boolean failed;
  
  Grid(EnergyNetLocal energyNet1) {
    this.nodes = new HashMap<>();
    this.hasNonZeroVoltages = false;
    this.lastVoltagesNeedUpdate = false;
    this.activeSources = new HashSet<>();
    this.activeSinks = new HashSet<>();
    this.cache = new StructureCache();
    this.lastData = null;
    this.uid = EnergyNetLocal.getNextGridUid();
    this.energyNet = energyNet1;
    energyNet1.grids.add(this);
  }
  
  public String toString() {
    return "Grid " + this.uid;
  }
  
  void add(Node node, Collection<Node> neighbors) {
    if (EnergyNetGlobal.debugGrid)
      IC2.log.debug(LogCategory.EnergyNet, "%d Add %s to %s neighbors: %s.", new Object[] { Integer.valueOf(this.uid), node, this, neighbors }); 
    invalidate();
    assert !this.nodes.isEmpty() || neighbors.isEmpty();
    assert this.nodes.isEmpty() || !neighbors.isEmpty() || node.isExtraNode();
    assert node.links.isEmpty();
    add(node);
    for (Node neighbor : neighbors) {
      assert neighbor != node;
      assert this.nodes.containsKey(Integer.valueOf(neighbor.uid));
      double loss = (node.getInnerLoss() + neighbor.getInnerLoss()) / 2.0D;
      NodeLink link = new NodeLink(node, neighbor, loss);
      node.links.add(link);
      neighbor.links.add(link);
    } 
  }
  
  void remove(Node node) {
    if (EnergyNetGlobal.debugGrid)
      IC2.log.debug(LogCategory.EnergyNet, "%d Remove Node %s from %s with %d nodes.", new Object[] { Integer.valueOf(this.uid), node, this, Integer.valueOf(this.nodes.size()) }); 
    invalidate();
    for (Iterator<NodeLink> it = node.links.iterator(); it.hasNext(); ) {
      NodeLink link = it.next();
      Node neighbor = link.getNeighbor(node);
      boolean found = false;
      for (Iterator<NodeLink> it2 = neighbor.links.iterator(); it2.hasNext();) {
        if (it2.next() == link) {
          it2.remove();
          found = true;
          break;
        } 
      } 
      assert found;
      if (neighbor.links.isEmpty() && neighbor.tile.removeExtraNode(neighbor)) {
        it.remove();
        this.nodes.remove(Integer.valueOf(neighbor.uid));
        neighbor.clearGrid();
      } 
    } 
    this.nodes.remove(Integer.valueOf(node.uid));
    node.clearGrid();
    if (node.links.isEmpty()) {
      this.energyNet.grids.remove(this);
    } else if (node.links.size() > 1 && node.nodeType == NodeType.Conductor) {
      List<Set<Node>> nodeTable = new ArrayList<>();
      int i;
      for (i = 0; i < node.links.size(); i++) {
        Node neighbor = ((NodeLink)node.links.get(i)).getNeighbor(node);
        Set<Node> connectedNodes = new HashSet<>();
        Queue<Node> nodesToCheck = new LinkedList<>(Arrays.asList(new Node[] { neighbor }));
        Node cNode;
        while ((cNode = nodesToCheck.poll()) != null) {
          if (connectedNodes.add(cNode) && cNode.nodeType == NodeType.Conductor)
            for (NodeLink link : cNode.links) {
              Node nNode = link.getNeighbor(cNode);
              if (!connectedNodes.contains(nNode))
                nodesToCheck.add(nNode); 
            }  
        } 
        nodeTable.add(connectedNodes);
      } 
      assert nodeTable.size() == node.links.size();
      for (i = 1; i < node.links.size(); i++) {
        if (EnergyNetGlobal.debugGrid)
          IC2.log.debug(LogCategory.EnergyNet, "%d Checking net %d with %d nodes.", new Object[] { Integer.valueOf(this.uid), Integer.valueOf(i), Integer.valueOf(((Set)nodeTable.get(i)).size()) }); 
        Set<Node> connectedNodes = nodeTable.get(i);
        Node neighbor = ((NodeLink)node.links.get(i)).getNeighbor(node);
        assert connectedNodes.contains(neighbor);
        boolean split = true;
        for (int j = 0; j < i; j++) {
          Set<Node> cmpList = nodeTable.get(j);
          if (cmpList.contains(neighbor)) {
            if (EnergyNetGlobal.debugGrid)
              IC2.log.debug(LogCategory.EnergyNet, "%d Same as %d.", new Object[] { Integer.valueOf(this.uid), Integer.valueOf(j) }); 
            split = false;
            break;
          } 
        } 
        if (split) {
          if (EnergyNetGlobal.debugGrid)
            IC2.log.debug(LogCategory.EnergyNet, "%d Moving nodes %s.", new Object[] { Integer.valueOf(this.uid), connectedNodes }); 
          Grid grid = new Grid(this.energyNet);
          for (Node cNode : connectedNodes) {
            boolean needsExtraNode = false;
            if (!cNode.links.isEmpty() && cNode.nodeType != NodeType.Conductor)
              for (int k = 0; k < i; k++) {
                Set<Node> cmpList = nodeTable.get(k);
                if (cmpList.contains(cNode)) {
                  needsExtraNode = true;
                  break;
                } 
              }  
            if (needsExtraNode) {
              if (EnergyNetGlobal.debugGrid)
                IC2.log.debug(LogCategory.EnergyNet, "%s Create extra Node for %s.", new Object[] { Integer.valueOf(this.uid), cNode }); 
              Node extraNode = new Node(this.energyNet, cNode.tile, cNode.nodeType);
              cNode.tile.addExtraNode(extraNode);
              for (Iterator<NodeLink> iterator = cNode.links.iterator(); iterator.hasNext(); ) {
                NodeLink link = iterator.next();
                if (connectedNodes.contains(link.getNeighbor(cNode))) {
                  link.replaceNode(cNode, extraNode);
                  extraNode.links.add(link);
                  iterator.remove();
                } 
              } 
              assert !extraNode.links.isEmpty();
              grid.add(extraNode);
              assert extraNode.getGrid() != null;
              continue;
            } 
            if (EnergyNetGlobal.debugGrid)
              IC2.log.debug(LogCategory.EnergyNet, "%d Move Node %s.", new Object[] { Integer.valueOf(this.uid), cNode }); 
            assert this.nodes.containsKey(Integer.valueOf(cNode.uid));
            this.nodes.remove(Integer.valueOf(cNode.uid));
            cNode.clearGrid();
            grid.add(cNode);
            assert cNode.getGrid() != null;
          } 
        } 
      } 
    } 
  }
  
  void merge(Grid grid, Map<Node, Node> nodeReplacements) {
    if (EnergyNetGlobal.debugGrid)
      IC2.log.debug(LogCategory.EnergyNet, "%d Merge %s -> %s.", new Object[] { Integer.valueOf(this.uid), grid, this }); 
    assert this.energyNet.grids.contains(grid);
    invalidate();
    for (Node node : grid.nodes.values()) {
      boolean found = false;
      if (node.nodeType != NodeType.Conductor)
        for (Node node2 : this.nodes.values()) {
          if (node2.tile == node.tile && node2.nodeType == node.nodeType) {
            if (EnergyNetGlobal.debugGrid)
              IC2.log.debug(LogCategory.EnergyNet, "%d Merge Node %s -> %s.", new Object[] { Integer.valueOf(this.uid), node, node2 }); 
            found = true;
            for (NodeLink link : node.links) {
              link.replaceNode(node, node2);
              node2.links.add(link);
            } 
            node2.tile.removeExtraNode(node);
            nodeReplacements.put(node, node2);
            break;
          } 
        }  
      if (!found) {
        if (EnergyNetGlobal.debugGrid)
          IC2.log.debug(LogCategory.EnergyNet, "%d Add Node %s.", new Object[] { Integer.valueOf(this.uid), node }); 
        node.clearGrid();
        add(node);
        assert node.getGrid() != null;
      } 
    } 
    if (EnergyNetGlobal.debugGrid)
      IC2.log.debug(LogCategory.EnergyNet, "Remove %s.", new Object[] { grid }); 
    this.energyNet.grids.remove(grid);
  }
  
  void prepareCalculation() {
    assert this.calculation == null;
    if (!this.activeSources.isEmpty())
      this.activeSources.clear(); 
    if (!this.activeSinks.isEmpty())
      this.activeSinks.clear(); 
    List<Node> dynamicTierNodes = new ArrayList<>();
    int maxSourceTier = 0;
    for (Node node : this.nodes.values()) {
      IEnergySource source;
      IEnergySink sink;
      assert node.getGrid() == this;
      switch (node.nodeType) {
        case Source:
          source = (IEnergySource)node.tile.mainTile;
          node.setTier(source.getSourceTier());
          node.setAmount(source.getOfferedEnergy());
          if (node.getAmount() > 0.0D) {
            this.activeSources.add(Integer.valueOf(node.uid));
            maxSourceTier = Math.max(node.getTier(), maxSourceTier);
            break;
          } 
          node.setAmount(0.0D);
          break;
        case Sink:
          sink = (IEnergySink)node.tile.mainTile;
          node.setTier(sink.getSinkTier());
          node.setAmount(sink.getDemandedEnergy());
          if (node.getAmount() > 0.0D) {
            this.activeSinks.add(Integer.valueOf(node.uid));
            if (node.getTier() == Integer.MAX_VALUE)
              dynamicTierNodes.add(node); 
            break;
          } 
          node.setAmount(0.0D);
          break;
        case Conductor:
          node.setAmount(0.0D);
          break;
      } 
      assert node.getAmount() >= 0.0D;
    } 
    for (Node node : dynamicTierNodes)
      node.setTier(maxSourceTier); 
  }
  
  Runnable startCalculation() {
    assert this.calculation == null;
    if (this.failed) {
      IC2.log.warn(LogCategory.EnergyNet, "Calculation failed previously, skipping calculation.");
      return null;
    } 
    boolean run = this.hasNonZeroVoltages;
    if (!this.activeSinks.isEmpty() && !this.activeSources.isEmpty()) {
      run = true;
      for (Iterator<Integer> iterator = this.activeSources.iterator(); iterator.hasNext(); ) {
        int nodeId = ((Integer)iterator.next()).intValue();
        Node node = this.nodes.get(Integer.valueOf(nodeId));
        int shareCount = 1;
        for (Node shared : node.tile.nodes) {
          if (shared.uid != nodeId && shared.nodeType == NodeType.Source && !(shared.getGrid()).activeSinks.isEmpty()) {
            assert (shared.getGrid()).activeSources.contains(Integer.valueOf(shared.uid));
            assert shared.getGrid() != this;
            shareCount++;
          } 
        } 
        node.setAmount(node.getAmount() / shareCount);
        IEnergySource source = (IEnergySource)node.tile.mainTile;
        source.drawEnergy(node.getAmount());
        if (EnergyNetGlobal.debugGrid)
          IC2.log.debug(LogCategory.EnergyNet, "%d %s %f EU", new Object[] { Integer.valueOf(this.uid), node, Double.valueOf(-node.getAmount()) }); 
      } 
    } 
    if (run) {
      RunnableFuture<Iterable<Node>> task = (IC2.getInstance()).threadPool.makeTask(new GridCalculation(this));
      this.calculation = task;
      return task;
    } 
    return null;
  }
  
  void finishCalculation() {
    if (this.calculation == null)
      return; 
    try {
      Iterable<Node> result = this.calculation.get();
      for (Node node : result) {
        EnumFacing dir;
        if (!node.links.isEmpty()) {
          dir = ((NodeLink)node.links.get(0)).getDirFrom(node);
        } else {
          dir = null;
          if (EnergyNetGlobal.debugGrid) {
            IC2.log.warn(LogCategory.EnergyNet, "Can't determine direction for %s.", new Object[] { node });
            dumpNodeInfo(IC2.log.getPrintStream(LogCategory.EnergyNet, Level.DEBUG), false, node);
            dumpGraph(false);
          } 
        } 
        this.energyNet.addChange(node, dir, node.getAmount(), node.getVoltage());
      } 
    } catch (InterruptedException e) {
      IC2.log.debug(LogCategory.EnergyNet, e, "Calculation interrupted.");
    } catch (ExecutionException e) {
      IC2.log.warn(LogCategory.EnergyNet, e, "Calculation failed.");
      PrintStream ps = IC2.log.getPrintStream(LogCategory.EnergyNet, Level.WARN);
      dumpStats(ps, false);
      dumpMatrix(ps, false, true, true);
      dumpGraph(false);
      this.failed = true;
    } 
    this.calculation = null;
  }
  
  void updateStats() {
    if (this.lastVoltagesNeedUpdate) {
      this.lastVoltagesNeedUpdate = false;
      for (Node node : this.nodes.values())
        node.updateStats(); 
    } 
  }
  
  Iterable<Node> calculate() {
    this.lastVoltagesNeedUpdate = true;
    if (this.activeSources.isEmpty() || this.activeSinks.isEmpty()) {
      for (Node node : this.nodes.values()) {
        node.setVoltage(0.0D);
        node.resetCurrents();
      } 
      if (!this.activeSources.isEmpty())
        this.activeSources.clear(); 
      if (!this.activeSinks.isEmpty())
        this.activeSinks.clear(); 
      this.hasNonZeroVoltages = false;
      return new ArrayList<>();
    } 
    StructureCache.Data data = calculateDistribution();
    calculateEffects(data);
    this.activeSources.clear();
    this.activeSinks.clear();
    List<Node> ret = new ArrayList<>();
    for (Node node : data.activeNodes) {
      if (node.nodeType == NodeType.Sink || node.nodeType == NodeType.Source)
        ret.add(node.getTop()); 
    } 
    this.hasNonZeroVoltages = true;
    return ret;
  }
  
  private void add(Node node) {
    node.setGrid(this);
    Node prev = this.nodes.put(Integer.valueOf(node.uid), node);
    if (prev != null)
      throw new IllegalStateException("duplicate node uid, new " + node + ", old " + prev); 
  }
  
  private void invalidate() {
    finishCalculation();
    this.cache.clear();
  }
  
  private StructureCache.Data calculateDistribution() {
    long time = System.nanoTime();
    StructureCache.Data data = this.cache.get(this.activeSources, this.activeSinks);
    this.lastData = data;
    if (!data.isInitialized) {
      copyForOptimize(data);
      optimize(data);
      determineEmittingNodes(data);
      int size = data.activeNodes.size();
      data.networkMatrix = new DenseMatrix64F(size, size);
      data.sourceMatrix = new DenseMatrix64F(size, 1);
      data.resultMatrix = new DenseMatrix64F(size, 1);
      data.solver = LinearSolverFactory.symmPosDef(size);
      if (!EnergyNetLocal.useLinearTransferModel) {
        populateNetworkMatrix(data);
        initializeSolver(data);
        if (data.solver instanceof ic2.shades.org.ejml.alg.dense.linsol.LinearSolver_B64_to_D64)
          data.networkMatrix = null; 
      } 
      data.isInitialized = true;
    } 
    if (EnergyNetLocal.useLinearTransferModel) {
      populateNetworkMatrix(data);
      initializeSolver(data);
    } 
    populateSourceMatrix(data);
    if (EnergyNetGlobal.debugGridVerbose)
      dumpMatrix(IC2.log.getPrintStream(LogCategory.EnergyNet, Level.TRACE), false, true, false); 
    data.solver.solve((Matrix64F)data.sourceMatrix, (Matrix64F)data.resultMatrix);
    assert !data.solver.modifiesB();
    if (EnergyNetGlobal.debugGridVerbose)
      dumpMatrix(IC2.log.getPrintStream(LogCategory.EnergyNet, Level.TRACE), false, false, true); 
    if (EnergyNetGlobal.debugGrid) {
      time = System.nanoTime() - time;
      IC2.log.debug(LogCategory.EnergyNet, "%d The distribution calculation took %d us.", new Object[] { Integer.valueOf(this.uid), Long.valueOf(time / 1000L) });
    } 
    return data;
  }
  
  private static void initializeSolver(StructureCache.Data data) {
    if (!data.solver.setA((Matrix64F)data.networkMatrix)) {
      int size = data.networkMatrix.numCols;
      if (data.solver.modifiesA())
        populateNetworkMatrix(data); 
      data.solver = LinearSolverFactory.linear(size);
      if (!data.solver.setA((Matrix64F)data.networkMatrix)) {
        if (data.solver.modifiesA())
          populateNetworkMatrix(data); 
        EigenDecomposition<DenseMatrix64F> ed = DecompositionFactory.eig(size, false);
        if (ed.decompose((Matrix64F)data.networkMatrix)) {
          int complex = size;
          int nonPositive = size;
          StringBuilder sb = new StringBuilder("Eigen values: ");
          for (int i = 0; i < size; i++) {
            Complex64F ev = ed.getEigenvalue(i);
            if (ev.isReal())
              complex--; 
            if (ev.real > 0.0D)
              nonPositive--; 
            if (i != 0)
              sb.append(", "); 
            sb.append(ev);
          } 
          IC2.log.info(LogCategory.EnergyNet, sb.toString());
          IC2.log.info(LogCategory.EnergyNet, "Total: %d, complex: %d, non positive: %d", new Object[] { Integer.valueOf(size), Integer.valueOf(complex), Integer.valueOf(nonPositive) });
        } else {
          IC2.log.info(LogCategory.EnergyNet, "Unable to compute the eigen values.");
        } 
        if (ed.inputModified())
          populateNetworkMatrix(data); 
        throw new RuntimeException("Can't decompose network matrix.");
      } 
    } 
  }
  
  private void calculateEffects(StructureCache.Data data) {
    long time = System.nanoTime();
    for (Node node : this.nodes.values()) {
      node.setVoltage(Double.NaN);
      node.resetCurrents();
    } 
    for (int row = 0; row < data.activeNodes.size(); row++) {
      double current;
      Node node = data.activeNodes.get(row);
      node.setVoltage(data.resultMatrix.get(row));
      switch (node.nodeType) {
        case Source:
          if (EnergyNetLocal.useLinearTransferModel) {
            current = data.sourceMatrix.get(row) - node.getVoltage() / node.getResistance();
            double actualAmount = current * node.getVoltage();
            assert actualAmount >= 0.0D : actualAmount + " (u=" + node.getVoltage() + ")";
            assert actualAmount <= node.getAmount() : actualAmount + " <= " + node.getAmount() + " (u=" + node.getVoltage() + ")";
            node.setAmount(actualAmount - node.getAmount());
          } else {
            current = node.getAmount();
            node.setAmount(0.0D);
          } 
          assert node.getAmount() <= 0.0D;
          if (EnergyNetGlobal.debugGrid)
            IC2.log.debug(LogCategory.EnergyNet, "%d %s %f EU, %f V, %f A.", new Object[] { Integer.valueOf(this.uid), node, Double.valueOf(-node.getAmount()), Double.valueOf(node.getVoltage()), Double.valueOf(-current) }); 
          break;
        case Sink:
          if (EnergyNetLocal.useLinearTransferModel) {
            current = node.getVoltage() / node.getResistance();
            node.setAmount(node.getVoltage() * current);
          } else {
            current = node.getVoltage();
            node.setAmount(current);
          } 
          assert node.getAmount() >= 0.0D;
          if (EnergyNetGlobal.debugGrid)
            IC2.log.debug(LogCategory.EnergyNet, "%d %s %f EU, %f V, %f A.", new Object[] { Integer.valueOf(this.uid), node, Double.valueOf(node.getAmount()), Double.valueOf(node.getVoltage()), Double.valueOf(current) }); 
          break;
      } 
    } 
    Set<NodeLink> visitedLinks = EnergyNetGlobal.verifyGrid() ? new HashSet<>() : null;
    for (Node node : data.activeNodes) {
      for (NodeLink link : node.links) {
        if (link.nodeA != node)
          continue; 
        Node nodeA = link.nodeA.getTop();
        Node nodeB = link.nodeB.getTop();
        double totalLoss = link.loss;
        for (Node skipped : link.skippedNodes) {
          assert skipped.nodeType == NodeType.Conductor;
          skipped = skipped.getTop();
          if (!Double.isNaN(skipped.getVoltage())) {
            assert false;
            break;
          } 
          NodeLink link2 = nodeA.getConnectionTo(skipped);
          assert link2 != null;
          if (EnergyNetGlobal.verifyGrid() && !$assertionsDisabled && !visitedLinks.add(link2))
            throw new AssertionError(); 
          skipped.setVoltage(Util.lerp(nodeA.getVoltage(), nodeB.getVoltage(), link2.loss / totalLoss));
          link2.updateCurrent();
          nodeA = skipped;
          totalLoss -= link2.loss;
        } 
        nodeA.getConnectionTo(nodeB).updateCurrent();
      } 
    } 
    time = System.nanoTime() - time;
    if (EnergyNetGlobal.debugGrid)
      IC2.log.debug(LogCategory.EnergyNet, "%d The effect calculation took %d us.", new Object[] { Integer.valueOf(this.uid), Long.valueOf(time / 1000L) }); 
  }
  
  private void copyForOptimize(StructureCache.Data data) {
    data.optimizedNodes = new HashMap<>();
    for (Node node : this.nodes.values()) {
      assert !node.links.isEmpty();
      if (node.getAmount() > 0.0D || node.nodeType == NodeType.Conductor) {
        assert node.nodeType != NodeType.Sink || this.activeSinks.contains(Integer.valueOf(node.uid));
        assert node.nodeType != NodeType.Source || this.activeSources.contains(Integer.valueOf(node.uid));
        assert node.getGrid() != null;
        data.optimizedNodes.put(Integer.valueOf(node.uid), new Node(node));
      } 
    } 
    for (Node node : data.optimizedNodes.values()) {
      assert !node.links.isEmpty();
      assert node.getGrid() == this;
      for (ListIterator<NodeLink> it = node.links.listIterator(); it.hasNext(); ) {
        NodeLink link = it.next();
        Node neighbor = link.getNeighbor(node.uid);
        assert neighbor.getGrid() == this;
        if ((neighbor.nodeType == NodeType.Sink || neighbor.nodeType == NodeType.Source) && neighbor.getAmount() <= 0.0D) {
          it.remove();
          continue;
        } 
        if (link.nodeA.uid == node.uid) {
          link.nodeA = data.optimizedNodes.get(Integer.valueOf(link.nodeA.uid));
          link.nodeB = data.optimizedNodes.get(Integer.valueOf(link.nodeB.uid));
          assert link.nodeA != null && link.nodeB != null;
          List<Node> newSkippedNodes = new ArrayList<>();
          for (Node skippedNode : link.skippedNodes)
            newSkippedNodes.add(data.optimizedNodes.get(Integer.valueOf(skippedNode.uid))); 
          link.skippedNodes = newSkippedNodes;
          continue;
        } 
        assert link.nodeB.uid == node.uid;
        boolean foundReverseLink = false;
        for (NodeLink reverseLink : ((Node)data.optimizedNodes.get(Integer.valueOf(link.nodeA.uid))).links) {
          assert reverseLink.nodeA.uid != node.uid;
          if (reverseLink.nodeB.uid == node.uid && !node.links.contains(reverseLink)) {
            assert reverseLink.nodeA.uid == link.nodeA.uid;
            foundReverseLink = true;
            it.set(reverseLink);
            break;
          } 
        } 
        assert foundReverseLink;
      } 
    } 
    if (EnergyNetGlobal.verifyGrid()) {
      for (Node node : data.optimizedNodes.values()) {
        assert !node.links.isEmpty();
        for (NodeLink link : node.links) {
          if (!data.optimizedNodes.containsValue(link.nodeA))
            IC2.log.debug(LogCategory.EnergyNet, "%d Link %s is broken.", new Object[] { Integer.valueOf(this.uid), link }); 
          assert data.optimizedNodes.containsValue(link.nodeA);
          assert data.optimizedNodes.containsValue(link.nodeB);
          assert link.nodeA != link.nodeB;
          assert (link.getNeighbor(node)).links.contains(link);
        } 
      } 
      Iterator<Integer> iterator;
      for (iterator = this.activeSources.iterator(); iterator.hasNext(); ) {
        int uid = ((Integer)iterator.next()).intValue();
        assert data.optimizedNodes.containsKey(Integer.valueOf(uid));
      } 
      for (iterator = this.activeSinks.iterator(); iterator.hasNext(); ) {
        int uid = ((Integer)iterator.next()).intValue();
        assert data.optimizedNodes.containsKey(Integer.valueOf(uid));
      } 
    } 
  }
  
  private void optimize(StructureCache.Data data) {
    int removed;
    do {
      removed = 0;
      for (Iterator<Node> it = data.optimizedNodes.values().iterator(); it.hasNext(); ) {
        Node node = it.next();
        if (node.nodeType == NodeType.Conductor) {
          if (node.links.size() < 2) {
            it.remove();
            removed++;
            for (NodeLink link : node.links) {
              boolean found = false;
              for (Iterator<NodeLink> it2 = (link.getNeighbor(node)).links.iterator(); it2.hasNext();) {
                if (it2.next() == link) {
                  found = true;
                  it2.remove();
                  break;
                } 
              } 
              assert found;
            } 
            continue;
          } 
          if (node.links.size() == 2) {
            it.remove();
            removed++;
            NodeLink linkA = node.links.get(0);
            NodeLink linkB = node.links.get(1);
            Node neighborA = linkA.getNeighbor(node);
            Node neighborB = linkB.getNeighbor(node);
            if (neighborA == neighborB) {
              neighborA.links.remove(linkA);
              neighborB.links.remove(linkB);
              continue;
            } 
            linkA.loss += linkB.loss;
            if (linkA.nodeA == node) {
              linkA.nodeA = neighborB;
              linkA.dirFromA = linkB.getDirFrom(neighborB);
              if (linkB.nodeA == node) {
                assert linkB.nodeB == neighborB;
                Collections.reverse(linkB.skippedNodes);
              } else {
                assert linkB.nodeB == node && linkB.nodeA == neighborB;
              } 
              linkB.skippedNodes.add(node);
              linkB.skippedNodes.addAll(linkA.skippedNodes);
              linkA.skippedNodes = linkB.skippedNodes;
            } else {
              linkA.nodeB = neighborB;
              linkA.dirFromB = linkB.getDirFrom(neighborB);
              if (linkB.nodeB == node) {
                assert linkB.nodeA == neighborB;
                Collections.reverse(linkB.skippedNodes);
              } else {
                assert linkB.nodeA == node && linkB.nodeB == neighborB;
              } 
              linkA.skippedNodes.add(node);
              linkA.skippedNodes.addAll(linkB.skippedNodes);
            } 
            assert linkA.nodeA != linkA.nodeB;
            assert linkA.nodeA == neighborA || linkA.nodeB == neighborA;
            assert linkA.nodeA == neighborB || linkA.nodeB == neighborB;
            boolean found = false;
            for (ListIterator<NodeLink> it2 = neighborB.links.listIterator(); it2.hasNext();) {
              if (it2.next() == linkB) {
                found = true;
                it2.set(linkA);
                break;
              } 
            } 
            assert found;
          } 
        } 
      } 
    } while (removed > 0);
    if (EnergyNetGlobal.verifyGrid()) {
      for (Node node : data.optimizedNodes.values()) {
        assert !node.links.isEmpty();
        for (NodeLink link : node.links) {
          List<Node> skippedNodes;
          if (!data.optimizedNodes.containsValue(link.nodeA))
            IC2.log.debug(LogCategory.EnergyNet, "%d Link %s is broken.", new Object[] { Integer.valueOf(this.uid), link }); 
          assert data.optimizedNodes.containsValue(link.nodeA);
          assert data.optimizedNodes.containsValue(link.nodeB);
          assert !this.nodes.containsValue(link.nodeA);
          assert !this.nodes.containsValue(link.nodeB);
          assert this.nodes.containsValue(link.nodeA.getTop());
          assert this.nodes.containsValue(link.nodeB.getTop());
          assert link.nodeA != link.nodeB;
          assert link.nodeA == node || link.nodeB == node;
          assert (link.getNeighbor(node)).links.contains(link);
          assert !link.skippedNodes.contains(link.nodeA);
          assert !link.skippedNodes.contains(link.nodeB);
          assert Collections.disjoint(link.skippedNodes, data.optimizedNodes.values());
          assert Collections.disjoint(link.skippedNodes, this.nodes.values());
          assert (new HashSet(link.skippedNodes)).size() == link.skippedNodes.size();
          Node start = node.getTop();
          if (link.nodeA == node) {
            skippedNodes = link.skippedNodes;
          } else {
            skippedNodes = new ArrayList<>(link.skippedNodes);
            Collections.reverse(skippedNodes);
          } 
          for (Node skipped : skippedNodes) {
            assert start.getConnectionTo(skipped.getTop()) != null : start + " -> " + skipped.getTop() + " not in " + start.links + " (skipped " + skippedNodes + ")";
            start = skipped.getTop();
          } 
          assert start.getConnectionTo(link.getNeighbor(node).getTop()) != null : start + " -> " + link.getNeighbor(node).getTop() + " not in " + start.links;
        } 
      } 
      Iterator<Integer> iterator;
      for (iterator = this.activeSources.iterator(); iterator.hasNext(); ) {
        int uid = ((Integer)iterator.next()).intValue();
        assert data.optimizedNodes.containsKey(Integer.valueOf(uid));
      } 
      for (iterator = this.activeSinks.iterator(); iterator.hasNext(); ) {
        int uid = ((Integer)iterator.next()).intValue();
        assert data.optimizedNodes.containsKey(Integer.valueOf(uid));
      } 
    } 
  }
  
  private static void determineEmittingNodes(StructureCache.Data data) {
    data.activeNodes = new ArrayList<>();
    int index = 0;
    for (Node node : data.optimizedNodes.values()) {
      switch (node.nodeType) {
        case Source:
          if (EnergyNetGlobal.debugGrid)
            IC2.log.debug(LogCategory.EnergyNet, "%d %d %s.", new Object[] { Integer.valueOf((node.getGrid()).uid), Integer.valueOf(index++), node }); 
          data.activeNodes.add(node);
        case Sink:
          if (EnergyNetGlobal.debugGrid)
            IC2.log.debug(LogCategory.EnergyNet, "%d %d %s.", new Object[] { Integer.valueOf((node.getGrid()).uid), Integer.valueOf(index++), node }); 
          data.activeNodes.add(node);
        case Conductor:
          if (EnergyNetGlobal.debugGrid)
            IC2.log.debug(LogCategory.EnergyNet, "%d %d %s.", new Object[] { Integer.valueOf((node.getGrid()).uid), Integer.valueOf(index++), node }); 
          data.activeNodes.add(node);
      } 
    } 
  }
  
  private static void populateNetworkMatrix(StructureCache.Data data) {
    for (int row = 0; row < data.activeNodes.size(); row++) {
      Node node = data.activeNodes.get(row);
      for (int col = 0; col < data.activeNodes.size(); col++) {
        double value = 0.0D;
        if (row == col) {
          for (NodeLink link : node.links) {
            if (link.getNeighbor(node) == node)
              continue; 
            value += 1.0D / link.loss;
            assert link.loss >= 0.0D;
          } 
          if (EnergyNetLocal.useLinearTransferModel) {
            if (node.nodeType == NodeType.Source) {
              double openCircuitVoltage = EnergyNet.instance.getPowerFromTier(node.getTier());
              double resistance = Util.square(openCircuitVoltage) / node.getAmount() * 4.0D;
              assert resistance > 0.0D;
              value += 1.0D / resistance;
              node.setResistance(resistance);
            } else if (node.nodeType == NodeType.Sink) {
              double resistance = EnergyNet.instance.getPowerFromTier(node.getTier());
              assert resistance > 0.0D;
              value += 1.0D / resistance;
              node.setResistance(resistance);
            } 
          } else if (node.nodeType == NodeType.Sink) {
            value++;
          } 
        } else {
          Node possibleNeighbor = data.activeNodes.get(col);
          for (NodeLink link : node.links) {
            Node neighbor = link.getNeighbor(node);
            if (neighbor == node)
              continue; 
            if (neighbor == possibleNeighbor) {
              value -= 1.0D / link.loss;
              assert link.loss >= 0.0D;
            } 
          } 
        } 
        data.networkMatrix.set(row, col, value);
      } 
    } 
  }
  
  private void populateSourceMatrix(StructureCache.Data data) {
    for (int row = 0; row < data.activeNodes.size(); row++) {
      Node node = data.activeNodes.get(row);
      double input = 0.0D;
      if (node.nodeType == NodeType.Source) {
        if (EnergyNetLocal.useLinearTransferModel) {
          double openCircuitVoltage = EnergyNet.instance.getPowerFromTier(node.getTier());
          input = openCircuitVoltage / node.getResistance();
        } else {
          input = node.getAmount();
        } 
        assert input > 0.0D;
      } 
      data.sourceMatrix.set(row, 0, input);
    } 
  }
  
  void dumpNodeInfo(PrintStream ps, boolean waitForFinish, Node node) {
    IEnergySink sink;
    IEnergySource source;
    if (waitForFinish)
      finishCalculation(); 
    ps.println("Node " + node + " info:");
    ps.println(" type: " + node.nodeType);
    switch (node.nodeType) {
      case Sink:
        sink = (IEnergySink)node.tile.mainTile;
        ps.println(" demanded: " + sink.getDemandedEnergy());
        ps.println(" tier: " + sink.getSinkTier());
        break;
      case Source:
        source = (IEnergySource)node.tile.mainTile;
        ps.println(" offered: " + source.getOfferedEnergy());
        ps.println(" tier: " + source.getSourceTier());
        break;
    } 
    ps.println(node.links.size() + " neighbor links:");
    for (NodeLink link : node.links)
      ps.println(" " + link.getNeighbor(node) + " " + link.loss + " " + link.skippedNodes); 
    StructureCache.Data data = this.lastData;
    if (data == null || !data.isInitialized || data.optimizedNodes == null) {
      ps.println("No optimized data");
    } else if (!data.optimizedNodes.containsKey(Integer.valueOf(node.uid))) {
      ps.println("Optimized away");
    } else {
      Node optimizedNode = data.optimizedNodes.get(Integer.valueOf(node.uid));
      ps.println(optimizedNode.links.size() + " optimized neighbor links:");
      for (NodeLink link : optimizedNode.links)
        ps.println(" " + link.getNeighbor(optimizedNode) + " " + link.loss + " " + link.skippedNodes); 
    } 
  }
  
  void dumpMatrix(PrintStream ps, boolean waitForFinish, boolean dumpNodesNetSrcMatrices, boolean dumpResultMatrix) {
    if (waitForFinish)
      finishCalculation(); 
    if (dumpNodesNetSrcMatrices)
      ps.println("Dumping matrices for " + this + "."); 
    StructureCache.Data data = this.lastData;
    if (data == null) {
      ps.println("Matrices unavailable");
    } else if (dumpNodesNetSrcMatrices || dumpResultMatrix) {
      if (!data.isInitialized)
        ps.println("Matrices potentially outdated"); 
      if (dumpNodesNetSrcMatrices) {
        ps.println("Emitting node indizes:");
        for (int i = 0; i < data.activeNodes.size(); i++) {
          Node node = data.activeNodes.get(i);
          ps.println(i + " " + node + " (amount=" + node.getAmount() + ", tier=" + node.getTier() + ")");
        } 
        ps.println("Network matrix:");
        printMatrix(data.networkMatrix, ps);
        ps.println("Source matrix:");
        printMatrix(data.sourceMatrix, ps);
      } 
      if (dumpResultMatrix) {
        ps.println("Result matrix:");
        printMatrix(data.resultMatrix, ps);
      } 
    } 
  }
  
  private static void printMatrix(DenseMatrix64F matrix, PrintStream ps) {
    if (matrix == null) {
      ps.println("null");
      return;
    } 
    boolean isZero = true;
    for (int i = 0; i < matrix.numRows; i++) {
      for (int j = 0; j < matrix.numCols; j++) {
        if (matrix.get(i, j) != 0.0D) {
          isZero = false;
          break;
        } 
      } 
    } 
    if (isZero) {
      ps.println(matrix.numRows + "x" + matrix.numCols + ", all zero");
    } else {
      MatrixIO.print(ps, (Matrix64F)matrix, "%.6f");
    } 
  }
  
  void dumpStats(PrintStream ps, boolean waitForFinish) {
    if (waitForFinish)
      finishCalculation(); 
    ps.println("Grid " + this.uid + " info:");
    ps.println(this.nodes.size() + " nodes");
    StructureCache.Data data = this.lastData;
    if (data != null && data.isInitialized) {
      if (data.activeNodes != null) {
        int srcCount = 0;
        int dstCount = 0;
        for (Node node : data.activeNodes) {
          if (node.nodeType == NodeType.Source) {
            srcCount++;
            continue;
          } 
          if (node.nodeType == NodeType.Sink)
            dstCount++; 
        } 
        ps.println("Active: " + srcCount + " sources -> " + dstCount + " sinks");
      } 
      if (data.optimizedNodes != null)
        ps.println(data.optimizedNodes.size() + " nodes after optimization"); 
      if (data.activeNodes != null)
        ps.println(data.activeNodes.size() + " emitting nodes"); 
    } 
    ps.printf("%d entries in cache, hitrate %.2f%%", new Object[] { Integer.valueOf(this.cache.size()), Double.valueOf(100.0D * this.cache.hits / (this.cache.hits + this.cache.misses)) });
    ps.println();
  }
  
  void dumpGraph(boolean waitForFinish) {
    if (waitForFinish)
      finishCalculation(); 
    StructureCache.Data data = this.lastData;
    for (int i = 0; i < 2 && (i != 1 || (data != null && data.isInitialized && data.optimizedNodes != null)); i++)
      FileWriter out = null; 
  }
  
  GridInfo getInfo() {
    int complexNodes = 0;
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int minZ = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    int maxZ = Integer.MIN_VALUE;
    for (Node node : this.nodes.values()) {
      if (node.links.size() > 2)
        complexNodes++; 
      for (IEnergyTile tile : node.tile.subTiles) {
        BlockPos pos = EnergyNet.instance.getPos(tile);
        if (pos.func_177958_n() < minX)
          minX = pos.func_177958_n(); 
        if (pos.func_177956_o() < minY)
          minY = pos.func_177956_o(); 
        if (pos.func_177952_p() < minZ)
          minZ = pos.func_177952_p(); 
        if (pos.func_177958_n() > maxX)
          maxX = pos.func_177958_n(); 
        if (pos.func_177956_o() > maxY)
          maxY = pos.func_177956_o(); 
        if (pos.func_177952_p() > maxZ)
          maxZ = pos.func_177952_p(); 
      } 
    } 
    return new GridInfo(this.uid, this.nodes.size(), complexNodes, minX, minY, minZ, maxX, maxY, maxZ);
  }
}
