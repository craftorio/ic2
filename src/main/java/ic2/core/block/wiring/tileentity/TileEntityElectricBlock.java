package ic2.core.block.wiring.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.tile.IEnergyStorage;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.block.wiring.ContainerElectricBlock;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;

import java.util.EnumSet;
import java.util.List;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TileEntityElectricBlock extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, IEnergyStorage
{
	protected double output;
	public byte redstoneMode = 0;
	public static byte redstoneModes = 7;
	public final InvSlotCharge chargeSlot;
	public final InvSlotDischarge dischargeSlot;
	public final Energy energy;
	public final Redstone redstone;
	public final RedstoneEmitter rsEmitter;

	public TileEntityElectricBlock(BlockEntityType<? extends TileEntityElectricBlock> type, BlockPos pos, BlockState state, int tier, int output, int maxStorage)
	{
		super(type, pos, state);
		this.output = output;
		this.chargeSlot = new InvSlotCharge(this, tier);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.IO, tier, InvSlot.InvSide.BOTTOM);
		this.energy = this.addComponent(
			new Energy(this, maxStorage, EnumSet.complementOf(EnumSet.of(Direction.DOWN)), EnumSet.of(Direction.DOWN), tier, tier, true)
				.addManagedSlot(this.chargeSlot)
				.addManagedSlot(this.dischargeSlot)
		);
		this.rsEmitter = this.addComponent(new RedstoneEmitter(this));
		this.redstone = this.addComponent(new Redstone(this));
		this.comparator.setUpdate(this.energy::getComparatorValue);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.redstoneMode = nbt.getByte("redstoneMode");
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putByte("redstoneMode", this.redstoneMode);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.energy.setSendingEnabled(this.shouldEmitEnergy());
		this.rsEmitter.setLevel(this.shouldEmitRedstone() ? 15 : 0);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerElectricBlock(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerElectricBlock(syncId, inventory, this);
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
	}

	protected final void superSetFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
	}

	protected boolean shouldEmitRedstone()
	{
		switch (this.redstoneMode)
		{
			case 1:
				return this.energy.getEnergy() >= this.energy.getCapacity() - this.output * 20.0;
			case 2:
				return this.energy.getEnergy() > this.output && this.energy.getEnergy() < this.energy.getCapacity() - this.output;
			case 3:
				return this.energy.getEnergy() < this.energy.getCapacity() - this.output;
			case 4:
				return this.energy.getEnergy() < this.output;
			default:
				return false;
		}
	}

	protected boolean shouldEmitEnergy()
	{
		boolean redstone = this.redstone.hasRedstoneInput();
		if (this.redstoneMode == 5)
		{
			return !redstone;
		} else
		{
			return this.redstoneMode != 6 ? true : !redstone || this.energy.getEnergy() > this.energy.getCapacity() - this.output * 20.0;
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		this.redstoneMode++;
		if (this.redstoneMode >= redstoneModes)
		{
			this.redstoneMode = 0;
		}

		IC2.sideProxy.messagePlayer(player, this.getRedstoneMode());
	}

	public String getRedstoneMode()
	{
		return this.redstoneMode < redstoneModes && this.redstoneMode >= 0 ? Localization.translate("ic2.EUStorage.gui.mod.redstone" + this.redstoneMode) : "";
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.getLevel().isClientSide)
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			this.energy.addEnergy(nbt.getDouble("energy"));
		}
	}

	public void onUpgraded()
	{
		this.rerender();
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		if (wrench || this.teBlock.getDefaultDrop() == Ic2TileEntityBlock.DefaultDrop.Self)
		{
			double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
			double totalEnergy = this.energy.getEnergy();
			if (retainedRatio > 0.0 && totalEnergy > 0.0)
			{
				CompoundTag nbt = StackUtil.getOrCreateNbtData(drop);
				nbt.putDouble("energy", totalEnergy * retainedRatio);
			}
		}

		return drop;
	}

	@Override
	public int getOutput()
	{
		return (int) this.output;
	}

	@Override
	public double getOutputEnergyUnitsPerTick()
	{
		return this.output;
	}

	@Override
	public void setStored(int energy)
	{
	}

	@Override
	public int addEnergy(int amount)
	{
		this.energy.addEnergy(amount);
		return amount;
	}

	@Override
	public int getStored()
	{
		return (int) this.energy.getEnergy();
	}

	@Override
	public int getCapacity()
	{
		return (int) this.energy.getCapacity();
	}

	@Override
	public boolean isTeleporterCompatible(Direction side)
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag advanced)
	{
		super.addInformation(stack, tooltip, advanced);
		tooltip.add(
			String.format(
				"%s %.0f %s %s %d %s",
				Localization.translate("ic2.item.tooltip.Output"),
				EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier()),
				Localization.translate("ic2.generic.text.EUt"),
				Localization.translate("ic2.item.tooltip.Capacity"),
				this.getCapacity(),
				Localization.translate("ic2.generic.text.EU")
			)
		);
		tooltip.add(
			Localization.translate("ic2.item.tooltip.Store")
				+ " "
				+ (long) StackUtil.getOrCreateNbtData(stack).getDouble("energy")
				+ " "
				+ Localization.translate("ic2.generic.text.EU")
		);
	}
}
