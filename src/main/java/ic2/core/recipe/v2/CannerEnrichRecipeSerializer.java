package ic2.core.recipe.v2;

import com.google.gson.JsonObject;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.ref.Ic2RecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerEnrichRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>>
{
	public RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> read(ResourceLocation id, JsonObject json)
	{
		Ic2FluidStack input = RecipeIo.parseFluidStack(GsonHelper.m_13930_(json, "input_ingredient"));
		IRecipeInput additive = RecipeIo.parseInput(json.get("additive_ingredient"));
		Ic2FluidStack result = RecipeIo.parseFluidStack(GsonHelper.m_13930_(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), id, this, Ic2RecipeTypes.CANNER_ENRICH);
	}

	public RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> read(ResourceLocation id, FriendlyByteBuf buf)
	{
		Ic2FluidStack input = RecipeIo.readFluidStack(buf);
		IRecipeInput additive = RecipeIo.readInput(buf);
		Ic2FluidStack result = RecipeIo.readFluidStack(buf);
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), id, this, Ic2RecipeTypes.CANNER_ENRICH);
	}

	public void write(FriendlyByteBuf buf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> recipe)
	{
		RecipeIo.writeFluidStack(buf, recipe.recipe().getInput().fluid);
		RecipeIo.writeInput(buf, recipe.recipe().getInput().additive);
		RecipeIo.writeFluidStack(buf, recipe.recipe().getOutput());
	}
}
