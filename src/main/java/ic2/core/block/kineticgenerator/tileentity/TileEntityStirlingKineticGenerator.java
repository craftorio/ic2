// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.kineticgenerator.gui.GuiStirlingKineticGenerator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ic2.api.energy.tile.IHeatSource;
import net.minecraft.util.EnumFacing;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.Recipes;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityStirlingKineticGenerator extends TileEntityInventory implements IKineticSource, IUpgradableBlock, IHasGui
{
    public FluidTank inputTank;
    public FluidTank outputTank;
    public InvSlotOutput hotoutputSlot;
    public InvSlotOutput cooloutputSlot;
    public InvSlotConsumableLiquidByTank hotfluidinputSlot;
    public InvSlotConsumableLiquidByManager coolfluidinputSlot;
    public InvSlotUpgrade upgradeSlot;
    private int heatbuffer;
    private final int maxHeatbuffer;
    private int kUBuffer;
    private final int maxkUBuffer;
    private boolean newActive;
    private int liquidHeatStored;
    protected final Fluids fluids;
    private static final int PARTS_KU = 3;
    private static final int PARTS_LIQUID = 1;
    private static final int PARTS_TOTAL = 4;
    
    public TileEntityStirlingKineticGenerator() {
        this.heatbuffer = 0;
        this.fluids = this.addComponent(new Fluids(this));
        this.inputTank = this.fluids.addTankInsert("inputTank", 2000, Fluids.fluidPredicate(Recipes.liquidHeatupManager.getSingleDirectionLiquidManager()));
        this.outputTank = this.fluids.addTankExtract("outputTank", 2000);
        this.hotoutputSlot = new InvSlotOutput(this, "hotOutputSlot", 1);
        this.cooloutputSlot = new InvSlotOutput(this, "outputSlot", 1);
        this.coolfluidinputSlot = new InvSlotConsumableLiquidByManager(this, "coolfluidinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, Recipes.liquidHeatupManager.getSingleDirectionLiquidManager());
        this.hotfluidinputSlot = new InvSlotConsumableLiquidByTank(this, "hotfluidoutputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 3);
        this.maxHeatbuffer = 1000;
        this.maxkUBuffer = 2000;
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.inputTank.readFromNBT(nbt.getCompoundTag("inputTank"));
        this.outputTank.readFromNBT(nbt.getCompoundTag("outputTank"));
        this.heatbuffer = nbt.getInteger("heatbuffer");
        this.kUBuffer = nbt.getInteger("kubuffer");
        this.liquidHeatStored = nbt.getInteger("liquidHeatStored");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        final NBTTagCompound inputTankTag = new NBTTagCompound();
        this.inputTank.writeToNBT(inputTankTag);
        nbt.setTag("inputTank", (NBTBase)inputTankTag);
        final NBTTagCompound outputTankTag = new NBTTagCompound();
        this.outputTank.writeToNBT(outputTankTag);
        nbt.setTag("outputTank", (NBTBase)outputTankTag);
        nbt.setInteger("heatbuffer", this.heatbuffer);
        nbt.setInteger("kUBuffer", this.kUBuffer);
        nbt.setInteger("liquidHeatStored", this.liquidHeatStored);
        return nbt;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.coolfluidinputSlot.processIntoTank((IFluidTank)this.inputTank, this.cooloutputSlot);
        this.hotfluidinputSlot.processFromTank((IFluidTank)this.outputTank, this.hotoutputSlot);
        if (this.heatbuffer < this.maxHeatbuffer) {
            this.heatbuffer += this.drawHu(this.maxHeatbuffer - this.heatbuffer);
        }
        this.newActive = false;
        if (this.inputTank.getFluidAmount() > 0 && this.outputTank.getFluidAmount() < this.outputTank.getCapacity() && Recipes.liquidHeatupManager.getSingleDirectionLiquidManager().acceptsFluid(this.inputTank.getFluid().getFluid()) && this.kUBuffer < this.maxkUBuffer) {
            final ILiquidHeatExchangerManager.HeatExchangeProperty property = Recipes.liquidHeatupManager.getHeatExchangeProperty(this.inputTank.getFluid().getFluid());
            if (this.outputTank.getFluid() == null || new FluidStack(property.outputFluid, 0).isFluidEqual(this.outputTank.getFluid())) {
                int heatbufferToUse = this.heatbuffer / 4;
                heatbufferToUse = Math.min(heatbufferToUse, (Math.min(this.outputTank.getCapacity() - this.outputTank.getFluidAmount(), this.inputTank.getFluidAmount()) * property.huPerMB - this.liquidHeatStored) / 1);
                heatbufferToUse = Math.min(heatbufferToUse, (this.maxkUBuffer - this.kUBuffer) / 3);
                if (heatbufferToUse > 0) {
                    this.kUBuffer += heatbufferToUse * 3 * 4;
                    this.liquidHeatStored += heatbufferToUse * 1;
                    this.heatbuffer -= heatbufferToUse * 4;
                    this.newActive = true;
                }
                if (this.liquidHeatStored >= property.huPerMB) {
                    int mbToConvert = this.liquidHeatStored / property.huPerMB;
                    mbToConvert = this.inputTank.drainInternal(mbToConvert, false).amount;
                    mbToConvert = this.outputTank.fillInternal(new FluidStack(property.outputFluid, mbToConvert), false);
                    this.liquidHeatStored -= mbToConvert * property.huPerMB;
                    this.inputTank.drainInternal(mbToConvert, true);
                    this.outputTank.fillInternal(new FluidStack(property.outputFluid, mbToConvert), true);
                }
            }
        }
        if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
        }
        this.upgradeSlot.tick();
    }
    
    private int drawHu(final int amount) {
        if (amount <= 0) {
            return 0;
        }
        final World world = this.getWorld();
        int tmpAmount = amount;
        for (final EnumFacing dir : EnumFacing.VALUES) {
            if (dir != this.getFacing()) {
                final TileEntity te = world.getTileEntity(this.pos.offset(dir));
                if (te instanceof IHeatSource) {
                    final IHeatSource hs = (IHeatSource)te;
                    final int request = hs.drawHeat(dir.getOpposite(), tmpAmount, true);
                    if (request > 0) {
                        tmpAmount -= hs.drawHeat(dir.getOpposite(), request, false);
                        if (tmpAmount <= 0) {
                            break;
                        }
                    }
                }
            }
        }
        return amount - tmpAmount;
    }
    
    @Override
    public int maxrequestkineticenergyTick(final EnumFacing directionFrom) {
        return Math.min(this.kUBuffer, this.getConnectionBandwidth(directionFrom));
    }
    
    @Override
    public int getConnectionBandwidth(final EnumFacing side) {
        if (side != this.getFacing()) {
            return 0;
        }
        return this.maxkUBuffer;
    }
    
    @Override
    public int requestkineticenergy(final EnumFacing directionFrom, final int requestkineticenergy) {
        return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
    }
    
    @Override
    public int drawKineticEnergy(final EnumFacing side, int request, final boolean simulate) {
        if (side != this.getFacing()) {
            return 0;
        }
        if (request > this.kUBuffer) {
            request = this.kUBuffer;
        }
        if (!simulate) {
            this.kUBuffer -= request;
        }
        return request;
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
    
    public FluidTank getInputTank() {
        return this.inputTank;
    }
    
    public FluidTank getOutputTank() {
        return this.outputTank;
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerStirlingKineticGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiStirlingKineticGenerator(new ContainerStirlingKineticGenerator(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
}
