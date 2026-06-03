package ic2.core.gui;

import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Process;
import ic2.core.util.Util;

public class ProgressGauge extends GuiElement<ProgressGauge> {
  private final Process process;
  
  private final ProgressBarType type;
  
  public ProgressGauge(GuiIC2<?> gui, int x, int y, TileEntityBlock te, ProgressBarType type) {
    super(gui, x, y, type.w, type.h);
    this.type = type;
    this.process = (Process)te.getComponent(Process.class);
  }
  
  public void drawBackground(int mouseX, int mouseY) {
    bindCommonTexture();
    this.gui.drawTexturedRect(this.x, this.y, this.type.w, this.type.h, this.type.emptyX, this.type.emptyY);
    int renderWidth = Util.limit((int)Math.round(getProgressRatio() * this.type.w), 0, this.type.w);
    if (renderWidth > 0)
      this.gui.drawTexturedRect(this.x, this.y, renderWidth, this.type.h, this.type.fullX, this.type.fullY); 
  }
  
  protected double getProgressRatio() {
    return this.process.getProgressRatio();
  }
  
  public enum ProgressBarType {
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
    
    ProgressBarType(int emptyX, int emptyY, int fullX, int fullY, int w, int h) {
      this.emptyX = emptyX;
      this.emptyY = emptyY;
      this.fullX = fullX;
      this.fullY = fullY;
      this.w = w;
      this.h = h;
    }
  }
}
