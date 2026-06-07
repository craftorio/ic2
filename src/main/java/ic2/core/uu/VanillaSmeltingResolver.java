package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class VanillaSmeltingResolver implements IRecipeResolver
{
	private static final double transformCost = 14.0;

	@Override
	public List<RecipeTransformation> getTransformations()
	{
		List<RecipeTransformation> ret = new ArrayList<>();

		for (SmeltingRecipe recipe : IC2.sideProxy.getRecipeManager().m_44013_(RecipeType.f_44108_))
		{
			try
			{
				List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(recipe.m_7527_());
				LeanItemStack output = new LeanItemStack(recipe.m_8043_());
				ret.add(new RecipeTransformation(14.0, inputs, output));
			} catch (IllegalArgumentException e)
			{
				IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
			}
		}

		return ret;
	}
}
