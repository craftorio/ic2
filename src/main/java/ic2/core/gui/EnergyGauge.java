package ic2.core.gui;

import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;
import ic2.core.init.Localization;
import ic2.core.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

public class EnergyGauge extends Gauge<EnergyGauge>
{
	private static final boolean useCleanEnergyValues = false;
	private final Energy energy;

	public static EnergyGauge asBar(GuiIC2<?> gui, int x, int y, TileEntityBlock te)
	{
		return new EnergyGauge(gui, x, y, te, EnergyGauge.EnergyGaugeStyle.Bar);
	}

	public static EnergyGauge asBolt(GuiIC2<?> gui, int x, int y, TileEntityBlock te)
	{
		return new EnergyGauge(gui, x, y, te, EnergyGauge.EnergyGaugeStyle.Bolt);
	}

	public EnergyGauge(GuiIC2<?> gui, int x, int y, TileEntityBlock te, EnergyGauge.EnergyGaugeStyle style)
	{
		super(gui, x, y, style.properties);
		this.energy = te.getComponent(Energy.class);
	}

	@Override
	protected List<String> getToolTip()
	{
		List<String> ret = super.getToolTip();
		double amount = this.energy.getEnergy();
		double capacity = this.energy.getCapacity();
		ret.add(Util.toSiString(amount, 4) + '/' + Util.toSiString(capacity, 4) + ' ' + Localization.translate("ic2.generic.text.EU"));
		return ret;
	}

	@Override
	protected double getRatio()
	{
		return this.energy.getFillRatio();
	}

	public enum EnergyGaugeStyle
	{
		Bar(new Gauge.GaugePropertyBuilder(132, 43, 24, 9, Gauge.GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-4, -11, 32, 32, 128, 0).build()),
		Bolt(new Gauge.GaugePropertyBuilder(116, 65, 7, 13, Gauge.GaugePropertyBuilder.GaugeOrientation.Up).withBackground(-4, -1, 16, 16, 96, 64).build()),
		StirlingBar(
			new Gauge.GaugePropertyBuilder(176, 15, 58, 14, Gauge.GaugePropertyBuilder.GaugeOrientation.Right)
				.withTexture(new ResourceLocation("ic2", "textures/gui/GUIStirlingGenerator.png"))
				.withBackground(59, 33)
				.build()
		);

		private static final Map<String, EnergyGauge.EnergyGaugeStyle> map = getMap();
		public final String name = this.name().toLowerCase(Locale.ENGLISH);
		public final Gauge.GaugeProperties properties;

		EnergyGaugeStyle(Gauge.GaugeProperties properties)
		{
			this.properties = properties;
		}

		public static EnergyGauge.EnergyGaugeStyle get(String name)
		{
			return map.get(name);
		}

		private static Map<String, EnergyGauge.EnergyGaugeStyle> getMap()
		{
			EnergyGauge.EnergyGaugeStyle[] values = values();
			Map<String, EnergyGauge.EnergyGaugeStyle> ret = new HashMap<>(values.length);

			for (EnergyGauge.EnergyGaugeStyle style : values)
			{
				ret.put(style.name, style);
			}

			return ret;
		}
	}
}
