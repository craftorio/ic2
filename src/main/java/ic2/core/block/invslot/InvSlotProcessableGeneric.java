// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

public class InvSlotProcessableGeneric extends InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    public InvSlotProcessableGeneric(final IInventorySlotHolder<?> base, final String name, final int count, final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeManager) {
        super(base, name, count, recipeManager);
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
