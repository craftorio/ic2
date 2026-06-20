package ic2.core.block.misc;

import ic2.core.ref.Ic2Blocks;
import ic2.core.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RubberWoodBlock extends Block
{
    public static final com.mojang.serialization.MapCodec<RubberWoodBlock> CODEC = simpleCodec(RubberWoodBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }

	public RubberWoodBlock(Properties settings)
	{
		super(settings);
	}

	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		ItemStack mainHandItem = player.getMainHandItem();
		if (mainHandItem.getItem() instanceof AxeItem)
		{
			WorldUtil.strip(state, world, pos, player, mainHandItem, Ic2Blocks.STRIPPED_RUBBER_WOOD.defaultBlockState());
			return InteractionResult.sidedSuccess(world.isClientSide);
		} else
		{
			return super.use(state, world, pos, player, hand, hit);
		}
	}
}
