package ic2.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidDistributor extends Ic2Gui<ContainerFluidDistributor>
{
	public GuiFluidDistributor(ContainerFluidDistributor container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(TankGauge.createPlain(this, 29, 38, 55, 47, container.base.fluidTank));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 116, 47, Localization.translate("ic2.FluidDistributor.gui.mode.info"), 5752026);
		
		String drawnString = this.menu.base.getActive() ? Localization.translate("ic2.FluidDistributor.gui.mode.concentrate") : Localization.translate("ic2.FluidDistributor.gui.mode.distribute");
		this.drawString(guiGraphics, 99, 71, drawnString, 5752026);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		mouseX -= this.leftPos;
		mouseY -= this.topPos;
		if (mouseX >= 117.0 && mouseY >= 58.0 && mouseX <= 135.0 && mouseY <= 66.0)
		{
			IC2.network.get(false).initiateClientTileEntityEvent(this.menu.base, 1);
			return true;
		} else
		{
			mouseX += this.leftPos;
			mouseY += this.topPos;
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guifluiddistributor.png");
	}
}
