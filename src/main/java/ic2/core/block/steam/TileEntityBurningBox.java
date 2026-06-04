package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.item.type.MiscResourceType;
import ic2.core.network.GuiSynced;
import ic2.core.ref.ItemName;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityBurningBox extends TileEntityInventory implements IHasGui, IGuiValueProvider {
  public int ticksSinceLastActiveUpdate = IC2.random.nextInt(128);
  
  public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel((IInventorySlotHolder)this, "fuel", 1, false);
  
  public final InvSlotOutput ashesSlot = new InvSlotOutput((IInventorySlotHolder)this, "ashes", 1);
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.delta = nbt.getInteger("delta");
    this.fuel = nbt.getInteger("fuel");
    this.remainingFuel = nbt.getInteger("remainingFuel");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("delta", this.delta);
    nbt.setInteger("fuel", this.fuel);
    nbt.setInteger("remainingFuel", this.remainingFuel);
    return nbt;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInventoryUpdate = false;
    if (needsFuel())
      needsInventoryUpdate = gainFuel(); 
    boolean newActive = work();
    if (needsInventoryUpdate)
      markDirty(); 
    if (!delayActiveUpdate()) {
      setActive(newActive);
    } else {
      if (this.ticksSinceLastActiveUpdate % 128 == 0) {
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
  
  public int getProvidedHeat(EnumFacing side) {
    return (side == EnumFacing.UP) ? this.heat : 0;
  }
  
  public boolean needsFuel() {
    return (this.fuel <= 0);
  }
  
  public boolean gainFuel() {
    if (this.ashesSlot.canAdd(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.ashes))) {
      int fuelValue = this.fuelSlot.consumeFuel() / 4;
      if (fuelValue == 0)
        return false; 
      this.fuel += fuelValue;
      this.remainingFuel = fuelValue;
      return true;
    } 
    return false;
  }
  
  public boolean work() {
    if (this.fuel > 0) {
      this.fuel--;
      if (this.fuel == 0 && 
        (int)(Math.random() * 2.0D) == 1)
        this.ashesSlot.add(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.ashes)); 
      this.delta = Math.min(++this.delta, 1100);
      int i = 55 - this.delta / 20;
      this.heat = 1400 + (int)(0.008D * -(i * i * i));
      return true;
    } 
    this.delta = Math.max(--this.delta, 0);
    int temp = this.delta / 20;
    this.heat = (int)(0.008D * (temp * temp * temp));
    return false;
  }
  
  public boolean delayActiveUpdate() {
    return false;
  }
  
  public ContainerBase<TileEntityBurningBox> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityBurningBox>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if ("fuel".equals(name))
      return (this.fuel == 0) ? 0.0D : (this.fuel / this.remainingFuel); 
    throw new IllegalArgumentException("Unexpected value requested: " + name);
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
    tooltip.add("");
    tooltip.add("Maximum temperature:");
    tooltip.add(" 1400K");
    tooltip.add("");
    tooltip.add("Time to reach maximum temperature:");
    tooltip.add(" 55 seconds");
    tooltip.add("");
  }
  
  protected int heat = 0;
  
  protected int delta = 0;
  
  public int activityMeter = 0;
  
  @GuiSynced
  public int fuel = 0;
  
  @GuiSynced
  private int remainingFuel = 0;
}
