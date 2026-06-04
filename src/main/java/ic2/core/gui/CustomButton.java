// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.GuiIC2;
import net.minecraft.util.ResourceLocation;

public class CustomButton extends Button<CustomButton>
{
    private final ResourceLocation texture;
    private final IOverlaySupplier overlaySupplier;
    
    public CustomButton(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final IClickHandler handler) {
        this(gui, x, y, width, height, 0, 0, null, handler);
    }
    
    public CustomButton(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final int overlayX, final int overlayY, final ResourceLocation texture, final IClickHandler handler) {
        this(gui, x, y, width, height, new OverlaySupplier(overlayX, overlayY, overlayX + width, overlayY + height), texture, handler);
    }
    
    public CustomButton(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final IOverlaySupplier overlaySupplier, final ResourceLocation texture, final IClickHandler handler) {
        super(gui, x, y, width, height, handler);
        this.texture = texture;
        this.overlaySupplier = overlaySupplier;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        if (this.texture != null) {
            GuiElement.bindTexture(this.texture);
            final double scale = 0.00390625;
            this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, this.overlaySupplier.getUS() * scale, this.overlaySupplier.getVS() * scale, this.overlaySupplier.getUE() * scale, this.overlaySupplier.getVE() * scale, false);
        }
        if (this.contains(mouseX, mouseY)) {
            this.gui.drawColoredRect(this.x, this.y, this.width, this.height, -2130706433);
        }
        super.drawBackground(mouseX, mouseY);
    }
}
