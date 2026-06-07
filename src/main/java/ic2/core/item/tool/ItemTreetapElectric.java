package ic2.core.item.tool;

import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemTreetapElectric extends ItemElectricTool
{
	public ItemTreetapElectric(Properties settings)
	{
		super(settings, 50);
		this.maxCharge = 10000;
		this.transferLimit = 100;
		this.tier = 1;
	}

	@Override
	public InteractionResult m_6225_(UseOnContext context)
	{
		Level world = context.m_43725_();
		BlockPos pos = context.m_8083_();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		ItemStack stack = context.m_43722_();
		if (block == Ic2Blocks.RUBBER_LOG && this.canUse(stack))
		{
			Player player = context.m_43723_();
			if (player == null)
			{
				return InteractionResult.PASS;
			} else
			{
				return ItemTreetap.attemptExtract(player, world, pos, context.m_43719_(), state, null, true)
					&& this.consumeEnergy(stack, this.operationEnergyCost, player)
					? InteractionResult.SUCCESS
					: super.m_6225_(context);
			}
		} else
		{
			return InteractionResult.PASS;
		}
	}
}
