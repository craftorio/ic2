// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import ic2.core.IC2;
import net.minecraftforge.fluids.FluidRegistry;
import ic2.api.recipe.Recipes;
import net.minecraft.item.Item;
import ic2.core.ref.IMultiBlock;
import net.minecraft.item.ItemBlock;
import ic2.core.ref.IMultiItem;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.function.ToIntFunction;
import net.minecraft.item.ItemStack;
import java.text.ParseException;
import ic2.api.recipe.IRecipeInput;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigUtil
{
    public static List<String> asList(String str) {
        str = str.trim();
        if (str.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(str.split("\\s*,\\s*"));
    }
    
    public static List<IRecipeInput> asRecipeInputList(final Config config, final String key) {
        final Config.Value value = config.get(key);
        try {
            try {
                return asRecipeInputList(value.getString());
            }
            catch (final ParseException e) {
                throw new Config.ParseException("Invalid value", value, e);
            }
        }
        catch (final Config.ParseException e2) {
            displayError(e2, key);
            return null;
        }
    }
    
    public static List<ItemStack> asStackList(final Config config, final String key) {
        final Config.Value value = config.get(key);
        try {
            try {
                return asStackList(value.getString());
            }
            catch (final ParseException e) {
                throw new Config.ParseException("Invalid value", value, e);
            }
        }
        catch (final Config.ParseException e2) {
            displayError(e2, key);
            return null;
        }
    }
    
    public static ItemStack asStack(final Config config, final String key) {
        final Config.Value value = config.get(key);
        try {
            try {
                return asStack(value.getString());
            }
            catch (final ParseException e) {
                throw new Config.ParseException("Invalid value", value, e);
            }
        }
        catch (final Config.ParseException e2) {
            displayError(e2, key);
            return null;
        }
    }
    
    public static String getString(final Config config, final String key) {
        return config.get(key).getString();
    }
    
    public static boolean getBool(final Config config, final String key) {
        final Config.Value value = config.get(key);
        try {
            return value.getBool();
        }
        catch (final Config.ParseException e) {
            displayError(e, key);
            return false;
        }
    }
    
    public static int getInt(final Config config, final String key) {
        final Config.Value value = config.get(key);
        try {
            return value.getInt();
        }
        catch (final Config.ParseException e) {
            displayError(e, key);
            return 0;
        }
    }
    
    public static float getFloat(final Config config, final String key) {
        final Config.Value value = config.get(key);
        try {
            return value.getFloat();
        }
        catch (final Config.ParseException e) {
            displayError(e, key);
            return 0.0f;
        }
    }
    
    public static double getDouble(final Config config, final String key) {
        final Config.Value value = config.get(key);
        try {
            return value.getDouble();
        }
        catch (final Config.ParseException e) {
            displayError(e, key);
            return 0.0;
        }
    }
    
    public static int[] asIntArray(final Config config, final String key) {
        final Config.Value value = config.get(key);
        try {
            return asList(value.getString()).stream().mapToInt((ToIntFunction<? super Object>)Integer::parseInt).toArray();
        }
        catch (final NumberFormatException e) {
            displayError(new Config.ParseException("Invalid value", value, e), key);
            return new int[0];
        }
    }
    
    public static List<ItemStack> asStackList(final String str) throws ParseException {
        final List<String> parts = asList(str);
        final List<ItemStack> ret = new ArrayList<ItemStack>(parts.size());
        for (final String part : parts) {
            ret.add(asStack(part));
        }
        return ret;
    }
    
    public static List<IRecipeInput> asRecipeInputList(final String str) throws ParseException {
        return asRecipeInputList(str, false);
    }
    
    public static List<IRecipeInput> asRecipeInputList(final String str, final boolean allowNull) throws ParseException {
        final List<String> parts = asList(str);
        final List<IRecipeInput> ret = new ArrayList<IRecipeInput>(parts.size());
        for (final String part : parts) {
            final IRecipeInput input = asRecipeInput(part);
            if (input == null && !allowNull) {
                throw new ParseException("There is no item matching " + part + ".", -1);
            }
            ret.add(input);
        }
        return ret;
    }
    
    private static ItemStack asStack(final String str, final boolean checkAmount) throws ParseException {
        final String[] parts = str.split("(?=(@|#|\\*))");
        final String itemName = parts[0];
        final Item item = Util.getItem(itemName);
        if (item == null) {
            return null;
        }
        ItemStack stack = new ItemStack(item);
        int amount = 1;
        for (int i = 1; i < parts.length; ++i) {
            final String tmp = parts[i];
            if (tmp.startsWith("@")) {
                if (i + 1 < parts.length && parts[i + 1].equals("*")) {
                    stack = new ItemStack(item, 1, 32767);
                    ++i;
                }
                else {
                    stack = new ItemStack(item, 1, Integer.parseInt(tmp.substring(1)));
                }
            }
            else if (tmp.startsWith("#")) {
                if (item instanceof IMultiItem) {
                    stack = ((IMultiItem)item).getItemStack(tmp.substring(1));
                }
                else {
                    if (!(item instanceof ItemBlock) || !(((ItemBlock)item).getBlock() instanceof IMultiBlock)) {
                        throw new ParseException("# is not supported on non-IC2-Items: " + str, 0);
                    }
                    stack = ((IMultiBlock)((ItemBlock)item).getBlock()).getItemStack(tmp.substring(1));
                }
            }
            else if (tmp.startsWith("*")) {
                if (!checkAmount) {
                    throw new ParseException("We do not support amount here.", -1);
                }
                amount = Integer.parseInt(tmp.substring(1));
            }
        }
        if (checkAmount) {
            stack = StackUtil.setSize(stack, amount);
        }
        return stack;
    }
    
    public static ItemStack asStack(final String str) throws ParseException {
        return asStack(str, false);
    }
    
    public static ItemStack asStackWithAmount(final String str) throws ParseException {
        return asStack(str, true);
    }
    
    public static String fromStack(final ItemStack stack) {
        return fromStack(stack, false);
    }
    
    private static String fromStack(final ItemStack stack, final boolean amount) {
        String ret = Util.getName(stack.getItem()).toString();
        if (amount) {
            ret = ret + "*" + StackUtil.getSize(stack);
        }
        if (stack.getItem() instanceof IMultiItem) {
            final String variant = ((IMultiItem)stack.getItem()).getVariant(stack);
            if (variant != null) {
                ret = ret + "#" + variant;
            }
        }
        else if (stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() instanceof IMultiBlock) {
            final String variant = ((IMultiBlock)((ItemBlock)stack.getItem()).getBlock()).getVariant(stack);
            if (variant != null) {
                ret = ret + "#" + variant;
            }
        }
        else if (stack.getItemDamage() == 32767) {
            ret += "@*";
        }
        else if (stack.getItemDamage() != 0) {
            ret = ret + "@" + stack.getItemDamage();
        }
        return ret;
    }
    
    public static String fromStackWithAmount(final ItemStack stack) {
        return fromStack(stack, true);
    }
    
    public static IRecipeInput asRecipeInput(final Config.Value value) {
        try {
            return asRecipeInput(value.getString());
        }
        catch (final ParseException e) {
            throw new Config.ParseException("Invalid value", value, e);
        }
    }
    
    private static IRecipeInput asRecipeInput(final String str, final boolean checkAmount) throws ParseException {
        final String[] parts = str.split("(?=(@|#|\\*))");
        final String itemName = parts[0];
        if (!itemName.startsWith("OreDict:") && !itemName.startsWith("Fluid:")) {
            final ItemStack stack = asStack(str, checkAmount);
            if (stack == null) {
                return null;
            }
            return Recipes.inputFactory.forStack(stack);
        }
        else {
            Integer amount = null;
            Integer meta = null;
            for (int i = 1; i < parts.length; ++i) {
                final String tmp = parts[i];
                if (tmp.startsWith("@")) {
                    if (i + 1 < parts.length && parts[i + 1].equals("*")) {
                        meta = 32767;
                        ++i;
                    }
                    else {
                        meta = Integer.parseInt(tmp.substring(1));
                    }
                }
                else if (tmp.startsWith("*")) {
                    if (!checkAmount) {
                        throw new ParseException("We do not support amount here.", -1);
                    }
                    amount = Integer.parseInt(tmp.substring(1));
                }
            }
            if (itemName.startsWith("OreDict:")) {
                if (amount == null) {
                    amount = 1;
                }
                if (meta == null) {
                    return Recipes.inputFactory.forOreDict(itemName.substring("OreDict:".length()), amount);
                }
                return Recipes.inputFactory.forOreDict(itemName.substring("OreDict:".length()), amount, meta);
            }
            else {
                if (itemName.startsWith("Fluid:")) {
                    if (amount == null) {
                        amount = 1000;
                    }
                    return Recipes.inputFactory.forFluidContainer(FluidRegistry.getFluid(itemName.substring("Fluid:".length())), amount);
                }
                return null;
            }
        }
    }
    
    public static IRecipeInput asRecipeInput(final String str) throws ParseException {
        return asRecipeInput(str, false);
    }
    
    public static IRecipeInput asRecipeInputWithAmount(final String str) throws ParseException {
        return asRecipeInput(str, true);
    }
    
    private static void displayError(final Config.ParseException e, final String key) {
        IC2.platform.displayError("The IC2 config file contains an invalid entry for %s.\n\n%s%s", key, e.getMessage(), (e.getCause() != null) ? ("\n\n" + e.getCause().getMessage()) : "");
    }
}
