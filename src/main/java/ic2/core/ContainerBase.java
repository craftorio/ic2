package ic2.core;

import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public abstract class ContainerBase<T extends IInventory> extends Container {
   protected static final int windowBorder = 8;
   protected static final int slotSize = 16;
   protected static final int slotDistance = 2;
   protected static final int slotSeparator = 4;
   protected static final int hotbarYOffset = -24;
   protected static final int inventoryYOffset = -82;
   public final T base;

   public ContainerBase(T base1) {
      this.base = base1;
   }

   protected void addPlayerInventorySlots(EntityPlayer player, int height) {
      this.addPlayerInventorySlots(player, 178, height);
   }

   protected void addPlayerInventorySlots(EntityPlayer player, int width, int height) {
      int xStart = (width - 162) / 2;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, xStart + col * 18, height + -82 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlotToContainer(new Slot(player.inventory, col, xStart + col * 18, height + -24));
      }
   }

   public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
      Slot slot;
      return slotId >= 0 && slotId < this.inventorySlots.size() && (slot = (Slot)this.inventorySlots.get(slotId)) instanceof SlotHologramSlot
         ? ((SlotHologramSlot)slot).slotClick(dragType, clickType, player)
         : super.slotClick(slotId, dragType, clickType, player);
   }

   public final ItemStack transferStackInSlot(EntityPlayer player, int sourceSlotIndex) {
      Slot sourceSlot = (Slot)this.inventorySlots.get(sourceSlotIndex);
      if (sourceSlot != null && sourceSlot.getHasStack()) {
         ItemStack sourceItemStack = sourceSlot.getStack();
         int oldSourceItemStackSize = StackUtil.getSize(sourceItemStack);
         ItemStack resultStack;
         if (sourceSlot.inventory == player.inventory) {
            resultStack = this.handlePlayerSlotShiftClick(player, sourceItemStack);
         } else {
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

   protected ItemStack handlePlayerSlotShiftClick(EntityPlayer player, ItemStack sourceItemStack) {
      for (int run = 0; run < 4 && !StackUtil.isEmpty(sourceItemStack); run++) {
         for (Slot targetSlot : this.inventorySlots) {
            if (targetSlot.inventory != player.inventory && isValidTargetSlot(targetSlot, sourceItemStack, run % 2 == 1, run < 2)) {
               sourceItemStack = this.transfer(sourceItemStack, targetSlot);
               if (!StackUtil.isEmpty(sourceItemStack)) {
                  continue;
               }
               break;
            }
         }
      }

      return sourceItemStack;
   }

   protected ItemStack handleGUISlotShiftClick(EntityPlayer player, ItemStack sourceItemStack) {
      for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); run++) {
         ListIterator<Slot> it = this.inventorySlots.listIterator(this.inventorySlots.size());

         while (it.hasPrevious()) {
            Slot targetSlot = it.previous();
            if (targetSlot.inventory == player.inventory && isValidTargetSlot(targetSlot, sourceItemStack, run == 1, false)) {
               sourceItemStack = this.transfer(sourceItemStack, targetSlot);
               if (!StackUtil.isEmpty(sourceItemStack)) {
                  continue;
               }
               break;
            }
         }
      }

      return sourceItemStack;
   }

   protected static final boolean isValidTargetSlot(Slot slot, ItemStack stack, boolean allowEmpty, boolean requireInputOnly) {
      if (slot instanceof SlotInvSlotReadOnly || slot instanceof SlotHologramSlot) {
         return false;
      } else if (!slot.isItemValid(stack)) {
         return false;
      } else if (!allowEmpty && !slot.getHasStack()) {
         return false;
      } else {
         return !requireInputOnly ? true : slot instanceof SlotInvSlot && ((SlotInvSlot)slot).invSlot.canInput();
      }
   }

   public boolean canInteractWith(EntityPlayer entityplayer) {
      return this.base.isUsableByPlayer(entityplayer);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();
      if (this.base instanceof TileEntity) {
         for (String name : this.getNetworkedFields()) {
            for (IContainerListener crafter : this.listeners) {
               if (crafter instanceof EntityPlayerMP) {
                  IC2.network.get(true).updateTileEntityFieldTo((TileEntity)this.base, name, (EntityPlayerMP)crafter);
               }
            }
         }

         if (this.base instanceof TileEntityBlock) {
            for (TileEntityComponent component : ((TileEntityBlock)this.base).getComponents()) {
               for (IContainerListener crafter : this.listeners) {
                  if (crafter instanceof EntityPlayerMP) {
                     component.onContainerUpdate((EntityPlayerMP)crafter);
                  }
               }
            }
         }
      }
   }

   public List<String> getNetworkedFields() {
      return new ArrayList<>();
   }

   public List<IContainerListener> getListeners() {
      return this.listeners;
   }

   public void onContainerEvent(String event) {
   }

   protected final ItemStack transfer(ItemStack stack, Slot dst) {
      int amount = this.getTransferAmount(stack, dst);
      if (amount <= 0) {
         return stack;
      }

      ItemStack dstStack = dst.getStack();
      if (StackUtil.isEmpty(dstStack)) {
         dst.putStack(StackUtil.copyWithSize(stack, amount));
      } else {
         dst.putStack(StackUtil.incSize(dstStack, amount));
      }

      return StackUtil.decSize(stack, amount);
   }

   private int getTransferAmount(ItemStack stack, Slot dst) {
      int amount = Math.min(dst.inventory.getInventoryStackLimit(), dst.getSlotStackLimit());
      amount = Math.min(amount, stack.isStackable() ? stack.getMaxStackSize() : 1);
      ItemStack dstStack = dst.getStack();
      if (!StackUtil.isEmpty(dstStack)) {
         if (!StackUtil.checkItemEqualityStrict(stack, dstStack)) {
            return 0;
         }

         amount -= StackUtil.getSize(dstStack);
      }

      return Math.min(amount, StackUtil.getSize(stack));
   }
}
