package ic2.core.block.machine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidDistributor extends Ic2Gui<ContainerFluidDistributor>
{
	public GuiFluidDistributor(ContainerFluidDistributor container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(TankGauge.createPlain(this, 29, 38, 55, 47, container.base.fluidTank));
	}

	@Override
	protected void drawForegroundLayer(PoseStack matrices, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(matrices, mouseX, mouseY);
		this.drawString(matrices, 112, 56, Localization.translate("ic2.FluidDistributor.gui.mode.info"), 5752026);
		if (((ContainerFluidDistributor) this.menu).base.getActive())
		{
			this.drawString(matrices, 95, 80, Localization.translate("ic2.FluidDistributor.gui.mode.concentrate"), 5752026);
		} else
		{
			this.drawString(matrices, 95, 80, Localization.translate("ic2.FluidDistributor.gui.mode.distribute"), 5752026);
		}
	}

	@Override
	public boolean m_6375_(double mouseX, double mouseY, int mouseButton)
	{
		mouseX -= this.f_97735_;
		mouseY -= this.f_97736_;
		if (mouseX >= 117.0 && mouseY >= 58.0 && mouseX <= 135.0 && mouseY <= 66.0)
		{
			IC2.network.get(false).initiateClientTileEntityEvent(((ContainerFluidDistributor) this.menu).base, 1);
			return true;
		} else
		{
			mouseX += this.f_97735_;
			mouseY += this.f_97736_;
			return super.m_6375_(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guifluiddistributor.png");
	}
}
