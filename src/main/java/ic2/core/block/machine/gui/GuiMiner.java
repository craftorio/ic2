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
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiMiner extends GuiIC2<ContainerMiner>
{
    public GuiMiner(final ContainerMiner container) {
        super(container);
        this.addElement(EnergyGauge.asBolt(this, 155, 41, (TileEntityBlock)container.base));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIMiner.png");
    }
}
