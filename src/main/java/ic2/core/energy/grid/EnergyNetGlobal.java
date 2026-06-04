package ic2.core.energy.grid;

import ic2.api.energy.IEnergyNet;
import ic2.api.energy.IEnergyNetEventReceiver;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import ic2.core.IC2;
import ic2.core.WorldData;
import ic2.core.energy.leg.EnergyCalculatorLeg;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class EnergyNetGlobal implements IEnergyNet {
  public static EnergyNetGlobal create() {
    if (System.getProperty("IC2ExpEnet") != null);
    calculator = (IEnergyCalculator)new EnergyCalculatorLeg();
    EventHandler.init();
    return new EnergyNetGlobal();
  }
  
  public IEnergyTile getTile(World world, BlockPos pos) {
    if (world == null)
      throw new NullPointerException("null world"); 
    if (pos == null)
      throw new NullPointerException("null pos"); 
    return getLocal(world).getIoTile(pos);
  }
  
  public IEnergyTile getSubTile(World world, BlockPos pos) {
    if (world == null)
      throw new NullPointerException("null world"); 
    if (pos == null)
      throw new NullPointerException("null pos"); 
    return getLocal(world).getSubTile(pos);
  }
  
  public <T extends TileEntity & IEnergyTile> void addTile(T tile) {
    if (tile == null)
      throw new NullPointerException("null tile"); 
    addTile((IEnergyTile)tile, tile.getWorld(), tile.getPos());
  }
  
  public <T extends ILocatable & IEnergyTile> void addTile(T tile) {
    if (tile == null)
      throw new NullPointerException("null tile"); 
    addTile((IEnergyTile)tile, tile.getWorldObj(), tile.getPosition());
  }
  
  private static void addTile(IEnergyTile tile, World world, BlockPos pos) {
    if (EnergyNetSettings.logEnetApiAccessTraces) {
      IC2.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API addTile %s.", new Object[] { Util.toString(tile, (IBlockAccess)world, pos) });
    } else if (EnergyNetSettings.logEnetApiAccesses) {
      IC2.log.debug(LogCategory.EnergyNet, "API addTile %s.", new Object[] { Util.toString(tile, (IBlockAccess)world, pos) });
    } 
    getLocal(world).addTile(tile, pos);
  }
  
  public void removeTile(IEnergyTile tile) {
    if (tile == null)
      throw new NullPointerException("null tile"); 
    World world = getWorld(tile);
    BlockPos pos = getPos(tile);
    if (EnergyNetSettings.logEnetApiAccessTraces) {
      IC2.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API removeTile %s.", new Object[] { Util.toString(tile, (IBlockAccess)world, pos) });
    } else if (EnergyNetSettings.logEnetApiAccesses) {
      IC2.log.debug(LogCategory.EnergyNet, "API removeTile %s.", new Object[] { Util.toString(tile, (IBlockAccess)world, pos) });
    } 
    getLocal(world).removeTile(tile, pos);
  }
  
  public World getWorld(IEnergyTile tile) {
    if (tile == null)
      throw new NullPointerException("null tile"); 
    if (tile instanceof ILocatable)
      return ((ILocatable)tile).getWorldObj(); 
    if (tile instanceof TileEntity)
      return ((TileEntity)tile).getWorld(); 
    throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
  }
  
  public BlockPos getPos(IEnergyTile tile) {
    if (tile == null)
      throw new NullPointerException("null tile"); 
    if (tile instanceof ILocatable)
      return ((ILocatable)tile).getPosition(); 
    if (tile instanceof TileEntity)
      return ((TileEntity)tile).getPos(); 
    throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
  }
  
  public NodeStats getNodeStats(IEnergyTile tile) {
    return getLocal(getWorld(tile)).getNodeStats(tile);
  }
  
  public boolean dumpDebugInfo(World world, BlockPos pos, PrintStream console, PrintStream chat) {
    return getLocal(world).dumpDebugInfo(pos, console, chat);
  }
  
  public double getPowerFromTier(int tier) {
    if (tier < 14)
      return (8 << tier * 2); 
    if (tier < 30)
      return 8.0D * Math.pow(4.0D, tier); 
    return 9.223372036854776E18D;
  }
  
  public int getTierFromPower(double power) {
    if (power <= 0.0D)
      return 0; 
    return (int)Math.ceil(Math.log(power / 8.0D) / Math.log(4.0D));
  }
  
  public synchronized void registerEventReceiver(IEnergyNetEventReceiver receiver) {
    if (eventReceivers.contains(receiver))
      return; 
    eventReceivers.add(receiver);
  }
  
  public synchronized void unregisterEventReceiver(IEnergyNetEventReceiver receiver) {
    eventReceivers.remove(receiver);
  }
  
  static Iterable<IEnergyNetEventReceiver> getEventReceivers() {
    return eventReceivers;
  }
  
  static IEnergyCalculator getCalculator() {
    return calculator;
  }
  
  public static EnergyNetLocal getLocal(World world) {
    if (world.isRemote)
      throw new IllegalStateException("not applicable clientside"); 
    assert world.getMinecraftServer().isCallingFromMinecraftThread();
    return (WorldData.get(world)).energyNet;
  }
  
  private static final List<IEnergyNetEventReceiver> eventReceivers = new CopyOnWriteArrayList<>();
  
  private static IEnergyCalculator calculator;
}
