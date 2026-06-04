// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.api.recipe.IRecipeInput;

public class RecipeInputMultiple extends RecipeInputBase implements IRecipeInput
{
    private final IRecipeInput[] inputs;
    
    RecipeInputMultiple(final IRecipeInput... inputs) {
        this.inputs = inputs;
    }
    
    RecipeInputMultiple(final List<IRecipeInput> inputs) {
        this.inputs = inputs.toArray(new IRecipeInput[0]);
    }
    
    @Override
    public boolean matches(final ItemStack subject) {
        for (final IRecipeInput input : this.inputs) {
            if (input.matches(subject)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int getAmount() {
        return 1;
    }
    
    @Override
    public List<ItemStack> getInputs() {
        final List<ItemStack> list = new ArrayList<ItemStack>();
        for (final IRecipeInput input : this.inputs) {
            list.addAll(input.getInputs());
        }
        return Collections.unmodifiableList((List<? extends ItemStack>)list);
    }
    
    public String toString() {
        if (this.inputs.length <= 0) {
            return "RecipeInputMultiple<Nothing>";
        }
        final StringBuilder b = new StringBuilder("RecipeInputMultiple<");
        int i = 0;
        final int end = this.inputs.length - 1;
        while (true) {
            b.append(this.inputs[i].toString());
            if (i == end) {
                break;
            }
            b.append(", ");
            ++i;
        }
        return b.append('>').toString();
    }
    
    public boolean equals(final Object obj) {
        if (obj != null && this.getClass() == obj.getClass()) {
            final IRecipeInput[] otherInputs = ((RecipeInputMultiple)obj).inputs;
            if (this.inputs.length == otherInputs.length) {
                for (int i = 0; i < this.inputs.length; ++i) {
                    if (!this.inputs[i].equals(otherInputs[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
