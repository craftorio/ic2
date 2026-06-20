package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.network.GrowingBuffer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldMeter extends HandHeldInventory
{
	public HandHeldMeter(Player player, InteractionHand hand, ItemStack containerStack)
	{
		super(player, hand, containerStack, 0);
	}

	void closeGUI()
	{
		if (!this.player.level().isClientSide)
		{
			((ServerPlayer) this.player).closeContainer();
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerMeter(syncId, this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerMeter(syncId, this);
	}
}
