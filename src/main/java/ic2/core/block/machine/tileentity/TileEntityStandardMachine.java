package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.sound.Sound;
import ic2.core.util.StackUtil;

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityStandardMachine<RI, RO, I>
	extends TileEntityElectricMachine
	implements IHasGui,
	IGuiValueProvider,
	INetworkTileEntityEventListener,
	IUpgradableBlock
{
	protected Collection<ItemStack> processResult = null;
	protected MachineRecipeResult<RI, RO, I> recipeResult = null;
	protected short progress = 0;
	public final int defaultEnergyConsume;
	public final int defaultOperationLength;
	public final int defaultTier;
	public final int defaultEnergyStorage;
	public int energyConsume;
	public int operationLength;
	public int operationsPerTick;
	@GuiSynced
	protected float guiProgress;
	public Sound sound;
	protected static final int EventStart = 0;
	protected static final int EventInterrupt = 1;
	protected static final int EventFinish = 2;
	protected static final int EventStop = 3;
	public InvSlotProcessable<RI, RO, I> inputSlot;
	public final InvSlotOutput outputSlot;
	public final InvSlotUpgrade upgradeSlot;

	public TileEntityStandardMachine(
		BlockEntityType<? extends TileEntityStandardMachine<RI, RO, I>> type, BlockPos pos, BlockState state, int energyPerTick, int length, int outputSlots
	)
	{
		this(type, pos, state, energyPerTick, length, outputSlots, 1);
	}

	public TileEntityStandardMachine(
		BlockEntityType<? extends TileEntityStandardMachine<RI, RO, I>> type,
		BlockPos pos,
		BlockState state,
		int energyPerTick,
		int length,
		int outputSlots,
		int aDefaultTier
	)
	{
		super(type, pos, state, energyPerTick * length, aDefaultTier);
		this.defaultEnergyConsume = this.energyConsume = energyPerTick;
		this.defaultOperationLength = this.operationLength = length;
		this.defaultTier = aDefaultTier;
		this.defaultEnergyStorage = energyPerTick * length;
		this.outputSlot = new InvSlotOutput(this, "output", outputSlots);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.comparator.setUpdate(() -> this.progress * 15 / this.operationLength);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = nbt.getShort("progress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putShort("progress", this.progress);
	}

	public float getProgress()
	{
		return this.guiProgress;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (IC2.sideProxy.isSimulating())
		{
			this.setOverclockRates();
		}
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (IC2.sideProxy.isSimulating())
		{
			this.setOverclockRates();
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		MachineRecipeResult<RI, RO, I> result;
		if (this.inputSlot == null)
		{
			return;
		}
		if (this.recipeResult == null
			|| (result = this.inputSlot.process()) == null
			|| !isSameRecipeInput(result.getRecipe().getInput(), this.recipeResult.getRecipe().getInput()))
		{
			this.recipeResult = this.getRecipeResult();
			this.progress = 0;
		}

		if (this.canOperate())
		{
			if (!this.getActive())
			{
				this.activate(false);
			}

			this.progress++;
			if (this.progress >= this.operationLength)
			{
				this.operate();
				needsInvUpdate = true;
				this.progress = 0;
				if (!this.canOperate())
				{
					this.shutdown(false);
				}
			}
		} else
		{
			if (this.getActive())
			{
				this.shutdown(this.progress != 0);
			}

			if (this.recipeResult == null)
			{
				this.progress = 0;
			}
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		this.guiProgress = (float) this.progress / this.operationLength;
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	public void setOverclockRates()
	{
		this.upgradeSlot.onChanged();
		double previousProgress = (double) this.progress / this.operationLength;
		this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
		this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
		this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
		int tier = this.upgradeSlot.getTier(this.defaultTier);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
		this.progress = (short) Math.floor(previousProgress * this.operationLength + 0.1);
	}

	private static <RI> boolean isSameRecipeInput(RI a, RI b)
	{
		if (a == b)
		{
			return true;
		}

		if (a == null || b == null)
		{
			return false;
		}

		if (a instanceof ItemStack aStack && b instanceof ItemStack bStack)
		{
			return StackUtil.checkItemEqualityStrict(aStack, bStack);
		}

		return a.equals(b);
	}

	private boolean canOperate()
	{
		return this.recipeResult != null && this.energy.useEnergy(this.energyConsume);
	}

	private void operate()
	{
		MachineRecipeResult<RI, RO, I> result = this.inputSlot.process();

		for (int i = 0; i < this.operationsPerTick; i++)
		{
			Collection<ItemStack> processRet = this.processResult == null ? this.getOutput(result.getOutput()) : this.processResult;

			for (int j = 0; j < this.upgradeSlot.size(); j++)
			{
				ItemStack stack = this.upgradeSlot.get(j);
				if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IUpgradeItem)
				{
					processRet = ((IUpgradeItem) stack.getItem()).onProcessEnd(stack, this, processRet);
				}
			}

			this.operateOnce(result, processRet);
			result = this.recipeResult = this.getRecipeResult();
			if (result == null)
			{
				break;
			}
		}
	}

	protected Collection<ItemStack> getOutput(RO output)
	{
		return StackUtil.copy((Collection<ItemStack>) output);
	}

	protected void operateOnce(MachineRecipeResult<RI, RO, I> result, Collection<ItemStack> processResult)
	{
		this.inputSlot.consume(result);
		this.outputSlot.add(processResult);
	}

	protected MachineRecipeResult<RI, RO, I> getRecipeResult()
	{
		if (this.inputSlot.isEmpty())
		{
			return null;
		} else
		{
			MachineRecipeResult<RI, RO, I> result = this.inputSlot.process();
			if (result == null)
			{
				return null;
			} else
			{
				Collection<ItemStack> processRet = this.getOutput(result.getOutput());
				if (this.outputSlot.canAdd(processRet))
				{
					this.processResult = processRet;
					return result;
				} else
				{
					return null;
				}
			}
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	public void onNetworkEvent(int event)
	{
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
	public double getGuiValue(String name)
	{
		if (name.equals("progress"))
		{
			return this.guiProgress;
		} else
		{
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
		}
	}
}
