package ic2.core.item.tool;

import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.IC2;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.core.init.Localization;
import ic2.core.item.PriorityUsableItem;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.RotationUtil;
import ic2.core.util.Util;

import java.util.List;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ItemToolCrowbar extends TieredItem implements IEnhancedOverlayProvider, PriorityUsableItem
{
	public ItemToolCrowbar(Tier material, Properties settings)
	{
		super(material, settings);
	}

	public boolean canTakeDamage(ItemStack stack, int amount)
	{
		return true;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.m_43725_();
		Player player = context.m_43723_();
		InteractionHand hand = context.m_43724_();
		BlockPos pos = context.m_8083_();
		Direction side = context.m_43719_();
		Vec3 hitPos = context.m_43720_();
		if (!this.canTakeDamage(stack, 1))
		{
			return InteractionResult.FAIL;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (state.isAir())
		{
			return InteractionResult.FAIL;
		}

		if (world.getBlockEntity(pos) instanceof ICoverHolder target)
		{
			Direction selectedFacing = RotationUtil.rotateByHit(side, (float) hitPos.f_82479_, (float) hitPos.f_82480_, (float) hitPos.f_82481_);
			if (target.canRemoveCover(world, pos, selectedFacing))
			{
				if (!world.isClientSide)
				{
					target.removeCover(world, pos, selectedFacing);
					if (player != null)
					{
						stack.m_41622_(1, player, p -> p.m_21190_(hand));
					}
				} else
				{
					IC2.soundManager.playOnce(Ic2SoundEvents.ITEM_CROWBAR_USE, SoundSource.BLOCKS, 1.0F, 1.0F, player);
					IC2.sideProxy.messagePlayer(player, Localization.translate("Attachment removed"));
				}
			}

			return world.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.FAIL;
		}
	}

	public boolean m_6832_(ItemStack toRepair, ItemStack repair)
	{
		return repair != null && Util.matchesOD(repair, Ic2ItemTags.BRONZE_INGOTS);
	}

	public boolean m_8120_(ItemStack stack)
	{
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void m_7373_(ItemStack stack, Level worldIn, List<Component> info, TooltipFlag flagIn)
	{
		info.add(Component.m_237115_(Minecraft.m_91087_().f_91066_.f_92088_.m_90860_() + ":"));
		info.add(Component.m_237113_(" Remove attachments from blocks"));
	}

	@Override
	public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack)
	{
		BlockEntity tileEntity = world.getBlockEntity(pos);
		return tileEntity instanceof ICoverHolder;
	}
}
