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
	public InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		ItemStack stack = context.getItemInHand();
		if (block == Ic2Blocks.RUBBER_LOG && this.canUse(stack))
		{
			Player player = context.getPlayer();
			if (player == null)
			{
				return InteractionResult.PASS;
			} else
			{
				return ItemTreetap.attemptExtract(player, world, pos, context.getClickedFace(), state, null, true)
					&& this.consumeEnergy(stack, this.operationEnergyCost, player)
					? InteractionResult.SUCCESS
					: super.useOn(context);
			}
		} else
		{
			return InteractionResult.PASS;
		}
	}
}
