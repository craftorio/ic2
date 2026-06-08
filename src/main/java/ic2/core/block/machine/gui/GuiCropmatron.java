package ic2.core.block.machine.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCropmatron extends Ic2Gui<ContainerCropmatron>
{
	public GuiCropmatron(ContainerCropmatron container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 192);
		this.addElement(EnergyGauge.asBolt(this, 138, 82, container.base));
		this.addElement(TankGauge.createPlain(this, 11, 26, 24, 47, container.base.getWaterTank()));
		this.addElement(TankGauge.createPlain(this, 105, 26, 24, 47, container.base.getExTank()));
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guicropmatron.png");
	}
}
