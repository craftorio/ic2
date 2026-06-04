package ic2.core;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.ScrollDirection;
import ic2.core.init.Localization;
import ic2.core.network.NetworkManager;
import ic2.core.util.StackUtil;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public abstract class GuiIC2<T extends ContainerBase<? extends IInventory>> extends GuiContainer {
  private boolean fixKeyEvents;
  
  private boolean tick;
  
  private boolean background;
  
  private boolean mouseClick;
  
  private boolean mouseDrag;
  
  private boolean mouseRelease;
  
  private boolean mouseScroll;
  
  private boolean key;
  
  private final Queue<Tooltip> queuedTooltips;
  
  protected final T container;
  
  protected final List<GuiElement<?>> elements;
  
  public static final int textHeight = 8;
  
  public GuiIC2(T container) {
    this(container, 176, 166);
  }
  
  public GuiIC2(T container, int ySize) {
    this(container, 176, ySize);
  }
  
  public GuiIC2(T container, int xSize, int ySize) {
    super((Container)container);
    this.fixKeyEvents = false;
    this.tick = false;
    this.background = false;
    this.mouseClick = false;
    this.mouseDrag = false;
    this.mouseRelease = false;
    this.mouseScroll = false;
    this.key = false;
    this.queuedTooltips = new ArrayDeque<>();
    this.elements = new ArrayList<>();
    this.container = container;
    this.field_147000_g = ySize;
    this.field_146999_f = xSize;
  }
  
  public T getContainer() {
    return this.container;
  }
  
  public void func_73866_w_() {
    super.func_73866_w_();
    for (GuiElement<?> element : this.elements) {
      if (element instanceof ic2.core.gui.IKeyboardDependent) {
        Keyboard.enableRepeatEvents(true);
        this.fixKeyEvents = true;
        break;
      } 
    } 
  }
  
  public void func_73863_a(int mouseX, int mouseY, float partialTicks) {
    func_146276_q_();
    super.func_73863_a(mouseX, mouseY, partialTicks);
    func_191948_b(mouseX, mouseY);
  }
  
  public void func_73876_c() {
    super.func_73876_c();
    if (this.tick)
      for (GuiElement<?> element : this.elements) {
        if (element.isEnabled())
          element.tick(); 
      }  
  }
  
  protected void func_146976_a(float partialTicks, int mouseX, int mouseY) {
    mouseX -= this.field_147003_i;
    mouseY -= this.field_147009_r;
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    drawBackgroundAndTitle(partialTicks, mouseX, mouseY);
    if (((ContainerBase)this.container).base instanceof IUpgradableBlock) {
      this.mc.func_110434_K().func_110577_a(new ResourceLocation("ic2", "textures/gui/infobutton.png"));
      drawTexturedRect(3.0D, 3.0D, 10.0D, 10.0D, 0.0D, 0.0D);
    } 
    if (this.background)
      for (GuiElement<?> element : this.elements) {
        if (element.isEnabled())
          element.drawBackground(mouseX, mouseY); 
      }  
  }
  
  protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY) {
    bindTexture();
    drawTexturedModalRect(this.field_147003_i, this.field_147009_r, 0, 0, this.field_146999_f, this.field_147000_g);
    String name = Localization.translate(((ContainerBase)this.container).base.func_70005_c_());
    drawXCenteredString(this.field_146999_f / 2, 6, name, 4210752, false);
  }
  
  protected final void func_146979_b(int mouseX, int mouseY) {
    drawForegroundLayer(mouseX - this.field_147003_i, mouseY - this.field_147009_r);
    flushTooltips();
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    if (((ContainerBase)this.container).base instanceof IUpgradableBlock)
      handleUpgradeTooltip(mouseX, mouseY); 
    for (GuiElement<?> element : this.elements) {
      if (element.isEnabled())
        element.drawForeground(mouseX, mouseY); 
    } 
  }
  
  private void handleUpgradeTooltip(int mouseX, int mouseY) {
    int areaSize = 12;
    if (mouseX < 0 || mouseX > 12 || mouseY < 0 || mouseY > 12)
      return; 
    List<String> text = new ArrayList<>();
    text.add(Localization.translate("ic2.generic.text.upgrade"));
    for (ItemStack stack : getCompatibleUpgrades((IUpgradableBlock)((ContainerBase)this.container).base))
      text.add(stack.func_82833_r()); 
    drawTooltip(mouseX, mouseY, text);
  }
  
  private static List<ItemStack> getCompatibleUpgrades(IUpgradableBlock block) {
    List<ItemStack> ret = new ArrayList<>();
    Set<UpgradableProperty> properties = block.getUpgradableProperties();
    for (ItemStack stack : UpgradeRegistry.getUpgrades()) {
      IUpgradeItem item = (IUpgradeItem)stack.getItem();
      if (item.isSuitableFor(stack, properties))
        ret.add(stack); 
    } 
    return ret;
  }
  
  public void func_146274_d() throws IOException {
    super.func_146274_d();
    if (this.mouseScroll) {
      ScrollDirection direction;
      int mouseX = Mouse.getEventX() * this.width / this.mc.field_71443_c - this.field_147003_i;
      int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.field_71440_d - 1 - this.field_147009_r;
      int scrollDelta = Mouse.getEventDWheel();
      if (scrollDelta != 0) {
        direction = (scrollDelta < 0) ? ScrollDirection.down : ScrollDirection.up;
      } else {
        direction = ScrollDirection.stopped;
      } 
      for (GuiElement<?> element : this.elements) {
        if (element.isEnabled() && element.contains(mouseX, mouseY))
          element.onMouseScroll(mouseX, mouseY, direction); 
      } 
    } 
  }
  
  protected void func_73864_a(int mouseX, int mouseY, int mouseButton) throws IOException {
    boolean handled = false;
    if (this.mouseClick) {
      MouseButton button = MouseButton.get(mouseButton);
      if (button != null) {
        mouseX -= this.field_147003_i;
        mouseY -= this.field_147009_r;
        for (GuiElement<?> element : this.elements) {
          if (element.isEnabled())
            handled |= element.onMouseClick(mouseX, mouseY, button, element.contains(mouseX, mouseY)); 
        } 
        if (!handled) {
          mouseX += this.field_147003_i;
          mouseY += this.field_147009_r;
        } else {
          this.mouseHandled = true;
        } 
      } 
    } 
    if (!handled)
      super.func_73864_a(mouseX, mouseY, mouseButton); 
  }
  
  protected void func_146273_a(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    boolean handled = false;
    if (this.mouseDrag) {
      MouseButton button = MouseButton.get(clickedMouseButton);
      if (button != null) {
        mouseX -= this.field_147003_i;
        mouseY -= this.field_147009_r;
        for (GuiElement<?> element : this.elements) {
          if (element.isEnabled())
            handled |= element.onMouseDrag(mouseX, mouseY, button, timeSinceLastClick, element.contains(mouseX, mouseY)); 
        } 
        if (!handled) {
          mouseX += this.field_147003_i;
          mouseY += this.field_147009_r;
        } else {
          this.mouseHandled = true;
        } 
      } 
    } 
    if (!handled)
      super.func_146273_a(mouseX, mouseY, clickedMouseButton, timeSinceLastClick); 
  }
  
  protected void func_146286_b(int mouseX, int mouseY, int state) {
    boolean handled = false;
    if (this.mouseRelease) {
      MouseButton button = MouseButton.get(state);
      if (button != null) {
        mouseX -= this.field_147003_i;
        mouseY -= this.field_147009_r;
        for (GuiElement<?> element : this.elements) {
          if (element.isEnabled())
            handled |= element.onMouseRelease(mouseX, mouseY, button, element.contains(mouseX, mouseY)); 
        } 
        if (!handled) {
          mouseX += this.field_147003_i;
          mouseY += this.field_147009_r;
        } else {
          this.mouseHandled = true;
        } 
      } 
    } 
    if (!handled)
      super.func_146286_b(mouseX, mouseY, state); 
  }
  
  protected void func_73869_a(char typedChar, int keyCode) throws IOException {
    boolean handled = false;
    if (this.key) {
      for (GuiElement<?> element : this.elements) {
        if (element.isEnabled())
          handled |= element.onKeyTyped(typedChar, keyCode); 
      } 
      this.keyHandled = handled;
    } 
    if (!handled)
      super.func_73869_a(typedChar, keyCode); 
  }
  
  public void func_146281_b() {
    super.func_146281_b();
    if (this.fixKeyEvents)
      Keyboard.enableRepeatEvents(false); 
  }
  
  public void drawTexturedRect(double x, double y, double width, double height, double texX, double texY) {
    drawTexturedRect(x, y, width, height, texX, texY, false);
  }
  
  public void drawTexturedRect(double x, double y, double width, double height, double texX, double texY, boolean mirrorX) {
    drawTexturedRect(x, y, width, height, texX / 256.0D, texY / 256.0D, (texX + width) / 256.0D, (texY + height) / 256.0D, mirrorX);
  }
  
  public void drawTexturedRect(double x, double y, double width, double height, double uS, double vS, double uE, double vE, boolean mirrorX) {
    x += this.field_147003_i;
    y += this.field_147009_r;
    double xE = x + width;
    double yE = y + height;
    if (mirrorX) {
      double tmp = uS;
      uS = uE;
      uE = tmp;
    } 
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    Tessellator tessellator = Tessellator.func_178181_a();
    BufferBuilder worldrenderer = tessellator.func_178180_c();
    worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
    worldrenderer.func_181662_b(x, y, this.field_73735_i).func_187315_a(uS, vS).func_181675_d();
    worldrenderer.func_181662_b(x, yE, this.field_73735_i).func_187315_a(uS, vE).func_181675_d();
    worldrenderer.func_181662_b(xE, yE, this.field_73735_i).func_187315_a(uE, vE).func_181675_d();
    worldrenderer.func_181662_b(xE, y, this.field_73735_i).func_187315_a(uE, vS).func_181675_d();
    tessellator.func_78381_a();
  }
  
  public void drawSprite(double x, double y, double width, double height, TextureAtlasSprite sprite, int color, double scale, boolean fixRight, boolean fixBottom) {
    if (sprite == null)
      sprite = this.mc.func_147117_R().func_174944_f(); 
    x += this.field_147003_i;
    y += this.field_147009_r;
    scale *= 16.0D;
    double spriteUS = sprite.func_94209_e();
    double spriteVS = sprite.func_94206_g();
    double spriteWidth = sprite.func_94212_f() - spriteUS;
    double spriteHeight = sprite.func_94210_h() - spriteVS;
    int a = color >>> 24 & 0xFF;
    int r = color >>> 16 & 0xFF;
    int g = color >>> 8 & 0xFF;
    int b = color & 0xFF;
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    Tessellator tessellator = Tessellator.func_178181_a();
    BufferBuilder buffer = tessellator.func_178180_c();
    buffer.func_181668_a(7, DefaultVertexFormats.field_181709_i);
    double xS;
    for (xS = x; xS < x + width; xS += maxWidth) {
      double uS, maxWidth;
      if (xS == x && fixRight && (maxWidth = width % scale) > 0.0D) {
        uS = spriteUS + spriteWidth * (1.0D - maxWidth / scale);
      } else {
        maxWidth = scale;
        uS = spriteUS;
      } 
      double xE = Math.min(xS + maxWidth, x + width);
      double uE = uS + (xE - xS) / scale * spriteWidth;
      double yS;
      for (yS = y; yS < y + height; yS += maxHeight) {
        double vS, maxHeight;
        if (yS == y && fixBottom && (maxHeight = height % scale) > 0.0D) {
          vS = spriteVS + spriteHeight * (1.0D - maxHeight / scale);
        } else {
          maxHeight = scale;
          vS = spriteVS;
        } 
        double yE = Math.min(yS + maxHeight, y + height);
        double vE = vS + (yE - yS) / scale * spriteHeight;
        buffer.func_181662_b(xS, yS, this.field_73735_i).func_187315_a(uS, vS).func_181669_b(r, g, b, a).func_181675_d();
        buffer.func_181662_b(xS, yE, this.field_73735_i).func_187315_a(uS, vE).func_181669_b(r, g, b, a).func_181675_d();
        buffer.func_181662_b(xE, yE, this.field_73735_i).func_187315_a(uE, vE).func_181669_b(r, g, b, a).func_181675_d();
        buffer.func_181662_b(xE, yS, this.field_73735_i).func_187315_a(uE, vS).func_181669_b(r, g, b, a).func_181675_d();
      } 
    } 
    tessellator.func_78381_a();
  }
  
  public void drawItem(int x, int y, ItemStack stack) {
    this.field_146296_j.func_175042_a(stack, this.field_147003_i + x, this.field_147009_r + y);
  }
  
  public void drawItemStack(int x, int y, ItemStack stack) {
    drawItem(x, y, stack);
    this.field_146296_j.func_180453_a(this.field_146289_q, stack, this.field_147003_i + x, this.field_147009_r + y, null);
  }
  
  public void drawColoredRect(int x, int y, int width, int height, int color) {
    x += this.field_147003_i;
    y += this.field_147009_r;
    func_73734_a(x, y, x + width, y + height, color);
  }
  
  public int drawString(int x, int y, String text, int color, boolean shadow) {
    return this.field_146289_q.func_175065_a(text, (this.field_147003_i + x), (this.field_147009_r + y), color, shadow) - this.field_147003_i;
  }
  
  public void drawXCenteredString(int x, int y, String text, int color, boolean shadow) {
    drawCenteredString(x, y, text, color, shadow, true, false);
  }
  
  public void drawXYCenteredString(int x, int y, String text, int color, boolean shadow) {
    drawCenteredString(x, y, text, color, shadow, true, true);
  }
  
  public void drawCenteredString(int x, int y, String text, int color, boolean shadow, boolean centerX, boolean centerY) {
    if (centerX)
      x -= getStringWidth(text) / 2; 
    if (centerY)
      y -= 4; 
    this.field_146289_q.func_78276_b(text, this.field_147003_i + x, this.field_147009_r + y, color);
  }
  
  public int getStringWidth(String text) {
    return this.field_146289_q.func_78256_a(text);
  }
  
  public String trimStringToWidth(String text, int width) {
    return this.field_146289_q.func_78262_a(text, width, false);
  }
  
  public String trimStringToWidthReverse(String text, int width) {
    return this.field_146289_q.func_78262_a(text, width, true);
  }
  
  public void drawTooltip(int x, int y, List<String> text) {
    this.queuedTooltips.add(new Tooltip(text, x, y));
  }
  
  public void drawTooltip(int x, int y, ItemStack stack) {
    assert !StackUtil.isEmpty(stack);
    func_146285_a(stack, x, y);
  }
  
  protected void flushTooltips() {
    for (Tooltip tooltip : this.queuedTooltips) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      func_146283_a(tooltip.text, tooltip.x, tooltip.y);
      GlStateManager.func_179140_f();
    } 
    this.queuedTooltips.clear();
  }
  
  protected void addElement(GuiElement<?> element) {
    this.elements.add(element);
    if (!this.tick || !this.background || !this.mouseClick || !this.mouseDrag || !this.mouseRelease || !this.mouseScroll || !this.key) {
      GuiElement.Subscriptions subs = element.getSubscriptions();
      if (!this.tick)
        this.tick = subs.tick; 
      if (!this.background)
        this.background = subs.background; 
      if (!this.mouseClick)
        this.mouseClick = subs.mouseClick; 
      if (!this.mouseDrag)
        this.mouseDrag = subs.mouseDrag; 
      if (!this.mouseRelease)
        this.mouseRelease = subs.mouseRelease; 
      if (!this.mouseScroll)
        this.mouseScroll = subs.mouseScroll; 
      if (!this.key)
        this.key = subs.key; 
    } 
  }
  
  protected final void bindTexture() {
    this.mc.func_110434_K().func_110577_a(getTexture());
  }
  
  protected IClickHandler createEventSender(final int event) {
    if (((ContainerBase)this.container).base instanceof TileEntity)
      return new IClickHandler() {
          public void onClick(MouseButton button) {
            ((NetworkManager)IC2.network.get(false)).initiateClientTileEntityEvent((TileEntity)((ContainerBase)GuiIC2.this.container).base, event);
          }
        }; 
    throw new IllegalArgumentException("not applicable for " + this.container.base);
  }
  
  protected abstract ResourceLocation getTexture();
  
  private static class Tooltip {
    final int x;
    
    final int y;
    
    final List<String> text;
    
    Tooltip(List<String> text, int x, int y) {
      this.text = text;
      this.x = x;
      this.y = y;
    }
  }
}
