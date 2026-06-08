package ic2.core.block.tileentity;

import com.google.common.base.Suppliers;
import ic2.api.block.BreakableBlock;
import ic2.api.crops.CropSoilType;
import ic2.api.tile.IWrenchable;
import ic2.api.tile.RetexturableBlock;
import ic2.core.block.comp.Obscuration;
import ic2.core.crop.Ic2CropType;
import ic2.core.crop.TileEntityCrop;
import ic2.core.util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Ic2TileEntityBlock extends Block implements EntityBlock, IWrenchable, BreakableBlock, RetexturableBlock
{
	private static final BlockEntityTicker<Ic2TileEntity> TICKER = (world, pos, state, be) -> be.tick();
	private static final String facingPropertyName = "facing";
	public static final Property<Direction> anyFacingProperty = DirectionProperty.create("facing", Util.allFacings);
	public static final Property<Direction> horizontalFacingProperty = DirectionProperty.create("facing", Util.horizontalFacings);
	public static final Property<Direction> verticalFacingProperty = DirectionProperty.create("facing", Util.verticalFacings);
	public static final BooleanProperty CROSSING_BASE = BooleanProperty.create("crossing_base");
	private static final Map<Integer, IntegerProperty> ageProperties = new HashMap<>();
	private int cropMaxAge = -1;
	private Ic2CropType cropType;
	private static final ThreadLocal<Ic2TileEntityBlock.InitData> pendingInitData = new ThreadLocal<>();
	private final Class<? extends Ic2TileEntity> teClass;
	private final boolean canActive;
	private final Ic2TileEntityBlock.DefaultDrop defaultDrop;
	private final boolean allowWrenchRotating;
	private final Set<Direction> supportedFacings;
	private final Supplier<Ic2TileEntity> dummyTe;
	public final Property<Direction> facingProperty;
	private boolean enableWorldTick;
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public static Ic2TileEntityBlock create(
		Properties settings,
		Class<? extends Ic2TileEntity> teClass,
		boolean canActive,
		Ic2TileEntityBlock.DefaultDrop defaultDrop,
		Set<Direction> supportedFacings,
		boolean allowWrenchRotating
	)
	{
		Ic2TileEntityBlock.InitData data = new Ic2TileEntityBlock.InitData(supportedFacings, canActive, teClass, null, -1);
		pendingInitData.set(data);
		Ic2TileEntityBlock ret = new Ic2TileEntityBlock(settings, teClass, canActive, data, defaultDrop, allowWrenchRotating);
		pendingInitData.remove();
		return ret;
	}

	public static Ic2TileEntityBlock create(
		Properties settings,
		Class<? extends Ic2TileEntity> teClass,
		boolean canActive,
		Ic2TileEntityBlock.DefaultDrop defaultDrop,
		Set<Direction> supportedFacings,
		boolean allowWrenchRotating,
		Ic2CropType cropType
	)
	{
		Ic2TileEntityBlock.InitData data = new Ic2TileEntityBlock.InitData(supportedFacings, canActive, teClass, cropType, cropType.getMaxAge());
		pendingInitData.set(data);
		Ic2TileEntityBlock ret = new Ic2TileEntityBlock(settings, teClass, canActive, data, defaultDrop, allowWrenchRotating, cropType);
		pendingInitData.remove();
		return ret;
	}

	private Ic2TileEntityBlock(
		Properties settings,
		Class<? extends Ic2TileEntity> teClass,
		boolean canActive,
		Ic2TileEntityBlock.InitData data,
		Ic2TileEntityBlock.DefaultDrop defaultDrop,
		boolean allowWrenchRotating,
		Ic2CropType cropType
	)
	{
		this(settings, teClass, canActive, data, defaultDrop, allowWrenchRotating);
		if (teClass.equals(TileEntityCrop.class))
		{
			this.cropMaxAge = cropType.getMaxAge();
			this.cropType = cropType;
		}
	}

	private Ic2TileEntityBlock(
		Properties settings,
		Class<? extends Ic2TileEntity> teClass,
		boolean canActive,
		Ic2TileEntityBlock.InitData data,
		Ic2TileEntityBlock.DefaultDrop defaultDrop,
		boolean allowWrenchRotating
	)
	{
		super(settings);
		assert data == pendingInitData.get();
		this.teClass = teClass;
		this.canActive = canActive;
		this.defaultDrop = defaultDrop;
		this.allowWrenchRotating = allowWrenchRotating;
		this.supportedFacings = data.supportedFacings;
		this.facingProperty = this.supportedFacings.size() > 1 ? (Property<Direction>) this.stateDefinition.getProperty("facing") : null;
		this.dummyTe = Suppliers.memoize(() -> this.newBlockEntity(BlockPos.ZERO, this.defaultBlockState()));
	}

	private IntegerProperty getAgeProperty(int cropMaxAge)
	{
		if (ageProperties.containsKey(cropMaxAge))
		{
			return ageProperties.get(cropMaxAge);
		}

		IntegerProperty ret = IntegerProperty.create("age", 0, cropMaxAge);
		ageProperties.put(cropMaxAge, ret);
		return ret;
	}

	public IntegerProperty getAgeProperty()
	{
		return this.cropMaxAge == -1 ? null : this.getAgeProperty(this.cropMaxAge);
	}

	public int getCropMaxAge()
	{
		return this.cropMaxAge;
	}

	public Ic2CropType getCropType()
	{
		return this.cropType;
	}

	public Class<? extends Ic2TileEntity> getTeClass()
	{
		return this.teClass;
	}

	public boolean canActive()
	{
		return this.canActive;
	}

	public void setActive(Level world, BlockPos pos, BlockState state, boolean active)
	{
		if (this.canActive())
		{
			if (state.is(this))
			{
				Ic2TileEntity tileEntity = (Ic2TileEntity) world.getBlockEntity(pos);
				if (tileEntity != null)
				{
					tileEntity.setActive(active);
					world.setBlockAndUpdate(pos, state.setValue(ACTIVE, active));
				}
			}
		}
	}

	public Ic2TileEntityBlock.DefaultDrop getDefaultDrop()
	{
		return this.defaultDrop;
	}

	public boolean allowWrenchRotating()
	{
		return this.allowWrenchRotating;
	}

	public Set<Direction> getSupportedFacings()
	{
		return this.supportedFacings;
	}

	public Ic2TileEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		try
		{
			return this.teClass.getConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
		} catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return (BlockEntityTicker<T>) TICKER;
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		Ic2TileEntityBlock.InitData data = pendingInitData.get();
		Set<Direction> facings = data.supportedFacings;
		if (facings.size() > 1)
		{
			if (facings.equals(Util.allFacings))
			{
				builder.add(anyFacingProperty);
			} else if (facings.equals(Util.horizontalFacings))
			{
				builder.add(horizontalFacingProperty);
			} else if (facings.equals(Util.verticalFacings))
			{
				builder.add(verticalFacingProperty);
			} else
			{
				builder.add(DirectionProperty.create("facing", facings));
			}
		}

		if (data.canActive)
		{
			builder.add(ACTIVE);
		}

		if (data.cropType != null)
		{
			if (data.cropType == Ic2CropType.none)
			{
				builder.add(CROSSING_BASE);
			} else
			{
				builder.add(this.getAgeProperty(data.maxAge));
			}
		}
	}

	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		BlockState ret = super.getStateForPlacement(ctx);
		if (ret == null)
		{
			return null;
		}

		if (this.facingProperty != null)
		{
			ret = ret.setValue(this.facingProperty, this.getPlacementFacing(ctx.getPlayer(), ctx.getNearestLookingDirection()));
		}

		if (this.canActive)
		{
			ret = ret.setValue(ACTIVE, false);
		}

		if (this.cropType != null)
		{
			if (this.cropType == Ic2CropType.none)
			{
				ret = ret.setValue(CROSSING_BASE, false);
			} else
			{
				ret = ret.setValue(this.getAgeProperty(), 0);
			}
		}

		return ret;
	}

	private Direction getPlacementFacing(LivingEntity placer, Direction facing)
	{
		Set<Direction> supportedFacings = this.getSupportedFacings();
		if (supportedFacings.isEmpty())
		{
			return Direction.DOWN;
		}

		if (placer != null)
		{
			Vec3 dir = placer.getLookAngle();
			Direction bestFacing = null;
			double maxMatch = Double.NEGATIVE_INFINITY;

			for (Direction cFacing : supportedFacings)
			{
				double match = dir.dot(Vec3.atLowerCornerOf(cFacing.getOpposite().getNormal()));
				if (match > maxMatch)
				{
					maxMatch = match;
					bestFacing = cFacing;
				}
			}

			return bestFacing;
		} else
		{
			return facing != null && supportedFacings.contains(facing.getOpposite()) ? facing.getOpposite() : this.getSupportedFacings().iterator().next();
		}
	}

	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		return this.cropType == null
			? super.canSurvive(state, world, pos)
			: CropSoilType.contains(world.getBlockState(pos.below()).getBlock()) && super.canSurvive(state, world, pos);
	}

	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
	{
		if (this.cropType == null)
		{
			return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
		} else
		{
			return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, world, pos, neighborPos);
		}
	}

	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
	{
		super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
		Ic2TileEntity tileEntity = getTe(world, pos);
		if (tileEntity != null)
		{
			tileEntity.onNeighborChange(sourceBlock, sourcePos);
		}
	}

	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
	{
		if (this.cropType != null && entity instanceof Ravager && world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
		{
			world.destroyBlock(pos, true, entity);
		}

		super.entityInside(state, world, pos, entity);
	}

	private void updateStateForEnergyNet(Level world, BlockPos pos, BlockState state, LivingEntity placer)
	{
		if (this.supportedFacings.size() > 1)
		{
			Direction[] supportedFacingArr = new Direction[this.supportedFacings.size()];
			supportedFacingArr = this.supportedFacings.toArray(supportedFacingArr);
			Direction direction = state.getValue(this.facingProperty);
			Direction iDirection = direction.equals(supportedFacingArr[0]) ? supportedFacingArr[1] : supportedFacingArr[0];
			this.setFacing(world, pos, iDirection, (Player) placer);
			this.setFacing(world, pos, direction, (Player) placer);
		}
	}

	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		Ic2TileEntity te = getTe(world, pos);
		if (te != null)
		{
			this.updateStateForEnergyNet(world, pos, state, placer);
			te.onPlaced(stack, placer, this.facingProperty != null ? state.getValue(this.facingProperty) : Direction.NORTH);
		}
	}

	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		Ic2TileEntity be = getTe(world, pos);
		if (be == null)
		{
			return !this.hasCollision ? Shapes.empty() : Shapes.block();
		} else
		{
			return be.getOutlineShape(context);
		}
	}

	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		Ic2TileEntity be = getTe(world, pos);
		if (be == null)
		{
			return !this.hasCollision ? Shapes.empty() : Shapes.block();
		} else
		{
			return be.getCollisionShape(context);
		}
	}

	public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos)
	{
		Ic2TileEntity be = getTe(world, pos);
		if (be == null)
		{
			return !this.hasCollision ? Shapes.empty() : Shapes.block();
		} else
		{
			return be.getCullingShape();
		}
	}

	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (player.isShiftKeyDown())
		{
			return InteractionResult.PASS;
		}

		Ic2TileEntity te = getTe(world, pos);
		return te == null ? InteractionResult.PASS : te.onActivated(player, hand, hit.getDirection(), hit.getLocation());
	}

	@Override
	public InteractionResult startBreak(Player player, Level world, InteractionHand hand, BlockPos pos, BlockState state, Direction direction)
	{
		return world.getBlockEntity(pos) instanceof Ic2TileEntity ic2TileEntity ? ic2TileEntity.onClicked(player) : InteractionResult.PASS;
	}

	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
	{
		if (!state.is(newState.getBlock()) && !moved)
		{
			if (world.getBlockEntity(pos) instanceof Ic2TileEntity tileEntity)
			{
				for (ItemStack stack : tileEntity.getAuxDrops(0))
				{
					Block.popResource(world, pos, stack);
				}

				super.onRemove(state, world, pos, newState, moved);
			}
		} else
		{
			super.onRemove(state, world, pos, newState, moved);
		}
	}

	@Override
	public Direction getFacing(Level world, BlockPos pos)
	{
		Ic2TileEntity te = getTe(world, pos);
		return te == null ? Direction.DOWN : te.getFacing();
	}

	@Override
	public boolean canSetFacing(Level world, BlockPos pos, Direction newDirection, Player player)
	{
		Ic2TileEntity te = getTe(world, pos);
		return te != null && te.canSetFacingWrench(newDirection, player);
	}

	@Override
	public boolean setFacing(Level world, BlockPos pos, Direction newDirection, Player player)
	{
		Ic2TileEntity te = getTe(world, pos);
		return te != null && te.setFacingWrench(world, newDirection, player);
	}

	@Override
	public boolean wrenchCanRemove(Level world, BlockPos pos, Player player)
	{
		Ic2TileEntity te = getTe(world, pos);
		return te != null && te.wrenchCanRemove(player);
	}

	@Override
	public List<ItemStack> getWrenchDrops(Level world, BlockPos pos, BlockState state, BlockEntity te, Player player, int fortune)
	{
		ItemStack stack = state.getBlock().asItem().getDefaultInstance();
		if (te instanceof Ic2TileEntity)
		{
			stack = ((Ic2TileEntity) te).adjustDrop(stack, true);
		}

		return Collections.singletonList(stack);
	}

	@Override
	public boolean retexture(
		BlockState state,
		Level world,
		BlockPos pos,
		Direction side,
		Player player,
		BlockState refState,
		String refVariant,
		Direction refSide,
		int[] refColorMultipliers
	)
	{
		Ic2TileEntity te = getTe(world, pos);
		if (te == null)
		{
			return false;
		}

		Obscuration component = te.getComponent(Obscuration.class);
		return component != null && component.applyObscuration(side, new Obscuration.ObscurationData(refState, refVariant, refSide, refColorMultipliers));
	}

	private static Ic2TileEntity getTe(BlockGetter world, BlockPos pos)
	{
		BlockEntity te = world.getBlockEntity(pos);
		return te instanceof Ic2TileEntity ? (Ic2TileEntity) te : null;
	}

	public Ic2TileEntity getDummyTe()
	{
		return this.dummyTe.get();
	}

	public enum DefaultDrop
	{
		Self,
		None,
		Generator,
		Machine,
		AdvMachine
	}

	private record InitData(Set<Direction> supportedFacings, boolean canActive, Class<?> teClass, Ic2CropType cropType,
	                        int maxAge)
	{
	}
}
