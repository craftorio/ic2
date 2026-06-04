// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraftforge.common.ForgeHooks;
import net.minecraft.util.NonNullList;
import net.minecraft.item.Item;
import ic2.core.util.StackUtil;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryCrafting;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import ic2.api.item.ICustomDamageItem;
import net.minecraft.item.crafting.IRecipe;

public class GradualRecipe implements IRecipe
{
    public ICustomDamageItem item;
    public ItemStack chargeMaterial;
    public int amount;
    public boolean hidden;
    private ResourceLocation name;
    
    public static void addAndRegister(final ItemStack itemToFill, final int amount, final Object... args) {
        try {
            if (itemToFill == null) {
                AdvRecipe.displayError("Null item to fill", null, null, true);
            }
            else {
                if (!(itemToFill.getItem() instanceof ICustomDamageItem)) {
                    AdvRecipe.displayError("Filling item must extends ItemGradualInt", null, itemToFill, true);
                }
                final ICustomDamageItem fillingItem = (ICustomDamageItem)itemToFill.getItem();
                Boolean hidden = false;
                ItemStack filler = null;
                for (final Object o : args) {
                    if (o instanceof Boolean) {
                        hidden = (Boolean)o;
                    }
                    else {
                        try {
                            filler = AdvRecipe.getRecipeObject(o).getInputs().get(0);
                            break;
                        }
                        catch (final IndexOutOfBoundsException e) {
                            AdvRecipe.displayError("Invalid filler item: " + o, null, itemToFill, true);
                        }
                        catch (final Exception e2) {
                            e2.printStackTrace();
                            AdvRecipe.displayError("unknown type", "O: " + o + "\nT: " + o.getClass().getName(), itemToFill, true);
                        }
                    }
                }
                Rezepte.registerRecipe((IRecipe)new GradualRecipe(fillingItem, filler, amount, hidden));
            }
        }
        catch (final RuntimeException e3) {
            if (!MainConfig.ignoreInvalidRecipes) {
                throw e3;
            }
        }
    }
    
    public GradualRecipe(final ICustomDamageItem item, final ItemStack chargeMaterial, final int amount) {
        this(item, chargeMaterial, amount, false);
    }
    
    public GradualRecipe(final ICustomDamageItem item, final ItemStack chargeMaterial, final int amount, final boolean hidden) {
        this.item = item;
        this.chargeMaterial = chargeMaterial;
        this.amount = amount;
        this.hidden = hidden;
    }
    
    public boolean matches(final InventoryCrafting ic, final World world) {
        return this.getCraftingResult(ic) != StackUtil.emptyStack;
    }
    
    public ItemStack getCraftingResult(final InventoryCrafting ic) {
        ItemStack gridItem = null;
        int chargeMats = 0;
        for (int slot = 0; slot < ic.getSizeInventory(); ++slot) {
            final ItemStack stack = ic.getStackInSlot(slot);
            if (!StackUtil.isEmpty(stack)) {
                if (gridItem == null && stack.getItem() == this.item) {
                    gridItem = stack;
                }
                else {
                    if (!StackUtil.checkItemEquality(stack, this.chargeMaterial)) {
                        return StackUtil.emptyStack;
                    }
                    ++chargeMats;
                }
            }
        }
        if (gridItem != null && chargeMats > 0) {
            final ItemStack stack2 = gridItem.copy();
            int damage = this.item.getCustomDamage(stack2) - this.amount * chargeMats;
            if (damage > this.item.getMaxCustomDamage(stack2)) {
                damage = this.item.getMaxCustomDamage(stack2);
            }
            else if (damage < 0) {
                damage = 0;
            }
            this.item.setCustomDamage(stack2, damage);
            return stack2;
        }
        return StackUtil.emptyStack;
    }
    
    public ItemStack getRecipeOutput() {
        return new ItemStack((Item)this.item);
    }
    
    public NonNullList<ItemStack> getRemainingItems(final InventoryCrafting inv) {
        return (NonNullList<ItemStack>)ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }
    
    public boolean canShow() {
        return AdvRecipe.canShow(new Object[] { this.chargeMaterial }, this.getRecipeOutput(), this.hidden);
    }
    
    public IRecipe setRegistryName(final ResourceLocation name) {
        this.name = name;
        return (IRecipe)this;
    }
    
    public ResourceLocation getRegistryName() {
        return this.name;
    }
    
    public Class<IRecipe> getRegistryType() {
        return IRecipe.class;
    }
    
    public boolean canFit(final int x, final int y) {
        return x * y >= 2;
    }
}
