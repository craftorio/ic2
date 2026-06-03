package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCokeKilnHatch extends TileEntityInventory implements IHasGui {
  protected final InvSlot inventory = new InvSlot((IInventorySlotHolder)this, "inventory", InvSlot.Access.I, 1, InvSlot.InvSide.ANY);
  
  public boolean func_180462_a(int index, ItemStack stack, EnumFacing side) {
    if (side != getFacing())
      return false; 
    return super.func_180462_a(index, stack, side);
  }
  
  public boolean func_180461_b(int index, ItemStack stack, EnumFacing side) {
    return false;
  }
  
  public ContainerBase<TileEntityCokeKilnHatch> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityCokeKilnHatch>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
}
