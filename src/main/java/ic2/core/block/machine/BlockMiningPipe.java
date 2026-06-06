package ic2.core.block.machine;

import ic2.core.block.BlockMultiID;
import ic2.core.block.state.IIdProvider;
import ic2.core.ref.BlockName;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMiningPipe extends BlockMultiID<BlockMiningPipe.MiningPipeType>
{
	private static final AxisAlignedBB pipeAabb = new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 1.0, 0.625);

	public static BlockMiningPipe create()
	{
		return BlockMultiID.create(BlockMiningPipe.class, BlockMiningPipe.MiningPipeType.class);
	}

	public BlockMiningPipe()
	{
		super(BlockName.mining_pipe, Material.IRON);
		this.setHardness(6.0F);
		this.setResistance(10.0F);
	}

	public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
	{
		return false;
	}

	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		BlockMiningPipe.MiningPipeType type = this.getType(state);
		return type == null ? true : type != BlockMiningPipe.MiningPipeType.pipe;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		BlockMiningPipe.MiningPipeType type = this.getType(state);
		return type == null ? super.getBoundingBox(state, world, pos) : this.getAabb(type);
	}

	private AxisAlignedBB getAabb(BlockMiningPipe.MiningPipeType type)
	{
		switch (type)
		{
			case pipe:
				return pipeAabb;
			case tip:
			default:
				return FULL_BLOCK_AABB;
		}
	}

	public int getLightOpacity(IBlockState state)
	{
		return state.isFullCube() ? 255 : 0;
	}

	public boolean isFullCube(IBlockState state)
	{
		BlockMiningPipe.MiningPipeType type = this.getType(state);
		if (type == null)
		{
			return super.isFullCube(state);
		}

		switch (type)
		{
			case pipe:
				return false;
			case tip:
			default:
				return true;
		}
	}

	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		BlockMiningPipe.MiningPipeType type = this.getType(state);
		if (type == null)
		{
			return true;
		}

		switch (type)
		{
			case pipe:
				return false;
			case tip:
				return true;
			default:
				return true;
		}
	}

	@Override
	public ItemStack getItemStack(IBlockState state)
	{
		BlockMiningPipe.MiningPipeType type = this.getType(state);
		return type == BlockMiningPipe.MiningPipeType.tip ? this.getItemStack(BlockMiningPipe.MiningPipeType.pipe) : super.getItemStack(state);
	}

	@Override
	public void getSubBlocks(CreativeTabs tabs, NonNullList<ItemStack> itemList)
	{
		for (BlockMiningPipe.MiningPipeType type : this.typeProperty.getShownValues())
		{
			if (type != BlockMiningPipe.MiningPipeType.tip)
			{
				itemList.add(this.getItemStack(type));
			}
		}
	}

	public enum MiningPipeType implements IIdProvider
	{
		pipe,
		tip;

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
	}
}
