// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntityFluidBottler;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiFluidBottler extends GuiIC2<ContainerFluidBottler>
{
    public GuiFluidBottler(final ContainerFluidBottler container) {
        super(container, 184);
        this.addElement(EnergyGauge.asBolt(this, 12, 35, (TileEntityBlock)container.base));
        this.addElement(TankGauge.createNormal(this, 78, 34, (IFluidTank)((TileEntityFluidBottler)container.base).fluidTank));
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(final float f, final int x, final int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        this.bindTexture();
        final int progressSize = Math.round(((TileEntityFluidBottler)((ContainerFluidBottler)this.container).base).getProgress() * 16.0f);
        if (progressSize > 0) {
            this.drawTexturedModalRect(this.guiLeft + 61, this.guiTop + 36, 198, 0, progressSize, 13);
            this.drawTexturedModalRect(this.guiLeft + 61, this.guiTop + 73, 198, 0, progressSize, 13);
            this.drawTexturedModalRect(this.guiLeft + 99, this.guiTop + 55, 198, 0, progressSize, 13);
        }
    }
    
    public ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIBottler.png");
    }
}
