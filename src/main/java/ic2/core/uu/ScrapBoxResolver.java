// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.Iterator;
import java.util.Collections;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import java.util.Map;
import ic2.api.recipe.Recipes;
import java.util.ArrayList;
import java.util.List;

public class ScrapBoxResolver implements IRecipeResolver
{
    private static final double transformCost = 1.0;
    
    @Override
    public List<RecipeTransformation> getTransformations() {
        final List<RecipeTransformation> ret = new ArrayList<RecipeTransformation>();
        final Map<ItemStack, Float> dropMap = Recipes.scrapboxDrops.getDrops();
        for (final Map.Entry<ItemStack, Float> drop : dropMap.entrySet()) {
            if (StackUtil.isEmpty(drop.getKey())) {
                IC2.log.warn(LogCategory.Uu, "Invalid itemstack in scrapbox drops detected.");
            }
            else {
                final int amount = Math.max(1, Math.round(1.0f / drop.getValue()));
                final List<LeanItemStack> input = Collections.singletonList(new LeanItemStack(ItemName.crafting.getItemStack(CraftingItemType.scrap_box), amount));
                ret.add(new RecipeTransformation(1.0, Collections.singletonList(input), new LeanItemStack[] { new LeanItemStack(drop.getKey()) }));
            }
        }
        return ret;
    }
}
