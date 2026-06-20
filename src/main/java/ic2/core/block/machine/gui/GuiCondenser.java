package ic2.core.block.machine.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerCondenser;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Supplier;

public class GuiCondenser extends Ic2Gui<ContainerCondenser>
{
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guicondenser.png");

	public GuiCondenser(ContainerCondenser container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		Supplier<String> ventTooltipSupplier = () -> Component.translatable("ic2.Condenser.gui.tooltipvent", (short) 2).getString();
		this.addElement(new SlotGrid(this, 25, 25, 1, 2, SlotGrid.SlotStyle.Normal).withTooltip(ventTooltipSupplier));
		this.addElement(new SlotGrid(this, 133, 25, 1, 2, SlotGrid.SlotStyle.Normal).withTooltip(ventTooltipSupplier));
		this.addElement(EnergyGauge.asBolt(this, 12, 26, container.base));
		this.addElement(TankGauge.createPlain(this, 46, 27, 84, 33, container.base.getInputTank()));
		this.addElement(TankGauge.createPlain(this, 46, 74, 84, 15, container.base.getOutputTank()));
		this.addElement(new LinkedGauge(this, 47, 63, container.base, "progress", Gauge.GaugeStyle.ProgressCondenser));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return BACKGROUND;
	}
}
