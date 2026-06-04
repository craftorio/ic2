// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.block.invslot.InvSlotUpgrade;
import java.util.Arrays;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import java.util.Collection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import java.util.Iterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import java.util.ArrayList;
import ic2.core.block.comp.ComparatorEmitter;
import net.minecraftforge.items.IItemHandler;
import ic2.core.block.invslot.InvSlot;
import java.util.List;
import net.minecraft.inventory.ISidedInventory;

public abstract class TileEntityInventory extends TileEntityBlock implements ISidedInventory, IInventorySlotHolder<TileEntityInventory>
{
    private final List<InvSlot> invSlots;
    private final IItemHandler[] itemHandler;
    protected final ComparatorEmitter comparator;
    
    public TileEntityInventory() {
        this.invSlots = new ArrayList<InvSlot>();
        this.itemHandler = new IItemHandler[EnumFacing.VALUES.length + 1];
        (this.comparator = this.addComponent(new ComparatorEmitter(this))).setUpdate(this::calcRedstoneFromInvSlots);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        final NBTTagCompound invSlotsTag = nbtTagCompound.getCompoundTag("InvSlots");
        for (final InvSlot invSlot : this.invSlots) {
            invSlot.readFromNbt(invSlotsTag.getCompoundTag(invSlot.name));
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        final NBTTagCompound invSlotsTag = new NBTTagCompound();
        for (final InvSlot invSlot : this.invSlots) {
            final NBTTagCompound invSlotTag = new NBTTagCompound();
            invSlot.writeToNbt(invSlotTag);
            invSlotsTag.setTag(invSlot.name, (NBTBase)invSlotTag);
        }
        nbt.setTag("InvSlots", (NBTBase)invSlotsTag);
        return nbt;
    }
    
    public int getSizeInventory() {
        int ret = 0;
        for (final InvSlot invSlot : this.invSlots) {
            ret += invSlot.size();
        }
        return ret;
    }
    
    public boolean isEmpty() {
        for (final InvSlot invSlot : this.invSlots) {
            if (!invSlot.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public ItemStack getStackInSlot(final int index) {
        final int loc = this.locateInvSlot(index);
        if (loc == -1) {
            return StackUtil.emptyStack;
        }
        return this.getStackAt(loc);
    }
    
    public ItemStack decrStackSize(final int index, int amount) {
        final int loc = this.locateInvSlot(index);
        if (loc == -1) {
            return StackUtil.emptyStack;
        }
        final ItemStack stack = this.getStackAt(loc);
        if (StackUtil.isEmpty(stack)) {
            return StackUtil.emptyStack;
        }
        if (amount >= StackUtil.getSize(stack)) {
            this.putStackAt(loc, StackUtil.emptyStack);
            return stack;
        }
        if (amount != 0) {
            if (amount < 0) {
                final int space = Math.min(this.getAt(loc).getStackSizeLimit(), stack.getMaxStackSize()) - StackUtil.getSize(stack);
                amount = Math.max(amount, -space);
            }
            this.putStackAt(loc, StackUtil.decSize(stack, amount));
        }
        ItemStack ret = stack.copy();
        ret = StackUtil.setSize(ret, amount);
        return ret;
    }
    
    public ItemStack removeStackFromSlot(final int index) {
        final int loc = this.locateInvSlot(index);
        if (loc == -1) {
            return StackUtil.emptyStack;
        }
        final ItemStack ret = this.getStackAt(loc);
        if (!StackUtil.isEmpty(ret)) {
            this.putStackAt(loc, StackUtil.emptyStack);
        }
        return ret;
    }
    
    public void setInventorySlotContents(final int index, ItemStack stack) {
        final int loc = this.locateInvSlot(index);
        if (loc != -1) {
            if (StackUtil.isEmpty(stack)) {
                stack = StackUtil.emptyStack;
            }
            this.putStackAt(loc, stack);
            return;
        }
        assert false;
    }
    
    public void markDirty() {
        super.markDirty();
        for (final InvSlot invSlot : this.invSlots) {
            invSlot.onChanged();
        }
    }
    
    public String getName() {
        final ITeBlock teBlock = TeBlockRegistry.get(this.getClass());
        final String name = (teBlock == null) ? "invalid" : teBlock.getName();
        return this.getBlockType().getUnlocalizedName() + "." + name;
    }
    
    public boolean hasCustomName() {
        return false;
    }
    
    public ITextComponent getDisplayName() {
        return (ITextComponent)new TextComponentString(this.getName());
    }
    
    public int getInventoryStackLimit() {
        int max = 0;
        for (final InvSlot slot : this.invSlots) {
            max = Math.max(max, slot.getStackSizeLimit());
        }
        return max;
    }
    
    public boolean isUsableByPlayer(final EntityPlayer player) {
        return !this.isInvalid() && player.getDistanceSq(this.pos) <= 64.0;
    }
    
    public void openInventory(final EntityPlayer player) {
    }
    
    public void closeInventory(final EntityPlayer player) {
    }
    
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        final InvSlot invSlot = this.getInventorySlot(index);
        return invSlot != null && invSlot.canInput() && invSlot.accepts(stack);
    }
    
    public int[] getSlotsForFace(final EnumFacing side) {
        final int[] ret = new int[this.getSizeInventory()];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = i;
        }
        return ret;
    }
    
    public boolean canInsertItem(final int index, final ItemStack stack, final EnumFacing side) {
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        final InvSlot targetSlot = this.getInventorySlot(index);
        if (targetSlot == null) {
            return false;
        }
        if (!targetSlot.canInput() || !targetSlot.accepts(stack)) {
            return false;
        }
        if (targetSlot.preferredSide != InvSlot.InvSide.ANY && targetSlot.preferredSide.matches(side)) {
            return true;
        }
        for (final InvSlot invSlot : this.invSlots) {
            if (invSlot != targetSlot && invSlot.preferredSide != InvSlot.InvSide.ANY && invSlot.preferredSide.matches(side) && invSlot.canInput() && invSlot.accepts(stack)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean canExtractItem(final int index, final ItemStack stack, final EnumFacing side) {
        final InvSlot targetSlot = this.getInventorySlot(index);
        if (targetSlot == null || !targetSlot.canOutput()) {
            return false;
        }
        final boolean correctSide = targetSlot.preferredSide.matches(side);
        if (targetSlot.preferredSide != InvSlot.InvSide.ANY && correctSide) {
            return true;
        }
        for (final InvSlot invSlot : this.invSlots) {
            if (invSlot != targetSlot && (invSlot.preferredSide != InvSlot.InvSide.ANY || !correctSide) && invSlot.preferredSide.matches(side) && invSlot.canOutput() && !invSlot.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public int getField(final int id) {
        return 0;
    }
    
    public void setField(final int id, final int value) {
    }
    
    public int getFieldCount() {
        return 0;
    }
    
    public void clear() {
        for (final InvSlot invSlot : this.invSlots) {
            invSlot.clear();
        }
    }
    
    public int getBaseIndex(final InvSlot invSlot) {
        int ret = 0;
        for (final InvSlot slot : this.invSlots) {
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
    
    public InvSlot getInventorySlot(final String name) {
        for (final InvSlot invSlot : this.invSlots) {
            if (invSlot.name.equals(name)) {
                return invSlot;
            }
        }
        return null;
    }
    
    public void addInventorySlot(final InvSlot inventorySlot) {
        assert this.invSlots.stream().noneMatch(slot -> slot.name.equals(inventorySlot.name));
        this.invSlots.add(inventorySlot);
    }
    
    private int locateInvSlot(int extIndex) {
        if (extIndex < 0) {
            return -1;
        }
        for (int i = 0; i < this.invSlots.size(); ++i) {
            final int size = this.invSlots.get(i).size();
            if (extIndex < size) {
                return i << 16 | extIndex;
            }
            extIndex -= size;
        }
        return -1;
    }
    
    private static int getIndex(final int loc) {
        return loc >>> 16;
    }
    
    private static int getOffset(final int loc) {
        return loc & 0xFFFF;
    }
    
    private InvSlot getAt(final int loc) {
        assert loc != -1;
        return this.invSlots.get(getIndex(loc));
    }
    
    private ItemStack getStackAt(final int loc) {
        return this.getAt(loc).get(getOffset(loc));
    }
    
    private void putStackAt(final int loc, final ItemStack stack) {
        this.getAt(loc).put(getOffset(loc), stack);
        super.markDirty();
    }
    
    private InvSlot getInventorySlot(final int extIndex) {
        final int loc = this.locateInvSlot(extIndex);
        if (loc == -1) {
            return null;
        }
        return this.getAt(loc);
    }
    
    @Override
    protected List<ItemStack> getAuxDrops(final int fortune) {
        final List<ItemStack> ret = new ArrayList<ItemStack>(super.getAuxDrops(fortune));
        for (final InvSlot slot : this.invSlots) {
            for (final ItemStack stack : slot) {
                if (StackUtil.isEmpty(stack)) {
                    continue;
                }
                ret.add(stack);
            }
        }
        return ret;
    }
    
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return super.getCapability(capability, facing);
        }
        if (facing == null) {
            if (this.itemHandler[this.itemHandler.length - 1] == null) {
                this.itemHandler[this.itemHandler.length - 1] = (IItemHandler)new InvWrapper((IInventory)this);
            }
            return (T)CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast((Object)this.itemHandler[this.itemHandler.length - 1]);
        }
        if (this.itemHandler[facing.ordinal()] == null) {
            this.itemHandler[facing.ordinal()] = (IItemHandler)new SidedInvWrapper((ISidedInventory)this, facing);
        }
        return (T)CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast((Object)this.itemHandler[facing.ordinal()]);
    }
    
    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }
    
    protected static int calcRedstoneFromInvSlots(final InvSlot... slots) {
        return calcRedstoneFromInvSlots(Arrays.asList(slots));
    }
    
    protected int calcRedstoneFromInvSlots() {
        return calcRedstoneFromInvSlots(this.invSlots);
    }
    
    protected static int calcRedstoneFromInvSlots(final Iterable<InvSlot> invSlots) {
        int space = 0;
        int used = 0;
        for (final InvSlot slot : invSlots) {
            if (slot instanceof InvSlotUpgrade) {
                continue;
            }
            final int size = slot.size();
            final int limit = slot.getStackSizeLimit();
            space += size * limit;
            for (int i = 0; i < size; ++i) {
                final ItemStack stack = slot.get(i);
                if (!StackUtil.isEmpty(stack)) {
                    used += Math.min(limit, stack.getCount() * limit / stack.getMaxStackSize());
                }
            }
        }
        if (used == 0 || space == 0) {
            return 0;
        }
        return 1 + used * 14 / space;
    }
}
