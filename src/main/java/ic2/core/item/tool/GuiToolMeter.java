package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.gui.CustomButton;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.MouseButton;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import java.io.IOException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiToolMeter extends GuiIC2<ContainerMeter> {
  public GuiToolMeter(ContainerMeter container) {
    super((ContainerBase)container, 217);
    addElement((new CustomButton(this, 112, 55, 20, 20, createModeSetter(ContainerMeter.Mode.EnergyIn)))
        .withTooltip("ic2.itemToolMEter.mode.switch\nic2.itemToolMEter.mode.EnergyIn"));
    addElement((new CustomButton(this, 132, 55, 20, 20, createModeSetter(ContainerMeter.Mode.EnergyOut)))
        .withTooltip("ic2.itemToolMEter.mode.switch\nic2.itemToolMEter.mode.EnergyOut"));
    addElement((new CustomButton(this, 112, 75, 20, 20, createModeSetter(ContainerMeter.Mode.EnergyGain)))
        .withTooltip("ic2.itemToolMEter.mode.switch\nic2.itemToolMEter.mode.EnergyGain"));
    addElement((new CustomButton(this, 132, 75, 20, 20, createModeSetter(ContainerMeter.Mode.Voltage)))
        .withTooltip("ic2.itemToolMEter.mode.switch\nic2.itemToolMEter.mode.Voltage"));
  }
  
  private IClickHandler createModeSetter(final ContainerMeter.Mode mode) {
    return new IClickHandler() {
        public void onClick(MouseButton button) {
          ((ContainerMeter)GuiToolMeter.this.container).setMode(mode);
        }
      };
  }
  
  protected void func_73864_a(int i, int j, int k) throws IOException {
    super.func_73864_a(i, j, k);
    int xMin = (this.field_146294_l - this.field_146999_f) / 2;
    int yMin = (this.field_146295_m - this.field_147000_g) / 2;
    int x = i - xMin;
    int y = j - yMin;
    if (x >= 26 && y >= 111 && x <= 83 && y <= 123)
      ((ContainerMeter)this.container).reset(); 
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    String unit = (((ContainerMeter)this.container).getMode() == ContainerMeter.Mode.Voltage) ? "ic2.generic.text.v" : "ic2.generic.text.EUt";
    unit = Localization.translate(unit);
    this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.mode"), 115, 43, 2157374);
    this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.avg"), 15, 41, 2157374);
    this.field_146289_q.func_78276_b("" + Util.toSiString(((ContainerMeter)this.container).getResultAvg(), 6) + unit, 15, 51, 2157374);
    this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.max/min"), 15, 64, 2157374);
    this.field_146289_q.func_78276_b("" + Util.toSiString(((ContainerMeter)this.container).getResultMax(), 6) + unit, 15, 74, 2157374);
    this.field_146289_q.func_78276_b("" + Util.toSiString(((ContainerMeter)this.container).getResultMin(), 6) + unit, 15, 84, 2157374);
    this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.cycle", new Object[] { Integer.valueOf(((ContainerMeter)this.container).getResultCount() / 20) }), 15, 100, 2157374);
    this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.mode.reset"), 39, 114, 2157374);
    switch (((ContainerMeter)this.container).getMode()) {
      case EnergyIn:
        this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.mode.EnergyIn"), 105, 100, 2157374);
        break;
      case EnergyOut:
        this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.mode.EnergyOut"), 105, 100, 2157374);
        break;
      case EnergyGain:
        this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.mode.EnergyGain"), 105, 100, 2157374);
        break;
      case Voltage:
        this.field_146289_q.func_78276_b(Localization.translate("ic2.itemToolMEter.mode.Voltage"), 105, 100, 2157374);
        break;
    } 
  }
  
  protected void func_146976_a(float f, int x, int y) {
    super.func_146976_a(f, x, y);
    bindTexture();
    switch (((ContainerMeter)this.container).getMode()) {
      case EnergyIn:
        drawTexturedRect(112.0D, 55.0D, 40.0D, 40.0D, 176.0D, 0.0D);
        break;
      case EnergyOut:
        drawTexturedRect(112.0D, 55.0D, 40.0D, 40.0D, 176.0D, 40.0D);
        break;
      case EnergyGain:
        drawTexturedRect(112.0D, 55.0D, 40.0D, 40.0D, 176.0D, 120.0D);
        break;
      case Voltage:
        drawTexturedRect(112.0D, 55.0D, 40.0D, 40.0D, 176.0D, 80.0D);
        break;
    } 
  }
  
  protected ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIToolEUMeter.png");
  }
}
