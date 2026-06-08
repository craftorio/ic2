package ic2.core.block.wiring.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargepadMFSU extends TileEntityChargepadBlock
{
	public TileEntityChargepadMFSU(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.MFSU_CHARGEPAD, pos, state, 4, 2048, 40000000);
	}

	@Override
	protected void getItems(Player player)
	{
		for (ItemStack current : player.getInventory().armor)
		{
			if (current != null)
			{
				this.chargeItem(current, 2048);
			}
		}

		for (ItemStack current : player.getInventory().items)
		{
			if (current != null)
			{
				this.chargeItem(current, 2048);
			}
		}
	}
}
