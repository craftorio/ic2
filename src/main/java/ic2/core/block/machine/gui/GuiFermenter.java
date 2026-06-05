package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerFermenter;
import ic2.core.gui.Gauge;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;

public class GuiFermenter extends GuiIC2<ContainerFermenter> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIFermenter.png");

   public GuiFermenter(final ContainerFermenter container) {
      super(container, 184);
      this.addElement(TankGauge.createPlain(this, 38, 49, 48, 30, container.base.getInputTank()));
      this.addElement(TankGauge.createNormal(this, 125, 22, container.base.getOutputTank()));
      this.addElement(new LinkedGauge(this, 42, 41, container.base, "heat", Gauge.GaugeStyle.HeatFermenter).withTooltip(new Supplier<String>() {
         public String get() {
            return Localization.translate("ic2.Fermenter.gui.info.conversion") + " " + (int)(container.base.getGuiValue("heat") * 100.0) + "%";
         }
      }));
      this.addElement(new LinkedGauge(this, 38, 88, container.base, "progress", Gauge.GaugeStyle.ProgressFermenter).withTooltip("ic2.Fermenter.gui.info.waste"));
   }

   @Override
   protected ResourceLocation getTexture() {
      return TEXTURE;
   }
}
