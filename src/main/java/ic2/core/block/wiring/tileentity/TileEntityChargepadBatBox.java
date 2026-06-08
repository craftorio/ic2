package ic2.core.block.wiring.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargepadBatBox extends TileEntityChargepadBlock
{
	public TileEntityChargepadBatBox(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.BATBOX_CHARGEPAD, pos, state, 1, 32, 40000);
	}

	@Override
	protected void getItems(Player player)
	{
		if (player != null)
		{
			for (ItemStack current : player.getInventory().armor)
			{
				if (current != null)
				{
					this.chargeItem(current, 32);
				}
			}

			for (ItemStack current : player.getInventory().items)
			{
				if (current != null)
				{
					this.chargeItem(current, 32);
				}
			}
		}
	}
}
