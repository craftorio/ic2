package ic2.core.block.heatgenerator.gui;

import ic2.core.GuiIC2;
import ic2.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import net.minecraft.util.ResourceLocation;

public class GuiRTHeatGenerator extends GuiIC2<ContainerRTHeatGenerator>
{
	public GuiRTHeatGenerator(final ContainerRTHeatGenerator container)
	{
		super(container);
		addElement(Text.create(this, 49, 66, 79, 13, TextProvider.of(() -> container.base.gettransmitHeat() + " / " + container.base.getMaxHeatEmittedPerTick()), 5752026, false, 0, 0, true, true).withTooltip("ic2.RTHeatGenerator.gui.tooltipheat"));
	}

	protected ResourceLocation getTexture()
	{
		return background;
	}

	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIRTHeatGenerator.png");
}
