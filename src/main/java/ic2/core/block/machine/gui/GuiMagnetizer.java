package ic2.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.block.inherit.Ic2FenceBlock;
import ic2.core.block.machine.container.ContainerMagnetizer;
import ic2.core.gui.EnergyGauge;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiMagnetizer extends Ic2Gui<ContainerMagnetizer>
{
	public GuiMagnetizer(ContainerMagnetizer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 11, 28, container.base));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guimagnetizer.png");
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		if (Ic2FenceBlock.hasMetalShoes(((ContainerMagnetizer) this.menu).getPlayer()))
		{
			this.drawString(guiGraphics, 18, 66, Localization.translate("ic2.Magnetizer.gui.hasMetalShoes"), 4259648);
		} else
		{
			this.drawString(guiGraphics, 18, 66, Localization.translate("ic2.Magnetizer.gui.noMetalShoes"), 16728128);
		}
	}
}
