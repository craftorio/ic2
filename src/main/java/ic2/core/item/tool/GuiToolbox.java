package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.gui.GuiElement;
import ic2.core.gui.Text;
import ic2.core.ref.ItemName;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiToolbox extends GuiIC2<ContainerToolbox> {
  public GuiToolbox(ContainerToolbox container) {
    super((ContainerBase)container);
    addElement((GuiElement)Text.create(this, 65, 11, ItemName.tool_box.getItemStack().func_82833_r(), 0, false));
  }
  
  protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY) {
    bindTexture();
    func_73729_b(this.field_147003_i, this.field_147009_r, 0, 0, this.field_146999_f, this.field_147000_g);
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIToolbox.png");
}
