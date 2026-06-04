// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.ContainerBase;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.Minecraft;
import java.util.HashMap;
import ic2.core.gui.dynamic.TextProvider;
import net.minecraft.inventory.IInventory;
import ic2.core.init.Localization;
import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Suppliers;
import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import java.util.Map;
import net.minecraft.util.ResourceLocation;

public abstract class GuiElement<T extends GuiElement<T>>
{
    protected static final int hoverColor = -2130706433;
    public static final ResourceLocation commonTexture;
    private static final Map<Class<?>, Subscriptions> SUBSCRIPTIONS;
    protected final GuiIC2<?> gui;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    private IEnableHandler enableHandler;
    private Supplier<String> tooltipProvider;
    
    protected GuiElement(final GuiIC2<?> gui, final int x, final int y, final int width, final int height) {
        if (width < 0) {
            throw new IllegalArgumentException("negative width");
        }
        if (height < 0) {
            throw new IllegalArgumentException("negative height");
        }
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public final boolean isEnabled() {
        return this.enableHandler == null || this.enableHandler.isEnabled();
    }
    
    public boolean contains(final int x, final int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }
    
    public T withEnableHandler(final IEnableHandler enableHandler) {
        this.enableHandler = enableHandler;
        return (T)this;
    }
    
    public T withTooltip(final String tooltip) {
        return this.withTooltip((Supplier<String>)Suppliers.ofInstance((Object)tooltip));
    }
    
    public T withTooltip(final Supplier<String> tooltipProvider) {
        this.tooltipProvider = tooltipProvider;
        return (T)this;
    }
    
    public void tick() {
    }
    
    public void drawBackground(final int mouseX, final int mouseY) {
    }
    
    public void drawForeground(final int mouseX, final int mouseY) {
        if (this.contains(mouseX, mouseY) && !this.suppressTooltip(mouseX, mouseY)) {
            final List<String> lines = this.getToolTip();
            if (this.tooltipProvider != null) {
                final String tooltip = (String)this.tooltipProvider.get();
                if (tooltip != null && !tooltip.isEmpty()) {
                    addLines(lines, tooltip);
                }
            }
            if (!lines.isEmpty()) {
                this.gui.drawTooltip(mouseX, mouseY, lines);
            }
        }
    }
    
    private static void addLines(final List<String> list, final String str) {
        int startPos;
        int pos;
        for (startPos = 0; (pos = str.indexOf(10, startPos)) != -1; startPos = pos + 1) {
            list.add(processText(str.substring(startPos, pos)));
        }
        if (startPos == 0) {
            list.add(processText(str));
        }
        else {
            list.add(processText(str.substring(startPos)));
        }
    }
    
    public boolean onMouseClick(final int mouseX, final int mouseY, final MouseButton button, final boolean onThis) {
        return onThis && this.onMouseClick(mouseX, mouseY, button);
    }
    
    protected boolean onMouseClick(final int mouseX, final int mouseY, final MouseButton button) {
        return false;
    }
    
    public boolean onMouseDrag(final int mouseX, final int mouseY, final MouseButton button, final long timeFromLastClick, final boolean onThis) {
        return onThis && this.onMouseDrag(mouseX, mouseY, button, timeFromLastClick);
    }
    
    protected boolean onMouseDrag(final int mouseX, final int mouseY, final MouseButton button, final long timeFromLastClick) {
        return false;
    }
    
    public boolean onMouseRelease(final int mouseX, final int mouseY, final MouseButton button, final boolean onThis) {
        return onThis && this.onMouseRelease(mouseX, mouseY, button);
    }
    
    protected boolean onMouseRelease(final int mouseX, final int mouseY, final MouseButton button) {
        return false;
    }
    
    public void onMouseScroll(final int mouseX, final int mouseY, final ScrollDirection direction) {
    }
    
    public boolean onKeyTyped(final char typedChar, final int keyCode) {
        return false;
    }
    
    protected boolean suppressTooltip(final int mouseX, final int mouseY) {
        return false;
    }
    
    protected List<String> getToolTip() {
        return new ArrayList<String>();
    }
    
    protected static String processText(final String text) {
        return Localization.translate(text);
    }
    
    protected final IInventory getBase() {
        return ((ContainerBase)this.gui.getContainer()).base;
    }
    
    protected final Map<String, TextProvider.ITextProvider> getTokens() {
        final Map<String, TextProvider.ITextProvider> ret = new HashMap<String, TextProvider.ITextProvider>();
        ret.put("name", TextProvider.ofTranslated(this.getBase().getName()));
        return ret;
    }
    
    protected static void bindTexture(final ResourceLocation texture) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }
    
    public static void bindCommonTexture() {
        Minecraft.getMinecraft().renderEngine.bindTexture(GuiElement.commonTexture);
    }
    
    protected static void bindBlockTexture() {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }
    
    protected static TextureMap getBlockTextureMap() {
        return Minecraft.getMinecraft().getTextureMapBlocks();
    }
    
    private static final Method hasMethod(final Class<?> cls, final String name, final Class<?>... params) {
        try {
            return cls.getDeclaredMethod(name, params).isAnnotationPresent(SkippedMethod.class) ? Method.SKIPPED : Method.PRESENT;
        }
        catch (final NoSuchMethodException e) {
            return Method.MISSING;
        }
    }
    
    public final Subscriptions getSubscriptions() {
        Class<?> cls = this.getClass();
        Subscriptions subscriptions = GuiElement.SUBSCRIPTIONS.get(cls);
        if (subscriptions == null) {
            Method tick;
            Method background;
            Method mouseClick;
            Method mouseDrag;
            Method mouseRelease;
            Method mouseScroll;
            Method key;
            for (tick = Method.MISSING, background = Method.MISSING, mouseClick = Method.MISSING, mouseDrag = Method.MISSING, mouseRelease = Method.MISSING, mouseScroll = Method.MISSING, key = Method.MISSING; cls != GuiElement.class && (!tick.hasSeen() || !background.hasSeen() || !mouseClick.hasSeen() || !mouseDrag.hasSeen() || !mouseRelease.hasSeen() || !mouseScroll.hasSeen() || !key.hasSeen()); cls = cls.getSuperclass()) {
                if (!tick.hasSeen()) {
                    tick = hasMethod(cls, "tick", (Class<?>[])new Class[0]);
                }
                if (!background.hasSeen()) {
                    background = hasMethod(cls, "drawBackground", Integer.TYPE, Integer.TYPE);
                }
                if (!mouseClick.hasSeen()) {
                    mouseClick = hasMethod(cls, "onMouseClick", Integer.TYPE, Integer.TYPE, MouseButton.class);
                }
                if (!mouseClick.hasSeen()) {
                    mouseClick = hasMethod(cls, "onMouseClick", Integer.TYPE, Integer.TYPE, MouseButton.class, Boolean.TYPE);
                }
                if (!mouseDrag.hasSeen()) {
                    mouseDrag = hasMethod(cls, "onMouseDrag", Integer.TYPE, Integer.TYPE, MouseButton.class, Long.TYPE);
                }
                if (!mouseDrag.hasSeen()) {
                    mouseDrag = hasMethod(cls, "onMouseDrag", Integer.TYPE, Integer.TYPE, MouseButton.class, Long.TYPE, Boolean.TYPE);
                }
                if (!mouseRelease.hasSeen()) {
                    mouseRelease = hasMethod(cls, "onMouseRelease", Integer.TYPE, Integer.TYPE, MouseButton.class);
                }
                if (!mouseRelease.hasSeen()) {
                    mouseRelease = hasMethod(cls, "onMouseRelease", Integer.TYPE, Integer.TYPE, MouseButton.class, Boolean.TYPE);
                }
                if (!mouseScroll.hasSeen()) {
                    mouseScroll = hasMethod(cls, "onMouseScroll", Integer.TYPE, Integer.TYPE, ScrollDirection.class);
                }
                if (!key.hasSeen()) {
                    key = hasMethod(cls, "onKeyTyped", Character.TYPE, Integer.TYPE);
                }
            }
            subscriptions = new Subscriptions(tick.isPresent(), background.isPresent(), mouseClick.isPresent(), mouseDrag.isPresent(), mouseRelease.isPresent(), mouseScroll.isPresent(), key.isPresent());
            GuiElement.SUBSCRIPTIONS.put(this.getClass(), subscriptions);
        }
        return subscriptions;
    }
    
    static {
        commonTexture = new ResourceLocation("ic2", "textures/gui/common.png");
        SUBSCRIPTIONS = new HashMap<Class<?>, Subscriptions>();
    }
    
    private enum Method
    {
        PRESENT, 
        SKIPPED, 
        MISSING;
        
        boolean hasSeen() {
            return this != Method.MISSING;
        }
        
        boolean isPresent() {
            return this == Method.PRESENT;
        }
    }
    
    public static final class Subscriptions
    {
        public final boolean tick;
        public final boolean background;
        public final boolean mouseClick;
        public final boolean mouseDrag;
        public final boolean mouseRelease;
        public final boolean mouseScroll;
        public final boolean key;
        
        Subscriptions(final boolean tick, final boolean background, final boolean mouseClick, final boolean mouseDrag, final boolean mouseRelease, final boolean mouseScroll, final boolean key) {
            this.tick = tick;
            this.background = background;
            this.mouseClick = mouseClick;
            this.mouseDrag = mouseDrag;
            this.mouseRelease = mouseRelease;
            this.mouseScroll = mouseScroll;
            this.key = key;
        }
        
        @Override
        public String toString() {
            return String.format("tick: %s, background: %s, mouseClick: %s, mouseDrag: %s, mouseRelease: %s, mouseScroll: %s, key: %s", this.tick, this.background, this.mouseClick, this.mouseDrag, this.mouseRelease, this.mouseScroll, this.key);
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    protected @interface SkippedMethod {
    }
}
