package ic2.core.block.personal;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTradeOMatClosed extends GuiIC2<ContainerTradeOMatClosed> {
  public GuiTradeOMatClosed(ContainerTradeOMatClosed container) {
    super((ContainerBase)container);
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.fontRenderer.drawString(Localization.translate("container.inventory"), 8, this.field_147000_g - 96 + 2, 4210752);
    this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.want"), 12, 23, 4210752);
    this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.offer"), 12, 42, 4210752);
    this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.stock"), 12, 60, 4210752);
    this.fontRenderer.drawString((((TileEntityTradeOMat)((ContainerTradeOMatClosed)this.container).base).stock < 0) ? "∞" : ("" + ((TileEntityTradeOMat)((ContainerTradeOMatClosed)this.container).base).stock), 50, 60, (((TileEntityTradeOMat)((ContainerTradeOMatClosed)this.container).base).stock != 0) ? 4210752 : 16733525);
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUITradeOMatClosed.png");
}
