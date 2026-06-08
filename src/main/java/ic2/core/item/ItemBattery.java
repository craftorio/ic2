package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.network.INetworkItemEventListener;
import ic2.core.IC2;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemBattery extends BaseElectricItem implements INetworkItemEventListener
{
	public ItemBattery(Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(settings, maxCharge, transferLimit, tier);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return true;
	}

	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide && StackUtil.getSize(stack) == 1)
		{
			if (ElectricItem.manager.getCharge(stack) > 0.0)
			{
				boolean transferred = false;

				for (int i = 0; i < 9; i++)
				{
					ItemStack target = (ItemStack) player.getInventory().items.get(i);
					if (target != null
						&& target != stack
						&& !(ElectricItem.manager.discharge(target, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true, true) > 0.0))
					{
						double transfer = ElectricItem.manager.discharge(stack, 2.0 * this.transferLimit, Integer.MAX_VALUE, true, true, true);
						if (!(transfer <= 0.0))
						{
							transfer = ElectricItem.manager.charge(target, transfer, this.tier, true, false);
							if (!(transfer <= 0.0))
							{
								ElectricItem.manager.discharge(stack, transfer, Integer.MAX_VALUE, true, true, false);
								transferred = true;
							}
						}
					}
				}

				if (transferred && !world.isClientSide)
				{
					player.containerMenu.broadcastChanges();
					IC2.network.get(true).initiateItemEvent(player, stack, 0, true);
				}
			}

			return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
		} else
		{
			return new InteractionResultHolder(InteractionResult.PASS, stack);
		}
	}

	@Override
	public void onNetworkEvent(ItemStack stack, Player player, int event)
	{
		player.playSound(Ic2SoundEvents.ITEM_BATTERY_USE, 1.0F, 1.0F);
	}
}
