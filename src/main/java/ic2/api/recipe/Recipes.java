package ic2.api.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Recipes
{
	public static IRecipeInputFactory inputFactory;
	public static IMachineRecipeManager<ItemStack, ItemStack, ItemStack> furnace;
	public static Recipes.IGetter<IBasicMachineRecipeManager> macerator;
	public static Recipes.IGetter<IBasicMachineRecipeManager> extractor;
	public static Recipes.IGetter<IBasicMachineRecipeManager> compressor;
	public static Recipes.IGetter<IBasicMachineRecipeManager> centrifuge;
	public static Recipes.IGetter<IBasicMachineRecipeManager> block_cutter;
	public static Recipes.IGetter<IBasicMachineRecipeManager> blast_furnace;
	public static IBasicMachineRecipeManager recycler;
	public static Recipes.IGetter<IBasicMachineRecipeManager> metalformerExtruding;
	public static Recipes.IGetter<IBasicMachineRecipeManager> metalformerCutting;
	public static Recipes.IGetter<IBasicMachineRecipeManager> metalformerRolling;
	public static Recipes.IGetter<IBasicMachineRecipeManager> oreWashing;
	public static Recipes.IGetter<IMachineRecipeManager<IRecipeInput, Integer, ItemStack>> matterFabricator;
	public static Recipes.IGetter<ICannerBottleRecipeManager> cannerBottle;
	public static Recipes.IGetter<ICannerEnrichRecipeManager> cannerEnrich;
	public static IElectrolyzerRecipeManager electrolyzer;
	public static IFermenterRecipeManager fermenter;
	public static IMachineRecipeManager<IRecipeInput, Integer, ItemStack> matterAmplifier;
	public static IScrapboxManager scrapboxDrops;
	public static IListRecipeManager recyclerBlacklist;
	public static IListRecipeManager recyclerWhitelist;
	public static ISemiFluidFuelManager semiFluidGenerator;
	public static IFluidHeatManager fluidHeatGenerator;
	public static ILiquidHeatExchangerManager liquidCooldownManager;
	public static ILiquidHeatExchangerManager liquidHeatUpManager;
	public static IEmptyFluidContainerRecipeManager emptyFluidContainer;
	public static IFillFluidContainerRecipeManager fillFluidContainer;

	public interface IGetter<T>
	{
		T get(Level var1);
	}
}
