// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import java.io.IOException;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.init.Localization;
import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.GuiIC2;

public class GuiFluidDistributor extends GuiIC2<ContainerFluidDistributor>
{
    public GuiFluidDistributor(final ContainerFluidDistributor container) {
        super(container, 184);
        this.addElement(TankGauge.createPlain(this, 29, 38, 55, 47, (IFluidTank)((TileEntityFluidDistributor)container.base).fluidTank));
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.info"), 112, 47, 5752026);
        if (((TileEntityFluidDistributor)((ContainerFluidDistributor)this.container).base).getActive()) {
            this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.concentrate"), 95, 71, 5752026);
        }
        else {
            this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.distribute"), 95, 71, 5752026);
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, final int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        mouseX -= this.guiLeft;
        mouseY -= this.guiTop;
        if (mouseX >= 117 && mouseY >= 58 && mouseX <= 135 && mouseY <= 66) {
            IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)((ContainerFluidDistributor)this.container).base, 1);
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIFluidDistributor.png");
    }
}
