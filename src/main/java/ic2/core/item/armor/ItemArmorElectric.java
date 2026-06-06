package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.IPseudoDamageItem;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ISpecialArmor;

public abstract class ItemArmorElectric extends ItemArmorIC2 implements ISpecialArmor, IPseudoDamageItem, IElectricItem, IItemHudInfo
{
	protected final double maxCharge;
	protected final double transferLimit;
	protected final int tier;

	public ItemArmorElectric(ItemName name, String armorName, EntityEquipmentSlot armorType, double maxCharge, double transferLimit, int tier)
	{
		super(name, ArmorMaterial.DIAMOND, armorName, armorType, null);
		this.maxCharge = maxCharge;
		this.tier = tier;
		this.transferLimit = transferLimit;
		this.setMaxDamage(27);
		this.setMaxStackSize(1);
		this.setNoRepair();
	}

	public int getItemEnchantability()
	{
		return 0;
	}

	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(ElectricItem.manager.getToolTip(stack));
		info.add(Localization.translate("ic2.item.tooltip.PowerTier", this.tier));
		return info;
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab))
		{
			ElectricItemManager.addChargeVariants(this, subItems);
		}
	}

	public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot)
	{
		if (source.isUnblockable())
		{
			return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
		}

		double absorptionRatio = this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio();
		int energyPerDamage = this.getEnergyPerDamage();
		int damageLimit = Integer.MAX_VALUE;
		if (energyPerDamage > 0)
		{
			damageLimit = (int) Math.min(damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / energyPerDamage);
		}

		return new ISpecialArmor.ArmorProperties(0, absorptionRatio, damageLimit);
	}

	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot)
	{
		return ElectricItem.manager.getCharge(armor) >= this.getEnergyPerDamage()
			? (int) Math.round(20.0 * this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio())
			: 0;
	}

	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot)
	{
		ElectricItem.manager.discharge(stack, damage * this.getEnergyPerDamage(), Integer.MAX_VALUE, true, false, false);
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
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return false;
	}

	public void setDamage(ItemStack stack, int damage)
	{
		int prev = this.getDamage(stack);
		if (damage != prev && BaseElectricItem.logIncorrectItemDamaging)
		{
			IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", damage - prev);
		}
	}

	@Override
	public void setStackDamage(ItemStack stack, int damage)
	{
		super.setDamage(stack, damage);
	}

	public abstract double getDamageAbsorptionRatio();

	public abstract int getEnergyPerDamage();

	protected final double getBaseAbsorptionRatio()
	{
		switch (this.armorType)
		{
			case HEAD:
				return 0.15;
			case CHEST:
				return 0.4;
			case LEGS:
				return 0.3;
			case FEET:
				return 0.15;
			default:
				return 0.0;
		}
	}
}
