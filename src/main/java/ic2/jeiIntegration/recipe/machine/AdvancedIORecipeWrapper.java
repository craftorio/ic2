// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.IRecipeInput;

public class AdvancedIORecipeWrapper extends IORecipeWrapper
{
    private final IRecipeInput secondary;
    
    AdvancedIORecipeWrapper(final MachineRecipe<IRecipeInput, Collection<ItemStack>> container, final IRecipeInput input, final IORecipeCategory<?> category) {
        super(container, category);
        this.secondary = input;
    }
    
    @Override
    public List<List<ItemStack>> getInputs() {
        final List<List<ItemStack>> list = new ArrayList<List<ItemStack>>(2);
        list.addAll(super.getInputs());
        list.add(this.secondary.getInputs());
        return list;
    }
}
