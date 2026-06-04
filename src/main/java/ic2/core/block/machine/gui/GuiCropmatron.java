// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntityCropmatron;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiCropmatron extends GuiIC2<ContainerCropmatron>
{
    public GuiCropmatron(final ContainerCropmatron container) {
        super(container, 192);
        this.addElement(EnergyGauge.asBolt(this, 138, 82, (TileEntityBlock)container.base));
        this.addElement(TankGauge.createPlain(this, 11, 26, 24, 47, (IFluidTank)((TileEntityCropmatron)container.base).getWaterTank()));
        this.addElement(TankGauge.createPlain(this, 105, 26, 24, 47, (IFluidTank)((TileEntityCropmatron)container.base).getExTank()));
    }
    
    public ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUICropmatron.png");
    }
}
