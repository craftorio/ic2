package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerCropHarvester;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCropHarvester extends GuiIC2<ContainerCropHarvester> {
  public GuiCropHarvester(ContainerCropHarvester container) {
    super((ContainerBase)container);
    addElement((GuiElement)EnergyGauge.asBolt(this, 19, 37, (TileEntityBlock)container.base));
  }
  
  public ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUICropHarvester.png");
  }
}
