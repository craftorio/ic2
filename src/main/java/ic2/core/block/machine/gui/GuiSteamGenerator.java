package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.gui.*;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;

public class GuiSteamGenerator extends GuiIC2<ContainerSteamGenerator>
{
	public GuiSteamGenerator(ContainerSteamGenerator container)
	{
		super(container, 220);
		addElement(TankGauge.createPlain(this, 10, 155, 75, 47, container.base.waterTank));
		addElement((new LinkedGauge(this, 13, 70, container.base, "heat", Gauge.GaugeStyle.HeatSteamGenerator))
			.withTooltip(() -> Localization.translate("ic2.SteamGenerator.gui.systemheat", container.base.getSystemHeat())));
		addElement((new LinkedGauge(this, 155, 61, container.base, "calcification", Gauge.GaugeStyle.CalcificationSteamGenerator))
			.withTooltip(() -> Localization.translate("ic2.SteamGenerator.gui.calcification", new Object[] { container.base.getCalcification() }) + '%'));
		addElement(Text.create(this, 91, 172, 59, 13, TextProvider.of(() -> GuiSteamGenerator.this.container.base.getInputMB() + Localization.translate("ic2.generic.text.mb") + Localization.translate("ic2.generic.text.tick")), 2157374, false, true, true).withTooltip("ic2.SteamGenerator.gui.info.waterinput"));
		addElement(Text.create(this, 31, 133, 111, 13, TextProvider.of(() -> Localization.translate("ic2.SteamGenerator.gui.heatInput", container.base.getHeatInput())), 2157374, false, 4, 0, false, true).withTooltip("ic2.SteamGenerator.gui.info.heatinput"));
		addElement(Text.create(this, 22, 35, 42, 13, TextProvider.of(() -> Localization.translate("ic2.SteamGenerator.gui.pressurevalve", container.base.getPressure())), 2157374, false, 4, 0, false, true).withTooltip("ic2.SteamGenerator.gui.info.pressvalve"));
		addElement(Text.create(this, 66, 25, 81, 13, TextProvider.of(() -> GuiSteamGenerator.this.container.base.getOutputMB() + Localization.translate("ic2.generic.text.mb") + Localization.translate("ic2.generic.text.tick")), 2157374, false, 4, 0, false, true).withTooltip("ic2.SteamGenerator.gui.info.fluidoutput"));
		addElement(Text.create(this, 66, 45, 100, 13, TextProvider.of(() -> Localization.translate(GuiSteamGenerator.this.container.base.getOutputFluidName())), 2157374, false, 4, 0, false, true));
		for (byte i = 0; i < 4; i = (byte) (i + 1))
		{
			int event = (int) Math.pow(10.0D, (3 - i));
			int xShift = 10 * i;
			addElement(new SteamBoilerButton(92 + xShift, 186, 9, 9, -event));
			addElement(new SteamBoilerButton(92 + xShift, 162, 9, 9, event));
			if (i != 3)
			{
				event = (int) Math.pow(10.0D, (2 - i));
				addElement(new SteamBoilerButton(23 + xShift, 49, 9, 9, -(2000 + event)));
				addElement(new SteamBoilerButton(23 + xShift, 25, 9, 9, 2000 + event));
			}
		}
	}

	public ResourceLocation getTexture()
	{
		return BACKGROUND;
	}

	private static final ResourceLocation BACKGROUND = new ResourceLocation("ic2", "textures/gui/GUISteamGenerator.png");

	private class SteamBoilerButton extends CustomButton
	{
		public SteamBoilerButton(int x, int y, int width, int height, int event)
		{
			super(GuiSteamGenerator.this, x, y, width, height, button ->
			{
				if (button == MouseButton.left)
					IC2.network.get(false).initiateClientTileEntityEvent(GuiSteamGenerator.this.container.base, event);
			});
		}
	}
}
