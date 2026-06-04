// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;

public class ItemArmorCFPack extends ItemArmorFluidTank
{
    public ItemArmorCFPack() {
        super(ItemName.cf_pack, "batpack", FluidName.construction_foam.getInstance(), 80000);
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        ItemStack stack = new ItemStack((Item)this, 1);
        this.filltank(stack);
        stack.setItemDamage(1);
        subItems.add((Object)stack);
        stack = new ItemStack((Item)this, 1);
        stack.setItemDamage(this.getMaxDamage(stack));
        subItems.add((Object)stack);
    }
}
