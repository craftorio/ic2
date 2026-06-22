package ic2.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import net.minecraft.resources.ResourceLocation;

public class VanillaButton extends Button<VanillaButton>
{
	private static final ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/gui/widgets.png");
	private static final int uNormal = 0;
	private static final int vNormal = 66;
	private static final int uHover = 0;
	private static final int vHover = 86;
	private static final int uDisabled = 0;
	private static final int vDisabled = 46;
	private static final int rawWidth = 200;
	private static final int rawHeight = 20;
	private static final int minLeft = 2;
	private static final int minRight = 2;
	private static final int minTop = 2;
	private static final int minBottom = 3;
	private static final int colorNormal = 14737632;
	private static final int colorHover = 16777120;
	private static final int colorDisabled = 10526880;
	protected IEnableHandler disableHandler;

	public VanillaButton(Ic2Gui<?> gui, int x, int y, int width, int height, IClickHandler handler)
	{
		super(gui, x, y, width, height, handler);
	}

	private static void drawVerticalPiece(Ic2Gui<?> gui, PoseStack matrices, int x, int y, int width, int height, int u, int v)
	{
		int minTop = 2;
		int minBottom = 3;

		while (height < minTop + minBottom)
		{
			if (minTop > minBottom)
			{
				minTop--;
			} else
			{
				minBottom--;
			}
		}

		int cHeight = Math.min(height, 20) - minBottom;
		gui.drawTexturedRect(matrices, x, y, width, cHeight, u, v);
		y += cHeight;
		height -= cHeight;

		while (height > 20 - minTop)
		{
			cHeight = Math.min(height, 20 - minTop) - minBottom;
			gui.drawTexturedRect(matrices, x, y, width, cHeight, u, v + minTop);
			y += cHeight;
			height -= cHeight;
		}

		gui.drawTexturedRect(matrices, x, y, width, height, u, v + 20 - height);
	}

	public VanillaButton withDisableHandler(IEnableHandler handler)
	{
		this.disableHandler = handler;
		return this;
	}

	public boolean isDisabled()
	{
		return this.disableHandler != null && !this.disableHandler.isEnabled();
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		bindTexture(texture);
		int u;
		int v;
		if (this.isDisabled())
		{
			u = 0;
			v = 46;
		} else if (!this.isActive(mouseX, mouseY))
		{
			u = 0;
			v = 66;
		} else
		{
			u = 0;
			v = 86;
		}

		int minLeft = 2;
		int minRight = 2;

		while (this.width < minLeft + minRight)
		{
			if (minLeft > minRight)
			{
				minLeft--;
			} else
			{
				minRight--;
			}
		}

		int cx = this.x;
		int remainingWidth = this.width;
		int cWidth = Math.min(remainingWidth, 200) - minRight;
		drawVerticalPiece(this.gui, guiGraphics.pose(), cx, this.y, cWidth, this.height, u, v);
		cx += cWidth;
		remainingWidth -= cWidth;

		while (remainingWidth > 200 - minLeft)
		{
			cWidth = Math.min(remainingWidth, 200 - minLeft) - minRight;
			drawVerticalPiece(this.gui, guiGraphics.pose(), cx, this.y, cWidth, this.height, u + minLeft, v);
			cx += cWidth;
			remainingWidth -= cWidth;
		}

		drawVerticalPiece(this.gui, guiGraphics.pose(), cx, this.y, remainingWidth, this.height, u + 200 - remainingWidth, v);
		super.drawBackground(guiGraphics, mouseX, mouseY);
	}

	protected boolean isActive(int mouseX, int mouseY)
	{
		return this.contains(mouseX, mouseY);
	}

	@Override
	protected int getTextColor(int mouseX, int mouseY)
	{
		return this.isDisabled() ? 10526880 : (this.isActive(mouseX, mouseY) ? 16777120 : 14737632);
	}

	@Override
	protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button)
	{
		return this.isDisabled() ? false : super.onMouseClick(mouseX, mouseY, button);
	}
}
