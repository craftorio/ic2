package ic2.data.loot_tables.generator;

import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public class EnergyBlockLootTableGenerator extends Ic2BlockLootTableGenerator
{
	@Override
	public Ic2BlockLootTableGenerator build()
	{
		BuiltInRegistries.BLOCK.forEach(block ->
		{
			if (block instanceof Ic2TileEntityBlock tileEntityBlock)
			{
				Ic2TileEntity tileEntity = tileEntityBlock.getDummyTe();
				ItemStack stack = tileEntity.adjustDrop(block.asItem().getDefaultInstance(), false);
				if (stack != null)
				{
					if (stack.hasTag())
					{
						this.addDropWithNbt(block);
					} else
					{
						this.addDrop(block, stack.getItem());
					}
				}
			}
		});
		return this;
	}
}
