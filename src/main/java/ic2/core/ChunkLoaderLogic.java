package ic2.core;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ChunkLoaderLogic implements ForgeChunkManager.LoadingCallback {
  private static ChunkLoaderLogic instance;
  
  private final Map<World, List<ForgeChunkManager.Ticket>> tickets = new IdentityHashMap<>();
  
  ChunkLoaderLogic() {
    if (instance != null)
      throw new IllegalStateException(); 
    instance = this;
    MinecraftForge.EVENT_BUS.register(this);
    ForgeChunkManager.setForcedChunkLoadingCallback(IC2.getInstance(), this);
  }
  
  private List<ForgeChunkManager.Ticket> getTicketsForWorld(World world) {
    if (world.isRemote)
      return null; 
    if (!this.tickets.containsKey(world))
      this.tickets.put(world, new ArrayList<>()); 
    return this.tickets.get(world);
  }
  
  @SubscribeEvent
  public void unloadWorld(WorldEvent.Unload event) {
    if ((event.getWorld()).isRemote)
      return; 
    if (this.tickets.containsKey(event.getWorld()))
      this.tickets.remove(event.getWorld()); 
  }
  
  public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
    List<ForgeChunkManager.Ticket> worldTickets = getTicketsForWorld(world);
    for (ForgeChunkManager.Ticket ticket : tickets) {
      worldTickets.add(ticket);
      NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);
      for (int i = 0; i < list.tagCount(); i++) {
        NBTTagLong value = (NBTTagLong)list.func_179238_g(i);
        ForgeChunkManager.forceChunk(ticket, deserialize(value.func_150291_c()));
      } 
      ChunkPos mainChunk = getChunkCoords(getPosFromTicket(ticket));
      if (!ticket.getChunkList().contains(mainChunk))
        ForgeChunkManager.forceChunk(ticket, mainChunk); 
    } 
  }
  
  public ForgeChunkManager.Ticket getTicket(World world, BlockPos pos, boolean create) {
    if (world.isRemote)
      return null; 
    List<ForgeChunkManager.Ticket> ticketList = getTicketsForWorld(world);
    if (ticketList == null)
      throw new IllegalStateException(); 
    for (ForgeChunkManager.Ticket ticket : ticketList) {
      if (pos.equals(getPosFromTicket(ticket)))
        return ticket; 
    } 
    if (create)
      return createTicket(world, pos); 
    return null;
  }
  
  public ForgeChunkManager.Ticket createTicket(World world, BlockPos pos) {
    assert getTicket(world, pos, false) == null;
    ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(IC2.getInstance(), world, ForgeChunkManager.Type.NORMAL);
    ticket.getModData().setInteger("x", pos.getX());
    ticket.getModData().setInteger("y", pos.getY());
    ticket.getModData().setInteger("z", pos.getZ());
    getTicketsForWorld(world).add(ticket);
    addChunkToTicket(ticket, getChunkCoords(pos));
    return ticket;
  }
  
  public void addChunkToTicket(ForgeChunkManager.Ticket ticket, ChunkPos chunk) {
    if (ticket.getChunkList().contains(chunk))
      return; 
    ForgeChunkManager.forceChunk(ticket, chunk);
    ForgeChunkManager.reorderChunk(ticket, getChunkCoords(getPosFromTicket(ticket)));
    NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);
    if (!ticket.getModData().hasKey("loadedChunks", 9))
      ticket.getModData().setTag("loadedChunks", (NBTBase)list); 
    ticket.getModData().setTag("loadedChunks", (NBTBase)list);
    list.appendTag((NBTBase)new NBTTagLong(chunk.field_77276_a & 0xFFFFFFFFL | (chunk.field_77275_b & 0xFFFFFFFFL) << 32L));
  }
  
  public void removeChunkFromTicket(ForgeChunkManager.Ticket ticket, ChunkPos chunk) {
    if (getChunkCoords(getPosFromTicket(ticket)).equals(chunk))
      return; 
    ForgeChunkManager.unforceChunk(ticket, chunk);
    NBTTagList list = ticket.getModData().getTagList("loadedChunks", 4);
    long serializedChunk = serialize(chunk);
    for (int i = 0; i < list.tagCount(); i++) {
      NBTTagLong pos = (NBTTagLong)list.func_179238_g(i);
      if (pos.func_150291_c() == serializedChunk)
        list.func_74744_a(i--); 
    } 
  }
  
  public void removeTicket(World world, BlockPos pos) {
    removeTicket(getTicket(world, pos, false));
  }
  
  public void removeTicket(ForgeChunkManager.Ticket ticket) {
    ForgeChunkManager.releaseTicket(ticket);
    getTicketsForWorld(ticket.world).remove(ticket);
  }
  
  public int getMaxChunksPerTicket() {
    return ForgeChunkManager.getMaxChunkDepthFor("ic2");
  }
  
  private BlockPos getPosFromTicket(ForgeChunkManager.Ticket ticket) {
    return new BlockPos(ticket.getModData().getInteger("x"), ticket.getModData().getInteger("y"), ticket.getModData().getInteger("z"));
  }
  
  public static ChunkLoaderLogic getInstance() {
    return instance;
  }
  
  public static long serialize(ChunkPos chunk) {
    return chunk.field_77276_a & 0xFFFFFFFFL | (chunk.field_77275_b & 0xFFFFFFFFL) << 32L;
  }
  
  public static ChunkPos deserialize(long value) {
    return new ChunkPos((int)(value & 0xFFFFFFFFFFFFFFFFL), (int)(value >> 32L));
  }
  
  public static ChunkPos getChunkCoords(BlockPos pos) {
    return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
  }
}
