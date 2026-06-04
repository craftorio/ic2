// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryCrafting;
import java.util.Iterator;
import net.minecraft.item.ItemArmor;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.Recipes;
import ic2.core.util.Ic2Color;
import java.util.HashMap;
import ic2.api.recipe.IRecipeInput;
import java.util.Map;
import net.minecraft.item.crafting.RecipesArmorDyes;

public class ArmorDyeingRecipe extends RecipesArmorDyes
{
    private static final Map<IRecipeInput, int[]> stackToRGB;
    protected final IRecipeInput armour;
    
    private static Map<IRecipeInput, int[]> buildDyeMap() {
        final Map<IRecipeInput, int[]> ret = new HashMap<IRecipeInput, int[]>();
        for (final Ic2Color colour : Ic2Color.values) {
            final float[] dyeMap = colour.mcColor.getColorComponentValues();
            assert dyeMap != null;
            ret.put(Recipes.inputFactory.forOreDict(colour.oreDictDyeName), new int[] { (int)(dyeMap[0] * 255.0f), (int)(dyeMap[1] * 255.0f), (int)(dyeMap[2] * 255.0f) });
        }
        return ret;
    }
    
    public ArmorDyeingRecipe(final ItemStack armour) {
        this(Recipes.inputFactory.forStack(armour));
        if (StackUtil.isEmpty(armour) || !(armour.getItem() instanceof ItemArmor)) {
            throw new IllegalArgumentException("Invalid input stack: " + StackUtil.toStringSafe(armour));
        }
    }
    
    public ArmorDyeingRecipe(final Class<? extends ItemArmor> type) {
        this(new RecipeInputClass(type));
        if (type == null || !ItemArmor.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Invalid input class: " + type);
        }
    }
    
    public ArmorDyeingRecipe(final IRecipeInput input) {
        this.armour = input;
    }
    
    public static boolean isDye(final ItemStack stack) {
        for (final IRecipeInput input : ArmorDyeingRecipe.stackToRGB.keySet()) {
            if (input.matches(stack)) {
                return true;
            }
        }
        return false;
    }
    
    public static int[] getColourForStack(final ItemStack stack) {
        for (final Map.Entry<IRecipeInput, int[]> entry : ArmorDyeingRecipe.stackToRGB.entrySet()) {
            if (entry.getKey().matches(stack)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public boolean matches(final InventoryCrafting craftingInv, final World world) {
        ItemStack Qsuit = null;
        for (int slot = 0; slot < craftingInv.getSizeInventory(); ++slot) {
            final ItemStack stack = craftingInv.getStackInSlot(slot);
            if (!StackUtil.isEmpty(stack)) {
                if (this.armour.matches(stack)) {
                    if (Qsuit != null) {
                        return false;
                    }
                    Qsuit = stack;
                }
                else if (getColourForStack(stack) == null) {
                    return false;
                }
            }
        }
        return Qsuit != null;
    }
    
    public ItemStack getCraftingResult(final InventoryCrafting craftingInv) {
        ItemStack armourStack = null;
        ItemArmor Qsuit = null;
        final int[] newRBG = new int[3];
        int totalColour = 0;
        int numberOfDyes = 0;
        for (int slot = 0; slot < craftingInv.getSizeInventory(); ++slot) {
            final ItemStack stack = craftingInv.getStackInSlot(slot);
            if (!StackUtil.isEmpty(stack)) {
                if (this.armour.matches(stack)) {
                    Qsuit = (ItemArmor)stack.getItem();
                    if (!StackUtil.isEmpty(armourStack)) {
                        return StackUtil.emptyStack;
                    }
                    armourStack = StackUtil.copyWithSize(stack, 1);
                    if (Qsuit.hasColor(stack)) {
                        final int oldColour = Qsuit.getColor(armourStack);
                        final int r = oldColour >> 16 & 0xFF;
                        final int g = oldColour >> 8 & 0xFF;
                        final int b = oldColour & 0xFF;
                        totalColour += Math.max(r, Math.max(g, b));
                        final int[] array = newRBG;
                        final int n = 0;
                        array[n] += r;
                        final int[] array2 = newRBG;
                        final int n2 = 1;
                        array2[n2] += g;
                        final int[] array3 = newRBG;
                        final int n3 = 2;
                        array3[n3] += b;
                        ++numberOfDyes;
                    }
                }
                else {
                    final int[] dyeRGB = getColourForStack(stack);
                    if (dyeRGB == null) {
                        return StackUtil.emptyStack;
                    }
                    final int r = dyeRGB[0];
                    final int g = dyeRGB[1];
                    final int b = dyeRGB[2];
                    totalColour += Math.max(r, Math.max(g, b));
                    final int[] array4 = newRBG;
                    final int n4 = 0;
                    array4[n4] += r;
                    final int[] array5 = newRBG;
                    final int n5 = 1;
                    array5[n5] += g;
                    final int[] array6 = newRBG;
                    final int n6 = 2;
                    array6[n6] += b;
                    ++numberOfDyes;
                }
            }
        }
        if (Qsuit == null || numberOfDyes == 0) {
            return StackUtil.emptyStack;
        }
        if (Qsuit.hasColor(armourStack) && numberOfDyes == 1) {
            Qsuit.removeColor(armourStack);
        }
        else {
            int averageRed = newRBG[0] / numberOfDyes;
            int averageGreen = newRBG[1] / numberOfDyes;
            int averageBlue = newRBG[2] / numberOfDyes;
            final float gain = totalColour / (float)numberOfDyes;
            final float averageMax = (float)Math.max(averageRed, Math.max(averageGreen, averageBlue));
            averageRed = (int)(averageRed * gain / averageMax);
            averageGreen = (int)(averageGreen * gain / averageMax);
            averageBlue = (int)(averageBlue * gain / averageMax);
            int finalColour = (averageRed << 8) + averageGreen;
            finalColour = (finalColour << 8) + averageBlue;
            Qsuit.setColor(armourStack, finalColour);
        }
        return armourStack;
    }
    
    static {
        stackToRGB = buildDyeMap();
    }
    
    public static class RecipeInputClass extends RecipeInputBase implements IRecipeInput
    {
        protected final Class<?> type;
        protected final int amount;
        
        public RecipeInputClass(final Class<?> type) {
            this(type, 1);
        }
        
        public RecipeInputClass(final Class<?> type, final int amount) {
            this.type = type;
            this.amount = amount;
        }
        
        @Override
        public boolean matches(final ItemStack subject) {
            return this.matches(subject.getItem());
        }
        
        protected boolean matches(final Item item) {
            return this.type.isInstance(item);
        }
        
        @Override
        public int getAmount() {
            return this.amount;
        }
        
        @Override
        public List<ItemStack> getInputs() {
            final List<ItemStack> ret = new ArrayList<ItemStack>();
            for (final Item item : ForgeRegistries.ITEMS) {
                if (this.matches(item)) {
                    ret.add(new ItemStack(item, 1, 32767));
                }
            }
            return ret;
        }
        
        public String toString() {
            return "RInputClass<" + this.type + ", " + this.amount + '>';
        }
        
        public boolean equals(final Object obj) {
            final RecipeInputClass other;
            return obj != null && this.getClass() == obj.getClass() && (other = (RecipeInputClass)obj).type == this.type && other.amount == this.amount;
        }
    }
}
