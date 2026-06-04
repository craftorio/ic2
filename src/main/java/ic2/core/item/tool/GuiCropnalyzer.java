package ic2.core.item.tool;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.ref.ItemName;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCropnalyzer extends GuiIC2<ContainerCropnalyzer> {
  public GuiCropnalyzer(ContainerCropnalyzer container) {
    super((ContainerBase)container, 223);
    addElement((GuiElement)Text.create(this, 74, 11, ItemName.cropnalyzer.getItemStack().func_82833_r(), 0, false));
    addElement(Text.create(this, 8, 37, "UNKNOWN", 16777215, false).withEnableHandler(() -> (((HandHeldCropnalyzer)container.base).getScannedLevel() == 0)));
    addElement(Text.create(this, 8, 37, cropSensitiveText((HandHeldCropnalyzer)container.base::getSeedName), 16777215, false).withEnableHandler(atLeastLevel(1)));
    IEnableHandler atLeast2 = atLeastLevel(2);
    addElement(Text.create(this, 8, 50, cropSensitiveText(() -> "Tier: " + ((HandHeldCropnalyzer)container.base).getSeedTier()), 16777215, false).withEnableHandler(atLeast2));
    addElement(Text.create(this, 8, 73, "Discovered by:", 16777215, false).withEnableHandler(atLeast2));
    addElement(Text.create(this, 8, 86, cropSensitiveText((HandHeldCropnalyzer)container.base::getSeedDiscovered), 16777215, false).withEnableHandler(atLeast2));
    IEnableHandler atLeast3 = atLeastLevel(3);
    addElement(Text.create(this, 8, 109, cropSensitiveText(() -> ((HandHeldCropnalyzer)container.base).getSeedDesc(0)), 16777215, false).withEnableHandler(atLeast3));
    addElement(Text.create(this, 8, 122, cropSensitiveText(() -> ((HandHeldCropnalyzer)container.base).getSeedDesc(1)), 16777215, false).withEnableHandler(atLeast3));
    IEnableHandler atLeast4 = atLeastLevel(4);
    addElement(Text.create(this, 118, 37, "Growth:", 11403055, false).withEnableHandler(atLeast4));
    addElement(Text.create(this, 118, 50, cropSensitiveText(() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedGrowth())), 11403055, false).withEnableHandler(atLeast4));
    addElement(Text.create(this, 118, 73, "Gain:", 15649024, false).withEnableHandler(atLeast4));
    addElement(Text.create(this, 118, 86, cropSensitiveText(() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedGain())), 15649024, false).withEnableHandler(atLeast4));
    addElement(Text.create(this, 118, 109, "Resis.:", 52945, false).withEnableHandler(atLeast4));
    addElement(Text.create(this, 118, 122, cropSensitiveText(() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedResistence())), 52945, false).withEnableHandler(atLeast4));
  }
  
  private IEnableHandler atLeastLevel(int level) {
    return () -> (((HandHeldCropnalyzer)((ContainerCropnalyzer)this.container).base).getScannedLevel() >= level);
  }
  
  private TextProvider.ITextProvider cropSensitiveText(Supplier<String> text) {
    return TextProvider.of(() -> (((HandHeldCropnalyzer)((ContainerCropnalyzer)this.container).base).getScannedLevel() > -1) ? (String)text.get() : "");
  }
  
  protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY) {
    bindTexture();
    drawTexturedModalRect(this.field_147003_i, this.field_147009_r, 0, 0, this.field_146999_f, this.field_147000_g);
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUICropnalyzer.png");
}
