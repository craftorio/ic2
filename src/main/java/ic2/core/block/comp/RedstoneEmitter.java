package ic2.core.block.comp;

import ic2.core.block.tileentity.Ic2TileEntity;

public class RedstoneEmitter extends BasicRedstoneComponent
{
	public RedstoneEmitter(Ic2TileEntity parent)
	{
		super(parent);
	}

	@Override
	public void onChange()
	{
		this.parent.getLevel().m_46672_(this.parent.getBlockPos(), this.parent.getBlockState().getBlock());
	}
}
