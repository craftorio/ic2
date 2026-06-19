package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.gui.BasicButton;
import ic2.core.gui.EnergyGauge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiAdvMiner extends Ic2Gui<ContainerAdvMiner>
{
	public GuiAdvMiner(ContainerAdvMiner container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 203);
		this.addElement(EnergyGauge.asBolt(this, 12, 55, container.base));
		this.addElement(BasicButton.create(this, 133, 101, this.createEventSender(0), BasicButton.ButtonStyle.AdvMinerReset).withTooltip("ic2.AdvMiner.gui.switch.reset"));
		this.addElement(BasicButton.create(this, 123, 27, this.createEventSender(1), BasicButton.ButtonStyle.AdvMinerMode).withTooltip("ic2.AdvMiner.gui.switch.mode"));
		this.addElement(BasicButton.create(this, 129, 45, this.createEventSender(2), BasicButton.ButtonStyle.AdvMinerSilkTouch).withTooltip((Supplier<String>) () -> Component.translatable("ic2.AdvMiner.gui.switch.silktouch", container.base.silkTouch).getString()));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		BlockPos target = this.menu.base.getMineTarget();
		if (target != null)
		{
			BlockPos pos = this.menu.base.getBlockPos();
			this.drawString(guiGraphics, 28, 104, Component.translatable("ic2.AdvMiner.gui.info.minelevel", target.getX() - pos.getX(), target.getZ() - pos.getZ(), target.getY() - pos.getY()).getString(), 2157374);
		}

		if (this.menu.base.blacklist)
		{
			this.drawString(guiGraphics, 40, 30, Component.translatable("ic2.AdvMiner.gui.mode.blacklist").getString(), 2157374);
		} else
		{
			this.drawString(guiGraphics, 40, 30, Component.translatable("ic2.AdvMiner.gui.mode.whitelist").getString(), 2157374);
		}

		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiadvminer.png");
	}
}
