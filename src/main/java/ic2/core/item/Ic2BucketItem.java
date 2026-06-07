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
import net.minecraft.world.level.material.Material;
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

	public InteractionResultHolder<ItemStack> m_7203_(Level world, Player user, InteractionHand hand)
	{
		ItemStack itemStack = user.m_21120_(hand);
		BlockHitResult blockHitResult = m_41435_(
			world,
			user,
			this.fluid == Fluids.f_76191_ ? net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY : net.minecraft.world.level.ClipContext.Fluid.NONE
		);
		InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onBucketUse(user, world, itemStack, blockHitResult);
		if (ret != null)
		{
			return ret;
		}

		if (blockHitResult.m_6662_() == Type.MISS)
		{
			return InteractionResultHolder.m_19098_(itemStack);
		}

		if (blockHitResult.m_6662_() != Type.BLOCK)
		{
			return InteractionResultHolder.m_19098_(itemStack);
		}

		BlockPos blockPos = blockHitResult.m_82425_();
		Direction direction = blockHitResult.m_82434_();
		BlockPos blockPos1 = blockPos.relative(direction);
		if (!world.m_7966_(user, blockPos) || !user.m_36204_(blockPos1, direction, itemStack))
		{
			return InteractionResultHolder.m_19100_(itemStack);
		}

		if (this.fluid == Fluids.f_76191_)
		{
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.getBlock() instanceof BucketPickup bucketPickUpFluid)
			{
				ItemStack afterDrainingItemStack = this.tryDrainFluid(world, blockPos, blockState);
				if (!afterDrainingItemStack.m_41619_())
				{
					user.m_36246_(Stats.f_12982_.m_12902_(this));
					bucketPickUpFluid.getPickupSound(blockState).ifPresent(p_150709_ -> user.m_5496_(p_150709_, 1.0F, 1.0F));
					world.m_142346_(user, GameEvent.f_157816_, blockPos);
					ItemStack exchangedStack = ItemUtils.m_41813_(itemStack, user, afterDrainingItemStack);
					if (!world.isClientSide)
					{
						CriteriaTriggers.f_10576_.m_38772_((ServerPlayer) user, afterDrainingItemStack);
					}

					return InteractionResultHolder.m_19092_(exchangedStack, world.m_5776_());
				}
			}

			return InteractionResultHolder.m_19100_(itemStack);
		} else
		{
			BlockState blockState = world.getBlockState(blockPos);
			BlockPos fluidDrainablePos = this.canBlockContainFluid(world, blockPos, blockState) ? blockPos : blockPos1;
			if (this.bucketUseOnBlock(new UseOnContext(user, hand, blockHitResult)))
			{
				return InteractionResultHolder.m_19098_(itemStack);
			}

			if (this.m_142073_(user, world, fluidDrainablePos, blockHitResult))
			{
				this.m_142131_(user, world, itemStack, fluidDrainablePos);
				if (user instanceof ServerPlayer)
				{
					CriteriaTriggers.f_10591_.m_59469_((ServerPlayer) user, fluidDrainablePos, itemStack);
				}

				user.m_36246_(Stats.f_12982_.m_12902_(this));
				ItemStack emptiedBucketExchangedItemStack = ItemUtils.m_41813_(itemStack, user, this.getEmptiedBucketStack(itemStack, user));
				return InteractionResultHolder.m_19092_(emptiedBucketExchangedItemStack, world.m_5776_());
			} else
			{
				return InteractionResultHolder.m_19100_(itemStack);
			}
		}
	}

	private ItemStack getEmptiedBucketStack(ItemStack stack, Player player)
	{
		return !player.m_150110_().f_35937_ ? new ItemStack(this.getEmptiedBucketItem()) : stack;
	}

	public ItemStack tryDrainFluid(LevelAccessor world, BlockPos pos, BlockState state)
	{
		if (state.getBlock() instanceof LiquidBlock)
		{
			Fluid fluid = state.m_60819_().m_76152_();
			if (this.drainableFluidList.contains(fluid))
			{
				world.m_7731_(pos, Blocks.f_50016_.defaultBlockState(), 11);
				return new ItemStack(this.getBucketItem(fluid));
			}
		}

		if (state.getBlock() instanceof SimpleWaterloggedBlock && (Boolean) state.getValue(BlockStateProperties.f_61362_))
		{
			world.m_7731_(pos, (BlockState) state.setValue(BlockStateProperties.f_61362_, false), 3);
			if (!state.m_60710_(world, pos))
			{
				world.m_46961_(pos, true);
			}

			return new ItemStack(this.getBucketItem(Fluids.f_76193_));
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

	public boolean m_142073_(@Nullable Player player, Level world, BlockPos pos, @Nullable BlockHitResult hitResult)
	{
		if (!(this.fluid instanceof FlowingFluid))
		{
			return false;
		}

		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		Material material = blockState.getMaterial();
		boolean bl = blockState.m_60722_(this.fluid);
		boolean bl2 = blockState.isAir()
			|| bl
			|| block instanceof LiquidBlockContainer && ((LiquidBlockContainer) block).m_6044_(world, pos, blockState, this.fluid);
		if (!bl2)
		{
			return hitResult != null && this.m_142073_(player, world, hitResult.m_82425_().relative(hitResult.m_82434_()), null);
		}

		if (world.m_6042_().f_63857_() && this.fluid.m_205067_(FluidTags.f_13131_))
		{
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			world.playSound(player, pos, SoundEvents.f_11937_, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

			for (int l = 0; l < 8; l++)
			{
				world.addParticle(ParticleTypes.f_123755_, i + Math.random(), j + Math.random(), k + Math.random(), 0.0, 0.0, 0.0);
			}

			return true;
		} else
		{
			if (block instanceof LiquidBlockContainer && this.fluid == Fluids.f_76193_)
			{
				((LiquidBlockContainer) block).m_7361_(world, pos, blockState, ((FlowingFluid) this.fluid).m_76068_(false));
				this.m_7718_(player, world, pos);
				return true;
			}

			if (!world.isClientSide && bl && !material.m_76332_())
			{
				world.m_46961_(pos, true);
			}

			if (!world.m_7731_(pos, this.fluid.defaultFluidState().createLegacyBlock(), 11) && !blockState.m_60819_().m_76170_())
			{
				return false;
			}

			this.m_7718_(player, world, pos);
			return true;
		}
	}

	private boolean canBlockContainFluid(Level worldIn, BlockPos posIn, BlockState blockstate)
	{
		return blockstate.getBlock() instanceof LiquidBlockContainer
			&& ((LiquidBlockContainer) blockstate.getBlock()).m_6044_(worldIn, posIn, blockstate, this.fluid);
	}

	protected void m_7718_(@Nullable Player player, LevelAccessor world, BlockPos pos)
	{
		SoundEvent soundEvent = this.fluid.m_205067_(FluidTags.f_13132_) ? SoundEvents.f_11780_ : SoundEvents.f_11778_;
		world.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
		world.m_142346_(player, GameEvent.f_157769_, pos);
	}
}
