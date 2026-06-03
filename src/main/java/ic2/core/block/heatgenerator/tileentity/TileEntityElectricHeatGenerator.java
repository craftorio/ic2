package ic2.core.block.heatgenerator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiElectricHeatGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityElectricHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui {
  private boolean newActive;
  
  public final InvSlotDischarge dischargeSlot;
  
  public final InvSlotConsumable coilSlot;
  
  protected final Energy energy;
  
  public TileEntityElectricHeatGenerator() {
    this.coilSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "CoilSlot", 10, new ItemStack[] { ItemName.crafting.getItemStack((Enum)CraftingItemType.coil) });
    this.coilSlot.setStackSizeLimit(1);
    this.dischargeSlot = new InvSlotDischarge((IInventorySlotHolder)this, InvSlot.Access.NONE, 4);
    this.energy = (Energy)addComponent((TileEntityComponent)Energy.asBasicSink((TileEntityBlock)this, 10000.0D, 4).addManagedSlot((InvSlot)this.dischargeSlot));
    this.newActive = false;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (getActive() != this.newActive)
      setActive(this.newActive); 
  }
  
  public ContainerBase<TileEntityElectricHeatGenerator> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityElectricHeatGenerator>)new ContainerElectricHeatGenerator(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiElectricHeatGenerator(new ContainerElectricHeatGenerator(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  protected int fillHeatBuffer(int maxAmount) {
    int amount = Math.min(maxAmount, (int)(this.energy.getEnergy() / outputMultiplier));
    if (amount > 0) {
      this.energy.useEnergy(amount / outputMultiplier);
      this.newActive = true;
    } else {
      this.newActive = false;
    } 
    return amount;
  }
  
  public int getMaxHeatEmittedPerTick() {
    int counter = 0;
    for (int i = 0; i < this.coilSlot.size(); i++) {
      if (!this.coilSlot.isEmpty(i))
        counter++; 
    } 
    return counter * 10;
  }
  
  public final float getChargeLevel() {
    return (float)Math.min(1.0D, this.energy.getFillRatio());
  }
  
  public static final double outputMultiplier = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/electric");
}
