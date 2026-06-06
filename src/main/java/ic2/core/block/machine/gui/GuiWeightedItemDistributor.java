package ic2.core.block.machine.gui;

import ic2.core.block.machine.container.ContainerWeightedItemDistributor;
import net.minecraft.util.ResourceLocation;

public class GuiWeightedItemDistributor extends GuiWeightedDistributor<ContainerWeightedItemDistributor>
{
	private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIWeightedItemDistributor.png");

	public GuiWeightedItemDistributor(ContainerWeightedItemDistributor container)
	{
		super(container, 211);
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return TEXTURE;
	}
}
