// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.util.EnumActionResult;
import ic2.core.util.Util;
import ic2.api.item.ElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.ref.ItemName;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IBoxable;

public class ItemBatterySU extends ItemIC2 implements IBoxable, IItemHudInfo
{
    public int capacity;
    public int tier;
    
    public ItemBatterySU(final ItemName internalName, final int capacity1, final int tier1) {
        super(internalName);
        this.capacity = capacity1;
        this.tier = tier1;
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        double energy = this.capacity;
        for (int i = 0; i < 9 && energy > 0.0; ++i) {
            final ItemStack target = (ItemStack)player.inventory.mainInventory.get(i);
            if (target != null && target != stack) {
                energy -= ElectricItem.manager.charge(target, energy, this.tier, true, false);
            }
        }
        if (!Util.isSimilar(energy, this.capacity)) {
            stack = StackUtil.decSize(stack);
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add(this.capacity + " EU");
        return info;
    }
}
