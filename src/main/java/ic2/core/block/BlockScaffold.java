package ic2.core.block;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.type.IBlockSound;
import ic2.core.block.type.IExtBlockType;
import ic2.core.ref.BlockName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockScaffold extends BlockMultiID<BlockScaffold.ScaffoldType>
{
	private static final IRecipeInput stickInput = Recipes.inputFactory.forOreDict("stickWood");
	private static final EnumFacing[] supportedFacings = new EnumFacing[] { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
	private static final double border = 0.03125;
	private static final AxisAlignedBB aabb = new AxisAlignedBB(0.03125, 0.0, 0.03125, 0.96875, 1.0, 0.96875);

	public static BlockScaffold create()
	{
		return BlockMultiID.create(BlockScaffold.class, BlockScaffold.ScaffoldType.class);
	}

	private BlockScaffold()
	{
		super(BlockName.scaffold, Material.WOOD);
		this.setTickRandomly(true);
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	public Material getMaterial(IBlockState state)
	{
		BlockScaffold.ScaffoldType type = this.getType(state);
		if (type == null)
		{
			return super.getMaterial(state);
		}

		switch (type)
		{
			case wood:
			case reinforced_wood:
				return Material.WOOD;
			case iron:
			case reinforced_iron:
				return Material.IRON;
			default:
				throw new IllegalStateException("Invalid scaffold type: " + type);
		}
	}

	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	public boolean isNormalCube(IBlockState state)
	{
		return false;
	}

	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		return true;
	}

	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity rawEntity)
	{
		if (rawEntity instanceof EntityLivingBase)
		{
			EntityLivingBase entity = (EntityLivingBase) rawEntity;
			entity.fallDistance = 0.0F;
			double limit = 0.15;
			entity.motionX = Util.limit(entity.motionX, -limit, limit);
			entity.motionZ = Util.limit(entity.motionZ, -limit, limit);
			if (entity.isSneaking() && entity instanceof EntityPlayer)
			{
				if (entity.isInWater())
				{
					entity.motionY = 0.02;
				} else
				{
					entity.motionY = 0.08;
				}
			} else if (entity.collidedHorizontally)
			{
				entity.motionY = 0.2;
			} else
			{
				entity.motionY = Math.max(entity.motionY, -0.07);
			}
		}
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return aabb;
	}

	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
	{
		return FULL_BLOCK_AABB.offset(pos);
	}

	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return side.getAxis() == Axis.Y;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		if (state.getBlock() != this)
		{
			return Collections.emptyList();
		}

		List<ItemStack> ret = new ArrayList<>();
		BlockScaffold.ScaffoldType type = (BlockScaffold.ScaffoldType) state.getValue(this.typeProperty);
		switch (type)
		{
			case wood:
			case iron:
				ret.add(this.getItemStack(type));
				break;
			case reinforced_wood:
				ret.add(this.getItemStack(BlockScaffold.ScaffoldType.wood));
				ret.add(new ItemStack(Items.STICK, 2));
				break;
			case reinforced_iron:
				ret.add(this.getItemStack(BlockScaffold.ScaffoldType.iron));
				ret.add(BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron));
				break;
			default:
				throw new IllegalStateException();
		}

		return ret;
	}

	public boolean onBlockActivated(
		World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ
	)
	{
		if (player.isSneaking())
		{
			return false;
		}

		ItemStack stack = player.getHeldItem(hand);
		if (StackUtil.isEmpty(stack))
		{
			return false;
		}

		BlockScaffold.ScaffoldType type = this.getType(state);
		if (type == null)
		{
			return false;
		}

		int stickCount = 2;
		int fenceCount = 1;
		switch (type)
		{
			case wood:
				if (!stickInput.matches(stack) || StackUtil.getSize(stack) < 2)
				{
					return false;
				}
				break;
			case reinforced_wood:
			case reinforced_iron:
				return false;
			case iron:
				if (!StackUtil.checkItemEquality(stack, BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron)) || StackUtil.getSize(stack) < 1)
				{
					return false;
				}
				break;
			default:
				throw new IllegalStateException();
		}

		if (!this.isPillar(world, pos))
		{
			return false;
		}

		switch (type)
		{
			case wood:
				StackUtil.consumeOrError(player, hand, StackUtil.recipeInput(stickInput), 2);
				type = BlockScaffold.ScaffoldType.reinforced_wood;
				break;
			case iron:
				StackUtil.consumeOrError(player, hand, StackUtil.sameStack(BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron)), 1);
				type = BlockScaffold.ScaffoldType.reinforced_iron;
				break;
			default:
				throw new IllegalStateException();
		}

		world.setBlockState(pos, state.withProperty(this.typeProperty, type));
		return true;
	}

	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
	{
		EnumHand hand = EnumHand.MAIN_HAND;
		ItemStack stack = player.getHeldItem(hand);
		if (!StackUtil.isEmpty(stack))
		{
			if (StackUtil.checkItemEquality(stack, Item.getItemFromBlock(this)))
			{
				while (world.getBlockState(pos).getBlock() == this)
				{
					pos = pos.up();
				}

				if (this.canPlaceBlockAt(world, pos) && pos.getY() < IC2.getWorldHeight(world))
				{
					boolean isCreative = player.capabilities.isCreativeMode;
					ItemStack prev = isCreative ? StackUtil.copy(stack) : null;
					stack.onItemUse(player, world, pos.down(), hand, EnumFacing.UP, 0.5F, 1.0F, 0.5F);
					if (!isCreative)
					{
						StackUtil.clearEmpty(player, hand);
					} else
					{
						StackUtil.set(player, hand, prev);
					}
				}
			}
		}
	}

	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		return super.canPlaceBlockAt(world, pos) && this.hasSupport(world, pos, BlockScaffold.ScaffoldType.wood);
	}

	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos)
	{
		this.checkSupport(world, pos);
	}

	public void randomTick(World world, BlockPos pos, IBlockState state, Random random)
	{
		if (random.nextInt(8) == 0)
		{
			this.checkSupport(world, pos);
		}
	}

	private boolean isPillar(World world, BlockPos pos)
	{
		while (world.getBlockState(pos).getBlock() == this)
		{
			pos = pos.down();
		}

		return world.isBlockNormalCube(pos, false);
	}

	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		BlockScaffold.ScaffoldType type = this.getType(world, pos);
		if (type == null)
		{
			return 0;
		}

		switch (type)
		{
			case wood:
			case reinforced_wood:
				return 8;
			case iron:
			case reinforced_iron:
				return 0;
			default:
				throw new IllegalStateException();
		}
	}

	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		BlockScaffold.ScaffoldType type = this.getType(world, pos);
		if (type == null)
		{
			return 0;
		}

		switch (type)
		{
			case wood:
			case reinforced_wood:
				return 20;
			case iron:
			case reinforced_iron:
				return 0;
			default:
				throw new IllegalStateException();
		}
	}

	private boolean hasSupport(IBlockAccess world, BlockPos start, BlockScaffold.ScaffoldType type)
	{
		return this.calculateSupport(world, start, type).get(start).strength >= 0;
	}

	private void checkSupport(World world, BlockPos start)
	{
		IBlockState state = world.getBlockState(start);
		if (state.getBlock() == this)
		{
			Map<BlockPos, BlockScaffold.Support> results = this.calculateSupport(world, start, (BlockScaffold.ScaffoldType) state.getValue(this.typeProperty));
			boolean droppedAny = false;

			for (BlockScaffold.Support support : results.values())
			{
				if (support.strength < 0)
				{
					world.setBlockState(support.pos, Blocks.AIR.getDefaultState(), 2);
					this.dropBlockAsItem(world, support.pos, this.getDefaultState().withProperty(this.typeProperty, support.type), 0);
					droppedAny = true;
				}
			}

			if (droppedAny)
			{
				for (BlockScaffold.Support support : results.values())
				{
					if (support.strength < 0)
					{
						world.notifyNeighborsRespectDebug(support.pos, this, true);
					}
				}
			}
		}
	}

	private Map<BlockPos, BlockScaffold.Support> calculateSupport(IBlockAccess world, BlockPos start, BlockScaffold.ScaffoldType type)
	{
		Map<BlockPos, BlockScaffold.Support> results = new HashMap<>();
		Queue<BlockScaffold.Support> queue = new ArrayDeque<>();
		Set<BlockPos> groundSupports = new HashSet<>();
		BlockScaffold.Support support = new BlockScaffold.Support(start, type, -1);
		results.put(start, support);
		queue.add(support);

		while ((support = queue.poll()) != null)
		{
			for (EnumFacing dir : EnumFacing.VALUES)
			{
				BlockPos pos = support.pos.offset(dir);
				if (!results.containsKey(pos))
				{
					IBlockState state = world.getBlockState(pos);
					Block block = state.getBlock();
					if (block == this)
					{
						type = (BlockScaffold.ScaffoldType) state.getValue(this.typeProperty);
						BlockScaffold.Support cSupport = new BlockScaffold.Support(pos, type, -1);
						results.put(pos, cSupport);
						queue.add(cSupport);
					} else if (block.isNormalCube(state, world, pos))
					{
						groundSupports.add(pos);
					}
				}
			}
		}

		for (BlockPos groundPos : groundSupports)
		{
			BlockPos pos = groundPos.up();
			int propagatedStrength = 0;

			while (true)
			{
				support = results.get(pos);
				if (support == null)
				{
					break;
				}

				int strength;
				if (support.type.strength >= propagatedStrength)
				{
					strength = support.type.strength;
					propagatedStrength = strength - 1;
				} else
				{
					strength = propagatedStrength--;
				}

				if (support.strength < strength)
				{
					support.strength = strength;

					for (EnumFacing dir : EnumFacing.HORIZONTALS)
					{
						BlockPos nPos = pos.offset(dir);
						BlockScaffold.Support nSupport = results.get(nPos);
						if (nSupport != null && nSupport.strength < strength)
						{
							nSupport.strength = strength - 1;
							queue.add(nSupport);
						}
					}
				}

				pos = pos.up();
			}
		}

		while ((support = queue.poll()) != null)
		{
			for (EnumFacing dir : supportedFacings)
			{
				BlockPos pos = support.pos.offset(dir);
				BlockScaffold.Support nSupport = results.get(pos);
				if (nSupport != null && nSupport.strength < support.strength)
				{
					nSupport.strength = support.strength - 1;
					if (nSupport.strength > 0)
					{
						queue.add(nSupport);
					}
				}
			}
		}

		return results;
	}

	public enum ScaffoldType implements IIdProvider, IExtBlockType, IBlockSound
	{
		wood(2, 0.5F, 0.12F, SoundType.WOOD),
		reinforced_wood(5, 0.6F, 0.24F, SoundType.WOOD),
		iron(5, 0.8F, 6.0F, SoundType.METAL),
		reinforced_iron(12, 1.0F, 8.0F, SoundType.METAL);

		public final int strength;
		private final float hardness;
		private final float explosionResistance;
		private final SoundType sound;

		ScaffoldType(int strength, float hardness, float explosionResistance, SoundType sound)
		{
			if (strength < 1)
			{
				throw new IllegalArgumentException();
			}

			this.strength = strength;
			this.hardness = hardness;
			this.explosionResistance = explosionResistance;
			this.sound = sound;
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

		@Override
		public SoundType getSound()
		{
			return this.sound;
		}
	}

	private static class Support
	{
		final BlockPos pos;
		final BlockScaffold.ScaffoldType type;
		int strength;

		Support(BlockPos pos, BlockScaffold.ScaffoldType type, int strength)
		{
			this.pos = pos;
			this.type = type;
			this.strength = strength;
		}
	}
}
