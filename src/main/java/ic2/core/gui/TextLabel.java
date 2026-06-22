package ic2.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.gui.dynamic.TextProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntSupplier;

import net.minecraft.client.Minecraft;

public class TextLabel extends GuiElement<TextLabel>
{
	private final TextProvider.ITextProvider textProvider;
	private final IntSupplier color;
	private final boolean shadow;
	private final boolean fixedHoverWidth;
	private final boolean fixedHoverHeight;
	private final int baseX;
	private final int baseY;
	private final boolean centerX;
	private final boolean centerY;

	private TextLabel(
		Ic2Gui<?> gui,
		int x,
		int y,
		int width,
		int height,
		TextProvider.ITextProvider textProvider,
		IntSupplier color,
		boolean shadow,
		boolean fixedHoverWidth,
		boolean fixedHoverHeight,
		int baseX,
		int baseY,
		boolean centerX,
		boolean centerY
	)
	{
		super(gui, x, y, width, height);
		this.textProvider = textProvider;
		this.color = color;
		this.shadow = shadow;
		this.fixedHoverWidth = fixedHoverWidth;
		this.fixedHoverHeight = fixedHoverHeight;
		this.baseX = baseX;
		this.baseY = baseY;
		this.centerX = centerX;
		this.centerY = centerY;
	}

	public static TextLabel create(Ic2Gui<?> gui, int x, int y, String text, int color, boolean shadow)
	{
		return create(gui, x, y, TextProvider.of(text), color, shadow);
	}

	public static TextLabel create(Ic2Gui<?> gui, int x, int y, TextProvider.ITextProvider textProvider, int color, boolean shadow)
	{
		return create(gui, x, y, textProvider, color, shadow, false, false);
	}

	public static TextLabel create(Ic2Gui<?> gui, int x, int y, String text, int color, boolean shadow, boolean centerX, boolean centerY)
	{
		return create(gui, x, y, TextProvider.of(text), color, shadow, centerX, centerY);
	}

	public static TextLabel create(
		Ic2Gui<?> gui, int x, int y, TextProvider.ITextProvider textProvider, int color, boolean shadow, boolean centerX, boolean centerY
	)
	{
		return create(gui, x, y, -1, -1, textProvider, color, shadow, centerX, centerY);
	}

	public static TextLabel create(
		Ic2Gui<?> gui, int x, int y, int width, int height, TextProvider.ITextProvider textProvider, int color, boolean shadow, boolean centerX, boolean centerY
	)
	{
		return create(gui, x, y, width, height, textProvider, color, shadow, 0, 0, centerX, centerY);
	}

	public static TextLabel create(
		Ic2Gui<?> gui,
		int x,
		int y,
		int width,
		int height,
		TextProvider.ITextProvider textProvider,
		int color,
		boolean shadow,
		int xOffset,
		int yOffset,
		boolean centerX,
		boolean centerY
	)
	{
		return create(gui, x, y, width, height, textProvider, () -> color, shadow, xOffset, yOffset, centerX, centerY);
	}

	public static TextLabel createRightAligned(
		Ic2Gui<?> gui,
		int x,
		int y,
		int width,
		int height,
		TextProvider.ITextProvider textProvider,
		int color,
		boolean shadow,
		int xOffset,
		int yOffset,
		boolean centerX,
		boolean centerY
	)
	{
		return create(gui, x, y, width, height, textProvider, () -> color, shadow, xOffset - getWidth(gui, textProvider), yOffset, centerX, centerY);
	}

	public static TextLabel create(
		Ic2Gui<?> gui,
		int x,
		int y,
		int width,
		int height,
		TextProvider.ITextProvider textProvider,
		IntSupplier color,
		boolean shadow,
		int xOffset,
		int yOffset,
		boolean centerX,
		boolean centerY
	)
	{
		boolean fixedHoverWidth;
		if (width < 0)
		{
			fixedHoverWidth = false;
			width = getWidth(gui, textProvider);
		} else
		{
			fixedHoverWidth = true;
		}

		boolean fixedHoverHeight;
		if (height < 0)
		{
			fixedHoverHeight = false;
			height = 8;
		} else
		{
			fixedHoverHeight = true;
		}

		int baseX = x + xOffset;
		int baseY = y + yOffset;
		if (centerX)
		{
			if (fixedHoverWidth)
			{
				baseX += width / 2;
			} else
			{
				x -= width / 2;
			}
		}

		if (centerY)
		{
			if (fixedHoverHeight)
			{
				baseY += (height + 1) / 2;
			} else
			{
				y -= height / 2;
			}
		}

		return new TextLabel(gui, x, y, width, height, textProvider, color, shadow, fixedHoverWidth, fixedHoverHeight, baseX, baseY, centerX, centerY);
	}

	private static int getWidth(Ic2Gui<?> gui, TextProvider.ITextProvider textProvider)
	{
		String text = textProvider.get(gui.getContainer().base, TextProvider.emptyTokens());
		return text.isEmpty() ? 0 : Minecraft.getInstance().font.width(text);
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		String text = this.textProvider.get(this.getBase(), this.getTokens());
		int textWidth;
		int textHeight;
		if (text.isEmpty())
		{
			textHeight = 0;
			textWidth = 0;
		} else
		{
			textWidth = this.gui.getStringWidth(text);
			textHeight = 8;
		}

		int textX = this.baseX;
		if (this.centerX)
		{
			textX -= textWidth / 2;
		}

		int textY = this.baseY;
		if (this.centerY)
		{
			textY -= textHeight / 2;
		}

		if (!this.fixedHoverWidth)
		{
			this.x = textX;
			this.width = textWidth;
		}

		if (!this.fixedHoverHeight)
		{
			this.y = textY;
			this.height = textHeight;
		}

		super.drawBackground(guiGraphics, mouseX, mouseY);
		if (!text.isEmpty())
		{
			this.gui.drawString(guiGraphics, textX, textY, text, this.color.getAsInt(), this.shadow);
		}
	}

	public enum TextAlignment
	{
		Start,
		Center,
		End;

		private static final Map<String, TextLabel.TextAlignment> map = getMap();
		public final String name = this.name().toLowerCase(Locale.ENGLISH);

		public static TextLabel.TextAlignment get(String name)
		{
			return map.get(name);
		}

		private static Map<String, TextLabel.TextAlignment> getMap()
		{
			TextLabel.TextAlignment[] values = values();
			Map<String, TextLabel.TextAlignment> ret = new HashMap<>(values.length);

			for (TextLabel.TextAlignment style : values)
			{
				ret.put(style.name, style);
			}

			return ret;
		}
	}
}
