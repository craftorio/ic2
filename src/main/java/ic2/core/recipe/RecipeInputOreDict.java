// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraftforge.oredict.OreDictionary;
import java.util.Collections;
import java.util.ArrayList;
import ic2.core.util.StackUtil;
import java.util.Iterator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.api.recipe.IRecipeInput;

public class RecipeInputOreDict extends RecipeInputBase implements IRecipeInput
{
    public final String input;
    public final int amount;
    public final Integer meta;
    private List<ItemStack> ores;
    
    RecipeInputOreDict(final String input) {
        this(input, 1);
    }
    
    RecipeInputOreDict(final String input, final int amount) {
        this(input, amount, null);
    }
    
    RecipeInputOreDict(final String input, final int amount, final Integer meta) {
        this.input = input;
        this.amount = amount;
        this.meta = meta;
    }
    
    @Override
    public boolean matches(final ItemStack subject) {
        final List<ItemStack> inputs = this.getOres();
        final boolean useOreStackMeta = this.meta == null;
        final Item subjectItem = subject.getItem();
        final int subjectMeta = subject.getItemDamage();
        for (final ItemStack oreStack : inputs) {
            final Item oreItem = oreStack.getItem();
            if (oreItem == null) {
                continue;
            }
            final int metaRequired = useOreStackMeta ? oreStack.getItemDamage() : this.meta;
            if (subjectItem == oreItem && (subjectMeta == metaRequired || metaRequired == 32767)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int getAmount() {
        return this.amount;
    }
    
    @Override
    public List<ItemStack> getInputs() {
        final List<ItemStack> ores = this.getOres();
        boolean hasUnsuitableEntries = false;
        for (final ItemStack stack : ores) {
            if (StackUtil.getSize(stack) != this.getAmount()) {
                hasUnsuitableEntries = true;
                break;
            }
        }
        if (!hasUnsuitableEntries) {
            return ores;
        }
        final List<ItemStack> ret = new ArrayList<ItemStack>(ores.size());
        for (ItemStack stack2 : ores) {
            if (stack2.getItem() != null) {
                if (StackUtil.getSize(stack2) != this.getAmount()) {
                    stack2 = StackUtil.copyWithSize(stack2, this.getAmount());
                }
                ret.add(stack2);
            }
        }
        return Collections.unmodifiableList((List<? extends ItemStack>)ret);
    }
    
    public String toString() {
        if (this.meta == null) {
            return "RInputOreDict<" + this.amount + "x" + this.input + ">";
        }
        return "RInputOreDict<" + this.amount + "x" + this.input + "@" + this.meta + ">";
    }
    
    public boolean equals(final Object obj) {
        final RecipeInputOreDict other;
        return obj != null && this.getClass() == obj.getClass() && this.input.equals((other = (RecipeInputOreDict)obj).input) && other.amount == this.amount && ((this.meta == null) ? (other.meta == null) : (this.meta == other.meta));
    }
    
    private List<ItemStack> getOres() {
        if (this.ores != null) {
            return this.ores;
        }
        final List<ItemStack> ret = (List<ItemStack>)OreDictionary.getOres(this.input);
        if (ret != OreDictionary.EMPTY_LIST) {
            this.ores = ret;
        }
        return ret;
    }
}
