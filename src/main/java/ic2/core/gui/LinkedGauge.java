package ic2.core.gui;

import ic2.core.Ic2Gui;
import ic2.core.gui.dynamic.IGuiValueProvider;

public class LinkedGauge extends Gauge<LinkedGauge>
{
	protected final String name;
	private final IGuiValueProvider provider;

	public LinkedGauge(Ic2Gui<?> gui, int x, int y, IGuiValueProvider provider, String name, Gauge.IGaugeStyle style)
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
