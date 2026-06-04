// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.kineticgenerator.gui.GuiElectricKineticGenertor;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Set;
import java.util.Collections;
import java.util.EnumSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityElectricKineticGenerator extends TileEntityInventory implements IKineticSource, IHasGui
{
    public InvSlotConsumableItemStack slotMotor;
    public InvSlotDischarge dischargeSlot;
    private final float kuPerEU;
    public double ku;
    public final int maxKU = 1000;
    protected final Energy energy;
    
    public TileEntityElectricKineticGenerator() {
        this.ku = 0.0;
        this.kuPerEU = 4.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/electric");
        (this.slotMotor = new InvSlotConsumableItemStack(this, "slotMotor", 10, new ItemStack[] { ItemName.crafting.getItemStack(CraftingItemType.electric_motor) })).setStackSizeLimit(1);
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
        this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 4).addManagedSlot(this.dischargeSlot));
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.updateDirections();
    }
    
    public void setFacing(final EnumFacing facing) {
        super.setFacing(facing);
        this.updateDirections();
    }
    
    private void updateDirections() {
        this.energy.setDirections((Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)this.getFacing())), Collections.emptySet());
    }
    
    @Override
    public int maxrequestkineticenergyTick(final EnumFacing directionFrom) {
        return this.drawKineticEnergy(directionFrom, Integer.MAX_VALUE, true);
    }
    
    @Override
    public int getConnectionBandwidth(final EnumFacing side) {
        if (side != this.getFacing()) {
            return 0;
        }
        return this.getMaxKU();
    }
    
    public int getMaxKU() {
        int counter = 0;
        final int a = this.getMaxKUForGUI() / 10;
        for (int i = 0; i < this.slotMotor.size(); ++i) {
            if (!this.slotMotor.isEmpty(i)) {
                counter += a;
            }
        }
        return counter;
    }
    
    public int getMaxKUForGUI() {
        return 1000;
    }
    
    @Override
    public int requestkineticenergy(final EnumFacing directionFrom, final int requestkineticenergy) {
        return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
    }
    
    @Override
    public int drawKineticEnergy(final EnumFacing side, final int request, final boolean simulate) {
        if (side != this.getFacing()) {
            return 0;
        }
        final int max = (int)Math.min(this.getMaxKU(), this.ku);
        final int out = Math.min(request, max);
        if (!simulate) {
            this.ku -= out;
            this.markDirty();
        }
        return out;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean newActive = false;
        if (1000.0 - this.ku > 1.0) {
            final double max = Math.min(1000.0 - this.ku, this.energy.getEnergy() * this.kuPerEU);
            this.energy.useEnergy(max / this.kuPerEU);
            this.ku += max;
            if (max > 0.0) {
                this.markDirty();
                newActive = true;
            }
        }
        this.setActive(newActive);
    }
    
    @Override
    public ContainerBase<TileEntityElectricKineticGenerator> getGuiContainer(final EntityPlayer player) {
        return new ContainerElectricKineticGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiElectricKineticGenertor((ContainerElectricKineticGenerator)this.getGuiContainer(player));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
}
