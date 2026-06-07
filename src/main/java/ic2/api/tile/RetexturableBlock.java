package ic2.api.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface RetexturableBlock
{
	boolean retexture(BlockState var1, Level var2, BlockPos var3, Direction var4, Player var5, BlockState var6, String var7, Direction var8, int[] var9);
}
