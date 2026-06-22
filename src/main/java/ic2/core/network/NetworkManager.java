package ic2.core.network;

import ic2.api.network.ClientModifiable;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkItemEventListener;
import ic2.api.network.INetworkManager;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Explosion;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.event.WorldData;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class NetworkManager implements INetworkManager
{
	public static final ResourceLocation channelId = IC2.getIdentifier("m");

	private static TeUpdateDataServer getTeUpdateData(BlockEntity te)
	{
		assert IC2.sideProxy.isSimulating();
		if (te == null)
		{
			throw new NullPointerException();
		}

		WorldData worldData = WorldData.get(te.getLevel());
		TeUpdateDataServer ret = worldData.tesToUpdate.get(te);
		if (ret == null)
		{
			ret = new TeUpdateDataServer();
			worldData.tesToUpdate.put(te, ret);
		}

		return ret;
	}

	static <T extends Collection<ServerPlayer>> T getPlayersInRange(Level world, BlockPos pos, T result)
	{
		if (!(world instanceof ServerLevel))
		{
			return result;
		}

		result.addAll(((ServerLevel) world).getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false));
		return result;
	}

	static void writeFieldData(Object object, String fieldName, GrowingBuffer out) throws IOException
	{
		int pos = fieldName.indexOf(61);
		if (pos != -1)
		{
			out.writeString(fieldName.substring(0, pos));
			DataEncoder.encode(out, fieldName.substring(pos + 1));
		} else
		{
			out.writeString(fieldName);

			try
			{
				DataEncoder.encode(out, ReflectionUtil.getValueRecursive(object, fieldName));
			} catch (NoSuchFieldException e)
			{
				throw new RuntimeException("Can't find field " + fieldName + " in " + object.getClass().getName(), e);
			}
		}
	}

	protected static ByteBuf makePacket(GrowingBuffer buffer, boolean advancePos)
	{
		return buffer.toByteBuf(advancePos);
	}

	protected boolean isClient()
	{
		return false;
	}

	public void onTickEnd(WorldData worldData)
	{
		try
		{
			TeUpdate.send(worldData, this);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public final void sendPlayerItemData(Player player, int slot, Object... data)
	{
		GrowingBuffer buffer = new GrowingBuffer(256);

		try
		{
			SubPacketType.PlayerItemData.writeTo(buffer);
			buffer.writeByte(slot);
			DataEncoder.encode(buffer, player.getInventory().items.get(slot).getItem(), false);
			buffer.writeVarInt(data.length);

			for (Object o : data)
			{
				DataEncoder.encode(buffer, o);
			}
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		buffer.flip();
		if (!this.isClient())
		{
			this.sendS2CPacket((ServerPlayer) player, buffer, true);
		} else
		{
			this.sendC2SPacket(buffer);
		}
	}

	@Override
	public final void updateTileEntityField(BlockEntity te, String field)
	{
		if (!this.isClient())
		{
			getTeUpdateData(te).addGlobalField(field);
		} else if (this.getClientModifiableField(te.getClass(), field) == null)
		{
			IC2.log.warn(LogCategory.Network, "Field update for %s failed.", te);
		} else
		{
			GrowingBuffer buffer = new GrowingBuffer(64);

			try
			{
				SubPacketType.TileEntityData.writeTo(buffer);
				DataEncoder.encode(buffer, te, false);
				writeFieldData(te, field, buffer);
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}

			buffer.flip();
			this.sendC2SPacket(buffer);
		}
	}

	private Field getClientModifiableField(Class<?> cls, String fieldName)
	{
		Field field = ReflectionUtil.getFieldRecursive(cls, fieldName);
		if (field == null)
		{
			IC2.log.warn(LogCategory.Network, "Can't find field %s in %s.", fieldName, cls.getName());
			return null;
		} else if (field.getAnnotation(ClientModifiable.class) == null)
		{
			IC2.log.warn(LogCategory.Network, "The field %s in %s is not modifiable.", fieldName, cls.getName());
			return null;
		} else
		{
			return field;
		}
	}

	public final void updateTileEntityFieldTo(BlockEntity te, String field, ServerPlayer player)
	{
		assert !this.isClient();
		getTeUpdateData(te).addPlayerField(field, player);
	}

	public final void sendComponentUpdate(Ic2TileEntity te, String componentName, ServerPlayer player, GrowingBuffer data)
	{
		assert !this.isClient();
		if (player.getCommandSenderWorld() != te.getLevel())
		{
			throw new IllegalArgumentException("mismatched world (te " + te.getLevel() + ", player " + player.getCommandSenderWorld() + ")");
		}

		GrowingBuffer buffer = new GrowingBuffer(64);

		try
		{
			SubPacketType.TileEntityBlockComponent.writeTo(buffer);
			DataEncoder.encode(buffer, te, false);
			buffer.writeString(componentName);
			buffer.writeVarInt(data.available());
			data.writeTo(buffer);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		buffer.flip();
		this.sendS2CPacket(player, buffer, true);
	}

	@Override
	public final void initiateTileEntityEvent(BlockEntity te, int event, boolean limitRange)
	{
		assert !this.isClient();
		if (!te.getLevel().players().isEmpty())
		{
			GrowingBuffer buffer = new GrowingBuffer(32);

			try
			{
				SubPacketType.TileEntityEvent.writeTo(buffer);
				DataEncoder.encode(buffer, te, false);
				buffer.writeInt(event);
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}

			buffer.flip();

			for (ServerPlayer target : getPlayersInRange(te.getLevel(), te.getBlockPos(), new ArrayList<>()))
			{
				if (limitRange)
				{
					int dX = (int) (te.getBlockPos().getX() + 0.5 - target.getX());
					int dZ = (int) (te.getBlockPos().getZ() + 0.5 - target.getZ());
					if (dX * dX + dZ * dZ > 400)
					{
						continue;
					}
				}

				this.sendS2CPacket(target, buffer, false);
			}
		}
	}

	@Override
	public final void initiateItemEvent(Player player, ItemStack stack, int event, boolean limitRange)
	{
		if (StackUtil.isEmpty(stack))
		{
			throw new NullPointerException("invalid stack: " + StackUtil.toStringSafe(stack));
		}

		assert !this.isClient();
		GrowingBuffer buffer = new GrowingBuffer(256);

		try
		{
			SubPacketType.ItemEvent.writeTo(buffer);
			DataEncoder.encode(buffer, player.getGameProfile(), false);
			DataEncoder.encode(buffer, stack, false);
			buffer.writeInt(event);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		buffer.flip();

		for (ServerPlayer target : getPlayersInRange(player.getCommandSenderWorld(), player.blockPosition(), new ArrayList<>()))
		{
			if (limitRange)
			{
				int dX = (int) (player.getX() - target.getX());
				int dZ = (int) (player.getZ() - target.getZ());
				if (dX * dX + dZ * dZ > 400)
				{
					continue;
				}
			}

			this.sendS2CPacket(target, buffer, false);
		}
	}

	@Override
	public void initiateClientItemEvent(ItemStack stack, int event)
	{
		assert false;
	}

	@Override
	public void initiateClientTileEntityEvent(BlockEntity te, int event)
	{
		assert false;
	}

	public void initiateRpc(int id, Class<? extends IRpcProvider<?>> provider, Object[] args)
	{
		assert false;
	}

	public void requestGUI(IHasGui inventory)
	{
		assert false;
	}

	private void handleSubData(GrowingBuffer buffer, ItemStack stack, Integer ID)
	{
		boolean subInv = ID != null && stack.getItem() instanceof IHandHeldSubInventory;
		buffer.writeBoolean(subInv);
		if (subInv)
		{
			buffer.writeShort(ID);
		}
	}

	public final void sendInitialData(BlockEntity te, ServerPlayer player)
	{
		assert !this.isClient();
		if (te instanceof INetworkDataProvider)
		{
			TeUpdateDataServer updateData = getTeUpdateData(te);

			for (String field : ((INetworkDataProvider) te).getNetworkedFields())
			{
				updateData.addPlayerField(field, player);
			}
		}
	}

	@Override
	public final void sendInitialData(BlockEntity te)
	{
		assert !this.isClient();
		if (te instanceof INetworkDataProvider)
		{
			TeUpdateDataServer updateData = getTeUpdateData(te);
			List<String> fields = ((INetworkDataProvider) te).getNetworkedFields();

			for (String field : fields)
			{
				updateData.addGlobalField(field);
			}

			if (TeUpdate.debug)
			{
				IC2.log.info(LogCategory.Network, "Sending initial TE data for %s (%s).", Util.formatPosition(te), fields);
			}
		}
	}

	public final void sendChat(ServerPlayer player, String message)
	{
		assert !this.isClient();
		GrowingBuffer buffer = new GrowingBuffer(message.length() * 2);
		buffer.writeString(message);
		buffer.flip();
		this.sendLargePacket(player, 1, buffer);
	}

	public final void sendConsole(ServerPlayer player, String message)
	{
		assert !this.isClient();
		GrowingBuffer buffer = new GrowingBuffer(message.length() * 2);
		buffer.writeString(message);
		buffer.flip();
		this.sendLargePacket(player, 2, buffer);
	}

	public final void sendContainerFields(ContainerBase<?> container, String... fieldNames)
	{
		for (String fieldName : fieldNames)
		{
			this.sendContainerField(container, fieldName);
		}
	}

	public final void sendContainerField(ContainerBase<?> container, String fieldName)
	{
		if (this.isClient() && this.getClientModifiableField(container.getClass(), fieldName) == null)
		{
			IC2.log.warn(LogCategory.Network, "Field update for %s failed.", container);
		} else
		{
			GrowingBuffer buffer = new GrowingBuffer(256);

			try
			{
				SubPacketType.ContainerData.writeTo(buffer);
				buffer.writeInt(container.containerId);
				writeFieldData(container, fieldName, buffer);
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}

			buffer.flip();
			if (!this.isClient())
			{
				if (container.getPlayer() instanceof ServerPlayer)
				{
					this.sendS2CPacket((ServerPlayer) container.getPlayer(), buffer, false);
				}
			} else
			{
				this.sendC2SPacket(buffer);
			}
		}
	}

	public final void sendContainerEvent(ContainerBase<?> container, String event)
	{
		GrowingBuffer buffer = new GrowingBuffer(64);
		SubPacketType.ContainerEvent.writeTo(buffer);
		buffer.writeInt(container.containerId);
		buffer.writeString(event);
		buffer.flip();
		if (!this.isClient())
		{
			for (ContainerListener listener : container.getListeners())
			{
				if (listener instanceof ServerPlayer)
				{
					this.sendS2CPacket((ServerPlayer) listener, buffer, false);
				}
			}
		} else
		{
			this.sendC2SPacket(buffer);
		}
	}

	public final void sendHandHeldInvField(ContainerBase<?> container, String fieldName)
	{
		if (!(container.base instanceof HandHeldInventory))
		{
			IC2.log.warn(LogCategory.Network, "Invalid container (%s) sent for field update.", container);
		} else if (this.isClient() && this.getClientModifiableField(container.base.getClass(), fieldName) == null)
		{
			IC2.log.warn(LogCategory.Network, "Field update for %s failed.", container);
		} else
		{
			GrowingBuffer buffer = new GrowingBuffer(256);

			try
			{
				SubPacketType.HandHeldInvData.writeTo(buffer);
				buffer.writeInt(container.containerId);
				writeFieldData(container.base, fieldName, buffer);
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}

			buffer.flip();
			if (!this.isClient())
			{
				for (ContainerListener listener : container.getListeners())
				{
					if (listener instanceof ServerPlayer)
					{
						this.sendS2CPacket((ServerPlayer) listener, buffer, false);
					}
				}
			} else
			{
				this.sendC2SPacket(buffer);
			}
		}
	}

	final void sendLargePacket(ServerPlayer player, int id, GrowingBuffer data)
	{
		GrowingBuffer buffer = new GrowingBuffer(16384);
		buffer.writeShort(0);

		try
		{
			DeflaterOutputStream deflate = new DeflaterOutputStream(buffer);
			data.writeTo(deflate);
			deflate.close();
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		buffer.flip();
		boolean firstPacket = true;

		boolean lastPacket;
		do
		{
			lastPacket = buffer.available() <= 32766;
			if (!firstPacket)
			{
				buffer.skipBytes(-2);
			}

			SubPacketType.LargePacket.writeTo(buffer);
			int state = 0;
			if (firstPacket)
			{
				state |= 1;
			}

			if (lastPacket)
			{
				state |= 2;
			}

			state |= id << 2;
			buffer.write(state);
			buffer.skipBytes(-2);
			if (lastPacket)
			{
				this.sendS2CPacket(player, buffer, true);
				assert !buffer.hasAvailable();
			} else
			{
				this.sendS2CPacket(player, buffer.copy(32766), true);
			}

			firstPacket = false;
		} while (!lastPacket);
	}

	public void onPacket(ByteBuf packet, Player player)
	{
		assert !player.level().isClientSide;

		try
		{
			this.onPacketData(GrowingBuffer.wrap(packet), player);
		} catch (Throwable t)
		{
			IC2.log.warn(LogCategory.Network, t, "Network fromJson failed");
			throw new RuntimeException(t);
		}
	}

	private void onPacketData(GrowingBuffer is, Player player) throws IOException
	{
		if (is.hasAvailable())
		{
			SubPacketType packetType = SubPacketType.read(is, true);
			if (packetType != null)
			{
				switch (packetType)
				{
					case ItemEvent:
					{
						final ItemStack stack = DataEncoder.decode(is, ItemStack.class);
						final int event = is.readInt();
						if (stack.getItem() instanceof INetworkItemEventListener)
						{
							IC2.sideProxy.requestTick(true, () -> ((INetworkItemEventListener) stack.getItem()).onNetworkEvent(stack, player, event));
						}
						break;
					}
					case KeyUpdate:
						final int keyState = is.readInt();
						IC2.sideProxy.requestTick(true, () -> IC2.keyboard.processKeyUpdate(player, keyState));
						break;
					case TileEntityEvent:
					{
						final Object teDeferred = DataEncoder.decodeDeferred(is, BlockEntity.class);
						final int event = is.readInt();
						IC2.sideProxy.requestTick(true, () ->
						{
							BlockEntity te = DataEncoder.getValue(teDeferred, player.getServer());
							if (te instanceof INetworkClientTileEntityEventListener)
							{
								((INetworkClientTileEntityEventListener) te).onNetworkEvent(player, event);
							}
						});
						break;
					}
					case RequestGUI:
						final boolean hand = is.readBoolean();
						if (!hand)
						{
							DataEncoder.decodeDeferred(is, BlockEntity.class);
						}

						IC2.sideProxy.requestTick(true, () ->
						{
							if (hand)
							{
								for (InteractionHand hand1 : Util.HANDS)
								{
									ItemStack stack = player.getItemInHand(hand1);
									if (stack.getItem() instanceof IHandHeldInventory)
									{
										IHasGui gui = ((IHandHeldInventory) stack.getItem()).getInventory(player, hand1, stack);
										gui.openManagedItem(player, hand1, null);
										break;
									}
								}
							}
						});
						break;
					case Rpc:
						RpcHandler.processRpcRequest(is, (ServerPlayer) player);
						break;
					default:
						this.onCommonPacketData(packetType, true, is, player);
				}
			}
		}
	}

	protected void onCommonPacketData(SubPacketType packetType, boolean simulating, GrowingBuffer is, Player player) throws IOException
	{
		switch (packetType)
		{
			case PlayerItemData:
				final int slot = is.readByte();
				final Item item = DataEncoder.decode(is, Item.class);
				int dataCount = is.readVarInt();
				final Object[] subData = new Object[dataCount];

				for (int i = 0; i < dataCount; i++)
				{
					subData[i] = DataEncoder.decode(is);
				}

				if (slot >= 0 && slot < 9)
				{
					IC2.sideProxy.requestTick(simulating, () ->
					{
						for (int i = 0; i < subData.length; i++)
						{
							subData[i] = DataEncoder.getValue(subData[i], player.getServer());
						}

						ItemStack stack = player.getInventory().items.get(slot);
						if (!StackUtil.isEmpty(stack) && stack.getItem() == item && item instanceof IPlayerItemDataListener)
						{
							((IPlayerItemDataListener) item).onPlayerItemNetworkData(player, slot, subData);
						}
					});
				}
				break;
			case ContainerData:
			{
				final int windowId = is.readInt();
				final String fieldName = is.readString();
				final Object value = DataEncoder.decode(is);
				IC2.sideProxy
					.requestTick(
						simulating,
						() ->
						{
							if (player.containerMenu instanceof ContainerBase
								&& player.containerMenu.containerId == windowId
								&& (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(player.containerMenu.getClass(), fieldName) != null))
							{
								ReflectionUtil.setValueRecursive(player.containerMenu, fieldName, DataEncoder.getValue(value, player.getServer()));
							}
						}
					);
				break;
			}
			case ContainerEvent:
			{
				final int windowId = is.readInt();
				final String event = is.readString();
				IC2.sideProxy.requestTick(simulating, () ->
				{
					if (player.containerMenu instanceof ContainerBase && player.containerMenu.containerId == windowId)
					{
						((ContainerBase<?>) player.containerMenu).onContainerEvent(event);
					}
				});
				break;
			}
			case HandHeldInvData:
			{
				final int windowId = is.readInt();
				final String fieldName = is.readString();
				final Object value = DataEncoder.decode(is);
				IC2.sideProxy
					.requestTick(
						simulating,
						() ->
						{
							if (player.containerMenu instanceof ContainerBase<?> container && player.containerMenu.containerId == windowId)
							{
								if (container.base instanceof HandHeldInventory
									&& (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(container.base.getClass(), fieldName) != null)
								)
								{
									ReflectionUtil.setValueRecursive(container.base, fieldName, DataEncoder.getValue(value, player.getServer()));
								}
							}
						}
					);
				break;
			}
			case TileEntityData:
			{
				final Object teDeferred = DataEncoder.decodeDeferred(is, BlockEntity.class);
				final String fieldName = is.readString();
				final Object value = DataEncoder.decode(is);
				IC2.sideProxy.requestTick(simulating, () ->
				{
					BlockEntity te = DataEncoder.getValue(teDeferred, player.getServer());
					if (te != null && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(te.getClass(), fieldName) != null))
					{
						ReflectionUtil.setValueRecursive(te, fieldName, DataEncoder.getValue(value, player.getServer()));
					}
				});
				break;
			}
			default:
				IC2.log.warn(LogCategory.Network, "Unhandled packet type: %s", packetType.name());
		}
	}

	public void initiateKeyUpdate(int keyState)
	{
	}
	public final void initiateExplosionEffect(Level world, Vec3 pos, Ic2Explosion.Type type)
	{
		assert !this.isClient();

		try
		{
			GrowingBuffer buffer = new GrowingBuffer(32);
			SubPacketType.ExplosionEffect.writeTo(buffer);
			DataEncoder.encode(buffer, world, false);
			DataEncoder.encode(buffer, pos, false);
			DataEncoder.encode(buffer, type, false);
			buffer.flip();

			for (Player player : world.players())
			{
				if (player instanceof ServerPlayer && player.distanceToSqr(pos.x, pos.y, pos.z) < 128.0)
				{
					this.sendS2CPacket((ServerPlayer) player, buffer, false);
				}
			}
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void sendC2SPacket(GrowingBuffer buffer)
	{
		throw new UnsupportedOperationException("can't send c2s packet serverside");
	}

	protected final void sendS2CPacket(ServerPlayer player, GrowingBuffer buffer, boolean advancePos)
	{
		assert !this.isClient();
		ByteBuf data = makePacket(buffer, advancePos);
		ServerGamePacketListenerImpl handler = player.connection;
		Packet<?> packet = new ClientboundCustomPayloadPacket(channelId, new FriendlyByteBuf(data));
		handler.send(packet);
	}
}
