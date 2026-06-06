package ic2.core.recipe;

import ic2.api.item.ICustomDamageItem;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.util.StackUtil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class GradualRecipe implements IRecipe
{
	public final ICustomDamageItem item;
	public final ItemStack chargeMaterial;
	public final int amount;
	public final boolean hidden;
	private ResourceLocation name;

	public static void addAndRegister(ItemStack itemToFill, int amount, Object... args)
	{
		try
		{
			if (itemToFill == null)
			{
				AdvRecipe.displayError("Null item to fill", null, null, true);
			} else
			{
				if (!(itemToFill.getItem() instanceof ICustomDamageItem))
				{
					AdvRecipe.displayError("Filling item must extends ItemGradualInt", null, itemToFill, true);
				}

				ICustomDamageItem fillingItem = (ICustomDamageItem) itemToFill.getItem();
				Boolean hidden = false;
				ItemStack filler = null;
				Object[] var6 = args;
				int var7 = var6.length;
				int var8 = 0;

				while (true)
				{
					label37:
					{
						if (var8 < var7)
						{
							Object o = var6[var8];
							if (o instanceof Boolean)
							{
								hidden = (Boolean) o;
								break label37;
							}

							try
							{
								filler = AdvRecipe.getRecipeObject(o).getInputs().get(0);
							} catch (IndexOutOfBoundsException e)
							{
								AdvRecipe.displayError("Invalid filler item: " + o, null, itemToFill, true);
								break label37;
							} catch (Exception e)
							{
								e.printStackTrace();
								AdvRecipe.displayError("unknown type", "O: " + o + "\nT: " + o.getClass().getName(), itemToFill, true);
								break label37;
							}
						}

						Rezepte.registerRecipe(new GradualRecipe(fillingItem, filler, amount, hidden));
						break;
					}

					var8++;
				}
			}
		} catch (RuntimeException e)
		{
			if (!MainConfig.ignoreInvalidRecipes)
			{
				throw e;
			}
		}
	}

	public GradualRecipe(ICustomDamageItem item, ItemStack chargeMaterial, int amount)
	{
		this(item, chargeMaterial, amount, false);
	}

	public GradualRecipe(ICustomDamageItem item, ItemStack chargeMaterial, int amount, boolean hidden)
	{
		this.item = item;
		this.chargeMaterial = chargeMaterial;
		this.amount = amount;
		this.hidden = hidden;
	}

	public boolean matches(InventoryCrafting ic, World world)
	{
		return this.getCraftingResult(ic) != StackUtil.emptyStack;
	}

	public ItemStack getCraftingResult(InventoryCrafting ic)
	{
		ItemStack gridItem = null;
		int chargeMats = 0;

		for (int slot = 0; slot < ic.getSizeInventory(); slot++)
		{
			ItemStack stack = ic.getStackInSlot(slot);
			if (!StackUtil.isEmpty(stack))
			{
				if (gridItem == null && stack.getItem() == this.item)
				{
					gridItem = stack;
				} else
				{
					if (!StackUtil.checkItemEquality(stack, this.chargeMaterial))
					{
						return StackUtil.emptyStack;
					}

					chargeMats++;
				}
			}
		}

		if (gridItem != null && chargeMats > 0)
		{
			ItemStack stack = gridItem.copy();
			int damage = this.item.getCustomDamage(stack) - this.amount * chargeMats;
			if (damage > this.item.getMaxCustomDamage(stack))
			{
				damage = this.item.getMaxCustomDamage(stack);
			} else if (damage < 0)
			{
				damage = 0;
			}

			this.item.setCustomDamage(stack, damage);
			return stack;
		} else
		{
			return StackUtil.emptyStack;
		}
	}

	public ItemStack getRecipeOutput()
	{
		return new ItemStack((Item) this.item);
	}

	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}

	public boolean canShow()
	{
		return AdvRecipe.canShow(new Object[] { this.chargeMaterial }, this.getRecipeOutput(), this.hidden);
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
		return x * y >= 2;
	}
}
