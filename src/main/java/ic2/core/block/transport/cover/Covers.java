// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.cover;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumFacing;
import ic2.core.block.TileEntityBlock;
import net.minecraft.item.ItemStack;
import ic2.core.block.comp.TileEntityComponent;

public class Covers extends TileEntityComponent
{
    protected ItemStack[] covers;
    
    public Covers(final TileEntityBlock parent) {
        super(parent);
        this.covers = new ItemStack[6];
    }
    
    public void addCover(final EnumFacing side, final ItemStack cover) {
        if (StackUtil.isEmpty(this.covers[side.ordinal()])) {
            final ItemStack ret = cover.copy();
            final NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(ret);
            nbtTagCompound.setByte("side", (byte)side.ordinal());
            this.covers[side.ordinal()] = ret;
        }
    }
    
    public ItemStack removeCover(final EnumFacing side) {
        final ItemStack ret = this.covers[side.ordinal()];
        ret.setTagCompound((NBTTagCompound)null);
        this.covers[side.ordinal()] = null;
        return ret;
    }
    
    public boolean hasCover(final EnumFacing side) {
        return !StackUtil.isEmpty(this.covers[side.ordinal()]);
    }
    
    public ICoverItem getCoverItem(final EnumFacing side) {
        final ItemStack stack = this.covers[side.ordinal()];
        if (StackUtil.isEmpty(stack)) {
            return null;
        }
        return (ICoverItem)stack.getItem();
    }
    
    @Override
    public void readFromNbt(final NBTTagCompound nbt) {
        final NBTTagList coversTag = nbt.getTagList("covers", 10);
        for (int i = 0; i < coversTag.tagCount(); ++i) {
            final NBTTagCompound coverTag = coversTag.getCompoundTagAt(i);
            final int index = coverTag.getByte("facing") & 0xFF;
            if (index >= this.covers.length) {
                IC2.log.error(LogCategory.Block, "Can't load cover for %s, index %d is out of bounds.", Util.toString(this.parent), index);
            }
            else {
                final ItemStack cover = new ItemStack(coverTag);
                if (StackUtil.isEmpty(cover)) {
                    IC2.log.warn(LogCategory.Block, "Can't load cover %s for %s, index %d, no matching item for %d:%d.", StackUtil.toStringSafe(cover), Util.toString(this.parent), index, coverTag.getShort("id"), coverTag.getShort("Damage"));
                }
                else {
                    if (!StackUtil.isEmpty(this.covers[index])) {
                        IC2.log.error(LogCategory.Block, "Loading cover to non-empty cover for %s, index %d, replacing %s with %s.", Util.toString(this.parent), index, this.covers[index], cover);
                    }
                    this.covers[index] = cover;
                }
            }
        }
    }
    
    @Override
    public NBTTagCompound writeToNbt() {
        final NBTTagCompound ret = new NBTTagCompound();
        final NBTTagList coversTag = new NBTTagList();
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final ItemStack cover = this.covers[facing.ordinal()];
            if (!StackUtil.isEmpty(cover)) {
                final NBTTagCompound coverTag = new NBTTagCompound();
                coverTag.setByte("facing", (byte)facing.ordinal());
                cover.writeToNBT(coverTag);
                coversTag.appendTag((NBTBase)coverTag);
            }
        }
        ret.setTag("covers", (NBTBase)coversTag);
        return ret;
    }
    
    @Override
    public boolean enableWorldTick() {
        return !this.parent.getWorld().isRemote;
    }
    
    @Override
    public void onWorldTick() {
        for (final EnumFacing facing : EnumFacing.VALUES) {
            if (!StackUtil.isEmpty(this.covers[facing.ordinal()])) {
                ((ICoverItem)this.covers[facing.ordinal()].getItem()).onTick(this.covers[facing.ordinal()], (ICoverHolder)this.parent);
            }
        }
    }
}
