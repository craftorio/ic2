package ic2.core.item.armor;

import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemArmorCFPack extends ItemArmorFluidTank
{
	public ItemArmorCFPack()
	{
		super(ItemName.cf_pack, "batpack", FluidName.construction_foam.getInstance(), 80000);
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab))
		{
			ItemStack stack = new ItemStack(this, 1);
			this.filltank(stack);
			stack.setItemDamage(1);
			subItems.add(stack);
			stack = new ItemStack(this, 1);
			stack.setItemDamage(this.getMaxDamage(stack));
			subItems.add(stack);
		}
	}
}
