// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import ic2.core.util.BiomeUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiSolarDestiller;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.ref.FluidName;
import ic2.core.IC2;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlot;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntitySolarDestiller extends TileEntityInventory implements IHasGui, IUpgradableBlock
{
    public final FluidTank inputTank;
    public final FluidTank outputTank;
    private int tickrate;
    private int updateTicker;
    private float skyLight;
    public final InvSlotOutput wateroutputSlot;
    public final InvSlotOutput destiwateroutputSlott;
    public final InvSlotConsumableLiquidByList waterinputSlot;
    public final InvSlotConsumableLiquidByTank destiwaterinputSlot;
    public final InvSlotUpgrade upgradeSlot;
    protected final Fluids fluids;
    
    public TileEntitySolarDestiller() {
        this.fluids = this.addComponent(new Fluids(this));
        this.inputTank = this.fluids.addTankInsert("inputTank", 10000, Fluids.fluidPredicate(FluidRegistry.WATER));
        this.outputTank = this.fluids.addTankExtract("outputTank", 10000);
        this.waterinputSlot = new InvSlotConsumableLiquidByList(this, "waterInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, new Fluid[] { FluidRegistry.WATER });
        this.destiwaterinputSlot = new InvSlotConsumableLiquidByTank(this, "destilledWaterInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
        this.wateroutputSlot = new InvSlotOutput(this, "waterOutput", 1);
        this.destiwateroutputSlott = new InvSlotOutput(this, "destilledWaterOutput", 1);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 3);
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.tickrate = this.getTickRate();
        this.updateTicker = IC2.random.nextInt(this.tickrate);
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.waterinputSlot.processIntoTank((IFluidTank)this.inputTank, this.wateroutputSlot);
        if (++this.updateTicker >= this.tickrate) {
            this.updateSunVisibility();
            if (this.canWork()) {
                this.inputTank.drainInternal(1, true);
                this.outputTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 1), true);
            }
            this.updateTicker = 0;
        }
        this.destiwaterinputSlot.processFromTank((IFluidTank)this.outputTank, this.destiwateroutputSlott);
        this.upgradeSlot.tick();
    }
    
    public void updateSunVisibility() {
        this.skyLight = TileEntitySolarGenerator.getSkyLight(this.getWorld(), this.pos.up());
    }
    
    @Override
    public ContainerBase<TileEntitySolarDestiller> getGuiContainer(final EntityPlayer player) {
        return new ContainerSolarDestiller(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiSolarDestiller(new ContainerSolarDestiller(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public int getTickRate() {
        final Biome biome = BiomeUtil.getBiome(this.getWorld(), this.pos);
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT)) {
            return 36;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)) {
            return 144;
        }
        return 72;
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
    
    public boolean canWork() {
        return this.inputTank.getFluidAmount() > 0 && this.outputTank.getFluidAmount() < this.outputTank.getCapacity() && this.skyLight > 0.5;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing);
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
