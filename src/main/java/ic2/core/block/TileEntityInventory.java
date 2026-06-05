package ic2.core.block;

import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public abstract class TileEntityInventory extends TileEntityBlock implements ISidedInventory, IInventorySlotHolder<TileEntityInventory> {
   private final List<InvSlot> invSlots = new ArrayList<>();
   private final IItemHandler[] itemHandler = new IItemHandler[EnumFacing.VALUES.length + 1];
   protected final ComparatorEmitter comparator = this.addComponent(new ComparatorEmitter(this));

   public TileEntityInventory() {
      this.comparator.setUpdate(this::calcRedstoneFromInvSlots);
   }

   @Override
   public void readFromNBT(NBTTagCompound nbtTagCompound) {
      super.readFromNBT(nbtTagCompound);
      NBTTagCompound invSlotsTag = nbtTagCompound.getCompoundTag("InvSlots");

      for (InvSlot invSlot : this.invSlots) {
         invSlot.readFromNbt(invSlotsTag.getCompoundTag(invSlot.name));
      }
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      NBTTagCompound invSlotsTag = new NBTTagCompound();

      for (InvSlot invSlot : this.invSlots) {
         NBTTagCompound invSlotTag = new NBTTagCompound();
         invSlot.writeToNbt(invSlotTag);
         invSlotsTag.setTag(invSlot.name, invSlotTag);
      }

      nbt.setTag("InvSlots", invSlotsTag);
      return nbt;
   }

   public int getSizeInventory() {
      int ret = 0;

      for (InvSlot invSlot : this.invSlots) {
         ret += invSlot.size();
      }

      return ret;
   }

   public boolean isEmpty() {
      for (InvSlot invSlot : this.invSlots) {
         if (!invSlot.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getStackInSlot(int index) {
      int loc = this.locateInvSlot(index);
      return loc == -1 ? StackUtil.emptyStack : this.getStackAt(loc);
   }

   public ItemStack decrStackSize(int index, int amount) {
      int loc = this.locateInvSlot(index);
      if (loc == -1) {
         return StackUtil.emptyStack;
      }

      ItemStack stack = this.getStackAt(loc);
      if (StackUtil.isEmpty(stack)) {
         return StackUtil.emptyStack;
      }

      if (amount >= StackUtil.getSize(stack)) {
         this.putStackAt(loc, StackUtil.emptyStack);
         return stack;
      }

      if (amount != 0) {
         if (amount < 0) {
            int space = Math.min(this.getAt(loc).getStackSizeLimit(), stack.getMaxStackSize()) - StackUtil.getSize(stack);
            amount = Math.max(amount, -space);
         }

         this.putStackAt(loc, StackUtil.decSize(stack, amount));
      }

      ItemStack ret = stack.copy();
      return StackUtil.setSize(ret, amount);
   }

   public ItemStack removeStackFromSlot(int index) {
      int loc = this.locateInvSlot(index);
      if (loc == -1) {
         return StackUtil.emptyStack;
      }

      ItemStack ret = this.getStackAt(loc);
      if (!StackUtil.isEmpty(ret)) {
         this.putStackAt(loc, StackUtil.emptyStack);
      }

      return ret;
   }

   public void setInventorySlotContents(int index, ItemStack stack) {
      int loc = this.locateInvSlot(index);
      if (loc == -1) {
         assert false;
      } else {
         if (StackUtil.isEmpty(stack)) {
            stack = StackUtil.emptyStack;
         }

         this.putStackAt(loc, stack);
      }
   }

   public void markDirty() {
      super.markDirty();

      for (InvSlot invSlot : this.invSlots) {
         invSlot.onChanged();
      }
   }

   public String getName() {
      ITeBlock teBlock = TeBlockRegistry.get((Class<? extends TileEntityBlock>)this.getClass());
      String name = teBlock == null ? "invalid" : teBlock.getName();
      return this.getBlockType().getUnlocalizedName() + "." + name;
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public int getInventoryStackLimit() {
      int max = 0;

      for (InvSlot slot : this.invSlots) {
         max = Math.max(max, slot.getStackSizeLimit());
      }

      return max;
   }

   public boolean isUsableByPlayer(EntityPlayer player) {
      return !this.isInvalid() && player.getDistanceSq(this.pos) <= 64.0;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }

      InvSlot invSlot = this.getInventorySlot(index);
      return invSlot != null && invSlot.canInput() && invSlot.accepts(stack);
   }

   public int[] getSlotsForFace(EnumFacing side) {
      int[] ret = new int[this.getSizeInventory()];
      int i = 0;

      while (i < ret.length) {
         ret[i] = i++;
      }

      return ret;
   }

   public boolean canInsertItem(int index, ItemStack stack, EnumFacing side) {
      if (StackUtil.isEmpty(stack)) {
         return false;
      }

      InvSlot targetSlot = this.getInventorySlot(index);
      if (targetSlot == null) {
         return false;
      }

      if (targetSlot.canInput() && targetSlot.accepts(stack)) {
         if (targetSlot.preferredSide != InvSlot.InvSide.ANY && targetSlot.preferredSide.matches(side)) {
            return true;
         }

         for (InvSlot invSlot : this.invSlots) {
            if (invSlot != targetSlot
               && invSlot.preferredSide != InvSlot.InvSide.ANY
               && invSlot.preferredSide.matches(side)
               && invSlot.canInput()
               && invSlot.accepts(stack)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean canExtractItem(int index, ItemStack stack, EnumFacing side) {
      InvSlot targetSlot = this.getInventorySlot(index);
      if (targetSlot != null && targetSlot.canOutput()) {
         boolean correctSide = targetSlot.preferredSide.matches(side);
         if (targetSlot.preferredSide != InvSlot.InvSide.ANY && correctSide) {
            return true;
         }

         for (InvSlot invSlot : this.invSlots) {
            if (invSlot != targetSlot
               && (invSlot.preferredSide != InvSlot.InvSide.ANY || !correctSide)
               && invSlot.preferredSide.matches(side)
               && invSlot.canOutput()
               && !invSlot.isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      for (InvSlot invSlot : this.invSlots) {
         invSlot.clear();
      }
   }

   @Override
   public int getBaseIndex(InvSlot invSlot) {
      int ret = 0;

      for (InvSlot slot : this.invSlots) {
         if (slot == invSlot) {
            return ret;
         }

         ret += slot.size();
      }

      return -1;
   }

   public TileEntityInventory getParent() {
      return this;
   }

   @Override
   public InvSlot getInventorySlot(String name) {
      for (InvSlot invSlot : this.invSlots) {
         if (invSlot.name.equals(name)) {
            return invSlot;
         }
      }

      return null;
   }

   @Override
   public void addInventorySlot(InvSlot inventorySlot) {
      assert this.invSlots.stream().noneMatch(slot -> slot.name.equals(inventorySlot.name));
      this.invSlots.add(inventorySlot);
   }

   private int locateInvSlot(int extIndex) {
      if (extIndex < 0) {
         return -1;
      }

      for (int i = 0; i < this.invSlots.size(); i++) {
         int size = this.invSlots.get(i).size();
         if (extIndex < size) {
            return i << 16 | extIndex;
         }

         extIndex -= size;
      }

      return -1;
   }

   private static int getIndex(int loc) {
      return loc >>> 16;
   }

   private static int getOffset(int loc) {
      return loc & 65535;
   }

   private InvSlot getAt(int loc) {
      assert loc != -1;
      return this.invSlots.get(getIndex(loc));
   }

   private ItemStack getStackAt(int loc) {
      return this.getAt(loc).get(getOffset(loc));
   }

   private void putStackAt(int loc, ItemStack stack) {
      this.getAt(loc).put(getOffset(loc), stack);
      super.markDirty();
   }

   private InvSlot getInventorySlot(int extIndex) {
      int loc = this.locateInvSlot(extIndex);
      return loc == -1 ? null : this.getAt(loc);
   }

   @Override
   protected List<ItemStack> getAuxDrops(int fortune) {
      List<ItemStack> ret = new ArrayList<>(super.getAuxDrops(fortune));

      for (InvSlot slot : this.invSlots) {
         for (ItemStack stack : slot) {
            if (!StackUtil.isEmpty(stack)) {
               ret.add(stack);
            }
         }
      }

      return ret;
   }

   @Override
   public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
      if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (facing == null) {
            if (this.itemHandler[this.itemHandler.length - 1] == null) {
               this.itemHandler[this.itemHandler.length - 1] = new InvWrapper(this);
            }

            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemHandler[this.itemHandler.length - 1]);
         } else {
            if (this.itemHandler[facing.ordinal()] == null) {
               this.itemHandler[facing.ordinal()] = new SidedInvWrapper(this, facing);
            }

            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemHandler[facing.ordinal()]);
         }
      } else {
         return super.getCapability(capability, facing);
      }
   }

   @Override
   public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
      return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
   }

   protected static int calcRedstoneFromInvSlots(InvSlot... slots) {
      return calcRedstoneFromInvSlots(Arrays.asList(slots));
   }

   protected int calcRedstoneFromInvSlots() {
      return calcRedstoneFromInvSlots(this.invSlots);
   }

   protected static int calcRedstoneFromInvSlots(Iterable<InvSlot> invSlots) {
      int space = 0;
      int used = 0;

      for (InvSlot slot : invSlots) {
         if (!(slot instanceof InvSlotUpgrade)) {
            int size = slot.size();
            int limit = slot.getStackSizeLimit();
            space += size * limit;

            for (int i = 0; i < size; i++) {
               ItemStack stack = slot.get(i);
               if (!StackUtil.isEmpty(stack)) {
                  used += Math.min(limit, stack.getCount() * limit / stack.getMaxStackSize());
               }
            }
         }
      }

      return used != 0 && space != 0 ? 1 + used * 14 / space : 0;
   }
}
