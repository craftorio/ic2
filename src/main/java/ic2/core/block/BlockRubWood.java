package ic2.core.block;

import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRubWood extends BlockBase
{
	public static final PropertyEnum<BlockRubWood.RubberWoodState> stateProperty = PropertyEnum.create("state", BlockRubWood.RubberWoodState.class);

	public BlockRubWood()
	{
		super(BlockName.rubber_wood, Material.WOOD);
		this.setTickRandomly(true);
		this.setHardness(1.0F);
		this.setSoundType(SoundType.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(stateProperty, BlockRubWood.RubberWoodState.plain_y));
	}

	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { stateProperty });
	}

	public IBlockState getStateFromMeta(int meta)
	{
		return meta >= 0 && meta < BlockRubWood.RubberWoodState.values.length
			? this.getDefaultState().withProperty(stateProperty, BlockRubWood.RubberWoodState.values[meta])
			: this.getDefaultState();
	}

	public int getMetaFromState(IBlockState state)
	{
		return ((BlockRubWood.RubberWoodState) state.getValue(stateProperty)).ordinal();
	}

	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
		return state.withProperty(stateProperty, getPlainAxisState(facing.getAxis()));
	}

	private static BlockRubWood.RubberWoodState getPlainAxisState(Axis axis)
	{
		switch (axis)
		{
			case X:
				return BlockRubWood.RubberWoodState.plain_x;
			case Y:
				return BlockRubWood.RubberWoodState.plain_y;
			case Z:
				return BlockRubWood.RubberWoodState.plain_z;
			default:
				throw new IllegalArgumentException("invalid axis: " + axis);
		}
	}

	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune)
	{
		if (!world.isRemote)
		{
			int count = this.quantityDropped(world.rand);

			for (int j1 = 0; j1 < count; j1++)
			{
				if (!(world.rand.nextFloat() > chance))
				{
					Item item = this.getItemDropped(state, world.rand, fortune);
					if (item != null)
					{
						spawnAsEntity(world, pos, new ItemStack(item, 1, 0));
					}

					if (!((BlockRubWood.RubberWoodState) state.getValue(stateProperty)).isPlain() && world.rand.nextInt(6) == 0)
					{
						spawnAsEntity(world, pos, ItemName.misc_resource.getItemStack(MiscResourceType.resin));
					}
				}
			}
		}
	}

	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		int range = 4;
		MutableBlockPos cPos = new MutableBlockPos();

		for (int y = -range; y <= range; y++)
		{
			for (int z = -range; z <= range; z++)
			{
				for (int x = -range; x <= range; x++)
				{
					cPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
					IBlockState cState = world.getBlockState(cPos);
					Block cBlock = cState.getBlock();
					if (cBlock.isLeaves(cState, world, cPos))
					{
						cBlock.beginLeavesDecay(cState, world, new BlockPos(cPos));
					}
				}
			}
		}
	}

	public void randomTick(World world, BlockPos pos, IBlockState state, Random random)
	{
		if (random.nextInt(7) == 0)
		{
			BlockRubWood.RubberWoodState rwState = (BlockRubWood.RubberWoodState) state.getValue(stateProperty);
			if (!rwState.canRegenerate())
			{
				return;
			}

			world.setBlockState(pos, state.withProperty(stateProperty, rwState.getWet()));
		}
	}

	public EnumPushReaction getMobilityFlag(IBlockState state)
	{
		BlockRubWood.RubberWoodState rstate = (BlockRubWood.RubberWoodState) state.getValue(stateProperty);
		return rstate != BlockRubWood.RubberWoodState.plain_x && rstate != BlockRubWood.RubberWoodState.plain_y && rstate != BlockRubWood.RubberWoodState.plain_z
			? EnumPushReaction.BLOCK
			: EnumPushReaction.NORMAL;
	}

	public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return true;
	}

	public boolean isWood(IBlockAccess world, BlockPos pos)
	{
		return true;
	}

	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		return 4;
	}

	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		return 20;
	}

	public enum RubberWoodState implements IStringSerializable
	{
		plain_y(Axis.Y, null, false),
		plain_x(Axis.X, null, false),
		plain_z(Axis.Z, null, false),
		dry_north(Axis.Y, EnumFacing.NORTH, false),
		dry_south(Axis.Y, EnumFacing.SOUTH, false),
		dry_west(Axis.Y, EnumFacing.WEST, false),
		dry_east(Axis.Y, EnumFacing.EAST, false),
		wet_north(Axis.Y, EnumFacing.NORTH, true),
		wet_south(Axis.Y, EnumFacing.SOUTH, true),
		wet_west(Axis.Y, EnumFacing.WEST, true),
		wet_east(Axis.Y, EnumFacing.EAST, true);

		public final Axis axis;
		public final EnumFacing facing;
		public final boolean wet;
		private static final BlockRubWood.RubberWoodState[] values = values();

		RubberWoodState(Axis axis, EnumFacing facing, boolean wet)
		{
			this.axis = axis;
			this.facing = facing;
			this.wet = wet;
		}

		public String getName()
		{
			return this.name();
		}

		public boolean isPlain()
		{
			return this.facing == null;
		}

		public boolean canRegenerate()
		{
			return !this.isPlain() && !this.wet;
		}

		public BlockRubWood.RubberWoodState getWet()
		{
			if (this.isPlain())
			{
				return null;
			} else
			{
				return this.wet ? this : values[this.ordinal() + 4];
			}
		}

		public BlockRubWood.RubberWoodState getDry()
		{
			return !this.isPlain() && this.wet ? values[this.ordinal() - 4] : this;
		}

		public static BlockRubWood.RubberWoodState getWet(EnumFacing facing)
		{
			switch (facing)
			{
				case NORTH:
					return wet_north;
				case SOUTH:
					return wet_south;
				case WEST:
					return wet_west;
				case EAST:
					return wet_east;
				default:
					throw new IllegalArgumentException("incompatible facing: " + facing);
			}
		}
	}
}
