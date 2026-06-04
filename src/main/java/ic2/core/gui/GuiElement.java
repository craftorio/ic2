package ic2.core.gui;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import ic2.core.GuiIC2;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public abstract class GuiElement<T extends GuiElement<T>> {
  protected static final int hoverColor = -2130706433;
  
  protected GuiElement(GuiIC2<?> gui, int x, int y, int width, int height) {
    if (width < 0)
      throw new IllegalArgumentException("negative width"); 
    if (height < 0)
      throw new IllegalArgumentException("negative height"); 
    this.gui = gui;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  
  public final boolean isEnabled() {
    return (this.enableHandler == null || this.enableHandler.isEnabled());
  }
  
  public boolean contains(int x, int y) {
    return (x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height);
  }
  
  public T withEnableHandler(IEnableHandler enableHandler) {
    this.enableHandler = enableHandler;
    return (T)this;
  }
  
  public T withTooltip(String tooltip) {
    return withTooltip(Suppliers.ofInstance(tooltip));
  }
  
  public T withTooltip(Supplier<String> tooltipProvider) {
    this.tooltipProvider = tooltipProvider;
    return (T)this;
  }
  
  public void tick() {}
  
  public void drawBackground(int mouseX, int mouseY) {}
  
  public void drawForeground(int mouseX, int mouseY) {
    if (contains(mouseX, mouseY) && !suppressTooltip(mouseX, mouseY)) {
      List<String> lines = getToolTip();
      if (this.tooltipProvider != null) {
        String tooltip = (String)this.tooltipProvider.get();
        if (tooltip != null && !tooltip.isEmpty())
          addLines(lines, tooltip); 
      } 
      if (!lines.isEmpty())
        this.gui.drawTooltip(mouseX, mouseY, lines); 
    } 
  }
  
  private static void addLines(List<String> list, String str) {
    int startPos = 0;
    int pos;
    while ((pos = str.indexOf('\n', startPos)) != -1) {
      list.add(processText(str.substring(startPos, pos)));
      startPos = pos + 1;
    } 
    if (startPos == 0) {
      list.add(processText(str));
    } else {
      list.add(processText(str.substring(startPos)));
    } 
  }
  
  public boolean onMouseClick(int mouseX, int mouseY, MouseButton button, boolean onThis) {
    return (onThis && onMouseClick(mouseX, mouseY, button));
  }
  
  protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button) {
    return false;
  }
  
  public boolean onMouseDrag(int mouseX, int mouseY, MouseButton button, long timeFromLastClick, boolean onThis) {
    return (onThis && onMouseDrag(mouseX, mouseY, button, timeFromLastClick));
  }
  
  protected boolean onMouseDrag(int mouseX, int mouseY, MouseButton button, long timeFromLastClick) {
    return false;
  }
  
  public boolean onMouseRelease(int mouseX, int mouseY, MouseButton button, boolean onThis) {
    return (onThis && onMouseRelease(mouseX, mouseY, button));
  }
  
  protected boolean onMouseRelease(int mouseX, int mouseY, MouseButton button) {
    return false;
  }
  
  public void onMouseScroll(int mouseX, int mouseY, ScrollDirection direction) {}
  
  public boolean onKeyTyped(char typedChar, int keyCode) {
    return false;
  }
  
  protected boolean suppressTooltip(int mouseX, int mouseY) {
    return false;
  }
  
  protected List<String> getToolTip() {
    return new ArrayList<>();
  }
  
  protected static String processText(String text) {
    return Localization.translate(text);
  }
  
  protected final IInventory getBase() {
    return (this.gui.getContainer()).base;
  }
  
  protected final Map<String, TextProvider.ITextProvider> getTokens() {
    Map<String, TextProvider.ITextProvider> ret = new HashMap<>();
    ret.put("name", TextProvider.ofTranslated(getBase().func_70005_c_()));
    return ret;
  }
  
  protected static void bindTexture(ResourceLocation texture) {
    (Minecraft.getMinecraft()).field_71446_o.func_110577_a(texture);
  }
  
  public static void bindCommonTexture() {
    (Minecraft.getMinecraft()).field_71446_o.func_110577_a(commonTexture);
  }
  
  protected static void bindBlockTexture() {
    (Minecraft.getMinecraft()).field_71446_o.func_110577_a(TextureMap.field_110575_b);
  }
  
  protected static TextureMap getBlockTextureMap() {
    return Minecraft.getMinecraft().func_147117_R();
  }
  
  private static final Method hasMethod(Class<?> cls, String name, Class<?>... params) {
    try {
      return !cls.getDeclaredMethod(name, params).isAnnotationPresent((Class)SkippedMethod.class) ? Method.PRESENT : Method.SKIPPED;
    } catch (NoSuchMethodException e) {
      return Method.MISSING;
    } 
  }
  
  public final Subscriptions getSubscriptions() {
    Class<?> cls = getClass();
    Subscriptions subscriptions = SUBSCRIPTIONS.get(cls);
    if (subscriptions == null) {
      Method tick = Method.MISSING, background = Method.MISSING, mouseClick = Method.MISSING, mouseDrag = Method.MISSING, mouseRelease = Method.MISSING, mouseScroll = Method.MISSING, key = Method.MISSING;
      while (cls != GuiElement.class && (!tick.hasSeen() || !background.hasSeen() || !mouseClick.hasSeen() || !mouseDrag.hasSeen() || !mouseRelease.hasSeen() || !mouseScroll.hasSeen() || !key.hasSeen())) {
        if (!tick.hasSeen())
          tick = hasMethod(cls, "tick", new Class[0]); 
        if (!background.hasSeen())
          background = hasMethod(cls, "drawBackground", new Class[] { int.class, int.class }); 
        if (!mouseClick.hasSeen())
          mouseClick = hasMethod(cls, "onMouseClick", new Class[] { int.class, int.class, MouseButton.class }); 
        if (!mouseClick.hasSeen())
          mouseClick = hasMethod(cls, "onMouseClick", new Class[] { int.class, int.class, MouseButton.class, boolean.class }); 
        if (!mouseDrag.hasSeen())
          mouseDrag = hasMethod(cls, "onMouseDrag", new Class[] { int.class, int.class, MouseButton.class, long.class }); 
        if (!mouseDrag.hasSeen())
          mouseDrag = hasMethod(cls, "onMouseDrag", new Class[] { int.class, int.class, MouseButton.class, long.class, boolean.class }); 
        if (!mouseRelease.hasSeen())
          mouseRelease = hasMethod(cls, "onMouseRelease", new Class[] { int.class, int.class, MouseButton.class }); 
        if (!mouseRelease.hasSeen())
          mouseRelease = hasMethod(cls, "onMouseRelease", new Class[] { int.class, int.class, MouseButton.class, boolean.class }); 
        if (!mouseScroll.hasSeen())
          mouseScroll = hasMethod(cls, "onMouseScroll", new Class[] { int.class, int.class, ScrollDirection.class }); 
        if (!key.hasSeen())
          key = hasMethod(cls, "onKeyTyped", new Class[] { char.class, int.class }); 
        cls = cls.getSuperclass();
      } 
      subscriptions = new Subscriptions(tick.isPresent(), background.isPresent(), mouseClick.isPresent(), mouseDrag.isPresent(), mouseRelease.isPresent(), mouseScroll.isPresent(), key.isPresent());
      SUBSCRIPTIONS.put(getClass(), subscriptions);
    } 
    return subscriptions;
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  protected static @interface SkippedMethod {}
  
  private enum Method {
    PRESENT, SKIPPED, MISSING;
    
    boolean hasSeen() {
      return (this != MISSING);
    }
    
    boolean isPresent() {
      return (this == PRESENT);
    }
  }
  
  public static final class Subscriptions {
    public final boolean tick;
    
    public final boolean background;
    
    public final boolean mouseClick;
    
    public final boolean mouseDrag;
    
    public final boolean mouseRelease;
    
    public final boolean mouseScroll;
    
    public final boolean key;
    
    Subscriptions(boolean tick, boolean background, boolean mouseClick, boolean mouseDrag, boolean mouseRelease, boolean mouseScroll, boolean key) {
      this.tick = tick;
      this.background = background;
      this.mouseClick = mouseClick;
      this.mouseDrag = mouseDrag;
      this.mouseRelease = mouseRelease;
      this.mouseScroll = mouseScroll;
      this.key = key;
    }
    
    public String toString() {
      return String.format("tick: %s, background: %s, mouseClick: %s, mouseDrag: %s, mouseRelease: %s, mouseScroll: %s, key: %s", new Object[] { Boolean.valueOf(this.tick), Boolean.valueOf(this.background), Boolean.valueOf(this.mouseClick), Boolean.valueOf(this.mouseDrag), Boolean.valueOf(this.mouseRelease), Boolean.valueOf(this.mouseScroll), Boolean.valueOf(this.key) });
    }
  }
  
  public static final ResourceLocation commonTexture = new ResourceLocation("ic2", "textures/gui/common.png");
  
  private static final Map<Class<?>, Subscriptions> SUBSCRIPTIONS = new HashMap<>();
  
  protected final GuiIC2<?> gui;
  
  protected int x;
  
  protected int y;
  
  protected int width;
  
  protected int height;
  
  private IEnableHandler enableHandler;
  
  private Supplier<String> tooltipProvider;
}
