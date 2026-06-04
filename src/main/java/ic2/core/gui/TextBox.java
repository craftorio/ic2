// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.util.Util;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import com.google.common.base.Predicates;
import ic2.core.GuiIC2;
import com.google.common.base.Predicate;

public class TextBox extends GuiElement<TextBox>
{
    protected String text;
    protected boolean focused;
    protected int cursor;
    protected int cursorTick;
    protected int scrollOffset;
    protected int selectionEnd;
    protected int maxTextLength;
    protected IEnableHandler enableHandler;
    protected Predicate<String> validator;
    protected ITextBoxWatcher watcher;
    protected final boolean drawBackground;
    protected static final int enabledColour = 14737632;
    protected static final int disabledColour = 7368816;
    protected static final int invalidColour = -3092272;
    
    public TextBox(final GuiIC2<?> gui, final int x, final int y, final int width, final int height) {
        this(gui, x, y, width, height, "");
    }
    
    public TextBox(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final String text) {
        this(gui, x, y, width, height, text, true);
    }
    
    public TextBox(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final boolean drawBackground) {
        this(gui, x, y, width, height, "", drawBackground);
    }
    
    public TextBox(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final String text, final boolean drawBackground) {
        super(gui, x, y, width, height);
        this.maxTextLength = 32;
        this.validator = (Predicate<String>)Predicates.alwaysTrue();
        this.text = text;
        this.drawBackground = drawBackground;
        final int length = text.length();
        this.cursor = length;
        this.selectionEnd = length;
    }
    
    public TextBox withTextEnableHandler(final IEnableHandler enableHandler) {
        this.enableHandler = enableHandler;
        return this;
    }
    
    public TextBox withTextValidator(final Predicate<String> validator) {
        this.validator = validator;
        return this;
    }
    
    public TextBox withTextWatcher(final ITextBoxWatcher watcher) {
        this.watcher = watcher;
        return this;
    }
    
    public boolean willDraw() {
        return this.enableHandler == null || this.enableHandler.isEnabled();
    }
    
    public void setFocused(final boolean focused) {
        if (focused && !this.focused) {
            this.cursorTick = 0;
        }
        this.focused = focused;
    }
    
    public boolean isFocused() {
        return this.focused;
    }
    
    public void setMaxTextLength(final int length) {
        if (length >= 0) {
            this.maxTextLength = length;
        }
    }
    
    public String getText() {
        return this.text;
    }
    
    public boolean setText(final String text) {
        if (this.setText(text, true)) {
            this.setCursorPositionEnd();
            return true;
        }
        return false;
    }
    
    public boolean setText(final String text, final boolean forceLength) {
        assert text != null;
        if (this.validator.apply((Object)text) && (text.length() <= this.maxTextLength || forceLength)) {
            final String old = this.text;
            this.text = ((text.length() <= this.maxTextLength) ? text : text.substring(0, this.maxTextLength));
            if (this.watcher != null) {
                this.watcher.onChanged(old, text);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        ++this.cursorTick;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        super.drawBackground(mouseX, mouseY);
        if (this.drawBackground) {
            this.gui.drawColoredRect(this.x - 1, this.y - 1, this.width + 2, this.height + 2, -6250336);
            this.gui.drawColoredRect(this.x, this.y, this.width, this.height, -16777216);
        }
    }
    
    @Override
    public void drawForeground(final int mouseX, final int mouseY) {
        super.drawForeground(mouseX, mouseY);
        final int colour = this.willDraw() ? 14737632 : 7368816;
        final int textOffset = this.cursor - this.scrollOffset;
        int selectionOffset = this.selectionEnd - this.scrollOffset;
        final String text = this.gui.trimStringToWidth(this.text.substring(this.scrollOffset), this.drawBackground ? (this.width - 8) : this.width);
        final boolean validOffset = textOffset >= 0 && textOffset <= text.length();
        final int xStartPos = (this.drawBackground ? (this.x + 4) : this.x) - this.gui.getGuiLeft();
        final int yPos = (this.drawBackground ? (this.y + (this.height - 8) / 2) : this.y) - this.gui.getGuiTop();
        int xPos = xStartPos;
        if (selectionOffset > text.length()) {
            selectionOffset = text.length();
        }
        if (!text.isEmpty()) {
            xPos = this.gui.drawString(xStartPos, yPos, validOffset ? text.substring(0, textOffset) : text, colour, true);
        }
        final boolean inStringOrFull = this.cursor < this.text.length() || this.text.length() >= this.maxTextLength;
        int xCursorPos = xPos;
        if (!validOffset) {
            xCursorPos = ((textOffset > 0) ? (xStartPos + this.width) : xStartPos);
        }
        else if (inStringOrFull) {
            xCursorPos = xPos - 1;
            --xPos;
        }
        if (!text.isEmpty() && validOffset && textOffset < text.length()) {
            xPos = this.gui.drawString(xPos, yPos, text.substring(textOffset), colour, true);
        }
        if (this.focused && this.cursorTick / 6 % 2 == 0 && validOffset) {
            if (inStringOrFull) {
                this.gui.drawColoredRect(xCursorPos, yPos - 1, 1, 10, -3092272);
            }
            else {
                this.gui.drawString(xCursorPos, yPos, "_", colour, true);
            }
        }
        if (selectionOffset != textOffset) {
            final int selectionEnd = xStartPos + this.gui.getStringWidth(text.substring(0, selectionOffset));
            this.drawHighlightedArea(xCursorPos, yPos - 1, selectionEnd - 1, yPos + 1 + 8);
        }
    }
    
    protected void drawHighlightedArea(int startX, int startY, int endX, int endY) {
        if (startX < endX) {
            final int temp = startX;
            startX = endX;
            endX = temp;
        }
        if (startY < endY) {
            final int temp = startY;
            startY = endY;
            endY = temp;
        }
        startX += this.gui.getGuiLeft();
        endX += this.gui.getGuiLeft();
        startY += this.gui.getGuiTop();
        endY += this.gui.getGuiTop();
        if (endX > this.x + this.width) {
            endX = this.x + this.width;
        }
        if (startX > this.x + this.width) {
            startX = this.x + this.width;
        }
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.color(0.0f, 0.0f, 255.0f, 255.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double)startX, (double)endY, 0.0).endVertex();
        vertexbuffer.pos((double)endX, (double)endY, 0.0).endVertex();
        vertexbuffer.pos((double)endX, (double)startY, 0.0).endVertex();
        vertexbuffer.pos((double)startX, (double)startY, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }
    
    @Override
    public boolean onMouseClick(final int mouseX, final int mouseY, final MouseButton button, final boolean onThis) {
        this.setFocused(onThis);
        if (this.focused && onThis && MouseButton.left == button) {
            int end = mouseX - this.x;
            if (this.drawBackground) {
                end -= 4;
            }
            final String text = this.gui.trimStringToWidth(this.text.substring(this.scrollOffset), this.drawBackground ? (this.width - 8) : this.width);
            this.setCursorPosition(this.gui.trimStringToWidth(text, end).length() + this.scrollOffset);
        }
        return onThis;
    }
    
    @Override
    public boolean onKeyTyped(final char typedChar, final int keyCode) {
        if (!this.focused) {
            return super.onKeyTyped(typedChar, keyCode);
        }
        if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
        }
        else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
        }
        else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.willDraw()) {
                this.writeText(GuiScreen.getClipboardString());
            }
        }
        else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            if (this.willDraw()) {
                this.writeText("");
            }
        }
        else {
            switch (keyCode) {
                case 14: {
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.willDraw()) {
                            this.deleteWords(-1);
                            break;
                        }
                        break;
                    }
                    else {
                        if (this.willDraw()) {
                            this.deleteFromCursor(-1);
                            break;
                        }
                        break;
                    }
                    break;
                }
                case 199: {
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                        break;
                    }
                    this.setCursorPositionStart();
                    break;
                }
                case 203: {
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.selectionEnd));
                            break;
                        }
                        this.setSelectionPos(this.selectionEnd - 1);
                        break;
                    }
                    else {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setCursorPosition(this.getNthWordFromCursor(-1));
                            break;
                        }
                        this.moveCursorBy(-1);
                        break;
                    }
                    break;
                }
                case 205: {
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.selectionEnd));
                            break;
                        }
                        this.setSelectionPos(this.selectionEnd + 1);
                        break;
                    }
                    else {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setCursorPosition(this.getNthWordFromCursor(1));
                            break;
                        }
                        this.moveCursorBy(1);
                        break;
                    }
                    break;
                }
                case 207: {
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                        break;
                    }
                    this.setCursorPositionEnd();
                    break;
                }
                case 211: {
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.willDraw()) {
                            this.deleteWords(1);
                            break;
                        }
                        break;
                    }
                    else {
                        if (this.willDraw()) {
                            this.deleteFromCursor(1);
                            break;
                        }
                        break;
                    }
                    break;
                }
                default: {
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && this.willDraw()) {
                        this.writeText(String.valueOf(typedChar));
                        break;
                    }
                    return super.onKeyTyped(typedChar, keyCode);
                }
            }
        }
        return true;
    }
    
    public void writeText(final String textToWrite) {
        final StringBuilder newText = new StringBuilder();
        final String cleanString = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
        final int start = Math.min(this.cursor, this.selectionEnd);
        final int end = Math.max(this.cursor, this.selectionEnd);
        final int insertionPoint = this.maxTextLength - this.text.length() - (start - end);
        if (!this.text.isEmpty()) {
            newText.append(this.text.substring(0, start));
        }
        int extraLength;
        if (insertionPoint < cleanString.length()) {
            newText.append(cleanString.substring(0, insertionPoint));
            extraLength = insertionPoint;
        }
        else {
            newText.append(cleanString);
            extraLength = cleanString.length();
        }
        if (!this.text.isEmpty() && end < this.text.length()) {
            newText.append(this.text.substring(end));
        }
        if (this.setText(newText.toString(), true)) {
            this.moveCursorBy(start - this.selectionEnd + extraLength);
        }
    }
    
    public void deleteWords(final int num) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursor) {
                this.writeText("");
            }
            else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursor);
            }
        }
    }
    
    public void deleteFromCursor(final int num) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursor) {
                this.writeText("");
            }
            else {
                int start;
                int end;
                if (num < 0) {
                    start = this.cursor;
                    end = this.cursor + num;
                }
                else {
                    start = this.cursor + num;
                    end = this.cursor;
                }
                final StringBuilder newText = new StringBuilder();
                if (end >= 0) {
                    newText.append(this.text.substring(0, end));
                }
                if (start < this.text.length()) {
                    newText.append(this.text.substring(start));
                }
                if (this.validator.apply((Object)newText.toString())) {
                    final String old = this.text;
                    this.text = newText.toString();
                    if (this.watcher != null) {
                        this.watcher.onChanged(old, this.text);
                    }
                    if (num < 0) {
                        this.moveCursorBy(num);
                    }
                }
            }
        }
    }
    
    protected int getNthWordFromCursor(final int numWords) {
        return this.getNthWordFromPos(numWords, this.cursor);
    }
    
    protected int getNthWordFromPos(final int numWords, final int position) {
        return this.getNthWordFromPosWS(numWords, position, true);
    }
    
    protected int getNthWordFromPosWS(final int numWords, int position, final boolean skipWs) {
        final boolean positive = numWords >= 0;
        for (int k = 0, absN = Math.abs(numWords); k < absN; ++k) {
            if (positive) {
                final int end = this.text.length();
                position = this.text.indexOf(32, position);
                if (position == -1) {
                    position = end;
                }
                else {
                    while (skipWs && position < end && this.text.charAt(position) == ' ') {
                        ++position;
                    }
                }
            }
            else {
                while (skipWs && position > 0 && this.text.charAt(position - 1) == ' ') {
                    --position;
                }
                while (position > 0 && this.text.charAt(position - 1) != ' ') {
                    --position;
                }
            }
        }
        return position;
    }
    
    public String getSelectedText() {
        return this.text.substring(Math.min(this.cursor, this.selectionEnd), Math.max(this.cursor, this.selectionEnd));
    }
    
    protected void setCursorPositionStart() {
        this.setCursorPosition(0);
    }
    
    protected void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }
    
    protected void moveCursorBy(final int num) {
        this.setCursorPosition(this.selectionEnd + num);
    }
    
    protected void setCursorPosition(final int position) {
        this.setSelectionPos(this.cursor = Util.limit(position, 0, this.text.length()));
    }
    
    protected void setSelectionPos(int position) {
        final int textLength = this.text.length();
        position = Util.limit(position, 0, textLength);
        this.selectionEnd = position;
        if (this.scrollOffset > textLength) {
            this.scrollOffset = textLength;
        }
        final int width = this.drawBackground ? (this.width - 8) : this.width;
        final int maxPosition = this.gui.trimStringToWidth(this.text.substring(this.scrollOffset), width).length() + this.scrollOffset;
        if (position == this.scrollOffset) {
            this.scrollOffset -= this.gui.trimStringToWidthReverse(this.text, width).length();
        }
        if (position > maxPosition) {
            this.scrollOffset += position - maxPosition;
        }
        else if (position <= this.scrollOffset) {
            this.scrollOffset -= this.scrollOffset - position;
        }
        this.scrollOffset = Util.limit(this.scrollOffset, 0, textLength);
    }
    
    public interface ITextBoxWatcher
    {
        void onChanged(final String p0, final String p1);
    }
}
