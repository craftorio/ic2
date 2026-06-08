package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.crop.TileEntityCrop;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCropmatron extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
	public final InvSlotUpgrade upgradeSlot;
	public int scanX = -4;
	public int scanY = -1;
	public int scanZ = -4;
	public final InvSlotConsumable fertilizerSlot;
	public final InvSlotOutput wasseroutputSlot;
	public final InvSlotOutput exOutputSlot;
	public final InvSlotConsumableLiquidByTank wasserinputSlot;
	public final InvSlotConsumableLiquidByTank exInputSlot;
	protected final Ic2FluidTank waterTank;
	protected final Ic2FluidTank exTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityCropmatron(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CROPMATRON, pos, state, 10000, 1);
		this.waterTank = this.fluids.addTankInsert("waterTank", 2000, Fluids.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER));
		this.exTank = this.fluids.addTankInsert("exTank", 2000, Fluids.fluidPredicate(Ic2Fluids.WEED_EX.still));
		this.fertilizerSlot = new InvSlotConsumableItemStack(this, "fertilizer", 7, new ItemStack(Ic2Items.FERTILIZER));
		this.wasserinputSlot = new InvSlotConsumableLiquidByTank(
			this, "wasserinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, this.waterTank
		);
		this.exInputSlot = new InvSlotConsumableLiquidByTank(
			this, "exInputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, this.exTank
		);
		this.wasseroutputSlot = new InvSlotOutput(this, "wasseroutputSlot", 1);
		this.exOutputSlot = new InvSlotOutput(this, "exOutputSlot", 1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.upgradeSlot.tick();
		this.wasserinputSlot.processIntoTank(this.waterTank, this.wasseroutputSlot);
		this.exInputSlot.processIntoTank(this.exTank, this.exOutputSlot);
		this.fertilizerSlot.organize();
		if (this.level.getGameTime() % 10L == 0L && this.energy.getEnergy() >= 31.0)
		{
			this.scan();
		}
	}

	public void scan()
	{
		this.scanX++;
		if (this.scanX > 4)
		{
			this.scanX = -4;
			this.scanZ++;
			if (this.scanZ > 4)
			{
				this.scanZ = -4;
				this.scanY++;
				if (this.scanY > 1)
				{
					this.scanY = -1;
				}
			}
		}

		this.energy.useEnergy(1.0);
		BlockPos scan = this.worldPosition.offset(this.scanX, this.scanY, this.scanZ);
		if (this.getLevel().getBlockEntity(scan) instanceof TileEntityCrop crop)
		{
			if (!this.fertilizerSlot.isEmpty() && this.fertilizerSlot.consume(1, true, false) != null && crop.applyFertilizer(false))
			{
				this.energy.useEnergy(10.0);
				this.fertilizerSlot.consume(1);
			}

			int amount;
			if (!this.waterTank.isEmpty() && (amount = crop.applyHydration(this.waterTank.getFluidAmount(), false)) > 0)
			{
				this.waterTank.drainMb(amount, false);
				this.energy.useEnergy(10.0);
			}

			if (!this.exTank.isEmpty() && (amount = crop.applyWeedEx(this.exTank.getFluidAmount(), false, false, false)) > 0)
			{
				this.exTank.drainMb(amount, false);
				this.energy.useEnergy(10.0);
			}
		} else if (!this.waterTank.isEmpty() && this.tryHydrateFarmland(scan))
		{
			this.energy.useEnergy(10.0);
		}
	}

	private boolean tryHydrateFarmland(BlockPos pos)
	{
		Level world = this.getLevel();
		BlockState state = world.getBlockState(pos);
		int hydration;
		if (state.getBlock() == Blocks.FARMLAND && (hydration = (Integer) state.getValue(FarmBlock.MOISTURE)) < 7)
		{
			int drainAmount = Math.min(this.waterTank.getFluidAmount(), 7 - hydration);
			assert drainAmount > 0;
			assert drainAmount <= 7;
			this.waterTank.drainMbUnchecked(drainAmount, false);
			world.setBlock(pos, (BlockState) state.setValue(FarmBlock.MOISTURE, hydration + drainAmount), 2);
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.FluidConsuming);
	}

	@Override
	public ContainerBase<TileEntityCropmatron> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerCropmatron(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerCropmatron(syncId, inventory, this);
	}

	public Ic2FluidTank getWaterTank()
	{
		return this.waterTank;
	}

	public Ic2FluidTank getExTank()
	{
		return this.exTank;
	}
}
