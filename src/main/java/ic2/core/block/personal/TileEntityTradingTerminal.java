package ic2.core.block.personal;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotUpgrade;
import java.util.Collections;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTradingTerminal extends TileEntityInventory implements IHasGui, IUpgradableBlock {
  protected int range;
  
  public final InvSlotUpgrade rangeUpgrade;
  
  public TileEntityTradingTerminal() {
    this.rangeUpgrade = new InvSlotUpgrade((IInventorySlotHolder)this, "range", 1);
    this.rangeUpgrade.setStackSizeLimit(16);
  }
  
  protected void onLoaded() {
    super.onLoaded();
    this.range = this.rangeUpgrade.getRemoteRange(512);
  }
  
  public void func_70296_d() {
    super.func_70296_d();
    if (!(getWorld()).isRemote)
      this.range = this.rangeUpgrade.getRemoteRange(512); 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.rangeUpgrade.tick();
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)new ContainerTradingTerminal(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiTradingTerminal(new ContainerTradingTerminal(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return Collections.singleton(UpgradableProperty.RemotelyAccessible);
  }
  
  public double getEnergy() {
    return 0.0D;
  }
  
  public boolean useEnergy(double amount) {
    return false;
  }
}
