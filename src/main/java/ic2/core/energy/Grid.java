// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy;

import net.minecraft.util.math.BlockPos;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.energy.grid.GridInfo;
import java.io.IOException;
import java.io.FileWriter;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.ops.MatrixIO;
import ic2.api.energy.EnergyNet;
import java.util.Collections;
import java.util.ListIterator;
import ic2.core.util.Util;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;
import ic2.shades.org.ejml.factory.DecompositionFactory;
import ic2.shades.org.ejml.alg.dense.linsol.LinearSolver_B64_to_D64;
import ic2.shades.org.ejml.factory.LinearSolverFactory;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.io.PrintStream;
import net.minecraft.util.EnumFacing;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.Level;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.Callable;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.ArrayList;
import ic2.core.energy.grid.NodeType;
import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.Set;
import java.util.Map;

class Grid
{
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
    static final /* synthetic */ boolean $assertionsDisabled;
    
    Grid(final EnergyNetLocal energyNet1) {
        this.nodes = new HashMap<Integer, Node>();
        this.hasNonZeroVoltages = false;
        this.lastVoltagesNeedUpdate = false;
        this.activeSources = new HashSet<Integer>();
        this.activeSinks = new HashSet<Integer>();
        this.cache = new StructureCache();
        this.lastData = null;
        this.uid = EnergyNetLocal.getNextGridUid();
        this.energyNet = energyNet1;
        energyNet1.grids.add(this);
    }
    
    @Override
    public String toString() {
        return "Grid " + this.uid;
    }
    
    void add(final Node node, final Collection<Node> neighbors) {
        if (EnergyNetGlobal.debugGrid) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Add %s to %s neighbors: %s.", this.uid, node, this, neighbors);
        }
        this.invalidate();
        assert !(!neighbors.isEmpty());
        assert !(!node.isExtraNode());
        assert node.links.isEmpty();
        this.add(node);
        for (final Node neighbor : neighbors) {
            assert neighbor != node;
            assert this.nodes.containsKey(neighbor.uid);
            final double loss = (node.getInnerLoss() + neighbor.getInnerLoss()) / 2.0;
            final NodeLink link = new NodeLink(node, neighbor, loss);
            node.links.add(link);
            neighbor.links.add(link);
        }
    }
    
    void remove(final Node node) {
        if (EnergyNetGlobal.debugGrid) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Remove Node %s from %s with %d nodes.", this.uid, node, this, this.nodes.size());
        }
        this.invalidate();
        final Iterator<NodeLink> it = node.links.iterator();
        while (it.hasNext()) {
            final NodeLink link = it.next();
            final Node neighbor = link.getNeighbor(node);
            boolean found = false;
            final Iterator<NodeLink> it2 = neighbor.links.iterator();
            while (it2.hasNext()) {
                if (it2.next() == link) {
                    it2.remove();
                    found = true;
                    break;
                }
            }
            assert found;
            if (!neighbor.links.isEmpty() || !neighbor.tile.removeExtraNode(neighbor)) {
                continue;
            }
            it.remove();
            this.nodes.remove(neighbor.uid);
            neighbor.clearGrid();
        }
        this.nodes.remove(node.uid);
        node.clearGrid();
        if (node.links.isEmpty()) {
            this.energyNet.grids.remove(this);
        }
        else if (node.links.size() > 1 && node.nodeType == NodeType.Conductor) {
            final List<Set<Node>> nodeTable = new ArrayList<Set<Node>>();
            for (int i = 0; i < node.links.size(); ++i) {
                final Node neighbor = node.links.get(i).getNeighbor(node);
                final Set<Node> connectedNodes = new HashSet<Node>();
                final Queue<Node> nodesToCheck = new LinkedList<Node>(Arrays.asList(neighbor));
                Node cNode;
                while ((cNode = nodesToCheck.poll()) != null) {
                    if (connectedNodes.add(cNode) && cNode.nodeType == NodeType.Conductor) {
                        for (final NodeLink link2 : cNode.links) {
                            final Node nNode = link2.getNeighbor(cNode);
                            if (!connectedNodes.contains(nNode)) {
                                nodesToCheck.add(nNode);
                            }
                        }
                    }
                }
                nodeTable.add(connectedNodes);
            }
            assert nodeTable.size() == node.links.size();
            for (int i = 1; i < node.links.size(); ++i) {
                if (EnergyNetGlobal.debugGrid) {
                    IC2.log.debug(LogCategory.EnergyNet, "%d Checking net %d with %d nodes.", this.uid, i, nodeTable.get(i).size());
                }
                final Set<Node> connectedNodes2 = nodeTable.get(i);
                final Node neighbor2 = node.links.get(i).getNeighbor(node);
                assert connectedNodes2.contains(neighbor2);
                boolean split = true;
                for (int j = 0; j < i; ++j) {
                    final Set<Node> cmpList = nodeTable.get(j);
                    if (cmpList.contains(neighbor2)) {
                        if (EnergyNetGlobal.debugGrid) {
                            IC2.log.debug(LogCategory.EnergyNet, "%d Same as %d.", this.uid, j);
                        }
                        split = false;
                        break;
                    }
                }
                if (split) {
                    if (EnergyNetGlobal.debugGrid) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d Moving nodes %s.", this.uid, connectedNodes2);
                    }
                    final Grid grid = new Grid(this.energyNet);
                    for (final Node cNode2 : connectedNodes2) {
                        boolean needsExtraNode = false;
                        if (!cNode2.links.isEmpty() && cNode2.nodeType != NodeType.Conductor) {
                            for (int k = 0; k < i; ++k) {
                                final Set<Node> cmpList2 = nodeTable.get(k);
                                if (cmpList2.contains(cNode2)) {
                                    needsExtraNode = true;
                                    break;
                                }
                            }
                        }
                        if (needsExtraNode) {
                            if (EnergyNetGlobal.debugGrid) {
                                IC2.log.debug(LogCategory.EnergyNet, "%s Create extra Node for %s.", this.uid, cNode2);
                            }
                            final Node extraNode = new Node(this.energyNet, cNode2.tile, cNode2.nodeType);
                            cNode2.tile.addExtraNode(extraNode);
                            final Iterator<NodeLink> it3 = cNode2.links.iterator();
                            while (it3.hasNext()) {
                                final NodeLink link3 = it3.next();
                                if (connectedNodes2.contains(link3.getNeighbor(cNode2))) {
                                    link3.replaceNode(cNode2, extraNode);
                                    extraNode.links.add(link3);
                                    it3.remove();
                                }
                            }
                            assert !extraNode.links.isEmpty();
                            grid.add(extraNode);
                            assert extraNode.getGrid() != null;
                            continue;
                        }
                        else {
                            if (EnergyNetGlobal.debugGrid) {
                                IC2.log.debug(LogCategory.EnergyNet, "%d Move Node %s.", this.uid, cNode2);
                            }
                            assert this.nodes.containsKey(cNode2.uid);
                            this.nodes.remove(cNode2.uid);
                            cNode2.clearGrid();
                            grid.add(cNode2);
                            assert cNode2.getGrid() != null;
                            continue;
                        }
                    }
                }
            }
        }
    }
    
    void merge(final Grid grid, final Map<Node, Node> nodeReplacements) {
        if (EnergyNetGlobal.debugGrid) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Merge %s -> %s.", this.uid, grid, this);
        }
        assert this.energyNet.grids.contains(grid);
        this.invalidate();
        for (final Node node : grid.nodes.values()) {
            boolean found = false;
            if (node.nodeType != NodeType.Conductor) {
                for (final Node node2 : this.nodes.values()) {
                    if (node2.tile == node.tile && node2.nodeType == node.nodeType) {
                        if (EnergyNetGlobal.debugGrid) {
                            IC2.log.debug(LogCategory.EnergyNet, "%d Merge Node %s -> %s.", this.uid, node, node2);
                        }
                        found = true;
                        for (final NodeLink link : node.links) {
                            link.replaceNode(node, node2);
                            node2.links.add(link);
                        }
                        node2.tile.removeExtraNode(node);
                        nodeReplacements.put(node, node2);
                        break;
                    }
                }
            }
            if (!found) {
                if (EnergyNetGlobal.debugGrid) {
                    IC2.log.debug(LogCategory.EnergyNet, "%d Add Node %s.", this.uid, node);
                }
                node.clearGrid();
                this.add(node);
                assert node.getGrid() != null;
                continue;
            }
        }
        if (EnergyNetGlobal.debugGrid) {
            IC2.log.debug(LogCategory.EnergyNet, "Remove %s.", grid);
        }
        this.energyNet.grids.remove(grid);
    }
    
    void prepareCalculation() {
        assert this.calculation == null;
        if (!this.activeSources.isEmpty()) {
            this.activeSources.clear();
        }
        if (!this.activeSinks.isEmpty()) {
            this.activeSinks.clear();
        }
        final List<Node> dynamicTierNodes = new ArrayList<Node>();
        int maxSourceTier = 0;
        for (final Node node : this.nodes.values()) {
            assert node.getGrid() == this;
            switch (node.nodeType) {
                case Source: {
                    final IEnergySource source = (IEnergySource)node.tile.mainTile;
                    node.setTier(source.getSourceTier());
                    node.setAmount(source.getOfferedEnergy());
                    if (node.getAmount() > 0.0) {
                        this.activeSources.add(node.uid);
                        maxSourceTier = Math.max(node.getTier(), maxSourceTier);
                        break;
                    }
                    node.setAmount(0.0);
                    break;
                }
                case Sink: {
                    final IEnergySink sink = (IEnergySink)node.tile.mainTile;
                    node.setTier(sink.getSinkTier());
                    node.setAmount(sink.getDemandedEnergy());
                    if (node.getAmount() <= 0.0) {
                        node.setAmount(0.0);
                        break;
                    }
                    this.activeSinks.add(node.uid);
                    if (node.getTier() == Integer.MAX_VALUE) {
                        dynamicTierNodes.add(node);
                        break;
                    }
                    break;
                }
                case Conductor: {
                    node.setAmount(0.0);
                    break;
                }
            }
            assert node.getAmount() >= 0.0;
        }
        for (final Node node : dynamicTierNodes) {
            node.setTier(maxSourceTier);
        }
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
            for (final int nodeId : this.activeSources) {
                final Node node = this.nodes.get(nodeId);
                int shareCount = 1;
                for (final Node shared : node.tile.nodes) {
                    if (shared.uid != nodeId && shared.nodeType == NodeType.Source && !shared.getGrid().activeSinks.isEmpty()) {
                        assert shared.getGrid().activeSources.contains(shared.uid);
                        assert shared.getGrid() != this;
                        ++shareCount;
                    }
                }
                node.setAmount(node.getAmount() / shareCount);
                final IEnergySource source = (IEnergySource)node.tile.mainTile;
                source.drawEnergy(node.getAmount());
                if (EnergyNetGlobal.debugGrid) {
                    IC2.log.debug(LogCategory.EnergyNet, "%d %s %f EU", this.uid, node, -node.getAmount());
                }
            }
        }
        if (run) {
            final RunnableFuture<Iterable<Node>> task = IC2.getInstance().threadPool.makeTask((Callable<Iterable<Node>>)new GridCalculation(this));
            return (Runnable)(this.calculation = task);
        }
        return null;
    }
    
    void finishCalculation() {
        if (this.calculation == null) {
            return;
        }
        try {
            final Iterable<Node> result = this.calculation.get();
            for (final Node node : result) {
                EnumFacing dir;
                if (!node.links.isEmpty()) {
                    dir = node.links.get(0).getDirFrom(node);
                }
                else {
                    dir = null;
                    if (EnergyNetGlobal.debugGrid) {
                        IC2.log.warn(LogCategory.EnergyNet, "Can't determine direction for %s.", node);
                        this.dumpNodeInfo(IC2.log.getPrintStream(LogCategory.EnergyNet, Level.DEBUG), false, node);
                        this.dumpGraph(false);
                    }
                }
                this.energyNet.addChange(node, dir, node.getAmount(), node.getVoltage());
            }
        }
        catch (final InterruptedException e) {
            IC2.log.debug(LogCategory.EnergyNet, e, "Calculation interrupted.");
        }
        catch (final ExecutionException e2) {
            IC2.log.warn(LogCategory.EnergyNet, e2, "Calculation failed.");
            final PrintStream ps = IC2.log.getPrintStream(LogCategory.EnergyNet, Level.WARN);
            this.dumpStats(ps, false);
            this.dumpMatrix(ps, false, true, true);
            this.dumpGraph(false);
            this.failed = true;
        }
        this.calculation = null;
    }
    
    void updateStats() {
        if (this.lastVoltagesNeedUpdate) {
            this.lastVoltagesNeedUpdate = false;
            for (final Node node : this.nodes.values()) {
                node.updateStats();
            }
        }
    }
    
    Iterable<Node> calculate() {
        this.lastVoltagesNeedUpdate = true;
        if (this.activeSources.isEmpty() || this.activeSinks.isEmpty()) {
            for (final Node node : this.nodes.values()) {
                node.setVoltage(0.0);
                node.resetCurrents();
            }
            if (!this.activeSources.isEmpty()) {
                this.activeSources.clear();
            }
            if (!this.activeSinks.isEmpty()) {
                this.activeSinks.clear();
            }
            this.hasNonZeroVoltages = false;
            return new ArrayList<Node>();
        }
        final StructureCache.Data data = this.calculateDistribution();
        this.calculateEffects(data);
        this.activeSources.clear();
        this.activeSinks.clear();
        final List<Node> ret = new ArrayList<Node>();
        for (final Node node2 : data.activeNodes) {
            if (node2.nodeType == NodeType.Sink || node2.nodeType == NodeType.Source) {
                ret.add(node2.getTop());
            }
        }
        this.hasNonZeroVoltages = true;
        return ret;
    }
    
    private void add(final Node node) {
        node.setGrid(this);
        final Node prev = this.nodes.put(node.uid, node);
        if (prev != null) {
            throw new IllegalStateException("duplicate node uid, new " + node + ", old " + prev);
        }
    }
    
    private void invalidate() {
        this.finishCalculation();
        this.cache.clear();
    }
    
    private StructureCache.Data calculateDistribution() {
        long time = System.nanoTime();
        final StructureCache.Data data = this.cache.get(this.activeSources, this.activeSinks);
        this.lastData = data;
        if (!data.isInitialized) {
            this.copyForOptimize(data);
            this.optimize(data);
            determineEmittingNodes(data);
            final int size = data.activeNodes.size();
            data.networkMatrix = new DenseMatrix64F(size, size);
            data.sourceMatrix = new DenseMatrix64F(size, 1);
            data.resultMatrix = new DenseMatrix64F(size, 1);
            data.solver = LinearSolverFactory.symmPosDef(size);
            if (!EnergyNetLocal.useLinearTransferModel) {
                populateNetworkMatrix(data);
                initializeSolver(data);
                if (data.solver instanceof LinearSolver_B64_to_D64) {
                    data.networkMatrix = null;
                }
            }
            data.isInitialized = true;
        }
        if (EnergyNetLocal.useLinearTransferModel) {
            populateNetworkMatrix(data);
            initializeSolver(data);
        }
        this.populateSourceMatrix(data);
        if (EnergyNetGlobal.debugGridVerbose) {
            this.dumpMatrix(IC2.log.getPrintStream(LogCategory.EnergyNet, Level.TRACE), false, true, false);
        }
        data.solver.solve(data.sourceMatrix, data.resultMatrix);
        assert !data.solver.modifiesB();
        if (EnergyNetGlobal.debugGridVerbose) {
            this.dumpMatrix(IC2.log.getPrintStream(LogCategory.EnergyNet, Level.TRACE), false, false, true);
        }
        if (EnergyNetGlobal.debugGrid) {
            time = System.nanoTime() - time;
            IC2.log.debug(LogCategory.EnergyNet, "%d The distribution calculation took %d us.", this.uid, time / 1000L);
        }
        return data;
    }
    
    private static void initializeSolver(final StructureCache.Data data) {
        if (!data.solver.setA(data.networkMatrix)) {
            final int size = data.networkMatrix.numCols;
            if (data.solver.modifiesA()) {
                populateNetworkMatrix(data);
            }
            data.solver = LinearSolverFactory.linear(size);
            if (!data.solver.setA(data.networkMatrix)) {
                if (data.solver.modifiesA()) {
                    populateNetworkMatrix(data);
                }
                final EigenDecomposition<DenseMatrix64F> ed = DecompositionFactory.eig(size, false);
                if (ed.decompose(data.networkMatrix)) {
                    int complex = size;
                    int nonPositive = size;
                    final StringBuilder sb = new StringBuilder("Eigen values: ");
                    for (int i = 0; i < size; ++i) {
                        final Complex64F ev = ed.getEigenvalue(i);
                        if (ev.isReal()) {
                            --complex;
                        }
                        if (ev.real > 0.0) {
                            --nonPositive;
                        }
                        if (i != 0) {
                            sb.append(", ");
                        }
                        sb.append(ev);
                    }
                    IC2.log.info(LogCategory.EnergyNet, sb.toString());
                    IC2.log.info(LogCategory.EnergyNet, "Total: %d, complex: %d, non positive: %d", size, complex, nonPositive);
                }
                else {
                    IC2.log.info(LogCategory.EnergyNet, "Unable to compute the eigen values.");
                }
                if (ed.inputModified()) {
                    populateNetworkMatrix(data);
                }
                throw new RuntimeException("Can't decompose network matrix.");
            }
        }
    }
    
    private void calculateEffects(final StructureCache.Data data) {
        long time = System.nanoTime();
        for (final Node node : this.nodes.values()) {
            node.setVoltage(Double.NaN);
            node.resetCurrents();
        }
        for (int row = 0; row < data.activeNodes.size(); ++row) {
            final Node node = data.activeNodes.get(row);
            node.setVoltage(data.resultMatrix.get(row));
            switch (node.nodeType) {
                case Source: {
                    double current;
                    if (EnergyNetLocal.useLinearTransferModel) {
                        current = data.sourceMatrix.get(row) - node.getVoltage() / node.getResistance();
                        final double actualAmount = current * node.getVoltage();
                        assert actualAmount >= 0.0 : actualAmount + " (u=" + node.getVoltage() + ")";
                        assert actualAmount <= node.getAmount() : actualAmount + " <= " + node.getAmount() + " (u=" + node.getVoltage() + ")";
                        node.setAmount(actualAmount - node.getAmount());
                    }
                    else {
                        current = node.getAmount();
                        node.setAmount(0.0);
                    }
                    assert node.getAmount() <= 0.0;
                    if (EnergyNetGlobal.debugGrid) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d %s %f EU, %f V, %f A.", this.uid, node, -node.getAmount(), node.getVoltage(), -current);
                        break;
                    }
                    break;
                }
                case Sink: {
                    double current;
                    if (EnergyNetLocal.useLinearTransferModel) {
                        current = node.getVoltage() / node.getResistance();
                        node.setAmount(node.getVoltage() * current);
                    }
                    else {
                        current = node.getVoltage();
                        node.setAmount(current);
                    }
                    assert node.getAmount() >= 0.0;
                    if (EnergyNetGlobal.debugGrid) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d %s %f EU, %f V, %f A.", this.uid, node, node.getAmount(), node.getVoltage(), current);
                        break;
                    }
                    break;
                }
            }
        }
        final Set<NodeLink> visitedLinks = EnergyNetGlobal.verifyGrid() ? new HashSet<NodeLink>() : null;
        for (final Node node2 : data.activeNodes) {
            for (final NodeLink link : node2.links) {
                if (link.nodeA != node2) {
                    continue;
                }
                Node nodeA = link.nodeA.getTop();
                final Node nodeB = link.nodeB.getTop();
                double totalLoss = link.loss;
                for (Node skipped : link.skippedNodes) {
                    assert skipped.nodeType == NodeType.Conductor;
                    skipped = skipped.getTop();
                    if (!Double.isNaN(skipped.getVoltage())) {
                        assert false;
                        break;
                    }
                    else {
                        final NodeLink link2 = nodeA.getConnectionTo(skipped);
                        assert link2 != null;
                        if (EnergyNetGlobal.verifyGrid() && !Grid.$assertionsDisabled && !visitedLinks.add(link2)) {
                            throw new AssertionError();
                        }
                        skipped.setVoltage(Util.lerp(nodeA.getVoltage(), nodeB.getVoltage(), link2.loss / totalLoss));
                        link2.updateCurrent();
                        nodeA = skipped;
                        totalLoss -= link2.loss;
                    }
                }
                nodeA.getConnectionTo(nodeB).updateCurrent();
            }
        }
        time = System.nanoTime() - time;
        if (EnergyNetGlobal.debugGrid) {
            IC2.log.debug(LogCategory.EnergyNet, "%d The effect calculation took %d us.", this.uid, time / 1000L);
        }
    }
    
    private void copyForOptimize(final StructureCache.Data data) {
        data.optimizedNodes = new HashMap<Integer, Node>();
        for (final Node node : this.nodes.values()) {
            assert !node.links.isEmpty();
            if (node.getAmount() <= 0.0 && node.nodeType != NodeType.Conductor) {
                continue;
            }
            assert !(!this.activeSinks.contains(node.uid));
            assert !(!this.activeSources.contains(node.uid));
            assert node.getGrid() != null;
            data.optimizedNodes.put(node.uid, new Node(node));
        }
        for (final Node node : data.optimizedNodes.values()) {
            assert !node.links.isEmpty();
            assert node.getGrid() == this;
            final ListIterator<NodeLink> it = node.links.listIterator();
            while (it.hasNext()) {
                final NodeLink link = it.next();
                final Node neighbor = link.getNeighbor(node.uid);
                assert neighbor.getGrid() == this;
                if ((neighbor.nodeType == NodeType.Sink || neighbor.nodeType == NodeType.Source) && neighbor.getAmount() <= 0.0) {
                    it.remove();
                }
                else if (link.nodeA.uid == node.uid) {
                    link.nodeA = data.optimizedNodes.get(link.nodeA.uid);
                    link.nodeB = data.optimizedNodes.get(link.nodeB.uid);
                    assert link.nodeA != null && link.nodeB != null;
                    final List<Node> newSkippedNodes = new ArrayList<Node>();
                    for (final Node skippedNode : link.skippedNodes) {
                        newSkippedNodes.add(data.optimizedNodes.get(skippedNode.uid));
                    }
                    link.skippedNodes = newSkippedNodes;
                }
                else {
                    assert link.nodeB.uid == node.uid;
                    boolean foundReverseLink = false;
                    for (final NodeLink reverseLink : data.optimizedNodes.get(link.nodeA.uid).links) {
                        assert reverseLink.nodeA.uid != node.uid;
                        if (reverseLink.nodeB.uid != node.uid || node.links.contains(reverseLink)) {
                            continue;
                        }
                        assert reverseLink.nodeA.uid == link.nodeA.uid;
                        foundReverseLink = true;
                        it.set(reverseLink);
                        break;
                    }
                    assert foundReverseLink;
                    continue;
                }
            }
        }
        if (EnergyNetGlobal.verifyGrid()) {
            for (final Node node : data.optimizedNodes.values()) {
                assert !node.links.isEmpty();
                for (final NodeLink link : node.links) {
                    if (!data.optimizedNodes.containsValue(link.nodeA)) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d Link %s is broken.", this.uid, link);
                    }
                    assert data.optimizedNodes.containsValue(link.nodeA);
                    assert data.optimizedNodes.containsValue(link.nodeB);
                    assert link.nodeA != link.nodeB;
                    assert link.getNeighbor(node).links.contains(link);
                }
            }
            for (final int uid : this.activeSources) {
                assert data.optimizedNodes.containsKey(uid);
            }
            for (final int uid : this.activeSinks) {
                assert data.optimizedNodes.containsKey(uid);
            }
        }
    }
    
    private void optimize(final StructureCache.Data data) {
        int removed;
        do {
            removed = 0;
            final Iterator<Node> it = data.optimizedNodes.values().iterator();
            while (it.hasNext()) {
                final Node node = it.next();
                if (node.nodeType == NodeType.Conductor) {
                    if (node.links.size() < 2) {
                        it.remove();
                        ++removed;
                        for (final NodeLink link : node.links) {
                            boolean found = false;
                            final Iterator<NodeLink> it2 = link.getNeighbor(node).links.iterator();
                            while (it2.hasNext()) {
                                if (it2.next() == link) {
                                    found = true;
                                    it2.remove();
                                    break;
                                }
                            }
                            assert found;
                        }
                    }
                    else {
                        if (node.links.size() != 2) {
                            continue;
                        }
                        it.remove();
                        ++removed;
                        final NodeLink linkA = node.links.get(0);
                        final NodeLink linkB = node.links.get(1);
                        final Node neighborA = linkA.getNeighbor(node);
                        final Node neighborB = linkB.getNeighbor(node);
                        if (neighborA == neighborB) {
                            neighborA.links.remove(linkA);
                            neighborB.links.remove(linkB);
                        }
                        else {
                            final NodeLink nodeLink = linkA;
                            nodeLink.loss += linkB.loss;
                            if (linkA.nodeA == node) {
                                linkA.nodeA = neighborB;
                                linkA.dirFromA = linkB.getDirFrom(neighborB);
                                if (linkB.nodeA == node) {
                                    assert linkB.nodeB == neighborB;
                                    Collections.reverse(linkB.skippedNodes);
                                }
                                else {
                                    assert linkB.nodeB == node && linkB.nodeA == neighborB;
                                }
                                linkB.skippedNodes.add(node);
                                linkB.skippedNodes.addAll(linkA.skippedNodes);
                                linkA.skippedNodes = linkB.skippedNodes;
                            }
                            else {
                                linkA.nodeB = neighborB;
                                linkA.dirFromB = linkB.getDirFrom(neighborB);
                                if (linkB.nodeB == node) {
                                    assert linkB.nodeA == neighborB;
                                    Collections.reverse(linkB.skippedNodes);
                                }
                                else {
                                    assert linkB.nodeA == node && linkB.nodeB == neighborB;
                                }
                                linkA.skippedNodes.add(node);
                                linkA.skippedNodes.addAll(linkB.skippedNodes);
                            }
                            assert linkA.nodeA != linkA.nodeB;
                            assert linkA.nodeB == neighborA;
                            assert linkA.nodeB == neighborB;
                            boolean found2 = false;
                            final ListIterator<NodeLink> it3 = neighborB.links.listIterator();
                            while (it3.hasNext()) {
                                if (it3.next() == linkB) {
                                    found2 = true;
                                    it3.set(linkA);
                                    break;
                                }
                            }
                            assert found2;
                            continue;
                        }
                    }
                }
            }
        } while (removed > 0);
        if (EnergyNetGlobal.verifyGrid()) {
            for (final Node node : data.optimizedNodes.values()) {
                assert !node.links.isEmpty();
                for (final NodeLink link : node.links) {
                    if (!data.optimizedNodes.containsValue(link.nodeA)) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d Link %s is broken.", this.uid, link);
                    }
                    assert data.optimizedNodes.containsValue(link.nodeA);
                    assert data.optimizedNodes.containsValue(link.nodeB);
                    assert !this.nodes.containsValue(link.nodeA);
                    assert !this.nodes.containsValue(link.nodeB);
                    assert this.nodes.containsValue(link.nodeA.getTop());
                    assert this.nodes.containsValue(link.nodeB.getTop());
                    assert link.nodeA != link.nodeB;
                    assert link.nodeB == node;
                    assert link.getNeighbor(node).links.contains(link);
                    assert !link.skippedNodes.contains(link.nodeA);
                    assert !link.skippedNodes.contains(link.nodeB);
                    assert Collections.disjoint(link.skippedNodes, data.optimizedNodes.values());
                    assert Collections.disjoint(link.skippedNodes, this.nodes.values());
                    assert new HashSet(link.skippedNodes).size() == link.skippedNodes.size();
                    Node start = node.getTop();
                    List<Node> skippedNodes;
                    if (link.nodeA == node) {
                        skippedNodes = link.skippedNodes;
                    }
                    else {
                        skippedNodes = new ArrayList<Node>(link.skippedNodes);
                        Collections.reverse(skippedNodes);
                    }
                    for (final Node skipped : skippedNodes) {
                        assert start.getConnectionTo(skipped.getTop()) != null : start + " -> " + skipped.getTop() + " not in " + start.links + " (skipped " + skippedNodes + ")";
                        start = skipped.getTop();
                    }
                    assert start.getConnectionTo(link.getNeighbor(node).getTop()) != null : start + " -> " + link.getNeighbor(node).getTop() + " not in " + start.links;
                }
            }
            for (final int uid : this.activeSources) {
                assert data.optimizedNodes.containsKey(uid);
            }
            for (final int uid : this.activeSinks) {
                assert data.optimizedNodes.containsKey(uid);
            }
        }
    }
    
    private static void determineEmittingNodes(final StructureCache.Data data) {
        data.activeNodes = new ArrayList<Node>();
        int index = 0;
        for (final Node node : data.optimizedNodes.values()) {
            switch (node.nodeType) {
                case Source: {
                    if (EnergyNetGlobal.debugGrid) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d %d %s.", node.getGrid().uid, index++, node);
                    }
                    data.activeNodes.add(node);
                    continue;
                }
                case Sink: {
                    if (EnergyNetGlobal.debugGrid) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d %d %s.", node.getGrid().uid, index++, node);
                    }
                    data.activeNodes.add(node);
                    continue;
                }
                case Conductor: {
                    if (EnergyNetGlobal.debugGrid) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d %d %s.", node.getGrid().uid, index++, node);
                    }
                    data.activeNodes.add(node);
                    continue;
                }
            }
        }
    }
    
    private static void populateNetworkMatrix(final StructureCache.Data data) {
        for (int row = 0; row < data.activeNodes.size(); ++row) {
            final Node node = data.activeNodes.get(row);
            for (int col = 0; col < data.activeNodes.size(); ++col) {
                double value = 0.0;
                if (row == col) {
                    for (final NodeLink link : node.links) {
                        if (link.getNeighbor(node) == node) {
                            continue;
                        }
                        value += 1.0 / link.loss;
                        assert link.loss >= 0.0;
                    }
                    if (EnergyNetLocal.useLinearTransferModel) {
                        if (node.nodeType == NodeType.Source) {
                            final double openCircuitVoltage = EnergyNet.instance.getPowerFromTier(node.getTier());
                            final double resistance = Util.square(openCircuitVoltage) / (node.getAmount() * 4.0);
                            assert resistance > 0.0;
                            value += 1.0 / resistance;
                            node.setResistance(resistance);
                        }
                        else if (node.nodeType == NodeType.Sink) {
                            final double resistance2 = EnergyNet.instance.getPowerFromTier(node.getTier());
                            assert resistance2 > 0.0;
                            value += 1.0 / resistance2;
                            node.setResistance(resistance2);
                        }
                    }
                    else if (node.nodeType == NodeType.Sink) {
                        ++value;
                    }
                }
                else {
                    final Node possibleNeighbor = data.activeNodes.get(col);
                    for (final NodeLink link2 : node.links) {
                        final Node neighbor = link2.getNeighbor(node);
                        if (neighbor == node) {
                            continue;
                        }
                        if (neighbor != possibleNeighbor) {
                            continue;
                        }
                        value -= 1.0 / link2.loss;
                        assert link2.loss >= 0.0;
                    }
                }
                data.networkMatrix.set(row, col, value);
            }
        }
    }
    
    private void populateSourceMatrix(final StructureCache.Data data) {
        for (int row = 0; row < data.activeNodes.size(); ++row) {
            final Node node = data.activeNodes.get(row);
            double input = 0.0;
            if (node.nodeType == NodeType.Source) {
                if (EnergyNetLocal.useLinearTransferModel) {
                    final double openCircuitVoltage = EnergyNet.instance.getPowerFromTier(node.getTier());
                    input = openCircuitVoltage / node.getResistance();
                }
                else {
                    input = node.getAmount();
                }
                assert input > 0.0;
            }
            data.sourceMatrix.set(row, 0, input);
        }
    }
    
    void dumpNodeInfo(final PrintStream ps, final boolean waitForFinish, final Node node) {
        if (waitForFinish) {
            this.finishCalculation();
        }
        ps.println("Node " + node + " info:");
        ps.println(" type: " + node.nodeType);
        switch (node.nodeType) {
            case Sink: {
                final IEnergySink sink = (IEnergySink)node.tile.mainTile;
                ps.println(" demanded: " + sink.getDemandedEnergy());
                ps.println(" tier: " + sink.getSinkTier());
                break;
            }
            case Source: {
                final IEnergySource source = (IEnergySource)node.tile.mainTile;
                ps.println(" offered: " + source.getOfferedEnergy());
                ps.println(" tier: " + source.getSourceTier());
                break;
            }
        }
        ps.println(node.links.size() + " neighbor links:");
        for (final NodeLink link : node.links) {
            ps.println(" " + link.getNeighbor(node) + " " + link.loss + " " + link.skippedNodes);
        }
        final StructureCache.Data data = this.lastData;
        if (data == null || !data.isInitialized || data.optimizedNodes == null) {
            ps.println("No optimized data");
        }
        else if (!data.optimizedNodes.containsKey(node.uid)) {
            ps.println("Optimized away");
        }
        else {
            final Node optimizedNode = data.optimizedNodes.get(node.uid);
            ps.println(optimizedNode.links.size() + " optimized neighbor links:");
            for (final NodeLink link2 : optimizedNode.links) {
                ps.println(" " + link2.getNeighbor(optimizedNode) + " " + link2.loss + " " + link2.skippedNodes);
            }
        }
    }
    
    void dumpMatrix(final PrintStream ps, final boolean waitForFinish, final boolean dumpNodesNetSrcMatrices, final boolean dumpResultMatrix) {
        if (waitForFinish) {
            this.finishCalculation();
        }
        if (dumpNodesNetSrcMatrices) {
            ps.println("Dumping matrices for " + this + ".");
        }
        final StructureCache.Data data = this.lastData;
        if (data == null) {
            ps.println("Matrices unavailable");
        }
        else if (dumpNodesNetSrcMatrices || dumpResultMatrix) {
            if (!data.isInitialized) {
                ps.println("Matrices potentially outdated");
            }
            if (dumpNodesNetSrcMatrices) {
                ps.println("Emitting node indizes:");
                for (int i = 0; i < data.activeNodes.size(); ++i) {
                    final Node node = data.activeNodes.get(i);
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
    
    private static void printMatrix(final DenseMatrix64F matrix, final PrintStream ps) {
        if (matrix == null) {
            ps.println("null");
            return;
        }
        boolean isZero = true;
        for (int i = 0; i < matrix.numRows; ++i) {
            for (int j = 0; j < matrix.numCols; ++j) {
                if (matrix.get(i, j) != 0.0) {
                    isZero = false;
                    break;
                }
            }
        }
        if (isZero) {
            ps.println(matrix.numRows + "x" + matrix.numCols + ", all zero");
        }
        else {
            MatrixIO.print(ps, matrix, "%.6f");
        }
    }
    
    void dumpStats(final PrintStream ps, final boolean waitForFinish) {
        if (waitForFinish) {
            this.finishCalculation();
        }
        ps.println("Grid " + this.uid + " info:");
        ps.println(this.nodes.size() + " nodes");
        final StructureCache.Data data = this.lastData;
        if (data != null && data.isInitialized) {
            if (data.activeNodes != null) {
                int srcCount = 0;
                int dstCount = 0;
                for (final Node node : data.activeNodes) {
                    if (node.nodeType == NodeType.Source) {
                        ++srcCount;
                    }
                    else {
                        if (node.nodeType != NodeType.Sink) {
                            continue;
                        }
                        ++dstCount;
                    }
                }
                ps.println("Active: " + srcCount + " sources -> " + dstCount + " sinks");
            }
            if (data.optimizedNodes != null) {
                ps.println(data.optimizedNodes.size() + " nodes after optimization");
            }
            if (data.activeNodes != null) {
                ps.println(data.activeNodes.size() + " emitting nodes");
            }
        }
        ps.printf("%d entries in cache, hitrate %.2f%%", this.cache.size(), 100.0 * this.cache.hits / (this.cache.hits + this.cache.misses));
        ps.println();
    }
    
    void dumpGraph(final boolean waitForFinish) {
        if (waitForFinish) {
            this.finishCalculation();
        }
        final StructureCache.Data data = this.lastData;
        for (int i = 0; i < 2; ++i) {
            if (i == 1) {
                if (data == null || !data.isInitialized) {
                    break;
                }
                if (data.optimizedNodes == null) {
                    break;
                }
            }
            FileWriter out = null;
            try {
                out = new FileWriter("graph_" + this.uid + "_" + ((i == 0) ? "raw" : "optimized") + ".txt");
                out.write("graph nodes {\n  overlap=false;\n");
                final Collection<Node> nodesToDump = ((i == 0) ? this.nodes : data.optimizedNodes).values();
                final Set<Node> dumpedConnections = new HashSet<Node>();
                for (final Node node : nodesToDump) {
                    out.write("  \"" + node + "\";\n");
                    for (final NodeLink link : node.links) {
                        final Node neighbor = link.getNeighbor(node);
                        if (!dumpedConnections.contains(neighbor)) {
                            out.write("  \"" + node + "\" -- \"" + neighbor + "\" [label=\"" + link.loss + "\"];\n");
                        }
                    }
                    dumpedConnections.add(node);
                }
                out.write("}\n");
            }
            catch (final IOException e) {
                IC2.log.debug(LogCategory.EnergyNet, e, "Graph saving failed.");
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                }
                catch (final IOException ex) {}
            }
        }
    }
    
    GridInfo getInfo() {
        int complexNodes = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (final Node node : this.nodes.values()) {
            if (node.links.size() > 2) {
                ++complexNodes;
            }
            for (final IEnergyTile tile : node.tile.subTiles) {
                final BlockPos pos = EnergyNet.instance.getPos(tile);
                if (pos.getX() < minX) {
                    minX = pos.getX();
                }
                if (pos.getY() < minY) {
                    minY = pos.getY();
                }
                if (pos.getZ() < minZ) {
                    minZ = pos.getZ();
                }
                if (pos.getX() > maxX) {
                    maxX = pos.getX();
                }
                if (pos.getY() > maxY) {
                    maxY = pos.getY();
                }
                if (pos.getZ() > maxZ) {
                    maxZ = pos.getZ();
                }
            }
        }
        return new GridInfo(this.uid, this.nodes.size(), complexNodes, minX, minY, minZ, maxX, maxY, maxZ);
    }
}
