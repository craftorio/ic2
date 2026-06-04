// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiBatchCrafter;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import ic2.api.upgrade.IUpgradeItem;
import java.util.Collections;
import net.minecraft.inventory.IInventory;
import gnu.trove.TIntCollection;
import net.minecraft.world.World;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Arrays;
import ic2.core.util.StackUtil;
import ic2.core.util.InventorySlotCrafting;
import ic2.core.block.IInventorySlotHolder;
import net.minecraft.item.crafting.IRecipe;
import ic2.core.util.Tuple;
import com.google.common.base.Predicate;
import net.minecraft.inventory.InventoryCrafting;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlot;
import ic2.api.network.ClientModifiable;
import net.minecraft.item.ItemStack;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.profile.NotClassic;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

@NotClassic
public class TileEntityBatchCrafter extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider, INetworkClientTileEntityEventListener
{
    private static final Set<UpgradableProperty> UPGRADES;
    public static final int defaultTier = 1;
    public static final int defaultEnergyConsume = 2;
    public static final int defaultOperationLength = 40;
    public static final int defaultEnergyStorage = 20000;
    @ClientModifiable
    public final ItemStack[] craftingGrid;
    public final InvSlot[] ingredientsRow;
    public final InvSlotOutput craftingOutput;
    public final InvSlotOutput containerOutput;
    public final InvSlotUpgrade upgradeSlot;
    protected final InventoryCrafting crafting;
    public final InventoryCrafting ingredients;
    public final Predicate<Tuple.T2<ItemStack, Integer>> acceptPredicate;
    protected IRecipe recipe;
    protected boolean canCraft;
    protected boolean newChange;
    protected boolean attemptToBalance;
    public ItemStack recipeOutput;
    public int energyConsume;
    public int operationLength;
    public int operationsPerTick;
    protected short progress;
    protected float guiProgress;
    
    public TileEntityBatchCrafter() {
        super(20000, 1);
        this.craftingGrid = new ItemStack[9];
        this.ingredientsRow = new InvSlot[this.craftingGrid.length];
        this.craftingOutput = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.SIDE);
        this.containerOutput = new InvSlotOutput(this, "containersOut", this.craftingGrid.length, InvSlot.InvSide.NOTSIDE);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
        this.crafting = new InventorySlotCrafting(3, 3) {
            @Override
            protected ItemStack get(final int index) {
                return StackUtil.wrapEmpty(TileEntityBatchCrafter.this.craftingGrid[index]);
            }
            
            @Override
            protected void put(final int index, final ItemStack stack) {
                TileEntityBatchCrafter.this.craftingGrid[index] = stack;
            }
            
            @Override
            public boolean isEmpty() {
                for (final ItemStack stack : TileEntityBatchCrafter.this.craftingGrid) {
                    if (!StackUtil.isEmpty(stack)) {
                        return false;
                    }
                }
                return true;
            }
            
            @Override
            public void clear() {
                Arrays.fill(TileEntityBatchCrafter.this.craftingGrid, StackUtil.emptyStack);
            }
        };
        this.ingredients = new InventorySlotCrafting(3, 3) {
            @Override
            protected ItemStack get(final int index) {
                return TileEntityBatchCrafter.this.ingredientsRow[index].get();
            }
            
            @Override
            protected void put(final int index, final ItemStack stack) {
                TileEntityBatchCrafter.this.ingredientsRow[index].put(stack);
            }
            
            @Override
            public boolean isEmpty() {
                for (final InvSlot slot : TileEntityBatchCrafter.this.ingredientsRow) {
                    if (!slot.isEmpty()) {
                        return false;
                    }
                }
                return true;
            }
            
            @Override
            public void clear() {
                for (final InvSlot slot : TileEntityBatchCrafter.this.ingredientsRow) {
                    slot.clear();
                }
            }
        };
        this.acceptPredicate = (Predicate<Tuple.T2<ItemStack, Integer>>)new Predicate<Tuple.T2<ItemStack, Integer>>() {
            public boolean apply(final Tuple.T2<ItemStack, Integer> input) {
                return TileEntityBatchCrafter.this.ingredientsRow[input.b].accepts(input.a);
            }
        };
        this.recipe = null;
        this.canCraft = false;
        this.newChange = true;
        this.attemptToBalance = false;
        this.recipeOutput = StackUtil.emptyStack;
        this.progress = 0;
        this.guiProgress = 0.0f;
        for (int i = 0; i < this.ingredientsRow.length; ++i) {
            final int slot = i;
            this.ingredientsRow[slot] = new InvSlot(this, "ingredient[" + slot + ']', InvSlot.Access.I, 1) {
                @Override
                public boolean accepts(final ItemStack ingredient) {
                    final IRecipe recipe = TileEntityBatchCrafter.this.world.isRemote ? TileEntityBatchCrafter.this.findRecipe() : TileEntityBatchCrafter.this.recipe;
                    if (recipe == null) {
                        return false;
                    }
                    assert recipe.matches(TileEntityBatchCrafter.this.crafting, TileEntityBatchCrafter.this.world);
                    final ItemStack recipeStack = TileEntityBatchCrafter.this.craftingGrid[slot];
                    try {
                        TileEntityBatchCrafter.this.craftingGrid[slot] = ingredient;
                        return recipe.matches(TileEntityBatchCrafter.this.crafting, TileEntityBatchCrafter.this.world);
                    }
                    finally {
                        TileEntityBatchCrafter.this.craftingGrid[slot] = recipeStack;
                    }
                }
                
                @Override
                public void onChanged() {
                    super.onChanged();
                    TileEntityBatchCrafter.this.ingredientChange(slot);
                }
            };
        }
        this.energyConsume = 2;
        this.operationLength = 40;
        this.operationsPerTick = 1;
        this.comparator.setUpdate(() -> this.progress * 15 / this.operationLength);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.progress = nbt.getShort("progress");
        final NBTTagList grid = nbt.getTagList("grid", 10);
        for (int i = 0; i < grid.tagCount(); ++i) {
            final NBTTagCompound contentTag = grid.getCompoundTagAt(i);
            this.craftingGrid[contentTag.getByte("index")] = new ItemStack(contentTag);
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("progress", this.progress);
        final NBTTagList grid = new NBTTagList();
        for (byte i = 0; i < this.craftingGrid.length; ++i) {
            final ItemStack content = this.craftingGrid[i];
            if (!StackUtil.isEmpty(content)) {
                final NBTTagCompound contentTag = new NBTTagCompound();
                contentTag.setByte("index", i);
                content.writeToNBT(contentTag);
                grid.appendTag((NBTBase)contentTag);
            }
        }
        nbt.setTag("grid", (NBTBase)grid);
        return nbt;
    }
    
    protected IRecipe findRecipe() {
        final World world = this.getWorld();
        return CraftingManager.findMatchingRecipe(this.crafting, world);
    }
    
    public void matrixChange(final int slot) {
        if (this.recipe == null || !this.recipe.matches(this.crafting, this.getWorld())) {
            this.recipe = this.findRecipe();
        }
        this.recipeOutput = ((this.recipe != null) ? this.recipe.getCraftingResult(this.crafting) : StackUtil.emptyStack);
        this.newChange = true;
    }
    
    public void ingredientChange(final int slot) {
        this.newChange = true;
    }
    
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            this.setOverclockRates();
            this.matrixChange(-1);
        }
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (!this.getWorld().isRemote) {
            this.setOverclockRates();
            this.attemptToBalance = true;
        }
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.attemptToBalance) {
            if (!this.ingredients.isEmpty()) {
                needsInvUpdate |= !((TIntCollection)StackUtil.balanceStacks((IInventory)this.ingredients, this.acceptPredicate).b).isEmpty();
            }
            this.attemptToBalance = false;
        }
        if (this.newChange) {
            this.canCraft = this.canCraft();
            this.newChange = false;
        }
        if (this.canCraft && this.craftingOutput.canAdd(this.recipeOutput) && this.energy.useEnergy(this.energyConsume)) {
            this.setActive(true);
            if (++this.progress >= this.operationLength) {
                this.doCrafting();
                needsInvUpdate = (this.newChange = true);
                this.progress = 0;
            }
        }
        else {
            if (!this.hasRecipe()) {
                this.progress = 0;
            }
            this.setActive(false);
        }
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        this.guiProgress = this.progress / (float)this.operationLength;
        if (needsInvUpdate) {
            super.markDirty();
        }
    }
    
    public boolean hasRecipe() {
        return this.recipe != null;
    }
    
    public boolean canCraft() {
        if (!this.hasRecipe()) {
            return false;
        }
        for (int slot = 0; slot < this.craftingGrid.length; ++slot) {
            if (!StackUtil.isEmpty(this.craftingGrid[slot]) && !this.ingredientsRow[slot].accepts(this.ingredientsRow[slot].get())) {
                return false;
            }
        }
        return true;
    }
    
    protected void doCrafting() {
        for (int operation = 0; operation < this.operationsPerTick; ++operation) {
            final List<ItemStack> outputs = Collections.singletonList(this.recipeOutput);
            for (final ItemStack stack : this.upgradeSlot) {
                if (stack != null && stack.getItem() instanceof IUpgradeItem) {
                    ((IUpgradeItem)stack.getItem()).onProcessEnd(stack, this, outputs);
                }
            }
            this.craft();
            if (!this.hasRecipe()) {
                break;
            }
            if (!this.craftingOutput.canAdd(this.recipeOutput)) {
                break;
            }
        }
    }
    
    protected void craft() {
        assert this.hasRecipe();
        assert this.craftingOutput.canAdd(this.recipeOutput);
        this.craftingOutput.add(this.recipeOutput);
        final List<ItemStack> stacks = (List<ItemStack>)this.recipe.getRemainingItems(this.ingredients);
        final World world = this.getWorld();
        for (int slot = 0; slot < this.ingredientsRow.length; ++slot) {
            ItemStack oldStack = this.ingredientsRow[slot].get();
            if (!StackUtil.isEmpty(oldStack) && !StackUtil.isEmpty(this.craftingGrid[slot])) {
                oldStack = StackUtil.decSize(oldStack);
                this.ingredientsRow[slot].put(oldStack);
            }
            if (stacks.size() > slot && !StackUtil.isEmpty(stacks.get(slot))) {
                final ItemStack newStack = stacks.get(slot);
                if (StackUtil.isEmpty(oldStack) && this.ingredientsRow[slot].accepts(newStack)) {
                    this.ingredientsRow[slot].put(newStack);
                }
                else if (StackUtil.checkItemEqualityStrict(oldStack, newStack)) {
                    this.ingredientsRow[slot].put(StackUtil.incSize(newStack, StackUtil.getSize(oldStack)));
                }
                else if (this.containerOutput.canAdd(newStack)) {
                    this.containerOutput.add(newStack);
                }
                else {
                    StackUtil.dropAsEntity(world, this.pos, newStack);
                }
            }
        }
        for (int i = this.ingredientsRow.length; i < stacks.size(); ++i) {
            final ItemStack newStack2 = stacks.get(i);
            if (this.containerOutput.canAdd(newStack2)) {
                this.containerOutput.add(newStack2);
            }
            else {
                StackUtil.dropAsEntity(world, this.pos, newStack2);
            }
        }
    }
    
    protected void setOverclockRates() {
        this.upgradeSlot.onChanged();
        final double previousProgress = this.progress / (double)this.operationLength;
        this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(40);
        this.operationLength = this.upgradeSlot.getOperationLength(40);
        this.energyConsume = this.upgradeSlot.getEnergyDemand(2);
        final int tier = this.upgradeSlot.getTier(1);
        this.energy.setSinkTier(tier);
        this.dischargeSlot.setTier(tier);
        this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(20000, 40, 2));
        this.progress = (short)Math.floor(previousProgress * this.operationLength + 0.1);
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        switch (event) {
            case 0: {
                this.matrixChange(-1);
                break;
            }
        }
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return TileEntityBatchCrafter.UPGRADES;
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerBatchCrafter(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiBatchCrafter(new ContainerBatchCrafter(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("progress".equals(name)) {
            return this.guiProgress;
        }
        throw new IllegalArgumentException("Unexpected value requested: " + name);
    }
    
    static {
        UPGRADES = EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
