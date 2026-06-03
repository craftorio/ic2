package ic2.core.block.transport.items;

import ic2.api.transport.IItemTransportTile;
import ic2.core.block.transport.TileEntityPipe;
import ic2.core.item.block.ItemPipe;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

public class TileEntityItemPipe extends TileEntityPipe implements IItemTransportTile {
  public TileEntityItemPipe() {}
  
  public void flipConnection(EnumFacing facing) {}
  
  public TileEntityItemPipe(PipeType type, PipeSize size) {
    this();
    this.type = type;
    this.size = size;
  }
  
  public int putItems(ItemStack stack, EnumFacing facing, boolean simulate) {
    if (StackUtil.isEmpty(stack))
      return 0; 
    if (!StackUtil.isEmpty(this.contents))
      return 0; 
    if (stack.func_190916_E() > getMaxStackSizeAllowed())
      return 0; 
    if (!simulate)
      this.contents = StackUtil.copy(stack); 
    return stack.func_190916_E();
  }
  
  public int getMaxStackSizeAllowed() {
    return this.size.maxStackSize;
  }
  
  public int getTransferRate() {
    return this.type.transferRate;
  }
  
  public ItemStack getContents() {
    return this.contents;
  }
  
  public void setContents(ItemStack stack) {
    this.contents = stack;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInventoryUpdate = false;
    if (!StackUtil.isEmpty(this.contents)) {
      EnumFacing facing = getFacing();
      TileEntity target = this.field_145850_b.func_175625_s(this.field_174879_c.func_177972_a(facing));
      if (target instanceof IItemTransportTile && (
        (IItemTransportTile)target).putItems(this.contents, facing.func_176734_d(), true) > 0) {
        int amount = ((IItemTransportTile)target).putItems(this.contents, facing.func_176734_d(), false);
        ItemStack newStack = StackUtil.copyShrunk(this.contents, amount);
        assert newStack.func_190926_b();
        this.contents = null;
        needsInventoryUpdate = true;
      } 
    } 
    if (needsInventoryUpdate)
      func_70296_d(); 
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.type = PipeType.values[nbt.func_74771_c("type") & 0xFF];
    this.size = PipeSize.values()[nbt.func_74771_c("size") & 0xFF];
    NBTTagList contentsTag = nbt.func_150295_c("contents", 10);
    for (int i = 0; i < contentsTag.func_74745_c(); i++) {
      NBTTagCompound contentTag = contentsTag.func_150305_b(i);
      ItemStack stack = new ItemStack(contentTag);
      if (!StackUtil.isEmpty(stack))
        this.contents = stack; 
    } 
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74774_a("type", (byte)this.type.ordinal());
    nbt.func_74774_a("size", (byte)this.size.ordinal());
    NBTTagList contentsTag = new NBTTagList();
    if (!StackUtil.isEmpty(this.contents)) {
      NBTTagCompound contentTag = new NBTTagCompound();
      this.contents.func_77955_b(contentTag);
      contentsTag.func_74742_a((NBTBase)contentTag);
      nbt.func_74782_a("contents", (NBTBase)contentsTag);
    } 
    return nbt;
  }
  
  protected void updateConnectivity() {}
  
  protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target) {
    return ItemPipe.getPipe(this.type, this.size);
  }
  
  protected List<ItemStack> getAuxDrops(int fortune) {
    List<ItemStack> ret = new ArrayList<>(super.getAuxDrops(fortune));
    if (!StackUtil.isEmpty(this.contents))
      ret.add(this.contents); 
    return ret;
  }
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    float th = this.size.thickness;
    float sp = (1.0F - th) / 2.0F;
    List<AxisAlignedBB> ret = new ArrayList<>(7);
    ret.add(new AxisAlignedBB(sp, sp, sp, (sp + th), (sp + th), (sp + th)));
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      boolean hasConnection = ((this.connectivity & 1 << facing.ordinal()) != 0);
      if (hasConnection) {
        float zS = sp, yS = zS, xS = yS;
        float zE = sp + th, yE = zE, xE = yE;
        switch (facing) {
          case DOWN:
            yS = 0.0F;
            yE = sp;
            break;
          case UP:
            yS = sp + th;
            yE = 1.0F;
            break;
          case NORTH:
            zS = 0.0F;
            zE = sp;
            break;
          case SOUTH:
            zS = sp + th;
            zE = 1.0F;
            break;
          case WEST:
            xS = 0.0F;
            xE = sp;
            break;
          case EAST:
            xS = sp + th;
            xE = 1.0F;
            break;
          default:
            throw new RuntimeException();
        } 
        ret.add(new AxisAlignedBB(xS, yS, zS, xE, yE, zE));
      } 
    } 
    return ret;
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("type");
    ret.add("size");
    return ret;
  }
  
  protected void updateRenderState() {
    this.renderState = new TileEntityPipe.PipeRenderState(this.type, this.size, this.connectivity, this.covers, getFacing().ordinal());
  }
  
  protected PipeType type = PipeType.bronze;
  
  protected PipeSize size = PipeSize.small;
  
  protected ItemStack contents;
}
