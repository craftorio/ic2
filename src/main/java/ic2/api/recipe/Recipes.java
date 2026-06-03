package ic2.api.recipe;

import net.minecraft.item.ItemStack;

public class Recipes {
  public static IRecipeInputFactory inputFactory;
  
  public static IMachineRecipeManager<ItemStack, ItemStack, ItemStack> furnace;
  
  public static IBasicMachineRecipeManager macerator;
  
  public static IBasicMachineRecipeManager extractor;
  
  public static IBasicMachineRecipeManager compressor;
  
  public static IBasicMachineRecipeManager centrifuge;
  
  public static IBasicMachineRecipeManager blockcutter;
  
  public static IBasicMachineRecipeManager blastfurnace;
  
  public static IBasicMachineRecipeManager recycler;
  
  public static IBasicMachineRecipeManager metalformerExtruding;
  
  public static IBasicMachineRecipeManager metalformerCutting;
  
  public static IBasicMachineRecipeManager metalformerRolling;
  
  public static IBasicMachineRecipeManager oreWashing;
  
  public static ICannerBottleRecipeManager cannerBottle;
  
  public static ICannerEnrichRecipeManager cannerEnrich;
  
  public static IElectrolyzerRecipeManager electrolyzer;
  
  public static IFermenterRecipeManager fermenter;
  
  public static IMachineRecipeManager<IRecipeInput, Integer, ItemStack> matterAmplifier;
  
  public static IScrapboxManager scrapboxDrops;
  
  public static IListRecipeManager recyclerBlacklist;
  
  public static IListRecipeManager recyclerWhitelist;
  
  public static ICraftingRecipeManager advRecipes;
  
  public static ISemiFluidFuelManager semiFluidGenerator;
  
  public static IFluidHeatManager fluidHeatGenerator;
  
  public static ILiquidHeatExchangerManager liquidCooldownManager;
  
  public static ILiquidHeatExchangerManager liquidHeatupManager;
  
  public static IEmptyFluidContainerRecipeManager emptyFluidContainer;
  
  public static IFillFluidContainerRecipeManager fillFluidContainer;
}
