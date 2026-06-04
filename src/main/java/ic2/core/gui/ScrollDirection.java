// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

public enum ScrollDirection
{
    stopped(0), 
    up(-1), 
    down(1);
    
    public final byte multiplier;
    
    private ScrollDirection(final int multiplier) {
        this.multiplier = (byte)multiplier;
    }
}
