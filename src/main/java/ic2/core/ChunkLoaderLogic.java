package ic2.core;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ChunkLoaderLogic implements ForgeChunkManager.LoadingCallback
{
	private static ChunkLoaderLogic instance;
	private final Map<World, List<ForgeChunkManager.Ticket>> tickets = new IdentityHashMap<>();

	ChunkLoaderLogic()
	{
		if (instance != null)
		{
			throw new IllegalStateException();
		}

		instance = this;
		MinecraftForge.EVENT_BUS.register(this);
		ForgeChunkManager.setForcedChunkLoadingCallback(IC2.getInstance(), this);
	}

	private List<ForgeChunkManager.Ticket> getTicketsForWorld(World world)
	{
		if (world.isRemote)
		{
			return null;
		}

		if (!this.tickets.containsKey(world))
		{
			this.tickets.put(world, new ArrayList<>());
		}

		return this.tickets.get(world);
	}

	@SubscribeEvent
	public void unloadWorld(WorldEvent.Unload event)
	{
		if (!event.getWorld().isRemote)
		{
			if (this.tickets.containsKey(event.getWorld()))
			{
				this.tickets.remove(event.getWorld());
			}
		}
	}

	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world)
	{
		List<ForgeChunkManager.Ticket> worldTickets = this.getTicketsForWorld(world);

		for (ForgeChunkManager.Ticket ticket : tickets)
		{
			worldTickets.add(ticket);
			NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);

			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagLong value = (NBTTagLong) list.get(i);
				ForgeChunkManager.forceChunk(ticket, deserialize(value.getLong()));
			}

			ChunkPos mainChunk = getChunkCoords(this.getPosFromTicket(ticket));
			if (!ticket.getChunkList().contains(mainChunk))
			{
				ForgeChunkManager.forceChunk(ticket, mainChunk);
			}
		}
	}

	public ForgeChunkManager.Ticket getTicket(World world, BlockPos pos, boolean create)
	{
		if (world.isRemote)
		{
			return null;
		}

		List<ForgeChunkManager.Ticket> ticketList = this.getTicketsForWorld(world);
		if (ticketList == null)
		{
			throw new IllegalStateException();
		}

		for (ForgeChunkManager.Ticket ticket : ticketList)
		{
			if (pos.equals(this.getPosFromTicket(ticket)))
			{
				return ticket;
			}
		}

		return create ? this.createTicket(world, pos) : null;
	}

	public ForgeChunkManager.Ticket createTicket(World world, BlockPos pos)
	{
		assert this.getTicket(world, pos, false) == null;
		ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(IC2.getInstance(), world, ForgeChunkManager.Type.NORMAL);
		ticket.getModData().setInteger("x", pos.getX());
		ticket.getModData().setInteger("y", pos.getY());
		ticket.getModData().setInteger("z", pos.getZ());
		this.getTicketsForWorld(world).add(ticket);
		this.addChunkToTicket(ticket, getChunkCoords(pos));
		return ticket;
	}

	public void addChunkToTicket(ForgeChunkManager.Ticket ticket, ChunkPos chunk)
	{
		if (!ticket.getChunkList().contains(chunk))
		{
			ForgeChunkManager.forceChunk(ticket, chunk);
			ForgeChunkManager.reorderChunk(ticket, getChunkCoords(this.getPosFromTicket(ticket)));
			NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);
			if (!ticket.getModData().hasKey("loadedChunks", 9))
			{
				ticket.getModData().setTag("loadedChunks", list);
			}

			ticket.getModData().setTag("loadedChunks", list);
			list.appendTag(new NBTTagLong(chunk.x & 4294967295L | (chunk.z & 4294967295L) << 32));
		}
	}

	public void removeChunkFromTicket(ForgeChunkManager.Ticket ticket, ChunkPos chunk)
	{
		if (!getChunkCoords(this.getPosFromTicket(ticket)).equals(chunk))
		{
			ForgeChunkManager.unforceChunk(ticket, chunk);
			NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);
			long serializedChunk = serialize(chunk);

			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagLong pos = (NBTTagLong) list.get(i);
				if (pos.getLong() == serializedChunk)
				{
					list.removeTag(i--);
				}
			}
		}
	}

	public void removeTicket(World world, BlockPos pos)
	{
		this.removeTicket(this.getTicket(world, pos, false));
	}

	public void removeTicket(ForgeChunkManager.Ticket ticket)
	{
		ForgeChunkManager.releaseTicket(ticket);
		this.getTicketsForWorld(ticket.world).remove(ticket);
	}

	public int getMaxChunksPerTicket()
	{
		return ForgeChunkManager.getMaxChunkDepthFor("ic2");
	}

	private BlockPos getPosFromTicket(ForgeChunkManager.Ticket ticket)
	{
		return new BlockPos(ticket.getModData().getInteger("x"), ticket.getModData().getInteger("y"), ticket.getModData().getInteger("z"));
	}

	public static ChunkLoaderLogic getInstance()
	{
		return instance;
	}

	public static long serialize(ChunkPos chunk)
	{
		return chunk.x & 4294967295L | (chunk.z & 4294967295L) << 32;
	}

	public static ChunkPos deserialize(long value)
	{
		return new ChunkPos((int) (value & -1L), (int) (value >> 32));
	}

	public static ChunkPos getChunkCoords(BlockPos pos)
	{
		return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
	}
}
