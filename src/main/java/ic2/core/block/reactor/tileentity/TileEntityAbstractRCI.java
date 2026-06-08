package ic2.core.block.reactor.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.item.reactor.ItemReactorCondensator;
import ic2.core.network.GrowingBuffer;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityAbstractRCI extends TileEntityElectricMachine implements IUpgradableBlock, IHasGui
{
	private TileEntityNuclearReactorElectric reactor;
	private final ItemStack target;
	private final double energyPerOperation = 1000.0;
	public final InvSlotConsumableItemStack inputSlot;
	public final InvSlotUpgrade upgradeSlot;

	protected TileEntityAbstractRCI(BlockEntityType<? extends TileEntityAbstractRCI> type, BlockPos pos, BlockState state, ItemStack target, ItemStack coolant)
	{
		super(type, pos, state, 48000, 2);
		this.target = target;
		this.inputSlot = new InvSlotConsumableItemStack(this, "input", InvSlot.Access.I, 9, InvSlot.InvSide.ANY, coolant);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			this.updateEnergyFacings();
		}

		this.updateReactor();
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (!this.inputSlot.isEmpty() && this.energy.getEnergy() >= 1000.0 && this.reactor != null)
		{
			this.setActive(true);
		} else
		{
			this.setActive(false);
		}

		if (this.getActive())
		{
			for (ItemStack comp : this.reactor.reactorSlot)
			{
				if (comp != null && StackUtil.checkItemEquality(comp, this.target))
				{
					ItemReactorCondensator cond = (ItemReactorCondensator) comp.getItem();
					if (cond.getUseFraction(comp) > 0.85 && this.inputSlot.consume(1) != null && this.energy.useEnergy(1000.0))
					{
						cond.setUse(comp, 0);
						needsInvUpdate = true;
					}
				}
			}
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		this.updateEnergyFacings();
		this.updateReactor();
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.updateEnergyFacings();
		this.updateReactor();
	}

	public void updateEnergyFacings()
	{
		Level world = this.getLevel();
		Set<Direction> ret = new HashSet<>();

		for (Direction facing : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(facing));
			if (!(te instanceof TileEntityNuclearReactorElectric) && !(te instanceof TileEntityReactorChamberElectric))
			{
				ret.add(facing);
			}
		}

		this.energy.setDirections(ret, Collections.emptySet());
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemConsuming);
	}

	@Override
	public double getEnergy()
	{
		return 0.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return false;
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

	private void updateReactor()
	{
		Level world = this.getLevel();
		if (!Util.isAreaLoaded(world, this.worldPosition, 2))
		{
			this.reactor = null;
		} else
		{
			BlockEntity tileEntity = world.getBlockEntity(this.worldPosition.relative(this.getFacing().getOpposite()));
			if (tileEntity instanceof TileEntityNuclearReactorElectric)
			{
				this.reactor = (TileEntityNuclearReactorElectric) tileEntity;
			} else if (tileEntity instanceof TileEntityReactorChamberElectric)
			{
				this.reactor = ((TileEntityReactorChamberElectric) tileEntity).getReactorInstance();
			} else
			{
				this.reactor = null;
			}
		}
	}
}
