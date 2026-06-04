// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.GuiIC2;

public class BasicButton extends Button<BasicButton>
{
    private final ButtonStyle style;
    
    public static BasicButton create(final GuiIC2<?> gui, final int x, final int y, final IClickHandler handler, final ButtonStyle style) {
        return new BasicButton(gui, x, y, handler, style);
    }
    
    protected BasicButton(final GuiIC2<?> gui, final int x, final int y, final IClickHandler handler, final ButtonStyle style) {
        super(gui, x, y, style.width, style.height, handler);
        this.style = style;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        bindCommonTexture();
        this.gui.drawTexturedRect(this.x, this.y, this.style.width, this.style.height, this.style.u, this.style.v);
        super.drawBackground(mouseX, mouseY);
    }
    
    public enum ButtonStyle
    {
        AdvMinerReset(192, 32, 36, 15), 
        AdvMinerMode(228, 32, 18, 15), 
        AdvMinerSilkTouch(192, 47, 18, 15);
        
        final int u;
        final int v;
        final int width;
        final int height;
        
        private ButtonStyle(final int u, final int v, final int width, final int height) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }
    }
}
