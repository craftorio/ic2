// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.upgrade;

import ic2.core.GuiIC2;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.TextBox;

class MoveableTextBox extends TextBox
{
    private IEnableHandler moveHandler;
    protected int normalX;
    protected int normalY;
    protected int shiftedX;
    protected int shiftedY;
    
    public MoveableTextBox(final GuiIC2<?> gui, final int normalX, final int normalY, final int shiftedX, final int shiftedY, final int width, final int height, final String text) {
        super(gui, normalX, normalY, width, height, text);
        this.normalX = normalX;
        this.normalY = normalY;
        this.shiftedX = shiftedX;
        this.shiftedY = shiftedY;
    }
    
    public MoveableTextBox withMoveHandler(final IEnableHandler moveHandler) {
        this.moveHandler = moveHandler;
        return this;
    }
    
    public boolean isMoved() {
        return this.moveHandler != null && this.moveHandler.isEnabled();
    }
    
    @Override
    public void tick() {
        super.tick();
        if (this.isMoved()) {
            this.x = this.shiftedX;
            this.y = this.shiftedY;
        }
        else {
            this.x = this.normalX;
            this.y = this.normalY;
        }
    }
}
