package ic2.api.energy;

import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IEnergyNet
{
	IEnergyTile getTile(Level var1, BlockPos var2);

	IEnergyTile getSubTile(Level var1, BlockPos var2);

	<T extends BlockEntity & IEnergyTile> void addBlockEntityTile(T var1);

	<T extends ILocatable & IEnergyTile> void addLocatableTile(T var1);

	default void addTileUnchecked(IEnergyTile tile)
	{
		if (tile instanceof BlockEntity)
		{
			this.addBlockEntityTile((BlockEntity & IEnergyTile) tile);
		} else
		{
			if (!(tile instanceof ILocatable))
			{
				throw new IllegalArgumentException("invalid tile type: " + tile.getClass().getName());
			}

			this.addLocatableTile((ILocatable & IEnergyTile) tile);
		}
	}

	void removeTile(IEnergyTile var1);

	Level getWorld(IEnergyTile var1);

	BlockPos getPos(IEnergyTile var1);

	NodeStats getNodeStats(IEnergyTile var1);

	int getAdjacentConnections(IEnergyTile var1);

	double getPowerFromTier(int var1);

	int getTierFromPower(double var1);

	void registerEventReceiver(IEnergyNetEventReceiver var1);

	void unregisterEventReceiver(IEnergyNetEventReceiver var1);
}
