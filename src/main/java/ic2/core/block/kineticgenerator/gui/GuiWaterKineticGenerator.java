package ic2.core.block.kineticgenerator.gui;

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
public class GuiWaterKineticGenerator extends GuiIC2<ContainerWaterKineticGenerator>
{
	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIWaterKineticGenerator.png");

	public GuiWaterKineticGenerator(ContainerWaterKineticGenerator container)
	{
		super(container);
		IEnableHandler validBiome = () -> container.base.type != TileEntityWaterKineticGenerator.BiomeState.INVALID;
		IEnableHandler invalidBiome = IEnableHandler.EnableHandlers.not(validBiome);
		this.addElement(
			Text.create(this, 38, 52, TextProvider.ofTranslated("ic2.WaterKineticGenerator.gui.wrongbiome1"), 2157374, false).withEnableHandler(invalidBiome)
		);
		this.addElement(
			Text.create(this, 45, 69, TextProvider.ofTranslated("ic2.WaterKineticGenerator.gui.wrongbiome2"), 2157374, false).withEnableHandler(invalidBiome)
		);
		IEnableHandler missingRotor = container.base.rotorSlot::isEmpty;
		this.addElement(
			Text.create(this, 27, 52, TextProvider.ofTranslated("ic2.WaterKineticGenerator.gui.rotormiss"), 2157374, false)
				.withEnableHandler(IEnableHandler.EnableHandlers.and(validBiome, missingRotor))
		);
		IEnableHandler hasRotor = IEnableHandler.EnableHandlers.not(missingRotor);
		IEnableHandler hasRotorSpace = () -> container.base.checkSpace(container.base.getRotorDiameter(), true) == 0;
		this.addElement(
			Text.create(this, 20, 52, TextProvider.ofTranslated("ic2.WaterKineticGenerator.gui.rotorspace"), 2157374, false)
				.withEnableHandler(IEnableHandler.EnableHandlers.and(validBiome, hasRotor, IEnableHandler.EnableHandlers.not(hasRotorSpace)))
		);
		this.addElement(
			Text.create(
					this,
					55,
					52,
					TextProvider.of(() -> Localization.translate("ic2.WaterKineticGenerator.gui.output", container.base.getKuOutput())),
					2157374,
					false
				)
				.withEnableHandler(IEnableHandler.EnableHandlers.and(validBiome, hasRotor, hasRotorSpace))
		);
		this.addElement(
			Text.create(this, 46, 70, TextProvider.of(container.base::getRotorHealth), 2157374, false)
				.withEnableHandler(IEnableHandler.EnableHandlers.and(validBiome, hasRotor, hasRotorSpace))
		);
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}
