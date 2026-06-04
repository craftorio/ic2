// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.slot;

import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ic2.core.block.invslot.InvSlot;

public class SlotInvSlotReadOnly extends SlotInvSlot
{
    public SlotInvSlotReadOnly(final InvSlot invSlot, final int index, final int xDisplayPosition, final int yDisplayPosition) {
        super(invSlot, index, xDisplayPosition, yDisplayPosition);
    }
    
    @Override
    public boolean isItemValid(final ItemStack stack) {
        return false;
    }
    
    @Override
    public ItemStack onTake(final EntityPlayer player, final ItemStack stack) {
        return stack;
    }
    
    public boolean canTakeStack(final EntityPlayer player) {
        return false;
    }
    
    @Override
    public ItemStack decrStackSize(final int par1) {
        return StackUtil.emptyStack;
    }
}
