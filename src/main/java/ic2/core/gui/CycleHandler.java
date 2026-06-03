package ic2.core.gui;

public class CycleHandler implements IClickHandler, IOverlaySupplier {
  private final int uS;
  
  private final int vS;
  
  private final int uE;
  
  private final int vE;
  
  private final int overlayStep;
  
  private final boolean vertical;
  
  private final int options;
  
  private final INumericValueHandler handler;
  
  public CycleHandler(int uS, int vS, int uE, int vE, int overlayStep, boolean vertical, int options, INumericValueHandler handler) {
    this.uS = uS;
    this.vS = vS;
    this.uE = uE;
    this.vE = vE;
    this.overlayStep = overlayStep;
    this.vertical = vertical;
    this.options = options;
    this.handler = handler;
  }
  
  public void onClick(MouseButton button) {
    int value = getValue();
    if (button == MouseButton.left) {
      value = (value + 1) % this.options;
    } else if (button == MouseButton.right) {
      value = (value + this.options - 1) % this.options;
    } else {
      return;
    } 
    this.handler.onChange(value);
  }
  
  public int getUS() {
    if (this.vertical)
      return this.uS; 
    return this.uS + this.overlayStep * getValue();
  }
  
  public int getVS() {
    if (!this.vertical)
      return this.vS; 
    return this.vS + this.overlayStep * getValue();
  }
  
  public int getUE() {
    if (this.vertical)
      return this.uE; 
    return this.uS + this.overlayStep * (getValue() + 1);
  }
  
  public int getVE() {
    if (!this.vertical)
      return this.vE; 
    return this.vS + this.overlayStep * (getValue() + 1);
  }
  
  protected int getValue() {
    int ret = this.handler.getValue();
    if (ret < 0 || ret >= this.options)
      throw new RuntimeException("invalid value: " + ret); 
    return ret;
  }
}
