package ic2.core.gui;

import ic2.core.GuiIC2;
import ic2.core.util.StackUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class SlotGrid extends GuiElement<SlotGrid> {
  private final SlotStyle style;
  
  private final int border;
  
  private final int spacing;
  
  public SlotGrid(GuiIC2<?> gui, int x, int y, SlotStyle style) {
    this(gui, x, y, 1, 1, style);
  }
  
  public SlotGrid(GuiIC2<?> gui, int x, int y, int xCount, int yCount, SlotStyle style) {
    this(gui, x, y, xCount, yCount, style, 0, 0);
  }
  
  public SlotGrid(GuiIC2<?> gui, int x, int y, SlotStyle style, int border) {
    this(gui, x, y, 1, 1, style, border, 0);
  }
  
  public SlotGrid(GuiIC2<?> gui, int x, int y, int xCount, int yCount, SlotStyle style, int border, int spacing) {
    super(gui, x - border, y - border, xCount * style.width + 2 * border + (xCount - 1) * spacing, yCount * style.height + 2 * border + (yCount - 1) * spacing);
    this.style = style;
    this.border = border;
    this.spacing = spacing;
  }
  
  public void drawBackground(int mouseX, int mouseY) {
    super.drawBackground(mouseX, mouseY);
    if (this.style.background != null) {
      bindTexture(this.style.background);
      int startX = this.x + this.border;
      int startY = this.y + this.border;
      int maxX = this.x + this.width - this.border;
      int maxY = this.y + this.height - this.border;
      int xStep = this.style.width + this.spacing;
      int yStep = this.style.height + this.spacing;
      int cy;
      for (cy = startY; cy < maxY; cy += yStep) {
        int cx;
        for (cx = startX; cx < maxX; cx += xStep)
          this.gui.drawTexturedRect(cx, cy, this.style.width, this.style.height, this.style.u, this.style.v); 
      } 
    } 
  }
  
  protected boolean suppressTooltip(int mouseX, int mouseY) {
    if (!StackUtil.isEmpty(this.gui.field_146297_k.field_71439_g.field_71071_by.func_70445_o()))
      return false; 
    Slot slot = this.gui.getSlotUnderMouse();
    return (slot != null && slot.func_75216_d());
  }
  
  public static final class SlotStyle {
    public SlotStyle(int u, int v, int width, int height) {
      this(u, v, width, height, GuiElement.commonTexture);
    }
    
    public SlotStyle(int width, int height) {
      this(0, 0, width, height, null);
    }
    
    public SlotStyle(int u, int v, int width, int height, ResourceLocation background) {
      this.u = u;
      this.v = v;
      this.width = width;
      this.height = height;
      this.background = background;
    }
    
    public static void registerVarient(String name, SlotStyle newSlotStyle) {
      assert name != null && newSlotStyle != null;
      SlotStyle old = map.put(name.toLowerCase(Locale.ENGLISH), newSlotStyle);
      if (old != null)
        throw new RuntimeException("Duplicate slot instance for name! " + name + " -> " + old + " and " + newSlotStyle); 
    }
    
    public static SlotStyle get(String name) {
      return map.get(name);
    }
    
    private static Map<String, SlotStyle> getMap() {
      Map<String, SlotStyle> ret = new HashMap<>(6, 0.5F);
      ret.put("normal", Normal);
      ret.put("large", Large);
      ret.put("plain", Plain);
      return ret;
    }
    
    public static final SlotStyle Normal = new SlotStyle(103, 7, 18, 18);
    
    public static final SlotStyle Large = new SlotStyle(99, 35, 26, 26);
    
    public static final SlotStyle Plain = new SlotStyle(16, 16);
    
    private static final Map<String, SlotStyle> map = getMap();
    
    public static final int refSize = 16;
    
    public final int u;
    
    public final int v;
    
    public final int width;
    
    public final int height;
    
    public final ResourceLocation background;
  }
}
