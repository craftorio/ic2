// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.util.Iterator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.Item;

public class ItemComparableItemStack
{
    private final Item item;
    private final int meta;
    private final NBTTagCompound nbt;
    private final int hashCode;
    
    public ItemComparableItemStack(final ItemStack stack, final boolean copyNbt) {
        this.item = stack.getItem();
        this.meta = (stack.getHasSubtypes() ? stack.getMetadata() : 0);
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null) {
            if (nbt.hasNoTags()) {
                nbt = null;
            }
            else {
                if (copyNbt) {
                    nbt = nbt.copy();
                }
                boolean copied = copyNbt;
                for (final String key : StackUtil.ignoredNbtKeys) {
                    if (!copied && nbt.hasKey(key)) {
                        nbt = nbt.copy();
                        copied = true;
                    }
                    nbt.removeTag(key);
                }
                if (nbt.hasNoTags()) {
                    nbt = null;
                }
            }
        }
        this.nbt = nbt;
        this.hashCode = this.calculateHashCode();
    }
    
    private ItemComparableItemStack(final ItemComparableItemStack src) {
        this.item = src.item;
        this.meta = src.meta;
        this.nbt = ((src.nbt != null) ? src.nbt.copy() : null);
        this.hashCode = src.hashCode;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ItemComparableItemStack)) {
            return false;
        }
        final ItemComparableItemStack cmp = (ItemComparableItemStack)obj;
        return cmp.hashCode == this.hashCode && (cmp == this || (cmp.item == this.item && cmp.meta == this.meta && ((cmp.nbt == null && this.nbt == null) || (cmp.nbt != null && this.nbt != null && cmp.nbt.equals((Object)this.nbt)))));
    }
    
    @Override
    public int hashCode() {
        return this.hashCode;
    }
    
    private int calculateHashCode() {
        int ret = 0;
        if (this.item != null) {
            ret = System.identityHashCode(this.item);
        }
        ret = ret * 31 + this.meta;
        if (this.nbt != null) {
            ret = ret * 61 + this.nbt.hashCode();
        }
        return ret;
    }
    
    public ItemComparableItemStack copy() {
        if (this.nbt == null) {
            return this;
        }
        return new ItemComparableItemStack(this);
    }
    
    public ItemStack toStack() {
        return this.toStack(1);
    }
    
    public ItemStack toStack(final int size) {
        if (this.item == null) {
            return null;
        }
        final ItemStack ret = new ItemStack(this.item, size, this.meta);
        ret.setTagCompound(this.nbt);
        return ret;
    }
}
