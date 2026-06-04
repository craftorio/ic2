// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.leg;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.Entity;
import ic2.core.ExplosionIC2;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.energy.tile.IOverloadHandler;
import net.minecraft.util.math.BlockPos;
import java.util.Set;
import net.minecraft.util.DamageSource;
import ic2.core.IC2DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import ic2.api.energy.tile.IEnergyConductor;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.init.MainConfig;
import java.util.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableDouble;
import ic2.api.energy.tile.IEnergySink;
import java.util.Queue;
import ic2.core.energy.grid.NodeLink;
import java.util.LinkedHashMap;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collection;
import ic2.core.energy.grid.NodeType;
import ic2.core.energy.grid.Node;
import ic2.api.energy.NodeStats;
import java.util.Iterator;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.EnergyNet;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.energy.grid.EnergyNetSettings;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.energy.grid.Tile;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.energy.grid.Grid;
import ic2.core.energy.grid.IEnergyCalculator;

public class EnergyCalculatorLeg implements IEnergyCalculator
{
    @Override
    public void handleGridChange(final Grid grid) {
        final long startTime = 0L;
        final GridData data = getData(grid);
        updateCache(grid, data);
    }
    
    @Override
    public boolean runSyncStep(final EnergyNetLocal enet) {
        boolean foundAny = false;
        for (final Tile tile : enet.getSources()) {
            final IEnergySource source = (IEnergySource)tile.getMainTile();
            int packets = 1;
            double amount;
            final IMultiEnergySource multiSource;
            if (!tile.isDisabled() && (amount = source.getOfferedEnergy()) > 0.0 && (!(source instanceof IMultiEnergySource) || !(multiSource = (IMultiEnergySource)source).sendMultipleEnergyPackets() || (packets = multiSource.getMultipleEnergyPacketAmount()) > 0)) {
                final int tier = source.getSourceTier();
                if (tier < 0) {
                    if (EnergyNetSettings.logGridCalculationIssues) {
                        IC2.log.warn(LogCategory.EnergyNet, "Tile %s reported an invalid tier (%d).", Util.toString(source, (IBlockAccess)enet.getWorld(), EnergyNet.instance.getPos(source)), tier);
                    }
                    tile.setSourceData(0.0, 0);
                }
                else {
                    foundAny = true;
                    final double power = EnergyNet.instance.getPowerFromTier(tier);
                    amount = Math.min(amount, power * packets);
                    tile.setSourceData(amount, packets);
                }
            }
            else {
                tile.setSourceData(0.0, 0);
            }
        }
        return foundAny;
    }
    
    @Override
    public boolean runSyncStep(final Grid grid) {
        final long startTime = 0L;
        if (runCalculation(grid, getData(grid))) {}
        return false;
    }
    
    @Override
    public void runAsyncStep(final Grid grid) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public NodeStats getNodeStats(final Tile tile) {
        double in = 0.0;
        double out = 0.0;
        double max = 0.0;
        for (final Node node : tile.getNodes()) {
            final GridData data = node.getGrid().getData();
            if (data != null) {
                if (!data.active) {
                    continue;
                }
                final int calcId = data.currentCalcId;
                final Collection<EnergyPath> paths = getPaths(node, data);
                double sum = 0.0;
                for (final EnergyPath path : paths) {
                    if (path.lastCalcId != calcId) {
                        continue;
                    }
                    sum += path.energySupplied;
                    max = Math.max(path.maxPacketConducted, max);
                }
                if (node.getType() == NodeType.Source) {
                    out += sum;
                }
                else if (node.getType() == NodeType.Sink) {
                    in += sum;
                }
                else {
                    in += sum;
                    out += sum;
                }
            }
        }
        return new NodeStats(in, out, EnergyNet.instance.getTierFromPower(max));
    }
    
    private static Collection<EnergyPath> getPaths(final Node node, final GridData data) {
        if (node.getType() == NodeType.Source) {
            List<EnergyPath> ret = data.energySourceToEnergyPathMap.get(node);
            if (ret == null) {
                ret = Collections.emptyList();
            }
            return ret;
        }
        List<EnergyPath> ret = data.pathCache.get(node);
        if (ret != null) {
            return ret;
        }
        ret = new ArrayList<EnergyPath>();
        for (final List<EnergyPath> paths : data.energySourceToEnergyPathMap.values()) {
            for (final EnergyPath path : paths) {
                if (node.getType() == NodeType.Sink) {
                    if (path.target != node) {
                        continue;
                    }
                    ret.add(path);
                }
                else {
                    if (!path.conductors.contains(node)) {
                        continue;
                    }
                    ret.add(path);
                }
            }
        }
        data.pathCache.put(node, ret);
        return ret;
    }
    
    @Override
    public void dumpNodeInfo(final Node node, final String prefix, final PrintStream console, final PrintStream chat) {
        final GridData data = getData(node.getGrid());
        final Collection<EnergyPath> paths = getPaths(node, data);
        switch (node.getType()) {
            case Source: {
                chat.printf("%s%d connected sink nodes%n", prefix, paths.size());
                break;
            }
            case Sink: {
                chat.printf("%s%d connected source nodes%n", prefix, paths.size());
                break;
            }
            case Conductor: {
                chat.printf("%s%d paths across this conductor%n", prefix, paths.size());
                break;
            }
        }
        double sum = 0.0;
        double max = 0.0;
        final int calcId = data.currentCalcId;
        final int pathsToPrint = 8;
        int n = 0;
        for (final EnergyPath path : paths) {
            boolean printPathEnergy = false;
            if (n < 8) {
                switch (node.getType()) {
                    case Source: {
                        chat.printf("%s %s", prefix, path.target);
                        break;
                    }
                    case Sink: {
                        chat.printf("%s %s", prefix, path.source);
                        break;
                    }
                    case Conductor: {
                        chat.printf("%s %s -> %s", prefix, path.source, path.target);
                        break;
                    }
                }
                printPathEnergy = true;
            }
            else if (n == 8) {
                chat.printf("%s ... (%d more)%n", paths.size() - 8);
            }
            ++n;
            if (path.lastCalcId != calcId) {
                if (!printPathEnergy) {
                    continue;
                }
                chat.println(" (idle)");
            }
            else {
                if (printPathEnergy) {
                    chat.printf(" (%.2f EU, max packet %.2f EU)%n", path.energySupplied, path.maxPacketConducted);
                }
                sum += path.energySupplied;
                max = Math.max(path.maxPacketConducted, max);
            }
        }
        chat.printf("%slast tick: %.2f EU, max packet %.2f EU%n", prefix, sum, max);
    }
    
    private static void updateCache(final Grid grid, final GridData data) {
        data.active = false;
        data.energySourceToEnergyPathMap.clear();
        data.activeSources.clear();
        data.activeSinks.clear();
        data.pathCache.clear();
        data.currentCalcId = -1;
        final Collection<Node> nodes = grid.getNodes();
        if (nodes.size() < 2) {
            return;
        }
        final List<Node> sources = new ArrayList<Node>();
        int sinkCount = 0;
        for (final Node node : nodes) {
            if (node.getType() == NodeType.Source) {
                sources.add(node);
            }
            else {
                if (node.getType() != NodeType.Sink) {
                    continue;
                }
                ++sinkCount;
            }
        }
        if (sources.isEmpty() || sinkCount == 0) {
            return;
        }
        final Map<Node, Node> path = new IdentityHashMap<Node, Node>();
        final Map<Node, Double> lossMap = new IdentityHashMap<Node, Double>();
        Queue<Node> queue;
        if (sources.size() <= 2048) {
            queue = new PriorityQueue<Node>(nodes.size(), new Comparator<Node>() {
                @Override
                public int compare(final Node a, final Node b) {
                    return lossMap.get(a).compareTo(lossMap.get(b));
                }
            });
        }
        else {
            queue = new ArrayDeque<Node>(nodes.size());
        }
        final Map<IEnergyTile, EnergyPath> paths = new LinkedHashMap<IEnergyTile, EnergyPath>();
        for (final Node srcNode : sources) {
            lossMap.put(srcNode, 0.0);
            queue.add(srcNode);
            Node node2;
            while ((node2 = queue.poll()) != null) {
                if (node2.getType() == NodeType.Sink) {
                    double loss = lossMap.get(node2);
                    final IEnergyTile tile = node2.getTile().getMainTile();
                    final EnergyPath prev = paths.get(tile);
                    if (prev != null && prev.loss <= loss) {
                        continue;
                    }
                    if (EnergyNetSettings.roundLossDown) {
                        loss = Math.floor(loss);
                    }
                    paths.put(tile, new EnergyPath(srcNode, node2, reconstructPath(srcNode, node2, path), loss));
                    if (paths.size() == sinkCount) {
                        break;
                    }
                    continue;
                }
                else {
                    if (node2.getType() != NodeType.Conductor && node2 != srcNode) {
                        continue;
                    }
                    double loss = lossMap.get(node2);
                    for (final NodeLink link : node2.getLinks()) {
                        final Node neighbor = link.getNeighbor(node2);
                        if (neighbor.getType() == NodeType.Source) {
                            final List<EnergyPath> srcPaths = data.energySourceToEnergyPathMap.get(neighbor);
                            if (srcPaths == null) {
                                continue;
                            }
                            if (!srcPaths.isEmpty()) {
                                loss -= link.getLoss();
                                List<Node> pathToHere = null;
                                for (final EnergyPath cPath : srcPaths) {
                                    double cLoss = loss + cPath.loss;
                                    final IEnergyTile tile2 = cPath.target.getTile().getMainTile();
                                    final EnergyPath prev2 = paths.get(tile2);
                                    if (prev2 != null && prev2.loss <= cLoss) {
                                        continue;
                                    }
                                    if (EnergyNetSettings.roundLossDown) {
                                        cLoss = Math.floor(cLoss);
                                    }
                                    if (pathToHere == null) {
                                        pathToHere = reconstructPath(srcNode, node2, path);
                                    }
                                    final List<Node> conductors = new ArrayList<Node>(pathToHere.size() + cPath.conductors.size());
                                    conductors.addAll(pathToHere);
                                    conductors.addAll(cPath.conductors);
                                    paths.put(tile2, new EnergyPath(srcNode, cPath.target, conductors, cLoss));
                                }
                                break;
                            }
                            break;
                        }
                        else {
                            final double newLoss = loss + link.getLoss();
                            final Double prevLoss = lossMap.get(neighbor);
                            if (prevLoss != null && prevLoss <= newLoss) {
                                continue;
                            }
                            if (prevLoss != null) {
                                queue.remove(neighbor);
                            }
                            lossMap.put(neighbor, newLoss);
                            path.put(neighbor, node2);
                            queue.add(neighbor);
                        }
                    }
                }
            }
            if (!paths.isEmpty()) {
                data.energySourceToEnergyPathMap.put(srcNode, new ArrayList<EnergyPath>(paths.values()));
            }
            lossMap.clear();
            path.clear();
            paths.clear();
            queue.clear();
        }
        if (!data.energySourceToEnergyPathMap.isEmpty()) {
            data.active = true;
        }
    }
    
    private static List<Node> reconstructPath(final Node srcNode, final Node dstNode, final Map<Node, Node> path) {
        final List<Node> ret = new ArrayList<Node>();
        Node node = dstNode;
        while ((node = path.get(node)) != srcNode) {
            assert node != null;
            ret.add(node);
        }
        Collections.reverse(ret);
        return ret;
    }
    
    private static boolean runCalculation(final Grid grid, final GridData data) {
        if (!data.active) {
            return false;
        }
        final List<Node> activeSources = data.activeSources;
        final Map<Node, MutableDouble> activeSinks = data.activeSinks;
        activeSources.clear();
        activeSinks.clear();
        final int calcId = ++data.currentCalcId;
        for (final Node node : grid.getNodes()) {
            final Tile tile = node.getTile();
            if (tile.isDisabled()) {
                continue;
            }
            if (node.getType() == NodeType.Source && data.energySourceToEnergyPathMap.containsKey(node) && tile.getAmount() > 0.0) {
                activeSources.add(node);
            }
            else {
                final double amount;
                if (node.getType() != NodeType.Sink || (amount = ((IEnergySink)tile.getMainTile()).getDemandedEnergy()) <= 0.0) {
                    continue;
                }
                activeSinks.put(node, new MutableDouble(amount));
            }
        }
        if (activeSources.isEmpty() || activeSinks.isEmpty()) {
            return false;
        }
        final World world = grid.getEnergyNet().getWorld();
        final Random rand = world.rand;
        final boolean shufflePaths = (world.getTotalWorldTime() & 0x3L) != 0x0L;
        int sourcesOffset;
        if (activeSources.size() > 1) {
            sourcesOffset = rand.nextInt(activeSources.size());
        }
        else {
            sourcesOffset = 0;
        }
        for (int i = sourcesOffset; i < activeSources.size() && !activeSinks.isEmpty(); ++i) {
            distribute(activeSources.get(i), data, shufflePaths, calcId, rand);
        }
        for (int i = 0; i < sourcesOffset && !activeSinks.isEmpty(); ++i) {
            distribute(activeSources.get(i), data, shufflePaths, calcId, rand);
        }
        if (!data.eventPaths.isEmpty()) {
            applyCableEffects(data.eventPaths, grid.getEnergyNet().getWorld());
            data.eventPaths.clear();
        }
        return true;
    }
    
    private static void distribute(final Node srcNode, final GridData data, final boolean shufflePaths, final int calcId, final Random rand) {
        final Tile tile = srcNode.getTile();
        final int packetCount = tile.getPacketCount();
        assert packetCount > 0;
        final List<EnergyPath> paths = data.energySourceToEnergyPathMap.get(srcNode);
        int pathOffset;
        if (paths.size() > 1 && shufflePaths) {
            pathOffset = rand.nextInt(paths.size());
        }
        else {
            pathOffset = 0;
        }
        final double totalOffer = tile.getAmount();
        assert totalOffer > 0.0;
        double offer;
        if (packetCount == 1) {
            offer = distributeSingle(totalOffer, tile, paths, pathOffset, data, calcId);
        }
        else {
            offer = distributeMultiple(totalOffer, tile, paths, pathOffset, data, calcId, packetCount);
        }
        final double used = totalOffer - Math.max(0.0, offer);
        if (used > 0.0) {
            tile.setAmount(offer);
            ((IEnergySource)tile.getMainTile()).drawEnergy(used);
        }
    }
    
    private static double distributeSingle(double offer, final Tile tile, final List<EnergyPath> paths, final int pathOffset, final GridData data, final int calcId) {
        for (int i = pathOffset; i < paths.size(); ++i) {
            offer -= emit(paths.get(i), offer, data, calcId);
            if (offer <= 0.0) {
                break;
            }
        }
        for (int i = 0; i < pathOffset && offer > 0.0; offer -= emit(paths.get(i), offer, data, calcId), ++i) {}
        return offer;
    }
    
    private static double distributeMultiple(double offer, final Tile tile, final List<EnergyPath> paths, final int pathOffset, final GridData data, final int calcId, int packetCount) {
        final IEnergySource source = (IEnergySource)tile.getMainTile();
        final double power = EnergyNet.instance.getPowerFromTier(source.getSourceTier());
        do {
            final double cOffer = Math.min(offer, power);
            final double used = cOffer - distributeSingle(cOffer, tile, paths, pathOffset, data, calcId);
            if (used <= 0.0) {
                break;
            }
            offer -= used;
        } while (--packetCount > 0 && offer > 0.0);
        return offer;
    }
    
    private static double emit(final EnergyPath path, final double offer, final GridData data, final int calcId) {
        final Tile targetTile = path.target.getTile();
        if (targetTile.isDisabled()) {
            return 0.0;
        }
        final double injectAmount = offer - path.loss;
        if (injectAmount <= 0.0) {
            return 0.0;
        }
        final MutableDouble sinkDemand = data.activeSinks.get(path.target);
        if (sinkDemand == null) {
            return 0.0;
        }
        final IEnergySink sink = (IEnergySink)targetTile.getMainTile();
        final double amount = Math.min(injectAmount, sinkDemand.doubleValue());
        final double rejected = sink.injectEnergy(path.targetDirection, amount, EnergyNet.instance.getTierFromPower(amount));
        if (rejected >= amount) {
            return 0.0;
        }
        final double effectiveAmount = Math.max(0.0, amount - rejected + path.loss);
        if (path.lastCalcId != calcId) {
            path.lastCalcId = calcId;
            path.energySupplied = 0.0;
            path.maxPacketConducted = 0.0;
        }
        path.energySupplied += amount - rejected;
        path.maxPacketConducted = Math.max(effectiveAmount, path.maxPacketConducted);
        if (effectiveAmount > path.minEffectEnergy || amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier())) {
            data.eventPaths.add(path);
        }
        if (amount >= sinkDemand.doubleValue() || rejected > 0.0) {
            data.activeSinks.remove(path.target);
        }
        return effectiveAmount;
    }
    
    private static void applyCableEffects(final Collection<EnergyPath> eventPaths, final World world) {
        if (!MainConfig.get().get("misc/enableEnetCableMeltdown").getBool()) {
            return;
        }
        final Set<Tile> cablesToRemove = Collections.newSetFromMap(new IdentityHashMap<Tile, Boolean>());
        final Set<Tile> cablesToStrip = Collections.newSetFromMap(new IdentityHashMap<Tile, Boolean>());
        final Map<Tile, MutableDouble> sinksToExplode = new IdentityHashMap<Tile, MutableDouble>();
        final Map<EntityLivingBase, MutableDouble> shockEnergyMap = new IdentityHashMap<EntityLivingBase, MutableDouble>();
        for (final EnergyPath path : eventPaths) {
            final double amount = path.maxPacketConducted;
            boolean conductorOverload = false;
            if (amount > path.minConductorBreakdownEnergy || amount > path.minInsulationBreakdownEnergy) {
                conductorOverload = true;
                for (final Node node : path.conductors) {
                    final Tile tile = node.getTile();
                    final IEnergyConductor conductor = (IEnergyConductor)tile.getMainTile();
                    if (amount > conductor.getConductorBreakdownEnergy()) {
                        cablesToRemove.add(tile);
                    }
                    else {
                        if (amount <= conductor.getInsulationBreakdownEnergy()) {
                            continue;
                        }
                        cablesToStrip.add(tile);
                    }
                }
            }
            if (amount > path.minInsulationEnergyAbsorption) {
                final List<EntityLivingBase> nearbyEntities = world.getEntitiesWithinAABB((Class)EntityLivingBase.class, new AxisAlignedBB((double)(path.minX - 1), (double)(path.minY - 1), (double)(path.minZ - 1), (double)(path.maxX + 2), (double)(path.maxY + 2), (double)(path.maxZ + 2)));
                if (!nearbyEntities.isEmpty()) {
                    final Map<EntityLivingBase, MutableDouble> localShockEnergyMap = new IdentityHashMap<EntityLivingBase, MutableDouble>();
                    for (final Node node2 : path.conductors) {
                        final Tile tile2 = node2.getTile();
                        final IEnergyConductor conductor2 = (IEnergyConductor)tile2.getMainTile();
                        if (amount <= conductor2.getInsulationEnergyAbsorption()) {
                            continue;
                        }
                        final int shockEnergy = (int)(amount - conductor2.getInsulationEnergyAbsorption());
                        for (final IEnergyTile subTile : tile2.getSubTiles()) {
                            final BlockPos pos = EnergyNet.instance.getPos(subTile);
                            for (final EntityLivingBase entity : nearbyEntities) {
                                final MutableDouble prev = localShockEnergyMap.get(entity);
                                if (prev != null && prev.doubleValue() >= shockEnergy) {
                                    continue;
                                }
                                if (!entity.getEntityBoundingBox().intersects(new AxisAlignedBB((double)(pos.getX() - 1), (double)(pos.getY() - 1), (double)(pos.getZ() - 1), (double)(pos.getX() + 2), (double)(pos.getY() + 2), (double)(pos.getZ() + 2)))) {
                                    continue;
                                }
                                if (prev == null) {
                                    localShockEnergyMap.put(entity, new MutableDouble((double)shockEnergy));
                                }
                                else {
                                    prev.setValue((double)shockEnergy);
                                }
                            }
                        }
                    }
                    for (final Map.Entry<EntityLivingBase, MutableDouble> entry : localShockEnergyMap.entrySet()) {
                        final MutableDouble prev2 = shockEnergyMap.get(entry.getKey());
                        if (prev2 == null) {
                            shockEnergyMap.put(entry.getKey(), entry.getValue());
                        }
                        else {
                            prev2.add(entry.getValue().doubleValue());
                        }
                    }
                }
            }
            final Tile sinkTile = path.target.getTile();
            final IEnergySink sink = (IEnergySink)sinkTile.getMainTile();
            if (!conductorOverload && amount > EnergyNet.instance.getPowerFromTier(sink.getSinkTier())) {
                final MutableDouble prev3 = sinksToExplode.get(sinkTile);
                if (prev3 == null) {
                    sinksToExplode.put(sinkTile, new MutableDouble(amount));
                }
                else {
                    if (prev3.doubleValue() >= amount) {
                        continue;
                    }
                    prev3.setValue(amount);
                }
            }
        }
        cablesToStrip.removeAll(cablesToRemove);
        for (final Tile tile3 : cablesToRemove) {
            ((IEnergyConductor)tile3.getMainTile()).removeConductor();
        }
        for (final Tile tile3 : cablesToStrip) {
            ((IEnergyConductor)tile3.getMainTile()).removeInsulation();
        }
        for (final Map.Entry<Tile, MutableDouble> entry2 : sinksToExplode.entrySet()) {
            explodeTile(world, entry2.getKey(), entry2.getValue().doubleValue());
        }
        for (final Map.Entry<EntityLivingBase, MutableDouble> entry3 : shockEnergyMap.entrySet()) {
            final EntityLivingBase target = entry3.getKey();
            final int damage = (int)Math.ceil(entry3.getValue().doubleValue() / 64.0);
            if (target.isEntityAlive() && damage > 0) {
                target.attackEntityFrom((DamageSource)IC2DamageSource.electricity, (float)damage);
            }
        }
    }
    
    private static GridData getData(final Grid grid) {
        GridData ret = grid.getData();
        if (ret == null) {
            ret = new GridData();
            grid.setData(ret);
        }
        return ret;
    }
    
    private static void explodeTile(final World world, final Tile tile, final double maxPower) {
        if (!MainConfig.get().get("misc/enableEnetExplosions").getBool()) {
            return;
        }
        final int tier = EnergyNet.instance.getTierFromPower(maxPower);
        for (final IEnergyTile subTile : tile.getSubTiles()) {
            final IEnergySink mainTile = (IEnergySink)tile.getMainTile();
            final BlockPos pos = EnergyNet.instance.getPos(subTile);
            final TileEntity realTe = world.getTileEntity(pos);
            if (!(mainTile instanceof IOverloadHandler) || !((IOverloadHandler)mainTile).onOverload(tier)) {
                if (realTe instanceof IOverloadHandler && ((IOverloadHandler)realTe).onOverload(tier)) {
                    continue;
                }
                float power = 2.5f;
                if (mainTile instanceof IExplosionPowerOverride) {
                    final IExplosionPowerOverride override = (IExplosionPowerOverride)mainTile;
                    if (!override.shouldExplode()) {
                        continue;
                    }
                    power = override.getExplosionPower(tier, power);
                }
                else if (realTe instanceof IExplosionPowerOverride) {
                    final IExplosionPowerOverride override = (IExplosionPowerOverride)realTe;
                    if (!override.shouldExplode()) {
                        continue;
                    }
                    power = override.getExplosionPower(tier, power);
                }
                final EntityPlayer closestPlayer = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20.0, false);
                if (closestPlayer != null) {
                    IC2.achievements.issueAchievement(closestPlayer, "explodeMachine");
                }
                world.setBlockToAir(pos);
                final ExplosionIC2 explosion = new ExplosionIC2(world, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, 0.75f, ExplosionIC2.Type.Electrical);
                explosion.doExplosion();
            }
        }
    }
    
    private static class GridData
    {
        boolean active;
        final Map<Node, List<EnergyPath>> energySourceToEnergyPathMap;
        final List<Node> activeSources;
        final Map<Node, MutableDouble> activeSinks;
        final Set<EnergyPath> eventPaths;
        final Map<Node, List<EnergyPath>> pathCache;
        int currentCalcId;
        
        private GridData() {
            this.energySourceToEnergyPathMap = new IdentityHashMap<Node, List<EnergyPath>>();
            this.activeSources = new ArrayList<Node>();
            this.activeSinks = new IdentityHashMap<Node, MutableDouble>();
            this.eventPaths = Collections.newSetFromMap(new IdentityHashMap<EnergyPath, Boolean>());
            this.pathCache = new IdentityHashMap<Node, List<EnergyPath>>();
            this.currentCalcId = -1;
        }
    }
}
