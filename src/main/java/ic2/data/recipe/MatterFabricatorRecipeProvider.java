package ic2.data.recipe;

import ic2.core.ref.Ic2Items;
import ic2.data.recipe.helper.Ic2RecipeProvider;
import ic2.data.recipe.helper.MatterFabricRecipeGenerator;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;

public class MatterFabricatorRecipeProvider extends Ic2RecipeProvider
{
	public MatterFabricatorRecipeProvider(PackOutput packOutput)
	{
		super(packOutput);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		MatterFabricRecipeGenerator gen = new MatterFabricRecipeGenerator(consumer);
		gen.add(Ic2Items.SCRAP, 1, 5000);
		gen.add(Ic2Items.SCRAP_BOX, 1, 45000);
	}
}
