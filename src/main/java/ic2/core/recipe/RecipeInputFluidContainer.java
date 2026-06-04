package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RecipeInputFluidContainer extends RecipeInputBase implements IRecipeInput {
  private final Fluid fluid;
  
  private final int amount;
  
  RecipeInputFluidContainer(Fluid fluid) {
    this(fluid, 1000);
  }
  
  RecipeInputFluidContainer(Fluid fluid, int amount) {
    this.fluid = fluid;
    this.amount = amount;
  }
  
  public boolean matches(ItemStack subject) {
    FluidStack fs = FluidUtil.getFluidContained(subject);
    return ((fs == null && this.fluid == null) || (fs != null && fs
      .getFluid() == this.fluid && fs.amount >= this.amount));
  }
  
  public int getAmount() {
    return 1;
  }
  
  public List<ItemStack> getInputs() {
    return getFluidContainer(this.fluid);
  }
  
  public String toString() {
    return "RInputFluidContainer<" + this.amount + "x" + this.fluid.getName() + ">";
  }
  
  public boolean equals(Object obj) {
    RecipeInputFluidContainer other;
    return (obj != null && getClass() == obj.getClass() && (other = (RecipeInputFluidContainer)obj).fluid == this.fluid && other.amount == this.amount);
  }
  
  public static List<ItemStack> getFluidContainer(Fluid fluid) {
    FluidHandlerInfo info = fluidHandlerInfo;
    if (info.loaderState != LoaderState.AVAILABLE && info.loaderState != Loader.instance().getLoaderState()) {
      List<ItemStack> list = new ArrayList<>();
      for (Item item : ForgeRegistries.ITEMS) {
        ItemStack stack = new ItemStack(item);
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler != null) {
          handler.drain(2147483647, true);
          ItemStack container = handler.getContainer();
          if (container == null)
            IC2.platform.displayError("Null container stack!\nItem: %s\nRegistry: %s\nUnlocalised: %s\nHandler: %s (%s)", new Object[] { item, item
                  .getRegistryName(), item.func_77658_a(), handler, handler.getClass() }); 
          if (FluidUtil.getFluidContained(container) == null)
            list.add(stack); 
        } 
      } 
      LoaderState state = Loader.instance().hasReachedState(LoaderState.AVAILABLE) ? LoaderState.AVAILABLE : Loader.instance().getLoaderState();
      fluidHandlerInfo = info = new FluidHandlerInfo(Collections.unmodifiableList(list), state);
    } 
    if (fluid == null)
      return info.items; 
    List<ItemStack> ret = new ArrayList<>();
    for (ItemStack stack : info.items) {
      IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack.copy());
      if (handler != null && handler
        .fill(new FluidStack(fluid, 2147483647), true) > 0) {
        ItemStack container = handler.getContainer();
        if (FluidUtil.getFluidContained(container) != null)
          ret.add(container); 
      } 
    } 
    return ret;
  }
  
  private static class FluidHandlerInfo {
    final List<ItemStack> items;
    
    final LoaderState loaderState;
    
    FluidHandlerInfo(List<ItemStack> items, LoaderState loaderState) {
      this.items = items;
      this.loaderState = loaderState;
    }
  }
  
  private static volatile FluidHandlerInfo fluidHandlerInfo = new FluidHandlerInfo(Collections.emptyList(), LoaderState.PREINITIALIZATION);
}
