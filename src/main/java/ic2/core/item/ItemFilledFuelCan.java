// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.util.StackUtil;
import ic2.core.item.type.CraftingItemType;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemFilledFuelCan extends ItemIC2
{
    public ItemFilledFuelCan() {
        super(ItemName.filled_fuel_can);
        this.setMaxStackSize(1);
    }
    
    public boolean hasContainerItem(final ItemStack stack) {
        return true;
    }
    
    public ItemStack getContainerItem(final ItemStack stack) {
        return ItemName.crafting.getItemStack(CraftingItemType.empty_fuel_can);
    }
    
    public int getItemBurnTime(final ItemStack stack) {
        return StackUtil.getOrCreateNbtData(stack).getInteger("value") * 2;
    }
}
