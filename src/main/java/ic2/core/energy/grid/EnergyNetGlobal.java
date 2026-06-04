// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import java.util.concurrent.CopyOnWriteArrayList;
import ic2.core.WorldData;
import java.io.PrintStream;
import ic2.api.energy.NodeStats;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.api.info.ILocatable;
import net.minecraft.tileentity.TileEntity;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.core.energy.leg.EnergyCalculatorLeg;
import ic2.api.energy.IEnergyNetEventReceiver;
import java.util.List;
import ic2.api.energy.IEnergyNet;

public class EnergyNetGlobal implements IEnergyNet
{
    private static final List<IEnergyNetEventReceiver> eventReceivers;
    private static IEnergyCalculator calculator;
    
    public static EnergyNetGlobal create() {
        if (System.getProperty("IC2ExpEnet") != null) {}
        EnergyNetGlobal.calculator = new EnergyCalculatorLeg();
        EventHandler.init();
        return new EnergyNetGlobal();
    }
    
    private EnergyNetGlobal() {
    }
    
    @Override
    public IEnergyTile getTile(final World world, final BlockPos pos) {
        if (world == null) {
            throw new NullPointerException("null world");
        }
        if (pos == null) {
            throw new NullPointerException("null pos");
        }
        return getLocal(world).getIoTile(pos);
    }
    
    @Override
    public IEnergyTile getSubTile(final World world, final BlockPos pos) {
        if (world == null) {
            throw new NullPointerException("null world");
        }
        if (pos == null) {
            throw new NullPointerException("null pos");
        }
        return getLocal(world).getSubTile(pos);
    }
    
    @Override
    public <T extends TileEntity & IEnergyTile> void addTile(final T tile) {
        if (tile == null) {
            throw new NullPointerException("null tile");
        }
        addTile(tile, tile.getWorld(), tile.getPos());
    }
    
    @Override
    public <T extends ILocatable & IEnergyTile> void addTile(final T tile) {
        if (tile == null) {
            throw new NullPointerException("null tile");
        }
        addTile(tile, tile.getWorldObj(), tile.getPosition());
    }
    
    private static void addTile(final IEnergyTile tile, final World world, final BlockPos pos) {
        if (EnergyNetSettings.logEnetApiAccessTraces) {
            IC2.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API addTile %s.", Util.toString(tile, (IBlockAccess)world, pos));
        }
        else if (EnergyNetSettings.logEnetApiAccesses) {
            IC2.log.debug(LogCategory.EnergyNet, "API addTile %s.", Util.toString(tile, (IBlockAccess)world, pos));
        }
        getLocal(world).addTile(tile, pos);
    }
    
    @Override
    public void removeTile(final IEnergyTile tile) {
        if (tile == null) {
            throw new NullPointerException("null tile");
        }
        final World world = this.getWorld(tile);
        final BlockPos pos = this.getPos(tile);
        if (EnergyNetSettings.logEnetApiAccessTraces) {
            IC2.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API removeTile %s.", Util.toString(tile, (IBlockAccess)world, pos));
        }
        else if (EnergyNetSettings.logEnetApiAccesses) {
            IC2.log.debug(LogCategory.EnergyNet, "API removeTile %s.", Util.toString(tile, (IBlockAccess)world, pos));
        }
        getLocal(world).removeTile(tile, pos);
    }
    
    @Override
    public World getWorld(final IEnergyTile tile) {
        if (tile == null) {
            throw new NullPointerException("null tile");
        }
        if (tile instanceof ILocatable) {
            return ((ILocatable)tile).getWorldObj();
        }
        if (tile instanceof TileEntity) {
            return ((TileEntity)tile).getWorld();
        }
        throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
    }
    
    @Override
    public BlockPos getPos(final IEnergyTile tile) {
        if (tile == null) {
            throw new NullPointerException("null tile");
        }
        if (tile instanceof ILocatable) {
            return ((ILocatable)tile).getPosition();
        }
        if (tile instanceof TileEntity) {
            return ((TileEntity)tile).getPos();
        }
        throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
    }
    
    @Override
    public NodeStats getNodeStats(final IEnergyTile tile) {
        return getLocal(this.getWorld(tile)).getNodeStats(tile);
    }
    
    @Override
    public boolean dumpDebugInfo(final World world, final BlockPos pos, final PrintStream console, final PrintStream chat) {
        return getLocal(world).dumpDebugInfo(pos, console, chat);
    }
    
    @Override
    public double getPowerFromTier(final int tier) {
        if (tier < 14) {
            return 8 << tier * 2;
        }
        if (tier < 30) {
            return 8.0 * Math.pow(4.0, tier);
        }
        return 9.223372036854776E18;
    }
    
    @Override
    public int getTierFromPower(final double power) {
        if (power <= 0.0) {
            return 0;
        }
        return (int)Math.ceil(Math.log(power / 8.0) / Math.log(4.0));
    }
    
    @Override
    public synchronized void registerEventReceiver(final IEnergyNetEventReceiver receiver) {
        if (EnergyNetGlobal.eventReceivers.contains(receiver)) {
            return;
        }
        EnergyNetGlobal.eventReceivers.add(receiver);
    }
    
    @Override
    public synchronized void unregisterEventReceiver(final IEnergyNetEventReceiver receiver) {
        EnergyNetGlobal.eventReceivers.remove(receiver);
    }
    
    static Iterable<IEnergyNetEventReceiver> getEventReceivers() {
        return EnergyNetGlobal.eventReceivers;
    }
    
    static IEnergyCalculator getCalculator() {
        return EnergyNetGlobal.calculator;
    }
    
    public static EnergyNetLocal getLocal(final World world) {
        if (world.isRemote) {
            throw new IllegalStateException("not applicable clientside");
        }
        assert world.getMinecraftServer().isCallingFromMinecraftThread();
        return WorldData.get(world).energyNet;
    }
    
    static {
        eventReceivers = new CopyOnWriteArrayList<IEnergyNetEventReceiver>();
    }
}
