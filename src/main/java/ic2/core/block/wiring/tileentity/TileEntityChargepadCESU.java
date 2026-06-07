package ic2.core.block.wiring.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargepadCESU extends TileEntityChargepadBlock
{
	public TileEntityChargepadCESU(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CESU_CHARGEPAD, pos, state, 2, 128, 300000);
	}

	@Override
	protected void getItems(Player player)
	{
		if (player != null)
		{
			for (ItemStack current : player.getInventory().f_35975_)
			{
				if (current != null)
				{
					this.chargeItem(current, 128);
				}
			}

			for (ItemStack current : player.getInventory().f_35974_)
			{
				if (current != null)
				{
					this.chargeItem(current, 128);
				}
			}
		}
	}
}
