package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.IEmptyFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotConsumableCanner;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotProcessableCanner;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.LiquidUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCanner extends TileEntityStandardMachine<Object, Object, Object> implements INetworkClientTileEntityEventListener
{
	private TileEntityCanner.Mode mode = TileEntityCanner.Mode.BottleSolid;
	public static final int eventSwapTanks = Mode.values.length + 1;
	public final Ic2FluidTank inputTank;
	public final Ic2FluidTank outputTank;
	public final InvSlotConsumableCanner canInputSlot;
	protected final Fluids fluids;

	public TileEntityCanner(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CANNER, pos, state, 4, 200, 1);
		this.inputSlot = new InvSlotProcessableCanner(this, "input", 1);
		this.canInputSlot = new InvSlotConsumableCanner(this, "canInput", 1);
		this.fluids = this.addComponent(new Fluids(this));
		this.inputTank = this.fluids.addTankInsert("inputTank", 8000);
		this.outputTank = this.fluids.addTankExtract("outputTank", 8000);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.setMode(TileEntityCanner.Mode.values[nbt.getInt("mode")]);
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("mode", this.mode.ordinal());
	}

	@Override
	public void operateOnce(MachineRecipeResult<Object, Object, Object> result, Collection<ItemStack> processResult)
	{
		super.operateOnce(result, processResult);
		if (this.mode == TileEntityCanner.Mode.EmptyLiquid)
		{
			IEmptyFluidContainerRecipeManager.Output output = (IEmptyFluidContainerRecipeManager.Output) result.getOutput();
			this.getOutputTank().fillMbUnchecked(output.fluid, false);
		} else if (this.mode == TileEntityCanner.Mode.EnrichLiquid)
		{
			Ic2FluidStack output = ((Ic2FluidStack) result.getOutput()).copy();
			LiquidUtil.FluidOperationResult outcome;
			if (!this.canInputSlot.isEmpty())
			{
				do
				{
					outcome = LiquidUtil.fillContainer(this.canInputSlot.get(), output, FluidContainerOutputMode.EmptyFullToOutput);
					if (outcome != null)
					{
						if (outcome.extraOutput != null && !this.outputSlot.canAdd(outcome.extraOutput))
						{
							outcome = null;
						} else
						{
							this.canInputSlot.put(outcome.inPlaceOutput);
							if (outcome.extraOutput != null)
							{
								this.outputSlot.add(outcome.extraOutput);
							}

							output.setAmountMb(output.getAmountMb() - outcome.fluidChange.getAmountMb());
						}
					}
				} while (outcome != null && !output.isEmpty());
			}

			this.getOutputTank().fillMbUnchecked(output, false);
		}
	}

	@Override
	protected Collection<ItemStack> getOutput(Object output)
	{
		if (output instanceof ItemStack)
		{
			return Collections.singletonList((ItemStack) output);
		} else if (output instanceof Ic2FluidStack)
		{
			return Collections.emptyList();
		} else
		{
			return output instanceof IEmptyFluidContainerRecipeManager.Output ? ((IEmptyFluidContainerRecipeManager.Output) output).container : super.getOutput(output);
		}
	}

	@Override
	public MachineRecipeResult<Object, Object, Object> getRecipeResult()
	{
		if (this.mode != TileEntityCanner.Mode.EmptyLiquid && this.mode != TileEntityCanner.Mode.BottleLiquid)
		{
			if (this.inputSlot.isEmpty())
			{
				return null;
			}
		} else if (this.canInputSlot.isEmpty())
		{
			return null;
		}

		MachineRecipeResult<Object, Object, Object> result = this.inputSlot.process();
		if (result == null)
		{
			return null;
		}

		if (!this.outputSlot.canAdd(this.getOutput(result.getOutput())))
		{
			return null;
		}

		if (this.mode == TileEntityCanner.Mode.EmptyLiquid)
		{
			IEmptyFluidContainerRecipeManager.Output output = (IEmptyFluidContainerRecipeManager.Output) result.getOutput();
			if (this.getOutputTank().fillMbUnchecked(output.fluid, true) != output.fluid.getAmountMb())
			{
				return null;
			}
		} else if (this.mode == TileEntityCanner.Mode.EnrichLiquid)
		{
			Ic2FluidStack output = ((Ic2FluidStack) result.getOutput()).copy();
			LiquidUtil.FluidOperationResult outcome;
			if (!this.canInputSlot.isEmpty())
			{
				do
				{
					outcome = LiquidUtil.fillContainer(this.canInputSlot.get(), output, FluidContainerOutputMode.EmptyFullToOutput);
					if (outcome != null)
					{
						if (outcome.extraOutput != null && !this.outputSlot.canAdd(outcome.extraOutput))
						{
							outcome = null;
						} else
						{
							output.setAmountMb(output.getAmountMb() - outcome.fluidChange.getAmountMb());
						}
					}
				} while (outcome != null && !output.isEmpty());
			}

			if (this.getOutputTank().fillMbUnchecked(output, true) != output.getAmountMb())
			{
				return null;
			}
		}

		return result;
	}

	public Ic2FluidTank getInputTank()
	{
		return this.inputTank;
	}

	public Ic2FluidTank getOutputTank()
	{
		return this.outputTank;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("canInputSlot");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return switch (this.mode)
		{
			case BottleSolid, BottleLiquid -> Ic2SoundEvents.MACHINE_CANNER_OPERATE;
			case EmptyLiquid -> Ic2SoundEvents.MACHINE_CANNER_REVERSE;
			default -> null;
		};
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return switch (this.mode)
		{
			case BottleSolid, BottleLiquid, EmptyLiquid -> Ic2SoundEvents.MACHINE_INTERRUPT1;
			default -> null;
		};
	}

	@Override
	public ContainerBase<TileEntityCanner> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerCanner(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerCanner(syncId, inventory, this);
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if (field.equals("mode"))
		{
			this.setMode(this.mode);
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event >= 0 && event < Mode.values.length)
		{
			this.setMode(TileEntityCanner.Mode.values[event]);
		} else if (event == eventSwapTanks)
		{
			this.switchTanks();
		}
	}

	public TileEntityCanner.Mode getMode()
	{
		return this.mode;
	}

	public void setMode(TileEntityCanner.Mode mode)
	{
		this.mode = mode;
		switch (mode)
		{
			case BottleSolid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.None);
				break;
			case BottleLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Fill);
				break;
			case EmptyLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Drain);
				break;
			case EnrichLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Both);
		}

		if (IC2.sideProxy.isRendering())
		{
			this.stopLoopingSound();
		}
	}

	private void switchTanks()
	{
		if (this.progress != 0)
		{
			return;
		}

		Ic2FluidStack inputStack = this.inputTank.getFluidStack();
		Ic2FluidStack outputStack = this.outputTank.getFluidStack();
		this.inputTank.setFluidStack(outputStack);
		this.outputTank.setFluidStack(inputStack);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
	}

	public enum Mode
	{
		BottleSolid, EmptyLiquid, BottleLiquid, EnrichLiquid;

		public static final TileEntityCanner.Mode[] values = values();
	}
}
