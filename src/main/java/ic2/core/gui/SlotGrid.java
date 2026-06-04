// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import java.util.HashMap;
import java.util.Locale;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import net.minecraft.inventory.Slot;
import ic2.core.util.StackUtil;
import ic2.core.GuiIC2;

public class SlotGrid extends GuiElement<SlotGrid>
{
    private final SlotStyle style;
    private final int border;
    private final int spacing;
    
    public SlotGrid(final GuiIC2<?> gui, final int x, final int y, final SlotStyle style) {
        this(gui, x, y, 1, 1, style);
    }
    
    public SlotGrid(final GuiIC2<?> gui, final int x, final int y, final int xCount, final int yCount, final SlotStyle style) {
        this(gui, x, y, xCount, yCount, style, 0, 0);
    }
    
    public SlotGrid(final GuiIC2<?> gui, final int x, final int y, final SlotStyle style, final int border) {
        this(gui, x, y, 1, 1, style, border, 0);
    }
    
    public SlotGrid(final GuiIC2<?> gui, final int x, final int y, final int xCount, final int yCount, final SlotStyle style, final int border, final int spacing) {
        super(gui, x - border, y - border, xCount * style.width + 2 * border + (xCount - 1) * spacing, yCount * style.height + 2 * border + (yCount - 1) * spacing);
        this.style = style;
        this.border = border;
        this.spacing = spacing;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        super.drawBackground(mouseX, mouseY);
        if (this.style.background != null) {
            GuiElement.bindTexture(this.style.background);
            final int startX = this.x + this.border;
            final int startY = this.y + this.border;
            final int maxX = this.x + this.width - this.border;
            final int maxY = this.y + this.height - this.border;
            final int xStep = this.style.width + this.spacing;
            for (int yStep = this.style.height + this.spacing, cy = startY; cy < maxY; cy += yStep) {
                for (int cx = startX; cx < maxX; cx += xStep) {
                    this.gui.drawTexturedRect(cx, cy, this.style.width, this.style.height, this.style.u, this.style.v);
                }
            }
        }
    }
    
    @Override
    protected boolean suppressTooltip(final int mouseX, final int mouseY) {
        if (!StackUtil.isEmpty(this.gui.mc.player.inventory.getItemStack())) {
            return false;
        }
        final Slot slot = this.gui.getSlotUnderMouse();
        return slot != null && slot.getHasStack();
    }
    
    public static final class SlotStyle
    {
        public static final SlotStyle Normal;
        public static final SlotStyle Large;
        public static final SlotStyle Plain;
        private static final Map<String, SlotStyle> map;
        public static final int refSize = 16;
        public final int u;
        public final int v;
        public final int width;
        public final int height;
        public final ResourceLocation background;
        
        public SlotStyle(final int u, final int v, final int width, final int height) {
            this(u, v, width, height, GuiElement.commonTexture);
        }
        
        public SlotStyle(final int width, final int height) {
            this(0, 0, width, height, null);
        }
        
        public SlotStyle(final int u, final int v, final int width, final int height, final ResourceLocation background) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
            this.background = background;
        }
        
        public static void registerVarient(final String name, final SlotStyle newSlotStyle) {
            assert name != null && newSlotStyle != null;
            final SlotStyle old = SlotStyle.map.put(name.toLowerCase(Locale.ENGLISH), newSlotStyle);
            if (old != null) {
                throw new RuntimeException("Duplicate slot instance for name! " + name + " -> " + old + " and " + newSlotStyle);
            }
        }
        
        public static SlotStyle get(final String name) {
            return SlotStyle.map.get(name);
        }
        
        private static Map<String, SlotStyle> getMap() {
            final Map<String, SlotStyle> ret = new HashMap<String, SlotStyle>(6, 0.5f);
            ret.put("normal", SlotStyle.Normal);
            ret.put("large", SlotStyle.Large);
            ret.put("plain", SlotStyle.Plain);
            return ret;
        }
        
        static {
            Normal = new SlotStyle(103, 7, 18, 18);
            Large = new SlotStyle(99, 35, 26, 26);
            Plain = new SlotStyle(16, 16);
            map = getMap();
        }
    }
}
