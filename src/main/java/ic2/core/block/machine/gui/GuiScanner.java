package ic2.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerScanner;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiScanner extends Ic2Gui<ContainerScanner>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiscanner.png");
	private final Component[] info = new Component[9];

	public GuiScanner(ContainerScanner container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 12, 25, container.base));
		this.addElement(new CustomButton(this, 102, 49, 12, 12, 176, 57, background, this.createEventSender(0)).withEnableHandler(() -> container.base.getState() == TileEntityScanner.State.COMPLETED || container.base.getState() == TileEntityScanner.State.TRANSFER_ERROR || container.base.getState() == TileEntityScanner.State.FAILED).withTooltip("ic2.Scanner.gui.button.delete"));
		this.addElement(new CustomButton(this, 143, 49, 24, 12, 176, 69, background, this.createEventSender(1)).withEnableHandler(() -> container.base.getState() == TileEntityScanner.State.COMPLETED || container.base.getState() == TileEntityScanner.State.TRANSFER_ERROR).withTooltip("ic2.Scanner.gui.button.save"));
		this.info[1] = Component.translatable("ic2.Scanner.gui.info1");
		this.info[2] = Component.translatable("ic2.Scanner.gui.info2");
		this.info[3] = Component.translatable("ic2.Scanner.gui.info3");
		this.info[4] = Component.translatable("ic2.Scanner.gui.info4");
		this.info[5] = Component.translatable("ic2.Scanner.gui.info5");
		this.info[6] = Component.translatable("ic2.Scanner.gui.info6");
		this.info[7] = Component.translatable("ic2.Scanner.gui.info7");
		this.info[8] = Component.translatable("ic2.Scanner.gui.info8");
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 123, 6, this.info[5] + ":", 4210752);
		TileEntityScanner te = this.menu.base;
		switch (te.getState())
		{
			case IDLE:
				this.drawString(guiGraphics, 10, 69, Component.translatable("ic2.Scanner.gui.idle").getString(), 15461152);
				break;
			case NO_STORAGE:
				this.drawString(guiGraphics, 10, 69, this.info[2].getString(), 15461152);
				break;
			case SCANNING:
				this.drawString(guiGraphics, 10, 69, this.info[1].getString(), 2157374);
				this.drawString(guiGraphics, 125, 69, te.getPercentageDone() + "%", 2157374);
				break;
			case NO_ENERGY:
				this.drawString(guiGraphics, 10, 69, this.info[3].getString(), 14094352);
				break;
			case ALREADY_RECORDED:
				this.drawString(guiGraphics, 10, 69, this.info[8].getString(), 14094352);
				break;
			case FAILED:
				this.drawString(guiGraphics, 10, 69, this.info[4].getString(), 2157374);
				this.drawString(guiGraphics, 110, 30, this.info[6].getString(), 14094352);
				break;
			case COMPLETED:
			case TRANSFER_ERROR:
				if (te.getState() == TileEntityScanner.State.COMPLETED)
				{
					this.drawString(guiGraphics, 10, 69, this.info[4].getString(), 2157374);
				}

				if (te.getState() == TileEntityScanner.State.TRANSFER_ERROR)
				{
					this.drawString(guiGraphics, 10, 69, this.info[7].getString(), 14094352);
				}

				this.drawString(guiGraphics, 105, 25, Util.toSiString(te.patternUu, 4) + "B UUM", 16777215);
				this.drawString(guiGraphics, 105, 36, Util.toSiString(te.patternEu, 4) + "EU", 16777215);
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY)
	{
		super.renderBg(guiGraphics, delta, mouseX, mouseY);
		this.bindTexture();
		TileEntityScanner te = this.menu.base;
		int scanning = te.getSubPercentageDoneScaled(43);
		if (scanning > 0)
		{
			this.drawTexturedRect(guiGraphics.pose(), 30, 20 + 43 - scanning, 66, scanning, 176, 14 + 43 - scanning);
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
