// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiSteamGenerator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.energy.tile.IHeatSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import ic2.core.ExplosionIC2;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.LiquidUtil;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.util.BiomeUtil;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.profile.NotClassic;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntitySteamGenerator extends TileEntityInventory implements IHasGui, IGuiValueProvider, INetworkClientTileEntityEventListener
{
    private static final float maxHeat = 500.0f;
    private static final float heatPerHu = 5.0E-4f;
    private static final float coolingPerMb = 0.1f;
    private static final float maxCooling = 2.0f;
    private static final int maxHuInput = 1200;
    private static final int maxCalcification = 100000;
    private static final int steamExpansion = 100;
    private static final float epsilon = 1.0E-4f;
    private int heatInput;
    private int inputMB;
    public final FluidTank waterTank;
    private int calcification;
    private int outputMB;
    private outputType outputFluid;
    private float systemHeat;
    private int pressure;
    private boolean newActive;
    protected final Fluids fluids;
    
    public TileEntitySteamGenerator() {
        this.heatInput = 0;
        this.inputMB = 0;
        this.calcification = 0;
        this.outputMB = 0;
        this.outputFluid = outputType.NONE;
        this.pressure = 0;
        this.newActive = false;
        this.fluids = this.addComponent(new Fluids(this));
        this.waterTank = this.fluids.addTankInsert("waterTank", 10000, Fluids.fluidPredicate(FluidRegistry.WATER, FluidName.distilled_water.getInstance()));
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.inputMB = nbttagcompound.getInteger("inputmb");
        this.pressure = nbttagcompound.getInteger("pressurevalve");
        this.systemHeat = nbttagcompound.getFloat("systemheat");
        this.calcification = nbttagcompound.getInteger("calcification");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("inputmb", this.inputMB);
        nbt.setInteger("pressurevalve", this.pressure);
        nbt.setFloat("systemheat", this.systemHeat);
        nbt.setInteger("calcification", this.calcification);
        return nbt;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.systemHeat = Math.max(this.systemHeat, (float)BiomeUtil.getBiomeTemperature(this.getWorld(), this.pos));
        if (this.isCalcified()) {
            if (this.getActive()) {
                this.setActive(false);
            }
        }
        else {
            this.newActive = this.work();
            if (this.getActive() != this.newActive) {
                this.setActive(this.newActive);
            }
        }
        if (!this.getActive()) {
            this.cooldown(0.01f);
        }
    }
    
    private boolean work() {
        this.heatInput = this.requestHeat(1200);
        if (this.heatInput <= 0) {
            return false;
        }
        assert this.heatInput <= 1200;
        this.outputMB = 0;
        this.outputFluid = outputType.NONE;
        if (this.waterTank.getFluid() == null || this.waterTank.getFluidAmount() <= 0 || this.inputMB <= 0) {
            this.heatup((float)this.heatInput);
            return true;
        }
        final Fluid inputFluid = this.waterTank.getFluid().getFluid();
        final boolean hasDistilledWater = inputFluid == FluidName.distilled_water.getInstance();
        final int maxAmount = Math.min(this.inputMB, this.waterTank.getFluidAmount());
        final float hUneeded = 100.0f + this.pressure / 220.0f * 100.0f;
        final float targetTemp = 100.0f + this.pressure / 220.0f * 100.0f * 2.74f;
        float reqHeat = targetTemp - this.systemHeat;
        float remainingHuInput = (float)this.heatInput;
        if (reqHeat > 1.0E-4f) {
            final int heatReq = (int)Math.ceil(reqHeat / 5.0E-4f);
            if (this.heatInput <= heatReq) {
                this.heatup((float)this.heatInput);
                if (this.pressure == 0 && this.systemHeat < 99.9999f) {
                    this.outputMB = maxAmount;
                    this.outputFluid = (hasDistilledWater ? outputType.DISTILLEDWATER : outputType.WATER);
                    final int transferred = LiquidUtil.distribute(this, new FluidStack(inputFluid, maxAmount), false);
                    if (transferred > 0) {
                        this.waterTank.drainInternal(transferred, true);
                    }
                }
                return true;
            }
            this.heatup((float)heatReq);
            remainingHuInput -= heatReq;
            reqHeat = targetTemp - this.systemHeat;
        }
        assert this.systemHeat >= targetTemp - 1.0E-4f;
        assert this.systemHeat >= 99.9999f;
        final float availableSystemHu = Math.min(-reqHeat / 5.0E-4f, (float)(1200 - this.heatInput));
        int totalAmount;
        final int activeAmount = totalAmount = Math.min(maxAmount, (int)((remainingHuInput + availableSystemHu) / hUneeded));
        remainingHuInput -= activeAmount * hUneeded;
        if (remainingHuInput < 0.0f) {
            this.cooldown(-remainingHuInput * 5.0E-4f);
            reqHeat = targetTemp - this.systemHeat;
        }
        if (reqHeat <= -0.1001f) {
            int coolingAmount = Math.min(maxAmount, (int)(-reqHeat / 0.1f));
            coolingAmount = Math.min(coolingAmount, (int)Math.ceil(20.0));
            assert coolingAmount >= 0;
            this.cooldown(coolingAmount * 0.1f);
            totalAmount = Math.max(activeAmount, coolingAmount);
        }
        if (remainingHuInput > 0.0f) {
            this.heatup(remainingHuInput);
        }
        if (totalAmount <= 0) {
            return true;
        }
        if (!hasDistilledWater) {
            this.calcification += totalAmount;
        }
        this.waterTank.drainInternal(totalAmount, true);
        if (activeAmount <= 0) {
            return true;
        }
        this.outputMB = activeAmount * 100;
        Fluid output;
        if (this.systemHeat >= 373.9999f) {
            output = FluidName.superheated_steam.getInstance();
            this.outputFluid = outputType.SUPERHEATEDSTEAM;
        }
        else {
            output = FluidName.steam.getInstance();
            this.outputFluid = outputType.STEAM;
        }
        final int transferred2 = LiquidUtil.distribute(this, new FluidStack(output, this.outputMB), false);
        final int remaining = this.outputMB - transferred2;
        if (remaining > 0) {
            final World world = this.getWorld();
            if (world.rand.nextInt(10) == 0) {
                new ExplosionIC2(world, null, this.pos, 1, 1.0f, ExplosionIC2.Type.Heat).doExplosion();
            }
            else if (remaining >= 100) {
                this.waterTank.fillInternal(new FluidStack(inputFluid, remaining / 100), true);
            }
        }
        return true;
    }
    
    private void heatup(final float heatinput) {
        assert heatinput >= -1.0E-4f;
        this.systemHeat += heatinput * 5.0E-4f;
        if (this.systemHeat > 500.0f) {
            final World world = this.getWorld();
            world.setBlockToAir(this.pos);
            new ExplosionIC2(world, null, this.pos, 10, 0.01f, ExplosionIC2.Type.Heat).doExplosion();
        }
    }
    
    private void cooldown(final float cool) {
        assert cool >= -1.0E-4f;
        this.systemHeat = Math.max(this.systemHeat - cool, (float)BiomeUtil.getBiomeTemperature(this.getWorld(), this.pos));
    }
    
    private int requestHeat(final int requestHeat) {
        final World world = this.getWorld();
        int targetHeat = requestHeat;
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity target = world.getTileEntity(this.pos.offset(dir));
            if (target instanceof IHeatSource) {
                final IHeatSource hs = (IHeatSource)target;
                final int request = hs.drawHeat(dir.getOpposite(), targetHeat, true);
                if (request > 0) {
                    targetHeat -= hs.drawHeat(dir.getOpposite(), request, false);
                    if (targetHeat == 0) {
                        return requestHeat;
                    }
                }
            }
        }
        return requestHeat - targetHeat;
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        if (event > 2000 || event < -2000) {
            if (event > 2000) {
                this.pressure = Math.min(this.pressure + (event - 2000), 300);
            }
            if (event < -2000) {
                this.pressure = Math.max(this.pressure + (event + 2000), 0);
            }
        }
        else {
            this.inputMB = Math.max(Math.min(this.inputMB + event, 1000), 0);
        }
    }
    
    public int gaugeLiquidScaled(final int i, final int tank) {
        if (tank != 0) {
            return 0;
        }
        if (this.waterTank.getFluidAmount() <= 0) {
            return 0;
        }
        return this.waterTank.getFluidAmount() * i / this.waterTank.getCapacity();
    }
    
    @Override
    public ContainerBase<TileEntitySteamGenerator> getGuiContainer(final EntityPlayer player) {
        return new ContainerSteamGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiSteamGenerator(new ContainerSteamGenerator(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("heat".equals(name)) {
            return (this.systemHeat == 0.0f) ? 0.0 : (this.systemHeat / 500.0);
        }
        if ("calcification".equals(name)) {
            return (this.calcification == 0) ? 0.0 : (this.calcification / 100000.0);
        }
        throw new IllegalArgumentException();
    }
    
    public int getOutputMB() {
        return this.outputMB;
    }
    
    public int getInputMB() {
        return this.inputMB;
    }
    
    public int getHeatInput() {
        return this.heatInput;
    }
    
    public int getPressure() {
        return this.pressure;
    }
    
    public float getSystemHeat() {
        return Math.round(this.systemHeat * 10.0f) / 10.0f;
    }
    
    public float getCalcification() {
        return Math.round(this.calcification / 100000.0f * 100.0f * 100.0f) / 100.0f;
    }
    
    public boolean isCalcified() {
        return this.calcification >= 100000;
    }
    
    public String getOutputFluidName() {
        return this.outputFluid.getName();
    }
    
    private enum outputType
    {
        NONE(""), 
        WATER("ic2.SteamGenerator.output.water"), 
        DISTILLEDWATER("ic2.SteamGenerator.output.destiwater"), 
        STEAM("ic2.SteamGenerator.output.steam"), 
        SUPERHEATEDSTEAM("ic2.SteamGenerator.output.hotsteam");
        
        private final String name;
        
        private outputType(final String name) {
            this.name = name;
        }
        
        public String getName() {
            return this.name;
        }
    }
}
