// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.gui;

import net.minecraft.util.ResourceLocation;
import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import ic2.core.GuiIC2;

public class GuiStirlingKineticGenerator extends GuiIC2<ContainerStirlingKineticGenerator>
{
    public GuiStirlingKineticGenerator(final ContainerStirlingKineticGenerator container) {
        super(container, 204);
        this.addElement(TankGauge.createPlain(this, 19, 47, 12, 44, (IFluidTank)((TileEntityStirlingKineticGenerator)container.base).getInputTank()));
        this.addElement(TankGauge.createPlain(this, 145, 47, 12, 44, (IFluidTank)((TileEntityStirlingKineticGenerator)container.base).getOutputTank()));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIStirlingKineticGenerator.png");
    }
}
