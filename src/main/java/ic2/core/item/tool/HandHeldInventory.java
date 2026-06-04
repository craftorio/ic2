// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import java.util.HashSet;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.crash.CrashReportCategory;
import ic2.core.util.LogCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.crash.CrashReport;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ic2.core.IHasGui;

public abstract class HandHeldInventory implements IHasGui
{
    protected ItemStack containerStack;
    protected final ItemStack[] inventory;
    protected final EntityPlayer player;
    private boolean cleared;
    private static final Set<EntityPlayer> PLAYERS_IN_GUI;
    
    public HandHeldInventory(final EntityPlayer player, final ItemStack containerStack, final int inventorySize) {
        this.containerStack = containerStack;
        this.inventory = new ItemStack[inventorySize];
        this.player = player;
        if (IC2.platform.isSimulating()) {
            final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(containerStack);
            if (!nbt.hasKey("uid", 3)) {
                nbt.setInteger("uid", IC2.random.nextInt());
            }
            final NBTTagList contentList = nbt.getTagList("Items", 10);
            for (int i = 0; i < contentList.tagCount(); ++i) {
                final NBTTagCompound slotNbt = contentList.getCompoundTagAt(i);
                final int slot = slotNbt.getByte("Slot");
                if (slot >= 0 && slot < this.inventory.length) {
                    this.inventory[slot] = new ItemStack(slotNbt);
                }
            }
        }
    }
    
    public int getSizeInventory() {
        return this.inventory.length;
    }
    
    public boolean isEmpty() {
        for (final ItemStack stack : this.inventory) {
            if (!StackUtil.isEmpty(stack)) {
                return false;
            }
        }
        return true;
    }
    
    public ItemStack getStackInSlot(final int slot) {
        return StackUtil.wrapEmpty(this.inventory[slot]);
    }
    
    public ItemStack decrStackSize(final int index, final int amount) {
        final ItemStack stack;
        if (index >= 0 && index < this.inventory.length && !StackUtil.isEmpty(stack = this.inventory[index])) {
            ItemStack ret;
            if (amount >= StackUtil.getSize(stack)) {
                ret = stack;
                this.inventory[index] = StackUtil.emptyStack;
            }
            else {
                ret = StackUtil.copyWithSize(stack, amount);
                this.inventory[index] = StackUtil.decSize(stack, amount);
            }
            this.save();
            return ret;
        }
        return StackUtil.emptyStack;
    }
    
    public void setInventorySlotContents(final int slot, ItemStack stack) {
        if (!StackUtil.isEmpty(stack) && StackUtil.getSize(stack) > this.getInventoryStackLimit()) {
            stack = StackUtil.copyWithSize(stack, this.getInventoryStackLimit());
        }
        if (StackUtil.isEmpty(stack)) {
            this.inventory[slot] = StackUtil.emptyStack;
        }
        else {
            this.inventory[slot] = stack;
        }
        this.save();
    }
    
    public int getInventoryStackLimit() {
        return 64;
    }
    
    public boolean isItemValidForSlot(final int slot, final ItemStack stack1) {
        return false;
    }
    
    public void markDirty() {
        this.save();
    }
    
    public boolean isUsableByPlayer(final EntityPlayer player) {
        return player == this.player && this.getPlayerInventoryIndex() >= -1;
    }
    
    public void openInventory(final EntityPlayer player) {
    }
    
    public void closeInventory(final EntityPlayer player) {
    }
    
    public ItemStack removeStackFromSlot(final int index) {
        final ItemStack ret = this.getStackInSlot(index);
        if (!StackUtil.isEmpty(ret)) {
            this.setInventorySlotContents(index, null);
        }
        return ret;
    }
    
    public int getField(final int id) {
        return 0;
    }
    
    public void setField(final int id, final int value) {
    }
    
    public int getFieldCount() {
        return 0;
    }
    
    public ITextComponent getDisplayName() {
        return (ITextComponent)new TextComponentString(this.getName());
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
        this.save();
        if (!player.getEntityWorld().isRemote) {
            if (HandHeldInventory.PLAYERS_IN_GUI.contains(player)) {
                HandHeldInventory.PLAYERS_IN_GUI.remove(player);
            }
            else {
                StackUtil.getOrCreateNbtData(this.containerStack).removeTag("uid");
            }
        }
    }
    
    public boolean isThisContainer(final ItemStack stack) {
        if (StackUtil.isEmpty(stack) || stack.getItem() != this.containerStack.getItem()) {
            return false;
        }
        final NBTTagCompound nbt = stack.getTagCompound();
        return nbt != null && nbt.getInteger("uid") == this.getUid();
    }
    
    protected int getUid() {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(this.containerStack);
        return nbt.getInteger("uid");
    }
    
    protected int getPlayerInventoryIndex() {
        for (int i = -1; i < this.player.inventory.getSizeInventory(); ++i) {
            final ItemStack stack = (i == -1) ? this.player.inventory.getItemStack() : this.player.inventory.getStackInSlot(i);
            if (this.isThisContainer(stack)) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }
    
    protected void save() {
        if (!IC2.platform.isSimulating()) {
            return;
        }
        if (this.cleared) {
            return;
        }
        boolean dropItself = false;
        for (int i = 0; i < this.inventory.length; ++i) {
            if (this.isThisContainer(this.inventory[i])) {
                this.inventory[i] = null;
                dropItself = true;
            }
        }
        final NBTTagList contentList = new NBTTagList();
        for (int j = 0; j < this.inventory.length; ++j) {
            if (!StackUtil.isEmpty(this.inventory[j])) {
                final NBTTagCompound nbt = new NBTTagCompound();
                nbt.setByte("Slot", (byte)j);
                this.inventory[j].writeToNBT(nbt);
                contentList.appendTag((NBTBase)nbt);
            }
        }
        StackUtil.getOrCreateNbtData(this.containerStack).setTag("Items", (NBTBase)contentList);
        try {
            this.containerStack = StackUtil.copyWithSize(this.containerStack, 1);
        }
        catch (final IllegalArgumentException e) {
            final CrashReport crash = new CrashReport("Hand held container stack vanished", (Throwable)e);
            CrashReportCategory category = crash.makeCategory("Container stack");
            category.addCrashSection("Stack", (Object)StackUtil.toStringSafe(this.containerStack));
            category.addCrashSection("NBT", (Object)this.containerStack.getTagCompound());
            category.addCrashSection("Position", (Object)this.getPlayerInventoryIndex());
            category.addCrashSection("Had thrown", (Object)dropItself);
            category = crash.makeCategory("Container info");
            category.addCrashSection("Type", (Object)this.getClass().getName());
            category.addCrashSection("Container", (Object)((this.player.openContainer == null) ? null : this.player.openContainer.getClass().getName()));
            if (this.player.world.isRemote) {
                category.addDetail("GUI", (ICrashReportDetail)new ICrashReportDetail<String>() {
                    public String call() throws Exception {
                        final GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                        return (gui == null) ? null : gui.getClass().getName();
                    }
                });
            }
            category.addCrashSection("Opened by", (Object)this.player);
            throw new ReportedException(crash);
        }
        if (dropItself) {
            StackUtil.dropAsEntity(this.player.getEntityWorld(), this.player.getPosition(), this.containerStack);
            this.clear();
        }
        else {
            final int idx = this.getPlayerInventoryIndex();
            if (idx < -1) {
                IC2.log.warn(LogCategory.Item, "Handheld inventory saving failed for player " + this.player.getDisplayName().getUnformattedText() + '.');
                this.clear();
            }
            else if (idx == -1) {
                this.player.inventory.setItemStack(this.containerStack);
            }
            else {
                this.player.inventory.setInventorySlotContents(idx, this.containerStack);
            }
        }
    }
    
    public void saveAsThrown(final ItemStack stack) {
        assert IC2.platform.isSimulating();
        final NBTTagList contentList = new NBTTagList();
        for (int i = 0; i < this.inventory.length; ++i) {
            if (!StackUtil.isEmpty(this.inventory[i]) && !this.isThisContainer(this.inventory[i])) {
                final NBTTagCompound nbt = new NBTTagCompound();
                nbt.setByte("Slot", (byte)i);
                this.inventory[i].writeToNBT(nbt);
                contentList.appendTag((NBTBase)nbt);
            }
        }
        StackUtil.getOrCreateNbtData(stack).setTag("Items", (NBTBase)contentList);
        assert StackUtil.getOrCreateNbtData(stack).getInteger("uid") == 0;
        this.clear();
    }
    
    public void clear() {
        for (int i = 0; i < this.inventory.length; ++i) {
            this.inventory[i] = null;
        }
        this.cleared = true;
    }
    
    public SlotHologramSlot.ChangeCallback makeSaveCallback() {
        return new SlotHologramSlot.ChangeCallback() {
            @Override
            public void onChanged(final int index) {
                HandHeldInventory.this.save();
            }
        };
    }
    
    public void onEvent(final String event) {
    }
    
    public static void addMaintainedPlayer(final EntityPlayer player) {
        HandHeldInventory.PLAYERS_IN_GUI.add(player);
    }
    
    static {
        PLAYERS_IN_GUI = new HashSet<EntityPlayer>();
    }
}
