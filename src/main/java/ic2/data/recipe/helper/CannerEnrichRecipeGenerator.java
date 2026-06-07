package ic2.data.recipe.helper;

import com.google.gson.JsonObject;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.recipe.input.RecipeInputBase;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.json.Ic2RecipeJsonProvider;

import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerEnrichRecipeGenerator
{
	private final Consumer<FinishedRecipe> exporter;

	public CannerEnrichRecipeGenerator(Consumer<FinishedRecipe> exporter)
	{
		this.exporter = exporter;
	}

	public void add(Ic2FluidStack input, RecipeInputBase additive, Ic2FluidStack output, String name)
	{
		this.exporter.accept(new Ic2RecipeJsonProvider(Ic2RecipeSerializers.CANNER_ENRICH, name)
		{
			@Override
			public void m_7917_(JsonObject json)
			{
				json.add("input_ingredient", RecipeIo.fluidStackToJson(input));
				json.add("additive_ingredient", additive.toJson());
				json.add("result", RecipeIo.fluidStackToJson(output));
			}
		});
	}
}
