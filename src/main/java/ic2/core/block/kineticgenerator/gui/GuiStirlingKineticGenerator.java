package ic2.core.block.kineticgenerator.gui;

import ic2.core.GuiIC2;
import ic2.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;

public class GuiStirlingKineticGenerator extends GuiIC2<ContainerStirlingKineticGenerator>
{
	public GuiStirlingKineticGenerator(ContainerStirlingKineticGenerator container)
	{
		super(container, 204);
		addElement(TankGauge.createPlain(this, 19, 47, 12, 44, container.base.getInputTank()));
		addElement(TankGauge.createPlain(this, 145, 47, 12, 44, container.base.getOutputTank()));
	}

	protected ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUIStirlingKineticGenerator.png");
	}
}
