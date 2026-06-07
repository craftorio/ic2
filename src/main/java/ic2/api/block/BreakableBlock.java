package ic2.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BreakableBlock
{
	InteractionResult startBreak(Player var1, Level var2, InteractionHand var3, BlockPos var4, BlockState var5, Direction var6);
}
