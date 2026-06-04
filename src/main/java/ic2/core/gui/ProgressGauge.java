// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.util.Util;
import ic2.core.block.TileEntityBlock;
import ic2.core.GuiIC2;
import ic2.core.block.comp.Process;

public class ProgressGauge extends GuiElement<ProgressGauge>
{
    private final Process process;
    private final ProgressBarType type;
    
    public ProgressGauge(final GuiIC2<?> gui, final int x, final int y, final TileEntityBlock te, final ProgressBarType type) {
        super(gui, x, y, type.w, type.h);
        this.type = type;
        this.process = te.getComponent(Process.class);
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        bindCommonTexture();
        this.gui.drawTexturedRect(this.x, this.y, this.type.w, this.type.h, this.type.emptyX, this.type.emptyY);
        final int renderWidth = Util.limit((int)Math.round(this.getProgressRatio() * this.type.w), 0, this.type.w);
        if (renderWidth > 0) {
            this.gui.drawTexturedRect(this.x, this.y, renderWidth, this.type.h, this.type.fullX, this.type.fullY);
        }
    }
    
    protected double getProgressRatio() {
        return this.process.getProgressRatio();
    }
    
    public enum ProgressBarType
    {
        type_1(165, 0, 165, 16, 22, 15), 
        type_2(165, 35, 165, 52, 21, 11), 
        type_3(165, 64, 165, 80, 22, 15), 
        type_4(165, 96, 165, 112, 22, 15), 
        type_5(133, 64, 133, 80, 18, 15);
        
        private int emptyX;
        private int emptyY;
        private int fullX;
        private int fullY;
        private int w;
        private int h;
        
        private ProgressBarType(final int emptyX, final int emptyY, final int fullX, final int fullY, final int w, final int h) {
            this.emptyX = emptyX;
            this.emptyY = emptyY;
            this.fullX = fullX;
            this.fullY = fullY;
            this.w = w;
            this.h = h;
        }
    }
}
