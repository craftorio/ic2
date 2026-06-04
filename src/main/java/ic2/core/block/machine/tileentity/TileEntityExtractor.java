// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessable;
import java.util.Vector;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import java.util.Map;
import java.util.List;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

public class TileEntityExtractor extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    public static List<Map.Entry<ItemStack, ItemStack>> recipes;
    
    public TileEntityExtractor() {
        super(2, 300, 1);
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableGeneric(this, "input", 1, Recipes.extractor);
    }
    
    public static void init() {
        Recipes.extractor = new BasicMachineRecipeManager();
    }
    
    @Override
    public String getStartSoundFile() {
        return "Machines/ExtractorOp.ogg";
    }
    
    @Override
    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
    
    static {
        TileEntityExtractor.recipes = new Vector<Map.Entry<ItemStack, ItemStack>>();
    }
}
