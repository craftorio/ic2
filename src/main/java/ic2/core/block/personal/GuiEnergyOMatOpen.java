package ic2.core.block.personal;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.gui.GuiElement;
import ic2.core.gui.VanillaButton;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;

public class GuiEnergyOMatOpen extends GuiIC2<ContainerEnergyOMatOpen> {
  public GuiEnergyOMatOpen(ContainerEnergyOMatOpen container) {
    super((ContainerBase)container);
    addElement((GuiElement)(new VanillaButton(this, 102, 16, 32, 10, createEventSender(0)))
        .withText("-100k"));
    addElement((GuiElement)(new VanillaButton(this, 102, 26, 32, 10, createEventSender(1)))
        .withText("-10k"));
    addElement((GuiElement)(new VanillaButton(this, 102, 36, 32, 10, createEventSender(2)))
        .withText("-1k"));
    addElement((GuiElement)(new VanillaButton(this, 102, 46, 32, 10, createEventSender(3)))
        .withText("-100"));
    addElement((GuiElement)(new VanillaButton(this, 134, 16, 32, 10, createEventSender(4)))
        .withText("+100k"));
    addElement((GuiElement)(new VanillaButton(this, 134, 26, 32, 10, createEventSender(5)))
        .withText("+10k"));
    addElement((GuiElement)(new VanillaButton(this, 134, 36, 32, 10, createEventSender(6)))
        .withText("+1k"));
    addElement((GuiElement)(new VanillaButton(this, 134, 46, 32, 10, createEventSender(7)))
        .withText("+100"));
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.fontRenderer.drawString(Localization.translate("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.offer"), 100, 60, 4210752);
    this.fontRenderer.drawString(((TileEntityEnergyOMat)((ContainerEnergyOMatOpen)this.container).base).euOffer + " EU", 100, 68, 4210752);
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIEnergyOMatOpen.png");
}
