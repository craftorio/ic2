package ic2.core.item;

import net.minecraft.world.level.block.Block;

public class BlockItemEnergyStorage extends ItemBlockIc2
{
	public final int maxEnergy;

	public BlockItemEnergyStorage(Block block, Properties properties, int maxEnergy)
	{
		super(block, properties);
		this.maxEnergy = maxEnergy;
	}
}
