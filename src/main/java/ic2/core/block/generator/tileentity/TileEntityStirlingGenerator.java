package ic2.core.block.generator.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.ConfigUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityStirlingGenerator extends TileEntityConversionGenerator
{
	private final double productionpeerheat = (double) (0.5F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/Stirling"));
	protected IHeatSource source;

	public TileEntityStirlingGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.STIRLING_GENERATOR, pos, state);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateSource();
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.updateSource();
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		if (this.getBlockPos().relative(this.getFacing()).equals(neighborPos))
		{
			this.updateSource();
		}
	}

	protected void updateSource()
	{
		if (this.source == null || ((BlockEntity) this.source).isRemoved())
		{
			BlockEntity te = this.level.getBlockEntity(this.worldPosition.relative(this.getFacing()));
			if (te instanceof IHeatSource)
			{
				this.source = (IHeatSource) te;
			} else
			{
				this.source = null;
			}
		}
	}

	@Override
	protected int getEnergyAvailable()
	{
		if (this.source == null)
		{
			return 0;
		}

		assert !((BlockEntity) this.source).isRemoved();
		return this.source.drawHeat(this.getFacing().m_122424_(), this.source.getConnectionBandwidth(this.getFacing().m_122424_()), true);
	}

	@Override
	protected void drawEnergyAvailable(int amount)
	{
		if (this.source != null)
		{
			assert !((BlockEntity) this.source).isRemoved();
			this.source.drawHeat(this.getFacing().m_122424_(), amount, false);
		} else
		{
			assert false;
		}
	}

	@Override
	protected double getMultiplier()
	{
		return this.productionpeerheat;
	}
}
