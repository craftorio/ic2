package ic2.core.block.misc;

import ic2.api.tile.RetexturableBlock;
import ic2.api.tile.StainableBlock;
import ic2.core.block.tileentity.TileEntityWall;
import ic2.core.ref.Ic2Blocks;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class WallBlock extends Block implements StainableBlock, RetexturableBlock
{
	public static final DyeColor DEFAULT_COLOR = DyeColor.LIGHT_GRAY;
	private static final Map<DyeColor, WallBlock> types = new EnumMap<>(DyeColor.class);
	final DyeColor color;

	public WallBlock(Properties settings, DyeColor color)
	{
		super(settings);
		this.color = color;
		types.put(color, this);
	}

	public static WallBlock get(DyeColor color)
	{
		return types.get(color);
	}

	@Override
	public DyeColor getColor(Level world, BlockPos pos, Direction side)
	{
		return this.color;
	}

	@Override
	public boolean setColor(Level world, BlockPos pos, Direction side, DyeColor color)
	{
		WallBlock newBlock;
		if (color != this.color && (newBlock = get(color)) != null)
		{
			world.setBlockAndUpdate(pos, newBlock.defaultBlockState());
			return true;
		} else
		{
			return false;
		}
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
		world.setBlockAndUpdate(pos, Ic2Blocks.OBSCURED_WALL.defaultBlockState());
		if (world.getBlockEntity(pos) instanceof TileEntityWall wallBe)
		{
			wallBe.setColor(this.color);
			if (wallBe.getBlockType().retexture(wallBe.getBlockState(), world, pos, side, player, refState, refVariant, refSide, refColorMultipliers))
			{
				return true;
			}
		}

		world.setBlockAndUpdate(pos, state);
		return false;
	}
}
