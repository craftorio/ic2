package ic2.jeiIntegration.recipe.machine;

import ic2.api.item.IBlockCuttingBlade;
import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.item.type.BlockCuttingBladeType;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public interface IRecipeWrapperGenerator<T> {
  public static final IRecipeWrapperGenerator<IBasicMachineRecipeManager> basicMachine = new IRecipeWrapperGenerator<IBasicMachineRecipeManager>() {
      public List<IRecipeWrapper> getRecipeList(IORecipeCategory<IBasicMachineRecipeManager> category) {
        List<IRecipeWrapper> recipes = new ArrayList<>();
        for (MachineRecipe<IRecipeInput, Collection<ItemStack>> container : (Iterable<MachineRecipe<IRecipeInput, Collection<ItemStack>>>)((IBasicMachineRecipeManager)category.recipeManager).getRecipes())
          recipes.add(new IORecipeWrapper(container, category)); 
        return recipes;
      }
    };
  
  public static final IRecipeWrapperGenerator<IBasicMachineRecipeManager> recycler = new IRecipeWrapperGenerator<IBasicMachineRecipeManager>() {
      public List<IRecipeWrapper> getRecipeList(IORecipeCategory<IBasicMachineRecipeManager> category) {
        IRecipeInput input = new IRecipeInput() {
            public boolean matches(ItemStack subject) {
              return StackUtil.checkItemEquality(subject, BlockName.te.getItemStack((Enum)TeBlock.recycler));
            }
            
            public List<ItemStack> getInputs() {
              return Collections.singletonList(BlockName.te.getItemStack((Enum)TeBlock.recycler));
            }
            
            public int getAmount() {
              return 1;
            }
          };
        return (List)Collections.singletonList(new IORecipeWrapper(new MachineRecipe(input, 
                
                Collections.singletonList(ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap))), category));
      }
    };
  
  public static final IRecipeWrapperGenerator<IBasicMachineRecipeManager> blockCutter = new IRecipeWrapperGenerator<IBasicMachineRecipeManager>() {
      private final List<ItemStack> candidates = Arrays.asList(new ItemStack[] { ItemName.block_cutting_blade
            .getItemStack((Enum)BlockCuttingBladeType.iron), ItemName.block_cutting_blade
            .getItemStack((Enum)BlockCuttingBladeType.steel), ItemName.block_cutting_blade
            .getItemStack((Enum)BlockCuttingBladeType.diamond) });
      
      public List<IRecipeWrapper> getRecipeList(IORecipeCategory<IBasicMachineRecipeManager> category) {
        List<IRecipeWrapper> list = new ArrayList<>();
        for (MachineRecipe<IRecipeInput, Collection<ItemStack>> container : (Iterable<MachineRecipe<IRecipeInput, Collection<ItemStack>>>)((IBasicMachineRecipeManager)category.recipeManager).getRecipes())
          list.add(new AdvancedIORecipeWrapper(container, getInput(getHardness(container.getMetaData())), category)); 
        return list;
      }
      
      private int getHardness(NBTTagCompound metadata) {
        if (metadata == null)
          return Integer.MAX_VALUE; 
        return metadata.getInteger("hardness");
      }
      
      private IRecipeInput getInput(final int hardness) {
        return new IRecipeInput() {
            public boolean matches(ItemStack subject) {
              return (subject != null && subject.getItem() instanceof IBlockCuttingBlade && ((IBlockCuttingBlade)subject.getItem()).getHardness(subject) > hardness);
            }
            
            public List<ItemStack> getInputs() {
              List<ItemStack> list = new ArrayList<>(IRecipeWrapperGenerator.null.this.candidates.size());
              for (ItemStack stack : IRecipeWrapperGenerator.null.this.candidates) {
                if (((IBlockCuttingBlade)stack.getItem()).getHardness(stack) >= hardness)
                  list.add(stack); 
              } 
              return list;
            }
            
            public int getAmount() {
              return 1;
            }
          };
      }
    };
  
  public static final IRecipeWrapperGenerator<IElectrolyzerRecipeManager> electrolyzer = new IRecipeWrapperGenerator<IElectrolyzerRecipeManager>() {
      public List<IRecipeWrapper> getRecipeList(IORecipeCategory<IElectrolyzerRecipeManager> category) {
        List<IRecipeWrapper> recipes = new ArrayList<>();
        for (Map.Entry<String, IElectrolyzerRecipeManager.ElectrolyzerRecipe> recipe : (Iterable<Map.Entry<String, IElectrolyzerRecipeManager.ElectrolyzerRecipe>>)((IElectrolyzerRecipeManager)category.recipeManager).getRecipeMap().entrySet()) {
          Fluid input = FluidRegistry.getFluid(recipe.getKey());
          if (input != null)
            recipes.add(new ElectrolyzerWrapper(new FluidStack(input, ((IElectrolyzerRecipeManager.ElectrolyzerRecipe)recipe.getValue()).inputAmount), ((IElectrolyzerRecipeManager.ElectrolyzerRecipe)recipe.getValue()).outputs, category)); 
        } 
        return recipes;
      }
    };
  
  public static final IRecipeWrapperGenerator<ICannerEnrichRecipeManager> cannerEnrichment = new IRecipeWrapperGenerator<ICannerEnrichRecipeManager>() {
      public List<IRecipeWrapper> getRecipeList(IORecipeCategory<ICannerEnrichRecipeManager> category) {
        List<IRecipeWrapper> recipes = new ArrayList<>();
        for (MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> recipe : (Iterable<MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack>>)((ICannerEnrichRecipeManager)category.recipeManager).getRecipes())
          recipes.add(new CannerEnrichmentWrapper((ICannerEnrichRecipeManager.Input)recipe.getInput(), (FluidStack)recipe.getOutput(), category)); 
        return recipes;
      }
    };
  
  public static final IRecipeWrapperGenerator<ICannerBottleRecipeManager> cannerBottling = new IRecipeWrapperGenerator<ICannerBottleRecipeManager>() {
      public List<IRecipeWrapper> getRecipeList(IORecipeCategory<ICannerBottleRecipeManager> category) {
        List<IRecipeWrapper> recipes = new ArrayList<>();
        for (MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe : (Iterable<MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>>)((ICannerBottleRecipeManager)category.recipeManager).getRecipes())
          recipes.add(new CannerCanningWrapper((ICannerBottleRecipeManager.Input)recipe.getInput(), (ItemStack)recipe.getOutput(), category)); 
        return recipes;
      }
    };
  
  List<IRecipeWrapper> getRecipeList(IORecipeCategory<T> paramIORecipeCategory);
}
