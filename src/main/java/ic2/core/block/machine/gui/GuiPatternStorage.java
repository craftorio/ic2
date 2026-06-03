package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerPatternStorage;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.gui.CustomButton;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.ItemImage;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPatternStorage extends GuiIC2<ContainerPatternStorage> {
  public GuiPatternStorage(final ContainerPatternStorage container) {
    super((ContainerBase)container);
    addElement((new CustomButton(this, 7, 19, 9, 18, createEventSender(0)))
        .withTooltip("ic2.PatternStorage.gui.info.last"));
    addElement((new CustomButton(this, 36, 19, 9, 18, createEventSender(1)))
        .withTooltip("ic2.PatternStorage.gui.info.next"));
    addElement((new CustomButton(this, 10, 37, 16, 8, createEventSender(2)))
        .withTooltip("ic2.PatternStorage.gui.info.export"));
    addElement((new CustomButton(this, 26, 37, 16, 8, createEventSender(3)))
        .withTooltip("ic2.PatternStorage.gui.info.import"));
    addElement((GuiElement)Text.create(this, this.field_146999_f / 2, 30, TextProvider.of(new Supplier<String>() {
              public String get() {
                TileEntityPatternStorage te = (TileEntityPatternStorage)container.base;
                return Math.min(te.index + 1, te.maxIndex) + " / " + te.maxIndex;
              }
            }), 4210752, false, true, false));
    addElement((GuiElement)Text.create(this, 10, 48, TextProvider.ofTranslated("ic2.generic.text.Name"), 16777215, false));
    addElement((GuiElement)Text.create(this, 10, 59, TextProvider.ofTranslated("ic2.generic.text.UUMatte"), 16777215, false));
    addElement((GuiElement)Text.create(this, 10, 70, TextProvider.ofTranslated("ic2.generic.text.Energy"), 16777215, false));
    IEnableHandler patternInfoEnabler = new IEnableHandler() {
        public boolean isEnabled() {
          return (((TileEntityPatternStorage)container.base).pattern != null);
        }
      };
    addElement(Text.create(this, 80, 48, TextProvider.of(new Supplier<String>() {
              public String get() {
                ItemStack pattern = ((TileEntityPatternStorage)container.base).pattern;
                return (pattern != null) ? pattern.func_82833_r() : null;
              }
            },  ), 16777215, false).withEnableHandler(patternInfoEnabler));
    addElement(Text.create(this, 80, 59, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Util.toSiString(((TileEntityPatternStorage)container.base).patternUu, 4) + Localization.translate("ic2.generic.text.bucketUnit");
              }
            }), 16777215, false).withEnableHandler(patternInfoEnabler));
    addElement(Text.create(this, 80, 70, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Util.toSiString(((TileEntityPatternStorage)container.base).patternEu, 4) + Localization.translate("ic2.generic.text.EU");
              }
            }), 16777215, false).withEnableHandler(patternInfoEnabler));
    addElement((GuiElement)new ItemImage(this, 152, 29, new Supplier<ItemStack>() {
            public ItemStack get() {
              return ((TileEntityPatternStorage)container.base).pattern;
            }
          }));
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIPatternStorage.png");
}
