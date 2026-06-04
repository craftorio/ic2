// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.gui;

import ic2.core.ContainerBase;
import ic2.core.gui.Area;
import ic2.core.gui.GuiElement;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import com.google.common.base.Supplier;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.gui.IEnableHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiNuclearReactor extends GuiIC2<ContainerNuclearReactor>
{
    private static final ResourceLocation background;
    private static final ResourceLocation backgroundFluid;
    
    public GuiNuclearReactor(final ContainerNuclearReactor container) {
        super(container, 212, 243);
        final IEnableHandler enableHandler = new IEnableHandler() {
            @Override
            public boolean isEnabled() {
                return ((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).isFluidCooled();
            }
        };
        this.addElement(((GuiElement<GuiElement<?>>)TankGauge.createBorderless(this, 10, 54, (IFluidTank)((TileEntityNuclearReactorElectric)container.base).getinputtank(), true)).withEnableHandler(enableHandler));
        this.addElement(((GuiElement<GuiElement<?>>)TankGauge.createBorderless(this, 190, 54, (IFluidTank)((TileEntityNuclearReactorElectric)container.base).getoutputtank(), false)).withEnableHandler(enableHandler));
        this.addElement(((GuiElement<GuiElement<?>>)new LinkedGauge(this, 7, 136, (IGuiValueProvider)container.base, "heat", Gauge.GaugeStyle.HeatNuclearReactor)).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.NuclearReactor.gui.info.temp", ((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).getGuiValue("heat") * 100.0);
            }
        }));
        this.addElement(Text.create(this, 107, 136, 200, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                if (((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).isFluidCooled()) {
                    return Localization.translate("ic2.NuclearReactor.gui.info.HUoutput", ((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).EmitHeat);
                }
                return Localization.translate("ic2.NuclearReactor.gui.info.EUoutput", Math.round(((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).getOfferedEnergy()));
            }
        }), 5752026, false, 4, 0, false, true));
        this.addElement(((GuiElement<GuiElement<?>>)new Area(this, 5, 160, 18, 18)).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                if (((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).isFluidCooled()) {
                    return "ic2.NuclearReactor.gui.mode.fluid";
                }
                return "ic2.NuclearReactor.gui.mode.electric";
            }
        }));
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        final int size = ((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)this.container).base).getReactorSize();
        final int startX = 26;
        final int startY = 25;
        this.bindTexture();
        for (int y = 0; y < 6; ++y) {
            for (int x = size; x < 9; ++x) {
                this.drawTexturedRect(26 + x * 18, 25 + y * 18, 16.0, 16.0, 213.0, 1.0);
            }
        }
        if (((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)this.container).base).isFluidCooled()) {
            final int heat = ((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)this.container).base).gaugeHeatScaled(160);
            this.drawTexturedRect(186 - heat, 23.0, 0.0, 243.0, heat, 2.0);
            this.drawTexturedRect(186 - heat, 41.0, 0.0, 243.0, heat, 2.0);
            this.drawTexturedRect(186 - heat, 59.0, 0.0, 243.0, heat, 2.0);
            this.drawTexturedRect(186 - heat, 77.0, 0.0, 243.0, heat, 2.0);
            this.drawTexturedRect(186 - heat, 95.0, 0.0, 243.0, heat, 2.0);
            this.drawTexturedRect(186 - heat, 113.0, 0.0, 243.0, heat, 2.0);
            this.drawTexturedRect(186 - heat, 131.0, 0.0, 243.0, heat, 2.0);
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        if (((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)this.container).base).isFluidCooled()) {
            return GuiNuclearReactor.backgroundFluid;
        }
        return GuiNuclearReactor.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUINuclearReactor.png");
        backgroundFluid = new ResourceLocation("ic2", "textures/gui/GUINuclearReactorFluid.png");
    }
}
