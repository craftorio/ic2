package ic2.core.crop;

import ic2.core.ref.Ic2Items;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CropStickBlock extends Block
{
	public static final BooleanProperty CROSSING_BASE = BooleanProperty.create("crossing_base");

	public CropStickBlock(Properties settings)
	{
		super(settings);
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(new Property[] { CROSSING_BASE });
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		return (BlockState) Objects.requireNonNull(super.getStateForPlacement(ctx)).setValue(CROSSING_BASE, false);
	}

	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		ItemStack heldStack = player.getItemInHand(hand);
		if (heldStack.is(Ic2Items.CROP_STICK))
		{
			world.setBlockAndUpdate(pos, (BlockState) state.setValue(CROSSING_BASE, true));
			return InteractionResult.SUCCESS;
		} else
		{
			return super.use(state, world, pos, player, hand, hit);
		}
	}
}
