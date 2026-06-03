package ic2.core.gui;

public abstract class FixedSizeOverlaySupplier implements IOverlaySupplier {
  private final int width;
  
  private final int height;
  
  public FixedSizeOverlaySupplier(int size) {
    this(size, size);
  }
  
  public FixedSizeOverlaySupplier(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  public int getUE() {
    return getUS() + this.width;
  }
  
  public int getVE() {
    return getVS() + this.height;
  }
}
