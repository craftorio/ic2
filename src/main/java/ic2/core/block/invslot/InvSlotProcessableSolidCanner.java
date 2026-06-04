// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.ICannerBottleRecipeManager;

public class InvSlotProcessableSolidCanner extends InvSlotProcessable<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput>
{
    public InvSlotProcessableSolidCanner(final TileEntitySolidCanner base1, final String name1, final int count) {
        super(base1, name1, count, Recipes.cannerBottle);
    }
    
    @Override
    protected ICannerBottleRecipeManager.RawInput getInput(final ItemStack stack) {
        return new ICannerBottleRecipeManager.RawInput(((TileEntitySolidCanner)this.base).canInputSlot.get(), stack);
    }
    
    @Override
    protected void setInput(final ICannerBottleRecipeManager.RawInput input) {
        ((TileEntitySolidCanner)this.base).canInputSlot.put(input.container);
        this.put(input.fill);
    }
}
