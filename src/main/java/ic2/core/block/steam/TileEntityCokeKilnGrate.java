package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import java.util.Collections;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCokeKilnGrate extends TileEntityInventory implements IHasGui {
   protected final Fluids fluids = this.addComponent(new Fluids(this));
   @GuiSynced
   protected final Fluids.InternalFluidTank fluidTank = this.fluids.addTank("fluidTank", 64000, InvSlot.Access.O, InvSlot.InvSide.ANY);

   @Override
   protected void setFacing(EnumFacing facing) {
      super.setFacing(facing);
      this.fluids.changeConnectivity(this.fluidTank, Collections.emptyList(), Collections.singleton(this.getFacing()));
   }

   @Override
   public ContainerBase<TileEntityCokeKilnGrate> getGuiContainer(EntityPlayer player) {
      return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return DynamicGui.<TileEntityCokeKilnGrate>create(this, player, GuiParser.parse(this.teBlock));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }
}
