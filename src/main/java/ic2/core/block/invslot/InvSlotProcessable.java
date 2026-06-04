// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.api.recipe.MachineRecipeResult;
import ic2.core.util.StackUtil;
import ic2.core.item.upgrade.ItemUpgradeModule;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.IMachineRecipeManager;

public abstract class InvSlotProcessable<RI, RO, I> extends InvSlotConsumable
{
    protected IMachineRecipeManager<RI, RO, I> recipeManager;
    
    public InvSlotProcessable(final IInventorySlotHolder<?> base, final String name, final int count, final IMachineRecipeManager<RI, RO, I> recipeManager) {
        super(base, name, count);
        this.recipeManager = recipeManager;
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        if (stack.getItem() instanceof ItemUpgradeModule) {
            return false;
        }
        final ItemStack tmp = StackUtil.copyWithSize(stack, Integer.MAX_VALUE);
        return this.getOutputFor(this.getInput(tmp), true) != null;
    }
    
    public MachineRecipeResult<RI, RO, I> process() {
        final ItemStack input = this.get();
        if (StackUtil.isEmpty(input) && !this.allowEmptyInput()) {
            return null;
        }
        return this.getOutputFor(this.getInput(input), false);
    }
    
    public void consume(final MachineRecipeResult<RI, RO, I> result) {
        if (result == null) {
            throw new NullPointerException("null result");
        }
        final ItemStack input = this.get();
        if (StackUtil.isEmpty(input) && !this.allowEmptyInput()) {
            throw new IllegalStateException("consume from empty slot");
        }
        this.setInput(result.getAdjustedInput());
    }
    
    public void setRecipeManager(final IMachineRecipeManager<RI, RO, I> recipeManager) {
        this.recipeManager = recipeManager;
    }
    
    protected boolean allowEmptyInput() {
        return false;
    }
    
    protected MachineRecipeResult<RI, RO, I> getOutputFor(final I input, final boolean forAccept) {
        return this.recipeManager.apply(input, forAccept);
    }
    
    protected abstract I getInput(final ItemStack p0);
    
    protected abstract void setInput(final I p0);
}
