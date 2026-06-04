// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.kineticgenerator.gui.GuiSteamKineticGenerator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import ic2.core.ExplosionIC2;
import ic2.core.util.LiquidUtil;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.TileEntityBlock;
import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotUpgrade;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;
import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntitySteamKineticGenerator extends TileEntityInventory implements IKineticSource, IHasGui, IUpgradableBlock
{
    protected final FluidTank steamTank;
    protected final FluidTank distilledWaterTank;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotConsumable turbineSlot;
    private static final float outputModifier;
    private int kUoutput;
    private boolean ventingSteam;
    private boolean throttled;
    private boolean isTurbineFilledWithWater;
    private int condensationProgress;
    private int updateTicker;
    protected final Fluids fluids;
    
    public TileEntitySteamKineticGenerator() {
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 1);
        this.turbineSlot = new InvSlotConsumableItemStack(this, "Turbineslot", 1, new ItemStack[] { ItemName.crafting.getItemStack(CraftingItemType.steam_turbine) });
        this.isTurbineFilledWithWater = false;
        this.condensationProgress = 0;
        this.updateTicker = IC2.random.nextInt(this.getTickRate());
        this.turbineSlot.setStackSizeLimit(1);
        this.fluids = this.addComponent(new Fluids(this));
        this.steamTank = this.fluids.addTankInsert("steamTank", 21000, Fluids.fluidPredicate(FluidName.steam.getInstance(), FluidName.superheated_steam.getInstance()));
        this.distilledWaterTank = this.fluids.addTank("distilledWaterTank", 1000, Fluids.fluidPredicate(FluidName.distilled_water.getInstance(), FluidRegistry.WATER));
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.distilledWaterTank.getCapacity() - this.distilledWaterTank.getFluidAmount() >= 1 && this.isTurbineFilledWithWater) {
            this.isTurbineFilledWithWater = false;
        }
        if (this.steamTank.getFluidAmount() > 0 && !this.isTurbineFilledWithWater && !this.turbineSlot.isEmpty()) {
            if (!this.getActive()) {
                this.setActive(true);
                needsInvUpdate = true;
            }
            final boolean hotSteam = this.steamTank.getFluid().getFluid() == FluidName.superheated_steam.getInstance();
            final boolean turbineDoneWork = this.turbineDoWork(hotSteam);
            if (this.updateTicker++ >= this.getTickRate()) {
                if (turbineDoneWork) {
                    this.turbineSlot.damage(hotSteam ? 1 : 2, false);
                }
                this.updateTicker = 0;
            }
        }
        else if (this.getActive()) {
            this.setActive(false);
            needsInvUpdate = true;
            this.kUoutput = 0;
        }
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        if (needsInvUpdate) {
            super.markDirty();
        }
    }
    
    private int handleSteam(final boolean hotSteam) {
        final int amount = this.steamTank.getFluidAmount();
        assert amount > 0;
        this.steamTank.drainInternal(amount, true);
        final int KUWorkbuffer = amount * 2 * (hotSteam ? 2 : 1);
        if (hotSteam) {
            this.outputSteam(amount, true);
        }
        else {
            final int condensation = amount / 10;
            this.condensationProgress += condensation;
            this.outputSteam(amount - condensation, false);
        }
        return KUWorkbuffer;
    }
    
    private boolean turbineDoWork(final boolean hotSteam) {
        final int rawOutput = this.handleSteam(hotSteam);
        final int waterAmount = this.distilledWaterTank.getFluidAmount();
        float throttle;
        if (waterAmount == 0) {
            this.throttled = false;
            throttle = 1.0f;
        }
        else {
            this.throttled = true;
            throttle = 1.0f - waterAmount / (float)this.distilledWaterTank.getCapacity();
        }
        this.kUoutput = (int)(rawOutput * throttle * TileEntitySteamKineticGenerator.outputModifier);
        if (this.condensationProgress >= 100) {
            if (this.distilledWaterTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 1), false) == 1) {
                this.condensationProgress -= 100;
                this.distilledWaterTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 1), true);
            }
            else {
                this.isTurbineFilledWithWater = true;
            }
        }
        return this.kUoutput > 0;
    }
    
    private void outputSteam(int amount, final boolean hotSteam) {
        final World world = this.getWorld();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity te = world.getTileEntity(this.pos.offset(dir));
            if (te instanceof TileEntityCondenser || (hotSteam && te instanceof TileEntitySteamKineticGenerator)) {
                final int transAmount = LiquidUtil.fillTile(te, dir.getOpposite(), new FluidStack(FluidName.steam.getInstance(), amount), false);
                if (transAmount > 0) {
                    amount -= transAmount;
                    if (amount <= 0) {
                        break;
                    }
                }
            }
        }
        if (amount > 0) {
            this.ventingSteam = true;
            if (world.rand.nextInt(10) == 0) {
                new ExplosionIC2(world, null, this.pos, 1, 1.0f, ExplosionIC2.Type.Heat).doExplosion();
            }
        }
        else {
            this.ventingSteam = false;
        }
    }
    
    public int getKUoutput() {
        return this.kUoutput;
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.condensationProgress = nbt.getInteger("condensationprogress");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("condensationprogress", this.condensationProgress);
        return nbt;
    }
    
    @Override
    public ContainerBase<TileEntitySteamKineticGenerator> getGuiContainer(final EntityPlayer player) {
        return new ContainerSteamKineticGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiSteamKineticGenerator(new ContainerSteamKineticGenerator(player, this));
    }
    
    @Override
    public int maxrequestkineticenergyTick(final EnumFacing directionFrom) {
        return this.getConnectionBandwidth(directionFrom);
    }
    
    @Override
    public int getConnectionBandwidth(final EnumFacing side) {
        return (side == this.getFacing()) ? this.kUoutput : 0;
    }
    
    @Override
    public int requestkineticenergy(final EnumFacing directionFrom, final int requestkineticenergy) {
        return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
    }
    
    @Override
    public int drawKineticEnergy(final EnumFacing side, final int request, final boolean simulate) {
        return (side == this.getFacing()) ? this.kUoutput : 0;
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public int gaugeLiquidScaled(final int i, final int tank) {
        if (tank == 0 && this.distilledWaterTank.getFluidAmount() > 0) {
            return this.distilledWaterTank.getFluidAmount() * i / this.distilledWaterTank.getCapacity();
        }
        return 0;
    }
    
    @Override
    public double getEnergy() {
        return 0.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return false;
    }
    
    public int getDistilledWaterTankFill() {
        return this.distilledWaterTank.getFluidAmount();
    }
    
    public FluidTank getDistilledWaterTank() {
        return this.distilledWaterTank;
    }
    
    public boolean hasTurbine() {
        return !this.turbineSlot.isEmpty();
    }
    
    public boolean isVentingSteam() {
        return this.ventingSteam;
    }
    
    public boolean isThrottled() {
        return this.throttled;
    }
    
    public boolean isTurbineBlockedByWater() {
        return this.isTurbineFilledWithWater;
    }
    
    public int getTickRate() {
        return 20;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
    }
    
    static {
        outputModifier = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/steam");
    }
}
