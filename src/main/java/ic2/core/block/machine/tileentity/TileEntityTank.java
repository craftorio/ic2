package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.util.FluidContainerOutputMode;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityTank extends TileEntityInventory implements IUpgradableBlock, IHasGui, INetworkClientTileEntityEventListener
{
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	@GuiSynced
	protected final Ic2FluidTank fluidTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityTank(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.TANK, pos, state);
		this.fluidTank = this.fluids.addTank("fluid", 24000);
		this.comparator.setUpdate(() -> this.fluidTank.getFluidAmount() == 0 ? 0 : (int) Util.lerp(1.0F, 15.0F, (float) this.fluidTank.getFluidAmount() / this.fluidTank.getCapacity()));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.upgradeSlot.tick();
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
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
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
	public void onNetworkEvent(Player player, int event)
	{
		if (event != 0) return;

		ItemStack carried = player.containerMenu.getCarried();
		if (StackUtil.isEmpty(carried) || !LiquidUtil.isFluidContainer(carried)) return;

		ItemStack single = StackUtil.copyWithSize(carried, 1);
		ItemStack remaining = StackUtil.getSize(carried) > 1 ? StackUtil.decSize(carried.copy()) : StackUtil.emptyStack;

		Ic2FluidStack tankFs = this.fluidTank.getFluidStack();

		// 先尝试从储罐向容器填充液体
		if (tankFs != null && !tankFs.isEmpty())
		{
			LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(single.copy(), tankFs.copy(), FluidContainerOutputMode.InPlacePreferred);
			if (result != null)
			{
				this.fluidTank.drainMb(result.fluidChange.getAmountMb(), false);
				player.containerMenu.setCarried(result.inPlaceOutput);
				if (!StackUtil.isEmpty(remaining) && !StackUtil.storeInventoryItem(remaining, player, false))
				{
					player.drop(remaining, false);
				}
				if (result.extraOutput != null && !StackUtil.storeInventoryItem(result.extraOutput, player, false))
				{
					player.drop(result.extraOutput, false);
				}
				player.containerMenu.broadcastChanges();
				return;
			}
		}

		// 再尝试从容器向储罐排入液体
		int space = this.fluidTank.getCapacity() - (tankFs != null ? tankFs.getAmountMb() : 0);
		if (space > 0)
		{
			LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(
				single.copy(),
				tankFs != null && !tankFs.isEmpty() ? tankFs.getFluid() : null,
				space,
				FluidContainerOutputMode.InPlacePreferred
			);
			if (result != null)
			{
				this.fluidTank.fillMb(result.fluidChange, false);
				player.containerMenu.setCarried(result.inPlaceOutput);
				if (!StackUtil.isEmpty(remaining) && !StackUtil.storeInventoryItem(remaining, player, false))
				{
					player.drop(remaining, false);
				}
				if (result.extraOutput != null && !StackUtil.storeInventoryItem(result.extraOutput, player, false))
				{
					player.drop(result.extraOutput, false);
				}
				player.containerMenu.broadcastChanges();
			}
		}
	}
}
