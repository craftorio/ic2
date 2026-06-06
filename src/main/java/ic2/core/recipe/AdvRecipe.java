package ic2.core.recipe;

import ic2.api.item.ElectricItem;
import ic2.api.recipe.ICraftingRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

public class AdvRecipe implements IShapedRecipe
{
	private static final boolean debug = Util.hasAssertions();
	public final ItemStack output;
	public final IRecipeInput[] input;
	public final IRecipeInput[] inputMirrored;
	public final int[] masks;
	public final int[] masksMirrored;
	public final int inputWidth;
	public final int inputHeight;
	public final boolean hidden;
	public final boolean consuming;
	private ResourceLocation name;

	public static void addAndRegister(ItemStack result, Object... args)
	{
		try
		{
			Rezepte.registerRecipe(new AdvRecipe(result, args));
		} catch (RuntimeException e)
		{
			if (!MainConfig.ignoreInvalidRecipes)
			{
				throw e;
			}
		}
	}

	public AdvRecipe(ItemStack result, Object... args)
	{
		if (StackUtil.isEmpty(result))
		{
			displayError("null result", null, result, false);
		}

		Map<Character, IRecipeInput> charMapping = new HashMap<>();
		List<String> inputArrangement = new ArrayList<>();
		Character lastChar = null;
		boolean isHidden = false;
		boolean isConsuming = false;
		boolean isFixedSize = false;

		for (Object arg : args)
		{
			if (arg instanceof String)
			{
				if (lastChar == null)
				{
					if (!charMapping.isEmpty())
					{
						displayError("oredict name without preceding char", "Name: " + arg, result, false);
					}

					String str = (String) arg;
					if (str.isEmpty() || str.length() > 3)
					{
						displayError("none or too many crafting columns", "Input: " + str + "\nSize: " + str.length(), result, false);
					}

					inputArrangement.add(str);
				} else
				{
					charMapping.put(lastChar, getRecipeObject(arg));
					lastChar = null;
				}
			} else if (arg instanceof Character)
			{
				if (lastChar != null)
				{
					displayError("two consecutive char definitions", "Input: " + arg + "\nprev. Input: " + lastChar, result, false);
				}

				lastChar = (Character) arg;
			} else if (arg instanceof Boolean)
			{
				isHidden = (Boolean) arg;
			} else if (arg instanceof ICraftingRecipeManager.AttributeContainer)
			{
				isHidden = ((ICraftingRecipeManager.AttributeContainer) arg).hidden;
				isConsuming = ((ICraftingRecipeManager.AttributeContainer) arg).consuming;
				isFixedSize = ((ICraftingRecipeManager.AttributeContainer) arg).fixedSize;
			} else
			{
				if (lastChar == null)
				{
					displayError("two consecutive char definitions", "Input: " + arg + "\nprev. Input: " + lastChar, result, false);
				}

				try
				{
					IRecipeInput last = charMapping.put(lastChar, getRecipeObject(arg));
					if (last != null)
					{
						displayError("duplicate char mapping", "Char: " + lastChar + "\nInput: " + arg + "\nType: " + arg.getClass().getName(), result, false);
					}

					lastChar = null;
				} catch (Exception e)
				{
					e.printStackTrace();
					displayError("unknown type", "Input: " + arg + "\nType: " + arg.getClass().getName(), result, false);
				}
			}
		}

		this.hidden = isHidden;
		this.consuming = isConsuming;
		this.inputHeight = inputArrangement.size();
		if (lastChar != null)
		{
			displayError("one or more unused mapping chars", "Letter: " + lastChar, result, false);
		}

		if (this.inputHeight == 0 || this.inputHeight > 3)
		{
			displayError("none or too many crafting rows", "Size: " + inputArrangement.size(), result, false);
		}

		if (charMapping.size() == 0)
		{
			displayError("no mapping chars", null, result, false);
		}

		this.inputWidth = inputArrangement.get(0).length();
		if (debug && !isFixedSize)
		{
			if (StringUtils.containsOnly(inputArrangement.get(0), new char[] { ' ' }))
			{
				IC2.log.warn(LogCategory.Recipe, "Leading empty row in shaped recipe for %s (%s), from %s.", result, result.getDisplayName(), getCaller());
			}

			if (StringUtils.containsOnly(inputArrangement.get(this.inputHeight - 1), new char[] { ' ' }))
			{
				IC2.log.warn(LogCategory.Recipe, "Trailing empty row in shaped recipe for %s (%s), from %s.", result, result.getDisplayName(), getCaller());
			}

			for (int pass = 0; pass < 2; pass++)
			{
				boolean found = true;

				for (int y = 0; y < this.inputHeight; y++)
				{
					String str = inputArrangement.get(y);
					if (pass == 0 && str.charAt(0) != ' ' || pass == 1 && str.charAt(this.inputWidth - 1) != ' ')
					{
						found = false;
						break;
					}
				}

				if (found)
				{
					if (pass == 0)
					{
						IC2.log.warn(LogCategory.Recipe, "Leading empty column in shaped recipe for %s (%s), from %s.", result, result.getDisplayName(), getCaller());
					} else
					{
						IC2.log.warn(LogCategory.Recipe, "Trailing empty column in shaped recipe for %s (%s), from %s.", result, result.getDisplayName(), getCaller());
					}
				}
			}
		}

		int xMasks = -this.inputWidth + 4;
		int yMasks = -this.inputHeight + 4;
		int mask = 0;
		List<Object> inputs = new ArrayList<>();

		for (int y = 0; y < 3; y++)
		{
			String str = null;
			if (y < this.inputHeight)
			{
				str = inputArrangement.get(y);
				if (str.length() != this.inputWidth)
				{
					displayError("no fixed width", "Expected: " + this.inputWidth + "\nGot: " + str.length(), result, false);
				}
			}

			for (int x = 0; x < 3; x++)
			{
				mask <<= 1;
				if (x < this.inputWidth && str != null)
				{
					char c = str.charAt(x);
					if (c != ' ')
					{
						if (!charMapping.containsKey(c))
						{
							displayError("missing char mapping", "Letter: " + c, result, false);
						}

						inputs.add(charMapping.get(c));
						mask |= 1;
					}
				}
			}
		}

		this.input = inputs.toArray(new IRecipeInput[0]);
		boolean mirror = false;
		if (this.inputWidth != 1)
		{
			for (String s : inputArrangement)
			{
				if (s.charAt(0) != s.charAt(this.inputWidth - 1))
				{
					mirror = true;
					break;
				}
			}
		}

		if (!mirror)
		{
			this.inputMirrored = null;
		} else
		{
			IRecipeInput[] tmp = new IRecipeInput[9];
			int i = 0;
			int j = 0;

			while (i < 9)
			{
				if ((mask & 1 << 8 - i) != 0)
				{
					tmp[i] = this.input[j];
					j++;
				}

				i++;
			}

			IRecipeInput old = tmp[0];
			tmp[0] = tmp[2];
			tmp[2] = old;
			IRecipeInput var36 = tmp[3];
			tmp[3] = tmp[5];
			tmp[5] = var36;
			IRecipeInput var37 = tmp[6];
			tmp[6] = tmp[8];
			tmp[8] = var37;
			this.inputMirrored = new IRecipeInput[this.input.length];
			j = 0;
			int jx = 0;

			while (j < 9)
			{
				if (tmp[j] != null)
				{
					this.inputMirrored[jx] = tmp[j];
					jx++;
				}

				j++;
			}
		}

		this.masks = new int[xMasks * yMasks];
		if (!mirror)
		{
			this.masksMirrored = null;
		} else
		{
			this.masksMirrored = new int[this.masks.length];
		}

		for (int y = 0; y < yMasks; y++)
		{
			int yMask = mask >>> y * 3;

			for (int x = 0; x < xMasks; x++)
			{
				int xyMask = yMask >>> x;
				this.masks[x + y * xMasks] = xyMask;
				if (mirror)
				{
					this.masksMirrored[x + y * xMasks] = xyMask << 2 & 292 | xyMask & 146 | xyMask >>> 2 & 73;
				}
			}
		}

		this.output = result;
	}

	public boolean matches(InventoryCrafting inventorycrafting, World world)
	{
		return this.getCraftingResult(inventorycrafting) != StackUtil.emptyStack;
	}

	public ItemStack getCraftingResult(InventoryCrafting inventorycrafting)
	{
		int size = inventorycrafting.getSizeInventory();
		int mask = 0;

		for (int i = 0; i < size; i++)
		{
			mask <<= 1;
			if (!StackUtil.isEmpty(inventorycrafting.getStackInSlot(i)))
			{
				mask |= 1;
			}
		}

		if (size == 4)
		{
			mask = (mask & 12) << 5 | (mask & 3) << 4;
		}

		if (checkMask(mask, this.masks))
		{
			ItemStack ret = this.checkItems(inventorycrafting, this.input);
			if (!StackUtil.isEmpty(ret))
			{
				return ret;
			}
		}

		if (this.masksMirrored != null && checkMask(mask, this.masksMirrored))
		{
			ItemStack ret = this.checkItems(inventorycrafting, this.inputMirrored);
			if (!StackUtil.isEmpty(ret))
			{
				return ret;
			}
		}

		return StackUtil.emptyStack;
	}

	public ItemStack getRecipeOutput()
	{
		return this.output;
	}

	public static boolean canShow(Object[] input, ItemStack output, boolean hidden)
	{
		return !hidden || !ConfigUtil.getBool(MainConfig.get(), "misc/hideSecretRecipes");
	}

	public boolean canShow()
	{
		return canShow(this.input, this.output, this.hidden);
	}

	public static List<ItemStack> expand(Object o)
	{
		List<ItemStack> ret = new ArrayList<>();
		if (o instanceof IRecipeInput)
		{
			ret.addAll(((IRecipeInput) o).getInputs());
		} else if (o instanceof String)
		{
			String s = (String) o;
			if (s.startsWith("liquid$"))
			{
				String name = s.substring(7);
				Fluid fluid = FluidRegistry.getFluid(name);
				ret.addAll(RecipeInputFluidContainer.getFluidContainer(fluid));
			} else
			{
				for (ItemStack stack : OreDictionary.getOres((String) o))
				{
					if (!StackUtil.isEmpty(stack))
					{
						ret.add(stack);
					}
				}
			}
		} else if (o instanceof ItemStack)
		{
			if (!StackUtil.isEmpty((ItemStack) o))
			{
				ret.add((ItemStack) o);
			}
		} else if (o.getClass().isArray())
		{
			assert Array.getLength(o) != 0 : "empty array";

			for (int i = 0; i < Array.getLength(o); i++)
			{
				ret.addAll(expand(Array.get(o, i)));
			}
		} else
		{
			if (!(o instanceof Iterable))
			{
				displayError("unknown type", "Input: " + o + "\nType: " + o.getClass().getName(), null, false);
				return null;
			}

			assert ((Iterable) o).iterator().hasNext() : "emtpy iterable";

			for (Object o2 : (Iterable) o)
			{
				ret.addAll(expand(o2));
			}
		}

		return ret;
	}

	public static List<ItemStack>[] expandArray(Object[] array)
	{
		List<ItemStack>[] ret = new List[array.length];

		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == null)
			{
				ret[i] = null;
			} else
			{
				ret[i] = expand(array[i]);
			}
		}

		return ret;
	}

	public static void displayError(String cause, String tech, ItemStack result, boolean shapeless)
	{
		String msg = "An invalid crafting recipe was attempted to be added. This could happen due to a bug in IndustrialCraft 2 or an addon.\n\n(Technical information: Adv"
			+ (shapeless ? "Shapeless" : "")
			+ "Recipe, "
			+ cause
			+ ")\n"
			+ (result != null ? "Output: " + result + "\n" : "")
			+ (tech != null ? tech + "\n" : "")
			+ "Source: "
			+ getCaller();
		if (MainConfig.ignoreInvalidRecipes)
		{
			IC2.log.warn(LogCategory.Recipe, msg);
			throw new RuntimeException(msg);
		}

		IC2.platform.displayError(msg);
	}

	private static String getCaller()
	{
		String ret = "unknown";

		for (StackTraceElement st : Thread.currentThread().getStackTrace())
		{
			String className = st.getClassName();
			int pkgSeparator = className.lastIndexOf(46);
			String pkg = pkgSeparator == -1 ? "" : className.substring(0, pkgSeparator);
			if (!className.equals("ic2.core.recipe.AdvRecipe")
				&& !className.equals("ic2.core.recipe.AdvShapelessRecipe")
				&& !className.equals("ic2.core.recipe.AdvCraftingRecipeManager")
				&& !pkg.startsWith("ic2.api")
				&& !pkg.startsWith("java."))
			{
				ret = className + "." + st.getMethodName() + "(" + st.getFileName() + ":" + st.getLineNumber() + ")";
				break;
			}
		}

		return ret;
	}

	private static boolean checkMask(int mask, int[] request)
	{
		for (int cmpMask : request)
		{
			if (mask == cmpMask)
			{
				return true;
			}
		}

		return false;
	}

	static IRecipeInput getRecipeObject(Object o)
	{
		if (o == null)
		{
			throw new NullPointerException("Null recipe input object.");
		}

		if (o instanceof IRecipeInput)
		{
			return (IRecipeInput) o;
		}

		if (o instanceof ItemStack)
		{
			return Recipes.inputFactory.forStack((ItemStack) o);
		}

		if (o instanceof Block)
		{
			return Recipes.inputFactory.forStack(new ItemStack((Block) o));
		}

		if (o instanceof Item)
		{
			return Recipes.inputFactory.forStack(new ItemStack((Item) o));
		}

		if (o instanceof String)
		{
			return Recipes.inputFactory.forOreDict((String) o);
		}

		if (o instanceof Fluid)
		{
			return Recipes.inputFactory.forFluidContainer((Fluid) o);
		}

		if (o instanceof FluidStack)
		{
			return Recipes.inputFactory.forFluidContainer(((FluidStack) o).getFluid(), ((FluidStack) o).amount);
		}

		if (o instanceof Iterable)
		{
			List<IRecipeInput> list = new ArrayList<>();

			for (Object o1 : (Iterable) o)
			{
				list.add(getRecipeObject(o1));
			}

			return Recipes.inputFactory.forAny(list);
		} else
		{
			if (!o.getClass().isArray())
			{
				throw new IllegalArgumentException("Invalid object found as RecipeInput: " + o);
			}

			IRecipeInput[] inputs = new IRecipeInput[Array.getLength(o)];

			for (int i = 0; i < inputs.length; i++)
			{
				inputs[i] = getRecipeObject(Array.get(o, i));
			}

			return Recipes.inputFactory.forAny(inputs);
		}
	}

	private ItemStack checkItems(IInventory inventory, IRecipeInput[] request)
	{
		int size = inventory.getSizeInventory();
		double outputCharge = 0.0;
		int i = 0;
		int j = 0;

		while (i < size)
		{
			ItemStack offer = inventory.getStackInSlot(i);
			if (!StackUtil.isEmpty(offer))
			{
				if (!request[j++].matches(offer))
				{
					return StackUtil.emptyStack;
				}

				outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
			}

			i++;
		}

		ItemStack ret = this.output.copy();
		ElectricItem.manager.charge(ret, outputCharge, Integer.MAX_VALUE, true, false);
		return ret;
	}

	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		return this.consuming ? NonNullList.withSize(inv.getSizeInventory(), StackUtil.emptyStack) : ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}

	public IRecipe setRegistryName(ResourceLocation name)
	{
		this.name = name;
		return this;
	}

	public ResourceLocation getRegistryName()
	{
		return this.name;
	}

	public Class<IRecipe> getRegistryType()
	{
		return IRecipe.class;
	}

	public boolean canFit(int x, int y)
	{
		return this.inputWidth <= x && this.inputHeight <= y;
	}

	@Override
	public int getRecipeWidth()
	{
		return this.inputWidth;
	}

	@Override
	public int getRecipeHeight()
	{
		return this.inputHeight;
	}

	public NonNullList<Ingredient> getIngredients()
	{
		NonNullList<Ingredient> list = NonNullList.create();
		if (!this.hidden)
		{
			int mask = this.masks[0];
			int actualIngredient = 0;

			for (int x = 0; x < 9; x++)
			{
				if ((mask >>> 8 - x & 1) != 0)
				{
					list.add(this.input[actualIngredient++].getIngredient());
				} else
				{
					list.add(Ingredient.EMPTY);
				}
			}
		}

		return list;
	}

	public boolean isDynamic()
	{
		return this.hidden;
	}
}
