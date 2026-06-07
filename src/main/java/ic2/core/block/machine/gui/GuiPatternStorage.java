package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerPatternStorage;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.gui.CustomButton;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.ItemImage;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiPatternStorage extends Ic2Gui<ContainerPatternStorage>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guipatternstorage.png");

	public GuiPatternStorage(ContainerPatternStorage container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new CustomButton(this, 7, 19, 9, 18, this.createEventSender(0)).withTooltip("ic2.PatternStorage.gui.info.last"));
		this.addElement(new CustomButton(this, 36, 19, 9, 18, this.createEventSender(1)).withTooltip("ic2.PatternStorage.gui.info.next"));
		this.addElement(new CustomButton(this, 10, 37, 16, 8, this.createEventSender(2)).withTooltip("ic2.PatternStorage.gui.info.export"));
		this.addElement(new CustomButton(this, 26, 37, 16, 8, this.createEventSender(3)).withTooltip("ic2.PatternStorage.gui.info.import"));
		this.addElement(TextLabel.create(this, this.imageWidth / 2, 30, TextProvider.of(new Supplier<String>()
		{
			public String get()
			{
				TileEntityPatternStorage te = container.base;
				return Math.min(te.index + 1, te.maxIndex) + " / " + te.maxIndex;
			}
		}), 4210752, false, true, false));
		this.addElement(TextLabel.create(this, 10, 48, TextProvider.ofTranslated("ic2.generic.text.Name"), 16777215, false));
		this.addElement(TextLabel.create(this, 10, 59, TextProvider.ofTranslated("ic2.generic.text.UUMatte"), 16777215, false));
		this.addElement(TextLabel.create(this, 10, 70, TextProvider.ofTranslated("ic2.generic.text.Energy"), 16777215, false));
		IEnableHandler patternInfoEnabler = new IEnableHandler()
		{
			@Override
			public boolean isEnabled()
			{
				return container.base.pattern != null;
			}
		};
		this.addElement(TextLabel.create(this, 80, 48, TextProvider.of(new Supplier<String>()
		{
			public String get()
			{
				ItemStack pattern = container.base.pattern;
				return pattern != null ? pattern.m_41786_().getString() : null;
			}
		}), 16777215, false).withEnableHandler(patternInfoEnabler));
		this.addElement(TextLabel.create(this, 80, 59, TextProvider.of(new Supplier<String>()
		{
			public String get()
			{
				return Util.toSiString(container.base.patternUu, 4) + Localization.translate("ic2.generic.text.bucketUnit");
			}
		}), 16777215, false).withEnableHandler(patternInfoEnabler));
		this.addElement(TextLabel.create(this, 80, 70, TextProvider.of(new Supplier<String>()
		{
			public String get()
			{
				return Util.toSiString(container.base.patternEu, 4) + Localization.translate("ic2.generic.text.EU");
			}
		}), 16777215, false).withEnableHandler(patternInfoEnabler));
		this.addElement(new ItemImage(this, 152, 29, new Supplier<ItemStack>()
		{
			public ItemStack get()
			{
				return container.base.pattern;
			}
		}));
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}
