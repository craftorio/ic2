package ic2.core.block;

import ic2.core.IC2;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.UnlistedProperty;
import ic2.core.network.NetworkManager;
import ic2.core.ref.BlockName;
import ic2.core.util.Ic2Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.property.IUnlistedProperty;

public class TileEntityWall extends TileEntityBlock {
  public TileEntityWall() {
    this(BlockWall.defaultColor);
  }
  
  public TileEntityWall(Ic2Color color) {
    this.color = BlockWall.defaultColor;
    this.obscuration = addComponent(new Obscuration(this, new Runnable() {
            public void run() {
              ((NetworkManager)IC2.network.get(true)).updateTileEntityField(TileEntityWall.this, "obscuration");
            }
          }));
    this.color = color;
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.color = Ic2Color.values[nbt.func_74771_c("color") & 0xFF];
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74774_a("color", (byte)this.color.ordinal());
    return nbt;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if ((func_145831_w()).field_72995_K)
      updateRenderState(); 
  }
  
  protected Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state) {
    state = super.getExtendedState(state);
    WallRenderState value = this.renderState;
    if (value != null)
      state = state.withProperties(new Object[] { renderStateProperty, value }); 
    return state;
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = new ArrayList<>();
    ret.add("color");
    ret.add("obscuration");
    ret.addAll(super.getNetworkedFields());
    return ret;
  }
  
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (updateRenderState())
      rerender(); 
  }
  
  protected boolean recolor(EnumFacing side, EnumDyeColor mcColor) {
    Ic2Color color = Ic2Color.get(mcColor);
    if (color == this.color)
      return false; 
    this.color = color;
    if (!(func_145831_w()).field_72995_K) {
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField(this, "obscuration");
      func_70296_d();
    } else if (updateRenderState()) {
      rerender();
    } 
    return true;
  }
  
  protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target) {
    return BlockName.wall.getItemStack((Enum)this.color);
  }
  
  protected boolean clientNeedsExtraModelInfo() {
    return this.obscuration.hasObscuration();
  }
  
  private boolean updateRenderState() {
    WallRenderState state = new WallRenderState(this.color, this.obscuration.getRenderState());
    if (state.equals(this.renderState))
      return false; 
    this.renderState = state;
    return true;
  }
  
  public static class WallRenderState {
    public final Ic2Color color;
    
    public final Obscuration.ObscurationData[] obscurations;
    
    public WallRenderState(Ic2Color color, Obscuration.ObscurationData[] obscurations) {
      this.color = color;
      this.obscurations = obscurations;
    }
    
    public boolean equals(Object obj) {
      if (obj == this)
        return true; 
      if (!(obj instanceof WallRenderState))
        return false; 
      WallRenderState o = (WallRenderState)obj;
      return (o.color == this.color && Arrays.equals((Object[])o.obscurations, (Object[])this.obscurations));
    }
    
    public int hashCode() {
      return this.color.hashCode() * 31 + Arrays.hashCode((Object[])this.obscurations);
    }
    
    public String toString() {
      return "WallState<" + this.color + ", " + Arrays.toString((Object[])this.obscurations) + '>';
    }
  }
  
  public static final IUnlistedProperty<WallRenderState> renderStateProperty = (IUnlistedProperty<WallRenderState>)new UnlistedProperty("renderstate", WallRenderState.class);
  
  protected final Obscuration obscuration;
  
  private Ic2Color color;
  
  private volatile WallRenderState renderState;
}
