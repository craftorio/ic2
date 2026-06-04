// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.init.Localization;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.util.Util;
import ic2.core.GuiIC2;
import net.minecraftforge.fluids.IFluidTank;

public class TankGauge extends GuiElement<TankGauge>
{
    public static final int filledBackgroundU = 6;
    public static final int filledScaleU = 38;
    public static final int emptyU = 70;
    public static final int v = 100;
    public static final int normalWidth = 20;
    public static final int normalHeight = 55;
    public static final int fluidOffsetX = 4;
    public static final int fluidOffsetY = 4;
    public static final int fluidNetWidth = 12;
    public static final int fluidNetHeight = 47;
    private final IFluidTank tank;
    private final TankGuiStyle style;
    
    public static TankGauge createNormal(final GuiIC2<?> gui, final int x, final int y, final IFluidTank tank) {
        return new TankGauge(gui, x, y, 20, 55, tank, TankGuiStyle.Normal);
    }
    
    public static TankGauge createPlain(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final IFluidTank tank) {
        return new TankGauge(gui, x, y, width, height, tank, TankGuiStyle.Plain);
    }
    
    public static TankGauge createBorderless(final GuiIC2<?> gui, final int x, final int y, final IFluidTank tank, final boolean mirrored) {
        return new TankGauge(gui, x, y, 12, 47, tank, mirrored ? TankGuiStyle.BorderlessMirrored : TankGuiStyle.Borderless);
    }
    
    private TankGauge(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final IFluidTank tank, final TankGuiStyle style) {
        super(gui, x, y, width, height);
        if (tank == null) {
            throw new NullPointerException("null tank");
        }
        this.tank = tank;
        this.style = style;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        bindCommonTexture();
        final FluidStack fs = this.tank.getFluid();
        if (fs == null || fs.amount <= 0) {
            if (this.style.withBorder) {
                this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, 70.0, 100.0, this.style.mirrorGauge);
            }
            else if (this.style.withGauge) {
                this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, 74.0, 104.0, this.style.mirrorGauge);
            }
        }
        else {
            if (this.style.withBorder) {
                this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, 6.0, 100.0);
            }
            int fluidX = this.x;
            int fluidY = this.y;
            int fluidWidth = this.width;
            int fluidHeight = this.height;
            if (this.style.withBorder) {
                fluidX += 4;
                fluidY += 4;
                fluidWidth = 12;
                fluidHeight = 47;
            }
            final Fluid fluid = fs.getFluid();
            final TextureAtlasSprite sprite = (fluid != null) ? GuiElement.getBlockTextureMap().getAtlasSprite(fluid.getStill(fs).toString()) : null;
            final int color = (fluid != null) ? fluid.getColor(fs) : -1;
            final double renderHeight = fluidHeight * Util.limit(fs.amount / (double)this.tank.getCapacity(), 0.0, 1.0);
            bindBlockTexture();
            this.gui.drawSprite(fluidX, fluidY + fluidHeight - renderHeight, fluidWidth, renderHeight, sprite, color, 1.0, false, true);
            if (this.style.withGauge) {
                bindCommonTexture();
                int gaugeX = this.x;
                int gaugeY = this.y;
                if (!this.style.withBorder) {
                    gaugeX -= 4;
                    gaugeY -= 4;
                }
                this.gui.drawTexturedRect(gaugeX, gaugeY, 20.0, 55.0, 38.0, 100.0, this.style.mirrorGauge);
            }
        }
    }
    
    @Override
    protected List<String> getToolTip() {
        final List<String> ret = super.getToolTip();
        final FluidStack fs = this.tank.getFluid();
        if (fs == null || fs.amount <= 0) {
            ret.add(Localization.translate("ic2.generic.text.empty"));
        }
        else {
            final Fluid fluid = fs.getFluid();
            if (fluid != null) {
                ret.add(fluid.getLocalizedName(fs) + ": " + fs.amount + " " + Localization.translate("ic2.generic.text.mb"));
            }
            else {
                ret.add("invalid fluid stack");
            }
        }
        return ret;
    }
    
    private enum TankGuiStyle
    {
        Normal(true, true, false), 
        Borderless(false, true, false), 
        BorderlessMirrored(false, true, true), 
        Plain(false, false, false);
        
        public final boolean withBorder;
        public final boolean withGauge;
        public final boolean mirrorGauge;
        
        private TankGuiStyle(final boolean withBorder, final boolean withGauge, final boolean mirrorGauge) {
            this.withBorder = withBorder;
            this.withGauge = withGauge;
            this.mirrorGauge = mirrorGauge;
        }
    }
}
