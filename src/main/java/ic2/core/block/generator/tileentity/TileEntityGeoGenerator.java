package ic2.core.block.generator.tileentity;

import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.ConfigUtil;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class TileEntityGeoGenerator extends TileEntityBaseGenerator
{
	private static final int fluidPerTick = 2;

	public final InvSlotConsumableLiquid fluidSlot;

	public final InvSlotOutput outputSlot;

	@GuiSynced
	protected final FluidTank fluidTank;

	protected final Fluids fluids;

	public TileEntityGeoGenerator()
	{
		super(20.0D, 1, 2400);
		this.fluids = (Fluids) addComponent((TileEntityComponent) new Fluids(this));
		this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(FluidRegistry.LAVA));
		this.production = Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/geothermal"));
		this.fluidSlot = new InvSlotConsumableLiquidByTank(this, "fluidSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, this.fluidTank);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
	}

	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot))
			markDirty();
	}

	public boolean gainFuel()
	{
		boolean dirty = false;
		FluidStack ret = this.fluidTank.drainInternal(2, false);
		if (ret != null && ret.amount >= 2)
		{
			this.fluidTank.drainInternal(2, true);
			this.fuel++;
			dirty = true;
		}
		return dirty;
	}

	public String getOperationSoundFile()
	{
		return "Generators/GeothermalLoop.ogg";
	}

	protected void onBlockBreak()
	{
		super.onBlockBreak();
		FluidEvent.fireEvent(new FluidEvent.FluidSpilledEvent(new FluidStack(FluidRegistry.LAVA, 1000), getWorld(), this.pos));
	}
}
