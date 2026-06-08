package ic2.core.block.misc;

import ic2.core.ref.Ic2Blocks;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FoamBlock extends Block
{
	public static final EnumProperty<FoamBlock.FoamType> typeProperty = EnumProperty.create("type", FoamBlock.FoamType.class);

	public FoamBlock(Properties settings)
	{
		super(settings);
		this.registerDefaultState((BlockState) this.defaultBlockState().setValue(typeProperty, FoamBlock.FoamType.normal));
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(new Property[] { typeProperty });
	}

	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return Shapes.empty();
	}

	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
	{
		int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
		if (tickSpeed <= 0)
		{
			throw new IllegalStateException("Foam was randomly ticked when world " + world + " is not ticking?");
		}

		FoamBlock.FoamType type = (FoamBlock.FoamType) state.getValue(typeProperty);
		float chance = getHardenChance(world, pos, state, type) * 4096.0F / tickSpeed;
		if (random.nextFloat() < chance)
		{
			world.setBlockAndUpdate(pos, ((FoamBlock.FoamType) state.getValue(typeProperty)).getResult());
		}
	}

	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (StackUtil.consume(player, hand, StackUtil.sameItem(Blocks.SAND), 1))
		{
			world.setBlockAndUpdate(pos, ((FoamBlock.FoamType) state.getValue(typeProperty)).getResult());
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.FAIL;
		}
	}

	public static float getHardenChance(Level world, BlockPos pos, BlockState state, FoamBlock.FoamType type)
	{
		int light = world.getMaxLocalRawBrightness(pos);
		if (state.getLightBlock(world, pos) == 0)
		{
			for (Direction side : Util.ALL_DIRS)
			{
				light = Math.max(light, world.getMaxLocalRawBrightness(pos.relative(side)));
			}
		}

		int avgTime = type.hardenTime * (16 - light);
		return 1.0F / (avgTime * 20);
	}

	public enum FoamType implements StringRepresentable
	{
		normal(300),
		reinforced(600);

		public final int hardenTime;

		FoamType(int hardenTime)
		{
			this.hardenTime = hardenTime;
		}

		public String getSerializedName()
		{
			return this.name();
		}

		public List<ItemStack> getDrops()
		{
			switch (this)
			{
				case normal:
					return new ArrayList<>();
				case reinforced:
					return new ArrayList<>();
				default:
					throw new UnsupportedOperationException();
			}
		}

		public BlockState getResult()
		{
			switch (this)
			{
				case normal:
					return WallBlock.get(WallBlock.DEFAULT_COLOR).defaultBlockState();
				case reinforced:
					return Ic2Blocks.REINFORCED_STONE.defaultBlockState();
				default:
					throw new UnsupportedOperationException();
			}
		}
	}
}
