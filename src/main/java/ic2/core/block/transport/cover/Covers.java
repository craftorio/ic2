package ic2.core.block.transport.cover;

import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;

public class Covers extends TileEntityComponent {
   protected ItemStack[] covers = new ItemStack[6];

   public Covers(TileEntityBlock parent) {
      super(parent);
   }

   public void addCover(EnumFacing side, ItemStack cover) {
      if (StackUtil.isEmpty(this.covers[side.ordinal()])) {
         ItemStack ret = cover.copy();
         NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(ret);
         nbtTagCompound.setByte("side", (byte)side.ordinal());
         this.covers[side.ordinal()] = ret;
      }
   }

   public ItemStack removeCover(EnumFacing side) {
      ItemStack ret = this.covers[side.ordinal()];
      ret.setTagCompound(null);
      this.covers[side.ordinal()] = null;
      return ret;
   }

   public boolean hasCover(EnumFacing side) {
      return !StackUtil.isEmpty(this.covers[side.ordinal()]);
   }

   public ICoverItem getCoverItem(EnumFacing side) {
      ItemStack stack = this.covers[side.ordinal()];
      return StackUtil.isEmpty(stack) ? null : (ICoverItem)stack.getItem();
   }

   @Override
   public void readFromNbt(NBTTagCompound nbt) {
      NBTTagList coversTag = nbt.getTagList("covers", 10);

      for (int i = 0; i < coversTag.tagCount(); i++) {
         NBTTagCompound coverTag = coversTag.getCompoundTagAt(i);
         int index = coverTag.getByte("facing") & 255;
         if (index >= this.covers.length) {
            IC2.log.error(LogCategory.Block, "Can't load cover for %s, index %d is out of bounds.", Util.toString(this.parent), index);
         } else {
            ItemStack cover = new ItemStack(coverTag);
            if (StackUtil.isEmpty(cover)) {
               IC2.log
                  .warn(
                     LogCategory.Block,
                     "Can't load cover %s for %s, index %d, no matching item for %d:%d.",
                     StackUtil.toStringSafe(cover),
                     Util.toString(this.parent),
                     index,
                     coverTag.getShort("id"),
                     coverTag.getShort("Damage")
                  );
            } else {
               if (!StackUtil.isEmpty(this.covers[index])) {
                  IC2.log
                     .error(
                        LogCategory.Block,
                        "Loading cover to non-empty cover for %s, index %d, replacing %s with %s.",
                        Util.toString(this.parent),
                        index,
                        this.covers[index],
                        cover
                     );
               }

               this.covers[index] = cover;
            }
         }
      }
   }

   @Override
   public NBTTagCompound writeToNbt() {
      NBTTagCompound ret = new NBTTagCompound();
      NBTTagList coversTag = new NBTTagList();

      for (EnumFacing facing : EnumFacing.VALUES) {
         ItemStack cover = this.covers[facing.ordinal()];
         if (!StackUtil.isEmpty(cover)) {
            NBTTagCompound coverTag = new NBTTagCompound();
            coverTag.setByte("facing", (byte)facing.ordinal());
            cover.writeToNBT(coverTag);
            coversTag.appendTag(coverTag);
         }
      }

      ret.setTag("covers", coversTag);
      return ret;
   }

   @Override
   public boolean enableWorldTick() {
      return !this.parent.getWorld().isRemote;
   }

   @Override
   public void onWorldTick() {
      for (EnumFacing facing : EnumFacing.VALUES) {
         if (!StackUtil.isEmpty(this.covers[facing.ordinal()])) {
            ((ICoverItem)this.covers[facing.ordinal()].getItem()).onTick(this.covers[facing.ordinal()], (ICoverHolder)this.parent);
         }
      }
   }
}
