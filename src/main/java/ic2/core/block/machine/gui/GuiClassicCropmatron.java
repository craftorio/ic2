package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerClassicCropmatron;
import ic2.core.gui.EnergyGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiClassicCropmatron extends GuiIC2<ContainerClassicCropmatron> {
   private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUI_Cropmatron_Classic.png");

   public GuiClassicCropmatron(ContainerClassicCropmatron container) {
      super(container);
      this.addElement(EnergyGauge.asBolt(this, 29, 39, container.base));
   }

   @Override
   protected ResourceLocation getTexture() {
      return background;
   }
}
