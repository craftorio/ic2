package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLinked;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.event.WorldData;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.io.IOException;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TileEntityTradeOMat
	extends TileEntityInventory
	implements IPersonalBlock,
	IHasGui,
	INetworkTileEntityEventListener,
	INetworkClientTileEntityEventListener
{
	private int ticker;
	private GameProfile owner = null;
	public int totalTradeCount = 0;
	public int stock = 0;
	public boolean infinite = false;
	private static final int stockUpdateRate = 64;
	private static final int EventTrade = 0;
	public final InvSlot demandSlot;
	public final InvSlot offerSlot;
	public final InvSlotConsumableLinked inputSlot;
	public final InvSlotOutput outputSlot;

	public TileEntityTradeOMat(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.TRADE_O_MAT, pos, state);
		this.ticker = IC2.random.nextInt(64);
		this.demandSlot = new InvSlot(this, "demand", InvSlot.Access.NONE, 1);
		this.offerSlot = new InvSlot(this, "offer", InvSlot.Access.NONE, 1);
		this.inputSlot = new InvSlotConsumableLinked(this, "input", 1, this.demandSlot);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		if (nbt.contains("ownerGameProfile"))
		{
			this.owner = NbtUtils.readGameProfile(nbt.getCompound("ownerGameProfile"));
		}

		this.totalTradeCount = nbt.getInt("totalTradeCount");
		if (nbt.contains("infinite"))
		{
			this.infinite = nbt.getBoolean("infinite");
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

		nbt.putInt("totalTradeCount", this.totalTradeCount);
		if (this.infinite)
		{
			nbt.putBoolean("infinite", this.infinite);
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("owner");
		return ret;
	}

	public final boolean isWireless()
	{
		return this.getActive();
	}

	public final boolean setWireless(boolean wireless)
	{
		if (this.isWireless() == wireless)
		{
			return false;
		}

		if (wireless)
		{
			this.setActive(true);
			WorldData.get(this.level).tradeMarket.registerTradeOMat(this);
		} else
		{
			this.setActive(false);
			WorldData.get(this.level).tradeMarket.unregisterTradeOMat(this);
		}

		return true;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.trade();
		if (this.infinite)
		{
			this.stock = -1;
		} else if (++this.ticker % 64 == 0)
		{
			this.updateStock();
		}
	}

	private void trade()
	{
		if (this.level != null)
		{
			ItemStack tradedIn = this.inputSlot.consumeLinked(true);
			if (!StackUtil.isEmpty(tradedIn))
			{
				ItemStack offer = this.offerSlot.get();
				if (!StackUtil.isEmpty(offer))
				{
					if (this.outputSlot.canAdd(offer))
					{
						if (this.infinite)
						{
							this.inputSlot.consumeLinked(false);
							this.outputSlot.add(offer);
						} else
						{
							int amount = StackUtil.fetch(this, offer, true);
							if (amount != StackUtil.getSize(offer))
							{
								return;
							}

							int transferredOut = StackUtil.distribute(this, tradedIn, true);
							if (transferredOut != StackUtil.getSize(tradedIn))
							{
								return;
							}

							amount = StackUtil.fetch(this, offer, false);
							if (amount == 0)
							{
								return;
							}

							if (amount != StackUtil.getSize(offer))
							{
								IC2.log
									.warn(
										LogCategory.Block,
										"The Trade-O-Mat at %s received an inconsistent result from an adjacent trade supply inventory, the %s items will be lost.",
										Util.formatPosition(this),
										amount
									);
								return;
							}

							StackUtil.distribute(this, this.inputSlot.consumeLinked(false), false);
							this.outputSlot.add(offer);
							this.stock--;
						}

						this.totalTradeCount++;
						IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
						this.level
							.playLocalSound(
								this.worldPosition.getX(),
								this.worldPosition.getY(),
								this.worldPosition.getZ(),
								Ic2SoundEvents.MACHINE_OMAT_OPERATE,
								SoundSource.BLOCKS,
								1.0F,
								1.0F,
								true
							);
						this.setChanged();
					}
				}
			}
		}
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (this.level == null)
		{
			IC2.log.error(LogCategory.Block, "World object is null while trade-o-mat block entity at: \"" + this.worldPosition.toString() + "\" is loaded");
		} else
		{
			if (!this.level.isClientSide)
			{
				this.updateStock();
				if (this.isWireless())
				{
					WorldData.get(this.level).tradeMarket.registerTradeOMat(this);
				}
			}
		}
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (!this.isWireless() && StackUtil.consume(player, hand, StackUtil.sameItem(Ic2Items.REMOTE_INTERFACE_UPGRADE), 1))
		{
			if (this.level == null)
			{
				IC2.log.error(LogCategory.Block, "World object is null while trade-o-mat block entity at: \"" + this.worldPosition.toString() + "\" is activated");
			}

			if (!this.level.isClientSide)
			{
				this.setWireless(true);
			}

			return InteractionResult.CONSUME;
		} else
		{
			return super.onActivated(player, hand, side, hit);
		}
	}

	public void updateStock()
	{
		ItemStack offer = this.offerSlot.get();
		if (StackUtil.isEmpty(offer))
		{
			this.stock = 0;
		} else
		{
			this.stock = StackUtil.fetch(this, StackUtil.copyWithSize(offer, Integer.MAX_VALUE), true) / StackUtil.getSize(offer);
		}
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
		if (this.level == null)
		{
			IC2.log.error(LogCategory.Block, "World object is null while trade-o-mat block entity at: \"" + this.worldPosition.toString() + "\" is unloaded");
		}

		if (!this.level.isClientSide && this.isWireless())
		{
			WorldData.get(this.level).tradeMarket.unregisterTradeOMat(this);
		}
	}

	@Override
	public boolean wrenchCanRemove(Player player)
	{
		return this.permitsAccess(player.getGameProfile());
	}

	@Override
	protected List<ItemStack> getAuxDrops(int fortune)
	{
		List<ItemStack> drops = super.getAuxDrops(fortune);
		if (this.isWireless())
		{
			drops.add(new ItemStack(Ic2Items.REMOTE_INTERFACE_UPGRADE));
		}

		return drops;
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
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return this.permitsAccess(player.getGameProfile())
			? new ContainerTradeOMatOpen(syncId, player.getInventory(), this, this.canToggleInfinite(player))
			: new ContainerTradeOMatClosed(syncId, player.getInventory(), this);
	}

	@Override
	public void writeScreenOpenData(Player player, InteractionHand hand, GrowingBuffer buffer) throws IOException
	{
		boolean open = this.permitsAccess(player.getGameProfile());
		buffer.writeBoolean(open);
		if (open)
		{
			buffer.writeBoolean(this.canToggleInfinite(player));
		}
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return data.readBoolean()
			? new ContainerTradeOMatOpen(syncId, inventory, this, data.readBoolean())
			: new ContainerTradeOMatClosed(syncId, inventory, this);
	}

	@Override
	public void onScreenClosed(Player player)
	{
	}

	@Override
	public void onNetworkEvent(int event)
	{
		switch (event)
		{
			default:
				IC2.sideProxy
					.displayError(
						"An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID "
							+ event
							+ ", tile entity below)\nT: "
							+ this
							+ " ("
							+ this.worldPosition
							+ ")"
					);
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event == 0 && this.canToggleInfinite(player))
		{
			this.infinite = !this.infinite;
			if (!this.infinite)
			{
				this.updateStock();
			}
		}
	}

	private boolean canToggleInfinite(Player player)
	{
		MinecraftServer server = player.m_20194_();
		return server != null && server.getPlayerList().isOp(player.getGameProfile());
	}
}
