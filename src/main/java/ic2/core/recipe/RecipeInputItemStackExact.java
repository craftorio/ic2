// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import java.util.Collections;
import java.util.List;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class RecipeInputItemStackExact extends RecipeInputBase
{
    public final ItemStack input;
    public final int amount;
    
    RecipeInputItemStackExact(final ItemStack input) {
        this(input, StackUtil.getSize(input));
    }
    
    RecipeInputItemStackExact(final ItemStack input, final int amount) {
        if (StackUtil.isEmpty(input) || input.getMetadata() == 32767) {
            throw new IllegalArgumentException("invalid input stack");
        }
        this.input = StackUtil.copy(input);
        this.amount = amount;
    }
    
    public boolean matches(final ItemStack subject) {
        return subject.getItem() == this.input.getItem() && subject.getMetadata() == this.input.getMetadata() && StackUtil.checkNbtEqualityStrict(subject, this.input);
    }
    
    @Override
    public int getAmount() {
        return this.amount;
    }
    
    public List<ItemStack> getInputs() {
        return Collections.singletonList(StackUtil.setImmutableSize(this.input, this.getAmount()));
    }
    
    public String toString() {
        return "RInputItemStackExact<" + StackUtil.setImmutableSize(this.input, this.amount) + '>';
    }
    
    public boolean equals(final Object obj) {
        final RecipeInputItemStackExact other;
        return obj != null && this.getClass() == obj.getClass() && StackUtil.checkItemEqualityStrict((other = (RecipeInputItemStackExact)obj).input, this.input) && other.amount == this.amount;
    }
}
