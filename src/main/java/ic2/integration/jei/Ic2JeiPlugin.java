package ic2.integration.jei;

import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.recipe.v2.RecipeHolder;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2RecipeTypes;
import ic2.integration.jei.recipe.machine.DynamicCategory;
import ic2.integration.jei.recipe.machine.IORecipeWrapper;
import ic2.integration.jei.recipe.machine.MetalFormerCategory;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class Ic2JeiPlugin implements IModPlugin
{
	private final RecipeType<IORecipeWrapper> BLAST_FURNACE = RecipeType.create("ic2", "blast_furnace", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> BLOCK_CUTTER = RecipeType.create("ic2", "block_cutter", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> CENTRIFUGE = RecipeType.create("ic2", "centrifuge", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> COMPRESSOR = RecipeType.create("ic2", "compressor", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> EXTRACTOR = RecipeType.create("ic2", "extractor", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> MACERATOR = RecipeType.create("ic2", "macerator", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_EXTRUDING = RecipeType.create("ic2", "metal_former_extruding", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_ROLLING = RecipeType.create("ic2", "metal_former_rolling", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_CUTTING = RecipeType.create("ic2", "metal_former_cutting", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> ORE_WASHER = RecipeType.create("ic2", "ore_washer", IORecipeWrapper.class);

	public ResourceLocation getPluginUid()
	{
		return IC2.getIdentifier("plugin");
	}

	public void registerCategories(IRecipeCategoryRegistration registration)
	{
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.BLAST_FURNACE, this.BLAST_FURNACE, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.BLOCK_CUTTER, this.BLOCK_CUTTER, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.CENTRIFUGE, this.CENTRIFUGE, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.COMPRESSOR, this.COMPRESSOR, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.EXTRACTOR, this.EXTRACTOR, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.MACERATOR, this.MACERATOR, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_EXTRUDING, 0, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_ROLLING, 1, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_CUTTING, 2, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.ORE_WASHING_PLANT, this.ORE_WASHER, guiHelper));
	}

	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BLAST_FURNACE), this.BLAST_FURNACE);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BLOCK_CUTTER), this.BLOCK_CUTTER);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CENTRIFUGE), this.CENTRIFUGE);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.COMPRESSOR), this.COMPRESSOR);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.EXTRACTOR), this.EXTRACTOR);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.MACERATOR), this.MACERATOR);
		registration.addRecipeCatalyst(
			new ItemStack(Ic2Blocks.METAL_FORMER),
			this.METAL_FORMER_CUTTING, this.METAL_FORMER_EXTRUDING, this.METAL_FORMER_ROLLING);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.ORE_WASHING_PLANT), this.ORE_WASHER);
	}

	public void registerRecipes(@NotNull IRecipeRegistration registration)
	{
		RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
		BiConsumer<RecipeType<IORecipeWrapper>, net.minecraft.world.item.crafting.RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>>> registerBasic = (id, type) ->
		{
			List<IORecipeWrapper> recipeList = recipeManager.getAllRecipesFor(type).stream().map(r -> new IORecipeWrapper(r.recipe())).toList();
			registration.addRecipes(id, recipeList);
		};
		registerBasic.accept(this.BLAST_FURNACE, Ic2RecipeTypes.BLAST_FURNACE);
		registerBasic.accept(this.BLOCK_CUTTER, Ic2RecipeTypes.BLOCK_CUTTER);
		registerBasic.accept(this.CENTRIFUGE, Ic2RecipeTypes.CENTRIFUGE);
		registerBasic.accept(this.COMPRESSOR, Ic2RecipeTypes.COMPRESSOR);
		registerBasic.accept(this.EXTRACTOR, Ic2RecipeTypes.EXTRACTOR);
		registerBasic.accept(this.MACERATOR, Ic2RecipeTypes.MACERATOR);
		registerBasic.accept(this.METAL_FORMER_CUTTING, Ic2RecipeTypes.METAL_FORMER_CUTTING);
		registerBasic.accept(this.METAL_FORMER_EXTRUDING, Ic2RecipeTypes.METAL_FORMER_EXTRUDING);
		registerBasic.accept(this.METAL_FORMER_ROLLING, Ic2RecipeTypes.METAL_FORMER_ROLLING);
		registerBasic.accept(this.ORE_WASHER, Ic2RecipeTypes.ORE_WASHER);
	}
}
