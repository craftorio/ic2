package ic2.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.init.Localization;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public abstract class AbstractFluidSlot extends GuiElement<TankFluidSlot>
{
	public static final int posU = 8;
	public static final int posV = 160;
	public static final int normalWidth = 18;
	public static final int normalHeight = 18;
	public static final int fluidOffsetX = 1;
	public static final int fluidOffsetY = 1;
	public static final int fluidNetWidth = 16;
	public static final int fluidNetHeight = 16;

	protected AbstractFluidSlot(Ic2Gui<?> gui, int x, int y, int width, int height)
	{
		super(gui, x, y, width, height);
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		bindCommonTexture();
		Ic2FluidStack fs = this.getFluidStack();
		this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.width, this.height, 8.0, 160.0);
		if (fs != null && !fs.isEmpty())
		{
			int fluidX = this.x + 1;
			int fluidY = this.y + 1;
			int fluidWidth = 16;
			int fluidHeight = 16;
			Fluid fluid = fs.getFluid();
			TextureAtlasSprite sprite = fluid != null ? getBlockTextureMap().getSprite(FluidHandler.getStillSpriteId(fluid)) : null;
			int color = fluid != null ? FluidHandler.getColor(fluid) : -1;
			bindBlockTexture();
			this.gui.drawSprite(guiGraphics.pose(), fluidX, fluidY, fluidWidth, fluidHeight, sprite, color, 1.0, false, false);
		}
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> ret = super.getToolTip();
		Ic2FluidStack fs = this.getFluidStack();
		if (fs != null && !fs.isEmpty())
		{
			Fluid fluid = fs.getFluid();
			if (fluid != null)
			{
				ret.add(Component.translatable(fs.getFluidTypeKey()));
				ret.add(Component.translatable("ic2.generic.text.amount", fs.getAmountMb()));
				String translateKey = FluidHandler.isGaseous(fluid) ? "ic2.generic.text.gas" : "ic2.generic.text.liquid";
				ret.add(Component.translatable("ic2.generic.text.state", Component.translatable(translateKey)));
			} else
			{
				ret.add(Component.literal("Invalid FluidStack instance."));
			}
		} else
		{
			ret.add(Component.translatable("ic2.generic.text.no_fluid"));
		}

		return ret;
	}

	protected abstract Ic2FluidStack getFluidStack();
}
