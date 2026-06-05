package ic2.core.item.armor.jetpack;

import ic2.api.item.ElectricItem;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class JetpackAttachmentRecipe implements IRecipe {
   private final IRecipeInput attachmentPlate = Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.jetpack_attachment_plate));
   public static final Set<Item> blacklistedItems = Collections.newSetFromMap(new IdentityHashMap<>());
   private ResourceLocation name;

   public static void init() {
      for (ItemStack stack : ConfigUtil.asStackList(MainConfig.get(), "recipes/jetpackAttachmentBlacklist")) {
         blacklistedItems.add(stack.getItem());
      }
   }

   public boolean matches(InventoryCrafting inv, World worldIn) {
      return this.getCraftingResult(inv) != StackUtil.emptyStack;
   }

   public ItemStack getCraftingResult(InventoryCrafting inv) {
      ItemStack jetpack = null;
      ItemStack armor = null;
      boolean attachmentPlate = false;

      for (int i = 0; i < inv.getSizeInventory(); i++) {
         ItemStack currentStack = inv.getStackInSlot(i);
         if (!StackUtil.isEmpty(currentStack)) {
            Item item = currentStack.getItem();
            if (item == ItemName.jetpack_electric.getInstance()) {
               if (jetpack != null) {
                  return StackUtil.emptyStack;
               }

               jetpack = currentStack;
            } else if (EntityLiving.getSlotForItemStack(currentStack) == EntityEquipmentSlot.CHEST && !blacklistedItems.contains(item)) {
               if (armor != null) {
                  return StackUtil.emptyStack;
               }

               armor = currentStack;
            } else {
               if (!this.attachmentPlate.matches(currentStack)) {
                  return StackUtil.emptyStack;
               }

               if (attachmentPlate) {
                  return StackUtil.emptyStack;
               }

               attachmentPlate = true;
            }
         }
      }

      if (jetpack != null && armor != null && attachmentPlate && !JetpackHandler.hasJetpackAttached(armor)) {
         ItemStack ret = armor.copy();
         JetpackHandler.setJetpackAttached(ret, true);
         ElectricItem.manager.charge(ret, ElectricItem.manager.getCharge(jetpack), Integer.MAX_VALUE, true, false);
         return ret;
      } else {
         return StackUtil.emptyStack;
      }
   }

   public ItemStack getRecipeOutput() {
      return StackUtil.emptyStack;
   }

   public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
      return ForgeHooks.defaultRecipeGetRemainingItems(inv);
   }

   public IRecipe setRegistryName(ResourceLocation name) {
      this.name = name;
      return this;
   }

   public ResourceLocation getRegistryName() {
      return this.name;
   }

   public Class<IRecipe> getRegistryType() {
      return IRecipe.class;
   }

   public boolean canFit(int x, int y) {
      return x * y >= 3;
   }

   static {
      blacklistedItems.add(ItemName.jetpack.getInstance());
      blacklistedItems.add(ItemName.jetpack_electric.getInstance());
      blacklistedItems.add(ItemName.quantum_chestplate.getInstance());
   }
}
