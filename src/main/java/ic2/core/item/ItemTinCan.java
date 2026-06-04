// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.IC2;
import ic2.core.item.type.CraftingItemType;
import net.minecraft.util.EnumActionResult;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.ref.ItemName;

public class ItemTinCan extends ItemIC2
{
    public ItemTinCan() {
        super(ItemName.filled_tin_can);
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!world.isRemote && player.getFoodStats().needFood()) {
            return this.onEaten(player, stack);
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
    }
    
    public ActionResult<ItemStack> onEaten(final EntityPlayer player, ItemStack stack) {
        final int amount = Math.min(StackUtil.getSize(stack), 20 - player.getFoodStats().getFoodLevel());
        if (amount <= 0) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        final ItemStack emptyStack = StackUtil.copyWithSize(ItemName.crafting.getItemStack(CraftingItemType.tin_can), amount);
        if (StackUtil.storeInventoryItem(emptyStack, player, true)) {
            player.getFoodStats().addStats(amount, (float)amount);
            stack = StackUtil.decSize(stack, amount);
            StackUtil.storeInventoryItem(emptyStack, player, false);
            IC2.platform.playSoundSp("Tools/eat.ogg", 1.0f, 1.0f);
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
    }
}
