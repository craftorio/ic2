package ic2.core.recipe.v2;

import com.google.gson.JsonObject;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.ref.Ic2RecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerBottleRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>>
{
	public RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> read(ResourceLocation id, JsonObject json)
	{
		IRecipeInput container = RecipeIo.parseInput(json.get("container_ingredient"));
		IRecipeInput fill = RecipeIo.parseInput(json.get("fill_ingredient"));
		ItemStack output = RecipeIo.parseOutput(GsonHelper.m_13930_(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), id, this, Ic2RecipeTypes.CANNER_BOTTLE);
	}

	public RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> read(ResourceLocation id, FriendlyByteBuf buf)
	{
		IRecipeInput container = RecipeIo.readInput(buf);
		IRecipeInput fill = RecipeIo.readInput(buf);
		ItemStack output = buf.m_130267_();
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), id, this, Ic2RecipeTypes.CANNER_BOTTLE);
	}

	public void write(FriendlyByteBuf buf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> recipe)
	{
		RecipeIo.writeInput(buf, recipe.recipe().getInput().container);
		RecipeIo.writeInput(buf, recipe.recipe().getInput().fill);
		buf.m_130055_(recipe.recipe().getOutput());
	}
}
