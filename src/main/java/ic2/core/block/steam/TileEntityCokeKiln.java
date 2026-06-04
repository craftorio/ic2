// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.steam;

import net.minecraft.client.util.ITooltipFlag;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import ic2.core.ref.BlockName;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.world.World;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import ic2.core.util.ParticleUtil;
import java.util.List;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.tileentity.TileEntity;
import ic2.core.recipe.dynamic.RecipeOutputFluidStack;
import ic2.core.recipe.dynamic.RecipeOutputItemStack;
import ic2.core.recipe.dynamic.RecipeOutputIngredient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.ref.ItemName;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.ref.FluidName;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.IC2;
import ic2.core.network.GuiSynced;
import ic2.core.recipe.dynamic.DynamicRecipe;
import ic2.core.recipe.dynamic.DynamicRecipeManager;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityCokeKiln extends TileEntityInventory implements IMultiBlockController, IHasGui, IGuiValueProvider
{
    protected int tickRate;
    protected int updateTicker;
    protected boolean isFormed;
    protected final InvSlotOutput outputSlot;
    public static DynamicRecipeManager recipeManager;
    protected int progress;
    protected int operationLength;
    protected DynamicRecipe recipe;
    @GuiSynced
    protected float guiProgress;
    
    public TileEntityCokeKiln() {
        this.tickRate = 20;
        this.isFormed = false;
        this.progress = 0;
        this.operationLength = 0;
        this.recipe = null;
        this.updateTicker = IC2.random.nextInt(this.tickRate);
        this.outputSlot = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.ANY);
    }
    
    public static void init() {
        TileEntityCokeKiln.recipeManager = new DynamicRecipeManager();
        TileEntityCokeKiln.recipeManager.createRecipe().withInput("logWood").withOutput(new ItemStack(Items.COAL, 1, 1)).withOutput(new FluidStack(FluidName.creosote.getInstance(), 250)).withOperationDurationTicks(1800).register();
        TileEntityCokeKiln.recipeManager.createRecipe().withInput(new ItemStack(Items.COAL, 1, 0)).withOutput(new ItemStack(ItemName.coke.getInstance(), 1)).withOutput(new FluidStack(FluidName.creosote.getInstance(), 500)).withOperationDurationTicks(1800).register();
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.progress = nbt.getInteger("progress");
        this.operationLength = nbt.getInteger("operationLength");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", this.progress);
        nbt.setInteger("operationLength", this.operationLength);
        return nbt;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.updateTicker++ % this.tickRate != 0) {
            return;
        }
        if (!(this.isFormed = this.hasValidStructure())) {
            this.progress = 0;
            this.guiProgress = 0.0f;
            this.setActive(false);
            return;
        }
        boolean needsInventoryUpdate = false;
        if (this.canWork()) {
            this.setActive(true);
            if (this.progress == 0) {
                needsInventoryUpdate = true;
            }
            final int progressNeeded = this.recipe.getOperationDuration();
            if (this.progress < progressNeeded) {
                this.progress += 20;
            }
            if (this.progress >= progressNeeded) {
                this.finishWork();
                needsInventoryUpdate = true;
            }
        }
        else {
            this.setActive(false);
        }
        if (this.progress == 0 || this.operationLength == 0) {
            this.guiProgress = 0.0f;
        }
        else {
            this.guiProgress = this.progress / (float)this.operationLength;
        }
        if (needsInventoryUpdate) {
            super.markDirty();
        }
    }
    
    protected boolean canWork() {
        final BlockPos hatchPos = new BlockPos(this.pos.getX() + -this.getFacing().getFrontOffsetX(), this.pos.getY() + 1, this.pos.getZ() + -this.getFacing().getFrontOffsetZ());
        final TileEntity hatch = this.world.getTileEntity(hatchPos);
        if (!(hatch instanceof TileEntityCokeKilnHatch)) {
            return false;
        }
        final ItemStack input = ((TileEntityCokeKilnHatch)hatch).inventory.get();
        if (input.isEmpty()) {
            return false;
        }
        if (this.recipe != null) {
            final boolean canUse = TileEntityCokeKiln.recipeManager.apply(this.recipe, new ItemStack[] { input }, new FluidStack[0], true);
            if (!canUse) {
                this.reset();
            }
        }
        DynamicRecipe maybeRecipe = this.recipe;
        if (maybeRecipe != null) {
            for (final RecipeOutputIngredient<?> entry : this.recipe.getOutputIngredients()) {
                if (entry instanceof RecipeOutputItemStack) {
                    if (!this.outputSlot.canAdd((ItemStack)entry.ingredient)) {
                        return false;
                    }
                    continue;
                }
                else {
                    if (!(entry instanceof RecipeOutputFluidStack)) {
                        continue;
                    }
                    final BlockPos gratePos = new BlockPos(this.pos.getX() + -this.getFacing().getFrontOffsetX(), this.pos.getY() - 1, this.pos.getZ() + -this.getFacing().getFrontOffsetZ());
                    final TileEntity grate = this.world.getTileEntity(gratePos);
                    if (!(grate instanceof TileEntityCokeKilnGrate)) {
                        return false;
                    }
                    if (((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal((FluidStack)entry.ingredient, false) < ((FluidStack)entry.ingredient).amount) {
                        return false;
                    }
                    continue;
                }
            }
            return true;
        }
        maybeRecipe = TileEntityCokeKiln.recipeManager.findRecipe(new ItemStack[] { input }, new FluidStack[0]);
        if (maybeRecipe == null) {
            return false;
        }
        this.updateRecipe(maybeRecipe);
        for (final RecipeOutputIngredient<?> entry : this.recipe.getOutputIngredients()) {
            if (entry instanceof RecipeOutputItemStack) {
                if (!this.outputSlot.canAdd((ItemStack)entry.ingredient)) {
                    return false;
                }
                continue;
            }
            else {
                if (!(entry instanceof RecipeOutputFluidStack)) {
                    continue;
                }
                final BlockPos gratePos = new BlockPos(this.pos.getX() + -this.getFacing().getFrontOffsetX(), this.pos.getY() - 1, this.pos.getZ() + -this.getFacing().getFrontOffsetZ());
                final TileEntity grate = this.world.getTileEntity(gratePos);
                if (!(grate instanceof TileEntityCokeKilnGrate)) {
                    return false;
                }
                if (((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal((FluidStack)entry.ingredient, false) < ((FluidStack)entry.ingredient).amount) {
                    return false;
                }
                continue;
            }
        }
        return true;
    }
    
    protected void finishWork() {
        final BlockPos hatchPos = new BlockPos(this.pos.getX() + -this.getFacing().getFrontOffsetX(), this.pos.getY() + 1, this.pos.getZ() + -this.getFacing().getFrontOffsetZ());
        final TileEntity hatch = this.world.getTileEntity(hatchPos);
        if (!(hatch instanceof TileEntityCokeKilnHatch)) {
            return;
        }
        final InvSlot inventory = ((TileEntityCokeKilnHatch)hatch).inventory;
        if (inventory.get().isEmpty()) {
            return;
        }
        TileEntityCokeKiln.recipeManager.apply(this.recipe, new ItemStack[] { inventory.get() }, new FluidStack[0], false);
        final List<ItemStack> itemOutputs = new ArrayList<ItemStack>();
        final List<FluidStack> fluidOutputs = new ArrayList<FluidStack>();
        for (final RecipeOutputIngredient<?> entry : this.recipe.getOutputIngredients()) {
            if (entry instanceof RecipeOutputItemStack) {
                itemOutputs.add(StackUtil.copy((ItemStack)entry.ingredient));
            }
            else {
                if (!(entry instanceof RecipeOutputFluidStack)) {
                    continue;
                }
                fluidOutputs.add(((FluidStack)entry.ingredient).copy());
            }
        }
        for (final ItemStack stack : itemOutputs) {
            final int amount = this.outputSlot.add(StackUtil.copy(stack));
            stack.shrink(amount);
        }
        itemOutputs.clear();
        final BlockPos gratePos = new BlockPos(this.pos.getX() + -this.getFacing().getFrontOffsetX(), this.pos.getY() - 1, this.pos.getZ() + -this.getFacing().getFrontOffsetZ());
        final TileEntity grate = this.world.getTileEntity(gratePos);
        if (grate instanceof TileEntityCokeKilnGrate) {
            for (final FluidStack stack2 : fluidOutputs) {
                final int amount2 = ((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal(stack2, true);
                final FluidStack fluidStack = stack2;
                fluidStack.amount -= amount2;
            }
        }
        fluidOutputs.clear();
        this.progress = 0;
    }
    
    protected void updateRecipe(final DynamicRecipe recipe) {
        this.operationLength = recipe.getOperationDuration();
        this.recipe = recipe;
    }
    
    protected void reset() {
        this.progress = 0;
        this.operationLength = 0;
        this.recipe = null;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        if (this.getActive()) {
            final World world = this.getWorld();
            ParticleUtil.showFlames(world, this.pos, this.getFacing());
            if (world.rand.nextDouble() < 0.1) {
                world.playSound(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            }
        }
    }
    
    @Override
    public boolean hasValidStructure() {
        final int range = 2;
        final ChunkCache cache = new ChunkCache(this.getWorld(), this.pos.add(-2, -2, -2), this.pos.add(2, 2, 2), 0);
        final BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                cPos.setPos(this.pos.getX() + (x - this.getFacing().getFrontOffsetX()), this.pos.getY() - 1, this.pos.getZ() + (z - this.getFacing().getFrontOffsetZ()));
                if (x == 0 && z == 0) {
                    final TileEntity tileEntity = cache.getTileEntity((BlockPos)cPos);
                    if (tileEntity == null) {
                        return false;
                    }
                    if (!(tileEntity instanceof TileEntityCokeKilnGrate)) {
                        return false;
                    }
                }
                else {
                    final IBlockState state = cache.getBlockState((BlockPos)cPos);
                    if (state.getBlock() != BlockName.refractory_bricks.getInstance()) {
                        return false;
                    }
                }
            }
        }
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                cPos.setPos(this.pos.getX() + (x - this.getFacing().getFrontOffsetX()), this.pos.getY(), this.pos.getZ() + (z - this.getFacing().getFrontOffsetZ()));
                if (x == 0 && z == 0) {
                    final IBlockState state = cache.getBlockState((BlockPos)cPos);
                    if (state.getBlock() != Blocks.AIR) {
                        return false;
                    }
                }
                else if (this.pos.getX() == cPos.getX() && this.pos.getZ() == cPos.getZ()) {
                    final TileEntity tileEntity = cache.getTileEntity((BlockPos)cPos);
                    if (tileEntity == null) {
                        return false;
                    }
                    if (tileEntity != this) {
                        return false;
                    }
                }
                else {
                    final IBlockState state = cache.getBlockState((BlockPos)cPos);
                    if (state.getBlock() != BlockName.refractory_bricks.getInstance()) {
                        return false;
                    }
                }
            }
        }
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                cPos.setPos(this.pos.getX() + (x - this.getFacing().getFrontOffsetX()), this.pos.getY() + 1, this.pos.getZ() + (z - this.getFacing().getFrontOffsetZ()));
                if (x == 0 && z == 0) {
                    final TileEntity tileEntity = cache.getTileEntity((BlockPos)cPos);
                    if (tileEntity == null) {
                        return false;
                    }
                    if (!(tileEntity instanceof TileEntityCokeKilnHatch)) {
                        return false;
                    }
                }
                else {
                    final IBlockState state = cache.getBlockState((BlockPos)cPos);
                    if (state.getBlock() != BlockName.refractory_bricks.getInstance()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    public boolean isFormed() {
        return this.isFormed;
    }
    
    @Override
    public ContainerBase<TileEntityCokeKiln> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getGuiValue(final String name) {
        if (name.equals("progress")) {
            return this.guiProgress;
        }
        throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final List<String> info, final ITooltipFlag advanced) {
        info.add("");
        info.add("MultiBlock Structure:");
        info.add("");
        info.add(" Bottom Layer - 3x3 of Refractory Blocks with a Coke Kiln Grate in the centre");
        info.add("");
        info.add(" Middle Layer - 3x3 of Refractory Blocks with a hollow centre and this block in the middle of one of the sides");
        info.add("");
        info.add(" Top Layer - 3x3 of Refractory Blocks with a Coke Kiln Hatch in the centre");
        info.add("");
    }
}
