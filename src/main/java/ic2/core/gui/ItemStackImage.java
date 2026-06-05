package ic2.core.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.util.StackUtil;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ItemStackImage extends GuiElement<ItemStackImage> {
   private final Supplier<ItemStack> itemSupplier;

   public ItemStackImage(GuiIC2<?> gui, int x, int y, Supplier<ItemStack> itemSupplier) {
      super(gui, x, y, 16, 16);
      this.itemSupplier = itemSupplier;
   }

   @Override
   public void drawBackground(int mouseX, int mouseY) {
      super.drawBackground(mouseX, mouseY);
      ItemStack stack = (ItemStack)this.itemSupplier.get();
      if (!StackUtil.isEmpty(stack)) {
         RenderHelper.enableGUIStandardItemLighting();
         this.gui.drawItemStack(this.x, this.y, stack);
         RenderHelper.disableStandardItemLighting();
      }
   }

   @Override
   public void drawForeground(int mouseX, int mouseY) {
      if (this.contains(mouseX, mouseY)) {
         ItemStack stack = (ItemStack)this.itemSupplier.get();
         if (!StackUtil.isEmpty(stack)) {
            this.gui.drawTooltip(mouseX, mouseY, stack);
         }
      }
   }
}
