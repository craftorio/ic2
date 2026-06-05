package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.recipe.dynamic.DynamicRecipe;
import ic2.core.recipe.dynamic.DynamicRecipeManager;
import ic2.core.recipe.dynamic.RecipeOutputFluidStack;
import ic2.core.recipe.dynamic.RecipeOutputIngredient;
import ic2.core.recipe.dynamic.RecipeOutputItemStack;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.ParticleUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCokeKiln extends TileEntityInventory implements IMultiBlockController, IHasGui, IGuiValueProvider {
   protected int tickRate = 20;
   protected int updateTicker;
   protected boolean isFormed = false;
   protected final InvSlotOutput outputSlot;
   public static DynamicRecipeManager recipeManager;
   protected int progress = 0;
   protected int operationLength = 0;
   protected DynamicRecipe recipe = null;
   @GuiSynced
   protected float guiProgress;

   public TileEntityCokeKiln() {
      this.updateTicker = IC2.random.nextInt(this.tickRate);
      this.outputSlot = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.ANY);
   }

   public static void init() {
      recipeManager = new DynamicRecipeManager();
      recipeManager.createRecipe()
         .withInput("logWood")
         .withOutput(new ItemStack(Items.COAL, 1, 1))
         .withOutput(new FluidStack(FluidName.creosote.getInstance(), 250))
         .withOperationDurationTicks(1800)
         .register();
      recipeManager.createRecipe()
         .withInput(new ItemStack(Items.COAL, 1, 0))
         .withOutput(new ItemStack(ItemName.coke.getInstance(), 1))
         .withOutput(new FluidStack(FluidName.creosote.getInstance(), 500))
         .withOperationDurationTicks(1800)
         .register();
   }

   @Override
   public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      this.progress = nbt.getInteger("progress");
      this.operationLength = nbt.getInteger("operationLength");
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      nbt.setInteger("progress", this.progress);
      nbt.setInteger("operationLength", this.operationLength);
      return nbt;
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      if (this.updateTicker++ % this.tickRate == 0) {
         this.isFormed = this.hasValidStructure();
         if (!this.isFormed) {
            this.progress = 0;
            this.guiProgress = 0.0F;
            this.setActive(false);
         } else {
            boolean needsInventoryUpdate = false;
            if (this.canWork()) {
               this.setActive(true);
               if (this.progress == 0) {
                  needsInventoryUpdate = true;
               }

               int progressNeeded = this.recipe.getOperationDuration();
               if (this.progress < progressNeeded) {
                  this.progress += 20;
               }

               if (this.progress >= progressNeeded) {
                  this.finishWork();
                  needsInventoryUpdate = true;
               }
            } else {
               this.setActive(false);
            }

            if (this.progress != 0 && this.operationLength != 0) {
               this.guiProgress = (float)this.progress / this.operationLength;
            } else {
               this.guiProgress = 0.0F;
            }

            if (needsInventoryUpdate) {
               super.markDirty();
            }
         }
      }
   }

   protected boolean canWork() {
      BlockPos hatchPos = new BlockPos(
         this.pos.getX() + -this.getFacing().getFrontOffsetX(),
         this.pos.getY() + 1,
         this.pos.getZ() + -this.getFacing().getFrontOffsetZ()
      );
      TileEntity hatch = this.world.getTileEntity(hatchPos);
      if (!(hatch instanceof TileEntityCokeKilnHatch)) {
         return false;
      }

      ItemStack input = ((TileEntityCokeKilnHatch)hatch).inventory.get();
      if (input.isEmpty()) {
         return false;
      }

      if (this.recipe != null) {
         boolean canUse = recipeManager.apply(this.recipe, new ItemStack[]{input}, new FluidStack[0], true);
         if (!canUse) {
            this.reset();
         }
      }

      DynamicRecipe maybeRecipe = this.recipe;
      if (maybeRecipe != null) {
         for (RecipeOutputIngredient<?> entry : this.recipe.getOutputIngredients()) {
            if (entry instanceof RecipeOutputItemStack) {
               if (!this.outputSlot.canAdd((ItemStack)entry.ingredient)) {
                  return false;
               }
            } else if (entry instanceof RecipeOutputFluidStack) {
               BlockPos gratePos = new BlockPos(
                  this.pos.getX() + -this.getFacing().getFrontOffsetX(),
                  this.pos.getY() - 1,
                  this.pos.getZ() + -this.getFacing().getFrontOffsetZ()
               );
               TileEntity grate = this.world.getTileEntity(gratePos);
               if (!(grate instanceof TileEntityCokeKilnGrate)) {
                  return false;
               }

               if (((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal((FluidStack)entry.ingredient, false) < ((FluidStack)entry.ingredient).amount) {
                  return false;
               }
            }
         }

         return true;
      } else {
         maybeRecipe = recipeManager.findRecipe(new ItemStack[]{input}, new FluidStack[0]);
         if (maybeRecipe == null) {
            return false;
         }

         this.updateRecipe(maybeRecipe);

         for (RecipeOutputIngredient<?> entry : this.recipe.getOutputIngredients()) {
            if (entry instanceof RecipeOutputItemStack) {
               if (!this.outputSlot.canAdd((ItemStack)entry.ingredient)) {
                  return false;
               }
            } else if (entry instanceof RecipeOutputFluidStack) {
               BlockPos gratePos = new BlockPos(
                  this.pos.getX() + -this.getFacing().getFrontOffsetX(),
                  this.pos.getY() - 1,
                  this.pos.getZ() + -this.getFacing().getFrontOffsetZ()
               );
               TileEntity grate = this.world.getTileEntity(gratePos);
               if (!(grate instanceof TileEntityCokeKilnGrate)) {
                  return false;
               }

               if (((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal((FluidStack)entry.ingredient, false) < ((FluidStack)entry.ingredient).amount) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   protected void finishWork() {
      BlockPos hatchPos = new BlockPos(
         this.pos.getX() + -this.getFacing().getFrontOffsetX(),
         this.pos.getY() + 1,
         this.pos.getZ() + -this.getFacing().getFrontOffsetZ()
      );
      TileEntity hatch = this.world.getTileEntity(hatchPos);
      if (hatch instanceof TileEntityCokeKilnHatch) {
         InvSlot inventory = ((TileEntityCokeKilnHatch)hatch).inventory;
         if (!inventory.get().isEmpty()) {
            recipeManager.apply(this.recipe, new ItemStack[]{inventory.get()}, new FluidStack[0], false);
            List<ItemStack> itemOutputs = new ArrayList<>();
            List<FluidStack> fluidOutputs = new ArrayList<>();

            for (RecipeOutputIngredient<?> entry : this.recipe.getOutputIngredients()) {
               if (entry instanceof RecipeOutputItemStack) {
                  itemOutputs.add(StackUtil.copy((ItemStack)entry.ingredient));
               } else if (entry instanceof RecipeOutputFluidStack) {
                  fluidOutputs.add(((FluidStack)entry.ingredient).copy());
               }
            }

            for (ItemStack stack : itemOutputs) {
               int amount = this.outputSlot.add(StackUtil.copy(stack));
               stack.shrink(amount);
            }

            itemOutputs.clear();
            BlockPos gratePos = new BlockPos(
               this.pos.getX() + -this.getFacing().getFrontOffsetX(),
               this.pos.getY() - 1,
               this.pos.getZ() + -this.getFacing().getFrontOffsetZ()
            );
            TileEntity grate = this.world.getTileEntity(gratePos);
            if (grate instanceof TileEntityCokeKilnGrate) {
               for (FluidStack stack : fluidOutputs) {
                  int amount = ((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal(stack, true);
                  stack.amount -= amount;
               }
            }

            fluidOutputs.clear();
            this.progress = 0;
         }
      }
   }

   protected void updateRecipe(DynamicRecipe recipe) {
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
         World world = this.getWorld();
         ParticleUtil.showFlames(world, this.pos, this.getFacing());
         if (world.rand.nextDouble() < 0.1) {
            world.playSound(
               this.pos.getX() + 0.5,
               this.pos.getY() + 0.5,
               this.pos.getZ() + 0.5,
               SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE,
               SoundCategory.BLOCKS,
               1.0F,
               1.0F,
               false
            );
         }
      }
   }

   @Override
   public boolean hasValidStructure() {
      int range = 2;
      ChunkCache cache = new ChunkCache(this.getWorld(), this.pos.add(-2, -2, -2), this.pos.add(2, 2, 2), 0);
      MutableBlockPos cPos = new MutableBlockPos();

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            cPos.setPos(
               this.pos.getX() + (x - this.getFacing().getFrontOffsetX()),
               this.pos.getY() - 1,
               this.pos.getZ() + (z - this.getFacing().getFrontOffsetZ())
            );
            if (x == 0 && z == 0) {
               TileEntity tileEntity = cache.getTileEntity(cPos);
               if (tileEntity == null) {
                  return false;
               }

               if (!(tileEntity instanceof TileEntityCokeKilnGrate)) {
                  return false;
               }
            } else {
               IBlockState state = cache.getBlockState(cPos);
               if (state.getBlock() != BlockName.refractory_bricks.getInstance()) {
                  return false;
               }
            }
         }
      }

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            cPos.setPos(
               this.pos.getX() + (x - this.getFacing().getFrontOffsetX()),
               this.pos.getY(),
               this.pos.getZ() + (z - this.getFacing().getFrontOffsetZ())
            );
            if (x == 0 && z == 0) {
               IBlockState state = cache.getBlockState(cPos);
               if (state.getBlock() != Blocks.AIR) {
                  return false;
               }
            } else if (this.pos.getX() == cPos.getX() && this.pos.getZ() == cPos.getZ()) {
               TileEntity tileEntity = cache.getTileEntity(cPos);
               if (tileEntity == null) {
                  return false;
               }

               if (tileEntity != this) {
                  return false;
               }
            } else {
               IBlockState state = cache.getBlockState(cPos);
               if (state.getBlock() != BlockName.refractory_bricks.getInstance()) {
                  return false;
               }
            }
         }
      }

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            cPos.setPos(
               this.pos.getX() + (x - this.getFacing().getFrontOffsetX()),
               this.pos.getY() + 1,
               this.pos.getZ() + (z - this.getFacing().getFrontOffsetZ())
            );
            if (x == 0 && z == 0) {
               TileEntity tileEntity = cache.getTileEntity(cPos);
               if (tileEntity == null) {
                  return false;
               }

               if (!(tileEntity instanceof TileEntityCokeKilnHatch)) {
                  return false;
               }
            } else {
               IBlockState state = cache.getBlockState(cPos);
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
   public ContainerBase<TileEntityCokeKiln> getGuiContainer(EntityPlayer player) {
      return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return DynamicGui.<TileEntityCokeKiln>create(this, player, GuiParser.parse(this.teBlock));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   @Override
   public double getGuiValue(String name) {
      if (name.equals("progress")) {
         return this.guiProgress;
      } else {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void addInformation(ItemStack stack, List<String> info, ITooltipFlag advanced) {
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
