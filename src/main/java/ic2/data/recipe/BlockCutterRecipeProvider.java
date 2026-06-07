package ic2.data.recipe;

import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class BlockCutterRecipeProvider extends Ic2RecipeProvider
{
	public BlockCutterRecipeProvider(DataGenerator root)
	{
		super(root);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>> gen = (BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>>) (new BasicMachineRecipeGenerator<>(
			consumer, Ic2RecipeSerializers.BLOCK_CUTTER, true
		));
		gen.cutterLevel(2).add(ItemTags.f_13184_, 1, Items.f_42647_, 6);
		gen.cutterLevel(2).add(ItemTags.f_13185_, 1, Items.f_42753_, 6);
		gen.cutterLevel(2).add(ItemTags.f_13188_, 1, Items.f_42700_, 6);
		gen.cutterLevel(2).add(ItemTags.f_13186_, 1, Items.f_42795_, 6);
		gen.cutterLevel(2).add(ItemTags.f_13187_, 1, Items.f_42794_, 6);
		gen.cutterLevel(2).add(ItemTags.f_13183_, 1, Items.f_42796_, 6);
		gen.cutterLevel(2).add(ItemTags.f_215869_, 1, Items.f_220174_, 6);
		gen.cutterLevel(2).add(ItemTags.f_13168_, 1, Items.f_42398_, 3);
		gen.cutterLevel(2).add(Ic2ItemTags.LEAD_BLOCKS, 1, Ic2Items.LEAD_PLATE, 9);
		gen.cutterLevel(2).add(Ic2ItemTags.TIN_BLOCKS, 1, Ic2Items.TIN_PLATE, 9);
		gen.cutterLevel(2).add(Ic2ItemTags.BRONZE_BLOCKS, 1, Ic2Items.BRONZE_PLATE, 9);
		gen.cutterLevel(2).add(Items.f_151000_, 1, Ic2Items.COPPER_PLATE, 9);
		gen.cutterLevel(2).add(Items.f_41912_, 1, Ic2Items.GOLD_PLATE, 9);
		gen.cutterLevel(5).add(Items.f_41854_, 1, Ic2Items.LAPIS_PLATE, 9);
		gen.cutterLevel(5).add(Items.f_41913_, 1, Ic2Items.IRON_PLATE, 9);
		gen.cutterLevel(8).add(Items.f_41999_, 1, Ic2Items.OBSIDIAN_PLATE, 9);
		gen.cutterLevel(8).add(Ic2ItemTags.STEEL_BLOCKS, 1, Ic2Items.STEEL_PLATE, 9);
	}
}
