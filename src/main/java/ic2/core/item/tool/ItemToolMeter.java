package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.PriorityUsableItem;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemToolMeter extends Item implements IBoxable, IHandHeldInventory, PriorityUsableItem
{
	public ItemToolMeter(Properties settings)
	{
		super(settings);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		InteractionHand hand = context.getHand();
		if (world == null)
		{
			return InteractionResult.PASS;
		}

		if (player == null)
		{
			return InteractionResult.PASS;
		}

		if (world.isClientSide)
		{
			return InteractionResult.PASS;
		}

		IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
		if (!(tile instanceof IEnergySource) && !(tile instanceof IEnergyConductor) && !(tile instanceof IEnergySink))
		{
			IC2.sideProxy.messagePlayer(player, "Not an energy net tile");
		} else if (this.getInventory(player, hand, StackUtil.get(player, hand)).openManagedItem(player, hand, null))
		{
			ContainerMeter container = (ContainerMeter) player.containerMenu;
			container.setUut(tile);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.SUCCESS;
	}

	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof ContainerMeter)
		{
			HandHeldMeter euReader = ((ContainerMeter) player.containerMenu).base;
			if (euReader.isThisContainer(stack))
			{
				euReader.saveAsThrown(stack);
				((ServerPlayer) player).closeContainer();
			}
		}

		return true;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	@Override
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		return new HandHeldMeter(player, hand, stack);
	}
}
