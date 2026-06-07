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
	public static final EnumProperty<DyeColor> colorProperty = EnumProperty.m_61587_("color", DyeColor.class);
	public static final EnumProperty<CableFoam> foamProperty = EnumProperty.m_61587_("foam", CableFoam.class);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.f_61362_;
	public static final BooleanProperty UP = BlockStateProperties.f_61366_;
	public static final BooleanProperty DOWN = BlockStateProperties.f_61367_;
	public static final BooleanProperty NORTH = BlockStateProperties.f_61368_;
	public static final BooleanProperty EAST = BlockStateProperties.f_61369_;
	public static final BooleanProperty SOUTH = BlockStateProperties.f_61370_;
	public static final BooleanProperty WEST = BlockStateProperties.f_61371_;
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

		this.m_49959_(defaultState);
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
		BlockState defaultState = (BlockState) this.f_49792_.m_61090_();
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

	protected void m_7926_(Builder<Block, BlockState> builder)
	{
		if (this.isFoam())
		{
			builder.m_61104_(new Property[] { foamProperty });
		} else
		{
			builder.m_61104_(new Property[] { WATERLOGGED, UP, DOWN, NORTH, EAST, SOUTH, WEST });
		}

		if (pendingHasColor)
		{
			builder.m_61104_(new Property[] { colorProperty });
		}
	}

	public FluidState m_5888_(BlockState state)
	{
		return !this.isFoam() && state.getValue(WATERLOGGED) ? Fluids.f_76193_.m_76068_(false) : Fluids.f_76191_.defaultFluidState();
	}

	public BlockState withConnectionStates(BlockState state, Level world, BlockPos pos)
	{
		boolean isConnectedDown = this.isConnectedWith(state, null, world, pos.m_7495_());
		boolean isConnectedUp = this.isConnectedWith(state, null, world, pos.m_7494_());
		boolean isConnectedNorth = this.isConnectedWith(state, null, world, pos.m_122012_());
		boolean isConnectedEast = this.isConnectedWith(state, null, world, pos.m_122029_());
		boolean isConnectedSouth = this.isConnectedWith(state, null, world, pos.m_122019_());
		boolean isConnectedWest = this.isConnectedWith(state, null, world, pos.m_122024_());
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
			return neighborState.m_204336_(Ic2BlockTags.CABLE_CONNECTABLE);
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

	public BlockState m_5573_(BlockPlaceContext ctx)
	{
		BlockState ret = this.defaultBlockState();
		Fluid fluid = ctx.m_43725_().m_6425_(ctx.m_8083_()).m_76152_();
		if (fluid == Fluids.f_76193_)
		{
			ret = (BlockState) ret.setValue(WATERLOGGED, true);
		}

		if (!this.isFoam())
		{
			ret = this.withConnectionStates(ret, ctx.m_43725_(), ctx.m_8083_());
		} else if (fluid == Ic2Fluids.CONSTRUCTION_FOAM.still)
		{
			ret = (BlockState) ret.setValue(foamProperty, CableFoam.SOFT);
		}

		return ret;
	}

	public VoxelShape m_7952_(BlockState state, BlockGetter world, BlockPos pos)
	{
		if (!this.isFoam())
		{
			return super.m_7952_(state, world, pos);
		} else
		{
			return ((CableFoam) state.getValue(foamProperty)).isSoft() ? Shapes.m_83040_() : super.m_7952_(state, world, pos);
		}
	}

	public VoxelShape m_5940_(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		if (!this.isFoam())
		{
			return super.m_5940_(state, world, pos, context);
		} else
		{
			return ((CableFoam) state.getValue(foamProperty)).isPresent() ? Shapes.block() : Shapes.m_83040_();
		}
	}

	public BlockState m_7417_(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
	{
		if (!this.isFoam() && (Boolean) state.getValue(WATERLOGGED))
		{
			world.m_186469_(pos, Fluids.f_76193_, Fluids.f_76193_.m_6718_(world));
		}

		if (!this.isFoam())
		{
			boolean isConnected = this.isConnectedWith(state, neighborState, (Level) world, neighborPos);
			return (BlockState) state.setValue((Property) PipeBlock.f_55154_.get(direction), isConnected);
		} else
		{
			return super.m_7417_(state, direction, neighborState, world, pos, neighborPos);
		}
	}

	public boolean m_6044_(BlockGetter world, BlockPos pos, BlockState state, Fluid fluid)
	{
		return !this.isFoam() && !(Boolean) state.getValue(WATERLOGGED) && fluid == Fluids.f_76193_;
	}

	public boolean m_7361_(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState)
	{
		if (!this.m_6044_(world, pos, state, fluidState.m_76152_()))
		{
			return false;
		}

		if (!world.m_5776_())
		{
			world.m_7731_(pos, (BlockState) state.setValue(WATERLOGGED, true), 3);
			world.m_186469_(pos, fluidState.m_76152_(), fluidState.m_76152_().m_6718_(world));
		}

		return true;
	}

	public ItemStack m_142598_(LevelAccessor world, BlockPos pos, BlockState state)
	{
		if (!this.isFoam() && (Boolean) state.getValue(WATERLOGGED))
		{
			world.m_7731_(pos, (BlockState) state.setValue(WATERLOGGED, false), 3);
			return new ItemStack(Items.f_42447_);
		} else
		{
			return ItemStack.EMPTY;
		}
	}

	public void m_6256_(BlockState state, Level world, BlockPos pos, Player player)
	{
		if (!this.isHardFoam(state))
		{
			ItemStack stack = player.m_21205_();
			Item item = stack.getItem();
			if (item instanceof ItemToolCutter)
			{
				((ItemToolCutter) item).removeInsulation(player, player.m_7655_(), state, world, pos);
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

	public void m_6810_(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
	{
		if (!newState.m_60713_(this) || this.getColor(newState) != this.getColor(state))
		{
			this.removeFromEnet(state, world, pos);
		}
	}

	public void m_6807_(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
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
			this.pos = pos.m_7949_();
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
