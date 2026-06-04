// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.init.Localization;
import com.google.common.base.Supplier;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.machine.container.ContainerFermenter;
import ic2.core.GuiIC2;

public class GuiFermenter extends GuiIC2<ContainerFermenter>
{
    private static final ResourceLocation TEXTURE;
    
    public GuiFermenter(final ContainerFermenter container) {
        super(container, 184);
        this.addElement(TankGauge.createPlain(this, 38, 49, 48, 30, (IFluidTank)((TileEntityFermenter)container.base).getInputTank()));
        this.addElement(TankGauge.createNormal(this, 125, 22, (IFluidTank)((TileEntityFermenter)container.base).getOutputTank()));
        this.addElement(((GuiElement<GuiElement<?>>)new LinkedGauge(this, 42, 41, (IGuiValueProvider)container.base, "heat", Gauge.GaugeStyle.HeatFermenter)).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.Fermenter.gui.info.conversion") + " " + (int)(((TileEntityFermenter)container.base).getGuiValue("heat") * 100.0) + "%";
            }
        }));
        this.addElement(((GuiElement<GuiElement<?>>)new LinkedGauge(this, 38, 88, (IGuiValueProvider)container.base, "progress", Gauge.GaugeStyle.ProgressFermenter)).withTooltip("ic2.Fermenter.gui.info.waste"));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiFermenter.TEXTURE;
    }
    
    static {
        TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIFermenter.png");
    }
}
