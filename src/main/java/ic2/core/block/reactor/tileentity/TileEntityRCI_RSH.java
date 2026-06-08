package ic2.core.block.reactor.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityRCI_RSH extends TileEntityAbstractRCI
{
	public TileEntityRCI_RSH(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.RCI_RSH, pos, state, new ItemStack(Ic2Items.RSH_CONDENSATOR), new ItemStack(Blocks.REDSTONE_BLOCK));
	}
}
