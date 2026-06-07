package ic2.core.network;

import com.mojang.authlib.GameProfile;
import ic2.api.network.INetworkItemEventListener;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Explosion;
import ic2.core.block.comp.Components;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.item.IHandHeldInventory;
import ic2.core.proxy.SideProxyClient;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.InflaterOutputStream;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

@OnlyIn(Dist.CLIENT)
public class NetworkManagerClient extends NetworkManager
{
	private GrowingBuffer largePacketBuffer;

	@Override
	protected boolean isClient()
	{
		return true;
	}

	@Override
	public void initiateClientItemEvent(ItemStack stack, int event)
	{
		try
		{
			GrowingBuffer buffer = new GrowingBuffer(256);
			SubPacketType.ItemEvent.writeTo(buffer);
			DataEncoder.encode(buffer, stack, false);
			buffer.writeInt(event);
			buffer.flip();
			this.sendC2SPacket(buffer);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void initiateKeyUpdate(int keyState)
	{
		GrowingBuffer buffer = new GrowingBuffer(5);
		SubPacketType.KeyUpdate.writeTo(buffer);
		buffer.writeInt(keyState);
		buffer.flip();
		this.sendC2SPacket(buffer);
	}

	@Override
	public void initiateClientTileEntityEvent(BlockEntity te, int event)
	{
		try
		{
			GrowingBuffer buffer = new GrowingBuffer(32);
			SubPacketType.TileEntityEvent.writeTo(buffer);
			DataEncoder.encode(buffer, te, false);
			buffer.writeInt(event);
			buffer.flip();
			this.sendC2SPacket(buffer);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void initiateRpc(int id, Class<? extends IRpcProvider<?>> provider, Object[] args)
	{
		try
		{
			GrowingBuffer buffer = new GrowingBuffer(256);
			SubPacketType.Rpc.writeTo(buffer);
			buffer.writeInt(id);
			buffer.writeString(provider.getName());
			DataEncoder.encode(buffer, args);
			buffer.flip();
			this.sendC2SPacket(buffer);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void requestGUI(IHasGui inventory)
	{
		try
		{
			GrowingBuffer buffer = new GrowingBuffer(32);
			SubPacketType.RequestGUI.writeTo(buffer);
			if (inventory instanceof BlockEntity te)
			{
				buffer.writeBoolean(false);
				DataEncoder.encode(buffer, te, false);
			} else
			{
				Player player = Minecraft.m_91087_().f_91074_;
				if ((StackUtil.isEmpty(player.getInventory().m_36056_()) || !(player.getInventory().m_36056_().getItem() instanceof IHandHeldInventory))
					&& (StackUtil.isEmpty(player.m_21206_()) || !(player.m_21206_().getItem() instanceof IHandHeldInventory)))
				{
					IC2.sideProxy
						.displayError(
							"An unknown GUI type was attempted to be displayed.\nThis could happen due to corrupted data from a player or a bug.\n\n(Technical information: "
								+ inventory
								+ ")"
						);
				} else
				{
					buffer.writeBoolean(true);
				}
			}

			buffer.flip();
			this.sendC2SPacket(buffer);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onPacket(ByteBuf packet, Player player)
	{
		assert player == null || player.f_19853_.isClientSide;

		try
		{
			this.onPacketData(GrowingBuffer.wrap(packet), player);
		} catch (Throwable t)
		{
			IC2.log.warn(LogCategory.Network, t, "Network read failed");
			throw new RuntimeException(t);
		}
	}

	private void onPacketData(GrowingBuffer is, Player player) throws IOException
	{
		if (is.hasAvailable())
		{
			SubPacketType packetType = SubPacketType.read(is, false);
			if (packetType != null)
			{
				switch (packetType)
				{
					case LargePacket:
						int state = is.readUnsignedByte();
						if ((state & 2) != 0)
						{
							GrowingBuffer input;
							if ((state & 1) != 0)
							{
								input = is;
							} else
							{
								input = this.largePacketBuffer;
								if (input == null)
								{
									throw new IOException("unexpected large packet continuation");
								}

								is.writeTo(input);
								input.flip();
								this.largePacketBuffer = null;
							}

							GrowingBuffer decompBuffer = new GrowingBuffer(input.available() * 2);
							InflaterOutputStream inflate = new InflaterOutputStream(decompBuffer);
							input.writeTo(inflate);
							inflate.close();
							decompBuffer.flip();
							switch (state >> 2)
							{
								case 0:
									TeUpdate.receive(decompBuffer);
									return;
								case 1:
									processChatPacket(decompBuffer);
									return;
								case 2:
									processConsolePacket(decompBuffer);
							}
						} else
						{
							if ((state & 1) != 0)
							{
								assert this.largePacketBuffer == null;
								this.largePacketBuffer = new GrowingBuffer(32752);
							}

							if (this.largePacketBuffer == null)
							{
								throw new IOException("unexpected large packet continuation");
							}

							is.writeTo(this.largePacketBuffer);
						}
						break;
					case TileEntityEvent:
					{
						final Object teDeferred = DataEncoder.decodeDeferred(is, BlockEntity.class);
						final int event = is.readInt();
						IC2.sideProxy.requestTick(false, new Runnable()
						{
							@Override
							public void run()
							{
								BlockEntity te = DataEncoder.getValue(teDeferred, null);
								if (te instanceof INetworkTileEntityEventListener)
								{
									((INetworkTileEntityEventListener) te).onNetworkEvent(event);
								}
							}
						});
						break;
					}
					case ItemEvent:
					{
						final GameProfile profile = DataEncoder.decode(is, GameProfile.class);
						final ItemStack stack = DataEncoder.decode(is, ItemStack.class);
						final int event = is.readInt();
						IC2.sideProxy
							.requestTick(
								false,
								new Runnable()
								{
									@Override
									public void run()
									{
										Level world = Minecraft.m_91087_().f_91073_;

										for (Player player : world.m_6907_())
										{
											if (profile.getId() != null && profile.getId().equals(player.getGameProfile().getId())
												|| profile.getId() == null && profile.getName().equals(player.getGameProfile().getName()))
											{
												if (stack.getItem() instanceof INetworkItemEventListener)
												{
													((INetworkItemEventListener) stack.getItem()).onNetworkEvent(stack, player, event);
												}
												break;
											}
										}
									}
								}
							);
						break;
					}
					case ExplosionEffect:
					{
						Object worldDeferred = DataEncoder.decodeDeferred(is, Level.class);
						Vec3 pos = DataEncoder.decode(is, Vec3.class);
						Ic2Explosion.Type type = DataEncoder.decodeEnum(is, Ic2Explosion.Type.class);
						IC2.sideProxy
							.requestTick(
								false,
								() ->
								{
									Level world = DataEncoder.getValue(worldDeferred, null);
									if (world != null && type != null)
									{
										switch (type)
										{
											case Normal:
												world.playLocalSound(
													pos.f_82479_,
													pos.f_82480_,
													pos.f_82481_,
													SoundEvents.f_11913_,
													SoundSource.BLOCKS,
													4.0F,
													(1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F,
													true
												);
												world.addParticle(ParticleTypes.f_123812_, pos.f_82479_, pos.f_82480_, pos.f_82481_, 0.0, 0.0, 0.0);
												break;
											case Electrical:
												world.playLocalSound(
													pos.f_82479_, pos.f_82480_, pos.f_82481_, Ic2SoundEvents.MACHINE_OVERLOAD, SoundSource.BLOCKS, 1.0F, 1.0F, true
												);
												world.addParticle(ParticleTypes.f_123812_, pos.f_82479_, pos.f_82480_, pos.f_82481_, 0.0, 0.0, 0.0);
												break;
											case Heat:
												world.playLocalSound(
													pos.f_82479_,
													pos.f_82480_,
													pos.f_82481_,
													SoundEvents.f_11937_,
													SoundSource.BLOCKS,
													4.0F,
													(1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F,
													true
												);
												world.addParticle(ParticleTypes.f_123812_, pos.f_82479_, pos.f_82480_, pos.f_82481_, 0.0, 0.0, 0.0);
												break;
											case Nuclear:
												world.playLocalSound(
													pos.f_82479_, pos.f_82480_, pos.f_82481_, Ic2SoundEvents.BLOCK_NUKE_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F, true
												);
												world.addParticle(ParticleTypes.f_123812_, pos.f_82479_, pos.f_82480_, pos.f_82481_, 0.0, 0.0, 0.0);
										}
									}
								}
							);
						break;
					}
					case Rpc:
						RpcHandler.processRpcResponse(is);
						break;
					case TileEntityBlockComponent:
					{
						final ResourceLocation dimensionId = DataEncoder.getValue(DataEncoder.decode(is, DataEncoder.EncodedType.ResourceLocation), null);
						final BlockPos pos = DataEncoder.decode(is, BlockPos.class);
						String componentName = is.readString();
						final Class<? extends TileEntityComponent> componentCls = Components.getClass(componentName);
						if (componentCls == null)
						{
							throw new IOException("invalid component: " + componentName);
						}

						int dataLen = is.readVarInt();
						if (dataLen > 65536)
						{
							throw new IOException("data length limit exceeded");
						}

						final byte[] data = new byte[dataLen];
						is.readFully(data);
						IC2.sideProxy.requestTick(false, new Runnable()
						{
							@Override
							public void run()
							{
								Level world = Minecraft.m_91087_().f_91073_;
								if (Util.getDimId(world).equals(dimensionId))
								{
									BlockEntity teRaw = world.getBlockEntity(pos);
									if (teRaw instanceof Ic2TileEntity)
									{
										TileEntityComponent component = ((Ic2TileEntity) teRaw).getComponent(componentCls);
										if (component != null)
										{
											DataInputStream dataIs = new DataInputStream(new ByteArrayInputStream(data));

											try
											{
												component.onNetworkUpdate(dataIs);
											} catch (IOException e)
											{
												throw new RuntimeException(e);
											}
										}
									}
								}
							}
						});
						break;
					}
					default:
						this.onCommonPacketData(packetType, false, is, player);
				}
			}
		}
	}

	private static void processChatPacket(GrowingBuffer buffer)
	{
		final String messages = buffer.readString();
		IC2.sideProxy.requestTick(false, new Runnable()
		{
			@Override
			public void run()
			{
				for (String line : messages.split("[\\r\\n]+"))
				{
					IC2.sideProxy.messagePlayer(null, line);
				}
			}
		});
	}

	private static void processConsolePacket(GrowingBuffer buffer)
	{
		String messages = buffer.readString();
		PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));

		for (String line : messages.split("[\\r\\n]+"))
		{
			console.println(line);
		}

		console.flush();
	}

	@Override
	protected final void sendC2SPacket(GrowingBuffer buffer)
	{
		ClientPacketListener handler = SideProxyClient.mc.m_91403_();
		if (handler != null)
		{
			handler.m_6198_().m_129512_(new ServerboundCustomPayloadPacket(channelId, new FriendlyByteBuf(makePacket(buffer, true))));
		}
	}
}
