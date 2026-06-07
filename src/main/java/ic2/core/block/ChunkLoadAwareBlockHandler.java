package ic2.core.block;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

public final class ChunkLoadAwareBlockHandler
{
	private static final int CS = 16;
	private static final int CSM = 15;
	private static final Map<BlockState, ChunkLoadAwareBlock> stateMap = new IdentityHashMap<>();

	public static void init()
	{
		for (Block rawBlock : Registry.BLOCK)
		{
			if (rawBlock instanceof ChunkLoadAwareBlock block)
			{
				for (BlockState state : block.getLoadAwareState(rawBlock))
				{
					stateMap.put(state, block);
				}
			}
		}
	}

	public static void onChunkLoad(LevelChunk chunk)
	{
		processChunk(chunk, true);
	}

	public static void onChunkUnload(LevelChunk chunk)
	{
		processChunk(chunk, false);
	}

	private static void processChunk(LevelChunk chunk, boolean isLoad)
	{
		for (LevelChunkSection section : chunk.m_7103_())
		{
			if (!section.m_188008_())
			{
				PalettedContainer<BlockState> container = section.m_63019_();
				if (container.m_63109_(stateMap::containsKey))
				{
					Level world = chunk.m_62953_();
					MutableBlockPos pos = new MutableBlockPos(chunk.m_7697_().m_45604_(), section.m_63017_(), chunk.m_7697_().m_45605_());
					BlockState lastState = null;
					ChunkLoadAwareBlock lastBlock = null;

					for (int y = 0; y < 16; y++)
					{
						for (int z = 0; z < 16; z++)
						{
							for (int x = 0; x < 16; x++)
							{
								BlockState state = (BlockState) container.m_63087_(x, y, z);
								if (state != lastState)
								{
									lastState = state;
									lastBlock = stateMap.get(state);
								}

								if (lastBlock != null)
								{
									pos.set(pos.getX() & -16 | x, pos.getY() & -16 | y, pos.getZ() & -16 | z);
									if (isLoad)
									{
										lastBlock.onLoad(state, world, pos);
									} else
									{
										lastBlock.onUnload(state, world, pos);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
