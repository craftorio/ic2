package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerFermenter;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.TankGauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;

public class GuiFermenter extends GuiIC2<ContainerFermenter> {
  public GuiFermenter(final ContainerFermenter container) {
    super((ContainerBase)container, 184);
    addElement((GuiElement)TankGauge.createPlain(this, 38, 49, 48, 30, (IFluidTank)((TileEntityFermenter)container.base).getInputTank()));
    addElement((GuiElement)TankGauge.createNormal(this, 125, 22, (IFluidTank)((TileEntityFermenter)container.base).getOutputTank()));
    addElement((new LinkedGauge(this, 42, 41, (IGuiValueProvider)container.base, "heat", (Gauge.IGaugeStyle)Gauge.GaugeStyle.HeatFermenter))
        .withTooltip(new Supplier<String>() {
            public String get() {
              return Localization.translate("ic2.Fermenter.gui.info.conversion") + " " + (int)(((TileEntityFermenter)container.base).getGuiValue("heat") * 100.0D) + "%";
            }
          }));
    addElement((new LinkedGauge(this, 38, 88, (IGuiValueProvider)container.base, "progress", (Gauge.IGaugeStyle)Gauge.GaugeStyle.ProgressFermenter))
        .withTooltip("ic2.Fermenter.gui.info.waste"));
  }
  
  protected ResourceLocation getTexture() {
    return TEXTURE;
  }
  
  private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIFermenter.png");
}
