// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.init.Localization;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.GuiIC2;
import net.minecraftforge.fluids.IFluidTank;

public class FluidSlot extends GuiElement<FluidSlot>
{
    public static final int posU = 8;
    public static final int posV = 160;
    public static final int normalWidth = 18;
    public static final int normalHeight = 18;
    public static final int fluidOffsetX = 1;
    public static final int fluidOffsetY = 1;
    public static final int fluidNetWidth = 16;
    public static final int fluidNetHeight = 16;
    private final IFluidTank tank;
    
    public static FluidSlot createFluidSlot(final GuiIC2<?> gui, final int x, final int y, final IFluidTank tank) {
        return new FluidSlot(gui, x, y, 18, 18, tank);
    }
    
    protected FluidSlot(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final IFluidTank tank) {
        super(gui, x, y, width, height);
        if (tank == null) {
            throw new NullPointerException("Null FluidTank instance.");
        }
        this.tank = tank;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        bindCommonTexture();
        final FluidStack fs = this.tank.getFluid();
        this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, 8.0, 160.0);
        if (fs != null && fs.amount > 0) {
            final int fluidX = this.x + 1;
            final int fluidY = this.y + 1;
            final int fluidWidth = 16;
            final int fluidHeight = 16;
            final Fluid fluid = fs.getFluid();
            final TextureAtlasSprite sprite = (fluid != null) ? GuiElement.getBlockTextureMap().getAtlasSprite(fluid.getStill(fs).toString()) : null;
            final int color = (fluid != null) ? fluid.getColor(fs) : -1;
            bindBlockTexture();
            this.gui.drawSprite(fluidX, fluidY, fluidWidth, fluidHeight, sprite, color, 1.0, false, false);
        }
    }
    
    @Override
    protected List<String> getToolTip() {
        final List<String> ret = super.getToolTip();
        final FluidStack fs = this.tank.getFluid();
        if (fs == null || fs.amount <= 0) {
            ret.add("No Fluid");
            ret.add("Amount: 0 " + Localization.translate("ic2.generic.text.mb"));
            ret.add("Type: Not Available");
        }
        else {
            final Fluid fluid = fs.getFluid();
            if (fluid != null) {
                ret.add(fluid.getLocalizedName(fs));
                ret.add("Amount: " + fs.amount + " " + Localization.translate("ic2.generic.text.mb"));
                final String state = fs.getFluid().isGaseous() ? "Gas" : "Liquid";
                ret.add("Type: " + state);
            }
            else {
                ret.add("Invalid FluidStack instance.");
            }
        }
        return ret;
    }
}
