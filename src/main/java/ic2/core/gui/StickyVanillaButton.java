// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.GuiIC2;

public class StickyVanillaButton extends VanillaButton
{
    protected boolean isOn;
    
    public StickyVanillaButton(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final IClickHandler handler) {
        super(gui, x, y, width, height, handler);
        this.isOn = false;
    }
    
    public void setOn(final boolean on) {
        this.isOn = on;
    }
    
    public boolean isOn() {
        return this.isOn;
    }
    
    @Override
    public StickyVanillaButton withDisableHandler(final IEnableHandler handler) {
        super.withDisableHandler(handler);
        return this;
    }
    
    @Override
    public StickyVanillaButton withText(final String text) {
        super.withText(text);
        return this;
    }
    
    @Override
    public StickyVanillaButton withTooltip(final String tooltip) {
        super.withTooltip(tooltip);
        return this;
    }
    
    @Override
    protected boolean isActive(final int mouseX, final int mouseY) {
        return this.isOn || super.isActive(mouseX, mouseY);
    }
}
