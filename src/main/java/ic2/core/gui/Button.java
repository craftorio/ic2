package ic2.core.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;

public abstract class Button<T extends Button<T>> extends GuiElement<T> {
  private static final int iconSize = 16;
  
  private final IClickHandler handler;
  
  private Supplier<String> textProvider;
  
  private Supplier<ItemStack> iconProvider;
  
  protected Button(GuiIC2<?> gui, int x, int y, int width, int height, IClickHandler handler) {
    super(gui, x, y, width, height);
    this.handler = handler;
  }
  
  public T withText(final String text) {
    return withText(new Supplier<String>() {
          public String get() {
            return text;
          }
        });
  }
  
  public T withText(Supplier<String> textProvider) {
    this.textProvider = textProvider;
    return (T)this;
  }
  
  public T withIcon(Supplier<ItemStack> iconProvider) {
    this.iconProvider = iconProvider;
    return (T)this;
  }
  
  protected int getTextColor(int mouseX, int mouseY) {
    return 14540253;
  }
  
  public void drawBackground(int mouseX, int mouseY) {
    if (this.textProvider != null) {
      String text = (String)this.textProvider.get();
      if (text != null && !text.isEmpty()) {
        text = Localization.translate(text);
        this.gui.drawXYCenteredString(this.x + this.width / 2, this.y + this.height / 2, text, getTextColor(mouseX, mouseY), true);
      } 
    } else if (this.iconProvider != null) {
      ItemStack stack = (ItemStack)this.iconProvider.get();
      if (stack != null && stack.func_77973_b() != null) {
        RenderHelper.func_74520_c();
        this.gui.drawItem(this.x + (this.width - 16) / 2, this.y + (this.height - 16) / 2, stack);
        RenderHelper.func_74518_a();
      } 
    } 
  }
  
  protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button) {
    this.gui.field_146297_k.func_147118_V().func_147682_a((ISound)PositionedSoundRecord.func_184371_a(SoundEvents.field_187909_gi, 1.0F));
    this.handler.onClick(button);
    return false;
  }
}
