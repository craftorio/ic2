// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.core.block.TileEntityBlock;
import ic2.api.upgrade.IRemoteAccessUpgrade;
import java.util.Iterator;
import java.util.Collection;
import ic2.core.block.comp.TileEntityComponent;
import ic2.api.upgrade.IRedstoneSensitiveUpgrade;
import ic2.api.upgrade.ITransformerUpgrade;
import ic2.api.upgrade.IEnergyStorageUpgrade;
import ic2.api.upgrade.IProcessingUpgrade;
import ic2.api.upgrade.IAugmentationUpgrade;
import ic2.api.upgrade.IFullUpgrade;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import net.minecraft.item.Item;
import ic2.api.upgrade.IUpgradeItem;
import net.minecraft.item.ItemStack;
import java.util.Collections;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.comp.Redstone;
import java.util.List;

public class InvSlotUpgrade extends InvSlot
{
    private static final int maxStackSize = 64;
    public int augmentation;
    public int extraProcessTime;
    public double processTimeMultiplier;
    public int extraEnergyDemand;
    public double energyDemandMultiplier;
    public int extraEnergyStorage;
    public double energyStorageMultiplier;
    public int extraTier;
    private List<Redstone.IRedstoneModifier> redstoneModifiers;
    
    public static InvSlotUpgrade createUnchecked(final IInventorySlotHolder<?> base, final String name, final int count) {
        return new InvSlotUpgrade(base, name, count);
    }
    
    public <T extends IInventorySlotHolder<?> & IUpgradableBlock> InvSlotUpgrade(final T base, final String name, final int count) {
        super(base, name, Access.NONE, count);
        this.redstoneModifiers = Collections.emptyList();
        this.resetRates();
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        final Item rawItem = stack.getItem();
        if (!(rawItem instanceof IUpgradeItem)) {
            return false;
        }
        final IUpgradeItem item = (IUpgradeItem)rawItem;
        return item.isSuitableFor(stack, ((IUpgradableBlock)this.base).getUpgradableProperties());
    }
    
    @Override
    public void onChanged() {
        this.resetRates();
        final IUpgradableBlock block = (IUpgradableBlock)this.base;
        final List<Redstone.IRedstoneModifier> newRedstoneModifiers = new ArrayList<Redstone.IRedstoneModifier>();
        for (int i = 0; i < this.size(); ++i) {
            final ItemStack stack = this.get(i);
            if (!StackUtil.isEmpty(stack)) {
                if (this.accepts(stack)) {
                    final IUpgradeItem upgrade = (IUpgradeItem)stack.getItem();
                    final boolean all = upgrade instanceof IFullUpgrade;
                    final int size = StackUtil.getSize(stack);
                    if (all || upgrade instanceof IAugmentationUpgrade) {
                        this.augmentation += ((IAugmentationUpgrade)upgrade).getAugmentation(stack, block) * size;
                    }
                    if (all || upgrade instanceof IProcessingUpgrade) {
                        final IProcessingUpgrade procUpgrade = (IProcessingUpgrade)upgrade;
                        this.extraProcessTime += procUpgrade.getExtraProcessTime(stack, block) * size;
                        this.processTimeMultiplier *= Math.pow(procUpgrade.getProcessTimeMultiplier(stack, block), size);
                        this.extraEnergyDemand += procUpgrade.getExtraEnergyDemand(stack, block) * size;
                        this.energyDemandMultiplier *= Math.pow(procUpgrade.getEnergyDemandMultiplier(stack, block), size);
                    }
                    if (all || upgrade instanceof IEnergyStorageUpgrade) {
                        final IEnergyStorageUpgrade engUpgrade = (IEnergyStorageUpgrade)upgrade;
                        this.extraEnergyStorage += engUpgrade.getExtraEnergyStorage(stack, block) * size;
                        this.energyStorageMultiplier *= Math.pow(engUpgrade.getEnergyStorageMultiplier(stack, block), size);
                    }
                    if (all || upgrade instanceof ITransformerUpgrade) {
                        this.extraTier += ((ITransformerUpgrade)upgrade).getExtraTier(stack, block) * size;
                    }
                    if (all || upgrade instanceof IRedstoneSensitiveUpgrade) {
                        final IRedstoneSensitiveUpgrade redUpgrade = (IRedstoneSensitiveUpgrade)upgrade;
                        if (redUpgrade.modifiesRedstoneInput(stack, block)) {
                            newRedstoneModifiers.add(new UpgradeRedstoneModifier(redUpgrade, stack, block));
                        }
                    }
                }
            }
        }
        for (final TileEntityComponent component : ((TileEntityBlock)this.base.getParent()).getComponents()) {
            if (!(component instanceof Redstone)) {
                continue;
            }
            final Redstone rs = (Redstone)component;
            rs.removeRedstoneModifiers(this.redstoneModifiers);
            rs.addRedstoneModifiers(newRedstoneModifiers);
            rs.update();
        }
        this.redstoneModifiers = (newRedstoneModifiers.isEmpty() ? Collections.emptyList() : newRedstoneModifiers);
    }
    
    private void resetRates() {
        this.augmentation = 0;
        this.extraProcessTime = 0;
        this.processTimeMultiplier = 1.0;
        this.extraEnergyDemand = 0;
        this.energyDemandMultiplier = 1.0;
        this.extraEnergyStorage = 0;
        this.energyStorageMultiplier = 1.0;
        this.extraTier = 0;
    }
    
    public int getOperationsPerTick(final int defaultOperationLength) {
        if (defaultOperationLength == 0) {
            return 64;
        }
        return this.getOpsPerTick(this.getStackOpLen(defaultOperationLength));
    }
    
    public int getOperationLength(final int defaultOperationLength) {
        if (defaultOperationLength == 0) {
            return 1;
        }
        final double stackOpLen = this.getStackOpLen(defaultOperationLength);
        final int opsPerTick = this.getOpsPerTick(stackOpLen);
        return Math.max(1, (int)Math.round(stackOpLen * opsPerTick / 64.0));
    }
    
    private double getStackOpLen(final int defaultOperationLength) {
        return (defaultOperationLength + (double)this.extraProcessTime) * 64.0 * this.processTimeMultiplier;
    }
    
    private int getOpsPerTick(final double stackOpLen) {
        return (int)Math.min(Math.ceil(64.0 / stackOpLen), 2.147483647E9);
    }
    
    public int getEnergyDemand(final int defaultEnergyDemand) {
        return applyModifier(defaultEnergyDemand, this.extraEnergyDemand, this.energyDemandMultiplier);
    }
    
    public int getEnergyStorage(final int defaultEnergyStorage, final int defaultOperationLength, final int defaultEnergyDemand) {
        final int opLen = this.getOperationLength(defaultOperationLength);
        final int energyDemand = this.getEnergyDemand(defaultEnergyDemand);
        return applyModifier(defaultEnergyStorage, this.extraEnergyStorage + opLen * energyDemand, this.energyStorageMultiplier);
    }
    
    public int getTier(final int defaultTier) {
        return applyModifier(defaultTier, this.extraTier, 1.0);
    }
    
    public int getRemoteRange(int existingRange) {
        for (final ItemStack stack : this) {
            if (!StackUtil.isEmpty(stack)) {
                if (!this.accepts(stack)) {
                    continue;
                }
                final IUpgradeItem upgrade = (IUpgradeItem)stack.getItem();
                if (!(upgrade instanceof IRemoteAccessUpgrade)) {
                    continue;
                }
                existingRange = ((IRemoteAccessUpgrade)upgrade).getRangeAmplification(stack, (IUpgradableBlock)this.base, existingRange);
            }
        }
        return existingRange;
    }
    
    private static int applyModifier(final int base, final int extra, final double multiplier) {
        final double ret = (double)Math.round((base + (double)extra) * multiplier);
        return (ret > 2.147483647E9) ? Integer.MAX_VALUE : ((int)ret);
    }
    
    public boolean tickNoMark() {
        final IUpgradableBlock block = (IUpgradableBlock)this.base;
        boolean ret = false;
        for (int i = 0; i < this.size(); ++i) {
            final ItemStack stack = this.get(i);
            if (!StackUtil.isEmpty(stack)) {
                if (stack.getItem() instanceof IUpgradeItem) {
                    if (((IUpgradeItem)stack.getItem()).onTick(stack, block)) {
                        ret = true;
                    }
                }
            }
        }
        return ret;
    }
    
    public void tick() {
        if (this.tickNoMark()) {
            ((TileEntityBlock)this.base.getParent()).markDirty();
        }
    }
    
    private static class UpgradeRedstoneModifier implements Redstone.IRedstoneModifier
    {
        private final IRedstoneSensitiveUpgrade upgrade;
        private final ItemStack stack;
        private final IUpgradableBlock block;
        
        UpgradeRedstoneModifier(final IRedstoneSensitiveUpgrade upgrade, final ItemStack stack, final IUpgradableBlock block) {
            this.upgrade = upgrade;
            this.stack = stack.copy();
            this.block = block;
        }
        
        @Override
        public int getRedstoneInput(final int redstoneInput) {
            return this.upgrade.getRedstoneInput(this.stack, this.block, redstoneInput);
        }
    }
}
