package ic2.data.recipe;

import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class BlockCutterRecipeProvider extends Ic2RecipeProvider
{
	public BlockCutterRecipeProvider(PackOutput packOutput)
	{
		super(packOutput);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>> gen = (BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>>) (new BasicMachineRecipeGenerator<>(
			consumer, Ic2RecipeSerializers.BLOCK_CUTTER, true
		));
		gen.cutterLevel(2).add(ItemTags.OAK_LOGS, 1, Items.OAK_PLANKS, 6);
		gen.cutterLevel(2).add(ItemTags.BIRCH_LOGS, 1, Items.BIRCH_PLANKS, 6);
		gen.cutterLevel(2).add(ItemTags.SPRUCE_LOGS, 1, Items.SPRUCE_PLANKS, 6);
		gen.cutterLevel(2).add(ItemTags.ACACIA_LOGS, 1, Items.ACACIA_PLANKS, 6);
		gen.cutterLevel(2).add(ItemTags.JUNGLE_LOGS, 1, Items.JUNGLE_PLANKS, 6);
		gen.cutterLevel(2).add(ItemTags.DARK_OAK_LOGS, 1, Items.DARK_OAK_PLANKS, 6);
		gen.cutterLevel(2).add(ItemTags.MANGROVE_LOGS, 1, Items.MANGROVE_PLANKS, 6);
		gen.cutterLevel(2).add(ItemTags.PLANKS, 1, Items.STICK, 3);
		gen.cutterLevel(2).add(Ic2ItemTags.LEAD_BLOCKS, 1, Ic2Items.LEAD_PLATE, 9);
		gen.cutterLevel(2).add(Ic2ItemTags.TIN_BLOCKS, 1, Ic2Items.TIN_PLATE, 9);
		gen.cutterLevel(2).add(Ic2ItemTags.BRONZE_BLOCKS, 1, Ic2Items.BRONZE_PLATE, 9);
		gen.cutterLevel(2).add(Items.COPPER_BLOCK, 1, Ic2Items.COPPER_PLATE, 9);
		gen.cutterLevel(2).add(Items.GOLD_BLOCK, 1, Ic2Items.GOLD_PLATE, 9);
		gen.cutterLevel(5).add(Items.LAPIS_BLOCK, 1, Ic2Items.LAPIS_PLATE, 9);
		gen.cutterLevel(5).add(Items.IRON_BLOCK, 1, Ic2Items.IRON_PLATE, 9);
		gen.cutterLevel(8).add(Items.OBSIDIAN, 1, Ic2Items.OBSIDIAN_PLATE, 9);
		gen.cutterLevel(8).add(Ic2ItemTags.STEEL_BLOCKS, 1, Ic2Items.STEEL_PLATE, 9);
	}
}
