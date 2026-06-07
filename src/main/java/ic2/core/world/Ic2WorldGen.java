package ic2.core.world;

import ic2.core.IC2;
import ic2.core.block.misc.RubberLogBlock;
import ic2.core.proxy.EnvProxy;
import ic2.core.ref.Ic2Blocks;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.Holder;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration.TreeConfigurationBuilder;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

public class Ic2WorldGen
{
	private static final WeightedStateProvider RUBBER_LOG_PROVIDER = new WeightedStateProvider(
		SimpleWeightedRandomList.m_146263_()
			.m_146271_(Ic2Blocks.RUBBER_LOG.defaultBlockState(), 16)
			.m_146271_((BlockState) Ic2Blocks.RUBBER_LOG.defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_north), 1)
			.m_146271_((BlockState) Ic2Blocks.RUBBER_LOG.defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_east), 1)
			.m_146271_((BlockState) Ic2Blocks.RUBBER_LOG.defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_south), 1)
			.m_146271_((BlockState) Ic2Blocks.RUBBER_LOG.defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_west), 1)
	);
	public static final CompletableFuture<Holder<ConfiguredFeature<TreeConfiguration, ?>>> RUBBER_TREE = register(
		"rubber_tree",
		Feature.f_65760_,
		new TreeConfigurationBuilder(
			RUBBER_LOG_PROVIDER,
			new StraightTrunkPlacer(4, 4, 0),
			BlockStateProvider.m_191382_(Ic2Blocks.RUBBER_LEAVES),
			RubberTreeFoliagePlacer.INSTANCE,
			new TwoLayersFeatureSize(1, 0, 1)
		)
			.m_68244_()
			.m_68251_()
	);

	public static void init()
	{
		attachOreFeatureToBiome("ore_lead");
		attachOreFeatureToBiome("ore_lead_lower");
		attachOreFeatureToBiome("ore_tin_small");
		attachOreFeatureToBiome("ore_tin_upper");
		attachOreFeatureToBiome("ore_uranium");
		attachOreFeatureToBiome("ore_uranium_buried");
		attachOreFeatureToBiome("ore_uranium_large");
		attachRubberTreeFeatureToBiome("trees_rubber_jungle", EnvProxy.BiomeSelector.JUNGLE);
		attachRubberTreeFeatureToBiome("trees_rubber_forest", EnvProxy.BiomeSelector.FOREST);
		attachRubberTreeFeatureToBiome("trees_rubber_swamp", EnvProxy.BiomeSelector.SWAMP);
		RubberTreeFoliagePlacer.init();
	}

	private static void attachOreFeatureToBiome(String id)
	{
		IC2.envProxy.attachPlacedFeatureToBiome(IC2.getIdentifier(id), EnvProxy.BiomeSelector.OVERWORLD, Decoration.UNDERGROUND_ORES);
	}

	private static void attachRubberTreeFeatureToBiome(String id, EnvProxy.BiomeSelector selector)
	{
		IC2.envProxy.attachPlacedFeatureToBiome(IC2.getIdentifier(id), selector, Decoration.VEGETAL_DECORATION);
	}

	private static <FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> register(
		String name, F feature, FC config
	)
	{
		return IC2.envProxy.registerConfiguredFeature(IC2.getIdentifier(name), feature, config);
	}
}
