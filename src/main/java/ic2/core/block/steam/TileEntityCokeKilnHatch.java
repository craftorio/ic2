// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.steam;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityCokeKilnHatch extends TileEntityInventory implements IHasGui
{
    protected final InvSlot inventory;
    
    public TileEntityCokeKilnHatch() {
        this.inventory = new InvSlot(this, "inventory", InvSlot.Access.I, 1, InvSlot.InvSide.ANY);
    }
    
    @Override
    public boolean canInsertItem(final int index, final ItemStack stack, final EnumFacing side) {
        return side == this.getFacing() && super.canInsertItem(index, stack, side);
    }
    
    @Override
    public boolean canExtractItem(final int index, final ItemStack stack, final EnumFacing side) {
        return false;
    }
    
    @Override
    public ContainerBase<TileEntityCokeKilnHatch> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
}
