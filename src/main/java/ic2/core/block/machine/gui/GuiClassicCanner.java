// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import ic2.core.gui.GuiElement;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiClassicCanner extends GuiIC2<ContainerClassicCanner>
{
    public static final ResourceLocation background;
    
    public GuiClassicCanner(final ContainerClassicCanner container) {
        super(container);
        this.addElement(new LinkedGauge(this, 74, 36, (IGuiValueProvider)container.base, "progress", Gauge.GaugeStyle.ProgressLongArrow));
        this.addElement(EnergyGauge.asBolt(this, 34, 28, (TileEntityBlock)container.base));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiClassicCanner.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUI_Canner_Classic.png");
    }
}
