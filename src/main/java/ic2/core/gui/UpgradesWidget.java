package ic2.core.gui;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

public class UpgradesWidget extends GuiElement<UpgradesWidget>
{
	private final List<ItemStack> compatibleUpgrades;
	private static final int xCoord = 96;
	private static final int yCoord = 128;
	private static final int iWidth = 10;
	private static final int iHeight = 10;

	public UpgradesWidget(GuiIC2<?> gui, int x, int y, IUpgradableBlock te)
	{
		super(gui, x, y, 10, 10);
		this.compatibleUpgrades = getCompatibleUpgrades(te);
	}

	@Override
	public void drawBackground(int mouseX, int mouseY)
	{
		bindCommonTexture();
		this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, 96.0, 128.0);
	}

	@Override
	protected List<String> getToolTip()
	{
		List<String> ret = super.getToolTip();
		ret.add(Localization.translate("ic2.generic.text.upgrade"));

		for (ItemStack itemstack : this.compatibleUpgrades)
		{
			ret.add(itemstack.getDisplayName());
		}

		return ret;
	}

	private static List<ItemStack> getCompatibleUpgrades(IUpgradableBlock block)
	{
		List<ItemStack> ret = new ArrayList<>();
		Set<UpgradableProperty> properties = block.getUpgradableProperties();

		for (ItemStack stack : UpgradeRegistry.getUpgrades())
		{
			IUpgradeItem item = (IUpgradeItem) stack.getItem();
			if (item.isSuitableFor(stack, properties))
			{
				ret.add(stack);
			}
		}

		return ret;
	}
}
