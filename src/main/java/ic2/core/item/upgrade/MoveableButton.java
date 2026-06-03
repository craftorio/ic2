package ic2.core.item.upgrade;

import ic2.core.GuiIC2;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.VanillaButton;

class MoveableButton extends VanillaButton {
  private IEnableHandler moveHandler;
  
  protected int normalX;
  
  protected int normalY;
  
  protected int shiftedX;
  
  protected int shiftedY;
  
  public MoveableButton(GuiIC2<?> gui, int normalX, int normalY, int shiftedX, int shiftedY, int width, int height, IClickHandler handler) {
    super(gui, normalX, normalY, width, height, handler);
    this.normalX = normalX;
    this.normalY = normalY;
    this.shiftedX = shiftedX;
    this.shiftedY = shiftedY;
  }
  
  public MoveableButton withMoveHandler(IEnableHandler moveHandler) {
    this.moveHandler = moveHandler;
    return this;
  }
  
  public boolean isMoved() {
    return (this.moveHandler != null && this.moveHandler.isEnabled());
  }
  
  public void tick() {
    super.tick();
    if (isMoved()) {
      this.x = this.shiftedX;
      this.y = this.shiftedY;
    } else {
      this.x = this.normalX;
      this.y = this.normalY;
    } 
  }
}
