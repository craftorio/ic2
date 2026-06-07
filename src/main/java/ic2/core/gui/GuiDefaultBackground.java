package ic2.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.ContainerBase;
import ic2.core.Ic2Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

public abstract class GuiDefaultBackground<T extends ContainerBase<? extends Container>> extends Ic2Gui<T>
{
	public GuiDefaultBackground(T container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
	}

	public GuiDefaultBackground(T container, Inventory playerInventory, Component title, int ySize)
	{
		super(container, playerInventory, title, ySize);
	}

	public GuiDefaultBackground(T container, Inventory playerInventory, Component title, int xSize, int ySize)
	{
		super(container, playerInventory, title, xSize, ySize);
	}

	@Override
	protected void drawBackgroundAndTitle(PoseStack matrices, float partialTicks, int mouseX, int mouseY)
	{
		GuiElement.bindCommonTexture();
		int width = this.imageWidth;
		int height = this.imageHeight;
		this.drawTexturedRect(matrices, -16.0, -16.0, 32.0, 32.0, 0.0, 0.0);
		this.drawTexturedRect(matrices, width - 16, -16.0, 32.0, 32.0, 64.0, 0.0);
		this.drawTexturedRect(matrices, -16.0, height - 16, 32.0, 32.0, 0.0, 64.0);
		this.drawTexturedRect(matrices, width - 16, height - 16, 32.0, 32.0, 64.0, 64.0);

		for (int side = 0; side < 2; side++)
		{
			int y = height * side - 16;
			int v = 64 * side;

			for (int x = 16; x < width - 16; x += 32)
			{
				int cwidth = Math.min(32, width - 16 - x);
				this.drawTexturedRect(matrices, x, y, cwidth, 32.0, 32.0, v);
			}
		}

		for (int side = 0; side < 2; side++)
		{
			int x = width * side - 16;
			int u = 64 * side;

			for (int y = 16; y < height - 16; y += 32)
			{
				int cheight = Math.min(32, height - 16 - y);
				this.drawTexturedRect(matrices, x, y, 32.0, cheight, u, 32.0);
			}
		}

		for (int y = 16; y < height - 16; y += 32)
		{
			int cheight = Math.min(32, height - 16 - y);

			for (int x = 16; x < width - 16; x += 32)
			{
				int cwidth = Math.min(32, width - 16 - x);
				this.drawTexturedRect(matrices, x, y, cwidth, cheight, 32.0, 32.0);
			}
		}
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return null;
	}
}
