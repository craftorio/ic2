package ic2.jeiIntegration;

import ic2.core.gui.Text;
import ic2.core.init.Localization;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;

public class TextDrawable implements IDrawable {
  private final String text;
  
  private final Text.TextAlignment alignment;
  
  private final int color;
  
  public TextDrawable(String text, Text.TextAlignment alignment, int color) {
    this.text = text;
    this.alignment = alignment;
    this.color = color;
  }
  
  public void draw(Minecraft arg0) {
    int x;
    switch (this.alignment) {
      case Start:
        x = 0;
        break;
      case Center:
        x = arg0.field_71462_r.field_146294_l / 2;
        break;
      case End:
        x = arg0.field_71462_r.field_146294_l - getWidth();
        break;
      default:
        throw new IllegalArgumentException("invalid alignment: " + this.alignment);
    } 
    arg0.field_71466_p.func_78276_b(Localization.translate(this.text), x, 0, this.color);
  }
  
  public void draw(Minecraft arg0, int arg1, int arg2) {}
  
  public int getHeight() {
    return 12;
  }
  
  public int getWidth() {
    return (Minecraft.func_71410_x()).field_71466_p.func_78256_a(Localization.translate(this.text));
  }
}
