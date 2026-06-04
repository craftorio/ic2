// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.entity.player.EntityPlayerMP;
import ic2.core.util.StackUtil;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import ic2.core.ContainerBase;
import ic2.core.item.tool.HandHeldInventory;

public class ContainerHandHeldInventory<T extends HandHeldInventory> extends ContainerBase<T>
{
    public ContainerHandHeldInventory(final T inventory) {
        super((IInventory)inventory);
    }
    
    @Override
    public ItemStack slotClick(final int slot, final int button, final ClickType type, final EntityPlayer player) {
        boolean closeGUI = false;
        switch (type) {
            case CLONE: {
                break;
            }
            case PICKUP: {
                if (slot >= 0 && slot < this.inventorySlots.size()) {
                    closeGUI = this.base.isThisContainer(this.inventorySlots.get(slot).getStack());
                    break;
                }
                break;
            }
            case PICKUP_ALL: {
                break;
            }
            case QUICK_CRAFT: {
                break;
            }
            case QUICK_MOVE: {
                if (slot >= 0 && slot < this.inventorySlots.size() && this.base.isThisContainer(this.inventorySlots.get(slot).getStack())) {
                    return StackUtil.emptyStack;
                }
                break;
            }
            case SWAP: {
                assert slot >= 0 && slot < this.inventorySlots.size();
                assert this.getSlotFromInventory((IInventory)player.inventory, button) != null;
                final boolean swapOut = this.base.isThisContainer(this.getSlotFromInventory((IInventory)player.inventory, button).getStack());
                final boolean swapTo = this.base.isThisContainer(this.inventorySlots.get(slot).getStack());
                if (swapOut || swapTo) {
                    int i = 0;
                    while (i < 9) {
                        if ((swapOut && slot == this.getSlotFromInventory((IInventory)player.inventory, i).slotNumber) || (swapTo && button == i)) {
                            if (player instanceof EntityPlayerMP) {
                                ((EntityPlayerMP)player).connection.sendPacket((Packet)new SPacketHeldItemChange(i));
                                break;
                            }
                            break;
                        }
                        else {
                            ++i;
                        }
                    }
                    break;
                }
                break;
            }
            case THROW: {
                if (slot >= 0 && slot < this.inventorySlots.size()) {
                    closeGUI = this.base.isThisContainer(this.inventorySlots.get(slot).getStack());
                    break;
                }
                break;
            }
            default: {
                throw new RuntimeException("Unexpected ClickType: " + type);
            }
        }
        final ItemStack stack = super.slotClick(slot, button, type, player);
        if (closeGUI && !player.getEntityWorld().isRemote) {
            this.base.saveAsThrown(stack);
            player.closeScreen();
        }
        else if (type == ClickType.CLONE) {
            final ItemStack held = player.inventory.getItemStack();
            if (this.base.isThisContainer(held)) {
                held.getTagCompound().removeTag("uid");
            }
        }
        return stack;
    }
    
    public void onContainerClosed(final EntityPlayer player) {
        this.base.onGuiClosed(player);
        super.onContainerClosed(player);
    }
}
