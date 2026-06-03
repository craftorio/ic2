package ic2.core.block.generator.tileentity;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.ConfigUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityGenerator extends TileEntityBaseGenerator implements IGuiValueProvider {
  public final InvSlotConsumableFuel fuelSlot;
  
  @GuiSynced
  public int totalFuel;
  
  public TileEntityGenerator() {
    super(Math.round(10.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/generator")), 1, 4000);
    this.totalFuel = 0;
    this.fuelSlot = new InvSlotConsumableFuel((IInventorySlotHolder)this, "fuel", 1, false);
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (getActive())
      TileEntityIronFurnace.showFlames(func_145831_w(), this.field_174879_c, getFacing()); 
  }
  
  public double getFuelRatio() {
    if (this.fuel <= 0)
      return 0.0D; 
    return this.fuel / this.totalFuel;
  }
  
  public boolean gainFuel() {
    int fuelValue = this.fuelSlot.consumeFuel() / 4;
    if (fuelValue == 0)
      return false; 
    this.fuel += fuelValue;
    this.totalFuel = fuelValue;
    return true;
  }
  
  public boolean isConverting() {
    return (this.fuel > 0);
  }
  
  public String getOperationSoundFile() {
    return "Generators/GeneratorLoop.ogg";
  }
  
  public double getGuiValue(String name) {
    if ("fuel".equals(name))
      return getFuelRatio(); 
    throw new IllegalArgumentException();
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.totalFuel = nbt.func_74762_e("totalFuel");
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74768_a("totalFuel", this.totalFuel);
    return nbt;
  }
}
