package ic2.data.recipe;

import com.mojang.datafixers.util.Pair;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.data.recipe.helper.CannerBottleRecipeGenerator;
import ic2.data.recipe.helper.CannerEnrichRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class CannerRecipeProvider extends Ic2RecipeProvider
{
	public CannerRecipeProvider(DataGenerator root)
	{
		super(root);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		generateBottle(consumer);
		generateEnrich(consumer);
	}

	private static void generateBottle(Consumer<FinishedRecipe> consumer)
	{
		CannerBottleRecipeGenerator gen = new CannerBottleRecipeGenerator(consumer);
		gen.add(itemInput(Ic2Items.FUEL_ROD), itemInput(Ic2Items.URANIUM), new ItemStack(Ic2Items.URANIUM_FUEL_ROD), "uranium_fuel_rod");
		gen.add(itemInput(Ic2Items.FUEL_ROD), itemInput(Ic2Items.MOX), new ItemStack(Ic2Items.MOX_FUEL_ROD), "mox_fuel_rod");

		for (Item item : Registry.ITEM)
		{
			FoodProperties comp = item.getFoodProperties();
			if (comp != null)
			{
				int hunger = Math.min(64, comp.getNutrition());
				if (hunger >= 1)
				{
					boolean harmful = false;

					for (Pair<MobEffectInstance, Float> effectPair : comp.getEffects())
					{
						if (((MobEffectInstance) effectPair.getFirst()).getEffect().getCategory() == MobEffectCategory.HARMFUL)
						{
							harmful = true;
							break;
						}
					}

					if (harmful)
					{
						hunger /= 2;
						if (hunger < 1)
						{
							continue;
						}
					}

					int fillItemCount = item != Items.POISONOUS_POTATO && item != Items.ROTTEN_FLESH ? 1 : 2;
					gen.add(
						itemInput(Ic2Items.TIN_CAN, hunger),
						itemInput(item, fillItemCount),
						new ItemStack(Ic2Items.FILLED_TIN_CAN, hunger),
						Registry.ITEM.getKey(item).getPath() + "_filled_tin_can"
					);
				}
			}
		}
	}

	private static void generateEnrich(Consumer<FinishedRecipe> consumer)
	{
		CannerEnrichRecipeGenerator gen = new CannerEnrichRecipeGenerator(consumer);
		gen.add(bucket(Fluids.WATER), itemInput(Ic2Items.CF_POWDER), bucket(Ic2Fluids.CONSTRUCTION_FOAM.still()), "construction_foam");
		gen.add(bucket(Fluids.WATER), tagInput(Ic2ItemTags.LAPIS_DUSTS, 8), bucket(Ic2Fluids.COOLANT.still()), "coolant_from_water");
		gen.add(bucket(Ic2Fluids.DISTILLED_WATER.still()), tagInput(Ic2ItemTags.LAPIS_DUSTS), bucket(Ic2Fluids.COOLANT.still()), "coolant_from_distilled_water");
		gen.add(bucket(Fluids.WATER), itemInput(Ic2Items.BIO_CHAFF), bucket(Ic2Fluids.BIOMASS.still()), "biomass");
	}
}
