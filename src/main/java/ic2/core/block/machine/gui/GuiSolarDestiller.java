// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiSolarDestiller extends GuiIC2<ContainerSolarDestiller>
{
    public GuiSolarDestiller(final ContainerSolarDestiller container) {
        super(container, 184);
        this.addElement(TankGauge.createPlain(this, 37, 43, 53, 18, (IFluidTank)((TileEntitySolarDestiller)container.base).inputTank));
        this.addElement(TankGauge.createPlain(this, 115, 55, 17, 43, (IFluidTank)((TileEntitySolarDestiller)container.base).outputTank));
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(final float f, final int x, final int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        this.bindTexture();
        if (((TileEntitySolarDestiller)((ContainerSolarDestiller)this.container).base).canWork()) {
            this.drawTexturedModalRect(this.guiLeft + 36, this.guiTop + 26, 0, 184, 97, 29);
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUISolarDestiller.png");
    }
}
