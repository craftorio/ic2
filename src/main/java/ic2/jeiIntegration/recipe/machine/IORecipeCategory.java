// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.gui.IDrawable;
import ic2.core.block.TeBlockRegistry;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.item.ItemStack;
import mezz.jei.api.ingredients.IIngredients;
import ic2.jeiIntegration.SlotPosition;
import java.util.List;
import net.minecraft.client.Minecraft;
import ic2.core.block.ITeBlock;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeCategory;

public abstract class IORecipeCategory<T> implements IRecipeCategory<IRecipeWrapper>
{
    protected final ITeBlock block;
    final T recipeManager;
    
    public IORecipeCategory(final ITeBlock block, final T recipeManager) {
        this.block = block;
        this.recipeManager = recipeManager;
    }
    
    public String getUid() {
        return this.block.getName();
    }
    
    public String getTitle() {
        return this.getBlockStack().getDisplayName();
    }
    
    public void drawExtras(final Minecraft minecraft) {
    }
    
    protected abstract List<SlotPosition> getInputSlotPos();
    
    protected abstract List<SlotPosition> getOutputSlotPos();
    
    protected List<List<ItemStack>> getInputStacks(final IIngredients ingredients) {
        return ingredients.getInputs((Class)ItemStack.class);
    }
    
    protected List<List<ItemStack>> getOutputStacks(final IIngredients ingredients) {
        return ingredients.getOutputs((Class)ItemStack.class);
    }
    
    public void setRecipe(final IRecipeLayout recipeLayout, final IRecipeWrapper recipeWrapper, final IIngredients ingredients) {
        final IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        final List<SlotPosition> inputSlots = this.getInputSlotPos();
        final List<List<ItemStack>> inputStacks = this.getInputStacks(ingredients);
        int idx;
        for (idx = 0; idx < inputSlots.size(); ++idx) {
            final SlotPosition pos = inputSlots.get(idx);
            itemStacks.init(idx, true, pos.getX(), pos.getY());
            if (idx < inputStacks.size()) {
                itemStacks.set(idx, (List)inputStacks.get(idx));
            }
        }
        final List<SlotPosition> outputSlots = this.getOutputSlotPos();
        final List<List<ItemStack>> outputStacks = this.getOutputStacks(ingredients);
        for (int i = 0; i < outputSlots.size(); ++i, ++idx) {
            final SlotPosition pos2 = outputSlots.get(i);
            itemStacks.init(idx, false, pos2.getX(), pos2.getY());
            if (i < outputStacks.size()) {
                itemStacks.set(idx, (List)outputStacks.get(i));
            }
        }
    }
    
    public ItemStack getBlockStack() {
        return TeBlockRegistry.get(this.block.getIdentifier()).getItemStack(this.block);
    }
    
    public IDrawable getIcon() {
        return null;
    }
    
    public String getModName() {
        return "ic2";
    }
}
