package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.BlockIC2Fence;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerMagnetizer;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMagnetizer extends GuiIC2<ContainerMagnetizer> {
  public GuiMagnetizer(ContainerMagnetizer container) {
    super((ContainerBase)container);
    addElement((GuiElement)EnergyGauge.asBolt(this, 11, 28, (TileEntityBlock)container.base));
  }
  
  protected ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIMagnetizer.png");
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    if (BlockIC2Fence.hasMetalShoes(((ContainerMagnetizer)this.container).player)) {
      this.field_146289_q.func_78276_b(Localization.translate("ic2.Magnetizer.gui.hasMetalShoes"), 18, 66, 4259648);
    } else {
      this.field_146289_q.func_78276_b(Localization.translate("ic2.Magnetizer.gui.noMetalShoes"), 18, 66, 16728128);
    } 
  }
}
