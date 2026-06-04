// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import ic2.core.util.Util;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeHooks;
import net.minecraft.util.NonNullList;
import ic2.api.item.ElectricItem;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import ic2.api.recipe.Recipes;
import net.minecraftforge.fluids.Fluid;
import java.lang.reflect.Array;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fluids.FluidRegistry;
import java.util.Collection;
import ic2.core.util.ConfigUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryCrafting;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import org.apache.commons.lang3.StringUtils;
import ic2.api.recipe.ICraftingRecipeManager;
import java.util.ArrayList;
import java.util.HashMap;
import ic2.core.util.StackUtil;
import ic2.core.init.MainConfig;
import net.minecraft.item.crafting.IRecipe;
import ic2.core.init.Rezepte;
import net.minecraft.util.ResourceLocation;
import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class AdvRecipe implements IShapedRecipe
{
    private static final boolean debug;
    public final ItemStack output;
    public final IRecipeInput[] input;
    public final IRecipeInput[] inputMirrored;
    public final int[] masks;
    public final int[] masksMirrored;
    public final int inputWidth;
    public final int inputHeight;
    public final boolean hidden;
    public final boolean consuming;
    private ResourceLocation name;
    
    public static void addAndRegister(final ItemStack result, final Object... args) {
        try {
            Rezepte.registerRecipe((IRecipe)new AdvRecipe(result, args));
        }
        catch (final RuntimeException e) {
            if (!MainConfig.ignoreInvalidRecipes) {
                throw e;
            }
        }
    }
    
    public AdvRecipe(final ItemStack result, final Object... args) {
        if (StackUtil.isEmpty(result)) {
            displayError("null result", null, result, false);
        }
        final Map<Character, IRecipeInput> charMapping = new HashMap<Character, IRecipeInput>();
        final List<String> inputArrangement = new ArrayList<String>();
        Character lastChar = null;
        boolean isHidden = false;
        boolean isConsuming = false;
        boolean isFixedSize = false;
        for (final Object arg : args) {
            if (arg instanceof String) {
                if (lastChar == null) {
                    if (!charMapping.isEmpty()) {
                        displayError("oredict name without preceding char", "Name: " + arg, result, false);
                    }
                    final String str = (String)arg;
                    if (str.isEmpty() || str.length() > 3) {
                        displayError("none or too many crafting columns", "Input: " + str + "\nSize: " + str.length(), result, false);
                    }
                    inputArrangement.add(str);
                }
                else {
                    charMapping.put(lastChar, getRecipeObject(arg));
                    lastChar = null;
                }
            }
            else if (arg instanceof Character) {
                if (lastChar != null) {
                    displayError("two consecutive char definitions", "Input: " + arg + "\nprev. Input: " + lastChar, result, false);
                }
                lastChar = (Character)arg;
            }
            else if (arg instanceof Boolean) {
                isHidden = (boolean)arg;
            }
            else if (arg instanceof ICraftingRecipeManager.AttributeContainer) {
                isHidden = ((ICraftingRecipeManager.AttributeContainer)arg).hidden;
                isConsuming = ((ICraftingRecipeManager.AttributeContainer)arg).consuming;
                isFixedSize = ((ICraftingRecipeManager.AttributeContainer)arg).fixedSize;
            }
            else {
                if (lastChar == null) {
                    displayError("two consecutive char definitions", "Input: " + arg + "\nprev. Input: " + lastChar, result, false);
                }
                try {
                    final IRecipeInput last = charMapping.put(lastChar, getRecipeObject(arg));
                    if (last != null) {
                        displayError("duplicate char mapping", "Char: " + lastChar + "\nInput: " + arg + "\nType: " + arg.getClass().getName(), result, false);
                    }
                    lastChar = null;
                }
                catch (final Exception e) {
                    e.printStackTrace();
                    displayError("unknown type", "Input: " + arg + "\nType: " + arg.getClass().getName(), result, false);
                }
            }
        }
        this.hidden = isHidden;
        this.consuming = isConsuming;
        this.inputHeight = inputArrangement.size();
        if (lastChar != null) {
            displayError("one or more unused mapping chars", "Letter: " + lastChar, result, false);
        }
        if (this.inputHeight == 0 || this.inputHeight > 3) {
            displayError("none or too many crafting rows", "Size: " + inputArrangement.size(), result, false);
        }
        if (charMapping.size() == 0) {
            displayError("no mapping chars", null, result, false);
        }
        this.inputWidth = inputArrangement.get(0).length();
        if (AdvRecipe.debug && !isFixedSize) {
            if (StringUtils.containsOnly((CharSequence)inputArrangement.get(0), new char[] { ' ' })) {
                IC2.log.warn(LogCategory.Recipe, "Leading empty row in shaped recipe for %s (%s), from %s.", result, result.getDisplayName(), getCaller());
            }
            if (StringUtils.containsOnly((CharSequence)inputArrangement.get(this.inputHeight - 1), new char[] { ' ' })) {
                IC2.log.warn(LogCategory.Recipe, "Trailing empty row in shaped recipe for %s (%s), from %s.", result, result.getDisplayName(), getCaller());
            }
            for (int pass = 0; pass < 2; ++pass) {
                boolean found = true;
                for (int y = 0; y < this.inputHeight; ++y) {
                    final String str2 = inputArrangement.get(y);
                    if ((pass == 0 && str2.charAt(0) != ' ') || (pass == 1 && str2.charAt(this.inputWidth - 1) != ' ')) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    if (pass == 0) {
                        IC2.log.warn(LogCategory.Recipe, "Leading empty column in shaped recipe for %s (%s), from %s.", result, result.getDisplayName(), getCaller());
                    }
                    else {
                        IC2.log.warn(LogCategory.Recipe, "Trailing empty column in shaped recipe for %s (%s), from %s.", result, result.getDisplayName(), getCaller());
                    }
                }
            }
        }
        final int xMasks = -this.inputWidth + 4;
        final int yMasks = -this.inputHeight + 4;
        int mask = 0;
        final List<Object> inputs = new ArrayList<Object>();
        for (int y2 = 0; y2 < 3; ++y2) {
            String str3 = null;
            if (y2 < this.inputHeight) {
                str3 = inputArrangement.get(y2);
                if (str3.length() != this.inputWidth) {
                    displayError("no fixed width", "Expected: " + this.inputWidth + "\nGot: " + str3.length(), result, false);
                }
            }
            for (int x = 0; x < 3; ++x) {
                mask <<= 1;
                if (x < this.inputWidth && str3 != null) {
                    final char c = str3.charAt(x);
                    if (c != ' ') {
                        if (!charMapping.containsKey(c)) {
                            displayError("missing char mapping", "Letter: " + c, result, false);
                        }
                        inputs.add(charMapping.get(c));
                        mask |= 0x1;
                    }
                }
            }
        }
        this.input = inputs.toArray(new IRecipeInput[0]);
        boolean mirror = false;
        if (this.inputWidth != 1) {
            for (final String s : inputArrangement) {
                if (s.charAt(0) != s.charAt(this.inputWidth - 1)) {
                    mirror = true;
                    break;
                }
            }
        }
        if (!mirror) {
            this.inputMirrored = null;
        }
        else {
            final IRecipeInput[] tmp = new IRecipeInput[9];
            int i = 0;
            int j = 0;
            while (i < 9) {
                if ((mask & 1 << 8 - i) != 0x0) {
                    tmp[i] = this.input[j];
                    ++j;
                }
                ++i;
            }
            IRecipeInput old = tmp[0];
            tmp[0] = tmp[2];
            tmp[2] = old;
            old = tmp[3];
            tmp[3] = tmp[5];
            tmp[5] = old;
            old = tmp[6];
            tmp[6] = tmp[8];
            tmp[8] = old;
            this.inputMirrored = new IRecipeInput[this.input.length];
            int k = 0;
            int l = 0;
            while (k < 9) {
                if (tmp[k] != null) {
                    this.inputMirrored[l] = tmp[k];
                    ++l;
                }
                ++k;
            }
        }
        this.masks = new int[xMasks * yMasks];
        if (!mirror) {
            this.masksMirrored = null;
        }
        else {
            this.masksMirrored = new int[this.masks.length];
        }
        for (int y3 = 0; y3 < yMasks; ++y3) {
            final int yMask = mask >>> y3 * 3;
            for (int x2 = 0; x2 < xMasks; ++x2) {
                final int xyMask = yMask >>> x2;
                this.masks[x2 + y3 * xMasks] = xyMask;
                if (mirror) {
                    this.masksMirrored[x2 + y3 * xMasks] = ((xyMask << 2 & 0x124) | (xyMask & 0x92) | (xyMask >>> 2 & 0x49));
                }
            }
        }
        this.output = result;
    }
    
    public boolean matches(final InventoryCrafting inventorycrafting, final World world) {
        return this.getCraftingResult(inventorycrafting) != StackUtil.emptyStack;
    }
    
    public ItemStack getCraftingResult(final InventoryCrafting inventorycrafting) {
        final int size = inventorycrafting.getSizeInventory();
        int mask = 0;
        for (int i = 0; i < size; ++i) {
            mask <<= 1;
            if (!StackUtil.isEmpty(inventorycrafting.getStackInSlot(i))) {
                mask |= 0x1;
            }
        }
        if (size == 4) {
            mask = ((mask & 0xC) << 5 | (mask & 0x3) << 4);
        }
        if (checkMask(mask, this.masks)) {
            final ItemStack ret = this.checkItems((IInventory)inventorycrafting, this.input);
            if (!StackUtil.isEmpty(ret)) {
                return ret;
            }
        }
        if (this.masksMirrored != null && checkMask(mask, this.masksMirrored)) {
            final ItemStack ret = this.checkItems((IInventory)inventorycrafting, this.inputMirrored);
            if (!StackUtil.isEmpty(ret)) {
                return ret;
            }
        }
        return StackUtil.emptyStack;
    }
    
    public ItemStack getRecipeOutput() {
        return this.output;
    }
    
    public static boolean canShow(final Object[] input, final ItemStack output, final boolean hidden) {
        return !hidden || !ConfigUtil.getBool(MainConfig.get(), "misc/hideSecretRecipes");
    }
    
    public boolean canShow() {
        return canShow(this.input, this.output, this.hidden);
    }
    
    public static List<ItemStack> expand(final Object o) {
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        if (o instanceof IRecipeInput) {
            ret.addAll(((IRecipeInput)o).getInputs());
        }
        else if (o instanceof String) {
            final String s = (String)o;
            if (s.startsWith("liquid$")) {
                final String name = s.substring(7);
                final Fluid fluid = FluidRegistry.getFluid(name);
                ret.addAll(RecipeInputFluidContainer.getFluidContainer(fluid));
            }
            else {
                for (final ItemStack stack : OreDictionary.getOres((String)o)) {
                    if (!StackUtil.isEmpty(stack)) {
                        ret.add(stack);
                    }
                }
            }
        }
        else if (o instanceof ItemStack) {
            if (!StackUtil.isEmpty((ItemStack)o)) {
                ret.add((ItemStack)o);
            }
        }
        else if (o.getClass().isArray()) {
            assert Array.getLength(o) != 0 : "empty array";
            for (int i = 0; i < Array.getLength(o); ++i) {
                ret.addAll(expand(Array.get(o, i)));
            }
        }
        else {
            if (!(o instanceof Iterable)) {
                displayError("unknown type", "Input: " + o + "\nType: " + o.getClass().getName(), null, false);
                return null;
            }
            assert ((Iterable)o).iterator().hasNext() : "emtpy iterable";
            for (final Object o2 : (Iterable)o) {
                ret.addAll(expand(o2));
            }
        }
        return ret;
    }
    
    public static List<ItemStack>[] expandArray(final Object[] array) {
        final List<ItemStack>[] ret = new List[array.length];
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == null) {
                ret[i] = null;
            }
            else {
                ret[i] = expand(array[i]);
            }
        }
        return ret;
    }
    
    public static void displayError(final String cause, final String tech, final ItemStack result, final boolean shapeless) {
        final String msg = "An invalid crafting recipe was attempted to be added. This could happen due to a bug in IndustrialCraft 2 or an addon.\n\n(Technical information: Adv" + (shapeless ? "Shapeless" : "") + "Recipe, " + cause + ")\n" + ((result != null) ? ("Output: " + result + "\n") : "") + ((tech != null) ? (tech + "\n") : "") + "Source: " + getCaller();
        if (MainConfig.ignoreInvalidRecipes) {
            IC2.log.warn(LogCategory.Recipe, msg);
            throw new RuntimeException(msg);
        }
        IC2.platform.displayError(msg, new Object[0]);
    }
    
    private static String getCaller() {
        String ret = "unknown";
        for (final StackTraceElement st : Thread.currentThread().getStackTrace()) {
            final String className = st.getClassName();
            final int pkgSeparator = className.lastIndexOf(46);
            final String pkg = (pkgSeparator == -1) ? "" : className.substring(0, pkgSeparator);
            if (!className.equals("ic2.core.recipe.AdvRecipe") && !className.equals("ic2.core.recipe.AdvShapelessRecipe") && !className.equals("ic2.core.recipe.AdvCraftingRecipeManager") && !pkg.startsWith("ic2.api") && !pkg.startsWith("java.")) {
                ret = className + "." + st.getMethodName() + "(" + st.getFileName() + ":" + st.getLineNumber() + ")";
                break;
            }
        }
        return ret;
    }
    
    private static boolean checkMask(final int mask, final int[] request) {
        for (final int cmpMask : request) {
            if (mask == cmpMask) {
                return true;
            }
        }
        return false;
    }
    
    static IRecipeInput getRecipeObject(final Object o) {
        if (o == null) {
            throw new NullPointerException("Null recipe input object.");
        }
        if (o instanceof IRecipeInput) {
            return (IRecipeInput)o;
        }
        if (o instanceof ItemStack) {
            return Recipes.inputFactory.forStack((ItemStack)o);
        }
        if (o instanceof Block) {
            return Recipes.inputFactory.forStack(new ItemStack((Block)o));
        }
        if (o instanceof Item) {
            return Recipes.inputFactory.forStack(new ItemStack((Item)o));
        }
        if (o instanceof String) {
            return Recipes.inputFactory.forOreDict((String)o);
        }
        if (o instanceof Fluid) {
            return Recipes.inputFactory.forFluidContainer((Fluid)o);
        }
        if (o instanceof FluidStack) {
            return Recipes.inputFactory.forFluidContainer(((FluidStack)o).getFluid(), ((FluidStack)o).amount);
        }
        if (o instanceof Iterable) {
            final List<IRecipeInput> list = new ArrayList<IRecipeInput>();
            for (final Object o2 : (Iterable)o) {
                list.add(getRecipeObject(o2));
            }
            return Recipes.inputFactory.forAny(list);
        }
        if (o.getClass().isArray()) {
            final IRecipeInput[] inputs = new IRecipeInput[Array.getLength(o)];
            for (int i = 0; i < inputs.length; ++i) {
                inputs[i] = getRecipeObject(Array.get(o, i));
            }
            return Recipes.inputFactory.forAny(inputs);
        }
        throw new IllegalArgumentException("Invalid object found as RecipeInput: " + o);
    }
    
    private ItemStack checkItems(final IInventory inventory, final IRecipeInput[] request) {
        final int size = inventory.getSizeInventory();
        double outputCharge = 0.0;
        int i = 0;
        int j = 0;
        while (i < size) {
            final ItemStack offer = inventory.getStackInSlot(i);
            if (!StackUtil.isEmpty(offer)) {
                if (!request[j++].matches(offer)) {
                    return StackUtil.emptyStack;
                }
                outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
            }
            ++i;
        }
        final ItemStack ret = this.output.copy();
        ElectricItem.manager.charge(ret, outputCharge, Integer.MAX_VALUE, true, false);
        return ret;
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
        return this.inputWidth <= x && this.inputHeight <= y;
    }
    
    public int getRecipeWidth() {
        return this.inputWidth;
    }
    
    public int getRecipeHeight() {
        return this.inputHeight;
    }
    
    public NonNullList<Ingredient> getIngredients() {
        final NonNullList<Ingredient> list = (NonNullList<Ingredient>)NonNullList.create();
        if (!this.hidden) {
            final int mask = this.masks[0];
            int actualIngredient = 0;
            for (int x = 0; x < 9; ++x) {
                if ((mask >>> 8 - x & 0x1) != 0x0) {
                    list.add((Object)this.input[actualIngredient++].getIngredient());
                }
                else {
                    list.add((Object)Ingredient.EMPTY);
                }
            }
        }
        return list;
    }
    
    public boolean isDynamic() {
        return this.hidden;
    }
    
    static {
        debug = Util.hasAssertions();
    }
}
