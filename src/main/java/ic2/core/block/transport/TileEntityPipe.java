package ic2.core.block.transport;

import ic2.api.transport.IPipe;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.UnlistedProperty;
import ic2.core.block.transport.cover.CoverProperty;
import ic2.core.block.transport.cover.Covers;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.core.block.transport.cover.ICoverItem;
import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import ic2.core.network.NetworkManager;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;

public abstract class TileEntityPipe extends TileEntityBlock implements IPipe, ICoverHolder {
  protected final Covers coversComponent = (Covers)addComponent((TileEntityComponent)new Covers(this));
  
  public TileEntity getTile() {
    return (TileEntity)this;
  }
  
  public boolean isConnected(EnumFacing facing) {
    return ((this.connectivity & 1 << facing.ordinal()) != 0);
  }
  
  public abstract void flipConnection(EnumFacing paramEnumFacing);
  
  public Set<CoverProperty> getCoverProperties() {
    return Collections.emptySet();
  }
  
  public boolean canPlaceCover(World world, BlockPos pos, EnumFacing side, ItemStack stack) {
    Item rawItem = stack.getItem();
    if (!(rawItem instanceof ICoverItem))
      return false; 
    ICoverItem item = (ICoverItem)rawItem;
    return (item.isSuitableFor(stack, super.getCoverProperties()) && (this.covers & 1 << side
      .ordinal()) == 0);
  }
  
  public void placeCover(World world, BlockPos pos, EnumFacing side, ItemStack stack) {
    this.coversComponent.addCover(side, stack);
    this.covers = (byte)(this.covers ^ 1 << side.ordinal());
    ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "covers");
  }
  
  public boolean canRemoveCover(World world, BlockPos pos, EnumFacing side) {
    return ((this.covers & 1 << side.ordinal()) != 0);
  }
  
  public void removeCover(World world, BlockPos pos, EnumFacing side) {
    ItemStack ret = this.coversComponent.removeCover(side);
    this.covers = (byte)(this.covers ^ 1 << side.ordinal());
    ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "covers");
    StackUtil.dropAsEntity(getWorld(), getPos(), StackUtil.copyWithSize(ret, 1));
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if ((getWorld()).isRemote)
      updateRenderState(); 
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.connectivity = nbt.getByte("connectivity");
    this.covers = nbt.getByte("covers");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setByte("connectivity", this.connectivity);
    nbt.setByte("covers", this.covers);
    return nbt;
  }
  
  protected void onUnloaded() {
    super.onUnloaded();
  }
  
  public void onNeighborChange(Block neighbor, BlockPos neighborPos) {
    super.onNeighborChange(neighbor, neighborPos);
  }
  
  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    if (this.world.isRemote)
      updateRenderState(); 
    super.onPlaced(stack, placer, facing);
  }
  
  protected abstract void updateConnectivity();
  
  public Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state) {
    state = super.getExtendedState(state);
    PipeRenderState pipeRenderState = this.renderState;
    if (pipeRenderState != null)
      state = state.withProperties(new Object[] { renderStateProperty, pipeRenderState }); 
    return state;
  }
  
  protected SoundType getBlockSound(Entity entity) {
    return SoundType.field_185852_e;
  }
  
  protected boolean isNormalCube() {
    return false;
  }
  
  protected boolean isSideSolid(EnumFacing side) {
    return false;
  }
  
  protected boolean doesSideBlockRendering(EnumFacing side) {
    return false;
  }
  
  protected int getLightOpacity() {
    return 0;
  }
  
  protected boolean clientNeedsExtraModelInfo() {
    return true;
  }
  
  public void onNetworkUpdate(String field) {
    updateRenderState();
    rerender();
    super.onNetworkUpdate(field);
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("connectivity");
    ret.add("covers");
    return ret;
  }
  
  protected List<ItemStack> getAuxDrops(int fortune) {
    List<ItemStack> ret = new ArrayList<>();
    for (EnumFacing facing : EnumFacing.VALUES) {
      if (this.coversComponent.hasCover(facing))
        ret.add(this.coversComponent.removeCover(facing)); 
    } 
    return ret;
  }
  
  public static class PipeRenderState {
    public final PipeType type;
    
    public final PipeSize size;
    
    public final int connectivity;
    
    public final int covers;
    
    public final int facing;
    
    public PipeRenderState(PipeType type, PipeSize size, int connectivity, int covers, int facing) {
      this.type = type;
      this.size = size;
      this.connectivity = connectivity;
      this.covers = covers;
      this.facing = facing;
    }
    
    public int hashCode() {
      int ret = this.type.hashCode();
      ret = ret * 31 + this.size.hashCode();
      ret = ret * 31 + this.connectivity;
      ret = ret * 31 + this.covers;
      ret = ret * 31 + this.facing;
      return ret;
    }
    
    public boolean equals(Object obj) {
      if (obj == this)
        return true; 
      if (!(obj instanceof PipeRenderState))
        return false; 
      PipeRenderState o = (PipeRenderState)obj;
      return (o.type == this.type && o.size == this.size && o.connectivity == this.connectivity && o.covers == this.covers && o.facing == this.facing);
    }
    
    public String toString() {
      return "PipeState<" + this.type + ", " + this.size + ", " + this.connectivity + ", " + this.covers + ", " + this.facing + '>';
    }
  }
  
  public static final IUnlistedProperty<PipeRenderState> renderStateProperty = (IUnlistedProperty<PipeRenderState>)new UnlistedProperty("renderstate", PipeRenderState.class);
  
  protected byte connectivity = 0;
  
  protected byte covers = 0;
  
  protected volatile PipeRenderState renderState;
  
  protected abstract void updateRenderState();
}
