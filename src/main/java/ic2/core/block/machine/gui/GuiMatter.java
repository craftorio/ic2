// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import ic2.core.init.Localization;
import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiMatter extends GuiIC2<ContainerMatter>
{
    public String progressLabel;
    public String amplifierLabel;
    
    public GuiMatter(final ContainerMatter container) {
        super(container);
        this.addElement(TankGauge.createNormal(this, 96, 22, (IFluidTank)((TileEntityMatter)container.base).fluidTank));
        this.progressLabel = Localization.translate("ic2.Matter.gui.info.progress");
        this.amplifierLabel = Localization.translate("ic2.Matter.gui.info.amplifier");
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(this.progressLabel, 8, 22, 4210752);
        this.fontRenderer.drawString(((TileEntityMatter)((ContainerMatter)this.container).base).getProgressAsString(), 18, 31, 4210752);
        if (((TileEntityMatter)((ContainerMatter)this.container).base).scrap > 0) {
            this.fontRenderer.drawString(this.amplifierLabel, 8, 46, 4210752);
            this.fontRenderer.drawString("" + ((TileEntityMatter)((ContainerMatter)this.container).base).scrap, 8, 58, 4210752);
        }
    }
    
    public ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIMatter.png");
    }
}
