// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerCropHarvester;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiCropHarvester extends GuiIC2<ContainerCropHarvester>
{
    public GuiCropHarvester(final ContainerCropHarvester container) {
        super(container);
        this.addElement(EnergyGauge.asBolt(this, 19, 37, (TileEntityBlock)container.base));
    }
    
    public ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUICropHarvester.png");
    }
}
