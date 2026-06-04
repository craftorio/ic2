package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerReplicator;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.gui.*;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiReplicator extends GuiIC2<ContainerReplicator>
{
	public GuiReplicator(final ContainerReplicator container)
	{
		super(container, 184);
		addElement(EnergyGauge.asBolt(this, 136, 84, container.base));
		addElement(TankGauge.createNormal(this, 27, 30, container.base.fluidTank));
		addElement((new ItemImage(this, 91, 17, () -> container.base.pattern)).withTooltip(() ->
		{
			TileEntityReplicator te = container.base;
			if (te.pattern == null)
				return null;
			String uuReq = Util.toSiString(te.patternUu, 4) + Localization.translate("ic2.generic.text.bucketUnit");
			String euReq = Util.toSiString(te.patternEu, 4) + Localization.translate("ic2.generic.text.EU");
			return te.pattern.getDisplayName() + " UU: " + uuReq + " EU: " + euReq;
		}));
		addElement((new CustomButton(this, 80, 16, 9, 18, createEventSender(0)))
			.withTooltip("ic2.Replicator.gui.info.last"));
		addElement((new CustomButton(this, 109, 16, 9, 18, createEventSender(1)))
			.withTooltip("ic2.Replicator.gui.info.next"));
		addElement((new CustomButton(this, 75, 82, 16, 16, createEventSender(3)))
			.withTooltip("ic2.Replicator.gui.info.Stop"));
		addElement((new CustomButton(this, 92, 82, 16, 16, createEventSender(4)))
			.withTooltip("ic2.Replicator.gui.info.single"));
		addElement((new CustomButton(this, 109, 82, 16, 16, createEventSender(5)))
			.withTooltip("ic2.Replicator.gui.info.repeat"));
		addElement(Text.create(this, 49, 36, 96, 16, TextProvider.of(() ->
		{
			TileEntityReplicator te = container.base;
			if (te.getMode() == TileEntityReplicator.Mode.STOPPED)
				return Localization.translate("ic2.Replicator.gui.info.Waiting");
			int progressUu = 0;
			int progressEu = 0;
			if (te.patternUu != 0.0D)
				progressUu = Math.min((int) Math.round(100.0D * te.uuProcessed / te.patternUu), 100);
			return String.format("UU:%d%%  EU:%d%%  >%s", progressUu, progressEu, (te.getMode() == TileEntityReplicator.Mode.SINGLE) ? "" : ">");
		}), () -> (container.base.getMode() == TileEntityReplicator.Mode.STOPPED) ? 15461152 : 2157374, false, 4, 0, false, true));
	}

	public ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUIReplicator.png");
	}
}
