package ic2.core.model;

import ic2.core.block.state.Ic2BlockState;
import ic2.core.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer.StateImplementation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelComparator
{
	private static final EnumFacing[] facings = new EnumFacing[] {
		null, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST
	};
	private static final Byte UNCACHEABLE = (byte) -1;
	private static final ConcurrentMap<ModelComparator.CacheKey, Byte> cache = new ConcurrentHashMap<>();

	public static boolean isEqual(IBlockState stateA, IBlockState stateB, World world, BlockPos pos)
	{
		assert stateA != stateB;
		byte renderMask = 0;

		for (EnumFacing facing : EnumFacing.VALUES)
		{
			boolean renderA = stateA.shouldSideBeRendered(world, pos, facing);
			boolean renderB = stateB.shouldSideBeRendered(world, pos, facing);
			if (renderA != renderB)
			{
				return false;
			}

			if (renderA)
			{
				renderMask = (byte) (renderMask | 1 << facing.ordinal());
			}
		}

		ModelComparator.CacheKey cacheKey;
		Byte cacheResult;
		if (stateA.getClass() == stateB.getClass()
			&& (
			stateA.getClass() == StateImplementation.class
				|| stateA instanceof Ic2BlockState.Ic2BlockStateInstance
				&& !((Ic2BlockState.Ic2BlockStateInstance) stateA).hasExtraProperties()
				&& !((Ic2BlockState.Ic2BlockStateInstance) stateB).hasExtraProperties()
				|| stateA instanceof IExtendedBlockState
				&& ((IExtendedBlockState) stateA).getClean() == stateA
				&& ((IExtendedBlockState) stateB).getClean() == stateB
		))
		{
			cacheKey = new ModelComparator.CacheKey(stateA, stateB);
			cacheResult = cache.get(cacheKey);
			if (cacheResult != null && cacheResult != UNCACHEABLE)
			{
				return (cacheResult | ~renderMask) == -1;
			}
		} else
		{
			cacheKey = null;
			cacheResult = UNCACHEABLE;
		}

		assert cacheResult == null || cacheResult == UNCACHEABLE;
		BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBakedModel modelA = renderer.getModelForState(stateA);
		IBakedModel modelB = renderer.getModelForState(stateB);
		Class<?> modelCls = modelA.getClass();
		if (modelB.getClass() != modelCls)
		{
			if (cacheResult == null)
			{
				cache.putIfAbsent(cacheKey, (byte) 0);
			}

			return false;
		} else
		{
			if (cacheResult == null
				&& modelCls != SimpleBakedModel.class
				&& modelCls != BasicBakedBlockModel.class
				&& !modelCls.getName().equals("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper$1"))
			{
				if (Util.inDev())
				{
					assert false;
				}

				cacheResult = UNCACHEABLE;
				cache.putIfAbsent(cacheKey, UNCACHEABLE);
			}

			long rand = MathHelper.getPositionRandom(pos);
			byte equal = 63;

			label132:
			for (EnumFacing facing : facings)
			{
				if (cacheResult == null || facing == null || (renderMask & 1 << facing.ordinal()) != 0)
				{
					List<BakedQuad> quadsA = modelA.getQuads(stateA, facing, rand);
					List<BakedQuad> quadsB = modelB.getQuads(stateB, facing, rand);
					if (quadsA.size() != quadsB.size())
					{
						if (cacheResult != null)
						{
							return false;
						}

						if (facing == null)
						{
							equal = 0;
							break;
						}

						equal = (byte) (equal & ~(1 << facing.ordinal()));
					} else if (!quadsA.isEmpty())
					{
						for (int i = 0; i < quadsA.size(); i++)
						{
							if (!Arrays.equals(quadsA.get(i).getVertexData(), quadsB.get(i).getVertexData()))
							{
								if (cacheResult != null)
								{
									return false;
								}

								if (facing == null)
								{
									equal = 0;
									break label132;
								}

								equal = (byte) (equal & ~(1 << facing.ordinal()));
								break;
							}
						}
					}
				}
			}

			if (cacheResult != null)
			{
				return true;
			}

			cache.putIfAbsent(cacheKey, equal);
			return (equal | ~renderMask) == -1;
		}
	}

	public static void onReload()
	{
		cache.clear();
	}

	private static class CacheKey
	{
		private final IBlockState stateA;
		private final IBlockState stateB;

		CacheKey(IBlockState stateA, IBlockState stateB)
		{
			this.stateA = stateA;
			this.stateB = stateB;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj != null && obj.getClass() == ModelComparator.CacheKey.class)
			{
				ModelComparator.CacheKey o = (ModelComparator.CacheKey) obj;
				return this.stateA == o.stateA && this.stateB == o.stateB || this.stateA == o.stateB && this.stateB == o.stateA;
			} else
			{
				return false;
			}
		}

		@Override
		public int hashCode()
		{
			return System.identityHashCode(this.stateA) ^ System.identityHashCode(this.stateB);
		}
	}
}
