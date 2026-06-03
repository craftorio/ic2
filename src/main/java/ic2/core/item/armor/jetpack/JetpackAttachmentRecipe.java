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
  private final IRecipeInput attachmentPlate = Recipes.inputFactory.forStack(ItemName.crafting.getItemStack((Enum)CraftingItemType.jetpack_attachment_plate));
  
  public static final Set<Item> blacklistedItems = Collections.newSetFromMap(new IdentityHashMap<>());
  
  private ResourceLocation name;
  
  static {
    blacklistedItems.add(ItemName.jetpack.getInstance());
    blacklistedItems.add(ItemName.jetpack_electric.getInstance());
    blacklistedItems.add(ItemName.quantum_chestplate.getInstance());
  }
  
  public static void init() {
    for (ItemStack stack : ConfigUtil.asStackList(MainConfig.get(), "recipes/jetpackAttachmentBlacklist"))
      blacklistedItems.add(stack.func_77973_b()); 
  }
  
  public boolean func_77569_a(InventoryCrafting inv, World worldIn) {
    return (func_77572_b(inv) != StackUtil.emptyStack);
  }
  
  public ItemStack func_77572_b(InventoryCrafting inv) {
    ItemStack jetpack = null;
    ItemStack armor = null;
    boolean attachmentPlate = false;
    for (int i = 0; i < inv.func_70302_i_(); i++) {
      ItemStack currentStack = inv.func_70301_a(i);
      if (!StackUtil.isEmpty(currentStack)) {
        Item item = currentStack.func_77973_b();
        if (item == ItemName.jetpack_electric.getInstance()) {
          if (jetpack != null)
            return StackUtil.emptyStack; 
          jetpack = currentStack;
        } else if (EntityLiving.func_184640_d(currentStack) == EntityEquipmentSlot.CHEST && 
          !blacklistedItems.contains(item)) {
          if (armor != null)
            return StackUtil.emptyStack; 
          armor = currentStack;
        } else if (this.attachmentPlate.matches(currentStack)) {
          if (attachmentPlate)
            return StackUtil.emptyStack; 
          attachmentPlate = true;
        } else {
          return StackUtil.emptyStack;
        } 
      } 
    } 
    if (jetpack == null || armor == null || !attachmentPlate || JetpackHandler.hasJetpackAttached(armor))
      return StackUtil.emptyStack; 
    ItemStack ret = armor.func_77946_l();
    JetpackHandler.setJetpackAttached(ret, true);
    ElectricItem.manager.charge(ret, ElectricItem.manager.getCharge(jetpack), 2147483647, true, false);
    return ret;
  }
  
  public ItemStack func_77571_b() {
    return StackUtil.emptyStack;
  }
  
  public NonNullList<ItemStack> func_179532_b(InventoryCrafting inv) {
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
  
  public boolean func_194133_a(int x, int y) {
    return (x * y >= 3);
  }
}
