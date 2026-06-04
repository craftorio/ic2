// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import ic2.core.gui.GuiElement;
import ic2.core.gui.Button;
import ic2.core.init.Localization;
import ic2.core.gui.VanillaButton;
import net.minecraft.util.ResourceLocation;
import ic2.core.GuiIC2;

public class GuiEnergyOMatOpen extends GuiIC2<ContainerEnergyOMatOpen>
{
    private static final ResourceLocation background;
    
    public GuiEnergyOMatOpen(final ContainerEnergyOMatOpen container) {
        super(container);
        this.addElement(((Button<GuiElement<?>>)new VanillaButton(this, 102, 16, 32, 10, this.createEventSender(0))).withText("-100k"));
        this.addElement(((Button<GuiElement<?>>)new VanillaButton(this, 102, 26, 32, 10, this.createEventSender(1))).withText("-10k"));
        this.addElement(((Button<GuiElement<?>>)new VanillaButton(this, 102, 36, 32, 10, this.createEventSender(2))).withText("-1k"));
        this.addElement(((Button<GuiElement<?>>)new VanillaButton(this, 102, 46, 32, 10, this.createEventSender(3))).withText("-100"));
        this.addElement(((Button<GuiElement<?>>)new VanillaButton(this, 134, 16, 32, 10, this.createEventSender(4))).withText("+100k"));
        this.addElement(((Button<GuiElement<?>>)new VanillaButton(this, 134, 26, 32, 10, this.createEventSender(5))).withText("+10k"));
        this.addElement(((Button<GuiElement<?>>)new VanillaButton(this, 134, 36, 32, 10, this.createEventSender(6))).withText("+1k"));
        this.addElement(((Button<GuiElement<?>>)new VanillaButton(this, 134, 46, 32, 10, this.createEventSender(7))).withText("+100"));
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(Localization.translate("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.offer"), 100, 60, 4210752);
        this.fontRenderer.drawString(((TileEntityEnergyOMat)((ContainerEnergyOMatOpen)this.container).base).euOffer + " EU", 100, 68, 4210752);
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiEnergyOMatOpen.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIEnergyOMatOpen.png");
    }
}
