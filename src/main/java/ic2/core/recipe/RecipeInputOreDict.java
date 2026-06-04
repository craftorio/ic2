package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeInputOreDict extends RecipeInputBase implements IRecipeInput {
  public final String input;
  
  public final int amount;
  
  public final Integer meta;
  
  private List<ItemStack> ores;
  
  RecipeInputOreDict(String input) {
    this(input, 1);
  }
  
  RecipeInputOreDict(String input, int amount) {
    this(input, amount, null);
  }
  
  RecipeInputOreDict(String input, int amount, Integer meta) {
    this.input = input;
    this.amount = amount;
    this.meta = meta;
  }
  
  public boolean matches(ItemStack subject) {
    List<ItemStack> inputs = getOres();
    boolean useOreStackMeta = (this.meta == null);
    Item subjectItem = subject.getItem();
    int subjectMeta = subject.func_77952_i();
    for (ItemStack oreStack : inputs) {
      Item oreItem = oreStack.getItem();
      if (oreItem == null)
        continue; 
      int metaRequired = useOreStackMeta ? oreStack.func_77952_i() : this.meta.intValue();
      if (subjectItem == oreItem && (subjectMeta == metaRequired || metaRequired == 32767))
        return true; 
    } 
    return false;
  }
  
  public int getAmount() {
    return this.amount;
  }
  
  public List<ItemStack> getInputs() {
    List<ItemStack> ores = getOres();
    boolean hasUnsuitableEntries = false;
    for (ItemStack stack : ores) {
      if (StackUtil.getSize(stack) != getAmount()) {
        hasUnsuitableEntries = true;
        break;
      } 
    } 
    if (!hasUnsuitableEntries)
      return ores; 
    List<ItemStack> ret = new ArrayList<>(ores.size());
    for (ItemStack stack : ores) {
      if (stack.getItem() != null) {
        if (StackUtil.getSize(stack) != getAmount())
          stack = StackUtil.copyWithSize(stack, getAmount()); 
        ret.add(stack);
      } 
    } 
    return Collections.unmodifiableList(ret);
  }
  
  public String toString() {
    if (this.meta == null)
      return "RInputOreDict<" + this.amount + "x" + this.input + ">"; 
    return "RInputOreDict<" + this.amount + "x" + this.input + "@" + this.meta + ">";
  }
  
  public boolean equals(Object obj) {
    RecipeInputOreDict other;
    if (obj != null && getClass() == obj.getClass() && this.input
      .equals((other = (RecipeInputOreDict)obj).input) && other.amount == this.amount)
      return (this.meta == null) ? ((other.meta == null)) : ((this.meta == other.meta)); 
    return false;
  }
  
  private List<ItemStack> getOres() {
    if (this.ores != null)
      return this.ores; 
    NonNullList nonNullList = OreDictionary.getOres(this.input);
    if (nonNullList != OreDictionary.EMPTY_LIST)
      this.ores = (List<ItemStack>)nonNullList; 
    return (List<ItemStack>)nonNullList;
  }
}
