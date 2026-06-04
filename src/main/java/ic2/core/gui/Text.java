// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.ContainerBase;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import com.google.common.base.Suppliers;
import ic2.core.GuiIC2;
import com.google.common.base.Supplier;
import ic2.core.gui.dynamic.TextProvider;

public class Text extends GuiElement<Text>
{
    private final TextProvider.ITextProvider textProvider;
    private final Supplier<Integer> color;
    private final boolean shadow;
    private final boolean fixedHoverWidth;
    private final boolean fixedHoverHeight;
    private final int baseX;
    private final int baseY;
    private final boolean centerX;
    private final boolean centerY;
    
    public static Text create(final GuiIC2<?> gui, final int x, final int y, final String text, final int color, final boolean shadow) {
        return create(gui, x, y, TextProvider.of(text), color, shadow);
    }
    
    public static Text create(final GuiIC2<?> gui, final int x, final int y, final TextProvider.ITextProvider textProvider, final int color, final boolean shadow) {
        return create(gui, x, y, textProvider, color, shadow, false, false);
    }
    
    public static Text create(final GuiIC2<?> gui, final int x, final int y, final String text, final int color, final boolean shadow, final boolean centerX, final boolean centerY) {
        return create(gui, x, y, TextProvider.of(text), color, shadow, centerX, centerY);
    }
    
    public static Text create(final GuiIC2<?> gui, final int x, final int y, final TextProvider.ITextProvider textProvider, final int color, final boolean shadow, final boolean centerX, final boolean centerY) {
        return create(gui, x, y, -1, -1, textProvider, color, shadow, centerX, centerY);
    }
    
    public static Text create(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final TextProvider.ITextProvider textProvider, final int color, final boolean shadow, final boolean centerX, final boolean centerY) {
        return create(gui, x, y, width, height, textProvider, color, shadow, 0, 0, centerX, centerY);
    }
    
    public static Text create(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final TextProvider.ITextProvider textProvider, final int color, final boolean shadow, final int xOffset, final int yOffset, final boolean centerX, final boolean centerY) {
        return create(gui, x, y, width, height, textProvider, (Supplier<Integer>)Suppliers.ofInstance((Object)color), shadow, xOffset, yOffset, centerX, centerY);
    }
    
    public static Text createRightAligned(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final TextProvider.ITextProvider textProvider, final int color, final boolean shadow, final int xOffset, final int yOffset, final boolean centerX, final boolean centerY) {
        return create(gui, x, y, width, height, textProvider, (Supplier<Integer>)Suppliers.ofInstance((Object)color), shadow, xOffset - getWidth(gui, textProvider), yOffset, centerX, centerY);
    }
    
    public static Text create(final GuiIC2<?> gui, int x, int y, int width, int height, final TextProvider.ITextProvider textProvider, final Supplier<Integer> color, final boolean shadow, final int xOffset, final int yOffset, final boolean centerX, final boolean centerY) {
        boolean fixedHoverWidth;
        if (width < 0) {
            fixedHoverWidth = false;
            width = getWidth(gui, textProvider);
        }
        else {
            fixedHoverWidth = true;
        }
        boolean fixedHoverHeight;
        if (height < 0) {
            fixedHoverHeight = false;
            height = 8;
        }
        else {
            fixedHoverHeight = true;
        }
        int baseX = x + xOffset;
        int baseY = y + yOffset;
        if (centerX) {
            if (fixedHoverWidth) {
                baseX += width / 2;
            }
            else {
                x -= width / 2;
            }
        }
        if (centerY) {
            if (fixedHoverHeight) {
                baseY += (height + 1) / 2;
            }
            else {
                y -= height / 2;
            }
        }
        return new Text(gui, x, y, width, height, textProvider, color, shadow, fixedHoverWidth, fixedHoverHeight, baseX, baseY, centerX, centerY);
    }
    
    private Text(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final TextProvider.ITextProvider textProvider, final Supplier<Integer> color, final boolean shadow, final boolean fixedHoverWidth, final boolean fixedHoverHeight, final int baseX, final int baseY, final boolean centerX, final boolean centerY) {
        super(gui, x, y, width, height);
        this.textProvider = textProvider;
        this.color = color;
        this.shadow = shadow;
        this.fixedHoverWidth = fixedHoverWidth;
        this.fixedHoverHeight = fixedHoverHeight;
        this.baseX = baseX;
        this.baseY = baseY;
        this.centerX = centerX;
        this.centerY = centerY;
    }
    
    private static int getWidth(final GuiIC2<?> gui, final TextProvider.ITextProvider textProvider) {
        final String text = textProvider.get(((ContainerBase)gui.getContainer()).base, TextProvider.emptyTokens());
        if (text.isEmpty()) {
            return 0;
        }
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        final String text = this.textProvider.get(this.getBase(), this.getTokens());
        int textWidth;
        int textHeight;
        if (text.isEmpty()) {
            textHeight = (textWidth = 0);
        }
        else {
            textWidth = this.gui.getStringWidth(text);
            textHeight = 8;
        }
        int textX = this.baseX;
        if (this.centerX) {
            textX -= textWidth / 2;
        }
        int textY = this.baseY;
        if (this.centerY) {
            textY -= textHeight / 2;
        }
        if (!this.fixedHoverWidth) {
            this.x = textX;
            this.width = textWidth;
        }
        if (!this.fixedHoverHeight) {
            this.y = textY;
            this.height = textHeight;
        }
        super.drawBackground(mouseX, mouseY);
        if (!text.isEmpty()) {
            this.gui.drawString(textX, textY, text, (int)this.color.get(), this.shadow);
        }
    }
    
    public enum TextAlignment
    {
        Start, 
        Center, 
        End;
        
        private static final Map<String, TextAlignment> map;
        public final String name;
        
        private TextAlignment() {
            this.name = this.name().toLowerCase(Locale.ENGLISH);
        }
        
        public static TextAlignment get(final String name) {
            return TextAlignment.map.get(name);
        }
        
        private static Map<String, TextAlignment> getMap() {
            final TextAlignment[] values = values();
            final Map<String, TextAlignment> ret = new HashMap<String, TextAlignment>(values.length);
            for (final TextAlignment style : values) {
                ret.put(style.name, style);
            }
            return ret;
        }
        
        static {
            map = getMap();
        }
    }
}
