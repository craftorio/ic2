package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.IHasGui;
import ic2.core.init.Localization;
import ic2.core.item.type.MiscResourceType;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.AdvRecipe;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityAssemblyBench extends TileEntityBatchCrafter implements IHasGui, IUpgradableBlock
{
	public static final List<IRecipe> RECIPES = new ArrayList<>();

	@Override
	protected IRecipe findRecipe()
	{
		for (IRecipe recipe : RECIPES)
		{
			if (recipe.matches(this.crafting, this.getWorld()))
			{
				return recipe;
			}
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced)
	{
		tooltip.add("You probably want the " + Localization.translate(this.getBlockType().getUnlocalizedName() + '.' + TeBlock.replicator.getName()));
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
	}

	public static class UuRecipe implements IRecipe
	{
		protected final ItemStack output;
		protected final boolean[][] shape;

		public static TileEntityAssemblyBench.UuRecipe create(ItemStack output, Object... args)
		{
			Queue<String> inputArrangement = new ArrayDeque<>();

			for (Object arg : args)
			{
				if (arg instanceof String)
				{
					String str = (String) arg;
					if (str.isEmpty() || str.length() > 3)
					{
						AdvRecipe.displayError("none or too many crafting columns", "Input: " + str + "\nSize: " + str.length(), output, false);
					}

					inputArrangement.add(str);
				}
			}

			boolean[][] shape = new boolean[3][3];

			for (int y = 0; y < 3; y++)
			{
				String layer = inputArrangement.poll();

				for (int x = 0; x < 3; x++)
				{
					shape[y][x] = layer.charAt(x) != ' ';
				}
			}

			return new TileEntityAssemblyBench.UuRecipe(output, shape);
		}

		public UuRecipe(ItemStack output, boolean[][] shape)
		{
			if (StackUtil.isEmpty(output))
			{
				AdvRecipe.displayError("Empty result", "UU recipe with shape " + Arrays.deepToString(shape), output, false);
			}

			int inputWidth = shape[0].length;

			for (boolean[] col : shape)
			{
				if (col.length != inputWidth)
				{
					AdvRecipe.displayError("Inconsistent recipe shape", "UU recipe with shape " + Arrays.deepToString(shape), output, false);
				}
			}

			this.output = output;
			this.shape = shape;
		}

		public boolean matches(InventoryCrafting inv, World world)
		{
			ItemStack uu = ItemName.misc_resource.getItemStack(MiscResourceType.matter);
			int y = 0;
			int height = inv.getHeight();
			int width = inv.getWidth();

			while (y < height)
			{
				boolean[] layer = this.shape[y];

				for (int x = 0; x < width; x++)
				{
					ItemStack stack = inv.getStackInRowAndColumn(x, y);
					if (layer[x])
					{
						if (!StackUtil.checkItemEquality(stack, uu))
						{
							return false;
						}
					} else if (!StackUtil.isEmpty(stack))
					{
						return false;
					}
				}

				y++;
			}

			return true;
		}

		public ItemStack getRecipeOutput()
		{
			return this.output;
		}

		public ItemStack getCraftingResult(InventoryCrafting inv)
		{
			return this.getRecipeOutput();
		}

		public boolean canFit(int width, int height)
		{
			throw new UnsupportedOperationException();
		}

		public IRecipe setRegistryName(ResourceLocation name)
		{
			throw new UnsupportedOperationException();
		}

		public ResourceLocation getRegistryName()
		{
			throw new UnsupportedOperationException();
		}

		public Class<IRecipe> getRegistryType()
		{
			throw new UnsupportedOperationException();
		}
	}
}
