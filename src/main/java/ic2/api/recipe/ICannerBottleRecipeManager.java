// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraft.item.ItemStack;

public interface ICannerBottleRecipeManager extends IMachineRecipeManager<Input, ItemStack, RawInput>
{
    boolean addRecipe(final IRecipeInput p0, final IRecipeInput p1, final ItemStack p2, final boolean p3);
    
    @Deprecated
    void addRecipe(final IRecipeInput p0, final IRecipeInput p1, final ItemStack p2);
    
    @Deprecated
    RecipeOutput getOutputFor(final ItemStack p0, final ItemStack p1, final boolean p2, final boolean p3);
    
    public static class Input
    {
        public final IRecipeInput container;
        public final IRecipeInput fill;
        
        public Input(final IRecipeInput container, final IRecipeInput fill) {
            this.container = container;
            this.fill = fill;
        }
        
        public boolean matches(final ItemStack container, final ItemStack fill) {
            return this.container.matches(container) && this.fill.matches(fill);
        }
    }
    
    public static class RawInput
    {
        public final ItemStack container;
        public final ItemStack fill;
        
        public RawInput(final ItemStack container, final ItemStack fill) {
            this.container = container;
            this.fill = fill;
        }
    }
}
