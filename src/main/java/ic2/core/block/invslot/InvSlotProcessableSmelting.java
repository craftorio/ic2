// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.api.recipe.Recipes;
import ic2.core.block.IInventorySlotHolder;
import net.minecraft.item.ItemStack;

public class InvSlotProcessableSmelting extends InvSlotProcessable<ItemStack, ItemStack, ItemStack>
{
    public InvSlotProcessableSmelting(final IInventorySlotHolder<?> base, final String name, final int count) {
        super(base, name, count, Recipes.furnace);
    }
    
    @Override
    protected ItemStack getInput(final ItemStack stack) {
        return stack;
    }
    
    @Override
    protected void setInput(final ItemStack input) {
        this.put(input);
    }
}
