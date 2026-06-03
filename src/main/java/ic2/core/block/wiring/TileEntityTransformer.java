package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.init.Localization;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityTransformer extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener {
  public TileEntityTransformer(int tier) {
    this.inputFlow = 0.0D;
    this.outputFlow = 0.0D;
    this.configuredMode = defaultMode;
    this.transformMode = null;
    this.defaultTier = tier;
    this.energy = (Energy)addComponent((TileEntityComponent)(new Energy((TileEntityBlock)this, EnergyNet.instance.getPowerFromTier(tier) * 8.0D, Collections.emptySet(), Collections.emptySet(), tier, tier, true)).setMultiSource(true));
  }
  
  public String getType() {
    switch (this.energy.getSourceTier()) {
      case 1:
        return "LV";
      case 2:
        return "MV";
      case 3:
        return "HV";
      case 4:
        return "EV";
    } 
    return "";
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    int mode = nbt.func_74762_e("mode");
    if (mode >= 0 && mode < Mode.VALUES.length) {
      this.configuredMode = Mode.VALUES[mode];
    } else {
      this.configuredMode = defaultMode;
    } 
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74768_a("mode", this.configuredMode.ordinal());
    return nbt;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(func_145831_w()).field_72995_K)
      updateRedstone(true); 
  }
  
  public Mode getMode() {
    return this.configuredMode;
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    if (event >= 0 && event < Mode.VALUES.length) {
      this.configuredMode = Mode.VALUES[event];
      updateRedstone(false);
    } else if (event == 3) {
    
    } 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    updateRedstone(false);
  }
  
  private void updateRedstone(boolean force) {
    Mode newMode;
    assert !(func_145831_w()).field_72995_K;
    switch (this.configuredMode) {
      case redstone:
        newMode = func_145831_w().func_175640_z(this.field_174879_c) ? Mode.stepup : Mode.stepdown;
        break;
      case stepdown:
      case stepup:
        newMode = this.configuredMode;
        break;
      default:
        throw new RuntimeException("invalid mode: " + this.configuredMode);
    } 
    if (newMode != Mode.stepup && newMode != Mode.stepdown)
      throw new RuntimeException("invalid mode: " + newMode); 
    this.energy.setEnabled(true);
    if (force || this.transformMode != newMode) {
      this.transformMode = newMode;
      setActive(isStepUp());
      if (isStepUp()) {
        this.energy.setSourceTier(this.defaultTier + 1);
        this.energy.setSinkTier(this.defaultTier);
        this.energy.setPacketOutput(1);
        this.energy.setDirections(EnumSet.complementOf((EnumSet)EnumSet.of(getFacing())), EnumSet.of(getFacing()));
      } else {
        this.energy.setSourceTier(this.defaultTier);
        this.energy.setSinkTier(this.defaultTier + 1);
        this.energy.setPacketOutput(4);
        this.energy.setDirections(EnumSet.of(getFacing()), EnumSet.complementOf((EnumSet)EnumSet.of(getFacing())));
      } 
      this.outputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier());
      this.inputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier());
    } 
  }
  
  public void setFacing(EnumFacing facing) {
    super.setFacing(facing);
    if (!(func_145831_w()).field_72995_K)
      updateRedstone(true); 
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, tooltip, advanced);
    tooltip.add(String.format("%s %.0f %s %s %.0f %s", new Object[] { Localization.translate("ic2.item.tooltip.Low"), Double.valueOf(EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier())), Localization.translate("ic2.generic.text.EUt"), Localization.translate("ic2.item.tooltip.High"), Double.valueOf(EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier() + 1)), Localization.translate("ic2.generic.text.EUt") }));
  }
  
  public ContainerBase<TileEntityTransformer> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityTransformer>)new ContainerTransformer(player, this, 219);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiTransformer(new ContainerTransformer(player, this, 219));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getinputflow() {
    if (!isStepUp())
      return this.inputFlow; 
    return this.outputFlow;
  }
  
  public double getoutputflow() {
    if (isStepUp())
      return this.inputFlow; 
    return this.outputFlow;
  }
  
  private boolean isStepUp() {
    return (this.transformMode == Mode.stepup);
  }
  
  private static final Mode defaultMode = Mode.redstone;
  
  private double inputFlow;
  
  private double outputFlow;
  
  private final int defaultTier;
  
  protected final Energy energy;
  
  private Mode configuredMode;
  
  private Mode transformMode;
  
  public enum Mode {
    redstone, stepdown, stepup;
    
    static final Mode[] VALUES = values();
    
    static {
    
    }
  }
}
