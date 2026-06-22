package ic2.core.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.BlockHitResult;

public class UUMatterBlock extends LiquidBlock
{
	public UUMatterBlock(FlowingFluid fluid, Properties properties)
	{
		super(fluid, properties);
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
	{
		if (!world.isClientSide && entity instanceof LivingEntity living)
		{
			living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		ItemStack heldItem = player.getItemInHand(hand);
		if (heldItem.is(Items.GLASS_BOTTLE) && state.getValue(LiquidBlock.LEVEL) == 0)
		{
			if (!world.isClientSide)
			{
				ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
				if (!player.getAbilities().instabuild)
				{
					heldItem.shrink(1);
				}
				if (heldItem.isEmpty())
				{
					player.setItemInHand(hand, waterBottle);
				} else if (!player.getInventory().add(waterBottle))
				{
					player.drop(waterBottle, false);
				}
			}
			world.playSound(player, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.sidedSuccess(world.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston)
	{
		if (state.getValue(LiquidBlock.LEVEL) != 0) return;

		FluidState neighborFluidState = world.getFluidState(neighborPos);
		Fluid neighborFluid = neighborFluidState.getType();

		if (!neighborFluidState.isEmpty() && neighborFluid != getFluid())
		{
			if (neighborFluidState.is(FluidTags.LAVA))
			{
				if (neighborFluidState.isSource())
				{
					world.setBlockAndUpdate(neighborPos, Blocks.OBSIDIAN.defaultBlockState());
				} else
				{
					world.setBlockAndUpdate(neighborPos, Blocks.COBBLESTONE.defaultBlockState());
				}
			} else if (neighborFluidState.isSource())
			{
				world.setBlock(neighborPos, Blocks.AIR.defaultBlockState(), 3);
			}
		}
	}
}
