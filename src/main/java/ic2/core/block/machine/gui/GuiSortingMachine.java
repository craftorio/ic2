package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.FixedSizeOverlaySupplier;
import ic2.core.gui.Image;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class GuiSortingMachine extends GuiIC2<ContainerSortingMachine>
{
	private static final ResourceLocation texture = new ResourceLocation("ic2", "textures/gui/GUISortingMachine.png");

	public GuiSortingMachine(final ContainerSortingMachine container)
	{
		super(container, 212, 243);
		this.addElement(EnergyGauge.asBolt(this, 174, 220, container.base));

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			final EnumFacing cDir = dir;
			this.addElement(Image.create(this, 60, 18 + dir.ordinal() * 20, 18, 18, texture, 256, 256, new FixedSizeOverlaySupplier(18)
			{
				@Override
				public int getUS()
				{
					return 212;
				}

				@Override
				public int getVS()
				{
					return StackUtil.getAdjacentInventory(container.base, cDir) != null ? 15 : 33;
				}
			}));
			this.addElement(new CustomButton(this, 42, 18 + dir.ordinal() * 20, 18, 18, new FixedSizeOverlaySupplier(18)
			{
				@Override
				public int getUS()
				{
					return 230;
				}

				@Override
				public int getVS()
				{
					return container.base.defaultRoute != cDir ? 15 : 33;
				}
			}, texture, this.createEventSender(dir.ordinal())).withTooltip(new Supplier<String>()
			{
				public String get()
				{
					return container.base.defaultRoute != cDir ? "ic2.SortingMachine.whitelist" : "ic2.SortingMachine.default";
				}
			}));
		}
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return texture;
	}
}
