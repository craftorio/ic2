// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import java.util.Arrays;
import java.util.List;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IRecipeInput;

public class RecipeInputItemStack extends RecipeInputBase implements IRecipeInput
{
    public final ItemStack input;
    public final int amount;
    
    RecipeInputItemStack(final ItemStack input) {
        this(input, StackUtil.getSize(input));
    }
    
    RecipeInputItemStack(final ItemStack input, final int amount) {
        if (StackUtil.isEmpty(input)) {
            throw new IllegalArgumentException("invalid input stack");
        }
        this.input = input.copy();
        this.amount = amount;
    }
    
    @Override
    public boolean matches(final ItemStack subject) {
        return subject.getItem() == this.input.getItem() && (subject.getMetadata() == this.input.getMetadata() || this.input.getMetadata() == 32767) && (this.input.getMetadata() == 32767 || StackUtil.matchesNBT(subject.getTagCompound(), this.input.getTagCompound()));
    }
    
    @Override
    public int getAmount() {
        return this.amount;
    }
    
    @Override
    public List<ItemStack> getInputs() {
        return Arrays.asList(StackUtil.setImmutableSize(this.input, this.getAmount()));
    }
    
    public String toString() {
        return "RInputItemStack<" + StackUtil.setImmutableSize(this.input, this.amount) + ">";
    }
    
    public boolean equals(final Object obj) {
        final RecipeInputItemStack other;
        return obj != null && this.getClass() == obj.getClass() && StackUtil.checkItemEqualityStrict((other = (RecipeInputItemStack)obj).input, this.input) && other.amount == this.amount;
    }
}
