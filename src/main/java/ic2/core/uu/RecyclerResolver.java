// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import ic2.api.recipe.IMachineRecipeManager;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import java.util.Arrays;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import java.util.Collections;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.api.recipe.Recipes;
import java.util.Collection;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;

public class RecyclerResolver implements ILateRecipeResolver
{
    private static final double transformCost;
    
    @Override
    public List<RecipeTransformation> getTransformations(final Iterable<LeanItemStack> obtainableStacks) {
        final List<LeanItemStack> input = new ArrayList<LeanItemStack>();
        for (final LeanItemStack obtainableStack : obtainableStacks) {
            final ItemStack stack = obtainableStack.toMcStack();
            if (StackUtil.isEmpty(stack)) {
                continue;
            }
            if (((IMachineRecipeManager<Object, Collection, ItemStack>)Recipes.recycler).apply(stack, false).getOutput().isEmpty()) {
                continue;
            }
            input.add(new LeanItemStack(stack, TileEntityRecycler.recycleChance()));
        }
        return Arrays.asList(new RecipeTransformation(RecyclerResolver.transformCost, Collections.singletonList(input), new LeanItemStack[] { new LeanItemStack(ItemName.crafting.getItemStack(CraftingItemType.scrap)) }));
    }
    
    static {
        transformCost = 55.0 * TileEntityRecycler.recycleChance() / 4000.0 * 107.0;
    }
}
