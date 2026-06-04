// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.heatgenerator.gui;

import ic2.core.gui.GuiElement;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import com.google.common.base.Supplier;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import ic2.core.GuiIC2;

public class GuiRTHeatGenerator extends GuiIC2<ContainerRTHeatGenerator>
{
    private static final ResourceLocation background;
    
    public GuiRTHeatGenerator(final ContainerRTHeatGenerator container) {
        super(container);
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 49, 66, 79, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return ((TileEntityRTHeatGenerator)container.base).gettransmitHeat() + " / " + ((TileEntityRTHeatGenerator)container.base).getMaxHeatEmittedPerTick();
            }
        }), 5752026, false, 0, 0, true, true)).withTooltip("ic2.RTHeatGenerator.gui.tooltipheat"));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiRTHeatGenerator.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIRTHeatGenerator.png");
    }
}
