// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiEnergyOMatClosed extends GuiIC2<ContainerEnergyOMatClosed>
{
    private static final ResourceLocation background;
    
    public GuiEnergyOMatClosed(final ContainerEnergyOMatClosed container) {
        super(container);
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(Localization.translate("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.want"), 12, 21, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.offer"), 12, 39, 4210752);
        this.fontRenderer.drawString(((TileEntityEnergyOMat)((ContainerEnergyOMatClosed)this.container).base).euOffer + " EU", 50, 39, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTraderEnergy.paidFor", ((TileEntityEnergyOMat)((ContainerEnergyOMatClosed)this.container).base).paidFor), 12, 57, 4210752);
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiEnergyOMatClosed.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIEnergyOMatClosed.png");
    }
}
