package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import ic2.core.gui.GuiElement;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSolarDestiller extends GuiIC2<ContainerSolarDestiller> {
  public GuiSolarDestiller(ContainerSolarDestiller container) {
    super((ContainerBase)container, 184);
    addElement((GuiElement)TankGauge.createPlain(this, 37, 43, 53, 18, (IFluidTank)((TileEntitySolarDestiller)container.base).inputTank));
    addElement((GuiElement)TankGauge.createPlain(this, 115, 55, 17, 43, (IFluidTank)((TileEntitySolarDestiller)container.base).outputTank));
  }
  
  protected void func_146976_a(float f, int x, int y) {
    super.func_146976_a(f, x, y);
    bindTexture();
    if (((TileEntitySolarDestiller)((ContainerSolarDestiller)this.container).base).canWork())
      drawTexturedModalRect(this.field_147003_i + 36, this.field_147009_r + 26, 0, 184, 97, 29); 
  }
  
  protected ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUISolarDestiller.png");
  }
}
