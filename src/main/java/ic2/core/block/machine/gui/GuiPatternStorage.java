// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.gui.ItemImage;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.item.ItemStack;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.GuiElement;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import com.google.common.base.Supplier;
import ic2.core.gui.CustomButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerPatternStorage;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiPatternStorage extends GuiIC2<ContainerPatternStorage>
{
    private static final ResourceLocation background;
    
    public GuiPatternStorage(final ContainerPatternStorage container) {
        super(container);
        this.addElement(((GuiElement<GuiElement<?>>)new CustomButton(this, 7, 19, 9, 18, this.createEventSender(0))).withTooltip("ic2.PatternStorage.gui.info.last"));
        this.addElement(((GuiElement<GuiElement<?>>)new CustomButton(this, 36, 19, 9, 18, this.createEventSender(1))).withTooltip("ic2.PatternStorage.gui.info.next"));
        this.addElement(((GuiElement<GuiElement<?>>)new CustomButton(this, 10, 37, 16, 8, this.createEventSender(2))).withTooltip("ic2.PatternStorage.gui.info.export"));
        this.addElement(((GuiElement<GuiElement<?>>)new CustomButton(this, 26, 37, 16, 8, this.createEventSender(3))).withTooltip("ic2.PatternStorage.gui.info.import"));
        this.addElement(Text.create(this, this.xSize / 2, 30, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                final TileEntityPatternStorage te = (TileEntityPatternStorage)container.base;
                return Math.min(te.index + 1, te.maxIndex) + " / " + te.maxIndex;
            }
        }), 4210752, false, true, false));
        this.addElement(Text.create(this, 10, 48, TextProvider.ofTranslated("ic2.generic.text.Name"), 16777215, false));
        this.addElement(Text.create(this, 10, 59, TextProvider.ofTranslated("ic2.generic.text.UUMatte"), 16777215, false));
        this.addElement(Text.create(this, 10, 70, TextProvider.ofTranslated("ic2.generic.text.Energy"), 16777215, false));
        final IEnableHandler patternInfoEnabler = new IEnableHandler() {
            @Override
            public boolean isEnabled() {
                return ((TileEntityPatternStorage)container.base).pattern != null;
            }
        };
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 80, 48, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                final ItemStack pattern = ((TileEntityPatternStorage)container.base).pattern;
                return (pattern != null) ? pattern.getDisplayName() : null;
            }
        }), 16777215, false)).withEnableHandler(patternInfoEnabler));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 80, 59, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Util.toSiString(((TileEntityPatternStorage)container.base).patternUu, 4) + Localization.translate("ic2.generic.text.bucketUnit");
            }
        }), 16777215, false)).withEnableHandler(patternInfoEnabler));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 80, 70, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Util.toSiString(((TileEntityPatternStorage)container.base).patternEu, 4) + Localization.translate("ic2.generic.text.EU");
            }
        }), 16777215, false)).withEnableHandler(patternInfoEnabler));
        this.addElement(new ItemImage(this, 152, 29, (Supplier<ItemStack>)new Supplier<ItemStack>() {
            public ItemStack get() {
                return ((TileEntityPatternStorage)container.base).pattern;
            }
        }));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiPatternStorage.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIPatternStorage.png");
    }
}
