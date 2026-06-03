package ic2.core.block.heatgenerator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiRTHeatGenerator;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.init.MainConfig;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityRTHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui {
  private boolean newActive;
  
  public final InvSlotConsumable fuelSlot;
  
  public TileEntityRTHeatGenerator() {
    this.fuelSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "fuelSlot", 6, new ItemStack[] { ItemName.nuclear.getItemStack((Enum)NuclearResourceType.rtg_pellet) });
    this.fuelSlot.setStackSizeLimit(1);
    this.newActive = false;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (this.HeatBuffer > 0) {
      this.newActive = true;
    } else {
      this.newActive = false;
    } 
    if (getActive() != this.newActive)
      setActive(this.newActive); 
  }
  
  protected int fillHeatBuffer(int maxAmount) {
    if (maxAmount >= getMaxHeatEmittedPerTick())
      return getMaxHeatEmittedPerTick(); 
    return maxAmount;
  }
  
  public int getMaxHeatEmittedPerTick() {
    int counter = 0;
    for (int i = 0; i < this.fuelSlot.size(); i++) {
      if (!this.fuelSlot.isEmpty(i))
        counter++; 
    } 
    if (counter == 0)
      return 0; 
    return (int)(Math.pow(2.0D, (counter - 1)) * outputMultiplier);
  }
  
  public ContainerBase<TileEntityRTHeatGenerator> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityRTHeatGenerator>)new ContainerRTHeatGenerator(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiRTHeatGenerator(new ContainerRTHeatGenerator(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public static final float outputMultiplier = 2.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/radioisotope");
}
