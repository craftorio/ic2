// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.uu.UuIndex;
import ic2.core.item.ItemCrystalMemory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiPatternStorage;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerPatternStorage;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Iterator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.ref.ItemName;
import net.minecraft.item.Item;
import ic2.core.block.invslot.InvSlot;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.profile.NotClassic;
import ic2.api.recipe.IPatternStorage;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityPatternStorage extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, IPatternStorage
{
    public final InvSlotConsumableId diskSlot;
    private final List<ItemStack> patterns;
    public int index;
    public int maxIndex;
    public ItemStack pattern;
    public double patternUu;
    public double patternEu;
    
    public TileEntityPatternStorage() {
        this.patterns = new ArrayList<ItemStack>();
        this.index = 0;
        this.diskSlot = new InvSlotConsumableId(this, "SaveSlot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, new Item[] { ItemName.crystal_memory.getInstance() });
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.readContents(nbttagcompound);
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        this.writeContentsAsNbtList(nbt);
        return nbt;
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (!this.getWorld().isRemote) {
            final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
            this.readContents(nbt);
        }
    }
    
    @Override
    protected ItemStack adjustDrop(ItemStack drop, final boolean wrench) {
        drop = super.adjustDrop(drop, wrench);
        if (wrench || this.teBlock.getDefaultDrop() == TeBlock.DefaultDrop.Self) {
            final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
            this.writeContentsAsNbtList(nbt);
        }
        return drop;
    }
    
    public void readContents(final NBTTagCompound nbt) {
        final NBTTagList patternList = nbt.getTagList("patterns", 10);
        for (int i = 0; i < patternList.tagCount(); ++i) {
            final NBTTagCompound contentTag = patternList.getCompoundTagAt(i);
            final ItemStack Item = new ItemStack(contentTag);
            this.addPattern(Item);
        }
        this.refreshInfo();
    }
    
    private void writeContentsAsNbtList(final NBTTagCompound nbt) {
        final NBTTagList list = new NBTTagList();
        for (final ItemStack stack : this.patterns) {
            final NBTTagCompound contentTag = new NBTTagCompound();
            stack.writeToNBT(contentTag);
            list.appendTag((NBTBase)contentTag);
        }
        nbt.setTag("patterns", (NBTBase)list);
    }
    
    @Override
    public ContainerBase<TileEntityPatternStorage> getGuiContainer(final EntityPlayer player) {
        return new ContainerPatternStorage(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiPatternStorage(new ContainerPatternStorage(player, this));
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        switch (event) {
            case 0: {
                if (!this.patterns.isEmpty()) {
                    if (this.index <= 0) {
                        this.index = this.patterns.size() - 1;
                    }
                    else {
                        --this.index;
                    }
                    this.refreshInfo();
                    break;
                }
                break;
            }
            case 1: {
                if (!this.patterns.isEmpty()) {
                    if (this.index >= this.patterns.size() - 1) {
                        this.index = 0;
                    }
                    else {
                        ++this.index;
                    }
                    this.refreshInfo();
                    break;
                }
                break;
            }
            case 2: {
                if (this.index >= 0 && this.index < this.patterns.size() && !this.diskSlot.isEmpty()) {
                    final ItemStack crystalMemory = this.diskSlot.get();
                    if (crystalMemory.getItem() instanceof ItemCrystalMemory) {
                        ((ItemCrystalMemory)crystalMemory.getItem()).writecontentsTag(crystalMemory, this.patterns.get(this.index));
                    }
                    break;
                }
                break;
            }
            case 3: {
                if (this.diskSlot.isEmpty()) {
                    break;
                }
                final ItemStack crystalMemory = this.diskSlot.get();
                if (!(crystalMemory.getItem() instanceof ItemCrystalMemory)) {
                    break;
                }
                final ItemStack record = ((ItemCrystalMemory)crystalMemory.getItem()).readItemStack(crystalMemory);
                if (record != null) {
                    this.addPattern(record);
                    break;
                }
                break;
            }
        }
    }
    
    public void refreshInfo() {
        if (this.index < 0 || this.index >= this.patterns.size()) {
            this.index = 0;
        }
        this.maxIndex = this.patterns.size();
        if (this.patterns.isEmpty()) {
            this.pattern = null;
        }
        else {
            this.pattern = this.patterns.get(this.index);
            this.patternUu = UuIndex.instance.getInBuckets(this.pattern);
        }
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public boolean addPattern(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            throw new IllegalArgumentException("empty stack: " + StackUtil.toStringSafe(stack));
        }
        for (final ItemStack pattern : this.patterns) {
            if (StackUtil.checkItemEquality(pattern, stack)) {
                return false;
            }
        }
        this.patterns.add(stack);
        this.refreshInfo();
        return true;
    }
    
    @Override
    public List<ItemStack> getPatterns() {
        return this.patterns;
    }
}
