package ic2.core.item.armor;

import ic2.api.item.IItemHudInfo;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.StandardFluidItem;
import ic2.core.init.Localization;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public abstract class ItemArmorFluidTank extends ItemArmorUtility implements StandardFluidItem, IItemHudInfo
{
	protected final int capacity;
	protected final Fluid allowfluid;

	public ItemArmorFluidTank(ArmorMaterial material, Properties settings, Fluid allowfluid, int capacity)
	{
		super(material, settings, EquipmentSlot.CHEST);
		this.capacity = capacity;
		this.allowfluid = allowfluid;
	}

	@Override
	public int getCapacityMb(ItemStack stack)
	{
		return this.capacity;
	}

	@Override
	public boolean canFill(ItemStack stack, Ic2FluidStack fs)
	{
		return fs.getFluid() == this.allowfluid;
	}

	public void filltank(ItemStack stack)
	{
		this.fillMb(stack, Ic2FluidStack.create(this.allowfluid, Integer.MAX_VALUE), false, null);
	}

	public double getCharge(ItemStack stack)
	{
		return Ic2FluidStack.get(stack).getAmountMb();
	}

	private double getChargeLevel(ItemStack stack)
	{
		return this.getCharge(stack) / this.capacity;
	}

	public double getMaxCharge(ItemStack stack)
	{
		return this.capacity;
	}

	public boolean isEmpty(ItemStack stack)
	{
		return Ic2FluidStack.get(stack).isEmpty();
	}

	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		super.appendHoverText(stack, world, tooltip, advanced);
		tooltip.add(Component.literal(getContentDescription(stack)));
	}

	public boolean isBarVisible(ItemStack stack)
	{
		return true;
	}

	public int getBarWidth(ItemStack stack)
	{
		return (int) Math.round(this.getChargeLevel(stack) * 13.0);
	}

	public int getBarColor(ItemStack stack)
	{
		return Mth.hsvToRgb((float) (this.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new ArrayList<>();
		info.add(getContentDescription(stack));
		return info;
	}

	private static String getContentDescription(ItemStack stack)
	{
		Ic2FluidStack fs = Ic2FluidStack.get(stack);
		return !fs.isEmpty()
			? String.format("< %s, %d mB >", Registry.FLUID.getKey(fs.getFluid()), fs.getAmountMb())
			: Localization.translate("ic2.item.FluidContainer.Empty");
	}

	public boolean isValidRepairItem(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return false;
	}
}
