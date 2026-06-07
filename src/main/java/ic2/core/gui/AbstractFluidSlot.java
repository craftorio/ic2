package ic2.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
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
	public void drawBackground(PoseStack matrices, int mouseX, int mouseY)
	{
		bindCommonTexture();
		Ic2FluidStack fs = this.getFluidStack();
		this.gui.drawTexturedRect(matrices, this.x, this.y, this.width, this.height, 8.0, 160.0);
		if (fs != null && !fs.isEmpty())
		{
			int fluidX = this.x + 1;
			int fluidY = this.y + 1;
			int fluidWidth = 16;
			int fluidHeight = 16;
			Fluid fluid = fs.getFluid();
			TextureAtlasSprite sprite = fluid != null ? getBlockTextureMap().m_118316_(FluidHandler.getStillSpriteId(fluid)) : null;
			int color = fluid != null ? FluidHandler.getColor(fluid) : -1;
			bindBlockTexture();
			this.gui.drawSprite(matrices, fluidX, fluidY, fluidWidth, fluidHeight, sprite, color, 1.0, false, false);
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
				ret.add(Component.m_237113_(Util.getName(fs.getFluid()).toString()));
				ret.add(Component.m_237113_("Amount: " + fs.getAmountMb() + " " + Localization.translate("ic2.generic.text.mb")));
				String state = FluidHandler.isGaseous(fluid) ? "Gas" : "Liquid";
				ret.add(Component.m_237113_("Type: " + state));
			} else
			{
				ret.add(Component.m_237113_("Invalid FluidStack instance."));
			}
		} else
		{
			ret.add(Component.m_237113_("No Fluid"));
			ret.add(Component.m_237113_("Amount: 0 " + Localization.translate("ic2.generic.text.mb")));
			ret.add(Component.m_237113_("Type: Not Available"));
		}

		return ret;
	}

	protected abstract Ic2FluidStack getFluidStack();
}
