// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessable;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import java.util.Collections;
import java.util.Collection;
import ic2.core.block.invslot.InvSlotProcessableSolidCanner;
import ic2.core.block.invslot.InvSlotConsumableSolidCanner;
import ic2.core.profile.NotClassic;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.ICannerBottleRecipeManager;

@NotClassic
public class TileEntitySolidCanner extends TileEntityStandardMachine<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput>
{
    public final InvSlotConsumableSolidCanner canInputSlot;
    
    public TileEntitySolidCanner() {
        super(2, 200, 1);
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableSolidCanner(this, "input", 1);
        this.canInputSlot = new InvSlotConsumableSolidCanner(this, "canInput", 1);
    }
    
    @Override
    public String getStartSoundFile() {
        return null;
    }
    
    @Override
    public String getInterruptSoundFile() {
        return null;
    }
    
    @Override
    protected Collection<ItemStack> getOutput(final ItemStack output) {
        return Collections.singleton(output);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
