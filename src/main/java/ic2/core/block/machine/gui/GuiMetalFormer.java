package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.block.wiring.CableType;
import ic2.core.gui.*;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMetalFormer extends GuiIC2<ContainerMetalFormer>
{
	public GuiMetalFormer(final ContainerMetalFormer container)
	{
		super(container);
		addElement(EnergyGauge.asBolt(this, 20, 37, container.base));
		addElement(CustomGauge.create(this, 52, 39, container.base::getProgress, Gauge.GaugeStyle.ProgressMetalFormer));
		addElement((new VanillaButton(this, 65, 53, 20, 20, createEventSender(0)))
			.withIcon(() ->
			{
				switch (container.base.getMode())
				{
					case 0:
						return ItemName.cable.getItemStack((Enum) CableType.copper);
					case 1:
						return ItemName.forge_hammer.getItemStack();
					case 2:
						return ItemName.cutter.getItemStack();
				}
				return null;
			}).withTooltip(() ->
			{
				switch (container.base.getMode())
				{
					case 0:
						return Localization.translate("ic2.MetalFormer.gui.switch.Extruding");
					case 1:
						return Localization.translate("ic2.MetalFormer.gui.switch.Rolling");
					case 2:
						return Localization.translate("ic2.MetalFormer.gui.switch.Cutting");
				}
				return null;
			}));
		if (RecipeButton.canUse())
			for (int i = 0; i < 3; i++)
			{
				final int mode = i;
				addElement((new RecipeButton(this, 52, 39, 46, 9, new String[] { "metal_former" + mode })).withEnableHandler(() -> (container.base.getMode() == mode)));
			}
	}

	protected ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUIMetalFormer.png");
	}
}
