package ic2.api.energy;

import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;

import java.io.PrintStream;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEnergyNet
{
	IEnergyTile getTile(World var1, BlockPos var2);

	IEnergyTile getSubTile(World var1, BlockPos var2);

	<T extends TileEntity & IEnergyTile> void addTile(T var1);

	<T extends ILocatable & IEnergyTile> void addTile(T var1);

	void removeTile(IEnergyTile var1);

	World getWorld(IEnergyTile var1);

	BlockPos getPos(IEnergyTile var1);

	NodeStats getNodeStats(IEnergyTile var1);

	boolean dumpDebugInfo(World var1, BlockPos var2, PrintStream var3, PrintStream var4);

	double getPowerFromTier(int var1);

	int getTierFromPower(double var1);

	void registerEventReceiver(IEnergyNetEventReceiver var1);

	void unregisterEventReceiver(IEnergyNetEventReceiver var1);
}
