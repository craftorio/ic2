package ic2.core.item;

import java.util.List;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public abstract class Ic2BucketItem extends BucketItem
{
	protected Fluid fluid;
	private final List<Fluid> drainableFluidList;

	public Ic2BucketItem(Fluid fluid, Properties settings)
	{
		super(() -> fluid, settings);
		this.fluid = fluid;
		this.drainableFluidList = this.getDrainableFluidList();
	}

	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
	{
		ItemStack itemStack = user.getItemInHand(hand);
		BlockHitResult blockHitResult = getPlayerPOVHitResult(
			world,
			user,
			this.fluid == Fluids.EMPTY ? net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY : net.minecraft.world.level.ClipContext.Fluid.NONE
		);
		InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onBucketUse(user, world, itemStack, blockHitResult);
		if (ret != null)
		{
			return ret;
		}

		if (blockHitResult.getType() == Type.MISS)
		{
			return InteractionResultHolder.pass(itemStack);
		}

		if (blockHitResult.getType() != Type.BLOCK)
		{
			return InteractionResultHolder.pass(itemStack);
		}

		BlockPos blockPos = blockHitResult.getBlockPos();
		Direction direction = blockHitResult.getDirection();
		BlockPos blockPos1 = blockPos.relative(direction);
		if (!world.mayInteract(user, blockPos) || !user.mayUseItemAt(blockPos1, direction, itemStack))
		{
			return InteractionResultHolder.fail(itemStack);
		}

		if (this.fluid == Fluids.EMPTY)
		{
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.getBlock() instanceof BucketPickup bucketPickUpFluid)
			{
				ItemStack afterDrainingItemStack = this.tryDrainFluid(world, blockPos, blockState);
				if (!afterDrainingItemStack.isEmpty())
				{
					user.awardStat(Stats.ITEM_USED.get(this));
					bucketPickUpFluid.getPickupSound(blockState).ifPresent((soundEvent) -> user.playSound(soundEvent, 1.0F, 1.0F));
					world.gameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
					ItemStack exchangedStack = ItemUtils.createFilledResult(itemStack, user, afterDrainingItemStack);
					if (!world.isClientSide)
					{
						CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) user, afterDrainingItemStack);
					}

					return InteractionResultHolder.sidedSuccess(exchangedStack, world.isClientSide());
				}
			}

			return InteractionResultHolder.fail(itemStack);
		} else
		{
			BlockState blockState = world.getBlockState(blockPos);
			BlockPos fluidDrainablePos = this.canBlockContainFluid(world, blockPos, blockState) ? blockPos : blockPos1;
			if (this.bucketUseOnBlock(new UseOnContext(user, hand, blockHitResult)))
			{
				return InteractionResultHolder.pass(itemStack);
			}

			if (this.emptyContents(user, world, fluidDrainablePos, blockHitResult))
			{
				this.checkExtraContent(user, world, itemStack, fluidDrainablePos);
				if (user instanceof ServerPlayer)
				{
					CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) user, fluidDrainablePos, itemStack);
				}

				user.awardStat(Stats.ITEM_USED.get(this));
				ItemStack emptiedBucketExchangedItemStack = ItemUtils.createFilledResult(itemStack, user, this.getEmptiedBucketStack(itemStack, user));
				return InteractionResultHolder.sidedSuccess(emptiedBucketExchangedItemStack, world.isClientSide());
			} else
			{
				return InteractionResultHolder.fail(itemStack);
			}
		}
	}

	private ItemStack getEmptiedBucketStack(ItemStack stack, Player player)
	{
		return !player.getAbilities().instabuild ? new ItemStack(this.getEmptiedBucketItem()) : stack;
	}

	public ItemStack tryDrainFluid(LevelAccessor world, BlockPos pos, BlockState state)
	{
		if (state.getBlock() instanceof LiquidBlock)
		{
			Fluid fluid = state.getFluidState().getType();
			if (this.getDrainableFluidList().contains(fluid))
			{
				world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
				return new ItemStack(this.getBucketItem(fluid));
			}
		}

		if (state.getBlock() instanceof SimpleWaterloggedBlock && (Boolean) state.getValue(BlockStateProperties.WATERLOGGED))
		{
			world.setBlock(pos, (BlockState) state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
			if (!state.canSurvive(world, pos))
			{
				world.destroyBlock(pos, true);
			}

			return new ItemStack(this.getBucketItem(Fluids.WATER));
		} else
		{
			return this.tryDrain(world, pos, state);
		}
	}

	public abstract Item getEmptiedBucketItem();

	public abstract List<Fluid> getDrainableFluidList();

	public abstract Item getBucketItem(Fluid var1);

	public ItemStack tryDrain(LevelAccessor world, BlockPos pos, BlockState state)
	{
		return ItemStack.EMPTY;
	}

	public boolean bucketUseOnBlock(UseOnContext context)
	{
		return false;
	}

	public boolean emptyContents(@Nullable Player player, Level world, BlockPos pos, @Nullable BlockHitResult hitResult)
	{
		if (!(this.fluid instanceof FlowingFluid))
		{
			return false;
		}

		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		boolean bl = blockState.canBeReplaced(this.fluid);
		boolean bl2 = blockState.isAir()
			|| bl
			|| block instanceof LiquidBlockContainer && ((LiquidBlockContainer) block).canPlaceLiquid(world, pos, blockState, this.fluid);
		if (!bl2)
		{
			return hitResult != null && this.emptyContents(player, world, hitResult.getBlockPos().relative(hitResult.getDirection()), null);
		}

		if (world.dimensionType().ultraWarm() && this.fluid.is(FluidTags.WATER))
		{
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

			for (int l = 0; l < 8; l++)
			{
				world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0, 0.0, 0.0);
			}

			return true;
		} else
		{
			if (block instanceof LiquidBlockContainer && this.fluid == Fluids.WATER)
			{
				((LiquidBlockContainer) block).placeLiquid(world, pos, blockState, ((FlowingFluid) this.fluid).getSource(false));
				this.playEmptySound(player, world, pos);
				return true;
			}

			if (!world.isClientSide && bl && blockState.getFluidState().isEmpty())
			{
				world.destroyBlock(pos, true);
			}

			if (!world.setBlock(pos, this.fluid.defaultFluidState().createLegacyBlock(), 11) && !blockState.getFluidState().isSource())
			{
				return false;
			}

			this.playEmptySound(player, world, pos);
			return true;
		}
	}

	protected boolean canBlockContainFluid(Level worldIn, BlockPos posIn, BlockState blockstate)
	{
		return blockstate.getBlock() instanceof LiquidBlockContainer
			&& ((LiquidBlockContainer) blockstate.getBlock()).canPlaceLiquid(worldIn, posIn, blockstate, this.fluid);
	}

	protected void playEmptySound(@Nullable Player player, LevelAccessor world, BlockPos pos)
	{
		SoundEvent soundEvent = this.fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		world.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
		world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
	}
}
