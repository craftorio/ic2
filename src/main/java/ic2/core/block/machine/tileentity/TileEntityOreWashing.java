// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessable;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.ContainerBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.IFluidTank;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.Fluid;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import ic2.core.block.comp.Fluids;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.profile.NotClassic;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

@NotClassic
public class TileEntityOreWashing extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    public final InvSlotConsumableLiquid fluidSlot;
    public final InvSlotOutput cellSlot;
    @GuiSynced
    protected final FluidTank fluidTank;
    protected final Fluids fluids;
    
    public TileEntityOreWashing() {
        super(16, 500, 3);
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableGeneric(this, "input", 1, Recipes.oreWashing);
        this.fluidSlot = new InvSlotConsumableLiquidByList(this, "fluid", 1, new Fluid[] { FluidRegistry.WATER });
        this.cellSlot = new InvSlotOutput(this, "cell", 1);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(FluidRegistry.WATER));
    }
    
    public static void init() {
        Recipes.oreWashing = new BasicMachineRecipeManager();
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity()) {
            this.gainFluid();
        }
    }
    
    public void operateOnce(final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output, final Collection<ItemStack> processResult) {
        super.operateOnce(output, processResult);
        this.fluidTank.drainInternal(output.getRecipe().getMetaData().getInteger("amount"), true);
    }
    
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
        final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getOutput();
        if (ret != null) {
            if (ret.getRecipe().getMetaData() == null) {
                return null;
            }
            if (ret.getRecipe().getMetaData().getInteger("amount") > this.fluidTank.getFluidAmount()) {
                return null;
            }
        }
        return ret;
    }
    
    public boolean gainFluid() {
        return this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.cellSlot);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public ContainerBase<TileEntityOreWashing> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming);
    }
}
