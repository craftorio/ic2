package ic2.core.item.tool;

import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.block.wiring.CableBlock;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemToolCutter extends ItemToolCrafting implements IEnhancedOverlayProvider
{
	public ItemToolCutter(Properties settings)
	{
		super(settings);
	}

	public InteractionResult m_6225_(UseOnContext context)
	{
		Level world = context.m_43725_();
		BlockPos pos = context.m_8083_();
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof CableBlock cable)
		{
			Player player = context.m_43723_();
			Predicate<ItemStack> request = StackUtil.sameItem(Ic2Items.RUBBER);
			if (player == null)
			{
				return InteractionResult.PASS;
			}

			if (StackUtil.consumeFromPlayerInventory(player, request, 1, true) && cable.tryAddInsulation(state, world, pos))
			{
				StackUtil.consumeFromPlayerInventory(player, request, 1, false);
				StackUtil.damageOrError(player, context.m_43724_(), 1);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	public boolean removeInsulation(Player player, InteractionHand hand, BlockState state, Level world, BlockPos pos)
	{
		CableBlock cable = (CableBlock) state.getBlock();
		if (cable.tryRemoveInsulation(state, world, pos, true) && StackUtil.damage(player, hand, StackUtil.sameItem(this), 3))
		{
			cable.tryRemoveInsulation(state, world, pos, false);
			if (world.isClientSide)
			{
				player.m_5496_(Ic2SoundEvents.ITEM_CUTTER_USE, 1.0F, 1.0F);
			} else
			{
				StackUtil.dropAsEntity(world, pos, new ItemStack(Ic2Items.RUBBER));
			}

			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack)
	{
		return false;
	}
}
