package ic2.core.block.personal;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.gui.VanillaButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiEnergyOMatOpen extends Ic2Gui<ContainerEnergyOMatOpen>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guienergyomatopen.png");

	public GuiEnergyOMatOpen(ContainerEnergyOMatOpen container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new VanillaButton(this, 102, 16, 32, 10, this.createEventSender(0)).withText("-100k"));
		this.addElement(new VanillaButton(this, 102, 26, 32, 10, this.createEventSender(1)).withText("-10k"));
		this.addElement(new VanillaButton(this, 102, 36, 32, 10, this.createEventSender(2)).withText("-1k"));
		this.addElement(new VanillaButton(this, 102, 46, 32, 10, this.createEventSender(3)).withText("-100"));
		this.addElement(new VanillaButton(this, 134, 16, 32, 10, this.createEventSender(4)).withText("+100k"));
		this.addElement(new VanillaButton(this, 134, 26, 32, 10, this.createEventSender(5)).withText("+10k"));
		this.addElement(new VanillaButton(this, 134, 36, 32, 10, this.createEventSender(6)).withText("+1k"));
		this.addElement(new VanillaButton(this, 134, 46, 32, 10, this.createEventSender(7)).withText("+100"));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 8, this.imageHeight - 96 + 2, Component.translatable("container.inventory").getString(), 4210752);
		this.drawString(guiGraphics, 100, 60, Component.translatable("ic2.container.personalTrader.offer").getString(), 4210752);
		this.drawString(guiGraphics, 100, 68, this.menu.base.euOffer + " EU", 4210752);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
