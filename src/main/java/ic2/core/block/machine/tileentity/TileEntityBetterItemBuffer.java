package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.profile.NotClassic;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityBetterItemBuffer extends TileEntityInventory implements IHasGui, IUpgradableBlock {
  public final InvSlot bufferSlot = new InvSlot((IInventorySlotHolder)this, "buffer", InvSlot.Access.IO, 9, InvSlot.InvSide.ANY);
  
  public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.upgradeSlot.tick();
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.ItemProducing, UpgradableProperty.ItemConsuming);
  }
  
  public double getEnergy() {
    return 0.0D;
  }
  
  public boolean useEnergy(double amount) {
    return true;
  }
  
  public ContainerBase<TileEntityBetterItemBuffer> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityBetterItemBuffer>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
}
