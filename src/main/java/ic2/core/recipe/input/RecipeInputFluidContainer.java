package ic2.core.recipe.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.mutable.MutableObject;

public class RecipeInputFluidContainer extends RecipeInputBase
{
	public final Fluid fluid;
	public final int amount;
	private static volatile List<ItemStack> containerItems = null;

	RecipeInputFluidContainer(Fluid fluid)
	{
		this(fluid, 1000);
	}

	public RecipeInputFluidContainer(Fluid fluid, int amount)
	{
		this.fluid = fluid;
		this.amount = amount;
	}

	@Override
	public boolean matches(ItemStack subject)
	{
		Ic2FluidStack fs = Ic2FluidStack.get(subject);
		return fs != null && !fs.isEmpty() ? fs.getFluid() == this.fluid && fs.getAmountMb() >= this.amount : this.fluid == null;
	}

	@Override
	public int getAmount()
	{
		return 1;
	}

	@Override
	protected List<ItemStack> listStacks()
	{
		return getFluidContainer(this.fluid);
	}

	@Override
	public String toString()
	{
		return "RInputFluidContainer<" + this.amount + "x" + this.fluid + ">";
	}

	@Override
	public boolean equals(Object obj)
	{
		RecipeInputFluidContainer other;
		return obj != null && this.getClass() == obj.getClass() && (other = (RecipeInputFluidContainer) obj).fluid == this.fluid && other.amount == this.amount;
	}

	@Override
	public JsonElement toJson()
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("fluid", Registry.FLUID.getKey(this.fluid).toString());
		obj.addProperty("amount", this.amount);
		return obj;
	}

	public static List<ItemStack> getFluidContainer(Fluid fluid)
	{
		List<ItemStack> containerItems = getContainerItems();
		if (fluid == null)
		{
			return containerItems;
		}

		List<ItemStack> ret = new ArrayList<>();
		Ic2FluidStack fillFs = Ic2FluidStack.create(fluid, Integer.MAX_VALUE);
		MutableObject<ItemStack> container = new MutableObject();

		for (ItemStack stack : containerItems)
		{
			int amount = FluidHandler.fillMb(stack.copy(), fillFs, false, container);
			if (amount > 0 && container.getValue() != null)
			{
				Ic2FluidStack fs = FluidHandler.drainMb((ItemStack) container.getValue(), Integer.MAX_VALUE, true, null);
				if (fs != null && fs.getAmountMb() > 0)
				{
					ret.add((ItemStack) container.getValue());
				}
			}
		}

		return ret;
	}

	private static List<ItemStack> getContainerItems()
	{
		List<ItemStack> ret = containerItems;
		if (ret != null)
		{
			return ret;
		}

		ret = new ArrayList<>();
		MutableObject<ItemStack> container = new MutableObject();

		for (Item item : Registry.ITEM)
		{
			if (item != Items.AIR)
			{
				ItemStack stack = new ItemStack(item);
				assert !stack.isEmpty();
				Ic2FluidStack fs = FluidHandler.drainMb(stack, Integer.MAX_VALUE, false, container);
				if (fs != null)
				{
					stack = (ItemStack) container.getValue();
					if (!stack.isEmpty())
					{
						fs = FluidHandler.drainMb(stack, Integer.MAX_VALUE, true, null);
						if (fs != null && fs.isEmpty())
						{
							boolean found = false;

							for (ItemStack s : ret)
							{
								if (StackUtil.checkItemEqualityStrict(s, stack))
								{
									found = true;
									break;
								}
							}

							if (!found)
							{
								ret.add(stack);
							}
						}
					}
				}
			}
		}

		containerItems = ret;
		return ret;
	}
}
