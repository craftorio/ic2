package ic2.core.recipe.input;

import com.google.gson.JsonElement;
import ic2.api.recipe.IRecipeInput;
import ic2.core.util.StackUtil;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;

public abstract class RecipeInputBase implements IRecipeInput
{
	private List<ItemStack> inputs = null;

	protected RecipeInputBase()
	{
	}

	public abstract JsonElement toJson();

	protected abstract List<ItemStack> listStacks();

	@Override
	public final List<ItemStack> getInputs()
	{
		if (this.inputs == null)
		{
			this.inputs = this.listStacks();
			this.inputs.replaceAll(stack -> StackUtil.setImmutableSize(stack, this.getAmount()));
			this.inputs = Collections.unmodifiableList(this.inputs);
		}

		return this.inputs;
	}
}
