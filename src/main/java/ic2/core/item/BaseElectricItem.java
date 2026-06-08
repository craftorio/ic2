package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public abstract class BaseElectricItem extends Item implements IElectricItem, IItemHudInfo
{
	protected final double maxCharge;
	protected final double transferLimit;
	protected final int tier;

	public BaseElectricItem(Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(settings);
		this.maxCharge = maxCharge;
		this.transferLimit = transferLimit;
		this.tier = tier;
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return false;
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return this.maxCharge;
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return this.tier;
	}

	@Override
	public double getTransferLimit(ItemStack stack)
	{
		return this.transferLimit;
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(ElectricItem.manager.getToolTip(stack));
		return info;
	}

	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems)
	{
		if (this.allowedIn(tab))
		{
			ElectricItemManager.addChargeVariants(this, subItems);
		}
	}

	public boolean isBarVisible(ItemStack stack)
	{
		return ElectricItem.manager.getCharge(stack) <= ElectricItem.manager.getMaxCharge(stack);
	}

	public int getBarWidth(ItemStack stack)
	{
		return (int) Math.round(ElectricItem.manager.getStackChargeLevel(stack) * 13.0);
	}

	public int getBarColor(ItemStack stack)
	{
		return Mth.hsvToRgb((float) (ElectricItem.manager.getStackChargeLevel(stack) / 3.0), 1.0F, 1.0F);
	}
}
