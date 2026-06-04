// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration;

import ic2.core.init.Localization;
import net.minecraft.client.Minecraft;
import ic2.core.gui.Text;
import mezz.jei.api.gui.IDrawable;

public class TextDrawable implements IDrawable
{
    private final String text;
    private final Text.TextAlignment alignment;
    private final int color;
    
    public TextDrawable(final String text, final Text.TextAlignment alignment, final int color) {
        this.text = text;
        this.alignment = alignment;
        this.color = color;
    }
    
    public void draw(final Minecraft arg0) {
        int x = 0;
        switch (this.alignment) {
            case Start: {
                x = 0;
                break;
            }
            case Center: {
                x = arg0.currentScreen.width / 2;
                break;
            }
            case End: {
                x = arg0.currentScreen.width - this.getWidth();
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid alignment: " + this.alignment);
            }
        }
        arg0.fontRenderer.drawString(Localization.translate(this.text), x, 0, this.color);
    }
    
    public void draw(final Minecraft arg0, final int arg1, final int arg2) {
    }
    
    public int getHeight() {
        return 12;
    }
    
    public int getWidth() {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(Localization.translate(this.text));
    }
}
