// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraft.world.World;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.invslot.InvSlot;
import ic2.core.IC2;
import ic2.core.network.GuiSynced;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.gui.dynamic.IGuiValueProvider;

public class TileEntityWaterGenerator extends TileEntityBaseRotorGenerator implements IGuiValueProvider
{
    private static final double energyMultiplier;
    private static final boolean allowAutomation;
    public final InvSlotConsumableLiquid fuelSlot;
    private static final int tickRate = 128;
    private int ticker;
    @GuiSynced
    public int water;
    public int microStorage;
    public int maxWater;
    
    public TileEntityWaterGenerator() {
        super(2.0, 1, 4, 2);
        this.ticker = IC2.random.nextInt(128);
        this.water = 0;
        this.microStorage = 0;
        this.maxWater = 2000;
        this.production = 2.0;
        this.fuelSlot = new InvSlotConsumableLiquidByList(this, "fuel", TileEntityWaterGenerator.allowAutomation ? InvSlot.Access.IO : InvSlot.Access.NONE, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, new Fluid[] { FluidRegistry.WATER });
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateWaterCount();
    }
    
    @Override
    public boolean gainFuel() {
        if (this.fuel + 500 > this.maxWater) {
            return false;
        }
        if (!this.fuelSlot.isEmpty()) {
            final ItemStack liquid = this.fuelSlot.consume(1);
            if (liquid == null) {
                return false;
            }
            this.fuel += 500;
            if (liquid.getItem().hasContainerItem(liquid)) {
                this.production = 1.0;
            }
            else {
                this.production = 2.0;
            }
            return true;
        }
        else {
            if (this.fuel > 0) {
                return false;
            }
            this.flowPower();
            this.production = this.microStorage / 100;
            this.microStorage -= (int)(this.production * 100.0);
            if (this.production > 0.0) {
                ++this.fuel;
                return true;
            }
            return false;
        }
    }
    
    @Override
    public boolean isConverting() {
        return this.fuel > 0;
    }
    
    @Override
    public boolean needsFuel() {
        return this.fuel <= this.maxWater;
    }
    
    public void flowPower() {
        if (++this.ticker % 128 == 0) {
            this.updateWaterCount();
        }
        this.water = (int)Math.round(this.water * TileEntityWaterGenerator.energyMultiplier);
        if (this.water > 0) {
            this.microStorage += this.water;
        }
    }
    
    public void updateWaterCount() {
        final World world = this.getWorld();
        int count = 0;
        for (int x = -1; x < 2; ++x) {
            for (int y = -1; y < 2; ++y) {
                for (int z = -1; z < 2; ++z) {
                    if (world.getBlockState(this.pos.add(x, y, z)).getMaterial() == Material.WATER) {
                        ++count;
                    }
                }
            }
        }
        this.water = count;
    }
    
    @Override
    public String getOperationSoundFile() {
        return "Generators/WatermillLoop.ogg";
    }
    
    @Override
    protected boolean delayActiveUpdate() {
        return true;
    }
    
    @Override
    protected boolean shouldRotorRotate() {
        return this.water > 0 || this.fuel > 0;
    }
    
    @Override
    protected float rotorSpeedFactor() {
        return (this.fuel > 0) ? 1.0f : (this.water / 25.0f);
    }
    
    @Override
    public double getGuiValue(final String name) {
        if (!"water".equals(name)) {
            throw new IllegalArgumentException("Unexpected value requested: " + name);
        }
        assert this.maxWater > 0;
        return this.fuel / (double)this.maxWater;
    }
    
    static {
        energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/water");
        allowAutomation = ConfigUtil.getBool(MainConfig.get(), "balance/watermillAutomation");
    }
}
