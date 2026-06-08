package ic2.core.block.personal;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiEnergyOMatClosed extends Ic2Gui<ContainerEnergyOMatClosed>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guienergyomatclosed.png");

	public GuiEnergyOMatClosed(ContainerEnergyOMatClosed container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 8, this.imageHeight - 96 + 2, Localization.translate("container.inventory"), 4210752);
		this.drawString(guiGraphics, 12, 21, Localization.translate("ic2.container.personalTrader.want"), 4210752);
		this.drawString(guiGraphics, 12, 39, Localization.translate("ic2.container.personalTrader.offer"), 4210752);
		this.drawString(guiGraphics, 50, 39, ((ContainerEnergyOMatClosed) this.menu).base.euOffer + " EU", 4210752);
		this.drawString(
			guiGraphics,
			12,
			57,
			Localization.translate("ic2.container.personalTraderEnergy.paidFor", ((ContainerEnergyOMatClosed) this.menu).base.paidFor),
			4210752
		);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
