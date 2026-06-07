package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerItemBuffer;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.StackUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityItemBuffer extends TileEntityInventory implements IHasGui, IUpgradableBlock
{
	public final InvSlot rightcontentSlot;
	public final InvSlot leftcontentSlot;
	public final InvSlotUpgrade upgradeSlot;
	private boolean tick = true;

	public TileEntityItemBuffer(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.ITEM_BUFFER, pos, state);
		this.rightcontentSlot = new InvSlot(this, "rightcontent", InvSlot.Access.IO, 24, InvSlot.InvSide.SIDE);
		this.leftcontentSlot = new InvSlot(this, "leftcontent", InvSlot.Access.IO, 24, InvSlot.InvSide.NOTSIDE);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
		this.comparator.setUpdate(() -> calcRedstoneFromInvSlots(this.rightcontentSlot, this.leftcontentSlot));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		ItemStack upgradeleft = this.upgradeSlot.get(0);
		ItemStack upgraderight = this.upgradeSlot.get(1);
		if (!StackUtil.isEmpty(upgradeleft) && !StackUtil.isEmpty(upgraderight))
		{
			if (this.tick)
			{
				if (((IUpgradeItem) upgradeleft.getItem()).onTick(upgradeleft, this))
				{
					super.setChanged();
				}
			} else if (((IUpgradeItem) upgraderight.getItem()).onTick(upgraderight, this))
			{
				super.setChanged();
			}

			this.tick = !this.tick;
		} else
		{
			if (!StackUtil.isEmpty(upgradeleft))
			{
				this.tick = true;
				if (((IUpgradeItem) upgradeleft.getItem()).onTick(upgradeleft, this))
				{
					super.setChanged();
				}
			}

			if (!StackUtil.isEmpty(upgraderight))
			{
				this.tick = false;
				if (((IUpgradeItem) upgraderight.getItem()).onTick(upgraderight, this))
				{
					super.setChanged();
				}
			}
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerItemBuffer(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerItemBuffer(syncId, inventory, this);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemProducing);
	}

	@Override
	public double getEnergy()
	{
		return 40.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return true;
	}
}
