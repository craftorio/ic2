package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.gui.*;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class GuiSortingMachine extends GuiIC2<ContainerSortingMachine>
{
	public GuiSortingMachine(final ContainerSortingMachine container)
	{
		super(container, 212, 243);
		addElement(EnergyGauge.asBolt(this, 174, 220, container.base));
		for (EnumFacing dir : EnumFacing.VALUES)
		{
			final EnumFacing cDir = dir;
			addElement(Image.create(this, 60, 18 + dir.ordinal() * 20, 18, 18, texture, 256, 256, new FixedSizeOverlaySupplier(18)
			{
				public int getUS()
				{
					return 212;
				}

				public int getVS()
				{
					if (StackUtil.getAdjacentInventory(container.base, cDir) != null)
						return 15;
					return 33;
				}
			}));
			addElement((new CustomButton(this, 42, 18 + dir.ordinal() * 20, 18, 18, new FixedSizeOverlaySupplier(18)
			{
				public int getUS()
				{
					return 230;
				}

				public int getVS()
				{
					if (container.base.defaultRoute != cDir)
						return 15;
					return 33;
				}
			}, texture, createEventSender(dir.ordinal())))
				.withTooltip(() ->
				{
					if (container.base.defaultRoute != cDir)
						return "ic2.SortingMachine.whitelist";
					return "ic2.SortingMachine.default";
				}));
		}
	}

	protected ResourceLocation getTexture()
	{
		return texture;
	}

	private static final ResourceLocation texture = new ResourceLocation("ic2", "textures/gui/GUISortingMachine.png");
}
