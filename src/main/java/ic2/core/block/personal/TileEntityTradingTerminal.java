package ic2.core.block.personal;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
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
   public final InvSlotUpgrade rangeUpgrade = new InvSlotUpgrade(this, "range", 1);

   public TileEntityTradingTerminal() {
      this.rangeUpgrade.setStackSizeLimit(16);
   }

   @Override
   protected void onLoaded() {
      super.onLoaded();
      this.range = this.rangeUpgrade.getRemoteRange(512);
   }

   @Override
   public void markDirty() {
      super.markDirty();
      if (!this.getWorld().isRemote) {
         this.range = this.rangeUpgrade.getRemoteRange(512);
      }
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      this.rangeUpgrade.tick();
   }

   @Override
   public ContainerBase<?> getGuiContainer(EntityPlayer player) {
      return new ContainerTradingTerminal(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new GuiTradingTerminal(new ContainerTradingTerminal(player, this));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   @Override
   public Set<UpgradableProperty> getUpgradableProperties() {
      return Collections.singleton(UpgradableProperty.RemotelyAccessible);
   }

   @Override
   public double getEnergy() {
      return 0.0;
   }

   @Override
   public boolean useEnergy(double amount) {
      return false;
   }
}
