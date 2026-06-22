package ic2.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import net.minecraft.resources.ResourceLocation;

public class Image extends GuiElement<Image>
{
	private final ResourceLocation texture;
	private final int baseWidth;
	private final int baseHeight;
	private final IOverlaySupplier overlay;
	private final boolean autoWidth;
	private final boolean autoHeight;

	protected Image(
		Ic2Gui<?> gui,
		int x,
		int y,
		int width,
		int height,
		ResourceLocation texture,
		int baseWidth,
		int baseHeight,
		IOverlaySupplier overlay,
		boolean autoWidth,
		boolean autoHeight
	)
	{
		super(gui, x, y, width, height);
		if (texture == null)
		{
			throw new NullPointerException("null texture");
		}

		if (overlay == null)
		{
			throw new NullPointerException("null overlay");
		}

		this.texture = texture;
		this.baseWidth = baseWidth;
		this.baseHeight = baseHeight;
		this.overlay = overlay;
		this.autoWidth = autoWidth;
		this.autoHeight = autoHeight;
	}

	public static Image create(
		Ic2Gui<?> gui, int x, int y, int width, int height, ResourceLocation texture, int baseWidth, int baseHeight, int uS, int vS, int uE, int vE
	)
	{
		return create(gui, x, y, width, height, texture, baseWidth, baseHeight, new OverlaySupplier(uS, vS, uE, vE));
	}

	public static Image create(
		Ic2Gui<?> gui, int x, int y, int width, int height, ResourceLocation texture, int baseWidth, int baseHeight, IOverlaySupplier overlay
	)
	{
		boolean autoWidth = width < 0;
		boolean autoHeight = height < 0;
		if (autoWidth)
		{
			width = 0;
		}

		if (autoHeight)
		{
			height = 0;
		}

		return new Image(gui, x, y, width, height, texture, baseWidth, baseHeight, overlay, autoWidth, autoHeight);
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawBackground(guiGraphics, mouseX, mouseY);
		GlTexture texture = GlTexture.get(this.texture);
		if (texture != null)
		{
			if (this.autoWidth)
			{
				this.width = texture.getWidth();
			}

			if (this.autoHeight)
			{
				this.height = texture.getHeight();
			}

			double widthScale = this.baseWidth > 0 ? 1.0 / this.baseWidth : 1.0 / texture.getCanvasWidth();
			double heightScale = this.baseHeight > 0 ? 1.0 / this.baseHeight : 1.0 / texture.getCanvasHeight();
			double uS = this.overlay.getUS();
			double vS = this.overlay.getVS();
			double uE = this.overlay.getUE();
			if (uE < 0.0)
			{
				uE = uS + this.width;
			}

			double vE = this.overlay.getVE();
			if (vE < 0.0)
			{
				vE = vS + this.height;
			}

			RenderSystem.setShaderTexture(0, this.texture);
			this.gui
				.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.width, this.height, uS * widthScale, vS * heightScale, uE * widthScale, vE * heightScale, false);
		} else
		{
			if (this.autoWidth)
			{
				this.width = 0;
			}

			if (this.autoHeight)
			{
				this.height = 0;
			}
		}
	}
}
