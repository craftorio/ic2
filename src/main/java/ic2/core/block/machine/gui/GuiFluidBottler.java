package ic2.core.block.machine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidBottler extends Ic2Gui<ContainerFluidBottler>
{
	public GuiFluidBottler(ContainerFluidBottler container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(EnergyGauge.asBolt(this, 12, 35, container.base));
		this.addElement(TankGauge.createNormal(this, 78, 34, container.base.fluidTank));
	}

	@Override
	protected void m_7286_(PoseStack matrices, float delta, int mouseX, int mouseY)
	{
		super.m_7286_(matrices, delta, mouseX, mouseY);
		this.bindTexture();
		int progressSize = Math.round(((ContainerFluidBottler) this.menu).base.getProgress() * 16.0F);
		if (progressSize > 0)
		{
			this.drawTexturedRect(matrices, this.f_97735_ + 61, this.f_97736_ + 36, 198.0, 0.0, progressSize, 13.0);
			this.drawTexturedRect(matrices, this.f_97735_ + 61, this.f_97736_ + 73, 198.0, 0.0, progressSize, 13.0);
			this.drawTexturedRect(matrices, this.f_97735_ + 99, this.f_97736_ + 55, 198.0, 0.0, progressSize, 13.0);
		}
	}

	@Override
	public ResourceLocation getTexture()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guibottler.png");
	}
}
