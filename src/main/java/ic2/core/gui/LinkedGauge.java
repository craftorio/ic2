package ic2.core.gui;

import ic2.core.GuiIC2;
import ic2.core.gui.dynamic.IGuiValueProvider;

public class LinkedGauge extends Gauge<LinkedGauge>
{
	private final IGuiValueProvider provider;
	protected final String name;

	public LinkedGauge(GuiIC2<?> gui, int x, int y, IGuiValueProvider provider, String name, Gauge.IGaugeStyle style)
	{
		super(gui, x, y, style.getProperties());
		this.provider = provider;
		this.name = name;
	}

	@Override
	protected double getRatio()
	{
		return this.provider.getGuiValue(this.name);
	}
}
