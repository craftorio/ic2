package ic2.core.block.comp;

import ic2.api.event.RetextureEvent;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.state.BlockStateUtil;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.ref.BlockName;
import ic2.core.util.Util;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Obscuration extends TileEntityComponent {
  private final Runnable changeHandler;
  
  private ObscurationData[] dataMap;
  
  public Obscuration(TileEntityBlock parent, Runnable changeHandler) {
    super(parent);
    this.changeHandler = changeHandler;
  }
  
  public void readFromNbt(NBTTagCompound nbt) {
    if (nbt.func_82582_d())
      return; 
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      if (nbt.func_150297_b(facing.func_176610_l(), 10)) {
        NBTTagCompound cNbt = nbt.getCompoundTag(facing.func_176610_l());
        Block block = Util.getBlock(cNbt.func_74779_i("block"));
        if (block != null) {
          String variant = cNbt.func_74779_i("variant");
          IBlockState state = BlockStateUtil.getState(block, variant);
          if (state != null) {
            int rawSide = cNbt.func_74771_c("side");
            if (rawSide >= 0 && rawSide < EnumFacing.field_82609_l.length) {
              EnumFacing side = EnumFacing.field_82609_l[rawSide];
              int[] colorMultipliers = ItemObscurator.internColorMultipliers(cNbt.func_74759_k("colorMuls"));
              ObscurationData data = new ObscurationData(state, variant, side, colorMultipliers);
              if (this.dataMap == null)
                this.dataMap = new ObscurationData[EnumFacing.field_82609_l.length]; 
              this.dataMap[facing.ordinal()] = data.intern();
            } 
          } 
        } 
      } 
    } 
  }
  
  public NBTTagCompound writeToNbt() {
    if (this.dataMap == null)
      return null; 
    NBTTagCompound ret = new NBTTagCompound();
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      ObscurationData data = this.dataMap[facing.ordinal()];
      if (data != null) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.func_74778_a("block", Util.getName(data.state.func_177230_c()).toString());
        nbt.func_74778_a("variant", data.variant);
        nbt.func_74774_a("side", (byte)data.side.ordinal());
        nbt.func_74783_a("colorMuls", data.colorMultipliers);
        ret.setTag(facing.func_176610_l(), (NBTBase)nbt);
      } 
    } 
    return ret;
  }
  
  public boolean applyObscuration(EnumFacing side, ObscurationData data) {
    if (this.dataMap != null && data.equals(this.dataMap[side.ordinal()]))
      return false; 
    if (this.dataMap == null)
      this.dataMap = new ObscurationData[EnumFacing.field_82609_l.length]; 
    this.dataMap[side.ordinal()] = data.intern();
    this.changeHandler.run();
    return true;
  }
  
  public void clear() {
    this.dataMap = null;
    this.changeHandler.run();
  }
  
  public boolean hasObscuration() {
    return (this.dataMap != null);
  }
  
  public ObscurationData[] getRenderState() {
    if (this.dataMap == null)
      return null; 
    return Arrays.<ObscurationData>copyOf(this.dataMap, this.dataMap.length);
  }
  
  public static class ObscurationComponentEventHandler {
    public static void init() {
      new ObscurationComponentEventHandler();
    }
    
    private ObscurationComponentEventHandler() {
      MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onObscuration(RetextureEvent event) {
      if (event.state.func_177230_c() != BlockName.te.getInstance())
        return; 
      TileEntity teRaw = event.getWorld().func_175625_s(event.pos);
      if (!(teRaw instanceof TileEntityBlock))
        return; 
      Obscuration obscuration = (Obscuration)((TileEntityBlock)teRaw).getComponent(Obscuration.class);
      if (obscuration == null)
        return; 
      Obscuration.ObscurationData data = new Obscuration.ObscurationData(event.refState, event.refVariant, event.refSide, event.refColorMultipliers);
      if (obscuration.applyObscuration(event.side, data)) {
        event.applied = true;
        event.setCanceled(true);
      } 
    }
  }
  
  public static class ObscurationData {
    public final IBlockState state;
    
    public final String variant;
    
    public final EnumFacing side;
    
    public final int[] colorMultipliers;
    
    public ObscurationData(IBlockState state, String variant, EnumFacing side, int[] colorMultipliers) {
      this.state = state;
      this.variant = variant;
      this.side = side;
      this.colorMultipliers = colorMultipliers;
    }
    
    public boolean equals(Object obj) {
      if (obj == this)
        return true; 
      if (!(obj instanceof ObscurationData))
        return false; 
      ObscurationData o = (ObscurationData)obj;
      return (o.state.equals(this.state) && o.variant
        .equals(this.variant) && o.side == this.side && 
        
        Arrays.equals(o.colorMultipliers, this.colorMultipliers));
    }
    
    public int hashCode() {
      return (this.state.hashCode() * 7 + this.side.ordinal()) * 23;
    }
    
    public ObscurationData intern() {
      return this;
    }
  }
}
