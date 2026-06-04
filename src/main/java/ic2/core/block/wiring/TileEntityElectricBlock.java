// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import ic2.core.init.Localization;
import ic2.core.IC2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Set;
import ic2.core.block.TileEntityBlock;
import java.util.EnumSet;
import net.minecraft.util.EnumFacing;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.api.tile.IEnergyStorage;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public abstract class TileEntityElectricBlock extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, IEnergyStorage
{
    protected double output;
    public byte redstoneMode;
    public static byte redstoneModes;
    public final InvSlotCharge chargeSlot;
    public final InvSlotDischarge dischargeSlot;
    public final Energy energy;
    public final Redstone redstone;
    public final RedstoneEmitter rsEmitter;
    
    public TileEntityElectricBlock(final int tier, final int output, final int maxStorage) {
        this.redstoneMode = 0;
        this.output = output;
        this.chargeSlot = new InvSlotCharge(this, tier);
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.IO, tier, InvSlot.InvSide.BOTTOM);
        this.energy = this.addComponent(new Energy(this, maxStorage, (Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)EnumFacing.DOWN)), EnumSet.of(EnumFacing.DOWN), tier, tier, true).addManagedSlot(this.chargeSlot).addManagedSlot(this.dischargeSlot));
        this.rsEmitter = this.addComponent(new RedstoneEmitter(this));
        this.redstone = this.addComponent(new Redstone(this));
        this.comparator.setUpdate(this.energy::getComparatorValue);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        this.superReadFromNBT(nbt);
        this.energy.setDirections((Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)this.getFacing())), EnumSet.of(this.getFacing()));
    }
    
    protected final void superReadFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.redstoneMode = nbt.getByte("redstoneMode");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("redstoneMode", this.redstoneMode);
        return nbt;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.energy.setSendingEnabled(this.shouldEmitEnergy());
        this.rsEmitter.setLevel(this.shouldEmitRedstone() ? 15 : 0);
    }
    
    @Override
    public ContainerBase<? extends TileEntityElectricBlock> getGuiContainer(final EntityPlayer player) {
        return new ContainerElectricBlock(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiElectricBlock(new ContainerElectricBlock(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public void setFacing(final EnumFacing facing) {
        super.setFacing(facing);
        this.energy.setDirections((Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)this.getFacing())), EnumSet.of(this.getFacing()));
    }
    
    protected final void superSetFacing(final EnumFacing facing) {
        super.setFacing(facing);
    }
    
    protected boolean shouldEmitRedstone() {
        switch (this.redstoneMode) {
            case 1: {
                return this.energy.getEnergy() >= this.energy.getCapacity() - this.output * 20.0;
            }
            case 2: {
                return this.energy.getEnergy() > this.output && this.energy.getEnergy() < this.energy.getCapacity() - this.output;
            }
            case 3: {
                return this.energy.getEnergy() < this.energy.getCapacity() - this.output;
            }
            case 4: {
                return this.energy.getEnergy() < this.output;
            }
            default: {
                return false;
            }
        }
    }
    
    protected boolean shouldEmitEnergy() {
        final boolean redstone = this.redstone.hasRedstoneInput();
        if (this.redstoneMode == 5) {
            return !redstone;
        }
        return this.redstoneMode != 6 || !redstone || this.energy.getEnergy() > this.energy.getCapacity() - this.output * 20.0;
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        ++this.redstoneMode;
        if (this.redstoneMode >= TileEntityElectricBlock.redstoneModes) {
            this.redstoneMode = 0;
        }
        IC2.platform.messagePlayer(player, this.getRedstoneMode(), new Object[0]);
    }
    
    public String getRedstoneMode() {
        if (this.redstoneMode >= TileEntityElectricBlock.redstoneModes || this.redstoneMode < 0) {
            return "";
        }
        return Localization.translate("ic2.EUStorage.gui.mod.redstone" + this.redstoneMode);
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (!this.getWorld().isRemote) {
            final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
            this.energy.addEnergy(nbt.getDouble("energy"));
        }
    }
    
    public void onUpgraded() {
        this.rerender();
    }
    
    @Override
    protected ItemStack adjustDrop(ItemStack drop, final boolean wrench) {
        drop = super.adjustDrop(drop, wrench);
        if (wrench || this.teBlock.getDefaultDrop() == TeBlock.DefaultDrop.Self) {
            final double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
            final double totalEnergy = this.energy.getEnergy();
            if (retainedRatio > 0.0 && totalEnergy > 0.0) {
                final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
                nbt.setDouble("energy", totalEnergy * retainedRatio);
            }
        }
        return drop;
    }
    
    @Override
    public int getOutput() {
        return (int)this.output;
    }
    
    @Override
    public double getOutputEnergyUnitsPerTick() {
        return this.output;
    }
    
    @Override
    public void setStored(final int energy) {
    }
    
    @Override
    public int addEnergy(final int amount) {
        this.energy.addEnergy(amount);
        return amount;
    }
    
    @Override
    public int getStored() {
        return (int)this.energy.getEnergy();
    }
    
    @Override
    public int getCapacity() {
        return (int)this.energy.getCapacity();
    }
    
    @Override
    public boolean isTeleporterCompatible(final EnumFacing side) {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final List<String> tooltip, final ITooltipFlag advanced) {
        super.addInformation(stack, tooltip, advanced);
        tooltip.add(String.format("%s %.0f %s %s %d %s", Localization.translate("ic2.item.tooltip.Output"), EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier()), Localization.translate("ic2.generic.text.EUt"), Localization.translate("ic2.item.tooltip.Capacity"), this.getCapacity(), Localization.translate("ic2.generic.text.EU")));
        tooltip.add(Localization.translate("ic2.item.tooltip.Store") + " " + (long)StackUtil.getOrCreateNbtData(stack).getDouble("energy") + " " + Localization.translate("ic2.generic.text.EU"));
    }
    
    static {
        TileEntityElectricBlock.redstoneModes = 7;
    }
}
