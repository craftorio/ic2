package ic2.core.block.generator.tileentity;

import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ConfigUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityGeoGenerator extends TileEntityBaseGenerator
{
	private static final int fluidPerTick = 2;
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput outputSlot;
	@GuiSynced
	protected final Ic2FluidTank fluidTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityGeoGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.GEO_GENERATOR, pos, state, 20.0, 1, 2400);
		this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(net.minecraft.world.level.material.Fluids.LAVA));
		this.production = Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/geothermal"));
		this.fluidSlot = new InvSlotConsumableLiquidByTank(
			this, "fluidSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, this.fluidTank
		);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot))
		{
			this.setChanged();
		}
	}

	@Override
	public boolean gainFuel()
	{
		boolean dirty = false;
		Ic2FluidStack ret = this.fluidTank.drainMbUnchecked(2, true);
		if (ret != null && ret.getAmountMb() >= 2)
		{
			this.fluidTank.drainMbUnchecked(2, false);
			this.fuel++;
			dirty = true;
		}

		return dirty;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.GENERATOR_GEOTHERMAL_LOOP;
	}

	@Override
	protected void onBlockBreak()
	{
		super.onBlockBreak();
		if (!this.fluidTank.isEmpty())
		{
		}
	}
}
