// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import java.io.IOException;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.init.Localization;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntityFluidRegulator;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.machine.container.ContainerFluidRegulator;
import ic2.core.GuiIC2;

public class GuiFluidRegulator extends GuiIC2<ContainerFluidRegulator>
{
    private static final ResourceLocation background;
    
    public GuiFluidRegulator(final ContainerFluidRegulator container) {
        super(container, 184);
        this.addElement(EnergyGauge.asBolt(this, 12, 39, (TileEntityBlock)container.base));
        this.addElement(TankGauge.createNormal(this, 78, 34, (IFluidTank)((TileEntityFluidRegulator)container.base).getFluidTank()));
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(((TileEntityFluidRegulator)((ContainerFluidRegulator)this.container).base).getoutputmb() + Localization.translate("ic2.generic.text.mb"), 105, 57, 2157374);
        this.fontRenderer.drawString(((TileEntityFluidRegulator)((ContainerFluidRegulator)this.container).base).getmodegui(), 145, 57, 2157374);
    }
    
    @Override
    protected void mouseClicked(final int i, final int j, final int k) throws IOException {
        super.mouseClicked(i, j, k);
        final int xMin = (this.width - this.xSize) / 2;
        final int yMin = (this.height - this.ySize) / 2;
        final int x = i - xMin;
        final int y = j - yMin;
        final TileEntityFluidRegulator te = (TileEntityFluidRegulator)((ContainerFluidRegulator)this.container).base;
        if (x >= 102 && y >= 68 && x <= 110 && y <= 76) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, -1000);
        }
        if (x >= 112 && y >= 68 && x <= 120 && y <= 76) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, -100);
        }
        if (x >= 122 && y >= 68 && x <= 130 && y <= 76) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, -10);
        }
        if (x >= 132 && y >= 68 && x <= 140 && y <= 76) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, -1);
        }
        if (x >= 132 && y >= 44 && x <= 140 && y <= 52) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, 1);
        }
        if (x >= 122 && y >= 44 && x <= 130 && y <= 52) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, 10);
        }
        if (x >= 112 && y >= 44 && x <= 120 && y <= 52) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, 100);
        }
        if (x >= 102 && y >= 44 && x <= 110 && y <= 52) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, 1000);
        }
        if (x >= 151 && y >= 44 && x <= 161 && y <= 52) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, 1001);
        }
        if (x >= 151 && y >= 68 && x <= 161 && y <= 76) {
            IC2.network.get(false).initiateClientTileEntityEvent(te, 1002);
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiFluidRegulator.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIFluidRegulator.png");
    }
}
