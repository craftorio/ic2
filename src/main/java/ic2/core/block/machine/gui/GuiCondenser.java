package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerCondenser;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;

public class GuiCondenser extends GuiIC2<ContainerCondenser>
{
	public GuiCondenser(final ContainerCondenser container)
	{
		super(container, 184);
		Supplier<String> ventTooltipSupplier = () -> Localization.translate("ic2.Condenser.gui.tooltipvent", (short) 2);
		addElement((new SlotGrid(this, 25, 25, 1, 2, SlotGrid.SlotStyle.Normal)).withTooltip(ventTooltipSupplier));
		addElement((new SlotGrid(this, 133, 25, 1, 2, SlotGrid.SlotStyle.Normal)).withTooltip(ventTooltipSupplier));
		addElement(EnergyGauge.asBolt(this, 12, 26, container.base));
		addElement(TankGauge.createPlain(this, 46, 27, 84, 33, container.base.getInputTank()));
		addElement(TankGauge.createPlain(this, 46, 74, 84, 15, container.base.getOutputTank()));
		addElement(new LinkedGauge(this, 47, 63, container.base, "progress", Gauge.GaugeStyle.ProgressCondenser));
	}

	protected ResourceLocation getTexture()
	{
		return BACKGROUND;
	}

	private static final ResourceLocation BACKGROUND = new ResourceLocation("ic2", "textures/gui/GUICondenser.png");
}
