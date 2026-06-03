package ic2.core.block.kineticgenerator.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiWaterKineticGenerator extends GuiIC2<ContainerWaterKineticGenerator> {
  public GuiWaterKineticGenerator(ContainerWaterKineticGenerator container) {
    super((ContainerBase)container);
    IEnableHandler validBiome = () -> (((TileEntityWaterKineticGenerator)container.base).type != TileEntityWaterKineticGenerator.BiomeState.INVALID);
    IEnableHandler invalidBiome = IEnableHandler.EnableHandlers.not(validBiome);
    addElement(Text.create(this, 38, 52, TextProvider.ofTranslated("ic2.WaterKineticGenerator.gui.wrongbiome1"), 2157374, false).withEnableHandler(invalidBiome));
    addElement(Text.create(this, 45, 69, TextProvider.ofTranslated("ic2.WaterKineticGenerator.gui.wrongbiome2"), 2157374, false).withEnableHandler(invalidBiome));
    IEnableHandler missingRotor = ((TileEntityWaterKineticGenerator)container.base).rotorSlot::isEmpty;
    addElement(Text.create(this, 27, 52, TextProvider.ofTranslated("ic2.WaterKineticGenerator.gui.rotormiss"), 2157374, false).withEnableHandler(IEnableHandler.EnableHandlers.and(new IEnableHandler[] { validBiome, missingRotor })));
    IEnableHandler hasRotor = IEnableHandler.EnableHandlers.not(missingRotor);
    IEnableHandler hasRotorSpace = () -> (((TileEntityWaterKineticGenerator)container.base).checkSpace(((TileEntityWaterKineticGenerator)container.base).getRotorDiameter(), true) == 0);
    addElement(Text.create(this, 20, 52, TextProvider.ofTranslated("ic2.WaterKineticGenerator.gui.rotorspace"), 2157374, false)
        .withEnableHandler(IEnableHandler.EnableHandlers.and(new IEnableHandler[] { validBiome, hasRotor, IEnableHandler.EnableHandlers.not(hasRotorSpace) })));
    addElement(Text.create(this, 55, 52, TextProvider.of(() -> Localization.translate("ic2.WaterKineticGenerator.gui.output", new Object[] { Integer.valueOf(((TileEntityWaterKineticGenerator)container.base).getKuOutput()) })), 2157374, false)
        .withEnableHandler(IEnableHandler.EnableHandlers.and(new IEnableHandler[] { validBiome, hasRotor, hasRotorSpace })));
    addElement(Text.create(this, 46, 70, TextProvider.of((TileEntityWaterKineticGenerator)container.base::getRotorHealth), 2157374, false).withEnableHandler(IEnableHandler.EnableHandlers.and(new IEnableHandler[] { validBiome, hasRotor, hasRotorSpace })));
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIWaterKineticGenerator.png");
}
