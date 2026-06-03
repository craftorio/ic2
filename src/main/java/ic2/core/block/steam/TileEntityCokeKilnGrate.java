package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import java.util.Collections;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCokeKilnGrate extends TileEntityInventory implements IHasGui {
  protected final Fluids fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
  
  @GuiSynced
  protected final Fluids.InternalFluidTank fluidTank = this.fluids.addTank("fluidTank", 64000, InvSlot.Access.O, InvSlot.InvSide.ANY);
  
  protected void setFacing(EnumFacing facing) {
    super.setFacing(facing);
    this.fluids.changeConnectivity(this.fluidTank, Collections.emptyList(), Collections.singleton(getFacing()));
  }
  
  public ContainerBase<TileEntityCokeKilnGrate> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityCokeKilnGrate>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
}
