package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityTank extends TileEntityInventory implements IUpgradableBlock, IHasGui {
  public final InvSlotUpgrade upgradeSlot;
  
  @GuiSynced
  protected final FluidTank fluidTank;
  
  protected final Fluids fluids;
  
  public TileEntityTank() {
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.fluidTank = (FluidTank)this.fluids.addTank("fluid", 24000);
    this.comparator.setUpdate(() -> (this.fluidTank.getFluidAmount() == 0) ? 0 : (int)Util.lerp(1.0F, 15.0F, this.fluidTank.getFluidAmount() / this.fluidTank.getCapacity()));
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.upgradeSlot.tick();
  }
  
  public double getEnergy() {
    return 0.0D;
  }
  
  public boolean useEnergy(double amount) {
    return false;
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
  }
  
  public ContainerBase<TileEntityTank> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityTank>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
}
