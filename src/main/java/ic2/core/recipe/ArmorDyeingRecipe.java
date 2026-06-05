package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.util.Ic2Color;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipesArmorDyes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ArmorDyeingRecipe extends RecipesArmorDyes {
   private static final Map<IRecipeInput, int[]> stackToRGB = buildDyeMap();
   protected final IRecipeInput armour;

   private static Map<IRecipeInput, int[]> buildDyeMap() {
      Map<IRecipeInput, int[]> ret = new HashMap<>();

      for (Ic2Color colour : Ic2Color.values) {
         float[] dyeMap = colour.mcColor.getColorComponentValues();
         assert dyeMap != null;
         ret.put(
            Recipes.inputFactory.forOreDict(colour.oreDictDyeName), new int[]{(int)(dyeMap[0] * 255.0F), (int)(dyeMap[1] * 255.0F), (int)(dyeMap[2] * 255.0F)}
         );
      }

      return ret;
   }

   public ArmorDyeingRecipe(ItemStack armour) {
      this(Recipes.inputFactory.forStack(armour));
      if (StackUtil.isEmpty(armour) || !(armour.getItem() instanceof ItemArmor)) {
         throw new IllegalArgumentException("Invalid input stack: " + StackUtil.toStringSafe(armour));
      }
   }

   public ArmorDyeingRecipe(Class<? extends ItemArmor> type) {
      this(new ArmorDyeingRecipe.RecipeInputClass(type));
      if (type == null || !ItemArmor.class.isAssignableFrom(type)) {
         throw new IllegalArgumentException("Invalid input class: " + type);
      }
   }

   public ArmorDyeingRecipe(IRecipeInput input) {
      this.armour = input;
   }

   public static boolean isDye(ItemStack stack) {
      for (IRecipeInput input : stackToRGB.keySet()) {
         if (input.matches(stack)) {
            return true;
         }
      }

      return false;
   }

   public static int[] getColourForStack(ItemStack stack) {
      for (Entry<IRecipeInput, int[]> entry : stackToRGB.entrySet()) {
         if (entry.getKey().matches(stack)) {
            return entry.getValue();
         }
      }

      return null;
   }

   public boolean matches(InventoryCrafting craftingInv, World world) {
      ItemStack Qsuit = null;

      for (int slot = 0; slot < craftingInv.getSizeInventory(); slot++) {
         ItemStack stack = craftingInv.getStackInSlot(slot);
         if (!StackUtil.isEmpty(stack)) {
            if (this.armour.matches(stack)) {
               if (Qsuit != null) {
                  return false;
               }

               Qsuit = stack;
            } else if (getColourForStack(stack) == null) {
               return false;
            }
         }
      }

      return Qsuit != null;
   }

   public ItemStack getCraftingResult(InventoryCrafting craftingInv) {
      ItemStack armourStack = null;
      ItemArmor Qsuit = null;
      int[] newRBG = new int[3];
      int totalColour = 0;
      int numberOfDyes = 0;

      for (int slot = 0; slot < craftingInv.getSizeInventory(); slot++) {
         ItemStack stack = craftingInv.getStackInSlot(slot);
         if (!StackUtil.isEmpty(stack)) {
            if (this.armour.matches(stack)) {
               Qsuit = (ItemArmor)stack.getItem();
               if (!StackUtil.isEmpty(armourStack)) {
                  return StackUtil.emptyStack;
               }

               armourStack = StackUtil.copyWithSize(stack, 1);
               if (Qsuit.hasColor(stack)) {
                  int oldColour = Qsuit.getColor(armourStack);
                  int r = oldColour >> 16 & 0xFF;
                  int g = oldColour >> 8 & 0xFF;
                  int b = oldColour & 0xFF;
                  totalColour += Math.max(r, Math.max(g, b));
                  newRBG[0] += r;
                  newRBG[1] += g;
                  newRBG[2] += b;
                  numberOfDyes++;
               }
            } else {
               int[] dyeRGB = getColourForStack(stack);
               if (dyeRGB == null) {
                  return StackUtil.emptyStack;
               }

               int r = dyeRGB[0];
               int g = dyeRGB[1];
               int b = dyeRGB[2];
               totalColour += Math.max(r, Math.max(g, b));
               newRBG[0] += r;
               newRBG[1] += g;
               newRBG[2] += b;
               numberOfDyes++;
            }
         }
      }

      if (Qsuit != null && numberOfDyes != 0) {
         if (Qsuit.hasColor(armourStack) && numberOfDyes == 1) {
            Qsuit.removeColor(armourStack);
         } else {
            int averageRed = newRBG[0] / numberOfDyes;
            int averageGreen = newRBG[1] / numberOfDyes;
            int averageBlue = newRBG[2] / numberOfDyes;
            float gain = (float)totalColour / numberOfDyes;
            float averageMax = Math.max(averageRed, Math.max(averageGreen, averageBlue));
            averageRed = (int)(averageRed * gain / averageMax);
            averageGreen = (int)(averageGreen * gain / averageMax);
            averageBlue = (int)(averageBlue * gain / averageMax);
            int finalColour = (averageRed << 8) + averageGreen;
            finalColour = (finalColour << 8) + averageBlue;
            Qsuit.setColor(armourStack, finalColour);
         }

         return armourStack;
      } else {
         return StackUtil.emptyStack;
      }
   }

   public static class RecipeInputClass extends RecipeInputBase implements IRecipeInput {
      protected final Class<?> type;
      protected final int amount;

      public RecipeInputClass(Class<?> type) {
         this(type, 1);
      }

      public RecipeInputClass(Class<?> type, int amount) {
         this.type = type;
         this.amount = amount;
      }

      @Override
      public boolean matches(ItemStack subject) {
         return this.matches(subject.getItem());
      }

      protected boolean matches(Item item) {
         return this.type.isInstance(item);
      }

      @Override
      public int getAmount() {
         return this.amount;
      }

      @Override
      public List<ItemStack> getInputs() {
         List<ItemStack> ret = new ArrayList<>();

         for (Item item : ForgeRegistries.ITEMS) {
            if (this.matches(item)) {
               ret.add(new ItemStack(item, 1, 32767));
            }
         }

         return ret;
      }

      @Override
      public String toString() {
         return "RInputClass<" + this.type + ", " + this.amount + '>';
      }

      @Override
      public boolean equals(Object obj) {
         ArmorDyeingRecipe.RecipeInputClass other;
         return obj != null
            && this.getClass() == obj.getClass()
            && (other = (ArmorDyeingRecipe.RecipeInputClass)obj).type == this.type
            && other.amount == this.amount;
      }
   }
}
