// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import ic2.core.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import ic2.core.gui.IClickHandler;
import ic2.core.util.StackUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import ic2.core.gui.MouseButton;
import java.io.IOException;
import ic2.core.gui.ScrollDirection;
import org.lwjgl.input.Mouse;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradeRegistry;
import net.minecraft.item.ItemStack;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import ic2.api.upgrade.IUpgradableBlock;
import net.minecraft.client.renderer.GlStateManager;
import java.util.Iterator;
import org.lwjgl.input.Keyboard;
import ic2.core.gui.IKeyboardDependent;
import java.util.ArrayList;
import java.util.ArrayDeque;
import net.minecraft.inventory.Container;
import ic2.core.gui.GuiElement;
import java.util.List;
import java.util.Queue;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;

public abstract class GuiIC2<T extends ContainerBase<? extends IInventory>> extends GuiContainer
{
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
    
    public GuiIC2(final T container) {
        this(container, 176, 166);
    }
    
    public GuiIC2(final T container, final int ySize) {
        this(container, 176, ySize);
    }
    
    public GuiIC2(final T container, final int xSize, final int ySize) {
        super((Container)container);
        this.fixKeyEvents = false;
        this.tick = false;
        this.background = false;
        this.mouseClick = false;
        this.mouseDrag = false;
        this.mouseRelease = false;
        this.mouseScroll = false;
        this.key = false;
        this.queuedTooltips = new ArrayDeque<Tooltip>();
        this.elements = new ArrayList<GuiElement<?>>();
        this.container = container;
        this.ySize = ySize;
        this.xSize = xSize;
    }
    
    public T getContainer() {
        return this.container;
    }
    
    public void initGui() {
        super.initGui();
        for (final GuiElement<?> element : this.elements) {
            if (element instanceof IKeyboardDependent) {
                Keyboard.enableRepeatEvents(true);
                this.fixKeyEvents = true;
                break;
            }
        }
    }
    
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
    
    public void updateScreen() {
        super.updateScreen();
        if (this.tick) {
            for (final GuiElement<?> element : this.elements) {
                if (element.isEnabled()) {
                    element.tick();
                }
            }
        }
    }
    
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, int mouseX, int mouseY) {
        mouseX -= this.guiLeft;
        mouseY -= this.guiTop;
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawBackgroundAndTitle(partialTicks, mouseX, mouseY);
        if (this.container.base instanceof IUpgradableBlock) {
            this.mc.getTextureManager().bindTexture(new ResourceLocation("ic2", "textures/gui/infobutton.png"));
            this.drawTexturedRect(3.0, 3.0, 10.0, 10.0, 0.0, 0.0);
        }
        if (this.background) {
            for (final GuiElement<?> element : this.elements) {
                if (element.isEnabled()) {
                    element.drawBackground(mouseX, mouseY);
                }
            }
        }
    }
    
    protected void drawBackgroundAndTitle(final float partialTicks, final int mouseX, final int mouseY) {
        this.bindTexture();
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        final String name = Localization.translate(this.container.base.getName());
        this.drawXCenteredString(this.xSize / 2, 6, name, 4210752, false);
    }
    
    protected final void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.drawForegroundLayer(mouseX - this.guiLeft, mouseY - this.guiTop);
        this.flushTooltips();
    }
    
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        if (this.container.base instanceof IUpgradableBlock) {
            this.handleUpgradeTooltip(mouseX, mouseY);
        }
        for (final GuiElement<?> element : this.elements) {
            if (element.isEnabled()) {
                element.drawForeground(mouseX, mouseY);
            }
        }
    }
    
    private void handleUpgradeTooltip(final int mouseX, final int mouseY) {
        final int areaSize = 12;
        if (mouseX < 0 || mouseX > 12 || mouseY < 0 || mouseY > 12) {
            return;
        }
        final List<String> text = new ArrayList<String>();
        text.add(Localization.translate("ic2.generic.text.upgrade"));
        for (final ItemStack stack : getCompatibleUpgrades((IUpgradableBlock)this.container.base)) {
            text.add(stack.getDisplayName());
        }
        this.drawTooltip(mouseX, mouseY, text);
    }
    
    private static List<ItemStack> getCompatibleUpgrades(final IUpgradableBlock block) {
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        final Set<UpgradableProperty> properties = block.getUpgradableProperties();
        for (final ItemStack stack : UpgradeRegistry.getUpgrades()) {
            final IUpgradeItem item = (IUpgradeItem)stack.getItem();
            if (item.isSuitableFor(stack, properties)) {
                ret.add(stack);
            }
        }
        return ret;
    }
    
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.mouseScroll) {
            final int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth - this.guiLeft;
            final int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - this.guiTop;
            final int scrollDelta = Mouse.getEventDWheel();
            ScrollDirection direction;
            if (scrollDelta != 0) {
                direction = ((scrollDelta < 0) ? ScrollDirection.down : ScrollDirection.up);
            }
            else {
                direction = ScrollDirection.stopped;
            }
            for (final GuiElement<?> element : this.elements) {
                if (element.isEnabled() && element.contains(mouseX, mouseY)) {
                    element.onMouseScroll(mouseX, mouseY, direction);
                }
            }
        }
    }
    
    protected void mouseClicked(int mouseX, int mouseY, final int mouseButton) throws IOException {
        boolean handled = false;
        if (this.mouseClick) {
            final MouseButton button = MouseButton.get(mouseButton);
            if (button != null) {
                mouseX -= this.guiLeft;
                mouseY -= this.guiTop;
                for (final GuiElement<?> element : this.elements) {
                    if (element.isEnabled()) {
                        handled |= element.onMouseClick(mouseX, mouseY, button, element.contains(mouseX, mouseY));
                    }
                }
                if (!handled) {
                    mouseX += this.guiLeft;
                    mouseY += this.guiTop;
                }
                else {
                    this.mouseHandled = true;
                }
            }
        }
        if (!handled) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
    
    protected void mouseClickMove(int mouseX, int mouseY, final int clickedMouseButton, final long timeSinceLastClick) {
        boolean handled = false;
        if (this.mouseDrag) {
            final MouseButton button = MouseButton.get(clickedMouseButton);
            if (button != null) {
                mouseX -= this.guiLeft;
                mouseY -= this.guiTop;
                for (final GuiElement<?> element : this.elements) {
                    if (element.isEnabled()) {
                        handled |= element.onMouseDrag(mouseX, mouseY, button, timeSinceLastClick, element.contains(mouseX, mouseY));
                    }
                }
                if (!handled) {
                    mouseX += this.guiLeft;
                    mouseY += this.guiTop;
                }
                else {
                    this.mouseHandled = true;
                }
            }
        }
        if (!handled) {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }
    
    protected void mouseReleased(int mouseX, int mouseY, final int state) {
        boolean handled = false;
        if (this.mouseRelease) {
            final MouseButton button = MouseButton.get(state);
            if (button != null) {
                mouseX -= this.guiLeft;
                mouseY -= this.guiTop;
                for (final GuiElement<?> element : this.elements) {
                    if (element.isEnabled()) {
                        handled |= element.onMouseRelease(mouseX, mouseY, button, element.contains(mouseX, mouseY));
                    }
                }
                if (!handled) {
                    mouseX += this.guiLeft;
                    mouseY += this.guiTop;
                }
                else {
                    this.mouseHandled = true;
                }
            }
        }
        if (!handled) {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }
    
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        boolean handled = false;
        if (this.key) {
            for (final GuiElement<?> element : this.elements) {
                if (element.isEnabled()) {
                    handled |= element.onKeyTyped(typedChar, keyCode);
                }
            }
            this.keyHandled = handled;
        }
        if (!handled) {
            super.keyTyped(typedChar, keyCode);
        }
    }
    
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.fixKeyEvents) {
            Keyboard.enableRepeatEvents(false);
        }
    }
    
    public void drawTexturedRect(final double x, final double y, final double width, final double height, final double texX, final double texY) {
        this.drawTexturedRect(x, y, width, height, texX, texY, false);
    }
    
    public void drawTexturedRect(final double x, final double y, final double width, final double height, final double texX, final double texY, final boolean mirrorX) {
        this.drawTexturedRect(x, y, width, height, texX / 256.0, texY / 256.0, (texX + width) / 256.0, (texY + height) / 256.0, mirrorX);
    }
    
    public void drawTexturedRect(double x, double y, final double width, final double height, double uS, final double vS, double uE, final double vE, final boolean mirrorX) {
        x += this.guiLeft;
        y += this.guiTop;
        final double xE = x + width;
        final double yE = y + height;
        if (mirrorX) {
            final double tmp = uS;
            uS = uE;
            uE = tmp;
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y, (double)this.zLevel).tex(uS, vS).endVertex();
        worldrenderer.pos(x, yE, (double)this.zLevel).tex(uS, vE).endVertex();
        worldrenderer.pos(xE, yE, (double)this.zLevel).tex(uE, vE).endVertex();
        worldrenderer.pos(xE, y, (double)this.zLevel).tex(uE, vS).endVertex();
        tessellator.draw();
    }
    
    public void drawSprite(double x, double y, final double width, final double height, TextureAtlasSprite sprite, final int color, double scale, final boolean fixRight, final boolean fixBottom) {
        if (sprite == null) {
            sprite = this.mc.getTextureMapBlocks().getMissingSprite();
        }
        x += this.guiLeft;
        y += this.guiTop;
        scale *= 16.0;
        final double spriteUS = sprite.getMinU();
        final double spriteVS = sprite.getMinV();
        final double spriteWidth = sprite.getMaxU() - spriteUS;
        final double spriteHeight = sprite.getMaxV() - spriteVS;
        final int a = color >>> 24 & 0xFF;
        final int r = color >>> 16 & 0xFF;
        final int g = color >>> 8 & 0xFF;
        final int b = color & 0xFF;
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        double maxWidth;
        for (double xS = x; xS < x + width; xS += maxWidth) {
            double uS;
            if (xS == x && fixRight && (maxWidth = width % scale) > 0.0) {
                uS = spriteUS + spriteWidth * (1.0 - maxWidth / scale);
            }
            else {
                maxWidth = scale;
                uS = spriteUS;
            }
            final double xE = Math.min(xS + maxWidth, x + width);
            final double uE = uS + (xE - xS) / scale * spriteWidth;
            double maxHeight;
            for (double yS = y; yS < y + height; yS += maxHeight) {
                double vS;
                if (yS == y && fixBottom && (maxHeight = height % scale) > 0.0) {
                    vS = spriteVS + spriteHeight * (1.0 - maxHeight / scale);
                }
                else {
                    maxHeight = scale;
                    vS = spriteVS;
                }
                final double yE = Math.min(yS + maxHeight, y + height);
                final double vE = vS + (yE - yS) / scale * spriteHeight;
                buffer.pos(xS, yS, (double)this.zLevel).tex(uS, vS).color(r, g, b, a).endVertex();
                buffer.pos(xS, yE, (double)this.zLevel).tex(uS, vE).color(r, g, b, a).endVertex();
                buffer.pos(xE, yE, (double)this.zLevel).tex(uE, vE).color(r, g, b, a).endVertex();
                buffer.pos(xE, yS, (double)this.zLevel).tex(uE, vS).color(r, g, b, a).endVertex();
            }
        }
        tessellator.draw();
    }
    
    public void drawItem(final int x, final int y, final ItemStack stack) {
        this.itemRender.renderItemIntoGUI(stack, this.guiLeft + x, this.guiTop + y);
    }
    
    public void drawItemStack(final int x, final int y, final ItemStack stack) {
        this.drawItem(x, y, stack);
        this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, this.guiLeft + x, this.guiTop + y, (String)null);
    }
    
    public void drawColoredRect(int x, int y, final int width, final int height, final int color) {
        x += this.guiLeft;
        y += this.guiTop;
        drawRect(x, y, x + width, y + height, color);
    }
    
    public int drawString(final int x, final int y, final String text, final int color, final boolean shadow) {
        return this.fontRenderer.drawString(text, (float)(this.guiLeft + x), (float)(this.guiTop + y), color, shadow) - this.guiLeft;
    }
    
    public void drawXCenteredString(final int x, final int y, final String text, final int color, final boolean shadow) {
        this.drawCenteredString(x, y, text, color, shadow, true, false);
    }
    
    public void drawXYCenteredString(final int x, final int y, final String text, final int color, final boolean shadow) {
        this.drawCenteredString(x, y, text, color, shadow, true, true);
    }
    
    public void drawCenteredString(int x, int y, final String text, final int color, final boolean shadow, final boolean centerX, final boolean centerY) {
        if (centerX) {
            x -= this.getStringWidth(text) / 2;
        }
        if (centerY) {
            y -= 4;
        }
        this.fontRenderer.drawString(text, this.guiLeft + x, this.guiTop + y, color);
    }
    
    public int getStringWidth(final String text) {
        return this.fontRenderer.getStringWidth(text);
    }
    
    public String trimStringToWidth(final String text, final int width) {
        return this.fontRenderer.trimStringToWidth(text, width, false);
    }
    
    public String trimStringToWidthReverse(final String text, final int width) {
        return this.fontRenderer.trimStringToWidth(text, width, true);
    }
    
    public void drawTooltip(final int x, final int y, final List<String> text) {
        this.queuedTooltips.add(new Tooltip(text, x, y));
    }
    
    public void drawTooltip(final int x, final int y, final ItemStack stack) {
        assert !StackUtil.isEmpty(stack);
        this.renderToolTip(stack, x, y);
    }
    
    protected void flushTooltips() {
        for (final Tooltip tooltip : this.queuedTooltips) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.drawHoveringText((List)tooltip.text, tooltip.x, tooltip.y);
            GlStateManager.disableLighting();
        }
        this.queuedTooltips.clear();
    }
    
    protected void addElement(final GuiElement<?> element) {
        this.elements.add(element);
        if (!this.tick || !this.background || !this.mouseClick || !this.mouseDrag || !this.mouseRelease || !this.mouseScroll || !this.key) {
            final GuiElement.Subscriptions subs = element.getSubscriptions();
            if (!this.tick) {
                this.tick = subs.tick;
            }
            if (!this.background) {
                this.background = subs.background;
            }
            if (!this.mouseClick) {
                this.mouseClick = subs.mouseClick;
            }
            if (!this.mouseDrag) {
                this.mouseDrag = subs.mouseDrag;
            }
            if (!this.mouseRelease) {
                this.mouseRelease = subs.mouseRelease;
            }
            if (!this.mouseScroll) {
                this.mouseScroll = subs.mouseScroll;
            }
            if (!this.key) {
                this.key = subs.key;
            }
        }
    }
    
    protected final void bindTexture() {
        this.mc.getTextureManager().bindTexture(this.getTexture());
    }
    
    protected IClickHandler createEventSender(final int event) {
        if (this.container.base instanceof TileEntity) {
            return new IClickHandler() {
                @Override
                public void onClick(final MouseButton button) {
                    IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)GuiIC2.this.container.base, event);
                }
            };
        }
        throw new IllegalArgumentException("not applicable for " + this.container.base);
    }
    
    protected abstract ResourceLocation getTexture();
    
    private static class Tooltip
    {
        final int x;
        final int y;
        final List<String> text;
        
        Tooltip(final List<String> text, final int x, final int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }
    }
}
