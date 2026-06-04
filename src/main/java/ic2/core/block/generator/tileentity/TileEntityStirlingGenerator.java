package ic2.core.block.generator.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@NotClassic
public class TileEntityStirlingGenerator extends TileEntityConversionGenerator
{
	private final double productionPerHeat = (0.5F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/Stirling"));

	protected IHeatSource source;

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
			if (te instanceof IHeatSource)
			{
				this.source = (IHeatSource) te;
			} else
			{
				this.source = null;
			}
		}
	}

	protected int getEnergyAvailable()
	{
		if (this.source == null)
			return 0;
		assert !((TileEntity) this.source).isInvalid();
		return this.source.drawHeat(getFacing().getOpposite(), this.source.getConnectionBandwidth(getFacing().getOpposite()), true);
	}

	protected void drawEnergyAvailable(int amount)
	{
		if (this.source != null)
		{
			assert !((TileEntity) this.source).isInvalid();
			this.source.drawHeat(getFacing().getOpposite(), amount, false);
		} else
		{
			assert false;
		}
	}

	protected double getMultiplier()
	{
		return this.productionPerHeat;
	}
}
