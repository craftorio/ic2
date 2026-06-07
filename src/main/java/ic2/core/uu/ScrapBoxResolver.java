package ic2.core.uu;

import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.world.item.ItemStack;

public class ScrapBoxResolver implements IRecipeResolver
{
	private static final double transformCost = 1.0;

	@Override
	public List<RecipeTransformation> getTransformations()
	{
		List<RecipeTransformation> ret = new ArrayList<>();
		Map<ItemStack, Float> dropMap = Recipes.scrapboxDrops.getDrops();

		for (Entry<ItemStack, Float> drop : dropMap.entrySet())
		{
			if (StackUtil.isEmpty(drop.getKey()))
			{
				IC2.log.warn(LogCategory.Uu, "Invalid itemstack in scrapbox drops detected.");
			} else
			{
				int amount = Math.max(1, Math.round(1.0F / drop.getValue()));
				List<LeanItemStack> input = Collections.singletonList(new LeanItemStack(Ic2Items.SCRAP_BOX, amount));
				ret.add(new RecipeTransformation(1.0, Collections.singletonList(input), new LeanItemStack(drop.getKey())));
			}
		}

		return ret;
	}
}
