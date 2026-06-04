package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.gui.BasicButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAdvMiner extends GuiIC2<ContainerAdvMiner>
{
	public GuiAdvMiner(final ContainerAdvMiner container)
	{
		super(container, 203);
		addElement(EnergyGauge.asBolt(this, 12, 55, container.base));
		addElement(BasicButton.create(this, 133, 101, createEventSender(0), BasicButton.ButtonStyle.AdvMinerReset)
			.withTooltip("ic2.AdvMiner.gui.switch.reset"));
		addElement(BasicButton.create(this, 123, 27, createEventSender(1), BasicButton.ButtonStyle.AdvMinerMode)
			.withTooltip("ic2.AdvMiner.gui.switch.mode"));
		addElement(BasicButton.create(this, 129, 45, createEventSender(2), BasicButton.ButtonStyle.AdvMinerSilkTouch)
			.withTooltip(() -> Localization.translate("ic2.AdvMiner.gui.switch.silktouch", container.base.silkTouch)));
	}

	protected void drawForegroundLayer(int mouseX, int mouseY)
	{
		BlockPos target = this.container.base.getMineTarget();
		if (target != null)
		{
			BlockPos pos = this.container.base.getPos();
			this.fontRenderer.drawString(Localization.translate("ic2.AdvMiner.gui.info.minelevel", target.getX() - pos.getX(), target.getZ() - pos.getZ(), target.getY() - pos.getY()), 28, 105, 2157374);
		}
		if (this.container.base.blacklist)
		{
			this.fontRenderer.drawString(Localization.translate("ic2.AdvMiner.gui.mode.blacklist"), 40, 31, 2157374);
		} else
		{
			this.fontRenderer.drawString(Localization.translate("ic2.AdvMiner.gui.mode.whitelist"), 40, 31, 2157374);
		}
		super.drawForegroundLayer(mouseX, mouseY);
	}

	public ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUIAdvMiner.png");
	}
}
