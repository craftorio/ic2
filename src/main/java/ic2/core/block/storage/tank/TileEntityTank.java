// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.storage.tank;

import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import ic2.core.util.LiquidUtil;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLivingBase;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.core.block.TileEntityBlock;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.comp.Fluids;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public abstract class TileEntityTank extends TileEntityInventory implements IHasGui
{
    protected final Fluids fluidsComponent;
    @GuiSynced
    protected final FluidTank contents;
    
    public TileEntityTank(final int bucketMultiplier) {
        this.fluidsComponent = this.addComponent(new Fluids(this));
        this.contents = this.fluidsComponent.addTank("contents", 1000 * bucketMultiplier);
    }
    
    @Override
    protected List<ItemStack> getAuxDrops(final int fortune) {
        return Collections.emptyList();
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (!this.world.isRemote) {
            final NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
            this.contents.readFromNBT(tag);
        }
    }
    
    @Override
    protected ItemStack adjustDrop(final ItemStack drop, final boolean wrench) {
        final NBTTagCompound tag = StackUtil.getOrCreateNbtData(drop);
        if (this.contents.getFluidAmount() > 0) {
            this.contents.writeToNBT(tag);
        }
        return drop;
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!this.world.isRemote && LiquidUtil.isFluidContainer(stack)) {
            final boolean changed = FluidUtil.interactWithFluidHandler(player, hand, (IFluidHandler)this.fluidsComponent.getCapability((net.minecraftforge.common.capabilities.Capability<IFluidHandler>)CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side));
            if (changed) {
                this.markDirty();
            }
            return changed;
        }
        return super.onActivated(player, hand, side, hitX, hitY, hitZ);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final List<String> info, final ITooltipFlag advanced) {
        info.add("Capacity: " + this.contents.getCapacity() + " mB");
        final NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
        if (!tag.hasKey("Empty")) {
            final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag);
            if (fluidStack == null) {
                info.add("Empty");
            }
            else {
                info.add(fluidStack.getLocalizedName());
                info.add("Amount: " + fluidStack.amount + " mB");
                info.add("Type: " + (fluidStack.getFluid().isGaseous() ? "Gas" : "Liquid"));
            }
        }
        else {
            info.add("Empty");
        }
    }
    
    @Override
    protected SoundType getBlockSound(final Entity entity) {
        return SoundType.METAL;
    }
    
    @Override
    public ContainerBase<? extends TileEntityTank> getGuiContainer(final EntityPlayer player) {
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
