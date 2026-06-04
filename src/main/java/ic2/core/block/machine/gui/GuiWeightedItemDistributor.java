// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import ic2.core.block.machine.container.ContainerWeightedItemDistributor;

public class GuiWeightedItemDistributor extends GuiWeightedDistributor<ContainerWeightedItemDistributor>
{
    private static final ResourceLocation TEXTURE;
    
    public GuiWeightedItemDistributor(final ContainerWeightedItemDistributor container) {
        super(container, 211);
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiWeightedItemDistributor.TEXTURE;
    }
    
    static {
        TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIWeightedItemDistributor.png");
    }
}
