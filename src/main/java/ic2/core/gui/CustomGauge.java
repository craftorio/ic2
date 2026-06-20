package ic2.core.gui;

import ic2.core.Ic2Gui;

public class CustomGauge extends Gauge<CustomGauge>
{
	private final CustomGauge.IGaugeRatioProvider provider;

	public static CustomGauge asFuel(Ic2Gui<?> gui, int x, int y, CustomGauge.IGaugeRatioProvider provider)
	{
		return new CustomGauge(gui, x, y, provider, Gauge.GaugeStyle.Fuel.properties);
	}

	public static CustomGauge create(Ic2Gui<?> gui, int x, int y, CustomGauge.IGaugeRatioProvider provider, Gauge.GaugeStyle style)
	{
		return new CustomGauge(gui, x, y, provider, style.properties);
	}

	public CustomGauge(Ic2Gui<?> gui, int x, int y, CustomGauge.IGaugeRatioProvider provider, Gauge.GaugeProperties properties)
	{
		super(gui, x, y, properties);
		this.provider = provider;
	}

	@Override
	protected double getRatio()
	{
		return this.provider.getRatio();
	}

	public interface IGaugeRatioProvider
	{
		double getRatio();
	}
}
