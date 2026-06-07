package ic2.core.network;

import ic2.api.network.INetworkUpdateListener;
import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.event.WorldData;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

class TeUpdate
{
	static final boolean debug = System.getProperty("ic2.network.debug.teupdate") != null;

	public static void send(WorldData worldData, NetworkManager network) throws IOException
	{
		if (!worldData.tesToUpdate.isEmpty())
		{
			Map<ServerPlayer, GrowingBuffer> buffers = new IdentityHashMap<>();
			List<ServerPlayer> playersInRange = new ArrayList<>();
			GrowingBuffer commonBuffer = new GrowingBuffer();

			for (Entry<BlockEntity, TeUpdateDataServer> entry : worldData.tesToUpdate.entrySet())
			{
				BlockEntity te = entry.getKey();
				NetworkManager.getPlayersInRange(te.getLevel(), te.getBlockPos(), playersInRange);
				if (!playersInRange.isEmpty())
				{
					TeUpdateDataServer updateData = entry.getValue();
					DataEncoder.encode(commonBuffer, te.getBlockPos(), false);
					commonBuffer.mark();
					commonBuffer.writeShort(0);

					for (String field : updateData.getGlobalFields())
					{
						NetworkManager.writeFieldData(te, field, commonBuffer);
					}

					commonBuffer.flip();

					for (ServerPlayer player : playersInRange)
					{
						Collection<String> playerFields = updateData.getPlayerFields(player);
						int fieldCount = updateData.getGlobalFields().size() + playerFields.size();
						if (fieldCount != 0)
						{
							if (fieldCount > 65535)
							{
								throw new RuntimeException("too many fields for " + te + ": " + fieldCount);
							}

							commonBuffer.reset();
							commonBuffer.writeShort(fieldCount);
							commonBuffer.rewind();
							GrowingBuffer playerBuffer = buffers.get(player);
							if (playerBuffer == null)
							{
								playerBuffer = new GrowingBuffer(0);
								buffers.put(player, playerBuffer);
								DataEncoder.encode(playerBuffer, Util.getDimId(player.getCommandSenderWorld()), false);
							}

							commonBuffer.writeTo(playerBuffer);
							commonBuffer.rewind();

							for (String field : playerFields)
							{
								NetworkManager.writeFieldData(te, field, playerBuffer);
							}
						}
					}

					commonBuffer.clear();
					playersInRange.clear();
				}
			}

			worldData.tesToUpdate.clear();

			for (Entry<ServerPlayer, GrowingBuffer> entry : buffers.entrySet())
			{
				ServerPlayer player = entry.getKey();
				GrowingBuffer playerBuffer = entry.getValue();
				playerBuffer.flip();
				network.sendLargePacket(player, 0, playerBuffer);
			}
		}
	}

	static void receive(GrowingBuffer buffer) throws IOException
	{
		final ResourceLocation worldId = DataEncoder.getValue(DataEncoder.decode(buffer, DataEncoder.EncodedType.ResourceLocation), null);
		final TeUpdateDataClient updateData = new TeUpdateDataClient();

		while (buffer.hasAvailable())
		{
			BlockPos pos = DataEncoder.decode(buffer, BlockPos.class);
			int fieldCount = buffer.readUnsignedShort();
			TeUpdateDataClient.TeData teData = updateData.addTe(pos, fieldCount);

			for (int i = 0; i < fieldCount; i++)
			{
				String fieldName = buffer.readString();
				Object value = DataEncoder.decode(buffer);
				if (fieldName.equals("teBlk"))
				{
					String id = ((String) Objects.requireNonNull(value)).split(":")[1];
					ResourceLocation identifier = IC2.getIdentifier(id);
					teData.teType = (Ic2TileEntityBlock) Registry.BLOCK.m_7745_(identifier);
				} else
				{
					teData.addField(fieldName, value);
				}
			}

			if (teData.hasClass())
			{
				Class<? extends Ic2TileEntity> teClass = teData.teType.getTeClass();

				for (TeUpdateDataClient.FieldData fieldData : teData.getFields())
				{
					fieldData.field = ReflectionUtil.getFieldRecursive(teClass, fieldData.name);
				}
			}
		}

		if (debug)
		{
			printDebugOutput(worldId, updateData);
		}

		IC2.sideProxy.requestTick(false, new Runnable()
		{
			@Override
			public void run()
			{
				Level world = IC2.sideProxy.getPlayerWorld();
				if (world != null && Util.getDimId(world).equals(worldId))
				{
					for (TeUpdateDataClient.TeData update : updateData.getTes())
					{
						try
						{
							TeUpdate.apply(update, world);
						} catch (Throwable t)
						{
							IC2.log.warn(LogCategory.Network, t, "TE update at %s failed.", Util.formatPosition(world, update.pos));
						}
					}
				}
			}
		});
	}

	private static void printDebugOutput(ResourceLocation dimensionId, TeUpdateDataClient data)
	{
		StringBuilder out = new StringBuilder();
		out.append("dimension: ");
		out.append(dimensionId);
		out.append(", ");
		out.append(data.getTes().size());
		out.append(" tes:\n");

		for (TeUpdateDataClient.TeData te : data.getTes())
		{
			out.append("  pos: ");
			out.append(te.pos.getX());
			out.append('/');
			out.append(te.pos.getY());
			out.append('/');
			out.append(te.pos.getZ());
			out.append(", ");
			out.append(te.getFields().size());
			out.append(" fields:\n");

			for (TeUpdateDataClient.FieldData field : te.getFields())
			{
				out.append("    ");
				out.append(field.name);
				out.append(" = ");
				out.append(field.value);
				if (field.value != null)
				{
					out.append(" (");
					out.append(field.value.getClass().getSimpleName());
					out.append(')');
				}

				out.append('\n');
			}

			if (te.teType != null)
			{
				out.append("    TE Type: ");
				out.append(te.teType.m_49954_());
				out.append('\n');
				if (te.teType.getTeClass() != null)
				{
					out.append("    TE Class: ");
					out.append(te.teType.getTeClass().getName());
					out.append('\n');
				} else
				{
					out.append("    no TE Class\n");
				}
			} else
			{
				out.append("    no TE Type\n");
			}
		}

		out.setLength(out.length() - 1);
		IC2.log.info(LogCategory.Network, "Received TE Update:\n" + out.toString());
	}

	private static void apply(TeUpdateDataClient.TeData update, Level world)
	{
		if (!world.m_46805_(update.pos))
		{
			if (debug)
			{
				IC2.log.info(LogCategory.Network, "Skipping update at %s, chunk not loaded.", Util.formatPosition(world, update.pos));
			}
		} else
		{
			BlockState state = world.getBlockState(update.pos);
			if (update.teType != null && state.getBlock() != update.teType)
			{
				if (debug)
				{
					IC2.log
						.info(
							LogCategory.Network,
							"Can't apply update to %d/%d/%d, invalid state %s.",
							update.pos.getX(),
							update.pos.getY(),
							update.pos.getZ(),
							state
						);
				}
			} else
			{
				BlockEntity te = world.getBlockEntity(update.pos);
				if (update.hasClass() && (te == null || te.getClass() != update.teType.getTeClass() || te.isRemoved() || te.getLevel() != world))
				{
					if (debug)
					{
						IC2.log.info(LogCategory.Network, "Instantiating %s with %s.", Util.formatPosition(world, update.pos), update.teType.getTeClass().getName());
					}

					assert !te.isRemoved();
					assert te.getLevel() == world;
				} else
				{
					if (te == null)
					{
						if (debug)
						{
							IC2.log.info(LogCategory.Network, "Can't apply update at %s, no te and no teClass.", Util.formatPosition(world, update.pos));
						}

						return;
					}

					if (te.isRemoved() || te.getLevel() != world)
					{
						if (debug)
						{
							IC2.log.warn(LogCategory.Network, "Can't apply update at %s, invalid te and no teClass.", Util.formatPosition(world, update.pos));
						}

						return;
					}

					if (debug)
					{
						IC2.log.info(LogCategory.Network, "TE class at %s unchanged.", Util.formatPosition(world, update.pos));
					}
				}

				for (TeUpdateDataClient.FieldData fieldUpdate : update.getFields())
				{
					Object value = DataEncoder.getValue(fieldUpdate.value, null);
					if (fieldUpdate.field != null)
					{
						ReflectionUtil.setValue(te, fieldUpdate.field, value);
					} else
					{
						ReflectionUtil.setValueRecursive(te, fieldUpdate.name, value);
					}

					if (te instanceof INetworkUpdateListener)
					{
						((INetworkUpdateListener) te).onNetworkUpdate(fieldUpdate.name);
					}
				}
			}
		}
	}
}
