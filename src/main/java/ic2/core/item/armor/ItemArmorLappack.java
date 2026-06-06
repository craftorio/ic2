package ic2.core.item.armor;

import ic2.core.profile.NotExperimental;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotExperimental
public class ItemArmorLappack extends ItemArmorElectric
{
	public ItemArmorLappack()
	{
		super(ItemName.lappack, "lappack", EntityEquipmentSlot.CHEST, 2.0E7, 2500.0, 4);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return true;
	}

	@Override
	public double getDamageAbsorptionRatio()
	{
		return 0.0;
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 0;
	}

	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.UNCOMMON;
	}
}
