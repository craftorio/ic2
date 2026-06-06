package ic2.core.block.storage.box;

import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;

public class TileEntityWoodenStorageBox extends TileEntityStorageBox
{
	public TileEntityWoodenStorageBox()
	{
		super(27);
	}

	@Override
	protected SoundType getBlockSound(Entity entity)
	{
		return SoundType.WOOD;
	}
}
