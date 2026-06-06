package ic2.core.item.armor;

import com.google.common.base.Function;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IItemHudProvider;
import ic2.core.init.Localization;
import ic2.core.item.capability.CapabilityFluidHandlerItem;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemArmorFluidTank extends ItemArmorUtility implements IItemHudInfo, IItemHudProvider.IItemHudBarProvider
{
	protected final int capacity;
	protected final Fluid allowfluid;

	public ItemArmorFluidTank(ItemName name, String armorName, Fluid allowfluid, int capacity)
	{
		super(name, armorName, EntityEquipmentSlot.CHEST);
		this.setMaxDamage(27);
		this.setMaxStackSize(1);
		this.capacity = capacity;
		this.allowfluid = allowfluid;
		this.addCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, new Function<ItemStack, IFluidHandlerItem>()
		{
			public IFluidHandlerItem apply(@Nullable ItemStack stack)
			{
				return new CapabilityFluidHandlerItem(stack, ItemArmorFluidTank.this.capacity)
				{
					@Override
					public boolean canFillFluidType(FluidStack fluid)
					{
						return fluid != null && fluid.getFluid() == ItemArmorFluidTank.this.allowfluid;
					}

					@Override
					public boolean canDrainFluidType(FluidStack fluid)
					{
						return fluid != null && fluid.getFluid() == ItemArmorFluidTank.this.allowfluid;
					}

					public ItemStack getContainer()
					{
						ItemStack ret = super.getContainer();
						ItemArmorFluidTank.this.Updatedamage(ret);
						return ret;
					}
				};
			}
		});
	}

	public void filltank(ItemStack stack)
	{
		NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(stack);
		NBTTagCompound fluidTag = nbtTagCompound.getCompoundTag("Fluid");
		FluidStack fs = new FluidStack(this.allowfluid, this.capacity);
		fs.writeToNBT(fluidTag);
		nbtTagCompound.setTag("Fluid", fluidTag);
	}

	public double getCharge(ItemStack stack)
	{
		FluidStack fs = FluidUtil.getFluidContained(stack);
		if (fs == null)
		{
			return 0.0;
		}

		double ret = fs.amount;
		return ret > 0.0 ? ret : 0.0;
	}

	public double getMaxCharge(ItemStack stack)
	{
		return this.capacity;
	}

	protected void Updatedamage(ItemStack stack)
	{
		stack.setItemDamage(stack.getMaxDamage() - 1 - (int) Util.map(this.getCharge(stack), this.getMaxCharge(stack), stack.getMaxDamage() - 2));
	}

	public boolean isEmpty(ItemStack stack)
	{
		return FluidUtil.getFluidContained(stack) == null;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced)
	{
		super.addInformation(stack, world, tooltip, advanced);
		FluidStack fs = FluidUtil.getFluidContained(stack);
		if (fs != null)
		{
			tooltip.add("< " + fs.getLocalizedName() + ", " + fs.amount + " mB >");
		} else
		{
			tooltip.add(Localization.translate("ic2.item.FluidContainer.Empty"));
		}
	}

	@Override
	public int getBarPercent(ItemStack stack)
	{
		return this.getMaxDamage(stack) - this.getDamage(stack) * 100 / this.getMaxDamage(stack);
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		FluidStack fs = FluidUtil.getFluidContained(stack);
		if (fs != null)
		{
			info.add("< " + fs.getLocalizedName() + ", " + fs.amount + " mB >");
		} else
		{
			info.add(Localization.translate("ic2.item.FluidContainer.Empty"));
		}

		return info;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return false;
	}
}
