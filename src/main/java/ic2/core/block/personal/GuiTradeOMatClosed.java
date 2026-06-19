package ic2.core.block.personal;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiTradeOMatClosed extends Ic2Gui<ContainerTradeOMatClosed>
{
	private static final ResourceLocation background = IC2.getIdentifier("textures/gui/guitradeomatclosed.png");

	public GuiTradeOMatClosed(ContainerTradeOMatClosed container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 8, this.imageHeight - 96 + 2, Component.translatable("container.inventory").getString(), 4210752);
		this.drawString(guiGraphics, 12, 23, Component.translatable("ic2.container.personalTrader.want").getString(), 4210752);
		this.drawString(guiGraphics, 12, 42, Component.translatable("ic2.container.personalTrader.offer").getString(), 4210752);
		this.drawString(guiGraphics, 12, 60, Component.translatable("ic2.container.personalTrader.stock").getString(), 4210752);
		this.drawString(guiGraphics, 50, 60, this.menu.base.stock < 0 ? "∞" : this.menu.base.stock + "", this.menu.base.stock != 0 ? 4210752 : 16733525);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
