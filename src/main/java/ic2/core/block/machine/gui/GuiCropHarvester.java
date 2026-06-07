package ic2.core.block.machine.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerCropHarvester;
import ic2.core.gui.EnergyGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCropHarvester extends Ic2Gui<ContainerCropHarvester>
{
	public GuiCropHarvester(ContainerCropHarvester container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 19, 37, container.base));
	}

	@Override
	public ResourceLocation getTexture()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guicropharvester.png");
	}
}
