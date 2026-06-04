// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;
import java.util.Map;
import ic2.api.item.IBlockCuttingBlade;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Arrays;
import ic2.core.item.type.BlockCuttingBladeType;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import java.util.Collections;
import ic2.core.util.StackUtil;
import ic2.core.ref.TeBlock;
import ic2.core.ref.BlockName;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import java.util.ArrayList;
import mezz.jei.api.recipe.IRecipeWrapper;
import java.util.List;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.api.recipe.IBasicMachineRecipeManager;

public interface IRecipeWrapperGenerator<T>
{
    public static final IRecipeWrapperGenerator<IBasicMachineRecipeManager> basicMachine = new IRecipeWrapperGenerator<IBasicMachineRecipeManager>() {
        @Override
        public List<IRecipeWrapper> getRecipeList(final IORecipeCategory<IBasicMachineRecipeManager> category) {
            final List<IRecipeWrapper> recipes = new ArrayList<IRecipeWrapper>();
            for (final MachineRecipe<IRecipeInput, Collection<ItemStack>> container : category.recipeManager.getRecipes()) {
                recipes.add((IRecipeWrapper)new IORecipeWrapper(container, category));
            }
            return recipes;
        }
    };
    public static final IRecipeWrapperGenerator<IBasicMachineRecipeManager> recycler = new IRecipeWrapperGenerator<IBasicMachineRecipeManager>() {
        @Override
        public List<IRecipeWrapper> getRecipeList(final IORecipeCategory<IBasicMachineRecipeManager> category) {
            final IRecipeInput input = new IRecipeInput() {
                @Override
                public boolean matches(final ItemStack subject) {
                    return StackUtil.checkItemEquality(subject, BlockName.te.getItemStack(TeBlock.recycler));
                }
                
                @Override
                public List<ItemStack> getInputs() {
                    return Collections.singletonList(BlockName.te.getItemStack(TeBlock.recycler));
                }
                
                @Override
                public int getAmount() {
                    return 1;
                }
            };
            return Collections.singletonList((IRecipeWrapper)new IORecipeWrapper(new MachineRecipe<IRecipeInput, Collection<ItemStack>>(input, Collections.singletonList(ItemName.crafting.getItemStack(CraftingItemType.scrap))), category));
        }
    };
    public static final IRecipeWrapperGenerator<IBasicMachineRecipeManager> blockCutter = new IRecipeWrapperGenerator<IBasicMachineRecipeManager>() {
        private final List<ItemStack> candidates = Arrays.asList(ItemName.block_cutting_blade.getItemStack(BlockCuttingBladeType.iron), ItemName.block_cutting_blade.getItemStack(BlockCuttingBladeType.steel), ItemName.block_cutting_blade.getItemStack(BlockCuttingBladeType.diamond));
        
        @Override
        public List<IRecipeWrapper> getRecipeList(final IORecipeCategory<IBasicMachineRecipeManager> category) {
            final List<IRecipeWrapper> list = new ArrayList<IRecipeWrapper>();
            for (final MachineRecipe<IRecipeInput, Collection<ItemStack>> container : category.recipeManager.getRecipes()) {
                list.add((IRecipeWrapper)new AdvancedIORecipeWrapper(container, this.getInput(this.getHardness(container.getMetaData())), category));
            }
            return list;
        }
        
        private int getHardness(final NBTTagCompound metadata) {
            if (metadata == null) {
                return Integer.MAX_VALUE;
            }
            return metadata.getInteger("hardness");
        }
        
        private IRecipeInput getInput(final int hardness) {
            return new IRecipeInput() {
                @Override
                public boolean matches(final ItemStack subject) {
                    return subject != null && subject.getItem() instanceof IBlockCuttingBlade && ((IBlockCuttingBlade)subject.getItem()).getHardness(subject) > hardness;
                }
                
                @Override
                public List<ItemStack> getInputs() {
                    final List<ItemStack> list = new ArrayList<ItemStack>(IRecipeWrapperGenerator.this.candidates.size());
                    for (final ItemStack stack : IRecipeWrapperGenerator.this.candidates) {
                        if (((IBlockCuttingBlade)stack.getItem()).getHardness(stack) >= hardness) {
                            list.add(stack);
                        }
                    }
                    return list;
                }
                
                @Override
                public int getAmount() {
                    return 1;
                }
            };
        }
    };
    public static final IRecipeWrapperGenerator<IElectrolyzerRecipeManager> electrolyzer = new IRecipeWrapperGenerator<IElectrolyzerRecipeManager>() {
        @Override
        public List<IRecipeWrapper> getRecipeList(final IORecipeCategory<IElectrolyzerRecipeManager> category) {
            final List<IRecipeWrapper> recipes = new ArrayList<IRecipeWrapper>();
            for (final Map.Entry<String, IElectrolyzerRecipeManager.ElectrolyzerRecipe> recipe : category.recipeManager.getRecipeMap().entrySet()) {
                final Fluid input = FluidRegistry.getFluid((String)recipe.getKey());
                if (input != null) {
                    recipes.add((IRecipeWrapper)new ElectrolyzerWrapper(new FluidStack(input, recipe.getValue().inputAmount), recipe.getValue().outputs, category));
                }
            }
            return recipes;
        }
    };
    public static final IRecipeWrapperGenerator<ICannerEnrichRecipeManager> cannerEnrichment = new IRecipeWrapperGenerator<ICannerEnrichRecipeManager>() {
        @Override
        public List<IRecipeWrapper> getRecipeList(final IORecipeCategory<ICannerEnrichRecipeManager> category) {
            final List<IRecipeWrapper> recipes = new ArrayList<IRecipeWrapper>();
            for (final MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> recipe : category.recipeManager.getRecipes()) {
                recipes.add((IRecipeWrapper)new CannerEnrichmentWrapper(recipe.getInput(), recipe.getOutput(), category));
            }
            return recipes;
        }
    };
    public static final IRecipeWrapperGenerator<ICannerBottleRecipeManager> cannerBottling = new IRecipeWrapperGenerator<ICannerBottleRecipeManager>() {
        @Override
        public List<IRecipeWrapper> getRecipeList(final IORecipeCategory<ICannerBottleRecipeManager> category) {
            final List<IRecipeWrapper> recipes = new ArrayList<IRecipeWrapper>();
            for (final MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe : category.recipeManager.getRecipes()) {
                recipes.add((IRecipeWrapper)new CannerCanningWrapper(recipe.getInput(), recipe.getOutput(), category));
            }
            return recipes;
        }
    };
    
    List<IRecipeWrapper> getRecipeList(final IORecipeCategory<T> p0);
}
