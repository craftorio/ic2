package ic2.core.block.personal;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnergyOMatClosed extends GuiIC2<ContainerEnergyOMatClosed> {
  public GuiEnergyOMatClosed(ContainerEnergyOMatClosed container) {
    super((ContainerBase)container);
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.fontRenderer.drawString(Localization.translate("container.inventory"), 8, this.field_147000_g - 96 + 2, 4210752);
    this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.want"), 12, 21, 4210752);
    this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.offer"), 12, 39, 4210752);
    this.fontRenderer.drawString(((TileEntityEnergyOMat)((ContainerEnergyOMatClosed)this.container).base).euOffer + " EU", 50, 39, 4210752);
    this.fontRenderer.drawString(Localization.translate("ic2.container.personalTraderEnergy.paidFor", new Object[] { Integer.valueOf(((TileEntityEnergyOMat)((ContainerEnergyOMatClosed)this.container).base).paidFor) }), 12, 57, 4210752);
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIEnergyOMatClosed.png");
}
