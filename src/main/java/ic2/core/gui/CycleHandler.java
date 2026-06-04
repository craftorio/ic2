// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

public class CycleHandler implements IClickHandler, IOverlaySupplier
{
    private final int uS;
    private final int vS;
    private final int uE;
    private final int vE;
    private final int overlayStep;
    private final boolean vertical;
    private final int options;
    private final INumericValueHandler handler;
    
    public CycleHandler(final int uS, final int vS, final int uE, final int vE, final int overlayStep, final boolean vertical, final int options, final INumericValueHandler handler) {
        this.uS = uS;
        this.vS = vS;
        this.uE = uE;
        this.vE = vE;
        this.overlayStep = overlayStep;
        this.vertical = vertical;
        this.options = options;
        this.handler = handler;
    }
    
    @Override
    public void onClick(final MouseButton button) {
        int value = this.getValue();
        if (button == MouseButton.left) {
            value = (value + 1) % this.options;
        }
        else {
            if (button != MouseButton.right) {
                return;
            }
            value = (value + this.options - 1) % this.options;
        }
        this.handler.onChange(value);
    }
    
    @Override
    public int getUS() {
        if (this.vertical) {
            return this.uS;
        }
        return this.uS + this.overlayStep * this.getValue();
    }
    
    @Override
    public int getVS() {
        if (!this.vertical) {
            return this.vS;
        }
        return this.vS + this.overlayStep * this.getValue();
    }
    
    @Override
    public int getUE() {
        if (this.vertical) {
            return this.uE;
        }
        return this.uS + this.overlayStep * (this.getValue() + 1);
    }
    
    @Override
    public int getVE() {
        if (!this.vertical) {
            return this.vE;
        }
        return this.vS + this.overlayStep * (this.getValue() + 1);
    }
    
    protected int getValue() {
        final int ret = this.handler.getValue();
        if (ret < 0 || ret >= this.options) {
            throw new RuntimeException("invalid value: " + ret);
        }
        return ret;
    }
}
