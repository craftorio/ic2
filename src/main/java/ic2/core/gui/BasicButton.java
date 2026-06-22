package ic2.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;

public class BasicButton extends Button<BasicButton>
{
	private final BasicButton.ButtonStyle style;

	protected BasicButton(Ic2Gui<?> gui, int x, int y, IClickHandler handler, BasicButton.ButtonStyle style)
	{
		super(gui, x, y, style.width, style.height, handler);
		this.style = style;
	}

	public static BasicButton create(Ic2Gui<?> gui, int x, int y, IClickHandler handler, BasicButton.ButtonStyle style)
	{
		return new BasicButton(gui, x, y, handler, style);
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		bindCommonTexture();
		this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.style.width, this.style.height, this.style.u, this.style.v);
		super.drawBackground(guiGraphics, mouseX, mouseY);
	}

	public enum ButtonStyle
	{
		AdvMinerReset(192, 32, 36, 15),
		AdvMinerMode(228, 32, 18, 15),
		AdvMinerSilkTouch(192, 47, 18, 15);

		final int u;
		final int v;
		final int width;
		final int height;

		ButtonStyle(int u, int v, int width, int height)
		{
			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
		}
	}
}
