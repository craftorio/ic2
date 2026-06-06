package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerLiquidHeatExchanger;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankGauge;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;

public class GuiLiquidHeatExchanger extends GuiIC2<ContainerLiquidHeatExchanger>
{
	public GuiLiquidHeatExchanger(ContainerLiquidHeatExchanger container)
	{
		super(container, 204);
		this.addElement(new SlotGrid(this, 46, 50, 5, 1, SlotGrid.SlotStyle.Plain, 1, 1).withTooltip("ic2.LiquidHeatExchanger.gui.tooltipvent"));
		this.addElement(new SlotGrid(this, 46, 72, 5, 1, SlotGrid.SlotStyle.Plain, 1, 1).withTooltip("ic2.LiquidHeatExchanger.gui.tooltipvent"));
		this.addElement(TankGauge.createPlain(this, 19, 47, 12, 44, container.base.getInputTank()));
		this.addElement(TankGauge.createPlain(this, 145, 47, 12, 44, container.base.getOutputTank()));
		this.addElement(
			Text.create(
					this,
					20,
					28,
					138,
					13,
					TextProvider.of(
						new Supplier<String>()
						{
							public String get()
							{
								return Localization.translate(
									"ic2.ElectricHeatGenerator.gui.hUmax",
									GuiLiquidHeatExchanger.this.container.base.gettransmitHeat(),
									GuiLiquidHeatExchanger.this.container.base.getMaxHeatEmittedPerTick()
								);
							}
						}
					),
					5752026,
					false,
					true,
					true
				)
				.withTooltip("ic2.LiquidHeatExchanger.gui.tooltipheat")
		);
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUIHeatSourceFluid.png");
	}
}
