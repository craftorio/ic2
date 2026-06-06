package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;

public class ComparatorEmitter extends BasicRedstoneComponent
{
	public ComparatorEmitter(TileEntityBlock parent)
	{
		super(parent);
	}

	@Override
	public void onChange()
	{
		this.parent.getWorld().updateComparatorOutputLevel(this.parent.getPos(), this.parent.getBlockType());
	}
}
