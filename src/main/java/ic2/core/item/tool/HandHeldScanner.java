package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.network.GrowingBuffer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldScanner extends HandHeldInventory
{
	ItemStack itemScanner;
	Player player;

	public HandHeldScanner(Player player, InteractionHand hand, ItemStack itemScanner)
	{
		super(player, hand, itemScanner, 0);
		this.itemScanner = itemScanner;
		this.player = player;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerToolScanner(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerToolScanner(syncId, inventory, this);
	}
}
