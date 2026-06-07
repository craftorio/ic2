package ic2.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import net.minecraft.resources.ResourceLocation;

public class CustomButton extends Button<CustomButton>
{
	private final ResourceLocation texture;
	private final IOverlaySupplier overlaySupplier;

	public CustomButton(Ic2Gui<?> gui, int x, int y, int width, int height, IClickHandler handler)
	{
		this(gui, x, y, width, height, 0, 0, null, handler);
	}

	public CustomButton(Ic2Gui<?> gui, int x, int y, int width, int height, int overlayX, int overlayY, ResourceLocation texture, IClickHandler handler)
	{
		this(gui, x, y, width, height, new OverlaySupplier(overlayX, overlayY, overlayX + width, overlayY + height), texture, handler);
	}

	public CustomButton(Ic2Gui<?> gui, int x, int y, int width, int height, IOverlaySupplier overlaySupplier, ResourceLocation texture, IClickHandler handler)
	{
		super(gui, x, y, width, height, handler);
		this.texture = texture;
		this.overlaySupplier = overlaySupplier;
	}

	@Override
	public void drawBackground(PoseStack matrices, int mouseX, int mouseY)
	{
		if (this.texture != null)
		{
			bindTexture(this.texture);
			double scale = 0.00390625;
			this.gui
				.drawTexturedRect(
					matrices,
					this.x,
					this.y,
					this.width,
					this.height,
					this.overlaySupplier.getUS() * scale,
					this.overlaySupplier.getVS() * scale,
					this.overlaySupplier.getUE() * scale,
					this.overlaySupplier.getVE() * scale,
					false
				);
		}

		if (this.contains(mouseX, mouseY))
		{
			this.gui.drawColoredRect(matrices, this.x, this.y, this.width, this.height, -2130706433);
		}

		super.drawBackground(matrices, mouseX, mouseY);
	}
}
