package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.block.machine.tileentity.TileEntityCropmatron;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCropmatron extends GuiIC2<ContainerCropmatron> {
  public GuiCropmatron(ContainerCropmatron container) {
    super((ContainerBase)container, 192);
    addElement((GuiElement)EnergyGauge.asBolt(this, 138, 82, (TileEntityBlock)container.base));
    addElement((GuiElement)TankGauge.createPlain(this, 11, 26, 24, 47, (IFluidTank)((TileEntityCropmatron)container.base).getWaterTank()));
    addElement((GuiElement)TankGauge.createPlain(this, 105, 26, 24, 47, (IFluidTank)((TileEntityCropmatron)container.base).getExTank()));
  }
  
  public ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUICropmatron.png");
  }
}
