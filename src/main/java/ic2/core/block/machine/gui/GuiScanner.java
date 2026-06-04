package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerScanner;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IEnableHandler;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScanner extends GuiIC2<ContainerScanner> {
  public GuiScanner(final ContainerScanner container) {
    super((ContainerBase)container);
    this.info = new String[9];
    addElement((GuiElement)EnergyGauge.asBolt(this, 12, 25, (TileEntityBlock)container.base));
    addElement(((CustomButton)(new CustomButton(this, 102, 49, 12, 12, 176, 57, background, createEventSender(0))).withEnableHandler(new IEnableHandler() {
            public boolean isEnabled() {
              return (((TileEntityScanner)container.base).getState() == TileEntityScanner.State.COMPLETED || ((TileEntityScanner)container.base).getState() == TileEntityScanner.State.TRANSFER_ERROR || ((TileEntityScanner)container.base).getState() == TileEntityScanner.State.FAILED);
            }
          })).withTooltip("ic2.Scanner.gui.button.delete"));
    addElement(((CustomButton)(new CustomButton(this, 143, 49, 24, 12, 176, 69, background, createEventSender(1))).withEnableHandler(new IEnableHandler() {
            public boolean isEnabled() {
              return (((TileEntityScanner)container.base).getState() == TileEntityScanner.State.COMPLETED || ((TileEntityScanner)container.base).getState() == TileEntityScanner.State.TRANSFER_ERROR);
            }
          })).withTooltip("ic2.Scanner.gui.button.save"));
    this.info[1] = Localization.translate("ic2.Scanner.gui.info1");
    this.info[2] = Localization.translate("ic2.Scanner.gui.info2");
    this.info[3] = Localization.translate("ic2.Scanner.gui.info3");
    this.info[4] = Localization.translate("ic2.Scanner.gui.info4");
    this.info[5] = Localization.translate("ic2.Scanner.gui.info5");
    this.info[6] = Localization.translate("ic2.Scanner.gui.info6");
    this.info[7] = Localization.translate("ic2.Scanner.gui.info7");
    this.info[8] = Localization.translate("ic2.Scanner.gui.info8");
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.fontRenderer.drawString(this.info[5] + ":", 105, 6, 4210752);
    TileEntityScanner te = (TileEntityScanner)((ContainerScanner)this.container).base;
    switch (te.getState()) {
      case IDLE:
        this.fontRenderer.drawString(Localization.translate("ic2.Scanner.gui.idle"), 10, 69, 15461152);
        break;
      case NO_STORAGE:
        this.fontRenderer.drawString(this.info[2], 10, 69, 15461152);
        break;
      case SCANNING:
        this.fontRenderer.drawString(this.info[1], 10, 69, 2157374);
        this.fontRenderer.drawString(te.getPercentageDone() + "%", 125, 69, 2157374);
        break;
      case NO_ENERGY:
        this.fontRenderer.drawString(this.info[3], 10, 69, 14094352);
        break;
      case ALREADY_RECORDED:
        this.fontRenderer.drawString(this.info[8], 10, 69, 14094352);
        break;
      case FAILED:
        this.fontRenderer.drawString(this.info[4], 10, 69, 2157374);
        this.fontRenderer.drawString(this.info[6], 110, 30, 14094352);
        break;
      case COMPLETED:
      case TRANSFER_ERROR:
        if (te.getState() == TileEntityScanner.State.COMPLETED)
          this.fontRenderer.drawString(this.info[4], 10, 69, 2157374); 
        if (te.getState() == TileEntityScanner.State.TRANSFER_ERROR)
          this.fontRenderer.drawString(this.info[7], 10, 69, 14094352); 
        this.fontRenderer.drawString(Util.toSiString(te.patternUu, 4) + "B UUM", 105, 25, 16777215);
        this.fontRenderer.drawString(Util.toSiString(te.patternEu, 4) + "EU", 105, 36, 16777215);
        break;
    } 
  }
  
  protected void func_146976_a(float partialTicks, int mouseX, int mouseY) {
    super.func_146976_a(partialTicks, mouseX, mouseY);
    bindTexture();
    TileEntityScanner te = (TileEntityScanner)((ContainerScanner)this.container).base;
    int scanningloop = te.getSubPercentageDoneScaled(66);
    if (scanningloop > 0)
      drawTexturedModalRect(this.field_147003_i + 30, this.field_147009_r + 20, 176, 14, scanningloop, 43); 
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIScanner.png");
  
  private final String[] info;
}
