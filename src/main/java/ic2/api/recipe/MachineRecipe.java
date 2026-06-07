package ic2.api.recipe;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class MachineRecipe<I, O>
{
	private final I input;
	private final O output;
	private final CompoundTag meta;

	public MachineRecipe(I input, O output)
	{
		this(input, output, null);
	}

	public MachineRecipe(I input, O output, CompoundTag meta)
	{
		this.input = input;
		this.output = output;
		this.meta = meta;
	}

	public I getInput()
	{
		return this.input;
	}

	public O getOutput()
	{
		return this.output;
	}

	@Nullable
	public CompoundTag getMetaData()
	{
		return this.meta;
	}

	public <AI> MachineRecipeResult<I, O, AI> getResult(AI adjustedInput)
	{
		return new MachineRecipeResult<>(this, adjustedInput);
	}
}
