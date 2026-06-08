package ic2.data.recipe;

import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

public class MetalFormerRecipeProvider extends Ic2RecipeProvider
{
	public MetalFormerRecipeProvider(PackOutput packOutput)
	{
		super(packOutput);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		generateCutting(consumer);
		generateExtruding(consumer);
		generateRolling(consumer);
	}

	private static void generateCutting(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>> gen = (BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>>) (new BasicMachineRecipeGenerator<>(
			consumer, Ic2RecipeSerializers.METAL_FORMER_CUTTING
		));
		gen.add(Ic2ItemTags.COPPER_PLATES, 1, Ic2Items.COPPER_CABLE, 2);
		gen.add(Ic2ItemTags.GOLD_PLATES, 1, Ic2Items.GOLD_CABLE, 4);
		gen.add(Ic2ItemTags.IRON_PLATES, 1, Ic2Items.IRON_CABLE, 4);
		gen.add(Ic2ItemTags.TIN_PLATES, 1, Ic2Items.TIN_CABLE, 3);
		gen.add(Ic2Items.IRON_CASING, 1, Ic2Items.COIN);
	}

	private static void generateExtruding(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator gen = new BasicMachineRecipeGenerator(consumer, Ic2RecipeSerializers.METAL_FORMER_EXTRUDING);
		gen.add(Ic2Items.IRON_CASING, 1, Ic2Items.IRON_FENCE);
		gen.add(Ic2ItemTags.IRON_PLATES, 1, Ic2Items.FUEL_ROD);
		gen.add(Ic2Items.TIN_CASING, 1, Ic2Items.TIN_CAN);
		gen.add(Ic2ItemTags.BRONZE_BLOCKS, 1, Ic2Items.BRONZE_SHAFT);
		gen.add(Items.IRON_BLOCK, 1, Ic2Items.IRON_SHAFT);
		gen.add(Ic2ItemTags.STEEL_BLOCKS, 1, Ic2Items.STEEL_SHAFT);
		gen.add(Items.COPPER_INGOT, 1, Ic2Items.COPPER_CABLE, 3);
		gen.add(Items.GOLD_INGOT, 1, Ic2Items.GOLD_CABLE, 4);
		gen.add(Items.IRON_INGOT, 1, Ic2Items.IRON_CABLE, 4);
		gen.add(Ic2ItemTags.TIN_INGOTS, 1, Ic2Items.TIN_CABLE, 3);
	}

	private static void generateRolling(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator gen = new BasicMachineRecipeGenerator(consumer, Ic2RecipeSerializers.METAL_FORMER_ROLLING);
		gen.add(Ic2ItemTags.BRONZE_INGOTS, 1, Ic2Items.BRONZE_PLATE);
		gen.add(Items.COPPER_INGOT, 1, Ic2Items.COPPER_PLATE);
		gen.add(Items.GOLD_INGOT, 1, Ic2Items.GOLD_PLATE);
		gen.add(Items.IRON_INGOT, 1, Ic2Items.IRON_PLATE);
		gen.add(Ic2ItemTags.LEAD_INGOTS, 1, Ic2Items.LEAD_PLATE);
		gen.add(Ic2ItemTags.STEEL_INGOTS, 1, Ic2Items.STEEL_PLATE);
		gen.add(Ic2ItemTags.TIN_INGOTS, 1, Ic2Items.TIN_PLATE);
		gen.add(Ic2ItemTags.BRONZE_PLATES, 1, Ic2Items.BRONZE_CASING, 2);
		gen.add(Ic2ItemTags.COPPER_PLATES, 1, Ic2Items.COPPER_CASING, 2);
		gen.add(Ic2ItemTags.GOLD_PLATES, 1, Ic2Items.GOLD_CASING, 2);
		gen.add(Ic2ItemTags.IRON_PLATES, 1, Ic2Items.IRON_CASING, 2);
		gen.add(Ic2ItemTags.LEAD_PLATES, 1, Ic2Items.LEAD_CASING, 2);
		gen.add(Ic2ItemTags.STEEL_PLATES, 1, Ic2Items.STEEL_CASING, 2);
		gen.add(Ic2ItemTags.TIN_PLATES, 1, Ic2Items.TIN_CASING, 2);
	}
}
