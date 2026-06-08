package ic2.core.item.armor;

import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.ref.Ic2Fluids;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemArmorJetpack extends ItemArmorFluidTank implements IJetpack
{
	public ItemArmorJetpack(Properties settings)
	{
		super(Ic2ArmorMaterials.JET_PACK, settings, Ic2Fluids.BIOGAS.still(), 30000);
	}

	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks)
	{
		if (true)
		{
			ItemStack stack = new ItemStack(this);
			this.filltank(stack);
			stacks.add(stack);
			stacks.add(new ItemStack(this));
		}
	}

	@Override
	public boolean drainEnergy(ItemStack pack, int amount)
	{
		if (this.isEmpty(pack))
		{
			return false;
		}

		Ic2FluidStack fs = this.drainMb(pack, amount, true, null);
		if (fs.getAmountMb() < amount)
		{
			return false;
		}

		this.drainMb(pack, amount, false, null);
		return true;
	}

	@Override
	public float getPower(ItemStack stack)
	{
		return 1.0F;
	}

	@Override
	public float getDropPercentage(ItemStack stack)
	{
		return 0.2F;
	}

	@Override
	public boolean isJetpackActive(ItemStack stack)
	{
		return true;
	}

	@Override
	public double getChargeLevel(ItemStack stack)
	{
		return this.getCharge(stack) / this.getMaxCharge(stack);
	}

	@Override
	public float getHoverMultiplier(ItemStack stack, boolean upwards)
	{
		return 0.2F;
	}

	@Override
	public float getWorldHeightDivisor(ItemStack stack)
	{
		return 1.0F;
	}
}
