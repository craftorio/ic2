package ic2.core.block.wiring;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.VanillaButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiElectricBlock extends Ic2Gui<ContainerElectricBlock>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guielectricblock.png");

	public GuiElectricBlock(ContainerElectricBlock container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 196);
		this.addElement(EnergyGauge.asBar(this, 79, 38, container.base));
		this.addElement(new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withIcon(() -> new ItemStack(Items.REDSTONE)).withTooltip(container.base::getRedstoneMode));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		int rightTextHeight = 24;
		int color = 4207152;
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 8, 74, Component.translatable("ic2.EUStorage.gui.info.armor").getString(), color);
		this.drawString(guiGraphics, 79, rightTextHeight, Component.translatable("ic2.EUStorage.gui.info.level", this.menu.base.energy.getSourceTier()).getString(), color);
		int e = (int) Math.min(this.menu.base.energy.getEnergy(), this.menu.base.energy.getCapacity());
		this.drawString(guiGraphics, 110, rightTextHeight + 10, " " + e, color);
		this.drawString(guiGraphics, 110, rightTextHeight + 20, "/" + (int) this.menu.base.energy.getCapacity(), color);
		String output = Component.translatable("ic2.EUStorage.gui.info.output", this.menu.base.getOutput()).getString();
		this.drawString(guiGraphics, 85, rightTextHeight + 35, output, color);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
