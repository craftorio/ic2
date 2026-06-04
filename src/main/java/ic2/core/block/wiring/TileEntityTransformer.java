// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import java.util.Set;
import java.util.EnumSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityBlock;
import java.util.Collections;
import ic2.api.energy.EnergyNet;
import ic2.core.block.comp.Energy;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public abstract class TileEntityTransformer extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener
{
    private static final Mode defaultMode;
    private double inputFlow;
    private double outputFlow;
    private final int defaultTier;
    protected final Energy energy;
    private Mode configuredMode;
    private Mode transformMode;
    
    public TileEntityTransformer(final int tier) {
        this.inputFlow = 0.0;
        this.outputFlow = 0.0;
        this.configuredMode = TileEntityTransformer.defaultMode;
        this.transformMode = null;
        this.defaultTier = tier;
        this.energy = this.addComponent(new Energy(this, EnergyNet.instance.getPowerFromTier(tier) * 8.0, Collections.emptySet(), Collections.emptySet(), tier, tier, true).setMultiSource(true));
    }
    
    public String getType() {
        switch (this.energy.getSourceTier()) {
            case 1: {
                return "LV";
            }
            case 2: {
                return "MV";
            }
            case 3: {
                return "HV";
            }
            case 4: {
                return "EV";
            }
            default: {
                return "";
            }
        }
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        final int mode = nbt.getInteger("mode");
        if (mode >= 0 && mode < Mode.VALUES.length) {
            this.configuredMode = Mode.VALUES[mode];
        }
        else {
            this.configuredMode = TileEntityTransformer.defaultMode;
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("mode", this.configuredMode.ordinal());
        return nbt;
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            this.updateRedstone(true);
        }
    }
    
    public Mode getMode() {
        return this.configuredMode;
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        if (event >= 0 && event < Mode.VALUES.length) {
            this.configuredMode = Mode.VALUES[event];
            this.updateRedstone(false);
        }
        else if (event == 3) {}
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.updateRedstone(false);
    }
    
    private void updateRedstone(final boolean force) {
        assert !this.getWorld().isRemote;
        Mode newMode = null;
        switch (this.configuredMode) {
            case redstone: {
                newMode = (this.getWorld().isBlockPowered(this.pos) ? Mode.stepup : Mode.stepdown);
                break;
            }
            case stepdown:
            case stepup: {
                newMode = this.configuredMode;
                break;
            }
            default: {
                throw new RuntimeException("invalid mode: " + this.configuredMode);
            }
        }
        if (newMode != Mode.stepup && newMode != Mode.stepdown) {
            throw new RuntimeException("invalid mode: " + newMode);
        }
        this.energy.setEnabled(true);
        if (force || this.transformMode != newMode) {
            this.transformMode = newMode;
            this.setActive(this.isStepUp());
            if (this.isStepUp()) {
                this.energy.setSourceTier(this.defaultTier + 1);
                this.energy.setSinkTier(this.defaultTier);
                this.energy.setPacketOutput(1);
                this.energy.setDirections((Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)this.getFacing())), EnumSet.of(this.getFacing()));
            }
            else {
                this.energy.setSourceTier(this.defaultTier);
                this.energy.setSinkTier(this.defaultTier + 1);
                this.energy.setPacketOutput(4);
                this.energy.setDirections(EnumSet.of(this.getFacing()), (Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)this.getFacing())));
            }
            this.outputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier());
            this.inputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier());
        }
    }
    
    public void setFacing(final EnumFacing facing) {
        super.setFacing(facing);
        if (!this.getWorld().isRemote) {
            this.updateRedstone(true);
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final List<String> tooltip, final ITooltipFlag advanced) {
        super.addInformation(stack, tooltip, advanced);
        tooltip.add(String.format("%s %.0f %s %s %.0f %s", Localization.translate("ic2.item.tooltip.Low"), EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier()), Localization.translate("ic2.generic.text.EUt"), Localization.translate("ic2.item.tooltip.High"), EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier() + 1), Localization.translate("ic2.generic.text.EUt")));
    }
    
    @Override
    public ContainerBase<TileEntityTransformer> getGuiContainer(final EntityPlayer player) {
        return new ContainerTransformer(player, this, 219);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiTransformer(new ContainerTransformer(player, this, 219));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public double getinputflow() {
        if (!this.isStepUp()) {
            return this.inputFlow;
        }
        return this.outputFlow;
    }
    
    public double getoutputflow() {
        if (this.isStepUp()) {
            return this.inputFlow;
        }
        return this.outputFlow;
    }
    
    private boolean isStepUp() {
        return this.transformMode == Mode.stepup;
    }
    
    static {
        defaultMode = Mode.redstone;
    }
    
    public enum Mode
    {
        redstone, 
        stepdown, 
        stepup;
        
        static final Mode[] VALUES;
        
        static {
            VALUES = values();
        }
    }
}
