package ic2.core.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface Ic2FluidBlock
{
	boolean isFluidBlock(BlockState var1, Level var2, BlockPos var3, BlockEntity var4);

	FluidTankInfo[] getTankInfos(BlockState var1, Level var2, BlockPos var3, BlockEntity var4);

	Ic2FluidStack drainMb(BlockState var1, Level var2, BlockPos var3, BlockEntity var4, Direction var5, int var6, boolean var7);

	int drainMb(BlockState var1, Level var2, BlockPos var3, BlockEntity var4, Direction var5, Ic2FluidStack var6, boolean var7);

	int fillMb(BlockState var1, Level var2, BlockPos var3, BlockEntity var4, Direction var5, Ic2FluidStack var6, boolean var7);
}
