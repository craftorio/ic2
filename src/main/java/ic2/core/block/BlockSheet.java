package ic2.core.block;

import ic2.core.IC2;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.type.IExtBlockType;
import ic2.core.item.block.ItemBlockSheet;
import ic2.core.item.type.MiscResourceType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.Ic2BlockPos;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSheet extends BlockMultiID<BlockSheet.SheetType>
{
	private static final AxisAlignedBB aabb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
	private static final EnumFacing[] positiveHorizontalFacings = new EnumFacing[] { EnumFacing.EAST, EnumFacing.SOUTH };

	public static BlockSheet create()
	{
		return BlockMultiID.create(BlockSheet.class, BlockSheet.SheetType.class);
	}

	public BlockSheet()
	{
		super(BlockName.sheet, Material.CIRCUITS, ItemBlockSheet.class);
	}

	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return aabb;
	}

	public void addCollisionBoxToList(
		IBlockState state,
		World worldIn,
		BlockPos pos,
		AxisAlignedBB entityBox,
		List<AxisAlignedBB> collidingBoxes,
		@Nullable Entity entityIn,
		boolean isActualState
	)
	{
		if (this.getType(state) != BlockSheet.SheetType.wool
			|| !(entityIn instanceof EntityPlayer)
			|| !entityIn.isSneaking() && !(entityIn.posY < pos.getY() + aabb.maxY - entityIn.stepHeight))
		{
			super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
	}

	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		AxisAlignedBB aabb = super.getCollisionBoundingBox(state, world, pos);
		switch ((BlockSheet.SheetType) this.getType(state))
		{
			case resin:
				return null;
			default:
				return aabb;
		}
	}

	public boolean canReplace(World world, BlockPos pos, EnumFacing side, ItemStack stack)
	{
		return this.isValidPosition(world, pos, this.getStateFromMeta(stack.getItemDamage()));
	}

	private boolean isValidPosition(World world, BlockPos pos, IBlockState state)
	{
		switch ((BlockSheet.SheetType) this.getType(state))
		{
			case resin:
				return this.isNormalCubeBelow(world, pos);
			case rubber:
				for (EnumFacing facing : EnumFacing.HORIZONTALS)
				{
					state = world.getBlockState(pos.offset(facing));
					if (state == BlockName.sheet.getBlockState(BlockSheet.SheetType.rubber) || state.getBlock().isNormalCube(state, world, pos))
					{
						return true;
					}
				}

				return this.isNormalCubeBelow(world, pos);
			case wool:
				return true;
			default:
				return false;
		}
	}

	private boolean isNormalCubeBelow(World world, BlockPos pos)
	{
		pos = pos.down();
		IBlockState state = world.getBlockState(pos);
		return state.getBlock().isNormalCube(state, world, pos);
	}

	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos)
	{
		if (!this.isValidPosition(world, pos, state))
		{
			world.setBlockToAir(pos);
			this.dropBlockAsItem(world, pos, state, 0);
		}
	}

	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		switch ((BlockSheet.SheetType) this.getType(state))
		{
			case resin:
				entity.fallDistance = (float) (entity.fallDistance * 0.75);
				entity.motionX *= 0.6;
				entity.motionY *= 0.85;
				entity.motionZ *= 0.6;
				break;
			case rubber:
				if (world.isBlockNormalCube(pos.down(), false))
				{
					return;
				}

				if (entity instanceof EntityLivingBase && !canSupportWeight(world, pos))
				{
					world.setBlockToAir(pos);
					return;
				}

				if (entity.motionY <= -0.4)
				{
					entity.fallDistance = 0.0F;
					entity.motionX *= 1.1;
					entity.motionZ *= 1.1;
					if (entity instanceof EntityLivingBase)
					{
						if (entity instanceof EntityPlayer && IC2.keyboard.isJumpKeyDown((EntityPlayer) entity))
						{
							entity.motionY *= -1.3;
						} else if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSneaking())
						{
							entity.motionY *= -0.1;
						} else
						{
							entity.motionY *= -0.8;
						}
					} else
					{
						entity.motionY *= -0.8;
					}
				}
				break;
			case wool:
				entity.fallDistance = (float) (entity.fallDistance * 0.95);
		}
	}

	private static boolean canSupportWeight(World world, BlockPos pos)
	{
		int maxRange = 16;
		Ic2BlockPos cPos = new Ic2BlockPos();

		for (EnumFacing axis : positiveHorizontalFacings)
		{
			for (int dir = -1; dir <= 1; dir += 2)
			{
				cPos.set(pos);
				boolean supported = false;

				for (int i = 0; i < 16; i++)
				{
					cPos.move(axis, dir);
					IBlockState state = cPos.getBlockState(world);
					if (state.getBlock().isNormalCube(state, world, cPos))
					{
						supported = true;
						break;
					}

					if (state != BlockName.sheet.getBlockState(BlockSheet.SheetType.rubber))
					{
						break;
					}

					cPos.moveDown();
					IBlockState baseState = cPos.getBlockState(world);
					if (baseState.getBlock().isNormalCube(baseState, world, cPos))
					{
						supported = true;
						break;
					}

					cPos.moveUp();
				}

				if (!supported)
				{
					break;
				}

				if (dir == 1)
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		switch ((BlockSheet.SheetType) this.getType(state))
		{
			case resin:
				if (IC2.random.nextInt(5) != 0)
				{
					List<ItemStack> ret = new ArrayList<>();
					ret.add(ItemName.misc_resource.getItemStack(MiscResourceType.resin));
					return ret;
				}

				return new ArrayList<>();
			default:
				return super.getDrops(world, pos, state, fortune);
		}
	}

	public enum SheetType implements IIdProvider, IExtBlockType
	{
		resin(1.6F, 0.5F),
		rubber(0.8F, 2.0F),
		@NotClassic
		wool(0.8F, 0.8F);

		public static BlockSheet.SheetType[] values = values();
		private final float hardness;
		private final float explosionResistance;

		SheetType(float hardness, float explosionResistance)
		{
			this.hardness = hardness;
			this.explosionResistance = explosionResistance;
		}

		@Override
		public String getName()
		{
			return this.name();
		}

		@Override
		public int getId()
		{
			return this.ordinal();
		}

		@Override
		public float getHardness()
		{
			return this.hardness;
		}

		@Override
		public float getExplosionResistance()
		{
			return this.explosionResistance;
		}
	}
}
