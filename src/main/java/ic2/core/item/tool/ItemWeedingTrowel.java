package ic2.core.item.tool;

import ic2.api.crops.Crops;
import ic2.core.IC2;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemWeedingTrowel extends Item
{
	public ItemWeedingTrowel(Properties settings)
	{
		super(settings);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();

		if (!IC2.sideProxy.isSimulating())
		{
			return InteractionResult.PASS;
		}

		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof TileEntityCrop tileEntityCrop)
		{
			if (tileEntityCrop.getCrop() == Crops.weed)
			{
				StackUtil.dropAsEntity(world, pos, StackUtil.copyWithSize(new ItemStack(Ic2Items.WEED), tileEntityCrop.getCurrentAge() + 1));
				tileEntityCrop.reset();
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}
}
