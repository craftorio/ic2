// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiContainmentbox extends GuiIC2<ContainerContainmentbox>
{
    public GuiContainmentbox(final ContainerContainmentbox container) {
        super(container);
    }
    
    public ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIContainmentbox.png");
    }
}
