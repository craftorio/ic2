// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.init.Localization;
import ic2.core.block.BlockIC2Fence;
import net.minecraft.util.ResourceLocation;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerMagnetizer;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiMagnetizer extends GuiIC2<ContainerMagnetizer>
{
    public GuiMagnetizer(final ContainerMagnetizer container) {
        super(container);
        this.addElement(EnergyGauge.asBolt(this, 11, 28, (TileEntityBlock)container.base));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIMagnetizer.png");
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        if (BlockIC2Fence.hasMetalShoes(((ContainerMagnetizer)this.container).player)) {
            this.fontRenderer.drawString(Localization.translate("ic2.Magnetizer.gui.hasMetalShoes"), 18, 66, 4259648);
        }
        else {
            this.fontRenderer.drawString(Localization.translate("ic2.Magnetizer.gui.noMetalShoes"), 18, 66, 16728128);
        }
    }
}
