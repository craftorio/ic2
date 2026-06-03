package ic2.core.block.heatgenerator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.item.type.MiscResourceType;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntitySolidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui, IGuiValueProvider {
  public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel((IInventorySlotHolder)this, "fuel", 1, false);
  
  public final InvSlotOutput outputslot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
  
  public int ticksSinceLastActiveUpdate = IC2.random.nextInt(256);
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (needsFuel())
      needsInvUpdate = gainFuel(); 
    boolean newActive = gainheat();
    if (needsInvUpdate)
      func_70296_d(); 
    if (!delayActiveUpdate()) {
      setActive(newActive);
    } else {
      if (this.ticksSinceLastActiveUpdate % 256 == 0) {
        setActive((this.activityMeter > 0));
        this.activityMeter = 0;
      } 
      if (newActive) {
        this.activityMeter++;
      } else {
        this.activityMeter--;
      } 
      this.ticksSinceLastActiveUpdate++;
    } 
  }
  
  public boolean gainheat() {
    if (isConverting()) {
      this.heatbuffer += getMaxHeatEmittedPerTick();
      this.fuel--;
      if (this.fuel == 0 && 
        (int)(Math.random() * 2.0D) == 1)
        this.outputslot.add(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.ashes)); 
      return true;
    } 
    return false;
  }
  
  public boolean needsFuel() {
    return (this.fuel <= 0 && getHeatBuffer() == 0);
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.fuel = nbt.func_74762_e("fuel");
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74768_a("fuel", this.fuel);
    return nbt;
  }
  
  public boolean delayActiveUpdate() {
    return false;
  }
  
  public boolean gainFuel() {
    if (this.outputslot.canAdd(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.ashes))) {
      int fuelValue = this.fuelSlot.consumeFuel() / 4;
      if (fuelValue == 0)
        return false; 
      this.fuel += fuelValue;
      this.itemFuelTime = fuelValue;
      return true;
    } 
    return false;
  }
  
  public boolean isConverting() {
    return (this.fuel > 0);
  }
  
  protected int fillHeatBuffer(int maxAmount) {
    if (this.heatbuffer - maxAmount >= 0) {
      this.heatbuffer -= maxAmount;
      return maxAmount;
    } 
    maxAmount = this.heatbuffer;
    this.heatbuffer = 0;
    return maxAmount;
  }
  
  public int getMaxHeatEmittedPerTick() {
    return emittedHU;
  }
  
  public ContainerBase<TileEntitySolidHeatGenerator> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntitySolidHeatGenerator>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if ("fuel".equals(name))
      return (this.fuel == 0) ? 0.0D : (this.fuel / this.itemFuelTime); 
    throw new IllegalArgumentException("Unexpected value requested: " + name);
  }
  
  private int heatbuffer = 0;
  
  public int activityMeter = 0;
  
  @GuiSynced
  public int fuel = 0;
  
  @GuiSynced
  public int itemFuelTime = 0;
  
  public static final int emittedHU = Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/solid"));
}
