package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class BaseElectricItem extends ItemIC2 implements IPseudoDamageItem, IElectricItem, IItemHudInfo
{
	public static final boolean logIncorrectItemDamaging = ConfigUtil.getBool(MainConfig.get(), "debug/logIncorrectItemDamaging");
	protected final double maxCharge;
	protected final double transferLimit;
	protected final int tier;

	public BaseElectricItem(ItemName name, double maxCharge, double transferLimit, int tier)
	{
		super(name);
		this.maxCharge = maxCharge;
		this.transferLimit = transferLimit;
		this.tier = tier;
		this.setMaxDamage(27);
		this.setMaxStackSize(1);
		this.setNoRepair();
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

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab))
		{
			ElectricItemManager.addChargeVariants(this, subItems);
		}
	}

	public void setDamage(ItemStack stack, int damage)
	{
		int prev = this.getDamage(stack);
		if (damage != prev && logIncorrectItemDamaging)
		{
			IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", damage - prev);
		}
	}

	@Override
	public void setStackDamage(ItemStack stack, int damage)
	{
		super.setDamage(stack, damage);
	}
}
