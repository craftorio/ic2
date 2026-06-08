package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IColoredEnergyTile;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import ic2.core.block.ChunkLoadAwareBlock;
import ic2.core.item.tool.ItemToolCutter;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2Fluids;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractCableBlock extends PipeBlock implements ChunkLoadAwareBlock, SimpleWaterloggedBlock
{
	public static final DyeColor DEFAULT_COLOR = DyeColor.BLACK;
	public static final EnumProperty<DyeColor> colorProperty = EnumProperty.create("color", DyeColor.class);
	public static final EnumProperty<CableFoam> foamProperty = EnumProperty.create("foam", CableFoam.class);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	private static final Map<CableType, Int2ReferenceMap<AbstractCableBlock>> types = new EnumMap<>(CableType.class);
	private static boolean pendingHasColor;
	private boolean hasColor;
	final CableType type;
	final int insulation;

	public abstract boolean isFoam();

	public abstract boolean isHardFoam(BlockState var1);

	public void initializeState(BlockState defaultState)
	{
		if (this.isFoam())
		{
			defaultState = (BlockState) defaultState.setValue(foamProperty, FoamCableBlock.DEFAULT_FOAM);
		} else
		{
			defaultState = (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) defaultState.setValue(UP, false)).setValue(DOWN, false))
				.setValue(NORTH, false))
				.setValue(EAST, false))
				.setValue(SOUTH, false))
				.setValue(WEST, false);
			if (this.hasColor())
			{
				defaultState = (BlockState) defaultState.setValue(colorProperty, DEFAULT_COLOR);
			}
		}

		this.registerDefaultState(defaultState);
	}

	protected BlockState copyState(BlockState from, AbstractCableBlock to)
	{
		BlockState ret = to.defaultBlockState();
		if (to.insulation >= to.type.minColoredInsulation)
		{
			ret = (BlockState) ret.setValue(colorProperty, ((AbstractCableBlock) from.getBlock()).getColor(from));
		}

		if (this.isFoam())
		{
			ret = (BlockState) ret.setValue(foamProperty, (CableFoam) from.getValue(foamProperty));
		} else
		{
			ret = (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ret.setValue(UP, (Boolean) from.getValue(UP)))
				.setValue(DOWN, (Boolean) from.getValue(DOWN)))
				.setValue(NORTH, (Boolean) from.getValue(NORTH)))
				.setValue(EAST, (Boolean) from.getValue(EAST)))
				.setValue(SOUTH, (Boolean) from.getValue(SOUTH)))
				.setValue(WEST, (Boolean) from.getValue(WEST));
		}

		return ret;
	}

	protected static void prepareCreate(CableType type, int insulation)
	{
		pendingHasColor = insulation >= type.minColoredInsulation;
	}

	protected AbstractCableBlock(Properties settings, CableType type, int insulation)
	{
		super(type.getThickness(insulation) / 2.0F, settings);
		if (insulation > type.maxInsulation)
		{
			throw new IllegalArgumentException("invalid insulation " + insulation + " for type " + type);
		}

		this.type = type;
		this.insulation = insulation;
		this.hasColor = insulation >= type.minColoredInsulation;
		BlockState defaultState = (BlockState) this.stateDefinition.any();
		if (!this.isFoam())
		{
			defaultState = (BlockState) defaultState.setValue(WATERLOGGED, false);
		}

		this.initializeState(defaultState);
		if (type.maxInsulation > 0)
		{
			types.computeIfAbsent(type, t -> new Int2ReferenceOpenHashMap(t.maxInsulation + 1)).put(insulation, this);
		}
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		if (this.isFoam())
		{
			builder.add(new Property[] { foamProperty });
		} else
		{
			builder.add(new Property[] { WATERLOGGED, UP, DOWN, NORTH, EAST, SOUTH, WEST });
		}

		if (pendingHasColor)
		{
			builder.add(new Property[] { colorProperty });
		}
	}

	public FluidState getFluidState(BlockState state)
	{
		return !this.isFoam() && state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	public BlockState withConnectionStates(BlockState state, Level world, BlockPos pos)
	{
		boolean isConnectedDown = this.isConnectedWith(state, null, world, pos.below());
		boolean isConnectedUp = this.isConnectedWith(state, null, world, pos.above());
		boolean isConnectedNorth = this.isConnectedWith(state, null, world, pos.north());
		boolean isConnectedEast = this.isConnectedWith(state, null, world, pos.east());
		boolean isConnectedSouth = this.isConnectedWith(state, null, world, pos.south());
		boolean isConnectedWest = this.isConnectedWith(state, null, world, pos.west());
		return (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) state.setValue(DOWN, isConnectedDown)).setValue(UP, isConnectedUp))
			.setValue(NORTH, isConnectedNorth))
			.setValue(EAST, isConnectedEast))
			.setValue(SOUTH, isConnectedSouth))
			.setValue(WEST, isConnectedWest);
	}

	public boolean isConnectedWith(BlockState state, BlockState neighborState, Level world, BlockPos neighborPos)
	{
		if (neighborState == null)
		{
			neighborState = world.getBlockState(neighborPos);
		}

		if (!(neighborState.getBlock() instanceof AbstractCableBlock neighborBlock))
		{
			return neighborState.is(Ic2BlockTags.CABLE_CONNECTABLE);
		} else if (neighborBlock.hasColor() && this.hasColor())
		{
			DyeColor color = this.getColor(state);
			DyeColor neighborColor = getColor(neighborState, neighborBlock.type, neighborBlock.insulation);
			return color == DEFAULT_COLOR || neighborColor == DEFAULT_COLOR || neighborColor == this.getColor(state);
		} else
		{
			return true;
		}
	}

	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		BlockState ret = this.defaultBlockState();
		Fluid fluid = ctx.getLevel().getFluidState(ctx.getClickedPos()).getType();
		if (fluid == Fluids.WATER)
		{
			ret = (BlockState) ret.setValue(WATERLOGGED, true);
		}

		if (!this.isFoam())
		{
			ret = this.withConnectionStates(ret, ctx.getLevel(), ctx.getClickedPos());
		} else if (fluid == Ic2Fluids.CONSTRUCTION_FOAM.still)
		{
			ret = (BlockState) ret.setValue(foamProperty, CableFoam.SOFT);
		}

		return ret;
	}

	public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos)
	{
		if (!this.isFoam())
		{
			return super.getOcclusionShape(state, world, pos);
		} else
		{
			return ((CableFoam) state.getValue(foamProperty)).isSoft() ? Shapes.empty() : super.getOcclusionShape(state, world, pos);
		}
	}

	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		if (!this.isFoam())
		{
			return super.getShape(state, world, pos, context);
		} else
		{
			return ((CableFoam) state.getValue(foamProperty)).isPresent() ? Shapes.block() : Shapes.empty();
		}
	}

	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
	{
		if (!this.isFoam() && (Boolean) state.getValue(WATERLOGGED))
		{
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}

		if (!this.isFoam())
		{
			boolean isConnected = this.isConnectedWith(state, neighborState, (Level) world, neighborPos);
			return (BlockState) state.setValue((Property) PipeBlock.PROPERTY_BY_DIRECTION.get(direction), isConnected);
		} else
		{
			return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
		}
	}

	public boolean canPlaceLiquid(BlockGetter world, BlockPos pos, BlockState state, Fluid fluid)
	{
		return !this.isFoam() && !(Boolean) state.getValue(WATERLOGGED) && fluid == Fluids.WATER;
	}

	public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState)
	{
		if (!this.canPlaceLiquid(world, pos, state, fluidState.getType()))
		{
			return false;
		}

		if (!world.isClientSide())
		{
			world.setBlock(pos, (BlockState) state.setValue(WATERLOGGED, true), 3);
			world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
		}

		return true;
	}

	public ItemStack pickupBlock(LevelAccessor world, BlockPos pos, BlockState state)
	{
		if (!this.isFoam() && (Boolean) state.getValue(WATERLOGGED))
		{
			world.setBlock(pos, (BlockState) state.setValue(WATERLOGGED, false), 3);
			return new ItemStack(Items.WATER_BUCKET);
		} else
		{
			return ItemStack.EMPTY;
		}
	}

	public void attack(BlockState state, Level world, BlockPos pos, Player player)
	{
		if (!this.isHardFoam(state))
		{
			ItemStack stack = player.getMainHandItem();
			Item item = stack.getItem();
			if (item instanceof ItemToolCutter)
			{
				((ItemToolCutter) item).removeInsulation(player, player.getUsedItemHand(), state, world, pos);
			}
		}
	}

	public boolean hasColor()
	{
		return this.hasColor;
	}

	DyeColor getColor(BlockState state)
	{
		return getColor(state, this.type, this.insulation);
	}

	public static DyeColor getColor(BlockState state, CableType type, int insulation)
	{
		return insulation >= type.minColoredInsulation ? (DyeColor) state.getValue(colorProperty) : DEFAULT_COLOR;
	}

	public boolean tryAddInsulation(BlockState state, Level world, BlockPos pos)
	{
		if (this.insulation >= this.type.maxInsulation)
		{
			return false;
		}

		if (this.isHardFoam(state))
		{
			return false;
		}

		AbstractCableBlock newBlock = (AbstractCableBlock) types.get(this.type).get(this.insulation + 1);
		if (newBlock == null)
		{
			return false;
		}

		world.setBlockAndUpdate(pos, this.copyState(state, newBlock));
		return true;
	}

	public boolean tryRemoveInsulation(BlockState state, Level world, BlockPos pos, boolean simulate)
	{
		if (this.insulation <= 0)
		{
			return false;
		}

		if (this.isHardFoam(state))
		{
			return false;
		}

		AbstractCableBlock newBlock = (AbstractCableBlock) types.get(this.type).get(this.insulation - 1);
		if (newBlock == null)
		{
			return false;
		}

		if (simulate)
		{
			return true;
		}

		BlockState newState = this.copyState(state, newBlock);
		IEnergyTile tile = null;
		if (this.insulation == this.type.minColoredInsulation && this.getColor(state) != DEFAULT_COLOR)
		{
			assert newBlock.getColor(newState) == DEFAULT_COLOR;
			if (!world.isClientSide)
			{
				tile = EnergyNet.instance.getTile(world, pos);
				if (tile != null)
				{
					EnergyNet.instance.removeTile(tile);
				}
			}
		}

		world.setBlockAndUpdate(pos, newState);
		if (tile != null)
		{
			EnergyNet.instance.addTileUnchecked(tile);
		}

		return true;
	}

	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
	{
		if (!newState.is(this) || this.getColor(newState) != this.getColor(state))
		{
			this.removeFromEnet(state, world, pos);
		}
	}

	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
	{
		this.addToEnet(state, world, pos, true);
	}

	@Override
	public void onLoad(BlockState state, Level world, BlockPos pos)
	{
		this.addToEnet(state, world, pos, false);
	}

	@Override
	public void onUnload(BlockState state, Level world, BlockPos pos)
	{
		this.removeFromEnet(state, world, pos);
	}

	protected void addToEnet(BlockState state, Level world, BlockPos pos, boolean checkConflicting)
	{
		if (!checkConflicting || EnergyNet.instance.getTile(world, pos) == null)
		{
			EnergyNet.instance.addLocatableTile(new AbstractCableBlock.Conductor(state, world, pos));
		}
	}

	protected void removeFromEnet(BlockState state, Level world, BlockPos pos)
	{
		IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
		if (tile != null)
		{
			EnergyNet.instance.removeTile(tile);
		}
	}

	private final class Conductor implements ILocatable, IColoredEnergyTile, IEnergyConductor
	{
		private BlockState state;
		private final Level world;
		private final BlockPos pos;

		Conductor(BlockState state, Level world, BlockPos pos)
		{
			this.state = state;
			this.world = world;
			this.pos = pos.immutable();
		}

		@Override
		public Level getWorldObj()
		{
			return this.world;
		}

		@Override
		public BlockPos getPosition()
		{
			return this.pos;
		}

		@Override
		public DyeColor getColor(Direction side)
		{
			return AbstractCableBlock.getColor(this.state, AbstractCableBlock.this.type, AbstractCableBlock.this.insulation);
		}

		@Override
		public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction side)
		{
			return this.canInteractWith(emitter, side);
		}

		@Override
		public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side)
		{
			return this.canInteractWith(receiver, side);
		}

		@Override
		public double getConductionLoss()
		{
			return AbstractCableBlock.this.type.loss;
		}

		@Override
		public double getInsulationEnergyAbsorption()
		{
			if (AbstractCableBlock.this.type.maxInsulation == 0)
			{
				return 2.147483647E9;
			} else
			{
				return AbstractCableBlock.this.type.capacity < 128
					? EnergyNet.instance.getPowerFromTier(AbstractCableBlock.this.insulation)
					: EnergyNet.instance.getPowerFromTier(AbstractCableBlock.this.insulation + 1);
			}
		}

		@Override
		public double getInsulationBreakdownEnergy()
		{
			return 9001.0;
		}

		@Override
		public double getConductorBreakdownEnergy()
		{
			return AbstractCableBlock.this.type.capacity + 1;
		}

		@Override
		public void removeInsulation()
		{
			AbstractCableBlock.this.tryRemoveInsulation(this.state, this.world, this.pos, false);
		}

		@Override
		public void removeConductor()
		{
			this.world.removeBlock(this.pos, false);
		}

		@Override
		public void onConnectionChange()
		{
		}

		private void setState(BlockState state)
		{
			assert state != this.state;
			this.state = state;
			this.world.setBlockAndUpdate(this.pos, state);
		}
	}
}
