package ic2.api.recipe;

public record MachineRecipeResult<RI, RO, I>(MachineRecipe<RI, RO> recipe, I adjustedInput)
{


	public RO getOutput()
	{
		return this.recipe.getOutput();
	}


}
