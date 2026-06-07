package ic2.core.block.machine.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerClassicCropmatron;
import ic2.core.gui.EnergyGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiClassicCropmatron extends Ic2Gui<ContainerClassicCropmatron>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/gui_cropmatron_classic.png");

	public GuiClassicCropmatron(ContainerClassicCropmatron container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 29, 39, container.base));
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}
