package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.util.StackUtil;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemFrequencyTransmitter extends Item
{
	private static final String targetSetNbt = "targetSet";
	private static final String targetJustSetNbt = "targetJustSet";
	private static final String targetXNbt = "targetX";
	private static final String targetYNbt = "targetY";
	private static final String targetZNbt = "targetZ";

	public ItemFrequencyTransmitter(Properties settings)
	{
		super(settings);
	}

	public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (IC2.sideProxy.isSimulating())
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			boolean hadJustSet = nbtData.getBoolean("targetJustSet");
			if (nbtData.getBoolean("targetSet") && !hadJustSet)
			{
				nbtData.putBoolean("targetSet", false);
				IC2.sideProxy.messagePlayer(player, "Frequency Transmitter unlinked");
			}

			if (hadJustSet)
			{
				nbtData.putBoolean("targetJustSet", false);
			}
		}

		return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
	}

	public InteractionResult m_6225_(UseOnContext context)
	{
		Level world = context.m_43725_();
		Player player = context.m_43723_();
		BlockPos pos = context.m_8083_();
		InteractionHand hand = context.m_43724_();
		if (player == null)
		{
			return InteractionResult.PASS;
		} else if (world.isClientSide)
		{
			return InteractionResult.PASS;
		} else if (!(world.getBlockEntity(pos) instanceof TileEntityTeleporter tp))
		{
			return InteractionResult.PASS;
		} else
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
			boolean targetSet = nbtData.getBoolean("targetSet");
			boolean justSetTarget = true;
			BlockPos target = new BlockPos(nbtData.getInt("targetX"), nbtData.getInt("targetY"), nbtData.getInt("targetZ"));
			if (!targetSet)
			{
				targetSet = true;
				target = tp.getBlockPos();
				IC2.sideProxy.messagePlayer(player, "Frequency Transmitter linked to Teleporter.");
			} else if (tp.getBlockPos().equals(target))
			{
				IC2.sideProxy.messagePlayer(player, "Can't link Teleporter to itself.");
			} else if (tp.hasTarget() && tp.getTarget().equals(target))
			{
				IC2.sideProxy.messagePlayer(player, "Teleportation link unchanged.");
			} else
			{
				BlockEntity targetTe = world.getBlockEntity(target);
				if (targetTe instanceof TileEntityTeleporter)
				{
					tp.setTarget(target);
					((TileEntityTeleporter) targetTe).setTarget(pos);
					IC2.sideProxy.messagePlayer(player, "Teleportation link established.");
				} else
				{
					justSetTarget = false;
					targetSet = false;
				}
			}

			nbtData.putBoolean("targetSet", targetSet);
			nbtData.putBoolean("targetJustSet", justSetTarget);
			nbtData.putInt("targetX", target.getX());
			nbtData.putInt("targetY", target.getY());
			nbtData.putInt("targetZ", target.getZ());
			return InteractionResult.SUCCESS;
		}
	}

	public void m_7373_(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
		if (nbtData.getBoolean("targetSet"))
		{
			tooltip.add(
				Component.m_237110_(
					"ic2.frequency_transmitter.tooltip.target",
					new Object[] { nbtData.getInt("targetX"), nbtData.getInt("targetY"), nbtData.getInt("targetZ") }
				)
			);
		} else
		{
			tooltip.add(Component.m_237115_("ic2.frequency_transmitter.tooltip.blank"));
		}
	}
}
