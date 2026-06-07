package ic2.core.block.reactor.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityRCI_LZH extends TileEntityAbstractRCI
{
	public TileEntityRCI_LZH(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.RCI_LZH, pos, state, new ItemStack(Ic2Items.LZH_CONDENSATOR), new ItemStack(Blocks.f_50060_));
	}
}
