package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiClassicCanner extends GuiIC2<ContainerClassicCanner> {
  public GuiClassicCanner(ContainerClassicCanner container) {
    super((ContainerBase)container);
    addElement((GuiElement)new LinkedGauge(this, 74, 36, (IGuiValueProvider)container.base, "progress", (Gauge.IGaugeStyle)Gauge.GaugeStyle.ProgressLongArrow));
    addElement((GuiElement)EnergyGauge.asBolt(this, 34, 28, (TileEntityBlock)container.base));
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  public static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUI_Canner_Classic.png");
}
