package ic2.core.block.transport.cover;

import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class Covers extends TileEntityComponent {
  protected ItemStack[] covers;
  
  public Covers(TileEntityBlock parent) {
    super(parent);
    this.covers = new ItemStack[6];
  }
  
  public void addCover(EnumFacing side, ItemStack cover) {
    if (StackUtil.isEmpty(this.covers[side.ordinal()])) {
      ItemStack ret = cover.func_77946_l();
      NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(ret);
      nbtTagCompound.func_74774_a("side", (byte)side.ordinal());
      this.covers[side.ordinal()] = ret;
    } 
  }
  
  public ItemStack removeCover(EnumFacing side) {
    ItemStack ret = this.covers[side.ordinal()];
    ret.func_77982_d(null);
    this.covers[side.ordinal()] = null;
    return ret;
  }
  
  public boolean hasCover(EnumFacing side) {
    return !StackUtil.isEmpty(this.covers[side.ordinal()]);
  }
  
  public ICoverItem getCoverItem(EnumFacing side) {
    ItemStack stack = this.covers[side.ordinal()];
    if (StackUtil.isEmpty(stack))
      return null; 
    return (ICoverItem)stack.func_77973_b();
  }
  
  public void readFromNbt(NBTTagCompound nbt) {
    NBTTagList coversTag = nbt.func_150295_c("covers", 10);
    for (int i = 0; i < coversTag.func_74745_c(); i++) {
      NBTTagCompound coverTag = coversTag.func_150305_b(i);
      int index = coverTag.func_74771_c("facing") & 0xFF;
      if (index >= this.covers.length) {
        IC2.log.error(LogCategory.Block, "Can't load cover for %s, index %d is out of bounds.", new Object[] { Util.toString((TileEntity)this.parent), Integer.valueOf(index) });
      } else {
        ItemStack cover = new ItemStack(coverTag);
        if (StackUtil.isEmpty(cover)) {
          IC2.log.warn(LogCategory.Block, "Can't load cover %s for %s, index %d, no matching item for %d:%d.", new Object[] { StackUtil.toStringSafe(cover), Util.toString((TileEntity)this.parent), Integer.valueOf(index), Short.valueOf(coverTag.func_74765_d("id")), Short.valueOf(coverTag.func_74765_d("Damage")) });
        } else {
          if (!StackUtil.isEmpty(this.covers[index]))
            IC2.log.error(LogCategory.Block, "Loading cover to non-empty cover for %s, index %d, replacing %s with %s.", new Object[] { Util.toString((TileEntity)this.parent), Integer.valueOf(index), this.covers[index], cover }); 
          this.covers[index] = cover;
        } 
      } 
    } 
  }
  
  public NBTTagCompound writeToNbt() {
    NBTTagCompound ret = new NBTTagCompound();
    NBTTagList coversTag = new NBTTagList();
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      ItemStack cover = this.covers[facing.ordinal()];
      if (!StackUtil.isEmpty(cover)) {
        NBTTagCompound coverTag = new NBTTagCompound();
        coverTag.func_74774_a("facing", (byte)facing.ordinal());
        cover.func_77955_b(coverTag);
        coversTag.func_74742_a((NBTBase)coverTag);
      } 
    } 
    ret.func_74782_a("covers", (NBTBase)coversTag);
    return ret;
  }
  
  public boolean enableWorldTick() {
    return !(this.parent.func_145831_w()).field_72995_K;
  }
  
  public void onWorldTick() {
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      if (!StackUtil.isEmpty(this.covers[facing.ordinal()]))
        ((ICoverItem)this.covers[facing.ordinal()].func_77973_b()).onTick(this.covers[facing.ordinal()], (ICoverHolder)this.parent); 
    } 
  }
}
