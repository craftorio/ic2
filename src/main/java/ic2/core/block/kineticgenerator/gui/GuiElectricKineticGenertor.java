package ic2.core.block.kineticgenerator.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
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
public class GuiElectricKineticGenertor extends GuiIC2<ContainerElectricKineticGenerator> {
  public GuiElectricKineticGenertor(ContainerElectricKineticGenerator container) {
    super((ContainerBase)container);
    addElement((new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal))
        .withTooltip("ic2.ElectricKineticGenerator.gui.motors"));
    addElement((GuiElement)EnergyGauge.asBolt(this, 12, 44, (TileEntityBlock)container.base));
    addElement(Text.create(this, 29, 66, 119, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Localization.translate("ic2.ElectricKineticGenerator.gui.kUmax", new Object[] { Integer.valueOf(((TileEntityElectricKineticGenerator)((ContainerElectricKineticGenerator)GuiElectricKineticGenertor.access$000(this.this$0)).base).getMaxKU()), 
                      Integer.valueOf(((TileEntityElectricKineticGenerator)((ContainerElectricKineticGenerator)GuiElectricKineticGenertor.access$100(this.this$0)).base).getMaxKUForGUI()) });
              }
            }), 5752026, false, true, true).withTooltip("ic2.ElectricKineticGenerator.gui.tooltipkin"));
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIElectricKineticGenerator.png");
}
