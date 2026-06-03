package ic2.core.gui;

import ic2.core.GuiIC2;
import net.minecraft.util.ResourceLocation;

public class CustomButton extends Button<CustomButton> {
  private final ResourceLocation texture;
  
  private final IOverlaySupplier overlaySupplier;
  
  public CustomButton(GuiIC2<?> gui, int x, int y, int width, int height, IClickHandler handler) {
    this(gui, x, y, width, height, 0, 0, (ResourceLocation)null, handler);
  }
  
  public CustomButton(GuiIC2<?> gui, int x, int y, int width, int height, int overlayX, int overlayY, ResourceLocation texture, IClickHandler handler) {
    this(gui, x, y, width, height, new OverlaySupplier(overlayX, overlayY, overlayX + width, overlayY + height), texture, handler);
  }
  
  public CustomButton(GuiIC2<?> gui, int x, int y, int width, int height, IOverlaySupplier overlaySupplier, ResourceLocation texture, IClickHandler handler) {
    super(gui, x, y, width, height, handler);
    this.texture = texture;
    this.overlaySupplier = overlaySupplier;
  }
  
  public void drawBackground(int mouseX, int mouseY) {
    if (this.texture != null) {
      bindTexture(this.texture);
      double scale = 0.00390625D;
      this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, this.overlaySupplier.getUS() * scale, this.overlaySupplier.getVS() * scale, this.overlaySupplier.getUE() * scale, this.overlaySupplier.getVE() * scale, false);
    } 
    if (contains(mouseX, mouseY))
      this.gui.drawColoredRect(this.x, this.y, this.width, this.height, -2130706433); 
    super.drawBackground(mouseX, mouseY);
  }
}
