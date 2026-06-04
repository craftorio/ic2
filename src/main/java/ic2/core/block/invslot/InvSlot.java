// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Collections;
import net.minecraft.util.EnumFacing;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Arrays;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import ic2.core.util.StackUtil;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.IInventorySlotHolder;
import net.minecraft.item.ItemStack;

public class InvSlot implements Iterable<ItemStack>
{
    public final IInventorySlotHolder<?> base;
    public final String name;
    private final ItemStack[] contents;
    protected final Access access;
    public final InvSide preferredSide;
    private int stackSizeLimit;
    
    public InvSlot(final IInventorySlotHolder<?> base, final String name, final Access access, final int count) {
        this(base, name, access, count, InvSide.ANY);
    }
    
    public InvSlot(final IInventorySlotHolder<?> base, final String name, final Access access, final int count, final InvSide preferredSide) {
        if (count <= 0) {
            throw new IllegalArgumentException("invalid slot count: " + count);
        }
        this.contents = new ItemStack[count];
        this.clear();
        this.base = base;
        this.name = name;
        this.access = access;
        this.preferredSide = preferredSide;
        this.stackSizeLimit = 64;
        base.addInventorySlot(this);
    }
    
    public InvSlot(final int count) {
        this.contents = new ItemStack[count];
        this.clear();
        this.base = null;
        this.name = null;
        this.access = Access.NONE;
        this.preferredSide = InvSide.ANY;
    }
    
    public void readFromNbt(final NBTTagCompound nbt) {
        this.clear();
        final NBTTagList contentsTag = nbt.getTagList("Contents", 10);
        for (int i = 0; i < contentsTag.tagCount(); ++i) {
            final NBTTagCompound contentTag = contentsTag.getCompoundTagAt(i);
            final int index = contentTag.getByte("Index") & 0xFF;
            if (index >= this.size()) {
                IC2.log.error(LogCategory.Block, "Can't load item stack for %s, slot %s, index %d is out of bounds.", Util.toString((TileEntity)this.base.getParent()), this.name, index);
            }
            else {
                final ItemStack stack = new ItemStack(contentTag);
                if (StackUtil.isEmpty(stack)) {
                    IC2.log.warn(LogCategory.Block, "Can't load item stack %s for %s, slot %s, index %d, no matching item for %d:%d.", StackUtil.toStringSafe(stack), Util.toString((TileEntity)this.base.getParent()), this.name, index, contentTag.getShort("id"), contentTag.getShort("Damage"));
                }
                else {
                    if (!this.isEmpty(index)) {
                        IC2.log.error(LogCategory.Block, "Loading content to non-empty slot for %s, slot %s, index %d, replacing %s with %s.", Util.toString((TileEntity)this.base.getParent()), this.name, index, this.get(index), stack);
                    }
                    this.putFromNBT(index, stack);
                }
            }
        }
        this.onChanged();
    }
    
    public void writeToNbt(final NBTTagCompound nbt) {
        final NBTTagList contentsTag = new NBTTagList();
        for (int i = 0; i < this.contents.length; ++i) {
            final ItemStack content = this.contents[i];
            if (!StackUtil.isEmpty(content)) {
                final NBTTagCompound contentTag = new NBTTagCompound();
                contentTag.setByte("Index", (byte)i);
                content.writeToNBT(contentTag);
                contentsTag.appendTag((NBTBase)contentTag);
            }
        }
        nbt.setTag("Contents", (NBTBase)contentsTag);
    }
    
    public int size() {
        return this.contents.length;
    }
    
    public boolean isEmpty() {
        for (final ItemStack stack : this.contents) {
            if (!StackUtil.isEmpty(stack)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isEmpty(final int index) {
        return StackUtil.isEmpty(this.contents[index]);
    }
    
    public ItemStack get() {
        return this.get(0);
    }
    
    public ItemStack get(final int index) {
        return this.contents[index];
    }
    
    public void put(final ItemStack content) {
        this.put(0, content);
    }
    
    protected void putFromNBT(final int index, final ItemStack content) {
        this.contents[index] = content;
    }
    
    public void put(final int index, ItemStack content) {
        if (StackUtil.isEmpty(content)) {
            content = StackUtil.emptyStack;
        }
        this.contents[index] = content;
        this.onChanged();
    }
    
    public void clear() {
        Arrays.fill(this.contents, StackUtil.emptyStack);
    }
    
    public void clear(final int index) {
        this.put(index, StackUtil.emptyStack);
    }
    
    public void onChanged() {
    }
    
    public boolean accepts(final ItemStack stack) {
        return true;
    }
    
    public boolean canInput() {
        return this.access == Access.I || this.access == Access.IO;
    }
    
    public boolean canOutput() {
        return this.access == Access.O || this.access == Access.IO;
    }
    
    public void organize() {
        for (int dstIndex = 0; dstIndex < this.contents.length - 1; ++dstIndex) {
            ItemStack dst = this.contents[dstIndex];
            if (StackUtil.isEmpty(dst) || StackUtil.getSize(dst) < dst.getMaxStackSize()) {
                for (int srcIndex = dstIndex + 1; srcIndex < this.contents.length; ++srcIndex) {
                    final ItemStack src = this.contents[srcIndex];
                    if (!StackUtil.isEmpty(src)) {
                        if (StackUtil.isEmpty(dst)) {
                            this.contents[srcIndex] = StackUtil.emptyStack;
                            dst = (this.contents[dstIndex] = src);
                        }
                        else if (StackUtil.checkItemEqualityStrict(dst, src)) {
                            final int space = Math.min(this.getStackSizeLimit(), dst.getMaxStackSize() - StackUtil.getSize(dst));
                            final int srcSize = StackUtil.getSize(src);
                            if (srcSize > space) {
                                this.contents[srcIndex] = StackUtil.decSize(src, space);
                                this.contents[dstIndex] = StackUtil.incSize(dst, space);
                                break;
                            }
                            this.contents[srcIndex] = StackUtil.emptyStack;
                            dst = (this.contents[dstIndex] = StackUtil.incSize(dst, srcSize));
                            if (srcSize == space) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    public int getStackSizeLimit() {
        return this.stackSizeLimit;
    }
    
    public void setStackSizeLimit(final int stackSizeLimit) {
        this.stackSizeLimit = stackSizeLimit;
    }
    
    @Override
    public Iterator<ItemStack> iterator() {
        return new Iterator<ItemStack>() {
            private int idx = 0;
            
            @Override
            public boolean hasNext() {
                return this.idx < InvSlot.this.contents.length;
            }
            
            @Override
            public ItemStack next() {
                if (this.idx >= InvSlot.this.contents.length) {
                    throw new NoSuchElementException();
                }
                return InvSlot.this.contents[this.idx++];
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public String toString() {
        String ret = this.name + "[" + this.contents.length + "]: ";
        for (int i = 0; i < this.contents.length; ++i) {
            ret += this.contents[i];
            if (i < this.contents.length - 1) {
                ret += ", ";
            }
        }
        return ret;
    }
    
    protected ItemStack[] backup() {
        final ItemStack[] ret = new ItemStack[this.contents.length];
        for (int i = 0; i < this.contents.length; ++i) {
            final ItemStack content = this.contents[i];
            ret[i] = (StackUtil.isEmpty(content) ? StackUtil.emptyStack : content.copy());
        }
        return ret;
    }
    
    protected void restore(final ItemStack[] backup) {
        if (backup.length != this.contents.length) {
            throw new IllegalArgumentException("invalid array size");
        }
        for (int i = 0; i < this.contents.length; ++i) {
            this.contents[i] = backup[i];
        }
    }
    
    public void onPickupFromSlot(final EntityPlayer player, final ItemStack stack) {
    }
    
    public enum Access
    {
        NONE, 
        I, 
        O, 
        IO;
        
        public boolean isInput() {
            return (this.ordinal() & 0x1) != 0x0;
        }
        
        public boolean isOutput() {
            return (this.ordinal() & 0x2) != 0x0;
        }
    }
    
    public enum InvSide
    {
        ANY(new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST }), 
        TOP(new EnumFacing[] { EnumFacing.UP }), 
        BOTTOM(new EnumFacing[] { EnumFacing.DOWN }), 
        SIDE(new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST }), 
        NOTSIDE(new EnumFacing[0]);
        
        private Set<EnumFacing> acceptedSides;
        
        private InvSide(final EnumFacing[] sides) {
            if (sides.length == 0) {
                this.acceptedSides = Collections.emptySet();
            }
            else {
                final Set<EnumFacing> acceptedSides = EnumSet.noneOf(EnumFacing.class);
                acceptedSides.addAll(Arrays.asList(sides));
                this.acceptedSides = Collections.unmodifiableSet((Set<? extends EnumFacing>)acceptedSides);
            }
        }
        
        public boolean matches(final EnumFacing side) {
            return this.acceptedSides.contains(side);
        }
        
        public Set<EnumFacing> getAcceptedSides() {
            return this.acceptedSides;
        }
    }
}
