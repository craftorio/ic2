package ic2.core.item.tool;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.gui.TextLabel;
import ic2.core.ref.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiToolbox extends Ic2Gui<ContainerToolbox>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guitoolbox.png");

	public GuiToolbox(ContainerToolbox container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TextLabel.create(this, 65, 11, Ic2Items.TOOL_BOX.getDescription().getString(), 0, false));
	}

	@Override
	protected void drawBackgroundAndTitle(PoseStack matrices, float partialTicks, int mouseX, int mouseY)
	{
		this.bindTexture();
		this.blit(matrices, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
