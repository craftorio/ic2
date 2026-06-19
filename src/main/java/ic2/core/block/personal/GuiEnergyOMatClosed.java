package ic2.core.block.personal;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
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
		this.drawString(guiGraphics, 8, this.imageHeight - 96 + 2, Component.translatable("container.inventory").getString(), 4210752);
		this.drawString(guiGraphics, 12, 21, Component.translatable("ic2.container.personalTrader.want").getString(), 4210752);
		this.drawString(guiGraphics, 12, 39, Component.translatable("ic2.container.personalTrader.offer").getString(), 4210752);
		this.drawString(guiGraphics, 50, 39, this.menu.base.euOffer + " EU", 4210752);
		this.drawString(guiGraphics, 12, 57, Component.translatable("ic2.container.personalTraderEnergy.paidFor", this.menu.base.paidFor).getString(), 4210752);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
