package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.block.machine.tileentity.TileEntityFluidBottler;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFluidBottler extends GuiIC2<ContainerFluidBottler> {
  public GuiFluidBottler(ContainerFluidBottler container) {
    super((ContainerBase)container, 184);
    addElement((GuiElement)EnergyGauge.asBolt(this, 12, 35, (TileEntityBlock)container.base));
    addElement((GuiElement)TankGauge.createNormal(this, 78, 34, (IFluidTank)((TileEntityFluidBottler)container.base).fluidTank));
  }
  
  protected void func_146976_a(float f, int x, int y) {
    super.func_146976_a(f, x, y);
    bindTexture();
    int progressSize = Math.round(((TileEntityFluidBottler)((ContainerFluidBottler)this.container).base).getProgress() * 16.0F);
    if (progressSize > 0) {
      drawTexturedModalRect(this.field_147003_i + 61, this.field_147009_r + 36, 198, 0, progressSize, 13);
      drawTexturedModalRect(this.field_147003_i + 61, this.field_147009_r + 73, 198, 0, progressSize, 13);
      drawTexturedModalRect(this.field_147003_i + 99, this.field_147009_r + 55, 198, 0, progressSize, 13);
    } 
  }
  
  public ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIBottler.png");
  }
}
