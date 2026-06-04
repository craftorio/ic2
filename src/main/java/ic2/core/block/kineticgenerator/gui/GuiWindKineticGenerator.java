package ic2.core.block.kineticgenerator.gui;

import ic2.core.GuiIC2;
import ic2.core.block.kineticgenerator.container.ContainerWindKineticGenerator;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Image;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;

public class GuiWindKineticGenerator extends GuiIC2<ContainerWindKineticGenerator>
{
	public GuiWindKineticGenerator(final ContainerWindKineticGenerator container)
	{
		super(container);
		addElement(Text.create(this, 17, 48, 143, 13, TextProvider.of(() ->
		{
			if (!container.base.hasRotor())
				return Localization.translate("ic2.WindKineticGenerator.gui.rotormiss");
			if (!container.base.rotorHasSpace())
				return Localization.translate("ic2.WindKineticGenerator.gui.rotorspace");
			if (!container.base.isWindStrongEnough())
				return Localization.translate("ic2.WindKineticGenerator.gui.windweak1");
			return Localization.translate("ic2.WindKineticGenerator.gui.output", container.base.getKuOutput());
		}), 2157374, false, 4, 0, false, true));
		addElement(Text.create(this, 17, 66, 143, 13, TextProvider.of(() ->
		{
			if (!container.base.hasRotor() || !container.base.rotorHasSpace())
				return null;
			if (!container.base.isWindStrongEnough())
				return Localization.translate("ic2.WindKineticGenerator.gui.windweak2");
			return container.base.getRotorHealth() + " %";
		}), 2157374, false, 4, 0, false, true));
		IEnableHandler warningEnabler = container.base::isRotorOverloaded;
		addElement(Image.create(this, 44, 20, 30, 26, background, 256, 256, 176, 0, 206, 26)
			.withEnableHandler(warningEnabler)
			.withTooltip("ic2.WindKineticGenerator.error.overload"));
		addElement(Image.create(this, 102, 20, 30, 26, background, 256, 256, 176, 0, 206, 26)
			.withEnableHandler(warningEnabler)
			.withTooltip("ic2.WindKineticGenerator.error.overload"));
	}

	protected ResourceLocation getTexture()
	{
		return background;
	}

	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIWindKineticGenerator.png");
}
