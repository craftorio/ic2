package ic2.core.block.comp;

import ic2.core.block.tileentity.Ic2TileEntity;

public class ComparatorEmitter extends BasicRedstoneComponent
{
	public ComparatorEmitter(Ic2TileEntity parent)
	{
		super(parent);
	}

	@Override
	public void onChange()
	{
		this.parent.getLevel().updateNeighbourForOutputSignal(this.parent.getBlockPos(), this.parent.getBlockState().getBlock());
	}
}
