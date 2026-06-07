package ic2.core.uu;

import java.util.ArrayList;
import java.util.List;

public class CannerBottleSolidResolver implements IRecipeResolver
{
	private static final double transformCost = 0.0;

	@Override
	public List<RecipeTransformation> getTransformations()
	{
		return new ArrayList<>();
	}
}
