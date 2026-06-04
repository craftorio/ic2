// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraft.item.ItemArmor;
import ic2.core.util.StackUtil;
import net.minecraft.inventory.InventoryCrafting;
import ic2.core.init.MainConfig;
import net.minecraft.item.crafting.IRecipe;
import ic2.core.init.Rezepte;
import net.minecraft.item.ItemStack;

public class ColourCarryingRecipe extends AdvRecipe
{
    public static void addAndRegister(final ItemStack result, final Object... args) {
        try {
            Rezepte.registerRecipe((IRecipe)new ColourCarryingRecipe(result, args));
        }
        catch (final RuntimeException e) {
            if (!MainConfig.ignoreInvalidRecipes) {
                throw e;
            }
        }
    }
    
    public ColourCarryingRecipe(final ItemStack result, final Object... args) {
        super(result, args);
    }
    
    @Override
    public ItemStack getCraftingResult(final InventoryCrafting craftingInv) {
        final ItemStack initialResult = super.getCraftingResult(craftingInv);
        if (!StackUtil.isEmpty(initialResult) && initialResult.getItem() instanceof ItemArmor) {
            int colour = -1;
            for (int slot = 0; slot < craftingInv.getSizeInventory(); ++slot) {
                final ItemStack offer = craftingInv.getStackInSlot(slot);
                if (!StackUtil.isEmpty(initialResult) && offer.getItem() instanceof ItemArmor) {
                    colour = ((ItemArmor)offer.getItem()).getColor(offer);
                    break;
                }
            }
            if (colour != -1) {
                ((ItemArmor)initialResult.getItem()).setColor(initialResult, colour);
            }
        }
        return initialResult;
    }
}
