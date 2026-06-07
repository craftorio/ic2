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

	protected boolean m_40610_(BlockPlaceContext context, BlockState state)
	{
		return CropSoilType.contains(context.m_43725_().getBlockState(context.m_8083_().m_7495_()).getBlock()) && super.m_40610_(context, state);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemStack)
	{
		return true;
	}
}
