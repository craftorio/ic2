package ic2.core.block.generator.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityCreativeGenerator extends Ic2TileEntity implements IMultiEnergySource
{
	public TileEntityCreativeGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CREATIVE_GENERATOR, pos, state);
	}

	@Override
	public double getOfferedEnergy()
	{
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public void drawEnergy(double amount)
	{
	}

	@Override
	public int getSourceTier()
	{
		return 1;
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side)
	{
		return true;
	}

	@Override
	public boolean sendMultipleEnergyPackets()
	{
		return true;
	}

	@Override
	public int getMultipleEnergyPacketAmount()
	{
		return 10;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			EnergyNet.instance.addBlockEntityTile(this);
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (!this.getLevel().isClientSide)
		{
			EnergyNet.instance.removeTile(this);
		}

		super.onUnloaded();
	}
}
