// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.gui.dynamic.TextProvider;
import ic2.core.gui.IEnableHandler;
import com.google.common.base.Supplier;
import ic2.core.gui.GuiElement;
import ic2.core.gui.Text;
import ic2.core.ref.ItemName;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiCropnalyzer extends GuiIC2<ContainerCropnalyzer>
{
    private static final ResourceLocation background;
    
    public GuiCropnalyzer(final ContainerCropnalyzer container) {
        super(container, 223);
        this.addElement(Text.create(this, 74, 11, ItemName.cropnalyzer.getItemStack().getDisplayName(), 0, false));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 8, 37, "UNKNOWN", 16777215, false)).withEnableHandler(() -> ((HandHeldCropnalyzer)container.base).getScannedLevel() == 0));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 8, 37, this.cropSensitiveText((Supplier<String>)(HandHeldCropnalyzer)container.base::getSeedName), 16777215, false)).withEnableHandler(this.atLeastLevel(1)));
        final IEnableHandler atLeast2 = this.atLeastLevel(2);
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 8, 50, this.cropSensitiveText((Supplier<String>)(() -> "Tier: " + ((HandHeldCropnalyzer)container.base).getSeedTier())), 16777215, false)).withEnableHandler(atLeast2));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 8, 73, "Discovered by:", 16777215, false)).withEnableHandler(atLeast2));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 8, 86, this.cropSensitiveText((Supplier<String>)(HandHeldCropnalyzer)container.base::getSeedDiscovered), 16777215, false)).withEnableHandler(atLeast2));
        final IEnableHandler atLeast3 = this.atLeastLevel(3);
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 8, 109, this.cropSensitiveText((Supplier<String>)(() -> ((HandHeldCropnalyzer)container.base).getSeedDesc(0))), 16777215, false)).withEnableHandler(atLeast3));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 8, 122, this.cropSensitiveText((Supplier<String>)(() -> ((HandHeldCropnalyzer)container.base).getSeedDesc(1))), 16777215, false)).withEnableHandler(atLeast3));
        final IEnableHandler atLeast4 = this.atLeastLevel(4);
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 118, 37, "Growth:", 11403055, false)).withEnableHandler(atLeast4));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 118, 50, this.cropSensitiveText((Supplier<String>)(() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedGrowth()))), 11403055, false)).withEnableHandler(atLeast4));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 118, 73, "Gain:", 15649024, false)).withEnableHandler(atLeast4));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 118, 86, this.cropSensitiveText((Supplier<String>)(() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedGain()))), 15649024, false)).withEnableHandler(atLeast4));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 118, 109, "Resis.:", 52945, false)).withEnableHandler(atLeast4));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 118, 122, this.cropSensitiveText((Supplier<String>)(() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedResistence()))), 52945, false)).withEnableHandler(atLeast4));
    }
    
    private IEnableHandler atLeastLevel(final int level) {
        return () -> ((HandHeldCropnalyzer)((ContainerCropnalyzer)this.container).base).getScannedLevel() >= level;
    }
    
    private TextProvider.ITextProvider cropSensitiveText(final Supplier<String> text) {
        return TextProvider.of((Supplier<String>)(() -> (((HandHeldCropnalyzer)((ContainerCropnalyzer)this.container).base).getScannedLevel() > -1) ? text.get() : ""));
    }
    
    @Override
    protected void drawBackgroundAndTitle(final float partialTicks, final int mouseX, final int mouseY) {
        this.bindTexture();
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiCropnalyzer.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUICropnalyzer.png");
    }
}
