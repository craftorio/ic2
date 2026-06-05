package ic2.core.util;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.ref.IMultiBlock;
import ic2.core.ref.IMultiItem;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;

public class ConfigUtil {
   public static List<String> asList(String str) {
      str = str.trim();
      return str.isEmpty() ? Collections.emptyList() : Arrays.asList(str.split("\\s*,\\s*"));
   }

   public static List<IRecipeInput> asRecipeInputList(Config config, String key) {
      Config.Value value = config.get(key);

      try {
         try {
            return asRecipeInputList(value.getString());
         } catch (ParseException e) {
            throw new Config.ParseException("Invalid value", value, e);
         }
      } catch (Config.ParseException e) {
         displayError(e, key);
         return null;
      }
   }

   public static List<ItemStack> asStackList(Config config, String key) {
      Config.Value value = config.get(key);

      try {
         try {
            return asStackList(value.getString());
         } catch (ParseException e) {
            throw new Config.ParseException("Invalid value", value, e);
         }
      } catch (Config.ParseException e) {
         displayError(e, key);
         return null;
      }
   }

   public static ItemStack asStack(Config config, String key) {
      Config.Value value = config.get(key);

      try {
         try {
            return asStack(value.getString());
         } catch (ParseException e) {
            throw new Config.ParseException("Invalid value", value, e);
         }
      } catch (Config.ParseException e) {
         displayError(e, key);
         return null;
      }
   }

   public static String getString(Config config, String key) {
      return config.get(key).getString();
   }

   public static boolean getBool(Config config, String key) {
      Config.Value value = config.get(key);

      try {
         return value.getBool();
      } catch (Config.ParseException e) {
         displayError(e, key);
         return false;
      }
   }

   public static int getInt(Config config, String key) {
      Config.Value value = config.get(key);

      try {
         return value.getInt();
      } catch (Config.ParseException e) {
         displayError(e, key);
         return 0;
      }
   }

   public static float getFloat(Config config, String key) {
      Config.Value value = config.get(key);

      try {
         return value.getFloat();
      } catch (Config.ParseException e) {
         displayError(e, key);
         return 0.0F;
      }
   }

   public static double getDouble(Config config, String key) {
      Config.Value value = config.get(key);

      try {
         return value.getDouble();
      } catch (Config.ParseException e) {
         displayError(e, key);
         return 0.0;
      }
   }

   public static int[] asIntArray(Config config, String key) {
      Config.Value value = config.get(key);

      try {
         return asList(value.getString()).stream().mapToInt(Integer::parseInt).toArray();
      } catch (NumberFormatException e) {
         displayError(new Config.ParseException("Invalid value", value, e), key);
         return new int[0];
      }
   }

   public static List<ItemStack> asStackList(String str) throws ParseException {
      List<String> parts = asList(str);
      List<ItemStack> ret = new ArrayList<>(parts.size());

      for (String part : parts) {
         ret.add(asStack(part));
      }

      return ret;
   }

   public static List<IRecipeInput> asRecipeInputList(String str) throws ParseException {
      return asRecipeInputList(str, false);
   }

   public static List<IRecipeInput> asRecipeInputList(String str, boolean allowNull) throws ParseException {
      List<String> parts = asList(str);
      List<IRecipeInput> ret = new ArrayList<>(parts.size());

      for (String part : parts) {
         IRecipeInput input = asRecipeInput(part);
         if (input == null && !allowNull) {
            throw new ParseException("There is no item matching " + part + ".", -1);
         }

         ret.add(input);
      }

      return ret;
   }

   private static ItemStack asStack(String str, boolean checkAmount) throws ParseException {
      String[] parts = str.split("(?=(@|#|\\*))");
      String itemName = parts[0];
      Item item = Util.getItem(itemName);
      if (item == null) {
         return null;
      }

      ItemStack stack = new ItemStack(item);
      int amount = 1;

      for (int i = 1; i < parts.length; i++) {
         String tmp = parts[i];
         if (tmp.startsWith("@")) {
            if (i + 1 < parts.length && parts[i + 1].equals("*")) {
               stack = new ItemStack(item, 1, 32767);
               i++;
            } else {
               stack = new ItemStack(item, 1, Integer.parseInt(tmp.substring(1)));
            }
         } else if (tmp.startsWith("#")) {
            if (item instanceof IMultiItem) {
               stack = ((IMultiItem)item).getItemStack(tmp.substring(1));
            } else {
               if (!(item instanceof ItemBlock) || !(((ItemBlock)item).getBlock() instanceof IMultiBlock)) {
                  throw new ParseException("# is not supported on non-IC2-Items: " + str, 0);
               }

               stack = ((IMultiBlock)((ItemBlock)item).getBlock()).getItemStack(tmp.substring(1));
            }
         } else if (tmp.startsWith("*")) {
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

   public static ItemStack asStack(String str) throws ParseException {
      return asStack(str, false);
   }

   public static ItemStack asStackWithAmount(String str) throws ParseException {
      return asStack(str, true);
   }

   public static String fromStack(ItemStack stack) {
      return fromStack(stack, false);
   }

   private static String fromStack(ItemStack stack, boolean amount) {
      String ret = Util.getName(stack.getItem()).toString();
      if (amount) {
         ret = ret + "*" + StackUtil.getSize(stack);
      }

      if (stack.getItem() instanceof IMultiItem) {
         String variant = ((IMultiItem)stack.getItem()).getVariant(stack);
         if (variant != null) {
            ret = ret + "#" + variant;
         }
      } else if (stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() instanceof IMultiBlock) {
         String variant = ((IMultiBlock)((ItemBlock)stack.getItem()).getBlock()).getVariant(stack);
         if (variant != null) {
            ret = ret + "#" + variant;
         }
      } else if (stack.getItemDamage() == 32767) {
         ret = ret + "@*";
      } else if (stack.getItemDamage() != 0) {
         ret = ret + "@" + stack.getItemDamage();
      }

      return ret;
   }

   public static String fromStackWithAmount(ItemStack stack) {
      return fromStack(stack, true);
   }

   public static IRecipeInput asRecipeInput(Config.Value value) {
      try {
         return asRecipeInput(value.getString());
      } catch (ParseException e) {
         throw new Config.ParseException("Invalid value", value, e);
      }
   }

   private static IRecipeInput asRecipeInput(String str, boolean checkAmount) throws ParseException {
      String[] parts = str.split("(?=(@|#|\\*))");
      String itemName = parts[0];
      if (!itemName.startsWith("OreDict:") && !itemName.startsWith("Fluid:")) {
         ItemStack stack = asStack(str, checkAmount);
         return stack == null ? null : Recipes.inputFactory.forStack(stack);
      }

      Integer amount = null;
      Integer meta = null;

      for (int i = 1; i < parts.length; i++) {
         String tmp = parts[i];
         if (tmp.startsWith("@")) {
            if (i + 1 < parts.length && parts[i + 1].equals("*")) {
               meta = 32767;
               i++;
            } else {
               meta = Integer.parseInt(tmp.substring(1));
            }
         } else if (tmp.startsWith("*")) {
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

         return meta == null
            ? Recipes.inputFactory.forOreDict(itemName.substring("OreDict:".length()), amount)
            : Recipes.inputFactory.forOreDict(itemName.substring("OreDict:".length()), amount, meta);
      } else if (itemName.startsWith("Fluid:")) {
         if (amount == null) {
            amount = 1000;
         }

         return Recipes.inputFactory.forFluidContainer(FluidRegistry.getFluid(itemName.substring("Fluid:".length())), amount);
      } else {
         return null;
      }
   }

   public static IRecipeInput asRecipeInput(String str) throws ParseException {
      return asRecipeInput(str, false);
   }

   public static IRecipeInput asRecipeInputWithAmount(String str) throws ParseException {
      return asRecipeInput(str, true);
   }

   private static void displayError(Config.ParseException e, String key) {
      IC2.platform
         .displayError(
            "The IC2 config file contains an invalid entry for %s.\n\n%s%s",
            key,
            e.getMessage(),
            e.getCause() != null ? "\n\n" + e.getCause().getMessage() : ""
         );
   }
}
