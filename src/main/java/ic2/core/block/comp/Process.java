// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import ic2.api.upgrade.IUpgradeItem;
import ic2.core.util.StackUtil;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.recipe.SmeltingRecipeManager;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotOutput;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import ic2.core.block.invslot.InvSlotProcessable;

public class Process extends TileEntityComponent
{
    protected int progress;
    public int defaultEnergyConsume;
    public int operationDuration;
    public int defaultTier;
    public int defaultEnergyStorage;
    public int energyConsume;
    public int operationLength;
    public int operationsPerTick;
    private final InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack> inputSlot;
    private final InvSlotOutput outputSlot;
    private InvSlotUpgrade upgradeSlot;
    
    public static Process asFurnace(final TileEntityInventory parent) {
        return asFurnace(parent, 3, 100, 1, 4);
    }
    
    public static Process asFurnace(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, SmeltingRecipeManager.SmeltingBridge.INSTANCE, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asMacerator(final TileEntityInventory parent) {
        return asMacerator(parent, 2, 300, 1, 4);
    }
    
    public static Process asMacerator(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.macerator, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asExtractor(final TileEntityInventory parent) {
        return asExtractor(parent, 2, 300, 1, 4);
    }
    
    public static Process asExtractor(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.extractor, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asCompressor(final TileEntityInventory parent) {
        return asCompressor(parent, 2, 300, 1, 4);
    }
    
    public static Process asCompressor(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.compressor, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asCentrifuge(final TileEntityInventory parent) {
        return asCentrifuge(parent, 48, 500, 3, 4);
    }
    
    public static Process asCentrifuge(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.centrifuge, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asRecycler(final TileEntityInventory parent) {
        return asRecycler(parent, 1, 45, 1, 4);
    }
    
    public static Process asRecycler(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.recycler, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asOreWasher(final TileEntityInventory parent) {
        return asOreWasher(parent, 16, 500, 3, 4);
    }
    
    public static Process asOreWasher(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.oreWashing, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asBlockCutter(final TileEntityInventory parent) {
        return asBlockCutter(parent, 48, 900, 1, 4);
    }
    
    public static Process asBlockCutter(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.blockcutter, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asBlastFurnace(final TileEntityInventory parent) {
        return asBlastFurnace(parent, 2, 300, 1, 4);
    }
    
    public static Process asBlastFurnace(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.blastfurnace, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asExtruder(final TileEntityInventory parent) {
        return asExtruder(parent, 10, 200, 1, 4);
    }
    
    public static Process asExtruder(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.metalformerExtruding, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asCutter(final TileEntityInventory parent) {
        return asCutter(parent, 10, 200, 1, 4);
    }
    
    public static Process asCutter(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.metalformerCutting, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public static Process asRollingMachine(final TileEntityInventory parent) {
        return asRollingMachine(parent, 10, 200, 1, 4);
    }
    
    public static Process asRollingMachine(final TileEntityInventory parent, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        return new Process(parent, Recipes.metalformerRolling, operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    public Process(final TileEntityInventory parent, final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeManager) {
        this(parent, recipeManager, 2, 100, 1, 0);
    }
    
    public Process(final TileEntityInventory parent, final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> recipeManager, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        this(parent, new InvSlotProcessableGeneric(parent, "input", 1, recipeManager), operationCost, operationDuration, outputSlots, upgradeSlots);
    }
    
    protected Process(final TileEntityInventory parent, final InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack> inputSlot, final int operationCost, final int operationDuration, final int outputSlots, final int upgradeSlots) {
        super(parent);
        this.progress = 0;
        this.operationDuration = operationDuration;
        assert inputSlot != null;
        this.inputSlot = inputSlot;
        this.outputSlot = new InvSlotOutput(parent, "output", outputSlots);
        if (parent instanceof IUpgradableBlock && upgradeSlots > 0) {
            this.upgradeSlot = InvSlotUpgrade.createUnchecked(parent, "upgrade", upgradeSlots);
        }
    }
    
    public void readFromNBT(final NBTTagCompound nbt) {
        this.progress = nbt.getInteger("progress");
    }
    
    public void writeToNBT(final NBTTagCompound nbt) {
        nbt.setInteger("progress", this.progress);
    }
    
    public static int applyModifier(final int base, final int extra, final double multiplier) {
        final double ret = (double)Math.round((base + (double)extra) * multiplier);
        return (ret > 2.147483647E9) ? Integer.MAX_VALUE : ((int)ret);
    }
    
    public void setOverclockRates() {
        this.upgradeSlot.onChanged();
        final double previousProgress = this.progress / (double)this.operationDuration;
        final double stackOpLen = (this.operationDuration + (double)this.upgradeSlot.extraProcessTime) * 64.0 * this.upgradeSlot.processTimeMultiplier;
        this.operationsPerTick = (int)Math.min(Math.ceil(64.0 / stackOpLen), 2.147483647E9);
        this.operationDuration = (int)Math.round(stackOpLen * this.operationsPerTick / 64.0);
        this.energyConsume = applyModifier(this.defaultEnergyConsume, this.upgradeSlot.extraEnergyDemand, this.upgradeSlot.energyDemandMultiplier);
        if (this.operationDuration < 1) {
            this.operationDuration = 1;
        }
        this.progress = (short)Math.floor(previousProgress * this.operationDuration + 0.1);
    }
    
    public void operate(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result) {
        for (int i = 0; i < this.operationsPerTick; ++i) {
            final Collection<ItemStack> processResult = StackUtil.copy(result.getOutput());
            if (this.parent instanceof IUpgradableBlock) {
                for (int j = 0; j < this.upgradeSlot.size(); ++j) {
                    final ItemStack stack = this.upgradeSlot.get(j);
                    if (!StackUtil.isEmpty(stack)) {
                        if (stack.getItem() instanceof IUpgradeItem) {
                            ((IUpgradeItem)stack.getItem()).onProcessEnd(stack, (IUpgradableBlock)this.parent, processResult);
                        }
                    }
                }
            }
            this.operateOnce(result, processResult);
            result = this.getOutput();
            if (result == null) {
                break;
            }
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
        final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.inputSlot.process();
        if (result == null) {
            return null;
        }
        if (this.outputSlot.canAdd(result.getOutput())) {
            return result;
        }
        return null;
    }
    
    public int getProgress() {
        return this.progress;
    }
    
    public double getProgressRatio() {
        return this.progress / this.operationDuration;
    }
}
