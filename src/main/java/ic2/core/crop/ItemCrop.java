package ic2.core.crop;

import ic2.api.crops.CropSoilType;
import ic2.api.item.IBoxable;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public class ItemCrop extends BlockItem implements IBoxable
{
	public ItemCrop(Properties settings)
	{
		super(Ic2Blocks.CROP_STICK, settings);
	}

	protected boolean canPlace(BlockPlaceContext context, BlockState state)
	{
		return CropSoilType.contains(context.getLevel().getBlockState(context.getClickedPos().below()).getBlock()) && super.canPlace(context, state);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemStack)
	{
		return true;
	}
}
