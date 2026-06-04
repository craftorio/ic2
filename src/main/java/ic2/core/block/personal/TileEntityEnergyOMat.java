// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.Entity;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.StackUtil;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.core.IC2;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.event.EnergyTileLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotConsumableLinked;
import ic2.core.block.invslot.InvSlot;
import com.mojang.authlib.GameProfile;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergySink;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityEnergyOMat extends TileEntityInventory implements IPersonalBlock, IHasGui, IEnergySink, IEnergySource, INetworkClientTileEntityEventListener, IUpgradableBlock
{
    public int euOffer;
    private GameProfile owner;
    private boolean addedToEnergyNet;
    public int paidFor;
    public double euBuffer;
    private int euBufferMax;
    private int tier;
    public final InvSlot demandSlot;
    public final InvSlotConsumableLinked inputSlot;
    public final InvSlotCharge chargeSlot;
    public final InvSlotUpgrade upgradeSlot;
    
    public TileEntityEnergyOMat() {
        this.euOffer = 1000;
        this.owner = null;
        this.addedToEnergyNet = false;
        this.euBufferMax = 10000;
        this.tier = 1;
        this.demandSlot = new InvSlot(this, "demand", InvSlot.Access.NONE, 1);
        this.inputSlot = new InvSlotConsumableLinked(this, "input", 1, this.demandSlot);
        this.chargeSlot = new InvSlotCharge(this, 1);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 1);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey("ownerGameProfile")) {
            this.owner = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("ownerGameProfile"));
        }
        this.euOffer = nbttagcompound.getInteger("euOffer");
        this.paidFor = nbttagcompound.getInteger("paidFor");
        try {
            this.euBuffer = nbttagcompound.getDouble("euBuffer");
        }
        catch (final Exception e) {
            this.euBuffer = nbttagcompound.getInteger("euBuffer");
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (this.owner != null) {
            final NBTTagCompound ownerNbt = new NBTTagCompound();
            NBTUtil.writeGameProfile(ownerNbt, this.owner);
            nbt.setTag("ownerGameProfile", (NBTBase)ownerNbt);
        }
        nbt.setInteger("euOffer", this.euOffer);
        nbt.setInteger("paidFor", this.paidFor);
        nbt.setDouble("euBuffer", this.euBuffer);
        return nbt;
    }
    
    public boolean wrenchCanRemove(final EntityPlayer player) {
        return this.permitsAccess(player.getGameProfile());
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
            this.addedToEnergyNet = true;
        }
    }
    
    @Override
    protected void onUnloaded() {
        if (IC2.platform.isSimulating() && this.addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
            this.addedToEnergyNet = false;
        }
        super.onUnloaded();
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean invChanged = false;
        this.euBufferMax = 10000;
        this.tier = 1;
        this.chargeSlot.setTier(1);
        if (!this.upgradeSlot.isEmpty()) {
            this.euBufferMax = this.upgradeSlot.getEnergyStorage(10000, 0, 0);
            this.tier = 1 + this.upgradeSlot.extraTier;
            this.chargeSlot.setTier(this.tier);
        }
        final ItemStack tradedIn = this.inputSlot.consumeLinked(true);
        if (tradedIn != null) {
            final int transferred = StackUtil.distribute(this, tradedIn, true);
            if (transferred == StackUtil.getSize(tradedIn)) {
                StackUtil.distribute(this, this.inputSlot.consumeLinked(false), false);
                this.paidFor += this.euOffer;
                invChanged = true;
            }
        }
        if (this.euBuffer >= 1.0) {
            final double sent = this.chargeSlot.charge(this.euBuffer);
            if (sent > 0.0) {
                this.euBuffer -= sent;
                invChanged = true;
            }
        }
        if (invChanged) {
            this.markDirty();
        }
    }
    
    @Override
    public boolean permitsAccess(final GameProfile profile) {
        return TileEntityPersonalChest.checkAccess(this, profile);
    }
    
    @Override
    public IInventory getPrivilegedInventory(final GameProfile accessor) {
        return (IInventory)this;
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = new ArrayList<String>();
        ret.add("owner");
        ret.addAll(super.getNetworkedFields());
        return ret;
    }
    
    @Override
    public GameProfile getOwner() {
        return this.owner;
    }
    
    @Override
    public void setOwner(final GameProfile owner) {
        this.owner = owner;
    }
    
    @Override
    protected boolean canEntityDestroy(final Entity entity) {
        return false;
    }
    
    @Override
    protected boolean canSetFacingWrench(final EnumFacing facing, final EntityPlayer player) {
        return player != null && this.permitsAccess(player.getGameProfile()) && super.canSetFacingWrench(facing, player);
    }
    
    public boolean acceptsEnergyFrom(final IEnergyEmitter emitter, final EnumFacing direction) {
        return !this.facingMatchesDirection(direction);
    }
    
    public boolean facingMatchesDirection(final EnumFacing direction) {
        return direction == this.getFacing();
    }
    
    public boolean emitsEnergyTo(final IEnergyAcceptor receiver, final EnumFacing direction) {
        return this.facingMatchesDirection(direction);
    }
    
    @Override
    public double getOfferedEnergy() {
        return this.euBuffer;
    }
    
    @Override
    public void drawEnergy(final double amount) {
        this.euBuffer -= amount;
    }
    
    @Override
    public double getDemandedEnergy() {
        return Math.min(this.paidFor, this.euBufferMax - this.euBuffer);
    }
    
    @Override
    public double injectEnergy(final EnumFacing directionFrom, final double amount, final double voltage) {
        final double toAdd = Math.min(Math.min(amount, this.paidFor), this.euBufferMax - this.euBuffer);
        this.paidFor -= (int)toAdd;
        this.euBuffer += toAdd;
        return amount - toAdd;
    }
    
    @Override
    public int getSourceTier() {
        return this.tier;
    }
    
    @Override
    public int getSinkTier() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public ContainerBase<TileEntityEnergyOMat> getGuiContainer(final EntityPlayer player) {
        if (this.permitsAccess(player.getGameProfile())) {
            return new ContainerEnergyOMatOpen(player, this);
        }
        return new ContainerEnergyOMatClosed(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        if (isAdmin || this.permitsAccess(player.getGameProfile())) {
            return (GuiScreen)new GuiEnergyOMatOpen(new ContainerEnergyOMatOpen(player, this));
        }
        return (GuiScreen)new GuiEnergyOMatClosed(new ContainerEnergyOMatClosed(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        if (!this.permitsAccess(player.getGameProfile())) {
            return;
        }
        switch (event) {
            case 0: {
                this.attemptSet(-100000);
                break;
            }
            case 1: {
                this.attemptSet(-10000);
                break;
            }
            case 2: {
                this.attemptSet(-1000);
                break;
            }
            case 3: {
                this.attemptSet(-100);
                break;
            }
            case 4: {
                this.attemptSet(100000);
                break;
            }
            case 5: {
                this.attemptSet(10000);
                break;
            }
            case 6: {
                this.attemptSet(1000);
                break;
            }
            case 7: {
                this.attemptSet(100);
                break;
            }
        }
    }
    
    private void attemptSet(final int amount) {
        this.euOffer += amount;
        if (this.euOffer < 100) {
            this.euOffer = 100;
        }
    }
    
    @Override
    public double getEnergy() {
        return this.euBuffer;
    }
    
    @Override
    public boolean useEnergy(double amount) {
        if (amount <= this.euBuffer) {
            amount -= this.euBuffer;
            return true;
        }
        return false;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.Transformer);
    }
}
