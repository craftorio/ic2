package ic2.core.crop;

import ic2.api.crops.ICropType;
import net.minecraft.world.item.ItemStack;

public abstract class CropBase extends Ic2CropCard
{
	protected final ItemStack cropDrop;

	public CropBase(ICropType cropType, ItemStack cropDrop)
	{
		super(cropType);
		this.cropDrop = cropDrop;
	}
}
