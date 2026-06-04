package ic2.core.block.machine.gui;

import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiWeightedFluidDistributor extends GuiWeightedDistributor<ContainerWeightedFluidDistributor>
{
	public GuiWeightedFluidDistributor(ContainerWeightedFluidDistributor container)
	{
		super(container, 211);
		addElement(TankGauge.createPlain(this, 33, 111, 110, 10, container.base.fluidTank));
	}

	protected ResourceLocation getTexture()
	{
		return TEXTURE;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIWeightedFluidDistributor.png");
}
