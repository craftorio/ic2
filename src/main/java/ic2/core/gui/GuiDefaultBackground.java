// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import net.minecraft.util.ResourceLocation;
import ic2.core.GuiIC2;
import net.minecraft.inventory.IInventory;
import ic2.core.ContainerBase;

public abstract class GuiDefaultBackground<T extends ContainerBase<? extends IInventory>> extends GuiIC2<T>
{
    public GuiDefaultBackground(final T container) {
        super(container);
    }
    
    public GuiDefaultBackground(final T container, final int ySize) {
        super(container, ySize);
    }
    
    public GuiDefaultBackground(final T container, final int xSize, final int ySize) {
        super(container, xSize, ySize);
    }
    
    @Override
    protected void drawBackgroundAndTitle(final float partialTicks, final int mouseX, final int mouseY) {
        GuiElement.bindCommonTexture();
        this.drawTexturedRect(-16.0, -16.0, 32.0, 32.0, 0.0, 0.0);
        this.drawTexturedRect(this.xSize - 16, -16.0, 32.0, 32.0, 64.0, 0.0);
        this.drawTexturedRect(-16.0, this.ySize - 16, 32.0, 32.0, 0.0, 64.0);
        this.drawTexturedRect(this.xSize - 16, this.ySize - 16, 32.0, 32.0, 64.0, 64.0);
        for (int side = 0; side < 2; ++side) {
            final int y = this.ySize * side - 16;
            final int v = 64 * side;
            for (int x = 16; x < this.xSize - 16; x += 32) {
                final int width = Math.min(32, this.xSize - 16 - x);
                this.drawTexturedRect(x, y, width, 32.0, 32.0, v);
            }
        }
        for (int side = 0; side < 2; ++side) {
            final int x2 = this.xSize * side - 16;
            final int u = 64 * side;
            for (int y2 = 16; y2 < this.ySize - 16; y2 += 32) {
                final int height = Math.min(32, this.ySize - 16 - y2);
                this.drawTexturedRect(x2, y2, 32.0, height, u, 32.0);
            }
        }
        for (int y3 = 16; y3 < this.ySize - 16; y3 += 32) {
            final int height2 = Math.min(32, this.ySize - 16 - y3);
            for (int x3 = 16; x3 < this.xSize - 16; x3 += 32) {
                final int width2 = Math.min(32, this.xSize - 16 - x3);
                this.drawTexturedRect(x3, y3, width2, height2, 32.0, 32.0);
            }
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return null;
    }
}
