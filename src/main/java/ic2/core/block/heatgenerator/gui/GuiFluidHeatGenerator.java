// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.heatgenerator.gui;

import ic2.core.init.Localization;
import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import ic2.core.GuiIC2;

public class GuiFluidHeatGenerator extends GuiIC2<ContainerFluidHeatGenerator>
{
    private static final ResourceLocation background;
    
    public GuiFluidHeatGenerator(final ContainerFluidHeatGenerator container) {
        super(container);
        this.addElement(TankGauge.createNormal(this, 70, 20, (IFluidTank)((TileEntityFluidHeatGenerator)container.base).getFluidTank()));
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(Localization.translate("ic2.FluidHeatGenerator.gui.info.Emit") + ((TileEntityFluidHeatGenerator)((ContainerFluidHeatGenerator)this.container).base).gettransmitHeat(), 96, 33, 5752026);
        this.fontRenderer.drawString(Localization.translate("ic2.FluidHeatGenerator.gui.info.MaxEmit") + ((TileEntityFluidHeatGenerator)((ContainerFluidHeatGenerator)this.container).base).getMaxHeatEmittedPerTick(), 96, 52, 5752026);
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiFluidHeatGenerator.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIFluidHeatGenerator.png");
    }
}
