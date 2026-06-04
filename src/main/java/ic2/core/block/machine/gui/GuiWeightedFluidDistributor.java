// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.GuiIC2;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;

@SideOnly(Side.CLIENT)
public class GuiWeightedFluidDistributor extends GuiWeightedDistributor<ContainerWeightedFluidDistributor>
{
    private static final ResourceLocation TEXTURE;
    
    public GuiWeightedFluidDistributor(final ContainerWeightedFluidDistributor container) {
        super(container, 211);
        this.addElement(TankGauge.createPlain(this, 33, 111, 110, 10, (IFluidTank)((TileEntityWeightedFluidDistributor)container.base).fluidTank));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiWeightedFluidDistributor.TEXTURE;
    }
    
    static {
        TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIWeightedFluidDistributor.png");
    }
}
