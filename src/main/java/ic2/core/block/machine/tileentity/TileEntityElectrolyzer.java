// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.LiquidUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.FluidRegistry;
import ic2.core.recipe.ElectrolyzerRecipeManager;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.TileEntityBlock;
import ic2.core.IC2;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotUpgrade;
import net.minecraftforge.fluids.FluidTank;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.core.ref.TeBlock;
import ic2.core.gui.CustomGauge;
import ic2.core.IHasGui;
import ic2.api.upgrade.IUpgradableBlock;

@TeBlock.Delegated(current = TileEntityElectrolyzer.class, old = TileEntityClassicElectrolyzer.class)
public class TileEntityElectrolyzer extends TileEntityElectricMachine implements IUpgradableBlock, IHasGui, CustomGauge.IGaugeRatioProvider
{
    protected int progress;
    protected IElectrolyzerRecipeManager.ElectrolyzerRecipe recipe;
    protected FluidTank input;
    public final InvSlotUpgrade upgradeSlot;
    protected final Fluids fluids;
    
    public static Class<? extends TileEntityInventory> delegate() {
        return (Class<? extends TileEntityInventory>)(IC2.version.isClassic() ? TileEntityClassicElectrolyzer.class : TileEntityElectrolyzer.class);
    }
    
    public TileEntityElectrolyzer() {
        super(32000, 2);
        this.progress = 0;
        this.recipe = null;
        this.fluids = this.addComponent(new Fluids(this));
        this.input = this.fluids.addTankInsert("input", 8000, Fluids.fluidPredicate(Recipes.electrolyzer));
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgradeSlot", 4);
    }
    
    public static void init() {
        (Recipes.electrolyzer = new ElectrolyzerRecipeManager()).addRecipe(FluidRegistry.WATER.getName(), 40, 32, new IElectrolyzerRecipeManager.ElectrolyzerOutput(FluidName.hydrogen.getName(), 26, EnumFacing.DOWN), new IElectrolyzerRecipeManager.ElectrolyzerOutput(FluidName.oxygen.getName(), 13, EnumFacing.UP));
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.progress = nbt.getInteger("progress");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", this.progress);
        return nbt;
    }
    
    public void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.canOperate()) {
            assert this.recipe != null;
            this.setActive(true);
            this.energy.useEnergy(this.recipe.EUaTick);
            ++this.progress;
            if (this.progress >= this.recipe.ticksNeeded) {
                this.operate();
                this.progress = 0;
                needsInvUpdate = true;
            }
        }
        else {
            this.setActive(false);
            this.progress = 0;
        }
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        if (needsInvUpdate) {
            super.markDirty();
        }
    }
    
    protected boolean canOperate() {
        if (this.input.getFluid() == null) {
            return false;
        }
        this.recipe = Recipes.electrolyzer.getElectrolysisInformation(this.input.getFluid().getFluid());
        if (this.recipe == null || this.energy.getEnergy() < this.recipe.EUaTick || this.input.getFluidAmount() < this.recipe.inputAmount) {
            return false;
        }
        for (final IElectrolyzerRecipeManager.ElectrolyzerOutput output : this.recipe.outputs) {
            if (!this.canFillTank(output.tankDirection, output.getOutput())) {
                return false;
            }
        }
        return true;
    }
    
    protected void operate() {
        assert this.recipe != null;
        this.input.drainInternal(this.recipe.inputAmount, true);
        for (final IElectrolyzerRecipeManager.ElectrolyzerOutput output : this.recipe.outputs) {
            this.fillTank(output.tankDirection, output.getOutput());
        }
    }
    
    protected boolean canFillTank(final EnumFacing facing, final FluidStack fluid) {
        final TileEntity te = this.getWorld().getTileEntity(this.pos.offset(facing));
        return te instanceof TileEntityTank && LiquidUtil.fillTile(te, facing, fluid, true) == fluid.amount;
    }
    
    protected void fillTank(final EnumFacing facing, final FluidStack fluid) {
        final TileEntity te = this.getWorld().getTileEntity(this.pos.offset(facing));
        if (te instanceof TileEntityTank) {
            LiquidUtil.fillTile(te, facing, fluid, false);
        }
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.FluidConsuming);
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    @Override
    public ContainerBase<TileEntityElectrolyzer> getGuiContainer(final EntityPlayer player) {
        return new ContainerElectrolyzer(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiElectrolyzer(new ContainerElectrolyzer(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public FluidTank getInput() {
        return this.input;
    }
    
    public boolean hasRecipe() {
        return this.getCurrentRecipe() != null;
    }
    
    public IElectrolyzerRecipeManager.ElectrolyzerRecipe getCurrentRecipe() {
        return this.recipe;
    }
    
    @Override
    public double getRatio() {
        return (this.recipe == null) ? 0.0 : (this.progress / (double)this.recipe.ticksNeeded);
    }
}
