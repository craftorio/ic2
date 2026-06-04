// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import java.util.ArrayList;
import java.util.List;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.TileEntityBlock;
import ic2.core.network.NetworkManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.tileentity.TileEntity;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import java.util.ListIterator;
import java.util.Iterator;
import ic2.core.util.StackUtil;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public abstract class ContainerBase<T extends IInventory> extends Container
{
    protected static final int windowBorder = 8;
    protected static final int slotSize = 16;
    protected static final int slotDistance = 2;
    protected static final int slotSeparator = 4;
    protected static final int hotbarYOffset = -24;
    protected static final int inventoryYOffset = -82;
    public final T base;
    
    public ContainerBase(final T base1) {
        this.base = base1;
    }
    
    protected void addPlayerInventorySlots(final EntityPlayer player, final int height) {
        this.addPlayerInventorySlots(player, 178, height);
    }
    
    protected void addPlayerInventorySlots(final EntityPlayer player, final int width, final int height) {
        final int xStart = (width - 162) / 2;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot((IInventory)player.inventory, col + row * 9 + 9, xStart + col * 18, height - 82 + row * 18));
            }
        }
        for (int col2 = 0; col2 < 9; ++col2) {
            this.addSlotToContainer(new Slot((IInventory)player.inventory, col2, xStart + col2 * 18, height - 24));
        }
    }
    
    public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickType, final EntityPlayer player) {
        final Slot slot;
        if (slotId >= 0 && slotId < this.inventorySlots.size() && (slot = this.inventorySlots.get(slotId)) instanceof SlotHologramSlot) {
            return ((SlotHologramSlot)slot).slotClick(dragType, clickType, player);
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }
    
    public final ItemStack transferStackInSlot(final EntityPlayer player, final int sourceSlotIndex) {
        final Slot sourceSlot = this.inventorySlots.get(sourceSlotIndex);
        if (sourceSlot != null && sourceSlot.getHasStack()) {
            final ItemStack sourceItemStack = sourceSlot.getStack();
            final int oldSourceItemStackSize = StackUtil.getSize(sourceItemStack);
            ItemStack resultStack;
            if (sourceSlot.inventory == player.inventory) {
                resultStack = this.handlePlayerSlotShiftClick(player, sourceItemStack);
            }
            else {
                resultStack = this.handleGUISlotShiftClick(player, sourceItemStack);
            }
            if (StackUtil.isEmpty(resultStack) || StackUtil.getSize(resultStack) != oldSourceItemStackSize) {
                sourceSlot.putStack(resultStack);
                sourceSlot.onTake(player, sourceItemStack);
                if (!player.getEntityWorld().isRemote) {
                    this.detectAndSendChanges();
                }
            }
        }
        return StackUtil.emptyStack;
    }
    
    protected ItemStack handlePlayerSlotShiftClick(final EntityPlayer player, ItemStack sourceItemStack) {
        for (int run = 0; run < 4 && !StackUtil.isEmpty(sourceItemStack); ++run) {
            for (final Slot targetSlot : this.inventorySlots) {
                if (targetSlot.inventory != player.inventory && isValidTargetSlot(targetSlot, sourceItemStack, run % 2 == 1, run < 2)) {
                    sourceItemStack = this.transfer(sourceItemStack, targetSlot);
                    if (StackUtil.isEmpty(sourceItemStack)) {
                        break;
                    }
                    continue;
                }
            }
        }
        return sourceItemStack;
    }
    
    protected ItemStack handleGUISlotShiftClick(final EntityPlayer player, ItemStack sourceItemStack) {
        for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); ++run) {
            final ListIterator<Slot> it = this.inventorySlots.listIterator(this.inventorySlots.size());
            while (it.hasPrevious()) {
                final Slot targetSlot = it.previous();
                if (targetSlot.inventory == player.inventory && isValidTargetSlot(targetSlot, sourceItemStack, run == 1, false)) {
                    sourceItemStack = this.transfer(sourceItemStack, targetSlot);
                    if (StackUtil.isEmpty(sourceItemStack)) {
                        break;
                    }
                    continue;
                }
            }
        }
        return sourceItemStack;
    }
    
    protected static final boolean isValidTargetSlot(final Slot slot, final ItemStack stack, final boolean allowEmpty, final boolean requireInputOnly) {
        return !(slot instanceof SlotInvSlotReadOnly) && !(slot instanceof SlotHologramSlot) && slot.isItemValid(stack) && (allowEmpty || slot.getHasStack()) && (!requireInputOnly || (slot instanceof SlotInvSlot && ((SlotInvSlot)slot).invSlot.canInput()));
    }
    
    public boolean canInteractWith(final EntityPlayer entityplayer) {
        return this.base.isUsableByPlayer(entityplayer);
    }
    
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.base instanceof TileEntity) {
            for (final String name : this.getNetworkedFields()) {
                for (final IContainerListener crafter : this.listeners) {
                    if (crafter instanceof EntityPlayerMP) {
                        IC2.network.get(true).updateTileEntityFieldTo((TileEntity)this.base, name, (EntityPlayerMP)crafter);
                    }
                }
            }
            if (this.base instanceof TileEntityBlock) {
                for (final TileEntityComponent component : ((TileEntityBlock)this.base).getComponents()) {
                    for (final IContainerListener crafter : this.listeners) {
                        if (crafter instanceof EntityPlayerMP) {
                            component.onContainerUpdate((EntityPlayerMP)crafter);
                        }
                    }
                }
            }
        }
    }
    
    public List<String> getNetworkedFields() {
        return new ArrayList<String>();
    }
    
    public List<IContainerListener> getListeners() {
        return this.listeners;
    }
    
    public void onContainerEvent(final String event) {
    }
    
    protected final ItemStack transfer(ItemStack stack, final Slot dst) {
        final int amount = this.getTransferAmount(stack, dst);
        if (amount <= 0) {
            return stack;
        }
        final ItemStack dstStack = dst.getStack();
        if (StackUtil.isEmpty(dstStack)) {
            dst.putStack(StackUtil.copyWithSize(stack, amount));
        }
        else {
            dst.putStack(StackUtil.incSize(dstStack, amount));
        }
        stack = StackUtil.decSize(stack, amount);
        return stack;
    }
    
    private int getTransferAmount(final ItemStack stack, final Slot dst) {
        int amount = Math.min(dst.inventory.getInventoryStackLimit(), dst.getSlotStackLimit());
        amount = Math.min(amount, stack.isStackable() ? stack.getMaxStackSize() : 1);
        final ItemStack dstStack = dst.getStack();
        if (!StackUtil.isEmpty(dstStack)) {
            if (!StackUtil.checkItemEqualityStrict(stack, dstStack)) {
                return 0;
            }
            amount -= StackUtil.getSize(dstStack);
        }
        amount = Math.min(amount, StackUtil.getSize(stack));
        return amount;
    }
}
