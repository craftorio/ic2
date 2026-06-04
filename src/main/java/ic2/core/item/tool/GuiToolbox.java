// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.gui.GuiElement;
import ic2.core.gui.Text;
import ic2.core.ref.ItemName;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiToolbox extends GuiIC2<ContainerToolbox>
{
    private static final ResourceLocation background;
    
    public GuiToolbox(final ContainerToolbox container) {
        super(container);
        this.addElement(Text.create(this, 65, 11, ItemName.tool_box.getItemStack().getDisplayName(), 0, false));
    }
    
    @Override
    protected void drawBackgroundAndTitle(final float partialTicks, final int mouseX, final int mouseY) {
        this.bindTexture();
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiToolbox.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIToolbox.png");
    }
}
