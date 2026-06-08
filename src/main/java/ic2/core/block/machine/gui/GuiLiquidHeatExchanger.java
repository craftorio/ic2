package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerLiquidHeatExchanger;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankGauge;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiLiquidHeatExchanger extends Ic2Gui<ContainerLiquidHeatExchanger>
{
	public GuiLiquidHeatExchanger(ContainerLiquidHeatExchanger container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 204);
		this.addElement(new SlotGrid(this, 46, 50, 5, 1, SlotGrid.SlotStyle.Plain, 1, 1).withTooltip("ic2.LiquidHeatExchanger.gui.tooltipvent"));
		this.addElement(new SlotGrid(this, 46, 72, 5, 1, SlotGrid.SlotStyle.Plain, 1, 1).withTooltip("ic2.LiquidHeatExchanger.gui.tooltipvent"));
		this.addElement(TankGauge.createPlain(this, 19, 47, 12, 44, container.base.getInputTank()));
		this.addElement(TankGauge.createPlain(this, 145, 47, 12, 44, container.base.getOutputTank()));
		this.addElement(
			TextLabel.create(
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
									((ContainerLiquidHeatExchanger) GuiLiquidHeatExchanger.this.menu).base.gettransmitHeat(),
									((ContainerLiquidHeatExchanger) GuiLiquidHeatExchanger.this.menu).base.getMaxHeatEmittedPerTick()
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
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiheatsourcefluid.png");
	}
}
