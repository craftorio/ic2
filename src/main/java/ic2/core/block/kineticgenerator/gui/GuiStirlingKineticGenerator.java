package ic2.core.block.kineticgenerator.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.gui.GuiElement;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;

public class GuiStirlingKineticGenerator extends GuiIC2<ContainerStirlingKineticGenerator> {
  public GuiStirlingKineticGenerator(ContainerStirlingKineticGenerator container) {
    super((ContainerBase)container, 204);
    addElement((GuiElement)TankGauge.createPlain(this, 19, 47, 12, 44, (IFluidTank)((TileEntityStirlingKineticGenerator)container.base).getInputTank()));
    addElement((GuiElement)TankGauge.createPlain(this, 145, 47, 12, 44, (IFluidTank)((TileEntityStirlingKineticGenerator)container.base).getOutputTank()));
  }
  
  protected ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIStirlingKineticGenerator.png");
  }
}
