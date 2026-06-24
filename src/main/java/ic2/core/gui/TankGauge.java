package ic2.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.proxy.SideProxyClient;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public class TankGauge extends GuiElement<TankGauge>
{
	public static final int v = 100;
	private final Ic2FluidTank tank;
	private final TankGauge.TankGuiStyle style;

	private TankGauge(Ic2Gui<?> gui, int x, int y, int width, int height, Ic2FluidTank tank, TankGauge.TankGuiStyle style)
	{
		super(gui, x, y, width, height);
		if (tank == null)
		{
			throw new NullPointerException("null tank");
		}

		this.tank = tank;
		this.style = style;
	}

	public static TankGauge createNormal(Ic2Gui<?> gui, int x, int y, Ic2FluidTank tank)
	{
		return new TankGauge(gui, x, y, 20, 55, tank, TankGauge.TankGuiStyle.Normal);
	}

	public static TankGauge createPlain(Ic2Gui<?> gui, int x, int y, int width, int height, Ic2FluidTank tank)
	{
		return new TankGauge(gui, x, y, width, height, tank, TankGauge.TankGuiStyle.Plain);
	}

	public static TankGauge createBorderless(Ic2Gui<?> gui, int x, int y, Ic2FluidTank tank, boolean mirrored)
	{
		return new TankGauge(gui, x, y, 12, 47, tank, mirrored ? TankGauge.TankGuiStyle.BorderlessMirrored : TankGauge.TankGuiStyle.Borderless);
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		bindCommonTexture();
		Ic2FluidStack fs = this.tank.getFluidStack();
		if (fs != null && !fs.isEmpty())
		{
			if (this.style.withBorder)
			{
				this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.width, this.height, 6.0, 100.0);
			}

			int fluidX = this.x;
			int fluidY = this.y;
			int fluidWidth = this.width;
			int fluidHeight = this.height;
			if (this.style.withBorder)
			{
				fluidX += 4;
				fluidY += 4;
				fluidWidth = 12;
				fluidHeight = 47;
			}

			TextureAtlasSprite sprite = SideProxyClient.envProxy.getFluidStillSprite(fs);
			int color = SideProxyClient.envProxy.getFluidColor(fs);
			double renderHeight = fluidHeight * Util.limit((double) fs.getAmountMb() / this.tank.getCapacity(), 0.0, 1.0);
			bindBlockTexture();
			this.gui.drawSprite(guiGraphics.pose(), fluidX, fluidY + fluidHeight - renderHeight, fluidWidth, renderHeight, sprite, color, 1.0, false, true);
			if (this.style.withGauge)
			{
				bindCommonTexture();
				int gaugeX = this.x;
				int gaugeY = this.y;
				if (!this.style.withBorder)
				{
					gaugeX -= 4;
					gaugeY -= 4;
				}

				this.gui.drawTexturedRect(guiGraphics.pose(), gaugeX, gaugeY, 20.0, 55.0, 38.0, 100.0, this.style.mirrorGauge);
			}
		} else if (this.style.withBorder)
		{
			this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.width, this.height, 70.0, 100.0, this.style.mirrorGauge);
		} else if (this.style.withGauge)
		{
			this.gui.drawTexturedRect(guiGraphics.pose(), this.x, this.y, this.width, this.height, 74.0, 104.0, this.style.mirrorGauge);
		}
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> ret = super.getToolTip();
		Ic2FluidStack fs = this.tank.getFluidStack();
		if (fs != null && !fs.isEmpty())
		{
			Fluid fluid = fs.getFluid();
			if (fluid != null)
			{
				ret.add(Component.literal(SideProxyClient.envProxy.getFluidName(fs) + ": " + fs.getAmountMb() + " " + Component.translatable("ic2.generic.text.mb").getString()));
			} else
			{
				ret.add(Component.literal("invalid fluid stack"));
			}
		} else
		{
			ret.add(Component.translatable("ic2.generic.text.empty"));
		}

		return ret;
	}

	private enum TankGuiStyle
	{
		Normal(true, true, false), Borderless(false, true, false), BorderlessMirrored(false, true, true), Plain(false, false, false);

		public final boolean withBorder;
		public final boolean withGauge;
		public final boolean mirrorGauge;

		TankGuiStyle(boolean withBorder, boolean withGauge, boolean mirrorGauge)
		{
			this.withBorder = withBorder;
			this.withGauge = withGauge;
			this.mirrorGauge = mirrorGauge;
		}
	}
}
