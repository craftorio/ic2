package ic2.data.recipe;

import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BlastFurnaceRecipeProvider extends Ic2RecipeProvider
{
	public BlastFurnaceRecipeProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>> gen = (BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>>) (new BasicMachineRecipeGenerator<>(
			consumer, Ic2RecipeSerializers.BLAST_FURNACE, true
		));
		gen.fluidDuration(1, 6000).add(Items.IRON_ORE, 1, new ItemStack(Ic2Items.STEEL_INGOT), new ItemStack(Ic2Items.SLAG));
		gen.fluidDuration(1, 6000).add(Ic2Items.CRUSHED_IRON, 1, new ItemStack(Ic2Items.STEEL_INGOT), new ItemStack(Ic2Items.SLAG));
		gen.fluidDuration(1, 6000).add(Ic2Items.PURIFIED_IRON, 1, new ItemStack(Ic2Items.STEEL_INGOT), new ItemStack(Ic2Items.SLAG));
		gen.fluidDuration(1, 6000).add(Ic2ItemTags.IRON_DUSTS, 1, new ItemStack(Ic2Items.STEEL_INGOT), new ItemStack(Ic2Items.SLAG));
		gen.fluidDuration(1, 6000).add(Items.IRON_INGOT, 1, new ItemStack(Ic2Items.STEEL_INGOT), new ItemStack(Ic2Items.SLAG));
	}
}
