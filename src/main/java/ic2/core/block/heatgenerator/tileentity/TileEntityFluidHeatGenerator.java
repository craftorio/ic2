// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.heatgenerator.tileentity;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import ic2.api.recipe.IFluidHeatManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.heatgenerator.gui.GuiFluidHeatGenerator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.FluidHeatManager;
import ic2.core.block.TileEntityBlock;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.comp.Fluids;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;

@NotClassic
public class TileEntityFluidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
    public final InvSlotConsumableLiquid fluidSlot;
    public final InvSlotOutput outputSlot;
    @GuiSynced
    protected final FluidTank fluidTank;
    private short ticker;
    protected int burnAmount;
    protected int production;
    boolean newActive;
    protected final Fluids fluids;
    
    public TileEntityFluidHeatGenerator() {
        this.ticker = 0;
        this.burnAmount = 0;
        this.production = 0;
        this.newActive = false;
        this.fluidSlot = new InvSlotConsumableLiquidByManager(this, "fluidSlot", 1, Recipes.fluidHeatGenerator);
        this.outputSlot = new InvSlotOutput(this, "output", 1);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankInsert("fluidTank", 10000, Fluids.fluidPredicate(Recipes.semiFluidGenerator));
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.needsFluid()) {
            needsInvUpdate = this.gainFuel();
        }
        if (needsInvUpdate) {
            this.markDirty();
        }
        if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
        }
    }
    
    public boolean isConverting() {
        return this.getTankAmount() > 0 && this.HeatBuffer < this.getMaxHeatEmittedPerTick();
    }
    
    public static void init() {
        Recipes.fluidHeatGenerator = new FluidHeatManager();
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidOil") > 0.0f) {
            addFuel("oil", 10, Math.round(32.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidOil")));
        }
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidFuel") > 0.0f) {
            addFuel("fuel", 5, Math.round(768.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidFuel")));
        }
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiomass") > 0.0f) {
            addFuel("biomass", 20, Math.round(16.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBiomass")));
        }
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBioethanol") > 0.0f) {
            addFuel("bio.ethanol", 10, Math.round(32.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBioethanol")));
        }
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiogas") > 0.0f) {
            addFuel("ic2biogas", 10, Math.round(32.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBiogas")));
        }
    }
    
    public static void addFuel(final String fluidName, final int amount, final int heat) {
        Recipes.fluidHeatGenerator.addFluid(fluidName, amount, heat);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.fluidTank.readFromNBT(nbttagcompound.getCompoundTag("fluidTank"));
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        final NBTTagCompound fluidTankTag = new NBTTagCompound();
        this.fluidTank.writeToNBT(fluidTankTag);
        nbt.setTag("fluidTank", (NBTBase)fluidTankTag);
        return nbt;
    }
    
    @Override
    protected int fillHeatBuffer(final int maxAmount) {
        if (this.isConverting()) {
            if (this.ticker >= 19) {
                this.getFluidTank().drain(this.burnAmount, true);
                this.ticker = 0;
            }
            else {
                ++this.ticker;
            }
            this.newActive = true;
            return this.production;
        }
        this.newActive = false;
        return 0;
    }
    
    @Override
    public int getMaxHeatEmittedPerTick() {
        return this.calcHeatProduction();
    }
    
    @Override
    public ContainerBase<TileEntityFluidHeatGenerator> getGuiContainer(final EntityPlayer player) {
        return new ContainerFluidHeatGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiFluidHeatGenerator(new ContainerFluidHeatGenerator(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    protected int calcHeatProduction() {
        if (this.fluidTank.getFluid() != null && this.getFluidfromTank() != null) {
            final IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(this.getFluidfromTank());
            if (property != null) {
                return this.production = property.heat;
            }
        }
        return this.production = 0;
    }
    
    protected void calcBurnAmount() {
        if (this.getFluidfromTank() != null) {
            final IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(this.getFluidfromTank());
            if (property != null) {
                this.burnAmount = property.amount;
                return;
            }
        }
        this.burnAmount = 0;
    }
    
    public FluidTank getFluidTank() {
        return this.fluidTank;
    }
    
    public FluidStack getFluidStackfromTank() {
        return this.getFluidTank().getFluid();
    }
    
    public Fluid getFluidfromTank() {
        return this.getFluidStackfromTank().getFluid();
    }
    
    public int getTankAmount() {
        return this.getFluidTank().getFluidAmount();
    }
    
    public int gaugeLiquidScaled(final int i) {
        if (this.getFluidTank().getFluidAmount() <= 0) {
            return 0;
        }
        return this.getFluidTank().getFluidAmount() * i / this.getFluidTank().getCapacity();
    }
    
    public boolean needsFluid() {
        return this.getFluidTank().getFluidAmount() <= this.getFluidTank().getCapacity();
    }
    
    protected boolean gainFuel() {
        if (this.fluidTank.getFluid() != null) {
            this.calcHeatProduction();
            this.calcBurnAmount();
        }
        return this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.outputSlot);
    }
}
