package ic2.data.recipe;

import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CentrifugeRecipeProvider extends Ic2RecipeProvider
{
	public CentrifugeRecipeProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator gen = new BasicMachineRecipeGenerator(consumer, Ic2RecipeSerializers.CENTRIFUGE, true);
		gen.minHeat(250).add(Ic2Items.CLAY_DUST, 4, Ic2Items.SILICON_DIOXIDE_DUST);
		gen.minHeat(100).add(Items.COBBLESTONE, 1, Ic2Items.STONE_DUST);
		gen.minHeat(500).add(Items.QUARTZ, 2, Ic2Items.SMALL_LITHIUM_DUST);
		gen.minHeat(1500).add(Ic2Items.SLAG, 1, new ItemStack(Ic2Items.SMALL_GOLD_DUST), new ItemStack(Ic2Items.COAL_DUST, 5));
		gen.minHeat(5000).add(Ic2Items.RTG_PELLET, 1, new ItemStack(Ic2Items.PLUTONIUM, 3), new ItemStack(Ic2Items.IRON_DUST, 54));
		gen.minHeat(4000).add(Ic2Items.URANIUM, 20, new ItemStack(Ic2Items.URANIUM_238, 112), new ItemStack(Ic2Items.URANIUM_235, 7));
		gen.minHeat(4000)
			.add(
				Ic2Items.DEPLETED_URANIUM_FUEL_ROD,
				1,
				new ItemStack(Ic2Items.SMALL_PLUTONIUM, 1),
				new ItemStack(Ic2Items.URANIUM_238, 4),
				new ItemStack(Ic2Items.IRON_DUST, 1)
			);
		gen.minHeat(4000)
			.add(
				Ic2Items.DEPLETED_DUAL_URANIUM_FUEL_ROD,
				1,
				new ItemStack(Ic2Items.SMALL_PLUTONIUM, 2),
				new ItemStack(Ic2Items.URANIUM_238, 8),
				new ItemStack(Ic2Items.IRON_DUST, 3)
			);
		gen.minHeat(4000)
			.add(
				Ic2Items.DEPLETED_QUAD_URANIUM_FUEL_ROD,
				1,
				new ItemStack(Ic2Items.SMALL_PLUTONIUM, 4),
				new ItemStack(Ic2Items.URANIUM_238, 16),
				new ItemStack(Ic2Items.IRON_DUST, 7)
			);
		gen.minHeat(5000)
			.add(
				Ic2Items.DEPLETED_MOX_FUEL_ROD,
				1,
				new ItemStack(Ic2Items.SMALL_PLUTONIUM, 1),
				new ItemStack(Ic2Items.PLUTONIUM, 3),
				new ItemStack(Ic2Items.IRON_DUST, 1)
			);
		gen.minHeat(5000)
			.add(
				Ic2Items.DEPLETED_DUAL_MOX_FUEL_ROD,
				1,
				new ItemStack(Ic2Items.SMALL_PLUTONIUM, 2),
				new ItemStack(Ic2Items.PLUTONIUM, 6),
				new ItemStack(Ic2Items.IRON_DUST, 3)
			);
		gen.minHeat(5000)
			.add(
				Ic2Items.DEPLETED_QUAD_MOX_FUEL_ROD,
				1,
				new ItemStack(Ic2Items.SMALL_PLUTONIUM, 4),
				new ItemStack(Ic2Items.PLUTONIUM, 12),
				new ItemStack(Ic2Items.IRON_DUST, 7)
			);
		gen.minHeat(500)
			.add(Ic2Items.CRUSHED_COPPER, 1, new ItemStack(Ic2Items.COPPER_DUST), new ItemStack(Ic2Items.SMALL_TIN_DUST), new ItemStack(Ic2Items.STONE_DUST));
		gen.minHeat(2000)
			.add(Ic2Items.CRUSHED_GOLD, 1, new ItemStack(Ic2Items.GOLD_DUST), new ItemStack(Ic2Items.SMALL_SILVER_DUST), new ItemStack(Ic2Items.STONE_DUST));
		gen.minHeat(1500)
			.add(Ic2Items.CRUSHED_IRON, 1, new ItemStack(Ic2Items.IRON_DUST), new ItemStack(Ic2Items.SMALL_GOLD_DUST), new ItemStack(Ic2Items.STONE_DUST));
		gen.minHeat(2000).add(Ic2Items.CRUSHED_LEAD, 1, new ItemStack(Ic2Items.LEAD_DUST), new ItemStack(Ic2Items.STONE_DUST));
		gen.minHeat(2000).add(Ic2Items.CRUSHED_SILVER, 1, new ItemStack(Ic2Items.SILVER_DUST), new ItemStack(Ic2Items.STONE_DUST));
		gen.minHeat(1000)
			.add(Ic2Items.CRUSHED_TIN, 1, new ItemStack(Ic2Items.TIN_DUST), new ItemStack(Ic2Items.SMALL_IRON_DUST), new ItemStack(Ic2Items.STONE_DUST));
		gen.minHeat(3000)
			.add(
				Ic2Items.CRUSHED_URANIUM, 1, new ItemStack(Ic2Items.SMALL_URANIUM_235), new ItemStack(Ic2Items.URANIUM_238, 4), new ItemStack(Ic2Items.STONE_DUST)
			);
		gen.minHeat(500).add(Ic2Items.PURIFIED_COPPER, 1, new ItemStack(Ic2Items.COPPER_DUST), new ItemStack(Ic2Items.SMALL_TIN_DUST));
		gen.minHeat(2000).add(Ic2Items.PURIFIED_GOLD, 1, new ItemStack(Ic2Items.GOLD_DUST), new ItemStack(Ic2Items.SMALL_SILVER_DUST));
		gen.minHeat(1500).add(Ic2Items.PURIFIED_IRON, 1, new ItemStack(Ic2Items.IRON_DUST), new ItemStack(Ic2Items.SMALL_GOLD_DUST));
		gen.minHeat(2000).add(Ic2Items.PURIFIED_LEAD, 1, new ItemStack(Ic2Items.LEAD_DUST), new ItemStack(Ic2Items.SMALL_COPPER_DUST));
		gen.minHeat(2000).add(Ic2Items.PURIFIED_SILVER, 1, new ItemStack(Ic2Items.SILVER_DUST), new ItemStack(Ic2Items.SMALL_LEAD_DUST));
		gen.minHeat(1000).add(Ic2Items.PURIFIED_TIN, 1, new ItemStack(Ic2Items.TIN_DUST), new ItemStack(Ic2Items.SMALL_IRON_DUST));
		gen.minHeat(3000).add(Ic2Items.PURIFIED_URANIUM, 1, new ItemStack(Ic2Items.SMALL_URANIUM_235), new ItemStack(Ic2Items.URANIUM_238, 6));
	}
}
