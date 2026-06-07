package ic2.data.recipe;

import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class OreWasherRecipeProvider extends Ic2RecipeProvider
{
	public OreWasherRecipeProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>> gen = (BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>>) (new BasicMachineRecipeGenerator<>(
			consumer, Ic2RecipeSerializers.ORE_WASHER, true
		));
		gen.amount(1000).add(Items.f_41832_, 1, Ic2Items.STONE_DUST);
		gen.amount(1000)
			.add(
				Ic2Items.CRUSHED_COPPER,
				1,
				new ItemStack(Ic2Items.PURIFIED_COPPER),
				new ItemStack(Ic2Items.SMALL_COPPER_DUST, 2),
				new ItemStack(Ic2Items.STONE_DUST)
			);
		gen.amount(1000)
			.add(Ic2Items.CRUSHED_GOLD, 1, new ItemStack(Ic2Items.PURIFIED_GOLD), new ItemStack(Ic2Items.SMALL_GOLD_DUST, 2), new ItemStack(Ic2Items.STONE_DUST));
		gen.amount(1000)
			.add(Ic2Items.CRUSHED_IRON, 1, new ItemStack(Ic2Items.PURIFIED_IRON), new ItemStack(Ic2Items.SMALL_IRON_DUST, 2), new ItemStack(Ic2Items.STONE_DUST));
		gen.amount(1000)
			.add(Ic2Items.CRUSHED_LEAD, 1, new ItemStack(Ic2Items.PURIFIED_LEAD), new ItemStack(Ic2Items.SMALL_SULFUR_DUST, 3), new ItemStack(Ic2Items.STONE_DUST));
		gen.amount(1000)
			.add(
				Ic2Items.CRUSHED_SILVER,
				1,
				new ItemStack(Ic2Items.PURIFIED_SILVER),
				new ItemStack(Ic2Items.SMALL_SILVER_DUST, 2),
				new ItemStack(Ic2Items.STONE_DUST)
			);
		gen.amount(1000)
			.add(Ic2Items.CRUSHED_TIN, 1, new ItemStack(Ic2Items.PURIFIED_TIN), new ItemStack(Ic2Items.SMALL_TIN_DUST, 2), new ItemStack(Ic2Items.STONE_DUST));
		gen.amount(1000)
			.add(
				Ic2Items.CRUSHED_URANIUM,
				1,
				new ItemStack(Ic2Items.PURIFIED_URANIUM),
				new ItemStack(Ic2Items.SMALL_LEAD_DUST, 2),
				new ItemStack(Ic2Items.STONE_DUST)
			);
	}
}
