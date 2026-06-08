package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.gui.CustomGauge;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.VanillaButton;
import ic2.core.init.Localization;
import ic2.core.ref.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiMetalFormer extends Ic2Gui<ContainerMetalFormer>
{
	public GuiMetalFormer(ContainerMetalFormer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 20, 37, container.base));
		this.addElement(CustomGauge.create(this, 52, 39, new CustomGauge.IGaugeRatioProvider()
		{
			@Override
			public double getRatio()
			{
				return container.base.getProgress();
			}
		}, Gauge.GaugeStyle.ProgressMetalFormer));
		this.addElement(new VanillaButton(this, 65, 53, 20, 20, this.createEventSender(0)).withIcon(new Supplier<ItemStack>()
		{
			public ItemStack get()
			{
				switch (container.base.getMode())
				{
					case 0:
						return new ItemStack(Ic2Items.COPPER_CABLE);
					case 1:
						return new ItemStack(Ic2Items.FORGE_HAMMER);
					case 2:
						return new ItemStack(Ic2Items.CUTTER);
					default:
						return null;
				}
			}
		}).withTooltip(new Supplier<String>()
		{
			public String get()
			{
				switch (container.base.getMode())
				{
					case 0:
						return Localization.translate("ic2.MetalFormer.gui.switch.Extruding");
					case 1:
						return Localization.translate("ic2.MetalFormer.gui.switch.Rolling");
					case 2:
						return Localization.translate("ic2.MetalFormer.gui.switch.Cutting");
					default:
						return null;
				}
			}
		}));
		if (RecipeButton.canUse())
		{
			for (int i = 0; i < 3; i++)
			{
				final int mode = i;
				this.addElement(new RecipeButton(this, 52, 39, 46, 9, new String[] { "metal_former" + mode }).withEnableHandler(new IEnableHandler()
				{
					@Override
					public boolean isEnabled()
					{
						return container.base.getMode() == mode;
					}
				}));
			}
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guimetalformer.png");
	}
}
