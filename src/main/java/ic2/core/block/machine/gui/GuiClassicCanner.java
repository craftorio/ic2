package ic2.core.block.machine.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.LinkedGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiClassicCanner extends Ic2Gui<ContainerClassicCanner>
{
	public static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/gui_canner_classic.png");

	public GuiClassicCanner(ContainerClassicCanner container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new LinkedGauge(this, 74, 36, container.base, "progress", Gauge.GaugeStyle.ProgressLongArrow));
		this.addElement(EnergyGauge.asBolt(this, 34, 28, container.base));
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}
