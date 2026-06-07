package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IHazmatLike;
import ic2.api.item.IItemHudProvider;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;

public class ItemArmorQuantumSuit extends ItemArmorElectric implements IJetpack, IHazmatLike, IItemHudProvider, DyeableLeatherItem
{
	public static final int[] CHARGED_PROTECTION = new int[] { 3, 6, 8, 3 };
	private static final int defaultColor = -1;

	public ItemArmorQuantumSuit(ArmorMaterial material, EquipmentSlot armorType, Properties settings)
	{
		super(material, armorType, settings, 1.0E7, 12000.0, 4);
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 20000;
	}

	public boolean m_41113_(ItemStack stack)
	{
		return this.m_41121_(stack) != -1;
	}

	public void m_41123_(ItemStack stack)
	{
		CompoundTag nbt = this.getDisplayNbt(stack, false);
		if (nbt != null && nbt.contains("color", 3))
		{
			nbt.m_128473_("color");
			if (nbt.m_128456_())
			{
				assert stack.getTag() != null;
				stack.getTag().m_128473_("display");
			}
		}
	}

	public int m_41121_(ItemStack stack)
	{
		CompoundTag nbt = this.getDisplayNbt(stack, false);
		return nbt != null && nbt.contains("color", 3) ? nbt.getInt("color") : -1;
	}

	public void m_41115_(ItemStack stack, int color)
	{
		CompoundTag nbt = this.getDisplayNbt(stack, true);
		assert nbt != null;
		nbt.putInt("color", color);
	}

	private CompoundTag getDisplayNbt(ItemStack stack, boolean create)
	{
		CompoundTag nbt = stack.getTag();
		if (nbt == null)
		{
			if (!create)
			{
				return null;
			}

			nbt = new CompoundTag();
			stack.m_41751_(nbt);
		}

		CompoundTag ret;
		if (!nbt.contains("display", 10))
		{
			if (!create)
			{
				return null;
			}

			ret = new CompoundTag();
			nbt.put("display", ret);
		} else
		{
			ret = nbt.getCompound("display");
		}

		return ret;
	}

	@Override
	public boolean addsProtection(LivingEntity entity, EquipmentSlot slot, ItemStack stack)
	{
		return ElectricItem.manager.getCharge(stack) > 0.0;
	}

	public boolean absorbFall(ItemStack stack, LivingEntity entity, float distance)
	{
		int fallDamage = Math.max((int) distance - 10, 0);
		double energyCost = this.getEnergyPerDamage() * fallDamage;
		if (energyCost > ElectricItem.manager.getCharge(stack))
		{
			return false;
		}

		ElectricItem.manager.discharge(stack, energyCost, Integer.MAX_VALUE, true, false, false);
		return true;
	}

	public Rarity m_41460_(ItemStack stack)
	{
		return Rarity.RARE;
	}

	public int m_6473_()
	{
		return 0;
	}

	@Override
	public boolean drainEnergy(ItemStack pack, int amount)
	{
		return ElectricItem.manager.discharge(pack, amount + 6, Integer.MAX_VALUE, true, false, false) > 0.0;
	}

	@Override
	public float getPower(ItemStack stack)
	{
		return 1.0F;
	}

	@Override
	public float getDropPercentage(ItemStack stack)
	{
		return 0.05F;
	}

	@Override
	public double getChargeLevel(ItemStack stack)
	{
		return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
	}

	@Override
	public boolean isJetpackActive(ItemStack stack)
	{
		return true;
	}

	@Override
	public float getHoverMultiplier(ItemStack stack, boolean upwards)
	{
		return 0.1F;
	}

	@Override
	public float getWorldHeightDivisor(ItemStack stack)
	{
		return 0.9F;
	}

	@Override
	public boolean doesProvideHUD(ItemStack stack)
	{
		return this.f_40377_ == EquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0;
	}

	@Override
	public HudMode getHudMode(ItemStack stack)
	{
		return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).getShort("HudMode"));
	}
}
