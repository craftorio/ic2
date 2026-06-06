package ic2.core.util;

import ic2.api.info.IInfoProvider;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.item.type.DustResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class ItemInfo implements IInfoProvider
{
	@Override
	public double getEnergyValue(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0.0;
		} else if (StackUtil.checkItemEquality(stack, ItemName.single_use_battery.getItemStack()))
		{
			return 1200.0;
		} else if (StackUtil.checkItemEquality(stack, Items.REDSTONE))
		{
			return 800.0;
		} else
		{
			return StackUtil.checkItemEquality(stack, ItemName.dust.getItemStack(DustResourceType.energium)) ? 16000.0 : 0.0;
		}
	}

	@Override
	public int getFuelValue(ItemStack stack, boolean allowLava)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		}

		if ((
			StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap))
				|| StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))
		)
			&& !ConfigUtil.getBool(MainConfig.get(), "misc/allowBurningScrap"))
		{
			return 0;
		}

		FluidStack liquid = FluidUtil.getFluidContained(stack);
		boolean isLava = liquid != null && liquid.amount > 0 && liquid.getFluid() == FluidRegistry.LAVA;
		if (isLava && !allowLava)
		{
			return 0;
		}

		int ret = TileEntityFurnace.getItemBurnTime(stack);
		return isLava ? ret / 10 : ret;
	}
}
