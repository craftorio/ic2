package ic2.core.block.personal;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.gui.VanillaButton;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiTradeOMatOpen extends Ic2Gui<ContainerTradeOMatOpen>
{
	private static final ResourceLocation background = IC2.getIdentifier("textures/gui/guitradeomatopen.png");

	public GuiTradeOMatOpen(ContainerTradeOMatOpen container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		if (((ContainerTradeOMatOpen) this.menu).canToggleInfinite)
		{
			this.addElement(new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withText("∞"));
		}
	}

	@Override
	protected void drawForegroundLayer(PoseStack matrices, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(matrices, mouseX, mouseY);
		this.drawString(matrices, 8, this.imageHeight - 96 + 2, Localization.translate("container.inventory"), 4210752);
		this.drawString(matrices, 12, 23, Localization.translate("ic2.container.personalTrader.want"), 4210752);
		this.drawString(matrices, 12, 57, Localization.translate("ic2.container.personalTrader.offer"), 4210752);
		this.drawString(matrices, 108, 28, Localization.translate("ic2.container.personalTrader.totalTrades0"), 4210752);
		this.drawString(matrices, 108, 36, Localization.translate("ic2.container.personalTrader.totalTrades1"), 4210752);
		this.drawString(matrices, 112, 44, ((ContainerTradeOMatOpen) this.menu).base.totalTradeCount + "", 4210752);
		this.drawString(
			matrices,
			108,
			60,
			Localization.translate("ic2.container.personalTrader.stock")
				+ " "
				+ (((ContainerTradeOMatOpen) this.menu).base.stock < 0 ? "∞" : ((ContainerTradeOMatOpen) this.menu).base.stock + ""),
			4210752
		);
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}
