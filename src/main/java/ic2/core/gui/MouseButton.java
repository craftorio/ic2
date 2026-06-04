// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

public enum MouseButton
{
    left(0), 
    right(1);
    
    public final int id;
    private static final MouseButton[] map;
    
    private MouseButton(final int id) {
        this.id = id;
    }
    
    public static MouseButton get(final int id) {
        if (id < 0 || id >= MouseButton.map.length) {
            return null;
        }
        return MouseButton.map[id];
    }
    
    private static MouseButton[] createMap() {
        final MouseButton[] values = values();
        int max = -1;
        for (final MouseButton button : values) {
            if (button.id > max) {
                max = button.id;
            }
        }
        if (max < 0) {
            return new MouseButton[0];
        }
        final MouseButton[] ret = new MouseButton[max + 1];
        for (final MouseButton button2 : values) {
            ret[button2.id] = button2;
        }
        return ret;
    }
    
    static {
        map = createMap();
    }
}
