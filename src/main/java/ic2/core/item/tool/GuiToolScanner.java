// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.util.ResourceLocation;
import java.util.Iterator;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import ic2.core.util.Tuple;
import ic2.core.init.Localization;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiToolScanner extends GuiIC2<ContainerToolScanner>
{
    public GuiToolScanner(final ContainerToolScanner container) {
        super(container, 230);
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(Localization.translate("ic2.itemScanner.found"), 10, 20, 2157374);
        if (((ContainerToolScanner)this.container).scanResults != null) {
            int count = 0;
            for (final Tuple.T2<ItemStack, Integer> result : ((ContainerToolScanner)this.container).scanResults) {
                final String name = result.a.getItem().getItemStackDisplayName((ItemStack)result.a);
                this.fontRenderer.drawString(result.b + "x " + name, 10, 34 + count * 11, 5752026);
                if (++count == 10) {
                    break;
                }
            }
            RenderHelper.disableStandardItemLighting();
        }
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(final float f, final int x, final int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        if (((ContainerToolScanner)this.container).scanResults != null) {
            int count = 0;
            RenderHelper.enableGUIStandardItemLighting();
            for (final Tuple.T2<ItemStack, Integer> result : ((ContainerToolScanner)this.container).scanResults) {
                final int xPos = 135 + (count & 0x1) * 15;
                this.drawItem(xPos, 11 * count + 28, result.a);
                if (++count == 10) {
                    break;
                }
            }
            RenderHelper.disableStandardItemLighting();
        }
    }
    
    public ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIToolScanner.png");
    }
}
