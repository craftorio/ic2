// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy;

import java.io.PrintStream;
import ic2.api.info.ILocatable;
import net.minecraft.tileentity.TileEntity;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEnergyNet
{
    IEnergyTile getTile(final World p0, final BlockPos p1);
    
    IEnergyTile getSubTile(final World p0, final BlockPos p1);
    
     <T extends TileEntity & IEnergyTile> void addTile(final T p0);
    
     <T extends ILocatable & IEnergyTile> void addTile(final T p0);
    
    void removeTile(final IEnergyTile p0);
    
    World getWorld(final IEnergyTile p0);
    
    BlockPos getPos(final IEnergyTile p0);
    
    NodeStats getNodeStats(final IEnergyTile p0);
    
    boolean dumpDebugInfo(final World p0, final BlockPos p1, final PrintStream p2, final PrintStream p3);
    
    double getPowerFromTier(final int p0);
    
    int getTierFromPower(final double p0);
    
    void registerEventReceiver(final IEnergyNetEventReceiver p0);
    
    void unregisterEventReceiver(final IEnergyNetEventReceiver p0);
}
