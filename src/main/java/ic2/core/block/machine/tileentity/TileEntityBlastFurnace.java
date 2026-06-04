// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import ic2.api.energy.tile.IHeatSource;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidTank;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.Fluid;
import ic2.core.IC2;
import ic2.core.item.type.IngotResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.Recipes;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityBlastFurnace extends TileEntityInventory implements IUpgradableBlock, IHasGui, IGuiValueProvider
{
    public int heat;
    public static int maxHeat;
    @GuiSynced
    public float guiHeat;
    protected final Redstone redstone;
    protected final Fluids fluids;
    protected int progress;
    protected int progressNeeded;
    @GuiSynced
    protected float guiProgress;
    public final InvSlotProcessableGeneric inputSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotConsumableLiquidByList tankInputSlot;
    public final InvSlotOutput tankOutputSlot;
    public final InvSlotUpgrade upgradeSlot;
    @GuiSynced
    public final FluidTank fluidTank;
    
    public TileEntityBlastFurnace() {
        this.heat = 0;
        this.progress = 0;
        this.progressNeeded = 300;
        this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.blastfurnace);
        this.outputSlot = new InvSlotOutput(this, "output", 2) {
            @Override
            public void onPickupFromSlot(final EntityPlayer player, final ItemStack stack) {
                if (player != null && ItemName.ingot.getItemStack(IngotResourceType.steel).isItemEqual(stack)) {
                    IC2.achievements.issueAchievement(player, "acquireRefinedIron");
                }
            }
        };
        this.tankInputSlot = new InvSlotConsumableLiquidByList(this, "cellInput", 1, new Fluid[] { FluidName.air.getInstance() });
        this.tankOutputSlot = new InvSlotOutput(this, "cellOutput", 1);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 2);
        this.redstone = this.addComponent(new Redstone(this));
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(FluidName.air.getInstance()));
    }
    
    public static void init() {
        Recipes.blastfurnace = new BasicMachineRecipeManager();
    }
    
    public void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        this.heatup();
        final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.getOutput();
        if (result != null && this.isHot()) {
            this.setActive(true);
            if (result.getRecipe().getMetaData().getInteger("fluid") <= this.fluidTank.getFluidAmount()) {
                ++this.progress;
                this.fluidTank.drainInternal(result.getRecipe().getMetaData().getInteger("fluid"), true);
            }
            this.progressNeeded = result.getRecipe().getMetaData().getInteger("duration");
            if (this.progress >= result.getRecipe().getMetaData().getInteger("duration")) {
                this.operateOnce(result, result.getOutput());
                needsInvUpdate = true;
                this.progress = 0;
            }
        }
        else {
            if (result == null) {
                this.progress = 0;
            }
            this.setActive(false);
        }
        if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity()) {
            this.gainFluid();
        }
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        this.guiProgress = this.progress / (float)this.progressNeeded;
        this.guiHeat = this.heat / (float)TileEntityBlastFurnace.maxHeat;
        if (needsInvUpdate) {
            super.markDirty();
        }
    }
    
    public void operateOnce(final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, final Collection<ItemStack> processResult) {
        this.inputSlot.consume(result);
        this.outputSlot.add(processResult);
    }
    
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
        if (this.inputSlot.isEmpty()) {
            return null;
        }
        final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = this.inputSlot.process();
        if (output == null || output.getRecipe().getMetaData() == null) {
            return null;
        }
        if (this.outputSlot.canAdd(output.getOutput())) {
            return output;
        }
        return null;
    }
    
    public boolean gainFluid() {
        return this.tankInputSlot.processIntoTank((IFluidTank)this.fluidTank, this.tankOutputSlot);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.heat = nbt.getInteger("heat");
        this.progress = nbt.getInteger("progress");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("heat", this.heat);
        nbt.setInteger("progress", this.progress);
        return nbt;
    }
    
    private void heatup() {
        final int coolingPerTick = 1;
        int heatRequested = 0;
        int gainhU = 0;
        if ((!this.inputSlot.isEmpty() || this.progress >= 1) && this.heat <= TileEntityBlastFurnace.maxHeat) {
            heatRequested = TileEntityBlastFurnace.maxHeat - this.heat + 100;
        }
        else if (this.redstone.hasRedstoneInput() && this.heat <= TileEntityBlastFurnace.maxHeat) {
            heatRequested = TileEntityBlastFurnace.maxHeat - this.heat + 100;
        }
        if (heatRequested > 0) {
            final EnumFacing dir = this.getFacing();
            final TileEntity te = this.getWorld().getTileEntity(this.pos.offset(dir));
            if (te instanceof IHeatSource) {
                gainhU = ((IHeatSource)te).drawHeat(dir.getOpposite(), heatRequested, false);
                this.heat += gainhU;
            }
            if (gainhU == 0) {
                this.heat -= Math.min(this.heat, 1);
            }
        }
        else {
            this.heat -= Math.min(this.heat, 1);
        }
    }
    
    public boolean isHot() {
        return this.heat >= TileEntityBlastFurnace.maxHeat;
    }
    
    @Override
    public ContainerBase<TileEntityBlastFurnace> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getGuiValue(final String name) {
        if (name.equals("progress")) {
            return this.guiProgress;
        }
        if (name.equals("heat")) {
            return this.guiHeat;
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public double getEnergy() {
        return 0.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return false;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming);
    }
    
    static {
        TileEntityBlastFurnace.maxHeat = 50000;
    }
}
