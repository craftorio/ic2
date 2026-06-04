package ic2.core.util;

import ic2.api.util.FluidContainerOutputMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.apache.commons.lang3.mutable.MutableObject;

public class LiquidUtil {
  public static List<Fluid> getAllFluids() {
    Set<Fluid> fluids = new HashSet<>(FluidRegistry.getRegisteredFluids().values());
    fluids.remove(null);
    List<Fluid> ret = new ArrayList<>(fluids);
    Collections.sort(ret, new Comparator<Fluid>() {
          public int compare(Fluid a, Fluid b) {
            String nameA = a.getName();
            String nameB = b.getName();
            if (nameA == null) {
              if (nameB == null)
                return 0; 
              return 1;
            } 
            if (nameB == null)
              return -1; 
            return nameA.toLowerCase(Locale.ENGLISH).compareTo(nameB.toLowerCase(Locale.ENGLISH));
          }
        });
    return ret;
  }
  
  public static LiquidData getLiquid(World world, BlockPos pos) {
    IBlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    Fluid liquid = null;
    boolean isSource = false;
    if (block instanceof IFluidBlock) {
      IFluidBlock fblock = (IFluidBlock)block;
      liquid = fblock.getFluid();
      isSource = fblock.canDrain(world, pos);
    } else if (block == Blocks.WATER || block == Blocks.field_150358_i) {
      liquid = FluidRegistry.WATER;
      isSource = (((Integer)state.func_177229_b((IProperty)BlockLiquid.field_176367_b)).intValue() == 0);
    } else if (block == Blocks.field_150353_l || block == Blocks.field_150356_k) {
      liquid = FluidRegistry.LAVA;
      isSource = (((Integer)state.func_177229_b((IProperty)BlockLiquid.field_176367_b)).intValue() == 0);
    } 
    if (liquid != null)
      return new LiquidData(liquid, isSource); 
    return null;
  }
  
  public static class LiquidData {
    public final Fluid liquid;
    
    public final boolean isSource;
    
    LiquidData(Fluid liquid1, boolean isSource1) {
      this.liquid = liquid1;
      this.isSource = isSource1;
    }
  }
  
  public static boolean isFluidContainer(ItemStack stack) {
    return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
  }
  
  public static boolean isDrainableFluidContainer(ItemStack stack) {
    if (!isFluidContainer(stack))
      return false; 
    ItemStack singleStack = StackUtil.copyWithSize(stack, 1);
    IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    if (handler == null)
      return false; 
    FluidStack fs = handler.drain(2147483647, false);
    return (fs != null && fs.amount > 0);
  }
  
  public static boolean isFillableFluidContainer(ItemStack stack) {
    return isFillableFluidContainer(stack, null);
  }
  
  public static boolean isFillableFluidContainer(ItemStack stack, Iterable<Fluid> testFluids) {
    if (!isFluidContainer(stack))
      return false; 
    if (testFluids == null)
      testFluids = registeredFluids; 
    ItemStack singleStack = StackUtil.copyWithSize(stack, 1);
    IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    if (handler == null)
      return false; 
    FluidStack fs = handler.drain(2147483647, false);
    if (fs != null && 
      testFillFluid(handler, fs.getFluid(), fs.tag))
      return true; 
    for (IFluidTankProperties properties : handler.getTankProperties()) {
      fs = properties.getContents();
      if (fs != null && testFillFluid(handler, fs.getFluid(), fs.tag))
        return true; 
    } 
    for (Fluid fluid : registeredFluids) {
      if (testFillFluid(handler, fluid, null))
        return true; 
    } 
    return false;
  }
  
  private static boolean testFillFluid(IFluidHandlerItem handler, Fluid fluid, NBTTagCompound nbt) {
    FluidStack fs = new FluidStack(fluid, 2147483647);
    fs.tag = nbt;
    return (handler.fill(fs, false) > 0);
  }
  
  private static final Collection<Fluid> registeredFluids = FluidRegistry.getRegisteredFluids().values();
  
  public static FluidStack drainContainer(EntityPlayer player, EnumHand hand, Fluid fluid, int maxAmount, FluidContainerOutputMode outputMode, boolean simulate) {
    ItemStack stack = StackUtil.get(player, hand);
    FluidOperationResult result = drainContainer(stack, fluid, maxAmount, outputMode);
    if (result == null)
      return null; 
    if (result.extraOutput != null && 
      !StackUtil.storeInventoryItem(result.extraOutput, player, simulate))
      return null; 
    if (!simulate)
      StackUtil.set(player, hand, result.inPlaceOutput); 
    return result.fluidChange;
  }
  
  public static int fillContainer(EntityPlayer player, EnumHand hand, FluidStack fs, FluidContainerOutputMode outputMode, boolean simulate) {
    ItemStack stack = StackUtil.get(player, hand);
    FluidOperationResult result = fillContainer(stack, fs, outputMode);
    if (result == null)
      return 0; 
    if (result.extraOutput != null && 
      !StackUtil.storeInventoryItem(result.extraOutput, player, simulate))
      return 0; 
    if (!simulate)
      StackUtil.set(player, hand, result.inPlaceOutput); 
    return result.fluidChange.amount;
  }
  
  public static FluidOperationResult drainContainer(ItemStack stack, Fluid fluid, int maxAmount, FluidContainerOutputMode outputMode) {
    FluidStack fs;
    if (StackUtil.isEmpty(stack) || maxAmount <= 0)
      return null; 
    ItemStack inPlace = StackUtil.copy(stack);
    ItemStack extra = null;
    if (inPlace.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
      ItemStack singleStack = StackUtil.copyWithSize(inPlace, 1);
      IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
      if (handler == null)
        return null; 
      if (fluid == null) {
        fs = handler.drain(maxAmount, true);
      } else {
        fs = handler.drain(new FluidStack(fluid, maxAmount), true);
      } 
      if (fs == null || fs.amount <= 0)
        return null; 
      if (StackUtil.isEmpty(singleStack)) {
        inPlace = StackUtil.decSize(inPlace);
      } else {
        FluidStack leftOver = handler.drain(2147483647, false);
        boolean isEmpty = (leftOver == null || leftOver.amount <= 0);
        if ((isEmpty && outputMode.isOutputEmptyFull()) || outputMode == FluidContainerOutputMode.AnyToOutput || (outputMode == FluidContainerOutputMode.InPlacePreferred && 
          
          StackUtil.getSize(inPlace) > 1)) {
          extra = handler.getContainer();
          inPlace = StackUtil.decSize(inPlace);
        } else {
          if (StackUtil.getSize(inPlace) > 1)
            return null; 
          inPlace = handler.getContainer();
        } 
      } 
    } else {
      return null;
    } 
    assert fs.amount > 0;
    return new FluidOperationResult(fs, inPlace, extra);
  }
  
  public static FluidOperationResult fillContainer(ItemStack stack, FluidStack fsIn, FluidContainerOutputMode outputMode) {
    FluidStack fsChange;
    ItemStack singleStack;
    boolean isFull;
    if (StackUtil.isEmpty(stack) || fsIn == null || fsIn.amount <= 0)
      return null; 
    ItemStack inPlace = StackUtil.copy(stack);
    ItemStack extra = null;
    if (inPlace.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
      singleStack = StackUtil.copyWithSize(inPlace, 1);
      IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
      if (handler == null)
        return null; 
      fsChange = fsIn.copy();
      int amount = handler.fill(fsChange, true);
      if (amount <= 0)
        return null; 
      fsChange.amount = amount;
      FluidStack fillTestFs = fsIn.copy();
      fillTestFs.amount = Integer.MAX_VALUE;
      isFull = (handler.fill(fillTestFs, false) <= 0);
      singleStack = handler.getContainer();
    } else {
      return null;
    } 
    assert fsChange.getFluid() == fsIn.getFluid();
    assert fsChange.amount > 0;
    assert StackUtil.getSize(singleStack) == 1;
    if ((isFull && outputMode.isOutputEmptyFull()) || outputMode == FluidContainerOutputMode.AnyToOutput || (outputMode == FluidContainerOutputMode.InPlacePreferred && 
      
      StackUtil.getSize(inPlace) > 1)) {
      extra = singleStack;
      inPlace = StackUtil.decSize(inPlace);
    } else {
      if (StackUtil.getSize(inPlace) > 1)
        return null; 
      inPlace = singleStack;
    } 
    return new FluidOperationResult(fsChange, inPlace, extra);
  }
  
  public static class FluidOperationResult {
    public final FluidStack fluidChange;
    
    public final ItemStack inPlaceOutput;
    
    public final ItemStack extraOutput;
    
    FluidOperationResult(FluidStack fluidChange, ItemStack inPlaceOutput, ItemStack extraOutput) {
      if (fluidChange == null)
        throw new NullPointerException("null fluid change"); 
      this.fluidChange = fluidChange;
      this.inPlaceOutput = inPlaceOutput;
      this.extraOutput = extraOutput;
    }
  }
  
  public static boolean isFluidTile(TileEntity te, EnumFacing side) {
    return (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side));
  }
  
  public static FluidStack drainTile(TileEntity te, EnumFacing side, int maxAmount, boolean simulate) {
    return drainTile(te, side, null, maxAmount, simulate);
  }
  
  public static FluidStack drainTile(TileEntity te, EnumFacing side, Fluid fluid, int maxAmount, boolean simulate) {
    if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
      IFluidHandler handler = (IFluidHandler)te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
      if (handler == null)
        return null; 
      if (fluid == null)
        return handler.drain(maxAmount, !simulate); 
      return handler.drain(new FluidStack(fluid, maxAmount), !simulate);
    } 
    return null;
  }
  
  public static int fillTile(TileEntity te, EnumFacing side, FluidStack fs, boolean simulate) {
    if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
      IFluidHandler handler = (IFluidHandler)te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
      if (handler == null)
        return 0; 
      return handler.fill(fs, !simulate);
    } 
    return 0;
  }
  
  public static List<AdjacentFluidHandler> getAdjacentHandlers(TileEntity source) {
    List<AdjacentFluidHandler> ret = new ArrayList<>();
    for (EnumFacing dir : EnumFacing.VALUES) {
      TileEntity te = source.getWorld().getTileEntity(source.getPos().offset(dir));
      if (isFluidTile(te, dir.getOpposite()))
        ret.add(new AdjacentFluidHandler(te, dir)); 
    } 
    return ret;
  }
  
  public static AdjacentFluidHandler getAdjacentHandler(TileEntity source, EnumFacing dir) {
    TileEntity te = source.getWorld().getTileEntity(source.getPos().offset(dir));
    if (!isFluidTile(te, dir.getOpposite()))
      return null; 
    return new AdjacentFluidHandler(te, dir);
  }
  
  public static class AdjacentFluidHandler {
    public final TileEntity handler;
    
    public final EnumFacing dir;
    
    AdjacentFluidHandler(TileEntity handler, EnumFacing dir) {
      this.handler = handler;
      this.dir = dir;
    }
  }
  
  public static int distribute(TileEntity source, FluidStack stack, boolean simulate) {
    int transferred = 0;
    for (AdjacentFluidHandler handler : getAdjacentHandlers(source)) {
      int amount = fillTile(handler.handler, handler.dir.getOpposite(), stack, simulate);
      transferred += amount;
      stack.amount -= amount;
      if (stack.amount <= 0)
        break; 
    } 
    stack.amount += transferred;
    return transferred;
  }
  
  public static int distributeAll(TileEntity source, int amount) {
    if (source == null)
      throw new IllegalArgumentException("source has to be a tile entity"); 
    TileEntity srcTe = source;
    int transferred = 0;
    for (EnumFacing dir : EnumFacing.VALUES) {
      TileEntity te = srcTe.getWorld().getTileEntity(srcTe.getPos().offset(dir));
      if (isFluidTile(te, dir.getOpposite())) {
        FluidStack stack = transfer(source, dir, te, amount);
        if (stack != null) {
          amount -= stack.amount;
          transferred += stack.amount;
          if (amount <= 0)
            break; 
        } 
      } 
    } 
    return transferred;
  }
  
  public static FluidStack transfer(TileEntity source, EnumFacing dir, TileEntity target, int amount) {
    do {
      ret = drainTile(source, dir, amount, true);
      if (ret == null || ret.amount <= 0)
        return null; 
      if (ret.amount > amount)
        throw new IllegalStateException("The fluid handler " + source + " drained more than the requested amount."); 
      int cAmount = fillTile(target, getOppositeDir(dir), ret.copy(), true);
      if (cAmount > amount)
        throw new IllegalStateException("The fluid handler " + target + " filled more than the requested amount."); 
      amount = cAmount;
    } while (amount != ret.amount && amount > 0);
    if (amount <= 0)
      return null; 
    FluidStack ret = drainTile(source, dir, amount, false);
    if (ret == null)
      throw new IllegalStateException("The fluid handler " + source + " drained inconsistently. Expected " + amount + ", couldn't find previous IFluidHandler facing " + dir + '.'); 
    if (ret.amount != amount)
      throw new IllegalStateException("The fluid handler " + source + " drained inconsistently. Expected " + amount + ", got " + ret.amount + '.'); 
    amount = fillTile(target, getOppositeDir(dir), ret.copy(), false);
    if (amount != ret.amount)
      throw new IllegalStateException("The fluid handler " + target + " filled inconsistently. Expected " + ret.amount + ", got " + amount + '.'); 
    return ret;
  }
  
  private static EnumFacing getOppositeDir(EnumFacing dir) {
    if (dir == null)
      return null; 
    return dir.getOpposite();
  }
  
  public static boolean check(FluidStack fs) {
    return (fs.getFluid() != null);
  }
  
  public static FluidStack drainBlock(World world, BlockPos pos, boolean simulate) {
    IBlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    if (block instanceof IFluidBlock) {
      IFluidBlock liquid = (IFluidBlock)block;
      if (liquid.canDrain(world, pos))
        return liquid.drain(world, pos, !simulate); 
    } else if (block instanceof BlockLiquid && ((Integer)state.func_177229_b((IProperty)BlockLiquid.field_176367_b)).intValue() == 0) {
      FluidStack fluid = null;
      if (block == Blocks.WATER || block == Blocks.field_150358_i) {
        fluid = new FluidStack(FluidRegistry.WATER, 1000);
      } else if (block == Blocks.field_150353_l || block == Blocks.field_150356_k) {
        fluid = new FluidStack(FluidRegistry.LAVA, 1000);
      } 
      if (fluid != null && !simulate)
        world.func_175698_g(pos); 
      return fluid;
    } 
    return null;
  }
  
  public static boolean drainBlockToContainer(World world, BlockPos pos, EntityPlayer player, EnumHand hand) {
    FluidStack fs = drainBlock(world, pos, true);
    if (fs == null || fs.amount <= 0)
      return false; 
    int amount = fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, true);
    if (amount != fs.amount)
      return false; 
    fs = drainBlock(world, pos, false);
    fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, false);
    return true;
  }
  
  public static boolean fillBlock(FluidStack fs, World world, BlockPos pos, boolean simulate) {
    if (fs == null || fs.amount < 1000)
      return false; 
    Fluid fluid = fs.getFluid();
    if (fluid == null || !fluid.canBePlacedInWorld() || fluid.getBlock() == null)
      return false; 
    IBlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    if (!block.isAir(state, (IBlockAccess)world, pos) && state.getMaterial().func_76220_a())
      return false; 
    if (block == fluid.getBlock() && isFullFluidBlock(world, pos, block, state))
      return false; 
    if (simulate)
      return true; 
    Block fluidBlock;
    if (world.provider.func_177500_n() && (
      fluidBlock = fluid.getBlock()) != null && fluidBlock
      .getDefaultState().getMaterial() == Material.WATER) {
      world.func_184133_a(null, pos, SoundEvents.field_187646_bt, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand
          .nextFloat() - world.rand.nextFloat()) * 0.8F);
      for (int i = 0; i < 8; i++)
        world.func_175688_a(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]); 
    } else {
      if (!world.isRemote && !state.getMaterial().func_76220_a() && !state.getMaterial().func_76224_d())
        world.func_175655_b(pos, true); 
      if (fluid == FluidRegistry.WATER) {
        BlockDynamicLiquid blockDynamicLiquid = Blocks.field_150358_i;
      } else if (fluid == FluidRegistry.LAVA) {
        BlockDynamicLiquid blockDynamicLiquid = Blocks.field_150356_k;
      } else {
        block = fluid.getBlock();
      } 
      if (!world.func_175656_a(pos, block.getDefaultState()))
        return false; 
    } 
    fs.amount -= 1000;
    return true;
  }
  
  private static boolean isFullFluidBlock(World world, BlockPos pos, Block block, IBlockState state) {
    if (block instanceof IFluidBlock) {
      IFluidBlock fBlock = (IFluidBlock)block;
      FluidStack drained = fBlock.drain(world, pos, false);
      return (drained != null && drained.amount >= 1000);
    } 
    if (state.func_177228_b().containsKey(BlockLiquid.field_176367_b))
      return (((Integer)state.func_177229_b((IProperty)BlockLiquid.field_176367_b)).intValue() == 0); 
    return false;
  }
  
  public static boolean fillBlockFromContainer(World world, BlockPos pos, EntityPlayer player, EnumHand hand) {
    FluidStack fs = drainContainer(player, hand, null, 1000, FluidContainerOutputMode.InPlacePreferred, true);
    if (fs == null || fs.amount < 1000)
      return false; 
    if (!fillBlock(fs, world, pos, false))
      return false; 
    drainContainer(player, hand, null, 1000, FluidContainerOutputMode.InPlacePreferred, false);
    return true;
  }
  
  public static boolean storeOutputContainer(MutableObject<ItemStack> output, EntityPlayer player) {
    if (output.getValue() == null)
      return true; 
    return StackUtil.storeInventoryItem((ItemStack)output.getValue(), player, false);
  }
  
  public static String toStringSafe(FluidStack fluidStack) {
    if (fluidStack.getFluid() == null)
      return fluidStack.amount + "(mb)x(null)@(unknown)"; 
    return fluidStack.toString();
  }
}
