package ic2.api.recipe;

import java.util.Collection;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class MachineRecipeWeighted<I> extends MachineRecipe<I, Collection<ItemStack>>
{
	private final RecipeOutputWeighted outputWeighted;

	public MachineRecipeWeighted(I input, RecipeOutputWeighted outputWeighted)
	{
		super(input, null);
		this.outputWeighted = outputWeighted;
	}

	public MachineRecipeWeighted(I input, RecipeOutputWeighted outputWeighted, CompoundTag meta)
	{
		super(input, null, meta);
		this.outputWeighted = outputWeighted;
	}

	public Collection<ItemStack> getOutput()
	{
		return List.of(this.outputWeighted.drawOutput());
	}

	public RecipeOutputWeighted getOutputWeighted()
	{
		return this.outputWeighted;
	}
}
