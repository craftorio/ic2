package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.container.ContainerFluidRegulator;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.LiquidUtil;

import java.util.Collections;
import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityFluidRegulator extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener
{
	private int mode;
	private int updateTicker;
	private int outputmb;
	private boolean newActive;
	public final InvSlotOutput wasseroutputSlot;
	public final InvSlotConsumableLiquidByTank wasserinputSlot;
	@GuiSynced
	protected final Fluids.InternalFluidTank fluidTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityFluidRegulator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.FLUID_REGULATOR, pos, state, 10000, 4);
		this.fluidTank = this.fluids.addTank("fluidTank", 10000, InvSlot.Access.NONE);
		this.wasserinputSlot = new InvSlotConsumableLiquidByTank(this, "wasserinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, this.fluidTank);
		this.wasseroutputSlot = new InvSlotOutput(this, "wasseroutputSlot", 1);
		this.newActive = false;
		this.outputmb = 0;
		this.mode = 0;
		this.updateTicker = IC2.random.nextInt(this.getTickRate());
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.outputmb = nbt.getInt("outputmb");
		this.mode = nbt.getInt("mode");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("outputmb", this.outputmb);
		nbt.putInt("mode", this.mode);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateConnectivity();
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.updateConnectivity();
	}

	private void updateConnectivity()
	{
		this.fluids.changeConnectivity(this.fluidTank, EnumSet.complementOf(EnumSet.of(this.getFacing())), Collections.emptySet());
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.wasserinputSlot.processIntoTank(this.fluidTank, this.wasseroutputSlot);
		if (this.updateTicker++ % this.getTickRate() == 0 || this.mode != 0)
		{
			this.newActive = this.work();
			if (this.getActive() != this.newActive)
			{
				this.setActive(this.newActive);
			}
		}
	}

	private boolean work()
	{
		if (this.outputmb == 0)
		{
			return false;
		}

		if (this.energy.getEnergy() < 10.0)
		{
			return false;
		}

		if (this.fluidTank.getFluidAmount() <= 0)
		{
			return false;
		}

		Direction dir = this.getFacing();
		BlockEntity te = this.getLevel().getBlockEntity(this.worldPosition.relative(dir));
		Direction side = dir.getOpposite();
		if (LiquidUtil.isFluidTile(te, side))
		{
			int amount = LiquidUtil.fillTile(te, side, this.fluidTank.drainMbUnchecked(this.outputmb, true), false);
			if (amount > 0)
			{
				this.fluidTank.drainMbUnchecked(this.outputmb, false);
				this.energy.useEnergy(10.0);
				return true;
			}
		}

		return false;
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event != 1001 && event != 1002)
		{
			this.outputmb += event;
			if (this.outputmb > 1000)
			{
				this.outputmb = 1000;
			}

			if (this.outputmb < 0)
			{
				this.outputmb = 0;
			}
		} else
		{
			if (event == 1001 && this.mode == 0)
			{
				this.mode = 1;
			}

			if (event == 1002 && this.mode == 1)
			{
				this.mode = 0;
			}
		}
	}

	public int getTickRate()
	{
		return 20;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerFluidRegulator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerFluidRegulator(syncId, inventory, this);
	}

	public int getOutputMb()
	{
		return this.outputmb;
	}

	public String getModeGui()
	{
		return switch (this.mode)
		{
			case 0 -> Component.translatable("ic2.generic.text.sec").getString();
			case 1 -> Component.translatable("ic2.generic.text.tick").getString();
			default -> "";
		};
	}

	public Ic2FluidTank getFluidTank()
	{
		return this.fluidTank;
	}
}
