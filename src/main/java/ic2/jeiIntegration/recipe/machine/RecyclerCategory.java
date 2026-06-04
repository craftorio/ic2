// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.ingredients.IIngredients;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.ArrayList;
import ic2.core.block.ITeBlock;
import ic2.api.recipe.Recipes;
import ic2.core.ref.TeBlock;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.api.recipe.IBasicMachineRecipeManager;

public class RecyclerCategory extends DynamicCategory<IBasicMachineRecipeManager>
{
    private final List<List<ItemStack>> trueInputs;
    
    public RecyclerCategory(final IGuiHelper guiHelper) {
        super(TeBlock.recycler, Recipes.recycler, guiHelper);
        final List<ItemStack> items = new ArrayList<ItemStack>();
        if (Recipes.recyclerWhitelist.isEmpty()) {
            for (final Item i : ForgeRegistries.ITEMS) {
                final ItemStack stack = new ItemStack(i, 1, 32767);
                if (!Recipes.recyclerBlacklist.contains(stack)) {
                    items.add(stack);
                }
            }
        }
        else {
            for (final IRecipeInput stack2 : Recipes.recyclerWhitelist) {
                items.addAll(stack2.getInputs());
            }
        }
        this.trueInputs = Collections.singletonList(items);
    }
    
    @Override
    protected List<List<ItemStack>> getInputStacks(final IIngredients wrapper) {
        return this.trueInputs;
    }
}
