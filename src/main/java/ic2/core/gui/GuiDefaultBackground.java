package ic2.core.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public abstract class GuiDefaultBackground<T extends ContainerBase<? extends IInventory>> extends GuiIC2<T> {
  public GuiDefaultBackground(T container) {
    super((ContainerBase)container);
  }
  
  public GuiDefaultBackground(T container, int ySize) {
    super((ContainerBase)container, ySize);
  }
  
  public GuiDefaultBackground(T container, int xSize, int ySize) {
    super((ContainerBase)container, xSize, ySize);
  }
  
  protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY) {
    GuiElement.bindCommonTexture();
    drawTexturedRect(-16.0D, -16.0D, 32.0D, 32.0D, 0.0D, 0.0D);
    drawTexturedRect((this.xSize - 16), -16.0D, 32.0D, 32.0D, 64.0D, 0.0D);
    drawTexturedRect(-16.0D, (this.ySize - 16), 32.0D, 32.0D, 0.0D, 64.0D);
    drawTexturedRect((this.xSize - 16), (this.ySize - 16), 32.0D, 32.0D, 64.0D, 64.0D);
    int side;
    for (side = 0; side < 2; side++) {
      int i = this.ySize * side - 16;
      int v = 64 * side;
      for (int x = 16; x < this.xSize - 16; x += 32) {
        int width = Math.min(32, this.xSize - 16 - x);
        drawTexturedRect(x, i, width, 32.0D, 32.0D, v);
      } 
    } 
    for (side = 0; side < 2; side++) {
      int x = this.xSize * side - 16;
      int u = 64 * side;
      for (int i = 16; i < this.ySize - 16; i += 32) {
        int height = Math.min(32, this.ySize - 16 - i);
        drawTexturedRect(x, i, 32.0D, height, u, 32.0D);
      } 
    } 
    for (int y = 16; y < this.ySize - 16; y += 32) {
      int height = Math.min(32, this.ySize - 16 - y);
      for (int x = 16; x < this.xSize - 16; x += 32) {
        int width = Math.min(32, this.xSize - 16 - x);
        drawTexturedRect(x, y, width, height, 32.0D, 32.0D);
      } 
    } 
  }
  
  protected ResourceLocation getTexture() {
    return null;
  }
}
