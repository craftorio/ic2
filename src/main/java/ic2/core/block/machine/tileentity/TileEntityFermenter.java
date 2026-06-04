// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiFermenter;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerFermenter;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.recipe.IFermenterRecipeManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import ic2.api.energy.tile.IHeatSource;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.ref.FluidName;
import ic2.core.recipe.FermenterRecipeManager;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.TileEntityBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityFermenter extends TileEntityInventory implements IHasGui, IGuiValueProvider, IUpgradableBlock
{
    private final FluidTank inputTank;
    private final FluidTank outputTank;
    public final InvSlotConsumableLiquidByManager fluidInputCellInSlot;
    public final InvSlotConsumableLiquidByTank fluidOutputCellInSlot;
    public final InvSlotOutput fluidInputCellOutSlot;
    public final InvSlotOutput fluidOutputCellOutSlot;
    public final InvSlotOutput fertiliserSlot;
    public final InvSlotUpgrade upgradeSlot;
    protected final Fluids fluids;
    private int heatBuffer;
    public int progress;
    private final int maxProgress;
    private boolean newActive;
    
    public TileEntityFermenter() {
        this.heatBuffer = 0;
        this.progress = 0;
        this.maxProgress = ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/biomass_per_fertilizier");
        this.newActive = false;
        this.fluids = this.addComponent(new Fluids(this));
        this.outputTank = this.fluids.addTankExtract("output", 2000);
        this.inputTank = this.fluids.addTankInsert("input", 10000, Fluids.fluidPredicate(Recipes.fermenter));
        this.fluidInputCellOutSlot = new InvSlotOutput(this, "biomassOutput", 1);
        this.fluidOutputCellOutSlot = new InvSlotOutput(this, "biogassOutput", 1);
        this.fertiliserSlot = new InvSlotOutput(this, "output", 1);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 2);
        this.fluidOutputCellInSlot = new InvSlotConsumableLiquidByTank(this, "biogasInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
        this.fluidInputCellInSlot = new InvSlotConsumableLiquidByManager(this, "biomassInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, Recipes.fermenter);
    }
    
    public static void init() {
        (Recipes.fermenter = new FermenterRecipeManager()).addRecipe(FluidName.biomass.getName(), ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/need_amount_biomass_per_run"), ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/hU_per_run"), FluidName.biogas.getName(), ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/output_amount_biogas_per_run"));
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.inputTank.readFromNBT(nbttagcompound.getCompoundTag("inputTank"));
        this.outputTank.readFromNBT(nbttagcompound.getCompoundTag("outputTank"));
        this.progress = nbttagcompound.getInteger("progress");
        this.heatBuffer = nbttagcompound.getInteger("heatBuffer");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("inputTank", (NBTBase)this.inputTank.writeToNBT(new NBTTagCompound()));
        nbt.setTag("outputTank", (NBTBase)this.outputTank.writeToNBT(new NBTTagCompound()));
        nbt.setInteger("progress", this.progress);
        nbt.setInteger("heatBuffer", this.heatBuffer);
        return nbt;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.fluidInputCellInSlot.processIntoTank((IFluidTank)this.inputTank, this.fluidInputCellOutSlot);
        this.fluidOutputCellInSlot.processFromTank((IFluidTank)this.outputTank, this.fluidOutputCellOutSlot);
        this.newActive = this.work();
        if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
        }
        this.upgradeSlot.tick();
    }
    
    private boolean work() {
        if (this.progress >= this.maxProgress) {
            this.fertiliserSlot.add(ItemName.crop_res.getItemStack(CropResItemType.fertilizer));
            this.progress = 0;
        }
        final EnumFacing dir = this.getFacing();
        final TileEntity te = this.getWorld().getTileEntity(this.pos.offset(dir));
        if (te instanceof IHeatSource && this.inputTank.getFluid() != null) {
            final IFermenterRecipeManager.FermentationProperty fp = Recipes.fermenter.getFermentationInformation(this.inputTank.getFluid().getFluid());
            if (fp != null && this.inputTank.getFluidAmount() >= fp.inputAmount && fp.outputAmount <= this.outputTank.getCapacity() - this.outputTank.getFluidAmount()) {
                this.heatBuffer += ((IHeatSource)te).drawHeat(dir.getOpposite(), 100, false);
                if (this.heatBuffer >= fp.heat) {
                    this.heatBuffer -= fp.heat;
                    this.inputTank.drainInternal(fp.inputAmount, true);
                    this.outputTank.fillInternal(fp.getOutput(), true);
                    this.progress += fp.inputAmount;
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ContainerBase<TileEntityFermenter> getGuiContainer(final EntityPlayer player) {
        return new ContainerFermenter(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiFermenter(new ContainerFermenter(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("heat".equals(name)) {
            if (this.heatBuffer == 0) {
                return 0.0;
            }
            double maxHeatBuff = ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/hU_per_run");
            if (this.inputTank.getFluid() != null) {
                final IFermenterRecipeManager.FermentationProperty fp = Recipes.fermenter.getFermentationInformation(this.inputTank.getFluid().getFluid());
                if (fp != null) {
                    maxHeatBuff = fp.heat;
                }
            }
            return this.heatBuffer / maxHeatBuff;
        }
        else {
            if ("progress".equals(name)) {
                return (this.progress == 0) ? 0.0 : (this.progress / (double)this.maxProgress);
            }
            throw new IllegalArgumentException("Invalid GUI value: " + name);
        }
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
    
    public FluidTank getInputTank() {
        return this.inputTank;
    }
    
    public FluidTank getOutputTank() {
        return this.outputTank;
    }
    
    @Override
    public double getEnergy() {
        return 40.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return true;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
    }
}
