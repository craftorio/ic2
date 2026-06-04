package ic2.core.block.generator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@NotClassic
public class TileEntityKineticGenerator extends TileEntityConversionGenerator
{
	private final double euPerKu = 0.25D * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/Kinetic");

	protected IKineticSource source;

	protected void onLoaded()
	{
		super.onLoaded();
		updateSource();
	}

	protected void setFacing(EnumFacing facing)
	{
		super.setFacing(facing);
		updateSource();
	}

	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		if (getPos().offset(getFacing()).equals(neighborPos))
			updateSource();
	}

	protected void updateSource()
	{
		if (this.source == null || ((TileEntity) this.source).isInvalid())
		{
			TileEntity te = this.world.getTileEntity(this.pos.offset(getFacing()));
			if (te instanceof IKineticSource)
			{
				this.source = (IKineticSource) te;
			} else
			{
				this.source = null;
			}
		}
	}

	protected int getEnergyAvailable()
	{
		if (this.source != null)
		{
			assert !((TileEntity) this.source).isInvalid();
			return this.source.drawKineticEnergy(getFacing().getOpposite(), this.source.getConnectionBandwidth(getFacing().getOpposite()), true);
		}
		return 0;
	}

	protected void drawEnergyAvailable(int amount)
	{
		if (this.source != null)
		{
			assert !((TileEntity) this.source).isInvalid();
			this.source.drawKineticEnergy(getFacing().getOpposite(), amount, false);
		} else
		{
			assert false;
		}
	}

	protected double getMultiplier()
	{
		return this.euPerKu;
	}
}
