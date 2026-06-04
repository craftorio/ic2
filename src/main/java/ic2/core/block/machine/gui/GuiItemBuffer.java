// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import ic2.core.block.machine.container.ContainerItemBuffer;
import ic2.core.GuiIC2;

public class GuiItemBuffer extends GuiIC2<ContainerItemBuffer>
{
    public GuiItemBuffer(final ContainerItemBuffer container) {
        super(container);
        this.ySize = 232;
    }
    
    public ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIItemBuffer.png");
    }
}
