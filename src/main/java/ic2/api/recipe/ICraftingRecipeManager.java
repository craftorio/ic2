// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraft.item.ItemStack;

public interface ICraftingRecipeManager
{
    void addRecipe(final ItemStack p0, final Object... p1);
    
    void addShapelessRecipe(final ItemStack p0, final Object... p1);
    
    public static class AttributeContainer
    {
        public final boolean hidden;
        public final boolean consuming;
        public final boolean fixedSize;
        
        public AttributeContainer(final boolean hidden, final boolean consuming) {
            this(hidden, consuming, false);
        }
        
        public AttributeContainer(final boolean hidden, final boolean consuming, final boolean fixedSize) {
            this.hidden = hidden;
            this.consuming = consuming;
            this.fixedSize = fixedSize;
        }
    }
}
