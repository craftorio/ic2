package ic2.core.block.heatgenerator.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElectricHeatGenerator extends GuiIC2<ContainerElectricHeatGenerator> {
  public GuiElectricHeatGenerator(ContainerElectricHeatGenerator container) {
    super((ContainerBase)container);
    addElement((new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal))
        .withTooltip("ic2.ElectricHeatGenerator.gui.coils"));
    addElement((GuiElement)EnergyGauge.asBolt(this, 12, 44, (TileEntityBlock)container.base));
    addElement(Text.create(this, 34, 66, 109, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Localization.translate("ic2.ElectricHeatGenerator.gui.hUmax", new Object[] { Integer.valueOf(((TileEntityElectricHeatGenerator)((ContainerElectricHeatGenerator)GuiElectricHeatGenerator.access$000(this.this$0)).base).gettransmitHeat()), 
                      Integer.valueOf(((TileEntityElectricHeatGenerator)((ContainerElectricHeatGenerator)GuiElectricHeatGenerator.access$100(this.this$0)).base).getMaxHeatEmittedPerTick()) });
              }
            }), 5752026, false, true, true).withTooltip("ic2.ElectricHeatGenerator.gui.tooltipheat"));
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIElectricHeatGenerator.png");
}
