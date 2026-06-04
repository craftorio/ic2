// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration;

import ic2.core.gui.SlotGrid;

public class SlotPosition
{
    private final int x;
    private final int y;
    private final SlotGrid.SlotStyle style;
    
    public SlotPosition(final int x, final int y) {
        this(x, y, SlotGrid.SlotStyle.Normal);
    }
    
    public SlotPosition(final SlotPosition old, final int x, final int y) {
        this(old.x + x, old.y + y, old.style);
    }
    
    public SlotPosition(final int x, final int y, final SlotGrid.SlotStyle style) {
        this.x = x;
        this.y = y;
        this.style = style;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public SlotGrid.SlotStyle getStyle() {
        return this.style;
    }
}
