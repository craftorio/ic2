// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerClassicCropmatron;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiClassicCropmatron extends GuiIC2<ContainerClassicCropmatron>
{
    private static final ResourceLocation background;
    
    public GuiClassicCropmatron(final ContainerClassicCropmatron container) {
        super(container);
        this.addElement(EnergyGauge.asBolt(this, 29, 39, (TileEntityBlock)container.base));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiClassicCropmatron.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUI_Cropmatron_Classic.png");
    }
}
