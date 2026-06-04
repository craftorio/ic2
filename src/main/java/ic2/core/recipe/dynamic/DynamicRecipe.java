// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

import net.minecraftforge.fluids.FluidStack;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import java.util.ArrayList;
import net.minecraft.nbt.NBTTagCompound;
import java.util.List;

public class DynamicRecipe
{
    private final DynamicRecipeManager manager;
    private List<RecipeInputIngredient> inputIngredients;
    private List<RecipeOutputIngredient> outputIngredients;
    private int operationEnergyCost;
    private int operationDuration;
    private NBTTagCompound metadata;
    
    public DynamicRecipe(final DynamicRecipeManager manager) {
        this.inputIngredients = new ArrayList<RecipeInputIngredient>();
        this.outputIngredients = new ArrayList<RecipeOutputIngredient>();
        this.manager = manager;
    }
    
    public DynamicRecipe withInput(final List<RecipeInputIngredient> inputs) {
        this.inputIngredients.addAll(inputs);
        return this;
    }
    
    public DynamicRecipe withInput(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        this.inputIngredients.add(RecipeInputItemStack.of(stack));
        return this;
    }
    
    public DynamicRecipe withInput(final String oreDict) {
        this.inputIngredients.add(RecipeInputOreDictionary.of(oreDict));
        return this;
    }
    
    public DynamicRecipe withInput(final String oreDict, final int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        this.inputIngredients.add(RecipeInputOreDictionary.of(oreDict, amount));
        return this;
    }
    
    public DynamicRecipe withInput(final String oreDict, final int amount, final Integer meta) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        this.inputIngredients.add(RecipeInputOreDictionary.of(oreDict, amount, meta));
        return this;
    }
    
    public DynamicRecipe withInput(final FluidStack stack) {
        if (stack.amount <= 0) {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        this.inputIngredients.add(RecipeInputFluidStack.of(stack));
        return this;
    }
    
    public DynamicRecipe withOutput(final List<RecipeOutputIngredient> outputs) {
        this.outputIngredients.addAll(outputs);
        return this;
    }
    
    public DynamicRecipe withOutput(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            throw new IllegalArgumentException("Output cannot be empty");
        }
        this.outputIngredients.add(RecipeOutputItemStack.of(stack));
        return this;
    }
    
    public DynamicRecipe withOutput(final FluidStack stack) {
        if (stack.amount <= 0) {
            throw new IllegalArgumentException("Output cannot be empty");
        }
        this.outputIngredients.add(RecipeOutputFluidStack.of(stack));
        return this;
    }
    
    public DynamicRecipe withOperationEnergyCost(final int operationEnergyCost) {
        this.operationEnergyCost = operationEnergyCost;
        return this;
    }
    
    public DynamicRecipe withOperationDurationTicks(final int operationDuration) {
        this.operationDuration = operationDuration;
        return this;
    }
    
    public DynamicRecipe withOperationDurationSeconds(final int operationDuration) {
        this.operationDuration = operationDuration * 20;
        return this;
    }
    
    public DynamicRecipe withMetadata(final NBTTagCompound metadata) {
        this.metadata = metadata;
        return this;
    }
    
    public DynamicRecipe withMetadata(final String key, final int value) {
        if (this.metadata == null) {
            this.metadata = new NBTTagCompound();
        }
        this.metadata.setInteger(key, value);
        return this;
    }
    
    public DynamicRecipe withMetadata(final String key, final short value) {
        if (this.metadata == null) {
            this.metadata = new NBTTagCompound();
        }
        this.metadata.setShort(key, value);
        return this;
    }
    
    public DynamicRecipe withMetadata(final String key, final byte value) {
        if (this.metadata == null) {
            this.metadata = new NBTTagCompound();
        }
        this.metadata.setByte(key, value);
        return this;
    }
    
    public DynamicRecipe withMetadata(final String key, final float value) {
        if (this.metadata == null) {
            this.metadata = new NBTTagCompound();
        }
        this.metadata.setFloat(key, value);
        return this;
    }
    
    public DynamicRecipe withMetadata(final String key, final double value) {
        if (this.metadata == null) {
            this.metadata = new NBTTagCompound();
        }
        this.metadata.setDouble(key, value);
        return this;
    }
    
    public DynamicRecipe withMetadata(final String key, final boolean value) {
        if (this.metadata == null) {
            this.metadata = new NBTTagCompound();
        }
        this.metadata.setBoolean(key, value);
        return this;
    }
    
    public void register() {
        this.register(false);
    }
    
    public void register(final boolean replace) {
        boolean success = false;
        success = this.manager.addRecipe(this, replace);
        if (!success) {
            this.manager.displayError("Registration failed for recipe " + this);
        }
    }
    
    public List<RecipeInputIngredient> getInputIngredients() {
        return this.inputIngredients;
    }
    
    public List<RecipeOutputIngredient> getOutputIngredients() {
        return this.outputIngredients;
    }
    
    public int getOperationEnergyCost() {
        return this.operationEnergyCost;
    }
    
    public int getOperationDuration() {
        return this.operationDuration;
    }
    
    public NBTTagCompound getMetadata() {
        return this.metadata;
    }
}
