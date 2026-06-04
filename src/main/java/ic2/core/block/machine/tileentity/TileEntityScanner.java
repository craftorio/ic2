// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.uu.UuIndex;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiScanner;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerScanner;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Iterator;
import ic2.api.recipe.IPatternStorage;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.uu.UuGraph;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.ref.ItemName;
import net.minecraft.item.Item;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotScannable;
import ic2.core.util.StackUtil;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import net.minecraft.item.ItemStack;
import ic2.core.profile.NotClassic;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;

@NotClassic
public class TileEntityScanner extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener
{
    private ItemStack currentStack;
    private ItemStack pattern;
    private final int energyusecycle = 256;
    public int progress;
    public final int duration = 3300;
    public final InvSlotConsumable inputSlot;
    public final InvSlot diskSlot;
    private State state;
    public double patternUu;
    public double patternEu;
    
    public TileEntityScanner() {
        super(512000, 4);
        this.currentStack = StackUtil.emptyStack;
        this.pattern = StackUtil.emptyStack;
        this.progress = 0;
        this.state = State.IDLE;
        this.inputSlot = new InvSlotScannable(this, "input", 1);
        this.diskSlot = new InvSlotConsumableId(this, "disk", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, new Item[] { ItemName.crystal_memory.getInstance() });
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean newActive = false;
        if (this.progress < 3300) {
            if (this.inputSlot.isEmpty() || (!StackUtil.isEmpty(this.currentStack) && !StackUtil.checkItemEquality(this.currentStack, this.inputSlot.get()))) {
                this.state = State.IDLE;
                this.reset();
            }
            else if (this.getPatternStorage() == null && this.diskSlot.isEmpty()) {
                this.state = State.NO_STORAGE;
                this.reset();
            }
            else if (this.energy.getEnergy() >= 256.0) {
                if (StackUtil.isEmpty(this.currentStack)) {
                    this.currentStack = StackUtil.copyWithSize(this.inputSlot.get(), 1);
                }
                this.pattern = UuGraph.find(this.currentStack);
                if (StackUtil.isEmpty(this.pattern)) {
                    this.state = State.FAILED;
                }
                else if (this.isPatternRecorded(this.pattern)) {
                    this.state = State.ALREADY_RECORDED;
                    this.reset();
                }
                else {
                    newActive = true;
                    this.state = State.SCANNING;
                    this.energy.useEnergy(256.0);
                    ++this.progress;
                    if (this.progress >= 3300) {
                        this.refreshInfo();
                        if (this.patternUu != Double.POSITIVE_INFINITY) {
                            this.state = State.COMPLETED;
                            this.inputSlot.consume(1, false, true);
                            this.markDirty();
                        }
                        else {
                            this.state = State.FAILED;
                        }
                    }
                }
            }
            else {
                this.state = State.NO_ENERGY;
            }
        }
        else if (StackUtil.isEmpty(this.pattern)) {
            this.state = State.IDLE;
            this.progress = 0;
        }
        this.setActive(newActive);
    }
    
    public void reset() {
        this.progress = 0;
        this.currentStack = StackUtil.emptyStack;
        this.pattern = StackUtil.emptyStack;
    }
    
    private boolean isPatternRecorded(final ItemStack stack) {
        if (!this.diskSlot.isEmpty() && this.diskSlot.get().getItem() instanceof ItemCrystalMemory) {
            final ItemStack crystalMemory = this.diskSlot.get();
            if (StackUtil.checkItemEquality(((ItemCrystalMemory)crystalMemory.getItem()).readItemStack(crystalMemory), stack)) {
                return true;
            }
        }
        final IPatternStorage storage = this.getPatternStorage();
        if (storage == null) {
            return false;
        }
        for (final ItemStack stored : storage.getPatterns()) {
            if (StackUtil.checkItemEquality(stored, stack)) {
                return true;
            }
        }
        return false;
    }
    
    private void record() {
        if (StackUtil.isEmpty(this.pattern) || this.patternUu == Double.POSITIVE_INFINITY) {
            this.reset();
            return;
        }
        if (!this.savetoDisk(this.pattern)) {
            final IPatternStorage storage = this.getPatternStorage();
            if (storage == null) {
                this.state = State.TRANSFER_ERROR;
                return;
            }
            if (!storage.addPattern(this.pattern)) {
                this.state = State.TRANSFER_ERROR;
                return;
            }
        }
        this.reset();
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.progress = nbttagcompound.getInteger("progress");
        NBTTagCompound contentTag = nbttagcompound.getCompoundTag("currentStack");
        this.currentStack = new ItemStack(contentTag);
        contentTag = nbttagcompound.getCompoundTag("pattern");
        this.pattern = new ItemStack(contentTag);
        final int stateIdx = nbttagcompound.getInteger("state");
        this.state = ((stateIdx < State.values().length) ? State.values()[stateIdx] : State.IDLE);
        this.refreshInfo();
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", this.progress);
        if (!StackUtil.isEmpty(this.currentStack)) {
            final NBTTagCompound contentTag = new NBTTagCompound();
            this.currentStack.writeToNBT(contentTag);
            nbt.setTag("currentStack", (NBTBase)contentTag);
        }
        if (!StackUtil.isEmpty(this.pattern)) {
            final NBTTagCompound contentTag = new NBTTagCompound();
            this.pattern.writeToNBT(contentTag);
            nbt.setTag("pattern", (NBTBase)contentTag);
        }
        nbt.setInteger("state", this.state.ordinal());
        return nbt;
    }
    
    @Override
    public ContainerBase<TileEntityScanner> getGuiContainer(final EntityPlayer player) {
        return new ContainerScanner(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiScanner(new ContainerScanner(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public IPatternStorage getPatternStorage() {
        final World world = this.getWorld();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity target = world.getTileEntity(this.pos.offset(dir));
            if (target instanceof IPatternStorage) {
                return (IPatternStorage)target;
            }
        }
        return null;
    }
    
    public boolean savetoDisk(final ItemStack stack) {
        if (this.diskSlot.isEmpty() || stack == null) {
            return false;
        }
        if (this.diskSlot.get().getItem() instanceof ItemCrystalMemory) {
            final ItemStack crystalMemory = this.diskSlot.get();
            ((ItemCrystalMemory)crystalMemory.getItem()).writecontentsTag(crystalMemory, stack);
            return true;
        }
        return false;
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        switch (event) {
            case 0: {
                this.reset();
                break;
            }
            case 1: {
                if (this.progress >= 3300) {
                    this.record();
                    break;
                }
                break;
            }
        }
    }
    
    private void refreshInfo() {
        if (!StackUtil.isEmpty(this.pattern)) {
            this.patternUu = UuIndex.instance.getInBuckets(this.pattern);
        }
    }
    
    public int getPercentageDone() {
        return 100 * this.progress / 3300;
    }
    
    public int getSubPercentageDoneScaled(final int width) {
        return width * (100 * this.progress % 3300) / 3300;
    }
    
    public boolean isDone() {
        return this.progress >= 3300;
    }
    
    public State getState() {
        return this.state;
    }
    
    public enum State
    {
        IDLE, 
        SCANNING, 
        COMPLETED, 
        FAILED, 
        NO_STORAGE, 
        NO_ENERGY, 
        TRANSFER_ERROR, 
        ALREADY_RECORDED;
    }
}
