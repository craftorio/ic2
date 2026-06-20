package ic2.core.block.generator.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityConversionGenerator extends TileEntityInventory implements IHasGui, IEnergySource
{
	private static final NumberFormat FORMAT = new DecimalFormat("#.#");
	@GuiSynced
	private double lastProduction;
	@GuiSynced
	private double maxProduction;
	private double production;
	private boolean registeredToEnet;

	public TileEntityConversionGenerator(BlockEntityType<? extends TileEntityConversionGenerator> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.lastProduction = this.production;
		this.production = 0.0;
		this.setActive(this.maxProduction > 0.0);
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
		if (this.registeredToEnet && !this.level.isClientSide)
		{
			EnergyNet.instance.removeTile(this);
			this.registeredToEnet = false;
		}
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.registeredToEnet && !this.level.isClientSide)
		{
			EnergyNet.instance.addBlockEntityTile(this);
			this.registeredToEnet = true;
		}
	}

	public String getProduction()
	{
		return FORMAT.format(this.lastProduction);
	}

	public String getMaxProduction()
	{
		return FORMAT.format(this.maxProduction);
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

	protected abstract int getEnergyAvailable();

	protected abstract void drawEnergyAvailable(int var1);

	protected abstract double getMultiplier();

	@Override
	public double getOfferedEnergy()
	{
		return this.maxProduction = this.getEnergyAvailable() * this.getMultiplier();
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.production += amount;
		this.drawEnergyAvailable((int) Math.ceil(amount / this.getMultiplier()));
	}

	@Override
	public int getSourceTier()
	{
		return Math.max(EnergyNet.instance.getTierFromPower(this.maxProduction), 2);
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side)
	{
		return side != this.getFacing();
	}
}
