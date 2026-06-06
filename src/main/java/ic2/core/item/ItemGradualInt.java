package ic2.core.item;

import ic2.api.item.ICustomDamageItem;
import ic2.core.IC2;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class ItemGradualInt extends ItemIC2 implements ICustomDamageItem
{
	private static final boolean alwaysShowDurability = true;
	private static final String nbtKey = "advDmg";
	private final int maxDamage;

	public ItemGradualInt(ItemName name, int maxDamage)
	{
		super(name);
		this.setNoRepair();
		this.maxDamage = maxDamage;
	}

	public boolean showDurabilityBar(ItemStack stack)
	{
		return true;
	}

	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double) this.getCustomDamage(stack) / this.getMaxCustomDamage(stack);
	}

	public boolean isDamageable()
	{
		return true;
	}

	public boolean isDamaged(ItemStack stack)
	{
		return this.getCustomDamage(stack) > 0;
	}

	public int getDamage(ItemStack stack)
	{
		return this.getCustomDamage(stack);
	}

	@Override
	public int getCustomDamage(ItemStack stack)
	{
		return !stack.hasTagCompound() ? 0 : stack.getTagCompound().getInteger("advDmg");
	}

	public int getMaxDamage(ItemStack stack)
	{
		return this.getMaxCustomDamage(stack);
	}

	@Override
	public int getMaxCustomDamage(ItemStack stack)
	{
		return this.maxDamage;
	}

	public void setDamage(ItemStack stack, int damage)
	{
		int prev = this.getCustomDamage(stack);
		if (damage != prev && BaseElectricItem.logIncorrectItemDamaging)
		{
			IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid gradual item damage application (%d):", damage - prev);
		}
	}

	@Override
	public void setCustomDamage(ItemStack stack, int damage)
	{
		NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
		nbt.setInteger("advDmg", damage);
	}

	@Override
	public boolean applyCustomDamage(ItemStack stack, int damage, EntityLivingBase src)
	{
		this.setCustomDamage(stack, this.getCustomDamage(stack) + damage);
		return true;
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab))
		{
			ItemStack stack = new ItemStack(this);
			this.setCustomDamage(stack, 0);
			subItems.add(stack);
		}
	}
}
