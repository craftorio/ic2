// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessable;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Iterator;
import ic2.api.recipe.MachineRecipe;
import java.util.Collections;
import ic2.core.util.LiquidUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraft.init.Items;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

public class TileEntityCompressor extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    protected boolean usingPumpRecipe;
    protected final Set<TileEntityPump> pumps;
    
    public TileEntityCompressor() {
        super(2, 300, 1);
        this.pumps = new HashSet<TileEntityPump>(12, 0.5f);
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableGeneric(this, "input", 1, Recipes.compressor);
    }
    
    public static void init() {
        Recipes.compressor = new BasicMachineRecipeManager();
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.findPumps();
    }
    
    protected void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        this.findPumps();
    }
    
    protected void findPumps() {
        final World world = this.getWorld();
        this.pumps.clear();
        for (final EnumFacing side : EnumFacing.VALUES) {
            final TileEntity te = world.getTileEntity(this.pos.offset(side));
            if (te instanceof TileEntityPump) {
                this.pumps.add((TileEntityPump)te);
            }
        }
    }
    
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
        this.usingPumpRecipe = false;
        MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = super.getOutput();
        if (output != null) {
            return output;
        }
        if (!this.pumps.isEmpty() && this.inputSlot.isEmpty() && this.outputSlot.canAdd(new ItemStack(Items.SNOWBALL))) {
            final FluidStack fluid = new FluidStack(FluidRegistry.WATER, 1000);
            for (final TileEntityPump pump : this.pumps) {
                final FluidStack amount = LiquidUtil.drainTile(pump, EnumFacing.UP, FluidRegistry.WATER, fluid.amount, true);
                if (amount != null) {
                    assert amount.getFluid() == FluidRegistry.WATER;
                    final FluidStack fluidStack = fluid;
                    fluidStack.amount -= amount.amount;
                }
                if (fluid.amount <= 0) {
                    this.usingPumpRecipe = true;
                    output = new MachineRecipe<IRecipeInput, Collection<ItemStack>>(null, Collections.singletonList(new ItemStack(Items.SNOWBALL))).getResult((ItemStack)null);
                    break;
                }
            }
        }
        return output;
    }
    
    public void operateOnce(final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output, final Collection<ItemStack> processResult) {
        if (this.usingPumpRecipe) {
            final FluidStack fluid = new FluidStack(FluidRegistry.WATER, 1000);
            for (final TileEntityPump pump : this.pumps) {
                final FluidStack amount = LiquidUtil.drainTile(pump, EnumFacing.UP, FluidRegistry.WATER, fluid.amount, false);
                if (amount != null && amount.getFluid() == FluidRegistry.WATER) {
                    final FluidStack fluidStack = fluid;
                    fluidStack.amount -= amount.amount;
                }
                if (fluid.amount <= 0) {
                    break;
                }
            }
            this.outputSlot.add(processResult);
        }
        else {
            super.operateOnce(output, processResult);
        }
    }
    
    @Override
    public String getStartSoundFile() {
        return "Machines/CompressorOp.ogg";
    }
    
    @Override
    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
