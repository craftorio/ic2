package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.network.GrowingBuffer;
import ic2.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldMiningFilter extends HandHeldInventory
{
	public boolean blacklist = true;

	public HandHeldMiningFilter(Player player, InteractionHand hand, ItemStack containerStack)
	{
		super(player, hand, containerStack, 45);
		CompoundTag nbt = StackUtil.getOrCreateNbtData(containerStack);
		if (nbt.contains("blacklist"))
		{
			this.blacklist = nbt.getBoolean("blacklist");
		}
	}

	@Override
	protected void save()
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(this.containerStack);
		nbt.putBoolean("blacklist", this.blacklist);
		super.save();
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerMiningFilter(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerMiningFilter(syncId, inventory, this);
	}
}
