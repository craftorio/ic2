package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.util.StackUtil;

import java.util.Collection;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityStandardMachine<RI, RO, I>
	extends TileEntityElectricMachine
	implements IHasGui,
	IGuiValueProvider,
	INetworkTileEntityEventListener,
	IUpgradableBlock
{
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
	public AudioSource audioSource;
	protected static final int EventStart = 0;
	protected static final int EventInterrupt = 1;
	protected static final int EventFinish = 2;
	protected static final int EventStop = 3;
	public InvSlotProcessable<RI, RO, I> inputSlot;
	public final InvSlotOutput outputSlot;
	public final InvSlotUpgrade upgradeSlot;

	public TileEntityStandardMachine(int energyPerTick, int length, int outputSlots)
	{
		this(energyPerTick, length, outputSlots, 1);
	}

	public TileEntityStandardMachine(int energyPerTick, int length, int outputSlots, int aDefaultTier)
	{
		super(energyPerTick * length, aDefaultTier);
		this.defaultEnergyConsume = this.energyConsume = energyPerTick;
		this.defaultOperationLength = this.operationLength = length;
		this.defaultTier = aDefaultTier;
		this.defaultEnergyStorage = energyPerTick * length;
		this.outputSlot = new InvSlotOutput(this, "output", outputSlots);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.comparator.setUpdate(() -> this.progress * 15 / this.operationLength);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.progress = nbttagcompound.getShort("progress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setShort("progress", this.progress);
		return nbt;
	}

	public float getProgress()
	{
		return this.guiProgress;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (IC2.platform.isSimulating())
		{
			this.setOverclockRates();
		}
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
		if (IC2.platform.isRendering() && this.audioSource != null)
		{
			IC2.audioManager.removeSources(this);
			this.audioSource = null;
		}
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (IC2.platform.isSimulating())
		{
			this.setOverclockRates();
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		MachineRecipeResult<RI, RO, I> output = this.getOutput();
		if (output != null && this.energy.useEnergy(this.energyConsume))
		{
			this.setActive(true);
			if (this.progress == 0)
			{
				IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
			}

			this.progress++;
			if (this.progress >= this.operationLength)
			{
				this.operate(output);
				needsInvUpdate = true;
				this.progress = 0;
				IC2.network.get(true).initiateTileEntityEvent(this, 2, true);
			}
		} else
		{
			if (this.getActive())
			{
				if (this.progress != 0)
				{
					IC2.network.get(true).initiateTileEntityEvent(this, 1, true);
				} else
				{
					IC2.network.get(true).initiateTileEntityEvent(this, 3, true);
				}
			}

			if (output == null)
			{
				this.progress = 0;
			}

			this.setActive(false);
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		this.guiProgress = (float) this.progress / this.operationLength;
		if (needsInvUpdate)
		{
			super.markDirty();
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

	private void operate(MachineRecipeResult<RI, RO, I> result)
	{
		for (int i = 0; i < this.operationsPerTick; i++)
		{
			Collection<ItemStack> processResult = this.getOutput(result.getOutput());

			for (int j = 0; j < this.upgradeSlot.size(); j++)
			{
				ItemStack stack = this.upgradeSlot.get(j);
				if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IUpgradeItem)
				{
					processResult = ((IUpgradeItem) stack.getItem()).onProcessEnd(stack, this, processResult);
				}
			}

			this.operateOnce(result, processResult);
			result = this.getOutput();
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

	protected MachineRecipeResult<RI, RO, I> getOutput()
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
				return this.outputSlot.canAdd(this.getOutput(result.getOutput())) ? result : null;
			}
		}
	}

	@Override
	public ContainerBase<? extends TileEntityStandardMachine<RI, RO, I>> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.<TileEntityStandardMachine<RI, RO, I>>create(this, player, GuiParser.parse(this.teBlock));
	}

	public String getStartSoundFile()
	{
		return null;
	}

	public String getInterruptSoundFile()
	{
		return null;
	}

	@Override
	public void onNetworkEvent(int event)
	{
		if (this.audioSource == null && this.getStartSoundFile() != null)
		{
			this.audioSource = IC2.audioManager.createSource(this, this.getStartSoundFile());
		}

		switch (event)
		{
			case 0:
				if (this.audioSource != null)
				{
					this.audioSource.play();
				}
				break;
			case 1:
				if (this.audioSource != null)
				{
					this.audioSource.stop();
					if (this.getInterruptSoundFile() != null)
					{
						IC2.audioManager.playOnce(this, PositionSpec.Center, this.getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume());
					}
				}
				break;
			case 2:
				if (this.audioSource != null)
				{
					this.audioSource.stop();
				}
			case 3:
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
	public void onGuiClosed(EntityPlayer player)
	{
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
