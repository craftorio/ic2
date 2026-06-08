package ic2.core.block.personal;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.init.Localization;
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
	protected void drawForegroundLayer(PoseStack matrices, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(matrices, mouseX, mouseY);
		this.drawString(matrices, 8, this.imageHeight - 96 + 2, Localization.translate("container.inventory"), 4210752);
		this.drawString(matrices, 12, 23, Localization.translate("ic2.container.personalTrader.want"), 4210752);
		this.drawString(matrices, 12, 42, Localization.translate("ic2.container.personalTrader.offer"), 4210752);
		this.drawString(matrices, 12, 60, Localization.translate("ic2.container.personalTrader.stock"), 4210752);
		this.drawString(
			matrices,
			50,
			60,
			((ContainerTradeOMatClosed) this.menu).base.stock < 0 ? "∞" : ((ContainerTradeOMatClosed) this.menu).base.stock + "",
			((ContainerTradeOMatClosed) this.menu).base.stock != 0 ? 4210752 : 16733525
		);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
