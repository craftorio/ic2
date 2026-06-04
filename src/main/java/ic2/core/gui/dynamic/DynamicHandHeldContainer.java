// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui.dynamic;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.ClickType;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.item.tool.HandHeldInventory;

public class DynamicHandHeldContainer<T extends HandHeldInventory> extends DynamicContainer<T>
{
    public static <T extends HandHeldInventory> DynamicHandHeldContainer<T> create(final T base, final EntityPlayer player, final GuiParser.GuiNode guiNode) {
        return new DynamicHandHeldContainer<T>(base, player, guiNode);
    }
    
    protected DynamicHandHeldContainer(final T base, final EntityPlayer player, final GuiParser.GuiNode guiNode) {
        super((IInventory)base, player, guiNode);
    }
    
    @Override
    protected SlotHologramSlot.ChangeCallback getCallback() {
        return this.base.makeSaveCallback();
    }
    
    @Override
    public void onContainerEvent(final String event) {
        this.base.onEvent(event);
        super.onContainerEvent(event);
    }
    
    @Override
    public ItemStack slotClick(final int slot, final int button, final ClickType type, final EntityPlayer player) {
        boolean thrown = false;
        Slot realSlot = null;
        if (!player.getEntityWorld().isRemote && slot >= 0 && slot < this.inventorySlots.size()) {
            realSlot = this.inventorySlots.get(slot);
            thrown = this.base.isThisContainer(realSlot.getStack());
        }
        final ItemStack stack = super.slotClick(slot, button, type, player);
        if (thrown && !realSlot.getHasStack()) {
            this.base.saveAsThrown(stack);
            player.closeScreen();
        }
        return stack;
    }
    
    public void onContainerClosed(final EntityPlayer player) {
        this.base.onGuiClosed(player);
        super.onContainerClosed(player);
    }
}
