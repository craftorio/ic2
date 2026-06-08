package ic2.core.world;

import com.mojang.serialization.Codec;
import ic2.core.IC2;

import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageSetter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;

public final class RubberTreeFoliagePlacer extends FoliagePlacer
{
	public static final RubberTreeFoliagePlacer INSTANCE = new RubberTreeFoliagePlacer();
	public static final Codec<RubberTreeFoliagePlacer> CODEC = Codec.unit(INSTANCE);
	public static final FoliagePlacerType<?> TYPE = registerFoliagePlacer("rubber_tree", CODEC);

	public static void init()
	{
	}

	RubberTreeFoliagePlacer()
	{
		super(ConstantInt.of(2), ConstantInt.of(0));
	}

	protected FoliagePlacerType<?> type()
	{
		return TYPE;
	}

	protected void createFoliage(
		LevelSimulatedReader world,
		FoliageSetter replacer,
		RandomSource random,
		TreeConfiguration config,
		int trunkHeight,
		FoliageAttachment treeNode,
		int foliageHeight,
		int radius,
		int offset
	)
	{
		int startY;
		if (trunkHeight < 4)
		{
			startY = 0;
		} else if (trunkHeight < 7)
		{
			startY = 2;
		} else
		{
			startY = 3;
		}

		BlockPos center = treeNode.pos();
		MutableBlockPos pos = new MutableBlockPos();

		for (int y = startY; y < trunkHeight; y++)
		{
			for (int x = -radius; x <= radius; x++)
			{
				for (int z = -radius; z <= radius; z++)
				{
					pos.setWithOffset(center, x, y - trunkHeight, z);
					int chance = y + 4 - trunkHeight;
					int dx = Math.abs(x);
					int dz = Math.abs(z);
					if (dx <= 1 && dz <= 1 || dx <= 1 && (chance <= 1 || random.nextInt(chance) == 0) || dz <= 1 && (chance <= 1 || random.nextInt(chance) == 0)
					)
					{
						FoliagePlacer.tryPlaceLeaf(world, replacer, random, config, pos);
					}
				}
			}
		}

		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				if (x == 0 || z == 0)
				{
					pos.setWithOffset(center, x, 0, z);
					FoliagePlacer.tryPlaceLeaf(world, replacer, random, config, pos);
				}
			}
		}

		for (int i = 0; i < foliageHeight; i++)
		{
			pos.setWithOffset(center, 0, i + 1, 0);
			FoliagePlacer.tryPlaceLeaf(world, replacer, random, config, pos);
		}
	}

	public int foliageHeight(RandomSource random, int trunkHeight, TreeConfiguration config)
	{
		return 1 + trunkHeight / 4 + random.nextInt(2);
	}

	protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int radius, boolean giantTrunk)
	{
		return false;
	}

	private static <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(String name, Codec<T> codec)
	{
		return IC2.envProxy.registerFoliagePlacer(IC2.getIdentifier(name), codec);
	}
}
