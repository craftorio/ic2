package ic2.core.util;

import ic2.api.info.IInfoProvider;
import ic2.core.IC2;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.init.MainConfig;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class ItemInfo implements IInfoProvider
{
	@Override
	public double getEnergyValue(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0.0;
		} else if (StackUtil.checkItemEquality(stack, Items.REDSTONE))
		{
			return 800.0;
		} else if (StackUtil.checkItemEquality(stack, Ic2Items.SINGLE_USE_BATTERY))
		{
			return 1200.0;
		} else
		{
			return StackUtil.checkItemEquality(stack, Ic2Items.ENERGIUM_DUST) ? 16000.0 : 0.0;
		}
	}

	@Override
	public int getFuelValue(ItemStack stack, boolean allowLava)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		}

		if ((StackUtil.checkItemEquality(stack, Ic2Items.SCRAP) || StackUtil.checkItemEquality(stack, Ic2Items.SCRAP_BOX))
			&& !ConfigUtil.getBool(MainConfig.get(), "misc/allowBurningScrap"))
		{
			return 0;
		}

		Ic2FluidStack liquid = Ic2FluidStack.get(stack);
		boolean isLava = liquid != null && !liquid.isEmpty() && liquid.getFluid() == Fluids.f_76195_;
		if (isLava && !allowLava)
		{
			return 0;
		}

		int ret = IC2.envProxy.getBurnTime(stack);
		return isLava ? ret / 10 : ret;
	}
}
