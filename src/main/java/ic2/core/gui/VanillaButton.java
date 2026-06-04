// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.GuiIC2;
import net.minecraft.util.ResourceLocation;

public class VanillaButton extends Button<VanillaButton>
{
    protected IEnableHandler disableHandler;
    private static final ResourceLocation texture;
    private static final int uNormal = 0;
    private static final int vNormal = 66;
    private static final int uHover = 0;
    private static final int vHover = 86;
    private static final int uDisabled = 0;
    private static final int vDisabled = 46;
    private static final int rawWidth = 200;
    private static final int rawHeight = 20;
    private static final int minLeft = 2;
    private static final int minRight = 2;
    private static final int minTop = 2;
    private static final int minBottom = 3;
    private static final int colorNormal = 14737632;
    private static final int colorHover = 16777120;
    private static final int colorDisabled = 10526880;
    
    public VanillaButton(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final IClickHandler handler) {
        super(gui, x, y, width, height, handler);
    }
    
    public VanillaButton withDisableHandler(final IEnableHandler handler) {
        this.disableHandler = handler;
        return this;
    }
    
    public boolean isDisabled() {
        return this.disableHandler != null && !this.disableHandler.isEnabled();
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        GuiElement.bindTexture(VanillaButton.texture);
        int u;
        int v;
        if (this.isDisabled()) {
            u = 0;
            v = 46;
        }
        else if (!this.isActive(mouseX, mouseY)) {
            u = 0;
            v = 66;
        }
        else {
            u = 0;
            v = 86;
        }
        int minLeft = 2;
        int minRight = 2;
        while (this.width < minLeft + minRight) {
            if (minLeft > minRight) {
                --minLeft;
            }
            else {
                --minRight;
            }
        }
        int cx = this.x;
        int remainingWidth = this.width;
        int cWidth = Math.min(remainingWidth, 200) - minRight;
        drawVerticalPiece(this.gui, cx, this.y, cWidth, this.height, u, v);
        cx += cWidth;
        for (remainingWidth -= cWidth; remainingWidth > 200 - minLeft; remainingWidth -= cWidth) {
            cWidth = Math.min(remainingWidth, 200 - minLeft) - minRight;
            drawVerticalPiece(this.gui, cx, this.y, cWidth, this.height, u + minLeft, v);
            cx += cWidth;
        }
        drawVerticalPiece(this.gui, cx, this.y, remainingWidth, this.height, u + 200 - remainingWidth, v);
        super.drawBackground(mouseX, mouseY);
    }
    
    private static void drawVerticalPiece(final GuiIC2<?> gui, final int x, int y, final int width, int height, final int u, final int v) {
        int minTop = 2;
        int minBottom = 3;
        while (height < minTop + minBottom) {
            if (minTop > minBottom) {
                --minTop;
            }
            else {
                --minBottom;
            }
        }
        int cHeight = Math.min(height, 20) - minBottom;
        gui.drawTexturedRect(x, y, width, cHeight, u, v);
        y += cHeight;
        for (height -= cHeight; height > 20 - minTop; height -= cHeight) {
            cHeight = Math.min(height, 20 - minTop) - minBottom;
            gui.drawTexturedRect(x, y, width, cHeight, u, v + minTop);
            y += cHeight;
        }
        gui.drawTexturedRect(x, y, width, height, u, v + 20 - height);
    }
    
    protected boolean isActive(final int mouseX, final int mouseY) {
        return this.contains(mouseX, mouseY);
    }
    
    @Override
    protected int getTextColor(final int mouseX, final int mouseY) {
        return this.isDisabled() ? 10526880 : (this.isActive(mouseX, mouseY) ? 16777120 : 14737632);
    }
    
    @Override
    protected boolean onMouseClick(final int mouseX, final int mouseY, final MouseButton button) {
        return !this.isDisabled() && super.onMouseClick(mouseX, mouseY, button);
    }
    
    static {
        texture = new ResourceLocation("textures/gui/widgets.png");
    }
}
