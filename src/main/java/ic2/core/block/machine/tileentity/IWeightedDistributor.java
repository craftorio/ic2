package ic2.core.block.machine.tileentity;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;

public interface IWeightedDistributor extends Container
{
	Direction getFacing();

	List<Direction> getPriority();

	void updatePriority(boolean var1);
}
