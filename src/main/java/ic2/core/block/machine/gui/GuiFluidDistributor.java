package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.gui.GuiElement;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import ic2.core.network.NetworkManager;
import java.io.IOException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;

public class GuiFluidDistributor extends GuiIC2<ContainerFluidDistributor> {
  public GuiFluidDistributor(ContainerFluidDistributor container) {
    super((ContainerBase)container, 184);
    addElement((GuiElement)TankGauge.createPlain(this, 29, 38, 55, 47, (IFluidTank)((TileEntityFluidDistributor)container.base).fluidTank));
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.info"), 112, 47, 5752026);
    if (((TileEntityFluidDistributor)((ContainerFluidDistributor)this.container).base).getActive()) {
      this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.concentrate"), 95, 71, 5752026);
    } else {
      this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.distribute"), 95, 71, 5752026);
    } 
  }
  
  protected void func_73864_a(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.func_73864_a(mouseX, mouseY, mouseButton);
    mouseX -= this.field_147003_i;
    mouseY -= this.field_147009_r;
    if (mouseX >= 117 && mouseY >= 58 && mouseX <= 135 && mouseY <= 66)
      ((NetworkManager)IC2.network.get(false)).initiateClientTileEntityEvent((TileEntity)((ContainerFluidDistributor)this.container).base, 1); 
  }
  
  protected ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIFluidDistributor.png");
  }
}
