package ic2.data.recipe;

import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class ExtractorRecipeProvider extends Ic2RecipeProvider
{
	public ExtractorRecipeProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>> gen = (BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>>) (new BasicMachineRecipeGenerator<>(
			consumer, Ic2RecipeSerializers.EXTRACTOR
		));
		gen.add(Items.f_41995_, 1, Items.f_42460_, 4);
		gen.add(Items.f_41983_, 1, Items.f_42461_, 4);
		gen.add(Items.f_42095_, 1, Items.f_42691_, 4);
		gen.add(Items.f_41981_, 1, Items.f_42452_, 4);
		gen.add(ItemTags.f_13167_, 1, Items.f_41870_);
		gen.add(Items.f_41911_, 1, Items.f_42398_);
		gen.add(Items.f_42785_, 1, Items.f_42784_, 2);
		gen.add(Items.f_42754_, 1, Items.f_41999_);
		gen.add(Items.f_42758_, 1, Items.f_42587_, 4);
		gen.add(Items.f_41836_, 1, Items.f_42587_, 9);
		gen.add(ItemTags.f_215862_, 1, Items.f_42588_, 9);
		gen.add(ItemTags.f_144319_, 1, Items.f_42784_);
		gen.add(Items.f_151063_, 1, Items.f_42617_);
		gen.add(Items.f_220216_, 1, Items.f_42461_, 4);
		gen.add(Items.f_220181_, 1, Items.f_220216_);
		gen.add(Ic2Items.AIR_CELL, 1, Ic2Items.EMPTY_CELL);
		gen.add(Ic2Items.FILLED_TIN_CAN, 1, Ic2Items.TIN_CAN);
		gen.add(Items.f_42403_, 1, Ic2Items.SULFUR_DUST);
		gen.add(Ic2Items.HYDRATED_TIN_DUST, 1, Ic2Items.IODINE);
		gen.add(Items.f_42048_, 1, Ic2Items.SMALL_SULFUR_DUST);
		gen.add(Ic2Items.RESIN, 1, Ic2Items.RUBBER, 3);
		gen.add(Ic2Items.RUBBER_SAPLING, 1, Ic2Items.RUBBER);
		gen.add(Ic2Items.RUBBER_LOG, 1, Ic2Items.RUBBER);
		gen.add(Ic2Items.NETHERRACK_DUST, 1, Ic2Items.SMALL_SULFUR_DUST);
	}
}
