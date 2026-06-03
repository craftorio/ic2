package ic2.core.energy.leg;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.energy.tile.IOverloadHandler;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.energy.grid.EnergyNetSettings;
import ic2.core.energy.grid.Grid;
import ic2.core.energy.grid.IEnergyCalculator;
import ic2.core.energy.grid.Node;
import ic2.core.energy.grid.NodeLink;
import ic2.core.energy.grid.NodeType;
import ic2.core.energy.grid.Tile;
import ic2.core.init.MainConfig;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableDouble;

public class EnergyCalculatorLeg implements IEnergyCalculator {
  public void handleGridChange(Grid grid) {
    long startTime = 0L;
    GridData data = getData(grid);
    updateCache(grid, data);
  }
  
  public boolean runSyncStep(EnergyNetLocal enet) {
    boolean foundAny = false;
    for (Tile tile : enet.getSources()) {
      IEnergySource source = (IEnergySource)tile.getMainTile();
      int packets = 1;
      IMultiEnergySource multiSource;
      double amount;
      if (!tile.isDisabled() && (
        amount = source.getOfferedEnergy()) > 0.0D && (!(source instanceof IMultiEnergySource) || 
        !(multiSource = (IMultiEnergySource)source).sendMultipleEnergyPackets() || (packets = multiSource.getMultipleEnergyPacketAmount()) > 0)) {
        int tier = source.getSourceTier();
        if (tier < 0) {
          if (EnergyNetSettings.logGridCalculationIssues)
            IC2.log.warn(LogCategory.EnergyNet, "Tile %s reported an invalid tier (%d).", new Object[] { Util.toString(source, (IBlockAccess)enet.getWorld(), EnergyNet.instance.getPos((IEnergyTile)source)), Integer.valueOf(tier) }); 
          tile.setSourceData(0.0D, 0);
          continue;
        } 
        foundAny = true;
        double power = EnergyNet.instance.getPowerFromTier(tier);
        amount = Math.min(amount, power * packets);
        tile.setSourceData(amount, packets);
        continue;
      } 
      tile.setSourceData(0.0D, 0);
    } 
    return foundAny;
  }
  
  public boolean runSyncStep(Grid grid) {
    long startTime = 0L;
    if (runCalculation(grid, getData(grid)));
    return false;
  }
  
  public void runAsyncStep(Grid grid) {
    throw new UnsupportedOperationException();
  }
  
  public NodeStats getNodeStats(Tile tile) {
    double in = 0.0D;
    double out = 0.0D;
    double max = 0.0D;
    for (Node node : tile.getNodes()) {
      GridData data = (GridData)node.getGrid().getData();
      if (data == null || !data.active)
        continue; 
      int calcId = data.currentCalcId;
      Collection<EnergyPath> paths = getPaths(node, data);
      double sum = 0.0D;
      for (EnergyPath path : paths) {
        if (path.lastCalcId != calcId)
          continue; 
        sum += path.energySupplied;
        max = Math.max(path.maxPacketConducted, max);
      } 
      if (node.getType() == NodeType.Source) {
        out += sum;
        continue;
      } 
      if (node.getType() == NodeType.Sink) {
        in += sum;
        continue;
      } 
      in += sum;
      out += sum;
    } 
    return new NodeStats(in, out, EnergyNet.instance.getTierFromPower(max));
  }
  
  private static Collection<EnergyPath> getPaths(Node node, GridData data) {
    if (node.getType() == NodeType.Source) {
      List<EnergyPath> list = data.energySourceToEnergyPathMap.get(node);
      if (list == null)
        list = Collections.emptyList(); 
      return list;
    } 
    List<EnergyPath> ret = data.pathCache.get(node);
    if (ret != null)
      return ret; 
    ret = new ArrayList<>();
    for (List<EnergyPath> paths : data.energySourceToEnergyPathMap.values()) {
      for (EnergyPath path : paths) {
        if (node.getType() == NodeType.Sink) {
          if (path.target == node)
            ret.add(path); 
          continue;
        } 
        if (path.conductors.contains(node))
          ret.add(path); 
      } 
    } 
    data.pathCache.put(node, ret);
    return ret;
  }
  
  public void dumpNodeInfo(Node node, String prefix, PrintStream console, PrintStream chat) {
    GridData data = getData(node.getGrid());
    Collection<EnergyPath> paths = getPaths(node, data);
    switch (node.getType()) {
      case Source:
        chat.printf("%s%d connected sink nodes%n", new Object[] { prefix, Integer.valueOf(paths.size()) });
        break;
      case Sink:
        chat.printf("%s%d connected source nodes%n", new Object[] { prefix, Integer.valueOf(paths.size()) });
        break;
      case Conductor:
        chat.printf("%s%d paths across this conductor%n", new Object[] { prefix, Integer.valueOf(paths.size()) });
        break;
    } 
    double sum = 0.0D;
    double max = 0.0D;
    int calcId = data.currentCalcId;
    int pathsToPrint = 8;
    int n = 0;
    for (EnergyPath path : paths) {
      boolean printPathEnergy = false;
      if (n < 8) {
        switch (node.getType()) {
          case Source:
            chat.printf("%s %s", new Object[] { prefix, path.target });
            break;
          case Sink:
            chat.printf("%s %s", new Object[] { prefix, path.source });
            break;
          case Conductor:
            chat.printf("%s %s -> %s", new Object[] { prefix, path.source, path.target });
            break;
        } 
        printPathEnergy = true;
      } else if (n == 8) {
        chat.printf("%s ... (%d more)%n", new Object[] { Integer.valueOf(paths.size() - 8) });
      } 
      n++;
      if (path.lastCalcId != calcId) {
        if (printPathEnergy)
          chat.println(" (idle)"); 
        continue;
      } 
      if (printPathEnergy)
        chat.printf(" (%.2f EU, max packet %.2f EU)%n", new Object[] { Double.valueOf(path.energySupplied), Double.valueOf(path.maxPacketConducted) }); 
      sum += path.energySupplied;
      max = Math.max(path.maxPacketConducted, max);
    } 
    chat.printf("%slast tick: %.2f EU, max packet %.2f EU%n", new Object[] { prefix, Double.valueOf(sum), Double.valueOf(max) });
  }
  
  private static void updateCache(Grid grid, GridData data) {
    Queue<Node> queue;
    data.active = false;
    data.energySourceToEnergyPathMap.clear();
    data.activeSources.clear();
    data.activeSinks.clear();
    data.pathCache.clear();
    data.currentCalcId = -1;
    Collection<Node> nodes = grid.getNodes();
    if (nodes.size() < 2)
      return; 
    List<Node> sources = new ArrayList<>();
    int sinkCount = 0;
    for (Node node : nodes) {
      if (node.getType() == NodeType.Source) {
        sources.add(node);
        continue;
      } 
      if (node.getType() == NodeType.Sink)
        sinkCount++; 
    } 
    if (sources.isEmpty() || sinkCount == 0)
      return; 
    Map<Node, Node> path = new IdentityHashMap<>();
    final Map<Node, Double> lossMap = new IdentityHashMap<>();
    if (sources.size() <= 2048) {
      queue = new PriorityQueue<>(nodes.size(), new Comparator<Node>() {
            public int compare(Node a, Node b) {
              return ((Double)lossMap.get(a)).compareTo((Double)lossMap.get(b));
            }
          });
    } else {
      queue = new ArrayDeque<>(nodes.size());
    } 
    Map<IEnergyTile, EnergyPath> paths = new LinkedHashMap<>();
    for (Node srcNode : sources) {
      lossMap.put(srcNode, Double.valueOf(0.0D));
      queue.add(srcNode);
      Node node;
      while ((node = queue.poll()) != null) {
        if (node.getType() == NodeType.Sink) {
          double loss = ((Double)lossMap.get(node)).doubleValue();
          IEnergyTile tile = node.getTile().getMainTile();
          EnergyPath prev = paths.get(tile);
          if (prev != null && prev.loss <= loss)
            continue; 
          if (EnergyNetSettings.roundLossDown)
            loss = Math.floor(loss); 
          paths.put(tile, new EnergyPath(srcNode, node, reconstructPath(srcNode, node, path), loss));
          if (paths.size() == sinkCount)
            break; 
          continue;
        } 
        if (node.getType() == NodeType.Conductor || node == srcNode) {
          double loss = ((Double)lossMap.get(node)).doubleValue();
          for (NodeLink link : node.getLinks()) {
            Node neighbor = link.getNeighbor(node);
            if (neighbor.getType() == NodeType.Source) {
              List<EnergyPath> srcPaths = data.energySourceToEnergyPathMap.get(neighbor);
              if (srcPaths != null) {
                if (!srcPaths.isEmpty()) {
                  loss -= link.getLoss();
                  List<Node> pathToHere = null;
                  for (EnergyPath cPath : srcPaths) {
                    double cLoss = loss + cPath.loss;
                    IEnergyTile tile = cPath.target.getTile().getMainTile();
                    EnergyPath prev = paths.get(tile);
                    if (prev != null && prev.loss <= cLoss)
                      continue; 
                    if (EnergyNetSettings.roundLossDown)
                      cLoss = Math.floor(cLoss); 
                    if (pathToHere == null)
                      pathToHere = reconstructPath(srcNode, node, path); 
                    List<Node> conductors = new ArrayList<>(pathToHere.size() + cPath.conductors.size());
                    conductors.addAll(pathToHere);
                    conductors.addAll(cPath.conductors);
                    paths.put(tile, new EnergyPath(srcNode, cPath.target, conductors, cLoss));
                  } 
                } 
                break;
              } 
              continue;
            } 
            double newLoss = loss + link.getLoss();
            Double prevLoss = lossMap.get(neighbor);
            if (prevLoss == null || prevLoss.doubleValue() > newLoss) {
              if (prevLoss != null)
                queue.remove(neighbor); 
              lossMap.put(neighbor, Double.valueOf(newLoss));
              path.put(neighbor, node);
              queue.add(neighbor);
            } 
          } 
        } 
      } 
      if (!paths.isEmpty())
        data.energySourceToEnergyPathMap.put(srcNode, new ArrayList<>(paths.values())); 
      lossMap.clear();
      path.clear();
      paths.clear();
      queue.clear();
    } 
    if (!data.energySourceToEnergyPathMap.isEmpty())
      data.active = true; 
  }
  
  private static List<Node> reconstructPath(Node srcNode, Node dstNode, Map<Node, Node> path) {
    List<Node> ret = new ArrayList<>();
    Node node = dstNode;
    while ((node = path.get(node)) != srcNode) {
      assert node != null;
      ret.add(node);
    } 
    Collections.reverse(ret);
    return ret;
  }
  
  private static boolean runCalculation(Grid grid, GridData data) {
    int sourcesOffset;
    if (!data.active)
      return false; 
    List<Node> activeSources = data.activeSources;
    Map<Node, MutableDouble> activeSinks = data.activeSinks;
    activeSources.clear();
    activeSinks.clear();
    int calcId = ++data.currentCalcId;
    for (Node node : grid.getNodes()) {
      Tile tile = node.getTile();
      if (tile.isDisabled())
        continue; 
      if (node.getType() == NodeType.Source && data.energySourceToEnergyPathMap
        .containsKey(node) && tile
        .getAmount() > 0.0D) {
        activeSources.add(node);
        continue;
      } 
      double amount;
      if (node.getType() == NodeType.Sink && (
        amount = ((IEnergySink)tile.getMainTile()).getDemandedEnergy()) > 0.0D)
        activeSinks.put(node, new MutableDouble(amount)); 
    } 
    if (activeSources.isEmpty() || activeSinks.isEmpty())
      return false; 
    World world = grid.getEnergyNet().getWorld();
    Random rand = world.field_73012_v;
    boolean shufflePaths = ((world.func_82737_E() & 0x3L) != 0L);
    if (activeSources.size() > 1) {
      sourcesOffset = rand.nextInt(activeSources.size());
    } else {
      sourcesOffset = 0;
    } 
    int i;
    for (i = sourcesOffset; i < activeSources.size() && !activeSinks.isEmpty(); i++)
      distribute(activeSources.get(i), data, shufflePaths, calcId, rand); 
    for (i = 0; i < sourcesOffset && !activeSinks.isEmpty(); i++)
      distribute(activeSources.get(i), data, shufflePaths, calcId, rand); 
    if (!data.eventPaths.isEmpty()) {
      applyCableEffects(data.eventPaths, grid.getEnergyNet().getWorld());
      data.eventPaths.clear();
    } 
    return true;
  }
  
  private static void distribute(Node srcNode, GridData data, boolean shufflePaths, int calcId, Random rand) {
    int pathOffset;
    double offer;
    Tile tile = srcNode.getTile();
    int packetCount = tile.getPacketCount();
    assert packetCount > 0;
    List<EnergyPath> paths = data.energySourceToEnergyPathMap.get(srcNode);
    if (paths.size() > 1 && shufflePaths) {
      pathOffset = rand.nextInt(paths.size());
    } else {
      pathOffset = 0;
    } 
    double totalOffer = tile.getAmount();
    assert totalOffer > 0.0D;
    if (packetCount == 1) {
      offer = distributeSingle(totalOffer, tile, paths, pathOffset, data, calcId);
    } else {
      offer = distributeMultiple(totalOffer, tile, paths, pathOffset, data, calcId, packetCount);
    } 
    double used = totalOffer - Math.max(0.0D, offer);
    if (used > 0.0D) {
      tile.setAmount(offer);
      ((IEnergySource)tile.getMainTile()).drawEnergy(used);
    } 
  }
  
  private static double distributeSingle(double offer, Tile tile, List<EnergyPath> paths, int pathOffset, GridData data, int calcId) {
    int i;
    for (i = pathOffset; i < paths.size(); i++) {
      offer -= emit(paths.get(i), offer, data, calcId);
      if (offer <= 0.0D)
        break; 
    } 
    for (i = 0; i < pathOffset && offer > 0.0D; i++)
      offer -= emit(paths.get(i), offer, data, calcId); 
    return offer;
  }
  
  private static double distributeMultiple(double offer, Tile tile, List<EnergyPath> paths, int pathOffset, GridData data, int calcId, int packetCount) {
    IEnergySource source = (IEnergySource)tile.getMainTile();
    double power = EnergyNet.instance.getPowerFromTier(source.getSourceTier());
    do {
      double cOffer = Math.min(offer, power);
      double used = cOffer - distributeSingle(cOffer, tile, paths, pathOffset, data, calcId);
      if (used <= 0.0D)
        break; 
      offer -= used;
    } while (--packetCount > 0 && offer > 0.0D);
    return offer;
  }
  
  private static double emit(EnergyPath path, double offer, GridData data, int calcId) {
    Tile targetTile = path.target.getTile();
    if (targetTile.isDisabled())
      return 0.0D; 
    double injectAmount = offer - path.loss;
    if (injectAmount <= 0.0D)
      return 0.0D; 
    MutableDouble sinkDemand = data.activeSinks.get(path.target);
    if (sinkDemand == null)
      return 0.0D; 
    IEnergySink sink = (IEnergySink)targetTile.getMainTile();
    double amount = Math.min(injectAmount, sinkDemand.doubleValue());
    double rejected = sink.injectEnergy(path.targetDirection, amount, EnergyNet.instance.getTierFromPower(amount));
    if (rejected >= amount)
      return 0.0D; 
    double effectiveAmount = Math.max(0.0D, amount - rejected + path.loss);
    if (path.lastCalcId != calcId) {
      path.lastCalcId = calcId;
      path.energySupplied = 0.0D;
      path.maxPacketConducted = 0.0D;
    } 
    path.energySupplied += amount - rejected;
    path.maxPacketConducted = Math.max(effectiveAmount, path.maxPacketConducted);
    if (effectiveAmount > path.minEffectEnergy || amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier()))
      data.eventPaths.add(path); 
    if (amount >= sinkDemand.doubleValue() || rejected > 0.0D)
      data.activeSinks.remove(path.target); 
    return effectiveAmount;
  }
  
  private static void applyCableEffects(Collection<EnergyPath> eventPaths, World world) {
    if (!MainConfig.get().get("misc/enableEnetCableMeltdown").getBool())
      return; 
    Set<Tile> cablesToRemove = Collections.newSetFromMap(new IdentityHashMap<>());
    Set<Tile> cablesToStrip = Collections.newSetFromMap(new IdentityHashMap<>());
    Map<Tile, MutableDouble> sinksToExplode = new IdentityHashMap<>();
    Map<EntityLivingBase, MutableDouble> shockEnergyMap = new IdentityHashMap<>();
    for (EnergyPath path : eventPaths) {
      double amount = path.maxPacketConducted;
      boolean conductorOverload = false;
      if (amount > path.minConductorBreakdownEnergy || amount > path.minInsulationBreakdownEnergy) {
        conductorOverload = true;
        for (Node node : path.conductors) {
          Tile tile = node.getTile();
          IEnergyConductor conductor = (IEnergyConductor)tile.getMainTile();
          if (amount > conductor.getConductorBreakdownEnergy()) {
            cablesToRemove.add(tile);
            continue;
          } 
          if (amount > conductor.getInsulationBreakdownEnergy())
            cablesToStrip.add(tile); 
        } 
      } 
      if (amount > path.minInsulationEnergyAbsorption) {
        List<EntityLivingBase> nearbyEntities = world.func_72872_a(EntityLivingBase.class, new AxisAlignedBB((path.minX - 1), (path.minY - 1), (path.minZ - 1), (path.maxX + 2), (path.maxY + 2), (path.maxZ + 2)));
        if (!nearbyEntities.isEmpty()) {
          Map<EntityLivingBase, MutableDouble> localShockEnergyMap = new IdentityHashMap<>();
          for (Node node : path.conductors) {
            Tile tile = node.getTile();
            IEnergyConductor conductor = (IEnergyConductor)tile.getMainTile();
            if (amount <= conductor.getInsulationEnergyAbsorption())
              continue; 
            int shockEnergy = (int)(amount - conductor.getInsulationEnergyAbsorption());
            for (IEnergyTile subTile : tile.getSubTiles()) {
              BlockPos pos = EnergyNet.instance.getPos(subTile);
              for (EntityLivingBase entity : nearbyEntities) {
                MutableDouble prev = localShockEnergyMap.get(entity);
                if (prev != null && prev.doubleValue() >= shockEnergy)
                  continue; 
                if (!entity.func_174813_aQ().func_72326_a(new AxisAlignedBB((pos
                      .func_177958_n() - 1), (pos
                      .func_177956_o() - 1), (pos
                      .func_177952_p() - 1), (pos
                      .func_177958_n() + 2), (pos
                      .func_177956_o() + 2), (pos
                      .func_177952_p() + 2))))
                  continue; 
                if (prev == null) {
                  localShockEnergyMap.put(entity, new MutableDouble(shockEnergy));
                  continue;
                } 
                prev.setValue(shockEnergy);
              } 
            } 
          } 
          for (Map.Entry<EntityLivingBase, MutableDouble> entry : localShockEnergyMap.entrySet()) {
            MutableDouble prev = shockEnergyMap.get(entry.getKey());
            if (prev == null) {
              shockEnergyMap.put(entry.getKey(), entry.getValue());
              continue;
            } 
            prev.add(((MutableDouble)entry.getValue()).doubleValue());
          } 
        } 
      } 
      Tile sinkTile = path.target.getTile();
      IEnergySink sink = (IEnergySink)sinkTile.getMainTile();
      if (!conductorOverload && amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier())) {
        MutableDouble prev = sinksToExplode.get(sinkTile);
        if (prev == null) {
          sinksToExplode.put(sinkTile, new MutableDouble(amount));
          continue;
        } 
        if (prev.doubleValue() < amount)
          prev.setValue(amount); 
      } 
    } 
    cablesToStrip.removeAll(cablesToRemove);
    for (Tile tile : cablesToRemove)
      ((IEnergyConductor)tile.getMainTile()).removeConductor(); 
    for (Tile tile : cablesToStrip)
      ((IEnergyConductor)tile.getMainTile()).removeInsulation(); 
    for (Map.Entry<Tile, MutableDouble> entry : sinksToExplode.entrySet())
      explodeTile(world, entry.getKey(), ((MutableDouble)entry.getValue()).doubleValue()); 
    for (Map.Entry<EntityLivingBase, MutableDouble> entry : shockEnergyMap.entrySet()) {
      EntityLivingBase target = entry.getKey();
      int damage = (int)Math.ceil(((MutableDouble)entry.getValue()).doubleValue() / 64.0D);
      if (target.func_70089_S() && damage > 0)
        target.func_70097_a((DamageSource)IC2DamageSource.electricity, damage); 
    } 
  }
  
  private static GridData getData(Grid grid) {
    GridData ret = (GridData)grid.getData();
    if (ret == null) {
      ret = new GridData();
      grid.setData(ret);
    } 
    return ret;
  }
  
  private static class GridData {
    boolean active;
    
    private GridData() {}
    
    final Map<Node, List<EnergyPath>> energySourceToEnergyPathMap = new IdentityHashMap<>();
    
    final List<Node> activeSources = new ArrayList<>();
    
    final Map<Node, MutableDouble> activeSinks = new IdentityHashMap<>();
    
    final Set<EnergyPath> eventPaths = Collections.newSetFromMap(new IdentityHashMap<>());
    
    final Map<Node, List<EnergyPath>> pathCache = new IdentityHashMap<>();
    
    int currentCalcId = -1;
  }
  
  private static void explodeTile(World world, Tile tile, double maxPower) {
    if (!MainConfig.get().get("misc/enableEnetExplosions").getBool())
      return; 
    int tier = EnergyNet.instance.getTierFromPower(maxPower);
    for (IEnergyTile subTile : tile.getSubTiles()) {
      IEnergySink mainTile = (IEnergySink)tile.getMainTile();
      BlockPos pos = EnergyNet.instance.getPos(subTile);
      TileEntity realTe = world.func_175625_s(pos);
      if ((mainTile instanceof IOverloadHandler && ((IOverloadHandler)mainTile).onOverload(tier)) || (realTe instanceof IOverloadHandler && ((IOverloadHandler)realTe)
        .onOverload(tier)))
        continue; 
      float power = 2.5F;
      if (mainTile instanceof IExplosionPowerOverride) {
        IExplosionPowerOverride override = (IExplosionPowerOverride)mainTile;
        if (!override.shouldExplode())
          continue; 
        power = override.getExplosionPower(tier, power);
      } else if (realTe instanceof IExplosionPowerOverride) {
        IExplosionPowerOverride override = (IExplosionPowerOverride)realTe;
        if (!override.shouldExplode())
          continue; 
        power = override.getExplosionPower(tier, power);
      } 
      EntityPlayer closestPlayer = world.func_184137_a(pos.func_177958_n() + 0.5D, pos.func_177956_o() + 0.5D, pos.func_177952_p() + 0.5D, 20.0D, false);
      if (closestPlayer != null)
        IC2.achievements.issueAchievement(closestPlayer, "explodeMachine"); 
      world.func_175698_g(pos);
      ExplosionIC2 explosion = new ExplosionIC2(world, null, pos.func_177958_n() + 0.5D, pos.func_177956_o() + 0.5D, pos.func_177952_p() + 0.5D, power, 0.75F, ExplosionIC2.Type.Electrical);
      explosion.doExplosion();
    } 
  }
}
