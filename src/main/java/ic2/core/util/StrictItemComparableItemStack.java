// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.Item;

public class StrictItemComparableItemStack
{
    private final Item item;
    private final int meta;
    private final NBTTagCompound nbt;
    private final int hashCode;
    
    public StrictItemComparableItemStack(final ItemStack stack, final boolean copyNbt) {
        this.item = stack.getItem();
        this.meta = StackUtil.getRawMeta(stack);
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null) {
            if (nbt.hasNoTags()) {
                nbt = null;
            }
            else if (copyNbt) {
                nbt = nbt.copy();
            }
        }
        this.nbt = nbt;
        this.hashCode = this.calculateHashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof StrictItemComparableItemStack)) {
            return false;
        }
        final StrictItemComparableItemStack cmp = (StrictItemComparableItemStack)obj;
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
}
