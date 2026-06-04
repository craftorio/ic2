// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiLiquidHeatExchanger;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerLiquidHeatExchanger;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.ref.FluidName;
import ic2.core.LiquidHeatExchangerManager;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumable;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;

@NotClassic
public class TileEntityLiquidHeatExchanger extends TileEntityHeatSourceInventory implements IHasGui, IUpgradableBlock
{
    private boolean newActive;
    public final FluidTank inputTank;
    public final FluidTank outputTank;
    public final InvSlotConsumable heatexchangerslots;
    public final InvSlotOutput hotoutputSlot;
    public final InvSlotOutput cooloutputSlot;
    public final InvSlotConsumableLiquid hotfluidinputSlot;
    public final InvSlotConsumableLiquid coolfluidinputSlot;
    public final InvSlotUpgrade upgradeSlot;
    protected final Fluids fluids;
    
    public TileEntityLiquidHeatExchanger() {
        this.fluids = this.addComponent(new Fluids(this));
        this.inputTank = this.fluids.addTankInsert("inputTank", 2000, Fluids.fluidPredicate(Recipes.liquidCooldownManager));
        this.outputTank = this.fluids.addTankExtract("outputTank", 2000);
        (this.heatexchangerslots = new InvSlotConsumableItemStack(this, "heatExchanger", 10, new ItemStack[] { ItemName.crafting.getItemStack(CraftingItemType.heat_conductor) })).setStackSizeLimit(1);
        this.hotoutputSlot = new InvSlotOutput(this, "hotOutputSlot", 1);
        this.cooloutputSlot = new InvSlotOutput(this, "outputSlot", 1);
        this.hotfluidinputSlot = new InvSlotConsumableLiquidByManager(this, "hotFluidInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, Recipes.liquidCooldownManager);
        this.coolfluidinputSlot = new InvSlotConsumableLiquidByTank(this, "coolFluidOutput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 3);
        this.newActive = false;
    }
    
    public static void init() {
        Recipes.liquidCooldownManager = new LiquidHeatExchangerManager(false);
        Recipes.liquidHeatupManager = new LiquidHeatExchangerManager(true);
        addCooldownRecipe("lava", FluidName.pahoehoe_lava.getName(), Math.round(20.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/fluidconversion/heatExchangerLava")));
        addBiDiRecipe(FluidName.hot_coolant.getName(), FluidName.coolant.getName(), Math.round(20.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/fluidconversion/heatExchangerHotCoolant")));
        addHeatupRecipe(FluidName.hot_water.getName(), "water", Math.round(1.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/fluidconversion/heatExchangerWater")));
    }
    
    public static void addBiDiRecipe(final String hotFluid, final String coldFluid, final int huPerMB) {
        addHeatupRecipe(hotFluid, coldFluid, huPerMB);
        addCooldownRecipe(hotFluid, coldFluid, huPerMB);
    }
    
    public static void addHeatupRecipe(final String hotFluid, final String coldFluid, final int huPerMB) {
        Recipes.liquidHeatupManager.addFluid(coldFluid, hotFluid, huPerMB);
    }
    
    public static void addCooldownRecipe(final String hotFluid, final String coldFluid, final int huPerMB) {
        Recipes.liquidCooldownManager.addFluid(hotFluid, coldFluid, huPerMB);
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.hotfluidinputSlot.processIntoTank((IFluidTank)this.inputTank, this.hotoutputSlot);
        this.coolfluidinputSlot.processFromTank((IFluidTank)this.outputTank, this.cooloutputSlot);
        this.newActive = (this.HeatBuffer > 0);
        if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
        }
        this.upgradeSlot.tick();
    }
    
    @Override
    public ContainerBase<TileEntityLiquidHeatExchanger> getGuiContainer(final EntityPlayer player) {
        return new ContainerLiquidHeatExchanger(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiLiquidHeatExchanger(new ContainerLiquidHeatExchanger(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public int gaugeLiquidScaled(final int i, final int tank) {
        switch (tank) {
            case 0: {
                if (this.inputTank.getFluidAmount() <= 0) {
                    return 0;
                }
                return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
            }
            case 1: {
                if (this.outputTank.getFluidAmount() <= 0) {
                    return 0;
                }
                return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
            }
            default: {
                return 0;
            }
        }
    }
    
    @Override
    public int getMaxHeatEmittedPerTick() {
        int count = 0;
        for (int i = 0; i < this.heatexchangerslots.size(); ++i) {
            if (!this.heatexchangerslots.isEmpty(i)) {
                count += 10;
            }
        }
        return count;
    }
    
    @Override
    protected int fillHeatBuffer(final int bufferspace) {
        if (bufferspace > 0) {
            final int AmountHotCoolant = this.inputTank.getFluidAmount();
            final int OutputTankFreeCap = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
            FluidStack draincoolant = null;
            if (OutputTankFreeCap == 0 || AmountHotCoolant == 0) {
                return 0;
            }
            final Fluid fluidInputTank = this.inputTank.getFluid().getFluid();
            Fluid fluidOutput = null;
            int hUper1mb = 0;
            if (Recipes.liquidCooldownManager.acceptsFluid(fluidInputTank)) {
                final ILiquidHeatExchangerManager.HeatExchangeProperty hep = Recipes.liquidCooldownManager.getHeatExchangeProperty(fluidInputTank);
                fluidOutput = hep.outputFluid;
                hUper1mb = hep.huPerMB;
            }
            if (fluidOutput == null) {
                return 0;
            }
            if (this.outputTank.getFluidAmount() > 0 && !this.outputTank.getFluid().getFluid().equals(fluidOutput)) {
                return 0;
            }
            final int mbtofillheatbuffer = bufferspace / hUper1mb;
            if (OutputTankFreeCap >= AmountHotCoolant) {
                if (mbtofillheatbuffer <= AmountHotCoolant) {
                    draincoolant = this.inputTank.drainInternal(mbtofillheatbuffer, false);
                }
                else {
                    draincoolant = this.inputTank.drainInternal(AmountHotCoolant, false);
                }
            }
            else if (mbtofillheatbuffer <= OutputTankFreeCap) {
                draincoolant = this.inputTank.drainInternal(mbtofillheatbuffer, false);
            }
            else {
                draincoolant = this.inputTank.drainInternal(OutputTankFreeCap * 20, false);
            }
            if (draincoolant != null) {
                this.inputTank.drainInternal(draincoolant.amount, true);
                this.outputTank.fillInternal(new FluidStack(fluidOutput, draincoolant.amount), true);
                return draincoolant.amount * hUper1mb;
            }
        }
        return 0;
    }
    
    public FluidTank getInputTank() {
        return this.inputTank;
    }
    
    public FluidTank getOutputTank() {
        return this.outputTank;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
    }
    
    @Override
    public double getEnergy() {
        return 40.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return true;
    }
}
