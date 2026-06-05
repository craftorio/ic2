package ic2.core.item.tool;

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
      super(container, 230);
   }

   @Override
   protected void drawForegroundLayer(int mouseX, int mouseY) {
      super.drawForegroundLayer(mouseX, mouseY);
      this.fontRenderer.drawString(Localization.translate("ic2.itemScanner.found"), 10, 20, 2157374);
      if (this.container.scanResults != null) {
         int count = 0;

         for (Tuple.T2<ItemStack, Integer> result : this.container.scanResults) {
            String name = result.a.getItem().getItemStackDisplayName(result.a);
            this.fontRenderer.drawString(result.b + "x " + name, 10, 34 + count * 11, 5752026);
            if (++count == 10) {
               break;
            }
         }

         RenderHelper.disableStandardItemLighting();
      }
   }

   @Override
   protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
      super.drawGuiContainerBackgroundLayer(f, x, y);
      if (this.container.scanResults != null) {
         int count = 0;
         RenderHelper.enableGUIStandardItemLighting();

         for (Tuple.T2<ItemStack, Integer> result : this.container.scanResults) {
            int xPos = 135 + (count & 1) * 15;
            this.drawItem(xPos, 11 * count + 28, result.a);
            if (++count == 10) {
               break;
            }
         }

         RenderHelper.disableStandardItemLighting();
      }
   }

   @Override
   public ResourceLocation getTexture() {
      return new ResourceLocation("ic2", "textures/gui/GUIToolScanner.png");
   }
}
