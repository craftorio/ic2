package ic2.core.item;

import ic2.core.crop.TileEntityCrop;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.Mutable;

public class ItemClassicCell extends Ic2BucketItem implements Ic2FluidItem
{
	private static final int WEED_EX_DRAIN = 50;
	private static Map<Fluid, ItemClassicCell> instances = new IdentityHashMap<>();
	private final Fluid fluid;
	private final int charges;

	public ItemClassicCell(Properties settings, Fluid fluid, int charges)
	{
		super(fluid, settings);
		this.fluid = fluid;
		this.charges = charges;
		if (fluid != null)
		{
			instances.put(fluid, this);
		}
	}

	@Override
	public Item getEmptiedBucketItem()
	{
		return Ic2Items.EMPTY_CELL;
	}

	@Override
	public List<Fluid> getDrainableFluidList()
	{
		return List.of(Fluids.f_76195_, Fluids.f_76193_);
	}

	@Override
	public Item getBucketItem(Fluid fluid)
	{
		if (fluid == Fluids.f_76193_)
		{
			return Ic2Items.WATER_CELL;
		} else
		{
			return fluid == Fluids.f_76195_ ? Ic2Items.LAVA_CELL : Ic2Items.EMPTY_CELL;
		}
	}

	@Override
	public boolean bucketUseOnBlock(UseOnContext context)
	{
		BlockEntity be;
		return (this == Ic2Items.WATER_CELL || this == Ic2Items.WEED_EX_CELL || this == Ic2Items.HYDRATION_CELL)
			&& (be = context.m_43725_().getBlockEntity(context.m_8083_())) instanceof TileEntityCrop
			&& this.useOnCrop(context.m_43722_(), (TileEntityCrop) be, true);
	}

	public boolean useOnCrop(ItemStack stack, TileEntityCrop crop, boolean manual)
	{
		if (this == Ic2Items.WATER_CELL)
		{
			if (crop.getStorageWater() < 10)
			{
				crop.setStorageWater(10);
				return true;
			}
		} else if (this == Ic2Items.WEED_EX_CELL)
		{
			if (crop.applyWeedEx(50, true, manual, false) > 0)
			{
				int uses = this.getUsage(stack) + 1;
				if (uses >= this.charges)
				{
					stack = StackUtil.decSize(stack);
				} else
				{
					this.setUsage(stack, uses);
				}

				return true;
			}
		} else if (this == Ic2Items.HYDRATION_CELL)
		{
			int consumed = this.getUsage(stack) + 1;
			int amount = Math.max(0, this.charges - consumed);
			if (!manual && amount > 180)
			{
				amount = 180;
			}

			amount = crop.applyHydration(amount, false);
			if (amount > 0)
			{
				consumed += amount;
				if (consumed >= this.charges)
				{
					stack = StackUtil.decSize(stack);
				} else
				{
					this.setUsage(stack, consumed);
				}

				return true;
			}
		}

		return false;
	}

	private int getUsage(ItemStack stack)
	{
		if (this.charges <= 1)
		{
			return 0;
		}

		CompoundTag nbt = stack.getTag();
		return nbt != null ? nbt.getInt("uses") : 0;
	}

	private void setUsage(ItemStack stack, int uses)
	{
		if (uses <= 0)
		{
			stack.m_41751_(null);
		} else
		{
			stack.m_41784_().putInt("uses", uses);
		}
	}

	private double getChargeLevel(ItemStack stack)
	{
		return (double) (this.charges - this.getUsage(stack)) / this.charges;
	}

	public boolean m_142522_(ItemStack stack)
	{
		return this.getUsage(stack) > 0;
	}

	public int m_142158_(ItemStack stack)
	{
		return (int) Math.round(this.getChargeLevel(stack) * 13.0);
	}

	public int m_142159_(ItemStack stack)
	{
		return Mth.m_14169_((float) (this.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
	}

	@OnlyIn(Dist.CLIENT)
	public void m_7373_(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		if (this.charges > 1 && stack.m_41613_() == 1 && advanced.m_7050_())
		{
			tooltip.add(Component.m_237110_("item.durability", new Object[] { this.charges - this.getUsage(stack), this.charges }).m_130940_(ChatFormatting.GRAY));
		}
	}

	@Override
	public Ic2FluidStack getFluidStack(ItemStack stack)
	{
		if (this.fluid == Fluids.f_76191_)
		{
			return Ic2FluidStack.EMPTY;
		} else
		{
			return this.fluid != null ? Ic2FluidStack.create(this.fluid, 1000) : null;
		}
	}

	@Override
	public int getCapacityMb(ItemStack stack)
	{
		return this.fluid != null ? 1000 : 0;
	}

	@Override
	public Ic2FluidStack drainMb(ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (stack.m_41613_() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		if (this.fluid == Fluids.f_76191_)
		{
			return Ic2FluidStack.EMPTY;
		}

		if (this.fluid == null)
		{
			return null;
		}

		if (amount < 1000)
		{
			return Ic2FluidStack.EMPTY;
		}

		if (!simulate)
		{
			stack.m_41774_(1);
		}

		if (newStack != null)
		{
			newStack.setValue(new ItemStack(Ic2Items.EMPTY_CELL));
		}

		return Ic2FluidStack.create(this.fluid, 1000);
	}

	@Override
	public int drainMb(ItemStack stack, Ic2FluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (stack.m_41613_() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		if (drainFs.getAmountMb() < 1000)
		{
			return 0;
		}

		if (this.fluid != null && this.fluid != Fluids.f_76191_ && drainFs.hasExactFluid(this.fluid))
		{
			if (!simulate)
			{
				stack.m_41774_(1);
			}

			if (newStack != null)
			{
				newStack.setValue(new ItemStack(Ic2Items.EMPTY_CELL));
			}

			return 1000;
		} else
		{
			return 0;
		}
	}

	@Override
	public int fillMb(ItemStack stack, Ic2FluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}

		if (stack.m_41613_() != 1)
		{
			throw new IllegalArgumentException("invalid stack size");
		}

		if (fillFs.getAmountMb() < 1000)
		{
			return 0;
		}

		if (this.fluid != Fluids.f_76191_)
		{
			return 0;
		}

		ItemClassicCell newItem = instances.get(fillFs.getFluid());
		if (newItem != null && fillFs.hasExactFluid(newItem.fluid))
		{
			if (!simulate)
			{
				stack.m_41774_(1);
			}

			if (newStack != null)
			{
				newStack.setValue(new ItemStack(newItem));
			}

			return 1000;
		} else
		{
			return 0;
		}
	}
}
