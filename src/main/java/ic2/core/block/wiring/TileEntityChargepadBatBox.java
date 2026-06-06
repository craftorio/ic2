package ic2.core.block.wiring;

import ic2.core.profile.NotClassic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@NotClassic
public class TileEntityChargepadBatBox extends TileEntityChargepadBlock
{
	public TileEntityChargepadBatBox()
	{
		super(1, 32, 40000);
	}

	@Override
	protected void getItems(EntityPlayer player)
	{
		if (player != null)
		{
			for (ItemStack current : player.inventory.armorInventory)
			{
				if (current != null)
				{
					this.chargeItem(current, 32);
				}
			}

			for (ItemStack current : player.inventory.mainInventory)
			{
				if (current != null)
				{
					this.chargeItem(current, 32);
				}
			}
		}
	}
}
