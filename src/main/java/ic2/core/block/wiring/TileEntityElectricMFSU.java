package ic2.core.block.wiring;

import ic2.core.IC2;
import ic2.core.ref.TeBlock;

@TeBlock.Delegated(current = TileEntityElectricMFSU.class, old = TileEntityElectricMFSU.TileEntityElectricClassicMFSU.class)
public class TileEntityElectricMFSU extends TileEntityElectricBlock
{
	public static Class<? extends TileEntityElectricBlock> delegate()
	{
		return IC2.version.isClassic() ? TileEntityElectricMFSU.TileEntityElectricClassicMFSU.class : TileEntityElectricMFSU.class;
	}

	public TileEntityElectricMFSU()
	{
		super(4, 2048, 40000000);
	}

	@TeBlock.Delegated(current = TileEntityElectricMFSU.class, old = TileEntityElectricMFSU.TileEntityElectricClassicMFSU.class)
	public static class TileEntityElectricClassicMFSU extends TileEntityElectricBlock
	{
		public TileEntityElectricClassicMFSU()
		{
			super(3, 512, 10000000);
			this.chargeSlot.setTier(4);
			this.dischargeSlot.setTier(4);
		}
	}
}
