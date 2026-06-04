// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor.jetpack;

import java.util.Map;
import java.util.Collections;
import java.util.IdentityHashMap;
import net.minecraftforge.common.ForgeHooks;
import net.minecraft.util.NonNullList;
import ic2.api.item.ElectricItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLiving;
import ic2.core.util.StackUtil;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryCrafting;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.api.recipe.Recipes;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import java.util.Set;
import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.crafting.IRecipe;

public class JetpackAttachmentRecipe implements IRecipe
{
    private final IRecipeInput attachmentPlate;
    public static final Set<Item> blacklistedItems;
    private ResourceLocation name;
    
    public static void init() {
        for (final ItemStack stack : ConfigUtil.asStackList(MainConfig.get(), "recipes/jetpackAttachmentBlacklist")) {
            JetpackAttachmentRecipe.blacklistedItems.add(stack.getItem());
        }
    }
    
    public JetpackAttachmentRecipe() {
        this.attachmentPlate = Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.jetpack_attachment_plate));
    }
    
    public boolean matches(final InventoryCrafting inv, final World worldIn) {
        return this.getCraftingResult(inv) != StackUtil.emptyStack;
    }
    
    public ItemStack getCraftingResult(final InventoryCrafting inv) {
        ItemStack jetpack = null;
        ItemStack armor = null;
        boolean attachmentPlate = false;
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            final ItemStack currentStack = inv.getStackInSlot(i);
            if (!StackUtil.isEmpty(currentStack)) {
                final Item item = currentStack.getItem();
                if (item == ItemName.jetpack_electric.getInstance()) {
                    if (jetpack != null) {
                        return StackUtil.emptyStack;
                    }
                    jetpack = currentStack;
                }
                else if (EntityLiving.getSlotForItemStack(currentStack) == EntityEquipmentSlot.CHEST && !JetpackAttachmentRecipe.blacklistedItems.contains(item)) {
                    if (armor != null) {
                        return StackUtil.emptyStack;
                    }
                    armor = currentStack;
                }
                else {
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
        if (jetpack == null || armor == null || !attachmentPlate || JetpackHandler.hasJetpackAttached(armor)) {
            return StackUtil.emptyStack;
        }
        final ItemStack ret = armor.copy();
        JetpackHandler.setJetpackAttached(ret, true);
        ElectricItem.manager.charge(ret, ElectricItem.manager.getCharge(jetpack), Integer.MAX_VALUE, true, false);
        return ret;
    }
    
    public ItemStack getRecipeOutput() {
        return StackUtil.emptyStack;
    }
    
    public NonNullList<ItemStack> getRemainingItems(final InventoryCrafting inv) {
        return (NonNullList<ItemStack>)ForgeHooks.defaultRecipeGetRemainingItems(inv);
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
        return x * y >= 3;
    }
    
    static {
        (blacklistedItems = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>())).add(ItemName.jetpack.getInstance());
        JetpackAttachmentRecipe.blacklistedItems.add(ItemName.jetpack_electric.getInstance());
        JetpackAttachmentRecipe.blacklistedItems.add(ItemName.quantum_chestplate.getInstance());
    }
}
