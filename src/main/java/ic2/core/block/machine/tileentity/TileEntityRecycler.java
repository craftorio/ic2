// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessable;
import ic2.api.recipe.MachineRecipe;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import ic2.core.util.StackUtil;
import ic2.api.recipe.RecipeOutput;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.recipe.IBasicMachineRecipeManager;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.IC2;
import ic2.api.recipe.MachineRecipeResult;
import java.util.Iterator;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.recipe.BasicListRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

public class TileEntityRecycler extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    public TileEntityRecycler() {
        super(1, 45, 1);
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableGeneric(this, "input", 1, Recipes.recycler);
    }
    
    public static void init() {
        Recipes.recycler = new RecyclerRecipeManager();
        Recipes.recyclerWhitelist = new BasicListRecipeManager();
        Recipes.recyclerBlacklist = new BasicListRecipeManager();
    }
    
    public static void initLate() {
        for (final IRecipeInput input : ConfigUtil.asRecipeInputList(MainConfig.get(), "balance/recyclerBlacklist")) {
            Recipes.recyclerBlacklist.add(input);
        }
        for (final IRecipeInput input : ConfigUtil.asRecipeInputList(MainConfig.get(), "balance/recyclerWhitelist")) {
            Recipes.recyclerWhitelist.add(input);
        }
    }
    
    public static int recycleChance() {
        return 8;
    }
    
    @Override
    public String getStartSoundFile() {
        return "Machines/RecyclerOp.ogg";
    }
    
    @Override
    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }
    
    public static boolean getIsItemBlacklisted(final ItemStack aStack) {
        if (Recipes.recyclerWhitelist.isEmpty()) {
            return Recipes.recyclerBlacklist.contains(aStack);
        }
        return !Recipes.recyclerWhitelist.contains(aStack);
    }
    
    public void operateOnce(final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, final Collection<ItemStack> processResult) {
        this.inputSlot.consume((MachineRecipeResult<RI, RO, I>)result);
        if (IC2.random.nextInt(recycleChance()) == 0) {
            this.outputSlot.add(processResult);
        }
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
    
    private static class RecyclerRecipeManager implements IBasicMachineRecipeManager
    {
        public RecyclerRecipeManager() {
        }
        
        @Override
        public boolean addRecipe(final IRecipeInput input, final Collection<ItemStack> output, final NBTTagCompound metadata, final boolean replace) {
            return false;
        }
        
        @Override
        public boolean addRecipe(final IRecipeInput input, final NBTTagCompound metadata, final boolean replace, final ItemStack... outputs) {
            return false;
        }
        
        @Override
        public RecipeOutput getOutputFor(final ItemStack input, final boolean adjustInput) {
            if (StackUtil.isEmpty(input)) {
                return null;
            }
            final RecipeOutput ret = new RecipeOutput(null, new ArrayList<ItemStack>(getOutput(input)));
            if (adjustInput) {
                input.shrink(1);
            }
            return ret;
        }
        
        private static Collection<ItemStack> getOutput(final ItemStack input) {
            return TileEntityRecycler.getIsItemBlacklisted(input) ? Collections.emptyList() : Collections.singletonList(ItemName.crafting.getItemStack(CraftingItemType.scrap));
        }
        
        @Override
        public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(final ItemStack input, final boolean acceptTest) {
            if (StackUtil.isEmpty(input)) {
                return null;
            }
            return new MachineRecipe<IRecipeInput, Collection<ItemStack>>(Recipes.inputFactory.forStack(input, 1), getOutput(input)).getResult(StackUtil.copyWithSize(input, StackUtil.getSize(input) - 1));
        }
        
        @Override
        public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean isIterable() {
            return false;
        }
    }
}
