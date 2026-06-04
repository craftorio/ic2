// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.GuiIC2;
import net.minecraft.util.ResourceLocation;

public class Image extends GuiElement<Image>
{
    private final ResourceLocation texture;
    private final int baseWidth;
    private final int baseHeight;
    private final IOverlaySupplier overlay;
    private final boolean autoWidth;
    private final boolean autoHeight;
    
    public static Image create(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final ResourceLocation texture, final int baseWidth, final int baseHeight, final int uS, final int vS, final int uE, final int vE) {
        return create(gui, x, y, width, height, texture, baseWidth, baseHeight, new OverlaySupplier(uS, vS, uE, vE));
    }
    
    public static Image create(final GuiIC2<?> gui, final int x, final int y, int width, int height, final ResourceLocation texture, final int baseWidth, final int baseHeight, final IOverlaySupplier overlay) {
        final boolean autoWidth = width < 0;
        final boolean autoHeight = height < 0;
        if (autoWidth) {
            width = 0;
        }
        if (autoHeight) {
            height = 0;
        }
        return new Image(gui, x, y, width, height, texture, baseWidth, baseHeight, overlay, autoWidth, autoHeight);
    }
    
    protected Image(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final ResourceLocation texture, final int baseWidth, final int baseHeight, final IOverlaySupplier overlay, final boolean autoWidth, final boolean autoHeight) {
        super(gui, x, y, width, height);
        if (texture == null) {
            throw new NullPointerException("null texture");
        }
        if (overlay == null) {
            throw new NullPointerException("null overlay");
        }
        this.texture = texture;
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.overlay = overlay;
        this.autoWidth = autoWidth;
        this.autoHeight = autoHeight;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        super.drawBackground(mouseX, mouseY);
        final GlTexture texture = GlTexture.get(this.texture);
        if (texture != null) {
            if (this.autoWidth) {
                this.width = texture.getWidth();
            }
            if (this.autoHeight) {
                this.height = texture.getHeight();
            }
            final double widthScale = (this.baseWidth > 0) ? (1.0 / this.baseWidth) : (1.0 / texture.getCanvasWidth());
            final double heightScale = (this.baseHeight > 0) ? (1.0 / this.baseHeight) : (1.0 / texture.getCanvasHeight());
            final double uS = this.overlay.getUS();
            final double vS = this.overlay.getVS();
            double uE = this.overlay.getUE();
            if (uE < 0.0) {
                uE = uS + this.width;
            }
            double vE = this.overlay.getVE();
            if (vE < 0.0) {
                vE = vS + this.height;
            }
            texture.bind();
            this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, uS * widthScale, vS * heightScale, uE * widthScale, vE * heightScale, false);
        }
        else {
            if (this.autoWidth) {
                this.width = 0;
            }
            if (this.autoHeight) {
                this.height = 0;
            }
        }
    }
}
