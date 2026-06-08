package ic2.core.util;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class ConfigUtil
{
	public static List<String> asList(String str)
	{
		str = str.trim();
		return str.isEmpty() ? Collections.emptyList() : Arrays.asList(str.split("\\s*,\\s*"));
	}

	public static List<IRecipeInput> asRecipeInputList(Config config, String key)
	{
		Config.Value value = config.get(key);

		try
		{
			try
			{
				return asRecipeInputList(value.getString());
			} catch (ParseException e)
			{
				throw new Config.ParseException("Invalid value", value, e);
			}
		} catch (Config.ParseException e)
		{
			displayError(e, key);
			return null;
		}
	}

	public static List<ItemStack> asStackList(Config config, String key)
	{
		Config.Value value = config.get(key);

		try
		{
			try
			{
				return asStackList(value.getString());
			} catch (ParseException e)
			{
				throw new Config.ParseException("Invalid value", value, e);
			}
		} catch (Config.ParseException e)
		{
			displayError(e, key);
			return null;
		}
	}

	public static ItemStack asStack(Config config, String key)
	{
		Config.Value value = config.get(key);

		try
		{
			try
			{
				return asStack(value.getString());
			} catch (ParseException e)
			{
				throw new Config.ParseException("Invalid value", value, e);
			}
		} catch (Config.ParseException e)
		{
			displayError(e, key);
			return null;
		}
	}

	public static String getString(Config config, String key)
	{
		return config.get(key).getString();
	}

	public static boolean getBool(Config config, String key)
	{
		Config.Value value = config.get(key);

		try
		{
			return value.getBool();
		} catch (Config.ParseException e)
		{
			displayError(e, key);
			return false;
		}
	}

	public static int getInt(Config config, String key)
	{
		Config.Value value = config.get(key);

		try
		{
			return value.getInt();
		} catch (Config.ParseException e)
		{
			displayError(e, key);
			return 0;
		}
	}

	public static float getFloat(Config config, String key)
	{
		Config.Value value = config.get(key);

		try
		{
			return value.getFloat();
		} catch (Config.ParseException e)
		{
			displayError(e, key);
			return 0.0F;
		}
	}

	public static double getDouble(Config config, String key)
	{
		Config.Value value = config.get(key);

		try
		{
			return value.getDouble();
		} catch (Config.ParseException e)
		{
			displayError(e, key);
			return 0.0;
		}
	}

	public static int[] asIntArray(Config config, String key)
	{
		Config.Value value = config.get(key);

		try
		{
			return asList(value.getString()).stream().mapToInt(Integer::parseInt).toArray();
		} catch (NumberFormatException e)
		{
			displayError(new Config.ParseException("Invalid value", value, e), key);
			return new int[0];
		}
	}

	public static List<ItemStack> asStackList(String str) throws ParseException
	{
		List<String> parts = asList(str);
		List<ItemStack> ret = new ArrayList<>(parts.size());

		for (String part : parts)
		{
			ret.add(asStack(part));
		}

		return ret;
	}

	public static List<IRecipeInput> asRecipeInputList(String str) throws ParseException
	{
		return asRecipeInputList(str, false);
	}

	public static List<IRecipeInput> asRecipeInputList(String str, boolean allowNull) throws ParseException
	{
		List<String> parts = asList(str);
		List<IRecipeInput> ret = new ArrayList<>(parts.size());

		for (String part : parts)
		{
			IRecipeInput input = asRecipeInput(part);
			if (input == null && !allowNull)
			{
				throw new ParseException("There is no item matching " + part + ".", -1);
			}

			ret.add(input);
		}

		return ret;
	}

	private static ItemStack asStack(String str, boolean checkAmount) throws ParseException
	{
		String[] parts = str.split("(?=(\\*))");
		String itemName = parts[0];
		Item item = Util.getItem(itemName);
		if (item == null)
		{
			return null;
		}

		ItemStack stack = new ItemStack(item);
		int amount = 1;

		for (int i = 1; i < parts.length; i++)
		{
			String tmp = parts[i];
			if (tmp.startsWith("*"))
			{
				if (!checkAmount)
				{
					throw new ParseException("We do not support amount here.", -1);
				}

				amount = Integer.parseInt(tmp.substring(1));
			}
		}

		if (checkAmount)
		{
			stack = StackUtil.setSize(stack, amount);
		}

		return stack;
	}

	public static ItemStack asStack(String str) throws ParseException
	{
		return asStack(str, false);
	}

	public static ItemStack asStackWithAmount(String str) throws ParseException
	{
		return asStack(str, true);
	}

	public static String fromStack(ItemStack stack)
	{
		return fromStack(stack, false);
	}

	private static String fromStack(ItemStack stack, boolean amount)
	{
		String ret = Util.getName(stack.getItem()).toString();
		if (amount)
		{
			ret = ret + "*" + StackUtil.getSize(stack);
		}

		return ret;
	}

	public static String fromStackWithAmount(ItemStack stack)
	{
		return fromStack(stack, true);
	}

	public static IRecipeInput asRecipeInput(Config.Value value)
	{
		try
		{
			return asRecipeInput(value.getString());
		} catch (ParseException e)
		{
			throw new Config.ParseException("Invalid value", value, e);
		}
	}

	private static IRecipeInput asRecipeInput(String str, boolean checkAmount) throws ParseException
	{
		String[] parts = str.split("(?=(\\*))");
		String itemName = parts[0];
		if (!itemName.startsWith("Tag:") && !itemName.startsWith("Fluid:"))
		{
			ItemStack stack = asStack(str, checkAmount);
			return stack == null ? null : Recipes.inputFactory.forStack(stack);
		}

		Integer amount = null;

		for (int i = 1; i < parts.length; i++)
		{
			String tmp = parts[i];
			if (tmp.startsWith("*"))
			{
				if (!checkAmount)
				{
					throw new ParseException("We do not support amount here.", -1);
				}

				amount = Integer.parseInt(tmp.substring(1));
			}
		}

		if (itemName.startsWith("Tag:"))
		{
			if (amount == null)
			{
				amount = 1;
			}

			return Recipes.inputFactory.forTag(itemName.substring("Tag:".length()), amount);
		} else if (itemName.startsWith("Fluid:"))
		{
			if (amount == null)
			{
				amount = 1000;
			}
			// TODO
			ResourceLocation id = ResourceLocation.withDefaultNamespace(itemName.substring("Fluid:".length()));
			Fluid fluid = Util.getFluid(id);
			return fluid == null ? null : Recipes.inputFactory.forFluidContainer(fluid, amount);
		} else
		{
			return null;
		}
	}

	public static IRecipeInput asRecipeInput(String str) throws ParseException
	{
		return asRecipeInput(str, false);
	}

	public static IRecipeInput asRecipeInputWithAmount(String str) throws ParseException
	{
		return asRecipeInput(str, true);
	}

	private static void displayError(Config.ParseException e, String key)
	{
		IC2.sideProxy
			.displayError(
				"The IC2 config file contains an invalid entry for %s.\n\n%s%s",
				key,
				e.getMessage(),
				e.getCause() != null ? "\n\n" + e.getCause().getMessage() : ""
			);
	}
}
