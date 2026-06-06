package ic2.jeiIntegration;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.gui.GuiIndustrialWorkbench;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.RecipeButton;
import ic2.core.item.ItemCropSeed;
import ic2.core.item.block.ItemCable;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.jeiIntegration.recipe.crafting.AdvRecipeHandler;
import ic2.jeiIntegration.recipe.crafting.AdvShapelessRecipeHandler;
import ic2.jeiIntegration.recipe.crafting.GradualRecipeHandler;
import ic2.jeiIntegration.recipe.crafting.JetpackRecipeHandler;
import ic2.jeiIntegration.recipe.crafting.JetpackRecipeWrapper;
import ic2.jeiIntegration.recipe.machine.CannerCanningHandler;
import ic2.jeiIntegration.recipe.machine.CannerCategory;
import ic2.jeiIntegration.recipe.machine.CannerEnrichmentHandler;
import ic2.jeiIntegration.recipe.machine.DynamicCategory;
import ic2.jeiIntegration.recipe.machine.ElectrolyzerCategory;
import ic2.jeiIntegration.recipe.machine.ElectrolyzerRecipeHandler;
import ic2.jeiIntegration.recipe.machine.IORecipeCategory;
import ic2.jeiIntegration.recipe.machine.IORecipeHandler;
import ic2.jeiIntegration.recipe.machine.IRecipeWrapperGenerator;
import ic2.jeiIntegration.recipe.machine.MetalFormerCategory;
import ic2.jeiIntegration.recipe.machine.RecyclerCategory;
import ic2.jeiIntegration.recipe.misc.ScrapboxRecipeCategory;
import ic2.jeiIntegration.recipe.misc.ScrapboxRecipeHandler;
import ic2.jeiIntegration.recipe.misc.ScrapboxRecipeWrapper;
import ic2.jeiIntegration.transferhandlers.TransferHandlerBatchCrafter;
import ic2.jeiIntegration.transferhandlers.TransferHandlerIndustrialWorkbench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.startup.StackHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

@JEIPlugin
public class SubModule implements IModPlugin
{
	private IIngredientRegistry ingredientRegistry;

	public void register(IModRegistry registry)
	{
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		registry.addRecipeHandlers(new IRecipeHandler[] { new AdvRecipeHandler() });
		registry.addRecipeHandlers(new IRecipeHandler[] { new AdvShapelessRecipeHandler() });
		registry.addRecipeHandlers(new IRecipeHandler[] { new GradualRecipeHandler() });
		registry.addRecipeHandlers(new IRecipeHandler[] { new JetpackRecipeHandler() });
		registry.addRecipes(JetpackRecipeWrapper.generateJetpackRecipes());
		registry.addRecipeCategories(new IRecipeCategory[] { new ScrapboxRecipeCategory(guiHelper) });
		registry.addRecipeCategoryCraftingItem(ItemName.crafting.getItemStack(CraftingItemType.scrap_box), new String[] { "ic2.scrapbox" });
		registry.addRecipeHandlers(new IRecipeHandler[] { new ScrapboxRecipeHandler() });
		registry.addRecipes(ScrapboxRecipeWrapper.createRecipes());
		registry.addRecipeHandlers(
			new IRecipeHandler[] { new IORecipeHandler(), new ElectrolyzerRecipeHandler(), new CannerEnrichmentHandler(), new CannerCanningHandler() }
		);
		this.addMachineRecipes(registry, new DynamicCategory<>(TeBlock.macerator, Recipes.macerator, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new DynamicCategory<>(TeBlock.extractor, Recipes.extractor, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new DynamicCategory<>(TeBlock.compressor, Recipes.compressor, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new DynamicCategory<>(TeBlock.centrifuge, Recipes.centrifuge, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new DynamicCategory<>(TeBlock.blast_furnace, Recipes.blastfurnace, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new DynamicCategory<>(TeBlock.ore_washing_plant, Recipes.oreWashing, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new DynamicCategory<>(TeBlock.block_cutter, Recipes.blockcutter, guiHelper), IRecipeWrapperGenerator.blockCutter);
		this.addMachineRecipes(registry, new MetalFormerCategory(Recipes.metalformerExtruding, 0, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new MetalFormerCategory(Recipes.metalformerRolling, 1, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new MetalFormerCategory(Recipes.metalformerCutting, 2, guiHelper), IRecipeWrapperGenerator.basicMachine);
		this.addMachineRecipes(registry, new ElectrolyzerCategory(guiHelper), IRecipeWrapperGenerator.electrolyzer);
		this.addMachineRecipes(registry, CannerCategory.enriching(guiHelper), IRecipeWrapperGenerator.cannerEnrichment);
		this.addMachineRecipes(registry, CannerCategory.bottling(guiHelper), IRecipeWrapperGenerator.cannerBottling);
		this.addMachineRecipes(registry, new DynamicCategory<>(TeBlock.solid_canner, Recipes.cannerBottle, guiHelper), IRecipeWrapperGenerator.cannerBottling);
		this.addMachineRecipes(registry, new RecyclerCategory(guiHelper), IRecipeWrapperGenerator.recycler);
		registry.addRecipeCategoryCraftingItem(BlockName.te.getItemStack(TeBlock.iron_furnace), new String[] { "minecraft.smelting" });
		registry.addRecipeCategoryCraftingItem(BlockName.te.getItemStack(TeBlock.electric_furnace), new String[] { "minecraft.smelting" });
		registry.addRecipeCategoryCraftingItem(BlockName.te.getItemStack(TeBlock.induction_furnace), new String[] { "minecraft.smelting" });
		registry.addRecipeCategoryCraftingItem(BlockName.te.getItemStack(TeBlock.iron_furnace), new String[] { "minecraft.fuel" });
		registry.addRecipeCategoryCraftingItem(BlockName.te.getItemStack(TeBlock.generator), new String[] { "minecraft.fuel" });
		registry.addRecipeCategoryCraftingItem(BlockName.te.getItemStack(TeBlock.solid_heat_generator), new String[] { "minecraft.fuel" });
		registry.getRecipeTransferRegistry()
			.addRecipeTransferHandler(
				new TransferHandlerIndustrialWorkbench(
					(StackHelper) registry.getJeiHelpers().getStackHelper(), registry.getJeiHelpers().recipeTransferHandlerHelper()
				),
				"minecraft.crafting"
			);
		registry.addRecipeCategoryCraftingItem(BlockName.te.getItemStack(TeBlock.industrial_workbench), new String[] { "minecraft.crafting" });
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new TransferHandlerBatchCrafter(), "minecraft.crafting");
		registry.addRecipeCategoryCraftingItem(BlockName.te.getItemStack(TeBlock.batch_crafter), new String[] { "minecraft.crafting" });
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
		{
			(new Runnable()
			{
				@Override
				public void run()
				{
					GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck = new Predicate<GuiScreen>()
					{
						public boolean apply(GuiScreen input)
						{
							return input instanceof IRecipesGui;
						}
					};
				}
			}).run();
		}
	}

	private <T> void addMachineRecipes(IModRegistry registry, IORecipeCategory<T> category, IRecipeWrapperGenerator<T> wrappergen)
	{
		registry.addRecipeCategories(new IRecipeCategory[] { category });
		registry.addRecipes(wrappergen.getRecipeList(category));
		registry.addRecipeCategoryCraftingItem(category.getBlockStack(), new String[] { category.getUid() });
	}

	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
		if (this.ingredientRegistry != null)
		{
			List<ItemStack> items = new ArrayList<>();

			for (CropCard crop : Crops.instance.getCrops())
			{
				items.add(ItemCropSeed.generateItemStackFromValues(crop, 1, 1, 1, 4));
			}

			this.ingredientRegistry.addIngredientsAtRuntime(ItemStack.class, items);
		}

		if (FMLCommonHandler.instance().getSide().isClient())
		{
			final IRecipesGui recipesGUI = jeiRuntime.getRecipesGui();
			(new Runnable()
			{
				@Override
				public void run()
				{
					RecipeButton.jeiRecipeListOpener = new Function<String[], IClickHandler>()
					{
						public IClickHandler apply(final String[] input)
						{
							assert input != null;
							return new IClickHandler()
							{
								@Override
								public void onClick(MouseButton button)
								{
									if (input.length > 0)
									{
										recipesGUI.showCategories(Arrays.asList(input));
									}
								}
							};
						}
					};
				}
			}).run();
		}
	}

	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
	{
		subtypeRegistry.registerSubtypeInterpreter(ItemName.cable.getInstance(), new ISubtypeInterpreter()
		{
			public String apply(ItemStack stack)
			{
				return ItemName.cable.<ItemCable>getInstance().getVariant(stack);
			}
		});
	}

	public void registerIngredients(IModIngredientRegistration registry)
	{
	}
}
