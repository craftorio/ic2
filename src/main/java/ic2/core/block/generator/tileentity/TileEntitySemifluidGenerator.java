// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import ic2.api.recipe.ISemiFluidFuelManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.SemiFluidFuelManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntitySemifluidGenerator extends TileEntityBaseGenerator
{
    public final InvSlotConsumableLiquid fluidSlot;
    public final InvSlotOutput outputSlot;
    @GuiSynced
    protected final FluidTank fluidTank;
    protected final Fluids fluids;
    
    public TileEntitySemifluidGenerator() {
        super(32.0, 1, 32000);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankInsert("fluid", 10000, Fluids.fluidPredicate(Recipes.semiFluidGenerator));
        this.fluidSlot = new InvSlotConsumableLiquidByManager(this, "fluidSlot", 1, Recipes.semiFluidGenerator);
        this.outputSlot = new InvSlotOutput(this, "output", 1);
    }
    
    public static void init() {
        Recipes.semiFluidGenerator = new SemiFluidFuelManager();
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidOil") > 0.0f) {
            addFuel("oil", 16L, 8L);
        }
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidFuel") > 0.0f) {
            addFuel("fuel", 128L, 32L);
        }
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiomass") > 0.0f) {
            addFuel("biomass", 8L, 8L);
        }
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBioethanol") > 0.0f) {
            addFuel("bio.ethanol", 128L, 32L);
        }
        if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiogas") > 0.0f) {
            addFuel("ic2biogas", 32L, 16L);
        }
        addFuel("ic2creosote", 3L, 8L);
    }
    
    public static void addFuel(final String fluidName, final long energyPerMb, final long energyPerTick) {
        Recipes.semiFluidGenerator.addFluid(fluidName, energyPerMb, energyPerTick);
    }
    
    public void updateEntityServer() {
        super.updateEntityServer();
        if (this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.outputSlot)) {
            this.markDirty();
        }
    }
    
    @Override
    public boolean gainEnergy() {
        if (this.isConverting()) {
            final double temp = Math.min(this.fuel, this.production);
            this.energy.addEnergy(temp);
            this.fuel -= (int)temp;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean needsFuel() {
        return this.fuel < this.production && this.energy.getFreeEnergy() >= this.production;
    }
    
    @Override
    public boolean gainFuel() {
        boolean dirty = false;
        final FluidStack ret = this.fluidTank.drain(Integer.MAX_VALUE, false);
        if (ret != null) {
            final ISemiFluidFuelManager.FuelProperty property = Recipes.semiFluidGenerator.getFuelProperty(ret.getFluid());
            int toBeConsumed = (property.energyPerMb >= property.energyPerTick) ? 1 : ((int)Math.ceil(property.energyPerTick / (double)property.energyPerMb));
            toBeConsumed = Math.min(toBeConsumed, ret.amount);
            if (property != null && ret.amount >= toBeConsumed) {
                this.fluidTank.drainInternal(toBeConsumed, true);
                this.production = (double)property.energyPerTick;
                this.fuel += (int)(toBeConsumed * property.energyPerMb);
                dirty = true;
            }
        }
        return dirty;
    }
    
    @Override
    public String getOperationSoundFile() {
        return "Generators/GeothermalLoop.ogg";
    }
}
