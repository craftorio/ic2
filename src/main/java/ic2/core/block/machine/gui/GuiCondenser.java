// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.gui.LinkedGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import ic2.core.gui.SlotGrid;
import ic2.core.init.Localization;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import com.google.common.base.Supplier;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.machine.container.ContainerCondenser;
import ic2.core.GuiIC2;

public class GuiCondenser extends GuiIC2<ContainerCondenser>
{
    private static final ResourceLocation BACKGROUND;
    
    public GuiCondenser(final ContainerCondenser container) {
        super(container, 184);
        final Supplier<String> ventTooltipSupplier = (Supplier<String>)new Supplier<String>() {
            public String get() {
                final String key = "ic2.Condenser.gui.tooltipvent";
                final Object[] args = { null };
                final int n = 0;
                ((TileEntityCondenser)container.base).getClass();
                args[n] = 2;
                return Localization.translate(key, args);
            }
        };
        this.addElement(((GuiElement<GuiElement<?>>)new SlotGrid(this, 25, 25, 1, 2, SlotGrid.SlotStyle.Normal)).withTooltip(ventTooltipSupplier));
        this.addElement(((GuiElement<GuiElement<?>>)new SlotGrid(this, 133, 25, 1, 2, SlotGrid.SlotStyle.Normal)).withTooltip(ventTooltipSupplier));
        this.addElement(EnergyGauge.asBolt(this, 12, 26, (TileEntityBlock)container.base));
        this.addElement(TankGauge.createPlain(this, 46, 27, 84, 33, (IFluidTank)((TileEntityCondenser)container.base).getInputTank()));
        this.addElement(TankGauge.createPlain(this, 46, 74, 84, 15, (IFluidTank)((TileEntityCondenser)container.base).getOutputTank()));
        this.addElement(new LinkedGauge(this, 47, 63, (IGuiValueProvider)container.base, "progress", Gauge.GaugeStyle.ProgressCondenser));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiCondenser.BACKGROUND;
    }
    
    static {
        BACKGROUND = new ResourceLocation("ic2", "textures/gui/GUICondenser.png");
    }
}
