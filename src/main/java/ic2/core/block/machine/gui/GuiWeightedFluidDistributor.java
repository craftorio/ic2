package ic2.core.block.machine.gui;

import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiWeightedFluidDistributor extends GuiWeightedDistributor<ContainerWeightedFluidDistributor>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiweightedfluiddistributor.png");

	public GuiWeightedFluidDistributor(ContainerWeightedFluidDistributor container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 211);
		this.addElement(TankGauge.createPlain(this, 33, 111, 110, 10, container.base.fluidTank));
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return TEXTURE;
	}
}
