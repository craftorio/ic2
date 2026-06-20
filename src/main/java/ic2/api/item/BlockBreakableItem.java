package ic2.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface BlockBreakableItem
{
	InteractionResult onBlockStartBreak(Player var1, Level var2, InteractionHand var3, BlockPos var4, Direction var5);

	boolean beforeBlockBreak(Level var1, Player var2, BlockPos var3, BlockState var4, @Nullable BlockEntity var5);

	void afterBlockBreak(Level var1, Player var2, BlockPos var3, BlockState var4, @Nullable BlockEntity var5);
}
