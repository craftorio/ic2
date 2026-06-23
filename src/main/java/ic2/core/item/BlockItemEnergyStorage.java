package ic2.core.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class BlockItemEnergyStorage extends BlockItem
{
	public final int maxEnergy;

	public BlockItemEnergyStorage(Block block, Properties properties, int maxEnergy)
	{
		super(block, properties);
		this.maxEnergy = maxEnergy;
	}
}
