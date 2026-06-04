package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import ic2.core.util.Tuple;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiToolScanner extends GuiIC2<ContainerToolScanner> {
  public GuiToolScanner(ContainerToolScanner container) {
    super((ContainerBase)container, 230);
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.fontRenderer.drawString(Localization.translate("ic2.itemScanner.found"), 10, 20, 2157374);
    if (((ContainerToolScanner)this.container).scanResults != null) {
      int count = 0;
      for (Tuple.T2<ItemStack, Integer> result : ((ContainerToolScanner)this.container).scanResults) {
        String name = ((ItemStack)result.a).getItem().getItemStackDisplayName((ItemStack)result.a);
        this.fontRenderer.drawString(result.b + "x " + name, 10, 34 + count * 11, 5752026);
        count++;
        if (count == 10)
          break; 
      } 
      RenderHelper.disableStandardItemLighting();
    } 
  }
  
  protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
    super.drawGuiContainerBackgroundLayer(f, x, y);
    if (((ContainerToolScanner)this.container).scanResults != null) {
      int count = 0;
      RenderHelper.enableGUIStandardItemLighting();
      for (Tuple.T2<ItemStack, Integer> result : ((ContainerToolScanner)this.container).scanResults) {
        int xPos = 135 + (count & 0x1) * 15;
        drawItem(xPos, 11 * count + 28, (ItemStack)result.a);
        count++;
        if (count == 10)
          break; 
      } 
      RenderHelper.disableStandardItemLighting();
    } 
  }
  
  public ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIToolScanner.png");
  }
}
