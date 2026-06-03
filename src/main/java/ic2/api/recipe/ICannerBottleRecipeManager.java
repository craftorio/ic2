package ic2.api.recipe;

import net.minecraft.item.ItemStack;

public interface ICannerBottleRecipeManager extends IMachineRecipeManager<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> {
  boolean addRecipe(IRecipeInput paramIRecipeInput1, IRecipeInput paramIRecipeInput2, ItemStack paramItemStack, boolean paramBoolean);
  
  @Deprecated
  void addRecipe(IRecipeInput paramIRecipeInput1, IRecipeInput paramIRecipeInput2, ItemStack paramItemStack);
  
  @Deprecated
  RecipeOutput getOutputFor(ItemStack paramItemStack1, ItemStack paramItemStack2, boolean paramBoolean1, boolean paramBoolean2);
  
  public static class Input {
    public final IRecipeInput container;
    
    public final IRecipeInput fill;
    
    public Input(IRecipeInput container, IRecipeInput fill) {
      this.container = container;
      this.fill = fill;
    }
    
    public boolean matches(ItemStack container, ItemStack fill) {
      return (this.container.matches(container) && this.fill.matches(fill));
    }
  }
  
  public static class RawInput {
    public final ItemStack container;
    
    public final ItemStack fill;
    
    public RawInput(ItemStack container, ItemStack fill) {
      this.container = container;
      this.fill = fill;
    }
  }
}
