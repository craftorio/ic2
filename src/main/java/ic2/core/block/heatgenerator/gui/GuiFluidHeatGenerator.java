package ic2.core.block.heatgenerator.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidHeatGenerator extends Ic2Gui<ContainerFluidHeatGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guifluidheatgenerator.png");

	public GuiFluidHeatGenerator(ContainerFluidHeatGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TankGauge.createNormal(this, 70, 20, container.base.getFluidTank()));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(
			guiGraphics,
			96,
			33,
			Localization.translate("ic2.FluidHeatGenerator.gui.info.Emit") + ((ContainerFluidHeatGenerator) this.menu).base.gettransmitHeat(),
			5752026
		);
		this.drawString(
			guiGraphics,
			96,
			52,
			Localization.translate("ic2.FluidHeatGenerator.gui.info.MaxEmit") + ((ContainerFluidHeatGenerator) this.menu).base.getMaxHeatEmittedPerTick(),
			5752026
		);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
