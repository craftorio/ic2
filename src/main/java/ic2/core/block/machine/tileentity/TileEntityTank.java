package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
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
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityTank extends TileEntityInventory implements IUpgradableBlock, IHasGui {
   public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
   @GuiSynced
   protected final FluidTank fluidTank;
   protected final Fluids fluids = this.addComponent(new Fluids(this));

   public TileEntityTank() {
      this.fluidTank = this.fluids.addTank("fluid", 24000);
      this.comparator
         .setUpdate(
            () -> this.fluidTank.getFluidAmount() == 0 ? 0 : (int)Util.lerp(1.0F, 15.0F, (float)this.fluidTank.getFluidAmount() / this.fluidTank.getCapacity())
         );
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      this.upgradeSlot.tick();
   }

   @Override
   public double getEnergy() {
      return 0.0;
   }

   @Override
   public boolean useEnergy(double amount) {
      return false;
   }

   @Override
   public Set<UpgradableProperty> getUpgradableProperties() {
      return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
   }

   @Override
   public ContainerBase<TileEntityTank> getGuiContainer(EntityPlayer player) {
      return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return DynamicGui.<TileEntityTank>create(this, player, GuiParser.parse(this.teBlock));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }
}
