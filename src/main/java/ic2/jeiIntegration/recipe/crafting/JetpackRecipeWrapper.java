// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.crafting;

import java.util.Arrays;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import mezz.jei.api.ingredients.IIngredients;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.ArrayList;
import ic2.core.util.ItemComparableItemStack;
import java.util.HashSet;
import net.minecraft.util.NonNullList;
import ic2.core.item.armor.jetpack.JetpackHandler;
import java.util.List;
import net.minecraft.item.ItemStack;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class JetpackRecipeWrapper extends BlankRecipeWrapper
{
    private final ItemStack in;
    private final ItemStack out;
    private static List<JetpackRecipeWrapper> jetpackRecipes;
    
    private JetpackRecipeWrapper(final ItemStack in) {
        this.in = in;
        final ItemStack out = in.copy();
        JetpackHandler.setJetpackAttached(out, true);
        this.out = out;
    }
    
    public static List<JetpackRecipeWrapper> generateJetpackRecipes() {
        if (JetpackRecipeWrapper.jetpackRecipes != null) {
            return JetpackRecipeWrapper.jetpackRecipes;
        }
        final NonNullList<ItemStack> stacks = (NonNullList<ItemStack>)NonNullList.create();
        final Set<ItemComparableItemStack> added = new HashSet<ItemComparableItemStack>();
        JetpackRecipeWrapper.jetpackRecipes = new ArrayList<JetpackRecipeWrapper>(100);
        for (final Item item : ForgeRegistries.ITEMS) {
            if (JetpackAttachmentRecipe.blacklistedItems.contains(item)) {
                continue;
            }
            stacks.clear();
            added.clear();
            item.getSubItems(CreativeTabs.SEARCH, (NonNullList)stacks);
            for (final ItemStack stack : stacks) {
                if (EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.CHEST) {
                    final ItemComparableItemStack comparable = new ItemComparableItemStack(stack, false);
                    if (added.contains(comparable)) {
                        continue;
                    }
                    JetpackRecipeWrapper.jetpackRecipes.add(new JetpackRecipeWrapper(stack));
                    added.add(comparable);
                }
            }
        }
        return JetpackRecipeWrapper.jetpackRecipes;
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInputs((Class)ItemStack.class, (List)Arrays.asList(ItemName.jetpack_electric.getItemStack(), ItemName.crafting.getItemStack(CraftingItemType.jetpack_attachment_plate), this.in));
        ingredients.setOutput((Class)ItemStack.class, (Object)this.out);
    }
}
