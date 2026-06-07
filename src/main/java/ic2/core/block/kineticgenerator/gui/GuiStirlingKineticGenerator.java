package ic2.core.block.kineticgenerator.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiStirlingKineticGenerator extends Ic2Gui<ContainerStirlingKineticGenerator>
{
	public GuiStirlingKineticGenerator(ContainerStirlingKineticGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 204);
		this.addElement(TankGauge.createPlain(this, 19, 47, 12, 44, container.base.getInputTank()));
		this.addElement(TankGauge.createPlain(this, 145, 47, 12, 44, container.base.getOutputTank()));
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guistirlingkineticgenerator.png");
	}
}
