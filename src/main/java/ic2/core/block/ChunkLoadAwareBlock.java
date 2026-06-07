package ic2.core.block;

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ChunkLoadAwareBlock
{
	default Collection<BlockState> getLoadAwareState(Block block)
	{
		return block.m_49965_().m_61056_();
	}

	void onLoad(BlockState var1, Level var2, BlockPos var3);

	void onUnload(BlockState var1, Level var2, BlockPos var3);
}
