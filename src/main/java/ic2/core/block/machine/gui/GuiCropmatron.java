package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCropmatron extends GuiIC2<ContainerCropmatron>
{
	public GuiCropmatron(ContainerCropmatron container)
	{
		super(container, 192);
		this.addElement(EnergyGauge.asBolt(this, 138, 82, container.base));
		this.addElement(TankGauge.createPlain(this, 11, 26, 24, 47, container.base.getWaterTank()));
		this.addElement(TankGauge.createPlain(this, 105, 26, 24, 47, container.base.getExTank()));
	}

	@Override
	public ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUICropmatron.png");
	}
}
