package ic2.core.crop;

import ic2.api.item.IBoxable;
import ic2.core.block.TileEntityBlock;
import ic2.core.item.ItemIC2;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCrop extends ItemIC2 implements IBoxable
{
	public ItemCrop()
	{
		super(ItemName.crop_stick);
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos))
		{
			pos = pos.offset(side);
		}

		ItemStack cropStickStack = StackUtil.get(player, hand);
		if (StackUtil.isEmpty(cropStickStack))
		{
			return EnumActionResult.PASS;
		} else if (world.getBlockState(pos.down()).getBlock() != Blocks.FARMLAND)
		{
			return EnumActionResult.PASS;
		} else if (!player.canPlayerEdit(pos, side, cropStickStack))
		{
			return EnumActionResult.PASS;
		} else if (!world.mayPlace(BlockName.te.getInstance(), pos, true, side, player))
		{
			return EnumActionResult.PASS;
		} else
		{
			TileEntityBlock tile = TileEntityBlock.instantiate(TeBlock.crop.getTeClass());
			if (ItemBlockTileEntity.placeTeBlock(cropStickStack, player, world, pos, side, tile))
			{
				SoundType stepSound = SoundType.PLANT;
				world.playSound(
					null,
					pos.getX() + 0.5,
					pos.getY() + 0.5,
					pos.getZ() + 0.5,
					stepSound.getPlaceSound(),
					SoundCategory.BLOCKS,
					(stepSound.getVolume() + 1.0F) / 2.0F,
					stepSound.getPitch() * 0.8F
				);
				StackUtil.consumeOrError(player, hand, 1);
				return EnumActionResult.SUCCESS;
			} else
			{
				return EnumActionResult.PASS;
			}
		}
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemStack)
	{
		return true;
	}
}
