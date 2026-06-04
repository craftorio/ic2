// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.storage.box;

import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLivingBase;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public abstract class TileEntityStorageBox extends TileEntityInventory implements IHasGui
{
    protected final InvSlot inventory;
    
    public TileEntityStorageBox(final int inventorySize) {
        this.inventory = new InvSlot(this, "inventory", InvSlot.Access.IO, inventorySize, InvSlot.InvSide.ANY);
    }
    
    @Override
    protected List<ItemStack> getAuxDrops(final int fortune) {
        return Collections.emptyList();
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (!this.getWorld().isRemote) {
            final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
            this.inventory.readFromNbt(nbt);
        }
    }
    
    @Override
    protected ItemStack adjustDrop(final ItemStack drop, final boolean wrench) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
        if (!this.inventory.isEmpty()) {
            this.inventory.writeToNbt(nbt);
        }
        return drop;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final List<String> info, final ITooltipFlag advanced) {
        info.add("Stores items even when broken");
        info.add("Inventory size: " + this.inventory.size());
    }
    
    @Override
    protected SoundType getBlockSound(final Entity entity) {
        return SoundType.METAL;
    }
    
    @Override
    public ContainerBase<? extends TileEntityStorageBox> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
}
