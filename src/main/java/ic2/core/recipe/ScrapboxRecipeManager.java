package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IScrapboxManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class ScrapboxRecipeManager implements IScrapboxManager
{
	private final List<ScrapboxRecipeManager.Drop> drops = new ArrayList<>();

	public static void setup()
	{
		if (Recipes.scrapboxDrops != null)
		{
			throw new IllegalStateException("already initialized");
		}

		Recipes.scrapboxDrops = new ScrapboxRecipeManager();
	}

	public static void load()
	{
		((ScrapboxRecipeManager) Recipes.scrapboxDrops).addBuiltinDrops();
	}

	private ScrapboxRecipeManager()
	{
	}

	public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, CompoundTag metadata, boolean replace)
	{
		if (!input.matches(new ItemStack(Ic2Items.SCRAP_BOX)))
		{
			throw new IllegalArgumentException("currently only scrap boxes are supported");
		}

		if (metadata != null && metadata.contains("weight"))
		{
			if (output.size() != 1)
			{
				throw new IllegalArgumentException("currently only a single drop stack is supported");
			} else
			{
				float weight = metadata.getFloat("weight");
				if (!(weight <= 0.0F) && !Float.isInfinite(weight) && !Float.isNaN(weight))
				{
					this.addDrop(output.iterator().next(), weight);
					return true;
				} else
				{
					throw new IllegalArgumentException("invalid weight");
				}
			}
		} else
		{
			throw new IllegalArgumentException("no weight metadata");
		}
	}

	public boolean addRecipe(IRecipeInput input, CompoundTag metadata, boolean replace, ItemStack... outputs)
	{
		return this.addRecipe(input, Arrays.asList(outputs), metadata, replace);
	}

	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest)
	{
		if (!StackUtil.isEmpty(input) && input.getItem() == Ic2Items.SCRAP_BOX)
		{
			if (this.drops.isEmpty())
			{
				return null;
			}

			float chance = IC2.random.nextFloat() * ScrapboxRecipeManager.Drop.topChance;
			int low = 0;
			int high = this.drops.size() - 1;

			while (low < high)
			{
				int mid = high + low >>> 1;
				if (chance < this.drops.get(mid).upperChanceBound)
				{
					high = mid;
				} else
				{
					low = mid + 1;
				}
			}

			ItemStack drop = this.drops.get(low).item.m_41777_();
			return new MachineRecipe<>(Recipes.inputFactory.forItem(Ic2Items.SCRAP_BOX), Collections.singletonList(drop))
				.getResult(StackUtil.copyShrunk(input, 1));
		} else
		{
			return null;
		}
	}

	@Override
	public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput)
	{
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.apply(input, false);
		return result != null && !result.getOutput().isEmpty() ? new RecipeOutput(null, new ArrayList<>(result.getOutput())) : null;
	}

	@Override
	public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isIterable()
	{
		return false;
	}

	public void addDrop(ItemStack drop, float rawChance)
	{
		this.drops.add(new ScrapboxRecipeManager.Drop(drop, rawChance));
	}

	@Override
	public ItemStack getDrop(ItemStack input, boolean adjustInput)
	{
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.apply(input, false);
		if (result != null && !result.getOutput().isEmpty())
		{
			if (adjustInput)
			{
				input.m_41764_(StackUtil.getSize(result.getAdjustedInput()));
			}

			return result.getOutput().iterator().next();
		} else
		{
			return null;
		}
	}

	@Override
	public Map<ItemStack, Float> getDrops()
	{
		Map<ItemStack, Float> ret = new HashMap<>(this.drops.size());

		for (ScrapboxRecipeManager.Drop drop : this.drops)
		{
			ret.put(drop.item, drop.originalChance / ScrapboxRecipeManager.Drop.topChance);
		}

		return ret;
	}

	private void addBuiltinDrops()
	{
		if (IC2.suddenlyHoes)
		{
			this.addDrop(Items.f_42424_, 9001.0F);
		} else
		{
			this.addDrop(Items.f_42424_, 5.01F);
		}

		this.addDrop(Blocks.f_50493_, 5.0F);
		this.addDrop(Items.f_42398_, 4.0F);
		this.addDrop(Blocks.f_50034_, 3.0F);
		this.addDrop(Blocks.f_49994_, 3.0F);
		this.addDrop(Blocks.f_50134_, 2.0F);
		this.addDrop(Items.f_42583_, 2.0F);
		this.addDrop(Items.f_42410_, 1.5F);
		this.addDrop(Items.f_42406_, 1.5F);
		this.addDrop(Ic2Items.FILLED_TIN_CAN, 1.5F);
		this.addDrop(Items.f_42420_, 1.0F);
		this.addDrop(Items.f_42421_, 1.0F);
		this.addDrop(Items.f_42422_, 1.0F);
		this.addDrop(Blocks.f_50135_, 1.0F);
		this.addDrop(Items.f_42438_, 1.0F);
		this.addDrop(Items.f_42454_, 1.0F);
		this.addDrop(Items.f_42402_, 1.0F);
		this.addDrop(Items.f_42500_, 1.0F);
		this.addDrop(Items.f_42486_, 0.9F);
		this.addDrop(Items.f_42580_, 0.9F);
		this.addDrop(Blocks.f_50133_, 0.9F);
		this.addDrop(Items.f_42582_, 0.9F);
		this.addDrop(Items.f_42449_, 0.01F);
		this.addDrop(Items.REDSTONE, 0.9F);
		this.addDrop(Ic2Items.RUBBER, 0.8F);
		this.addDrop(Items.f_42525_, 0.8F);
		this.addDrop(Ic2Items.COAL_DUST, 0.8F);
		this.addDrop(Ic2Items.COPPER_DUST, 0.8F);
		this.addDrop(Ic2Items.TIN_DUST, 0.8F);
		this.addDrop(Ic2Items.SINGLE_USE_BATTERY, 0.7F);
		this.addDrop(Ic2Items.IRON_DUST, 0.7F);
		this.addDrop(Ic2Items.GOLD_DUST, 0.7F);
		this.addDrop(Items.f_42518_, 0.6F);
		this.addDrop(Blocks.f_49996_, 0.5F);
		this.addDrop(Items.f_42476_, 0.01F);
		this.addDrop(Blocks.f_49995_, 0.5F);
		this.addDrop(Items.f_42502_, 0.5F);
		this.addDrop(Items.f_42415_, 0.1F);
		this.addDrop(Items.f_42616_, 0.05F);
		this.addDrop(Items.f_42584_, 0.08F);
		this.addDrop(Items.f_42585_, 0.04F);
		this.addDrop(Items.f_42521_, 0.8F);
		this.addDrop(Blocks.f_152505_, 0.7F);
		this.addDrop(Ic2Items.TIN_ORE, 0.7F);
	}

	private void addDrop(Block block, float rawChance)
	{
		this.addDrop(new ItemStack(block), rawChance);
	}

	private void addDrop(Item item, float rawChance)
	{
		this.addDrop(new ItemStack(item), rawChance);
	}

	private static class Drop
	{
		final ItemStack item;
		final float originalChance;
		final float upperChanceBound;
		static float topChance;

		Drop(ItemStack item, float chance)
		{
			this.item = item;
			this.originalChance = chance;
			this.upperChanceBound = topChance += chance;
		}
	}
}
