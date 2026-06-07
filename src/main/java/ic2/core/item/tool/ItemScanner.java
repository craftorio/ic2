package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.core.IHasGui;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.IHandHeldInventory;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemScanner extends BaseElectricItem implements IBoxable, IHandHeldInventory
{
	public ItemScanner(Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(settings, maxCharge, transferLimit, tier);
	}

	@OnlyIn(Dist.CLIENT)
	public void m_7373_(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		super.m_7373_(stack, world, tooltip, advanced);
		tooltip.add(Component.m_237110_("ic2.scanner.range", new Object[] { this.getScanRange() + "" }).m_130940_(ChatFormatting.GRAY));
	}

	public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if ((this.tier != 1 || ElectricItem.manager.use(stack, 50.0, player)) && (this.tier != 2 || ElectricItem.manager.use(stack, 250.0, player)))
		{
			if (!world.isClientSide)
			{
				if (this.getInventory(player, hand, stack).openManagedItem(player, hand, null) && player.f_36096_ instanceof ContainerToolScanner container)
				{
					Map<ItemComparableItemStack, Integer> scanResult = this.scan(player.getCommandSenderWorld(), player.m_20183_(), this.getScanRange());
					container.setResults(this.scanMapToSortedList(scanResult));
				}
			} else
			{
				player.m_5496_(Ic2SoundEvents.ITEM_SCANNER_USE, 1.0F, 1.0F);
			}

			return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
		} else
		{
			return new InteractionResultHolder(InteractionResult.FAIL, stack);
		}
	}

	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(stack) && player.f_36096_ instanceof ContainerToolScanner)
		{
			HandHeldScanner scanner = ((ContainerToolScanner) player.f_36096_).base;
			if (scanner.isThisContainer(stack))
			{
				scanner.saveAsThrown(stack);
				((ServerPlayer) player).m_6915_();
			}
		}

		return true;
	}

	public int startLayerScan(ItemStack stack)
	{
		return ElectricItem.manager.use(stack, 50.0, null) ? this.getScanRange() / 2 : 0;
	}

	public int getScanRange()
	{
		return 6;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	@Override
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		return new HandHeldScanner(player, hand, stack);
	}

	private Map<ItemComparableItemStack, Integer> scan(Level world, BlockPos center, int range)
	{
		Map<ItemComparableItemStack, Integer> ret = new HashMap<>();
		MutableBlockPos tmpPos = new MutableBlockPos();

		for (int y = center.getY() - range; y <= center.getY() + range; y++)
		{
			for (int z = center.getZ() - range; z <= center.getZ() + range; z++)
			{
				for (int x = center.getX() - range; x <= center.getX() + range; x++)
				{
					tmpPos.set(x, y, z);
					BlockState state = world.getBlockState(tmpPos);
					if (!state.isAir())
					{
						ItemStack blockItemStack = new ItemStack(state.getBlock().m_5456_(), 1);
						if (StackUtil.isOreStack(blockItemStack))
						{
							ItemStack pickStack = state.getBlock().m_7397_(world, center, state);
							ItemComparableItemStack key = new ItemComparableItemStack(pickStack, true);
							Integer count = ret.get(key);
							if (count == null)
							{
								count = 0;
							}

							count = count + StackUtil.getSize(pickStack);
							ret.put(key, count);
						}
					}
				}
			}
		}

		return ret;
	}

	private List<Tuple.T2<ItemStack, Integer>> scanMapToSortedList(Map<ItemComparableItemStack, Integer> map)
	{
		List<Tuple.T2<ItemStack, Integer>> ret = new ArrayList<>(map.size());

		for (Entry<ItemComparableItemStack, Integer> entry : map.entrySet())
		{
			ret.add(new Tuple.T2<>(entry.getKey().toStack(), entry.getValue()));
		}

		ret.sort((a, b) -> b.b - a.b);
		return ret;
	}
}
