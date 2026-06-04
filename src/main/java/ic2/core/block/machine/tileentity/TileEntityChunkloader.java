// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import com.google.common.collect.ImmutableSet;
import ic2.core.util.LogCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiChunkLoader;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import net.minecraft.world.World;
import java.util.Iterator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import ic2.core.ChunkLoaderLogic;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import java.util.HashSet;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotUpgrade;
import net.minecraft.util.math.ChunkPos;
import java.util.Set;
import net.minecraftforge.common.ForgeChunkManager;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityChunkloader extends TileEntityInventory implements INetworkClientTileEntityEventListener, IHasGui, IUpgradableBlock
{
    private ForgeChunkManager.Ticket ticket;
    private final Set<ChunkPos> loadedChunks;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotDischarge dischargeSlot;
    public final Energy energy;
    private static final int defaultTier = 1;
    private static final int defaultEnergyStorage = 2500;
    private final double euPerChunk;
    
    public TileEntityChunkloader() {
        this.loadedChunks = new HashSet<ChunkPos>();
        this.euPerChunk = ConfigUtil.getFloat(MainConfig.get(), "balance/euPerChunk");
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.IO, 1, true, InvSlot.InvSide.ANY);
        this.energy = this.addComponent(Energy.asBasicSink(this, 2500.0, 1).addManagedSlot(this.dischargeSlot));
    }
    
    public void updateEntityServer() {
        super.updateEntityServer();
        final boolean active = this.energy.useEnergy(this.getLoadedChunks().size() * this.euPerChunk);
        if (active != this.getActive()) {
            this.setActive(active);
        }
        this.upgradeSlot.tick();
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        final NBTTagList list = nbt.getTagList("loadedChunks", 4);
        this.loadedChunks.clear();
        for (int i = 0; i < list.tagCount(); ++i) {
            final NBTTagLong currentNBT = (NBTTagLong)list.get(i);
            final long value = currentNBT.getLong();
            this.loadedChunks.add(ChunkLoaderLogic.deserialize(value));
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        final NBTTagList list = new NBTTagList();
        nbt.setTag("loadedChunks", (NBTBase)list);
        for (final ChunkPos chunk : this.loadedChunks) {
            list.appendTag((NBTBase)new NBTTagLong(ChunkLoaderLogic.serialize(chunk)));
        }
        return nbt;
    }
    
    @Override
    public void setActive(final boolean active) {
        final World world = this.getWorld();
        if (!world.isRemote && this.getActive() != active) {
            if (active) {
                if (this.ticket != null) {
                    throw new IllegalStateException("Cannot activate ChunkLoader: " + this.pos + " " + this.ticket);
                }
                this.ticket = ChunkLoaderLogic.getInstance().createTicket(world, this.pos);
                for (final ChunkPos coords : this.loadedChunks) {
                    ChunkLoaderLogic.getInstance().addChunkToTicket(this.ticket, coords);
                }
            }
            else {
                if (this.ticket == null) {
                    throw new IllegalStateException("Cannot deactivate ChunkLoader: " + this.pos + " " + this.ticket);
                }
                ChunkLoaderLogic.getInstance().removeTicket(this.ticket);
                this.ticket = null;
            }
        }
        super.setActive(active);
    }
    
    public void onLoaded() {
        super.onLoaded();
        final World world = this.getWorld();
        if (!world.isRemote) {
            this.ticket = ChunkLoaderLogic.getInstance().getTicket(world, this.pos, false);
            if (this.ticket != null) {
                this.loadedChunks.clear();
                this.loadedChunks.addAll((Collection<? extends ChunkPos>)this.ticket.getChunkList());
            }
            super.setActive(this.ticket != null);
            this.setOverclockRates();
        }
    }
    
    @Override
    protected void onUnloaded() {
        super.onUnloaded();
        this.ticket = null;
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        this.loadedChunks.add(ChunkLoaderLogic.getChunkCoords(this.pos));
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        return this.getWorld().isRemote || IC2.platform.launchGui(player, this);
    }
    
    @Override
    public ContainerBase<TileEntityChunkloader> getGuiContainer(final EntityPlayer player) {
        return new ContainerChunkLoader(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiChunkLoader(new ContainerChunkLoader(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public void addChunkToLoaded(final ChunkPos chunk) {
        if (this.getWorld().isRemote) {
            new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
            return;
        }
        if (!this.isChunkInRange(chunk)) {
            IC2.log.warn(LogCategory.Block, "Trying to add a Chunk to loaded, however the chunk is too far away. Aborting.");
            return;
        }
        if (this.getLoadedChunks().size() < ChunkLoaderLogic.getInstance().getMaxChunksPerTicket()) {
            if (this.ticket != null) {
                ChunkLoaderLogic.getInstance().addChunkToTicket(this.ticket, chunk);
            }
            this.loadedChunks.add(chunk);
            this.markDirty();
        }
    }
    
    public void removeChunkFromLoaded(final ChunkPos chunk) {
        if (this.getWorld().isRemote) {
            new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
            return;
        }
        if (ChunkLoaderLogic.getChunkCoords(this.pos).equals((Object)chunk)) {
            return;
        }
        if (this.ticket != null) {
            ChunkLoaderLogic.getInstance().removeChunkFromTicket(this.ticket, chunk);
        }
        this.loadedChunks.remove(chunk);
        this.markDirty();
    }
    
    public ImmutableSet<ChunkPos> getLoadedChunks() {
        return (ImmutableSet<ChunkPos>)ImmutableSet.copyOf((Collection)this.loadedChunks);
    }
    
    public boolean isChunkInRange(final ChunkPos chunk) {
        final ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.pos);
        return Math.abs(chunk.x - mainChunk.x) <= 4 && Math.abs(chunk.z - mainChunk.z) <= 4;
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        final int x = (event & 0xF) - 8;
        final int z = (event >> 4 & 0xF) - 8;
        final ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.pos);
        final ChunkPos chunk = new ChunkPos(mainChunk.x + x, mainChunk.z + z);
        if (this.isChunkInRange(chunk)) {
            if (this.getLoadedChunks().contains((Object)chunk)) {
                this.removeChunkFromLoaded(chunk);
            }
            else {
                this.addChunkToLoaded(chunk);
            }
        }
    }
    
    @Override
    protected void onBlockBreak() {
        super.onBlockBreak();
        if (this.ticket != null) {
            ChunkLoaderLogic.getInstance().removeTicket(this.ticket);
            this.ticket = null;
        }
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.Transformer);
    }
    
    public void setOverclockRates() {
        this.upgradeSlot.onChanged();
        final int tier = this.upgradeSlot.getTier(1);
        this.energy.setSinkTier(tier);
        this.dischargeSlot.setTier(tier);
        this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(2500, 0, 0));
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.setOverclockRates();
        }
    }
}
