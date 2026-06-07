package ic2.core.item.armor;

import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.ref.Ic2Fluids;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemArmorCFPack extends ItemArmorFluidTank
{
	public ItemArmorCFPack(Properties settings)
	{
		super(Ic2ArmorMaterials.CF_PACK, settings, Ic2Fluids.CONSTRUCTION_FOAM.still, 80000);
	}

	public void m_6787_(CreativeModeTab tab, NonNullList<ItemStack> subItems)
	{
		if (this.m_220152_(tab))
		{
			ItemStack stack = new ItemStack(this);
			this.filltank(stack);
			subItems.add(stack);
			subItems.add(new ItemStack(this));
		}
	}
}
