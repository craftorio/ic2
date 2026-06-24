package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotConsumableLinked;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.StackUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityEnergyOMat
	extends TileEntityInventory
	implements IPersonalBlock,
	IHasGui,
	IEnergySink,
	IEnergySource,
	INetworkClientTileEntityEventListener,
	IUpgradableBlock
{
	public final InvSlot demandSlot = new InvSlot(this, "demand", InvSlot.Access.NONE, 1);
	public final InvSlotConsumableLinked inputSlot = new InvSlotConsumableLinked(this, "input", 1, this.demandSlot);
	public final InvSlotCharge chargeSlot = new InvSlotCharge(this, 1);
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
	public int euOffer = 1000;
	public int paidFor;
	public double euBuffer;
	private GameProfile owner = null;
	private boolean addedToEnergyNet = false;
	private int euBufferMax = 10000;
	private int tier = 1;

	public TileEntityEnergyOMat(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.ENERGY_O_MAT, pos, state);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		if (nbt.contains("ownerGameProfile"))
		{
			this.owner = NbtUtils.readGameProfile(nbt.getCompound("ownerGameProfile"));
		}

		this.euOffer = nbt.getInt("euOffer");
		this.paidFor = nbt.getInt("paidFor");

		try
		{
			this.euBuffer = nbt.getDouble("euBuffer");
		} catch (Exception e)
		{
			this.euBuffer = nbt.getInt("euBuffer");
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if (this.owner != null)
		{
			CompoundTag ownerNbt = new CompoundTag();
			NbtUtils.writeGameProfile(ownerNbt, this.owner);
			nbt.put("ownerGameProfile", ownerNbt);
		}

		nbt.putInt("euOffer", this.euOffer);
		nbt.putInt("paidFor", this.paidFor);
		nbt.putDouble("euBuffer", this.euBuffer);
	}

	@Override
	public boolean wrenchCanRemove(Player player)
	{
		return this.permitsAccess(player.getGameProfile());
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			EnergyNet.instance.addBlockEntityTile(this);
			this.addedToEnergyNet = true;
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (IC2.sideProxy.isSimulating() && this.addedToEnergyNet)
		{
			EnergyNet.instance.removeTile(this);
			this.addedToEnergyNet = false;
		}

		super.onUnloaded();
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean invChanged = false;
		this.euBufferMax = 10000;
		this.tier = 1;
		this.chargeSlot.setTier(1);
		if (!this.upgradeSlot.isEmpty())
		{
			this.euBufferMax = this.upgradeSlot.getEnergyStorage(10000, 0, 0);
			this.tier = 1 + this.upgradeSlot.extraTier;
			this.chargeSlot.setTier(this.tier);
		}

		ItemStack tradedIn = this.inputSlot.consumeLinked(true);
		if (tradedIn != null)
		{
			int transferred = StackUtil.distribute(this, tradedIn, true);
			if (transferred == StackUtil.getSize(tradedIn))
			{
				StackUtil.distribute(this, this.inputSlot.consumeLinked(false), false);
				this.paidFor = this.paidFor + this.euOffer;
				invChanged = true;
			}
		}

		if (this.euBuffer >= 1.0)
		{
			double sent = this.chargeSlot.charge(this.euBuffer);
			if (sent > 0.0)
			{
				this.euBuffer -= sent;
				invChanged = true;
			}
		}

		if (invChanged)
		{
			this.setChanged();
		}
	}

	@Override
	public boolean permitsAccess(GameProfile profile)
	{
		return TileEntityPersonalChest.checkAccess(this, profile);
	}

	@Override
	public Container getPrivilegedInventory(GameProfile accessor)
	{
		return this;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("owner");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public GameProfile getOwner()
	{
		return this.owner;
	}

	@Override
	public void setOwner(GameProfile owner)
	{
		this.owner = owner;
	}

	@Override
	protected boolean canEntityDestroy(Entity entity)
	{
		return false;
	}

	@Override
	protected boolean canSetFacingWrench(Direction facing, Player player)
	{
		return player != null && this.permitsAccess(player.getGameProfile()) ? super.canSetFacingWrench(facing, player) : false;
	}

	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction direction)
	{
		return !this.facingMatchesDirection(direction);
	}

	public boolean facingMatchesDirection(Direction direction)
	{
		return direction == this.getFacing();
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction direction)
	{
		return this.facingMatchesDirection(direction);
	}

	@Override
	public double getOfferedEnergy()
	{
		return this.euBuffer;
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.euBuffer -= amount;
	}

	@Override
	public double getDemandedEnergy()
	{
		return Math.min(this.paidFor, this.euBufferMax - this.euBuffer);
	}

	@Override
	public double injectEnergy(Direction directionFrom, double amount, double voltage)
	{
		double toAdd = Math.min(Math.min(amount, this.paidFor), this.euBufferMax - this.euBuffer);
		this.paidFor = (int) (this.paidFor - toAdd);
		this.euBuffer += toAdd;
		return amount - toAdd;
	}

	@Override
	public int getSourceTier()
	{
		return this.tier;
	}

	@Override
	public int getSinkTier()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return this.permitsAccess(player.getGameProfile())
			? new ContainerEnergyOMatOpen(syncId, player.getInventory(), this)
			: new ContainerEnergyOMatClosed(syncId, player.getInventory(), this);
	}

	@Override
	public void writeScreenOpenData(Player player, InteractionHand hand, GrowingBuffer buffer)
	{
		buffer.writeBoolean(this.permitsAccess(player.getGameProfile()));
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return data.readBoolean() ? new ContainerEnergyOMatOpen(syncId, inventory, this) : new ContainerEnergyOMatClosed(syncId, inventory, this);
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (this.permitsAccess(player.getGameProfile()))
		{
			switch (event)
			{
				case 0:
					this.attemptSet(-100000);
					break;
				case 1:
					this.attemptSet(-10000);
					break;
				case 2:
					this.attemptSet(-1000);
					break;
				case 3:
					this.attemptSet(-100);
					break;
				case 4:
					this.attemptSet(100000);
					break;
				case 5:
					this.attemptSet(10000);
					break;
				case 6:
					this.attemptSet(1000);
					break;
				case 7:
					this.attemptSet(100);
			}
		}
	}

	private void attemptSet(int amount)
	{
		this.euOffer += amount;
		if (this.euOffer < 100)
		{
			this.euOffer = 100;
		}
	}

	@Override
	public double getEnergy()
	{
		return this.euBuffer;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		if (amount <= this.euBuffer)
		{
			amount -= this.euBuffer;
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.Transformer);
	}
}
