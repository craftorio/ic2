package ic2.core.entity.block;

import ic2.api.entity.block.ExplosiveEntity;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Entities;
import ic2.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NukeEntity extends ExplosiveEntity
{
	public NukeEntity(Level world, double x, double y, double z, float power, int radiationRange)
	{
		super(Ic2Entities.NUKE, world, x, y, z, 300, power, 0.05F, 1.5F, Ic2Blocks.NUKE.defaultBlockState(), radiationRange);
	}

	public NukeEntity(EntityType<? extends NukeEntity> type, Level world)
	{
		this(world, 0.0, 0.0, 0.0, 1.0F, 1);
	}

	public InteractionResult interact(Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof ItemToolWrench)
		{
			ItemToolWrench wrench = (ItemToolWrench) stack.getItem();
			if (wrench.canTakeDamage(stack, 1))
			{
				if (this.level.isClientSide)
				{
					return InteractionResult.PASS;
				}

				wrench.damage(stack, 1, player, hand);
				this.discard();
				return InteractionResult.CONSUME;
			}
		}

		return InteractionResult.PASS;
	}
}
