package ic2.core.block.machine.tileentity;

import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;

public class TileEntityInduction extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider
{
	private static final short maxHeat = 10000;
	public final InvSlotProcessableSmelting inputSlotA;
	public final InvSlotProcessableSmelting inputSlotB;
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlotOutput outputSlotA;
	public final InvSlotOutput outputSlotB;
	protected final Redstone redstone;
	@GuiSynced
	public short heat = 0;
	@GuiSynced
	public short progress = 0;

	public TileEntityInduction(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.INDUCTION_FURNACE, pos, state, 10000, 2);
		this.inputSlotA = new InvSlotProcessableSmelting(this, "inputA", 1);
		this.inputSlotB = new InvSlotProcessableSmelting(this, "inputB", 1);
		this.outputSlotA = new InvSlotOutput(this, "outputA", 1);
		this.outputSlotB = new InvSlotOutput(this, "outputB", 1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
		this.redstone = this.addComponent(new Redstone(this));
		this.comparator.setUpdate(() -> this.heat * 15 / 10000);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.heat = nbt.getShort("heat");
		this.progress = nbt.getShort("progress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		nbt.putShort("heat", this.heat);
		nbt.putShort("progress", this.progress);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		boolean newActive = this.getActive();
		if (this.heat == 0)
		{
			newActive = false;
		}

		if (this.progress >= 4000)
		{
			this.operate();
			needsInvUpdate = true;
			this.progress = 0;
			newActive = false;
		}

		boolean canOperate = this.canOperate();
		if ((canOperate || this.redstone.hasRedstoneInput()) && this.energy.useEnergy(1.0))
		{
			if (this.heat < 10000)
			{
				this.heat++;
			}

			newActive = true;
		} else
		{
			this.heat = (short) (this.heat - Math.min(this.heat, 4));
		}

		if (newActive && this.progress != 0)
		{
			if (!canOperate || this.energy.getEnergy() < 15.0)
			{
				if (!canOperate)
				{
					this.progress = 0;
				}

				newActive = false;
				IC2.network.get(true).initiateTileEntityEvent(this, 1, true);
				this.shutdown(true);
			}
		} else if (canOperate)
		{
			if (this.energy.getEnergy() >= 15.0)
			{
				newActive = true;
				IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
				this.activate(false);
			}
		} else
		{
			if (needsInvUpdate)
			{
				IC2.network.get(true).initiateTileEntityEvent(this, 3, true);
			}

			this.progress = 0;
			this.shutdown(false);
		}

		if (newActive && canOperate)
		{
			this.progress = (short) (this.progress + this.heat / 30);
			this.energy.useEnergy(15.0);
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		if (needsInvUpdate)
		{
			this.setChanged();
		}
	}

	public String getHeat()
	{
		return this.heat * 100 / 10000 + "%";
	}

	public int gaugeProgressScaled(int i)
	{
		return i * this.progress / 4000;
	}

	public void operate()
	{
		this.operate(this.inputSlotA, this.outputSlotA);
		this.operate(this.inputSlotB, this.outputSlotB);
	}

	public void operate(InvSlotProcessableSmelting inputSlot, InvSlotOutput outputSlot)
	{
		if (this.canOperate(inputSlot, outputSlot))
		{
			MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = inputSlot.process();
			outputSlot.add(result.getOutput());
			inputSlot.consume(result);
		}
	}

	public boolean canOperate()
	{
		return this.canOperate(this.inputSlotA, this.outputSlotA) || this.canOperate(this.inputSlotB, this.outputSlotB);
	}

	public boolean canOperate(InvSlotProcessableSmelting inputSlot, InvSlotOutput outputSlot)
	{
		if (inputSlot.isEmpty())
		{
			return false;
		}

		MachineRecipeResult<? extends ItemStack, ? extends ItemStack, ? extends ItemStack> result = inputSlot.process();
		return result == null ? false : outputSlot.canAdd(result.getOutput());
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
		return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("progress".equals(name))
		{
			return this.gaugeProgressScaled(1000) / 1000.0;
		} else
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public SoundEvent getStartSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FURNACE_INDUCTION_START;
	}

	@Override
	public SoundEvent getStopSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FURNACE_INDUCTION_STOP;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FURNACE_INDUCTION_LOOP;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
	}
}
