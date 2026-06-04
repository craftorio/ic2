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
import java.util.Collection;
import java.util.Collections;
import net.minecraft.util.EnumFacing;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityBlock;
import ic2.core.network.GuiSynced;
import ic2.core.block.comp.Fluids;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityCokeKilnGrate extends TileEntityInventory implements IHasGui
{
    protected final Fluids fluids;
    @GuiSynced
    protected final Fluids.InternalFluidTank fluidTank;
    
    public TileEntityCokeKilnGrate() {
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluidTank", 64000, InvSlot.Access.O, InvSlot.InvSide.ANY);
    }
    
    @Override
    protected void setFacing(final EnumFacing facing) {
        super.setFacing(facing);
        this.fluids.changeConnectivity(this.fluidTank, (Collection<EnumFacing>)Collections.emptyList(), Collections.singleton(this.getFacing()));
    }
    
    @Override
    public ContainerBase<TileEntityCokeKilnGrate> getGuiContainer(final EntityPlayer player) {
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
