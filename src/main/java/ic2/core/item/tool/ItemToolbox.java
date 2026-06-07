package ic2.core.item.tool;

import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.util.StackUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemToolbox extends Item implements IHandHeldInventory
{
	public ItemToolbox(Properties settings)
	{
		super(settings);
	}

	public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide)
		{
			this.getInventory(player, hand, stack).openManagedItem(player, hand, null);
		}

		return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
	}

	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(stack) && player.f_36096_ instanceof ContainerToolbox)
		{
			HandHeldToolbox toolbox = ((ContainerToolbox) player.f_36096_).base;
			if (toolbox.isThisContainer(stack))
			{
				toolbox.saveAsThrown(stack);
				((ServerPlayer) player).m_6915_();
			}
		}

		return true;
	}

	public Rarity m_41460_(ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	@Override
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		return new HandHeldToolbox(player, hand, stack, 9);
	}
}
