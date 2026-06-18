package ic2.core.block;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
		for (Block rawBlock : BuiltInRegistries.BLOCK)
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
		LevelChunkSection[] sections = chunk.getSections();

		for (int sectionIdx = 0; sectionIdx < sections.length; sectionIdx++)
		{
			LevelChunkSection section = sections[sectionIdx];
			if (!section.hasOnlyAir())
			{
				PalettedContainer<BlockState> container = section.getStates();
				if (container.maybeHas(stateMap::containsKey))
				{
					Level world = chunk.getLevel();
					if (world.isClientSide) return;
					MutableBlockPos pos = new MutableBlockPos();
					pos.set(chunk.getPos().getMinBlockX(), chunk.getMinBuildHeight() + sectionIdx * 16, chunk.getPos().getMinBlockZ());
					BlockState lastState = null;
					ChunkLoadAwareBlock lastBlock = null;

					for (int y = 0; y < 16; y++)
					{
						for (int z = 0; z < 16; z++)
						{
							for (int x = 0; x < 16; x++)
							{
								BlockState state = (BlockState) container.get(x, y, z);
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
