// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessable;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.block.TileEntityBlock;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import ic2.core.network.GuiSynced;
import ic2.core.block.comp.Redstone;
import ic2.core.profile.NotClassic;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

@NotClassic
public class TileEntityCentrifuge extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    protected final Redstone redstone;
    public static final short maxHeat = 5000;
    @GuiSynced
    public short heat;
    @GuiSynced
    public short workheat;
    
    public TileEntityCentrifuge() {
        super(48, 500, 3, 2);
        this.heat = 0;
        this.workheat = 5000;
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableGeneric(this, "input", 1, Recipes.centrifuge);
        this.redstone = this.addComponent(new Redstone(this));
    }
    
    public static void init() {
        Recipes.centrifuge = new BasicMachineRecipeManager();
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.heat = nbt.getShort("heat");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("heat", this.heat);
        return nbt;
    }
    
    public double getHeatRatio() {
        return this.heat / (double)this.workheat;
    }
    
    private static short min(final short a, final short b) {
        return (a <= b) ? a : b;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        final int energyPerHeat = 1;
        final int coolingPerTick = 1;
        boolean heating = false;
        if (this.energy.canUseEnergy(1.0)) {
            short heatRequested = -32768;
            final MachineRecipeResult<? extends IRecipeInput, ? extends Collection<ItemStack>, ? extends ItemStack> output = super.getOutput();
            if (output != null && !this.redstone.hasRedstoneInput()) {
                heatRequested = min((short)5000, output.getRecipe().getMetaData().getShort("minHeat"));
                if (this.heat > (this.workheat = heatRequested)) {
                    this.heat = heatRequested;
                }
            }
            else if (this.heat <= 5000 && this.redstone.hasRedstoneInput()) {
                heatRequested = 5000;
                this.workheat = heatRequested;
            }
            if (this.heat - 1 < heatRequested) {
                this.energy.useEnergy(1.0);
                heating = true;
            }
        }
        if (heating) {
            ++this.heat;
        }
        else {
            this.heat -= (short)Math.min(this.heat, 1);
        }
    }
    
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
        final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getOutput();
        if (ret != null) {
            if (ret.getRecipe().getMetaData() == null) {
                return null;
            }
            if (ret.getRecipe().getMetaData().getInteger("minHeat") > this.heat) {
                return null;
            }
        }
        return ret;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("heat".equals(name)) {
            return this.heat / (double)this.workheat;
        }
        return super.getGuiValue(name);
    }
}
