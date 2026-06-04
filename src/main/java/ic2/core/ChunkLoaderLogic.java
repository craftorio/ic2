// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.nbt.NBTTagList;
import java.util.Iterator;
import net.minecraft.nbt.NBTTagLong;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;
import java.util.ArrayList;
import net.minecraftforge.common.MinecraftForge;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.world.World;
import java.util.Map;
import net.minecraftforge.common.ForgeChunkManager;

public final class ChunkLoaderLogic implements ForgeChunkManager.LoadingCallback
{
    private static ChunkLoaderLogic instance;
    private final Map<World, List<ForgeChunkManager.Ticket>> tickets;
    
    ChunkLoaderLogic() {
        this.tickets = new IdentityHashMap<World, List<ForgeChunkManager.Ticket>>();
        if (ChunkLoaderLogic.instance != null) {
            throw new IllegalStateException();
        }
        ChunkLoaderLogic.instance = this;
        MinecraftForge.EVENT_BUS.register((Object)this);
        ForgeChunkManager.setForcedChunkLoadingCallback((Object)IC2.getInstance(), (ForgeChunkManager.LoadingCallback)this);
    }
    
    private List<ForgeChunkManager.Ticket> getTicketsForWorld(final World world) {
        if (world.isRemote) {
            return null;
        }
        if (!this.tickets.containsKey(world)) {
            this.tickets.put(world, new ArrayList<ForgeChunkManager.Ticket>());
        }
        return this.tickets.get(world);
    }
    
    @SubscribeEvent
    public void unloadWorld(final WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            return;
        }
        if (this.tickets.containsKey(event.getWorld())) {
            this.tickets.remove(event.getWorld());
        }
    }
    
    public void ticketsLoaded(final List<ForgeChunkManager.Ticket> tickets, final World world) {
        final List<ForgeChunkManager.Ticket> worldTickets = this.getTicketsForWorld(world);
        for (final ForgeChunkManager.Ticket ticket : tickets) {
            worldTickets.add(ticket);
            final NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);
            for (int i = 0; i < list.tagCount(); ++i) {
                final NBTTagLong value = (NBTTagLong)list.get(i);
                ForgeChunkManager.forceChunk(ticket, deserialize(value.getLong()));
            }
            final ChunkPos mainChunk = getChunkCoords(this.getPosFromTicket(ticket));
            if (!ticket.getChunkList().contains((Object)mainChunk)) {
                ForgeChunkManager.forceChunk(ticket, mainChunk);
            }
        }
    }
    
    public ForgeChunkManager.Ticket getTicket(final World world, final BlockPos pos, final boolean create) {
        if (world.isRemote) {
            return null;
        }
        final List<ForgeChunkManager.Ticket> ticketList = this.getTicketsForWorld(world);
        if (ticketList == null) {
            throw new IllegalStateException();
        }
        for (final ForgeChunkManager.Ticket ticket : ticketList) {
            if (pos.equals((Object)this.getPosFromTicket(ticket))) {
                return ticket;
            }
        }
        if (create) {
            return this.createTicket(world, pos);
        }
        return null;
    }
    
    public ForgeChunkManager.Ticket createTicket(final World world, final BlockPos pos) {
        assert this.getTicket(world, pos, false) == null;
        final ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket((Object)IC2.getInstance(), world, ForgeChunkManager.Type.NORMAL);
        ticket.getModData().setInteger("x", pos.getX());
        ticket.getModData().setInteger("y", pos.getY());
        ticket.getModData().setInteger("z", pos.getZ());
        this.getTicketsForWorld(world).add(ticket);
        this.addChunkToTicket(ticket, getChunkCoords(pos));
        return ticket;
    }
    
    public void addChunkToTicket(final ForgeChunkManager.Ticket ticket, final ChunkPos chunk) {
        if (ticket.getChunkList().contains((Object)chunk)) {
            return;
        }
        ForgeChunkManager.forceChunk(ticket, chunk);
        ForgeChunkManager.reorderChunk(ticket, getChunkCoords(this.getPosFromTicket(ticket)));
        final NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);
        if (!ticket.getModData().hasKey("loadedChunks", 9)) {
            ticket.getModData().setTag("loadedChunks", (NBTBase)list);
        }
        ticket.getModData().setTag("loadedChunks", (NBTBase)list);
        list.appendTag((NBTBase)new NBTTagLong(((long)chunk.x & 0xFFFFFFFFL) | ((long)chunk.z & 0xFFFFFFFFL) << 32));
    }
    
    public void removeChunkFromTicket(final ForgeChunkManager.Ticket ticket, final ChunkPos chunk) {
        if (getChunkCoords(this.getPosFromTicket(ticket)).equals((Object)chunk)) {
            return;
        }
        ForgeChunkManager.unforceChunk(ticket, chunk);
        final NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);
        final long serializedChunk = serialize(chunk);
        for (int i = 0; i < list.tagCount(); ++i) {
            final NBTTagLong pos = (NBTTagLong)list.get(i);
            if (pos.getLong() == serializedChunk) {
                list.removeTag(i--);
            }
        }
    }
    
    public void removeTicket(final World world, final BlockPos pos) {
        this.removeTicket(this.getTicket(world, pos, false));
    }
    
    public void removeTicket(final ForgeChunkManager.Ticket ticket) {
        ForgeChunkManager.releaseTicket(ticket);
        this.getTicketsForWorld(ticket.world).remove(ticket);
    }
    
    public int getMaxChunksPerTicket() {
        return ForgeChunkManager.getMaxChunkDepthFor("ic2");
    }
    
    private BlockPos getPosFromTicket(final ForgeChunkManager.Ticket ticket) {
        return new BlockPos(ticket.getModData().getInteger("x"), ticket.getModData().getInteger("y"), ticket.getModData().getInteger("z"));
    }
    
    public static ChunkLoaderLogic getInstance() {
        return ChunkLoaderLogic.instance;
    }
    
    public static long serialize(final ChunkPos chunk) {
        return ((long)chunk.x & 0xFFFFFFFFL) | ((long)chunk.z & 0xFFFFFFFFL) << 32;
    }
    
    public static ChunkPos deserialize(final long value) {
        return new ChunkPos((int)(value & -1L), (int)(value >> 32));
    }
    
    public static ChunkPos getChunkCoords(final BlockPos pos) {
        return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
