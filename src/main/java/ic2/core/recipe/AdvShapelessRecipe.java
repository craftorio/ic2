// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeHooks;
import net.minecraft.util.NonNullList;
import java.util.List;
import ic2.api.item.ElectricItem;
import java.util.Vector;
import ic2.core.util.StackUtil;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryCrafting;
import ic2.core.util.Util;
import ic2.api.recipe.ICraftingRecipeManager;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import net.minecraft.util.ResourceLocation;
import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class AdvShapelessRecipe implements IRecipe
{
    public ItemStack output;
    public IRecipeInput[] input;
    public boolean hidden;
    public boolean consuming;
    private ResourceLocation name;
    
    public static void addAndRegister(final ItemStack result, final Object... args) {
        try {
            Rezepte.registerRecipe((IRecipe)new AdvShapelessRecipe(result, args));
        }
        catch (final RuntimeException e) {
            if (!MainConfig.ignoreInvalidRecipes) {
                throw e;
            }
        }
    }
    
    public AdvShapelessRecipe(ItemStack result, final Object... args) {
        if (result == null) {
            AdvRecipe.displayError("null result", null, null, true);
        }
        else {
            result = result.copy();
        }
        this.input = new IRecipeInput[args.length - Util.countInArray(args, Boolean.class, ICraftingRecipeManager.AttributeContainer.class)];
        int inputIndex = 0;
        for (final Object o : args) {
            if (o instanceof Boolean) {
                this.hidden = (boolean)o;
            }
            else if (o instanceof ICraftingRecipeManager.AttributeContainer) {
                this.hidden = ((ICraftingRecipeManager.AttributeContainer)o).hidden;
                this.consuming = ((ICraftingRecipeManager.AttributeContainer)o).consuming;
            }
            else {
                try {
                    this.input[inputIndex++] = AdvRecipe.getRecipeObject(o);
                }
                catch (final Exception e) {
                    e.printStackTrace();
                    AdvRecipe.displayError("unknown type", "O: " + o + "\nT: " + o.getClass().getName(), result, true);
                }
            }
        }
        if (inputIndex != this.input.length) {
            AdvRecipe.displayError("length calculation error", "I: " + inputIndex + "\nL: " + this.input.length, result, true);
        }
        this.output = result;
    }
    
    public boolean matches(final InventoryCrafting inventorycrafting, final World world) {
        return this.getCraftingResult(inventorycrafting) != StackUtil.emptyStack;
    }
    
    public ItemStack getCraftingResult(final InventoryCrafting inventorycrafting) {
        final int offerSize = inventorycrafting.getSizeInventory();
        if (offerSize < this.input.length) {
            return StackUtil.emptyStack;
        }
        final List<IRecipeInput> unmatched = new Vector<IRecipeInput>();
        for (final IRecipeInput o : this.input) {
            unmatched.add(o);
        }
        double outputCharge = 0.0;
    Label_0176:
        for (int i = 0; i < offerSize; ++i) {
            final ItemStack offer = inventorycrafting.getStackInSlot(i);
            if (!StackUtil.isEmpty(offer)) {
                for (int j = 0; j < unmatched.size(); ++j) {
                    if (unmatched.get(j).matches(offer)) {
                        outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
                        unmatched.remove(j);
                        continue Label_0176;
                    }
                }
                return StackUtil.emptyStack;
            }
        }
        if (!unmatched.isEmpty()) {
            return StackUtil.emptyStack;
        }
        final ItemStack ret = this.output.copy();
        ElectricItem.manager.charge(ret, outputCharge, Integer.MAX_VALUE, true, false);
        return ret;
    }
    
    public ItemStack getRecipeOutput() {
        return this.output;
    }
    
    public boolean canShow() {
        return AdvRecipe.canShow(this.input, this.output, this.hidden);
    }
    
    public NonNullList<ItemStack> getRemainingItems(final InventoryCrafting inv) {
        return (NonNullList<ItemStack>)(this.consuming ? NonNullList.withSize(inv.getSizeInventory(), (Object)StackUtil.emptyStack) : ForgeHooks.defaultRecipeGetRemainingItems(inv));
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
        return x * y >= this.input.length;
    }
    
    public NonNullList<Ingredient> getIngredients() {
        final NonNullList<Ingredient> list = (NonNullList<Ingredient>)NonNullList.create();
        if (!this.hidden) {
            for (final IRecipeInput input : this.input) {
                list.add((Object)input.getIngredient());
            }
        }
        return list;
    }
    
    public boolean isDynamic() {
        return this.hidden;
    }
}
