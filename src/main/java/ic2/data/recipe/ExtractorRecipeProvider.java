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
		gen.add(Items.BRICKS, 1, Items.BRICK, 4);
		gen.add(Items.CLAY, 1, Items.CLAY_BALL, 4);
		gen.add(Items.NETHER_BRICKS, 1, Items.NETHER_BRICK, 4);
		gen.add(Items.SNOW_BLOCK, 1, Items.SNOWBALL, 4);
		gen.add(ItemTags.WOOL, 1, Items.WHITE_WOOL);
		gen.add(Items.BAMBOO, 1, Items.STICK);
		gen.add(Items.BEE_NEST, 1, Items.HONEYCOMB, 2);
		gen.add(Items.CRYING_OBSIDIAN, 1, Items.OBSIDIAN);
		gen.add(Items.GILDED_BLACKSTONE, 1, Items.GOLD_NUGGET, 4);
		gen.add(Items.NETHER_GOLD_ORE, 1, Items.GOLD_NUGGET, 9);
		gen.add(ItemTags.WART_BLOCKS, 1, Items.NETHER_WART, 9);
		gen.add(ItemTags.CANDLES, 1, Items.HONEYCOMB);
		gen.add(Items.GLOW_ITEM_FRAME, 1, Items.ITEM_FRAME);
		gen.add(Items.MUD, 1, Items.CLAY_BALL, 4);
		gen.add(Items.MUDDY_MANGROVE_ROOTS, 1, Items.MUD);
		gen.add(Ic2Items.AIR_CELL, 1, Ic2Items.EMPTY_CELL);
		gen.add(Ic2Items.FILLED_TIN_CAN, 1, Ic2Items.TIN_CAN);
		gen.add(Items.GUNPOWDER, 1, Ic2Items.SULFUR_DUST);
		gen.add(Ic2Items.HYDRATED_TIN_DUST, 1, Ic2Items.IODINE);
		gen.add(Items.NETHERRACK, 1, Ic2Items.SMALL_SULFUR_DUST);
		gen.add(Ic2Items.RESIN, 1, Ic2Items.RUBBER, 3);
		gen.add(Ic2Items.RUBBER_SAPLING, 1, Ic2Items.RUBBER);
		gen.add(Ic2Items.RUBBER_LOG, 1, Ic2Items.RUBBER);
		gen.add(Ic2Items.NETHERRACK_DUST, 1, Ic2Items.SMALL_SULFUR_DUST);
	}
}
