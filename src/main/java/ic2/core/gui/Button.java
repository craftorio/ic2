// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.client.renderer.RenderHelper;
import ic2.core.init.Localization;
import ic2.core.GuiIC2;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;

public abstract class Button<T extends Button<T>> extends GuiElement<T>
{
    private static final int iconSize = 16;
    private final IClickHandler handler;
    private Supplier<String> textProvider;
    private Supplier<ItemStack> iconProvider;
    
    protected Button(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final IClickHandler handler) {
        super(gui, x, y, width, height);
        this.handler = handler;
    }
    
    public T withText(final String text) {
        return this.withText((Supplier<String>)new Supplier<String>() {
            public String get() {
                return text;
            }
        });
    }
    
    public T withText(final Supplier<String> textProvider) {
        this.textProvider = textProvider;
        return (T)this;
    }
    
    public T withIcon(final Supplier<ItemStack> iconProvider) {
        this.iconProvider = iconProvider;
        return (T)this;
    }
    
    protected int getTextColor(final int mouseX, final int mouseY) {
        return 14540253;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        if (this.textProvider != null) {
            String text = (String)this.textProvider.get();
            if (text != null && !text.isEmpty()) {
                text = Localization.translate(text);
                this.gui.drawXYCenteredString(this.x + this.width / 2, this.y + this.height / 2, text, this.getTextColor(mouseX, mouseY), true);
            }
        }
        else if (this.iconProvider != null) {
            final ItemStack stack = (ItemStack)this.iconProvider.get();
            if (stack != null && stack.getItem() != null) {
                RenderHelper.enableGUIStandardItemLighting();
                this.gui.drawItem(this.x + (this.width - 16) / 2, this.y + (this.height - 16) / 2, stack);
                RenderHelper.disableStandardItemLighting();
            }
        }
    }
    
    @Override
    protected boolean onMouseClick(final int mouseX, final int mouseY, final MouseButton button) {
        this.gui.mc.getSoundHandler().playSound((ISound)PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        this.handler.onClick(button);
        return false;
    }
}
