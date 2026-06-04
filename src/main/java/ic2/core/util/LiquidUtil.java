// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import org.apache.commons.lang3.mutable.MutableObject;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraft.tileentity.TileEntity;
import ic2.api.util.FluidContainerOutputMode;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Iterator;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Set;
import java.util.Collections;
import java.util.Locale;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashSet;
import net.minecraftforge.fluids.FluidRegistry;
import java.util.List;
import net.minecraftforge.fluids.Fluid;
import java.util.Collection;

public class LiquidUtil
{
    private static final Collection<Fluid> registeredFluids;
    
    public static List<Fluid> getAllFluids() {
        final Set<Fluid> fluids = new HashSet<Fluid>(FluidRegistry.getRegisteredFluids().values());
        fluids.remove(null);
        final List<Fluid> ret = new ArrayList<Fluid>(fluids);
        Collections.sort(ret, new Comparator<Fluid>() {
            @Override
            public int compare(final Fluid a, final Fluid b) {
                final String nameA = a.getName();
                final String nameB = b.getName();
                if (nameA == null) {
                    if (nameB == null) {
                        return 0;
                    }
                    return 1;
                }
                else {
                    if (nameB == null) {
                        return -1;
                    }
                    return nameA.toLowerCase(Locale.ENGLISH).compareTo(nameB.toLowerCase(Locale.ENGLISH));
                }
            }
        });
        return ret;
    }
    
    public static LiquidData getLiquid(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        Fluid liquid = null;
        boolean isSource = false;
        if (block instanceof IFluidBlock) {
            final IFluidBlock fblock = (IFluidBlock)block;
            liquid = fblock.getFluid();
            isSource = fblock.canDrain(world, pos);
        }
        else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            liquid = FluidRegistry.WATER;
            isSource = ((int)state.getValue((IProperty)BlockLiquid.LEVEL) == 0);
        }
        else if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            liquid = FluidRegistry.LAVA;
            isSource = ((int)state.getValue((IProperty)BlockLiquid.LEVEL) == 0);
        }
        if (liquid != null) {
            return new LiquidData(liquid, isSource);
        }
        return null;
    }
    
    public static boolean isFluidContainer(final ItemStack stack) {
        return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
    }
    
    public static boolean isDrainableFluidContainer(final ItemStack stack) {
        if (!isFluidContainer(stack)) {
            return false;
        }
        final ItemStack singleStack = StackUtil.copyWithSize(stack, 1);
        final IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
        if (handler == null) {
            return false;
        }
        final FluidStack fs = handler.drain(Integer.MAX_VALUE, false);
        return fs != null && fs.amount > 0;
    }
    
    public static boolean isFillableFluidContainer(final ItemStack stack) {
        return isFillableFluidContainer(stack, null);
    }
    
    public static boolean isFillableFluidContainer(final ItemStack stack, Iterable<Fluid> testFluids) {
        if (!isFluidContainer(stack)) {
            return false;
        }
        if (testFluids == null) {
            testFluids = LiquidUtil.registeredFluids;
        }
        final ItemStack singleStack = StackUtil.copyWithSize(stack, 1);
        final IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
        if (handler == null) {
            return false;
        }
        FluidStack fs = handler.drain(Integer.MAX_VALUE, false);
        if (fs != null && testFillFluid(handler, fs.getFluid(), fs.tag)) {
            return true;
        }
        for (final IFluidTankProperties properties : handler.getTankProperties()) {
            fs = properties.getContents();
            if (fs != null && testFillFluid(handler, fs.getFluid(), fs.tag)) {
                return true;
            }
        }
        for (final Fluid fluid : LiquidUtil.registeredFluids) {
            if (testFillFluid(handler, fluid, null)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean testFillFluid(final IFluidHandlerItem handler, final Fluid fluid, final NBTTagCompound nbt) {
        final FluidStack fs = new FluidStack(fluid, Integer.MAX_VALUE);
        fs.tag = nbt;
        return handler.fill(fs, false) > 0;
    }
    
    public static FluidStack drainContainer(final EntityPlayer player, final EnumHand hand, final Fluid fluid, final int maxAmount, final FluidContainerOutputMode outputMode, final boolean simulate) {
        final ItemStack stack = StackUtil.get(player, hand);
        final FluidOperationResult result = drainContainer(stack, fluid, maxAmount, outputMode);
        if (result == null) {
            return null;
        }
        if (result.extraOutput != null && !StackUtil.storeInventoryItem(result.extraOutput, player, simulate)) {
            return null;
        }
        if (!simulate) {
            StackUtil.set(player, hand, result.inPlaceOutput);
        }
        return result.fluidChange;
    }
    
    public static int fillContainer(final EntityPlayer player, final EnumHand hand, final FluidStack fs, final FluidContainerOutputMode outputMode, final boolean simulate) {
        final ItemStack stack = StackUtil.get(player, hand);
        final FluidOperationResult result = fillContainer(stack, fs, outputMode);
        if (result == null) {
            return 0;
        }
        if (result.extraOutput != null && !StackUtil.storeInventoryItem(result.extraOutput, player, simulate)) {
            return 0;
        }
        if (!simulate) {
            StackUtil.set(player, hand, result.inPlaceOutput);
        }
        return result.fluidChange.amount;
    }
    
    public static FluidOperationResult drainContainer(final ItemStack stack, final Fluid fluid, final int maxAmount, final FluidContainerOutputMode outputMode) {
        if (StackUtil.isEmpty(stack) || maxAmount <= 0) {
            return null;
        }
        ItemStack inPlace = StackUtil.copy(stack);
        ItemStack extra = null;
        if (!inPlace.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null)) {
            return null;
        }
        final ItemStack singleStack = StackUtil.copyWithSize(inPlace, 1);
        final IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
        if (handler == null) {
            return null;
        }
        FluidStack fs;
        if (fluid == null) {
            fs = handler.drain(maxAmount, true);
        }
        else {
            fs = handler.drain(new FluidStack(fluid, maxAmount), true);
        }
        if (fs == null || fs.amount <= 0) {
            return null;
        }
        if (StackUtil.isEmpty(singleStack)) {
            inPlace = StackUtil.decSize(inPlace);
        }
        else {
            final FluidStack leftOver = handler.drain(Integer.MAX_VALUE, false);
            final boolean isEmpty = leftOver == null || leftOver.amount <= 0;
            if ((isEmpty && outputMode.isOutputEmptyFull()) || outputMode == FluidContainerOutputMode.AnyToOutput || (outputMode == FluidContainerOutputMode.InPlacePreferred && StackUtil.getSize(inPlace) > 1)) {
                extra = handler.getContainer();
                inPlace = StackUtil.decSize(inPlace);
            }
            else {
                if (StackUtil.getSize(inPlace) > 1) {
                    return null;
                }
                inPlace = handler.getContainer();
            }
        }
        assert fs.amount > 0;
        return new FluidOperationResult(fs, inPlace, extra);
    }
    
    public static FluidOperationResult fillContainer(final ItemStack stack, final FluidStack fsIn, final FluidContainerOutputMode outputMode) {
        if (StackUtil.isEmpty(stack) || fsIn == null || fsIn.amount <= 0) {
            return null;
        }
        ItemStack inPlace = StackUtil.copy(stack);
        ItemStack extra = null;
        if (!inPlace.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null)) {
            return null;
        }
        ItemStack singleStack = StackUtil.copyWithSize(inPlace, 1);
        final IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
        if (handler == null) {
            return null;
        }
        final FluidStack fsChange = fsIn.copy();
        final int amount = handler.fill(fsChange, true);
        if (amount <= 0) {
            return null;
        }
        fsChange.amount = amount;
        final FluidStack fillTestFs = fsIn.copy();
        fillTestFs.amount = Integer.MAX_VALUE;
        final boolean isFull = handler.fill(fillTestFs, false) <= 0;
        singleStack = handler.getContainer();
        assert fsChange.getFluid() == fsIn.getFluid();
        assert fsChange.amount > 0;
        assert StackUtil.getSize(singleStack) == 1;
        if ((isFull && outputMode.isOutputEmptyFull()) || outputMode == FluidContainerOutputMode.AnyToOutput || (outputMode == FluidContainerOutputMode.InPlacePreferred && StackUtil.getSize(inPlace) > 1)) {
            extra = singleStack;
            inPlace = StackUtil.decSize(inPlace);
        }
        else {
            if (StackUtil.getSize(inPlace) > 1) {
                return null;
            }
            inPlace = singleStack;
        }
        return new FluidOperationResult(fsChange, inPlace, extra);
    }
    
    public static boolean isFluidTile(final TileEntity te, final EnumFacing side) {
        return te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }
    
    public static FluidStack drainTile(final TileEntity te, final EnumFacing side, final int maxAmount, final boolean simulate) {
        return drainTile(te, side, null, maxAmount, simulate);
    }
    
    public static FluidStack drainTile(final TileEntity te, final EnumFacing side, final Fluid fluid, final int maxAmount, final boolean simulate) {
        if (!te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
            return null;
        }
        final IFluidHandler handler = (IFluidHandler)te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
        if (handler == null) {
            return null;
        }
        if (fluid == null) {
            return handler.drain(maxAmount, !simulate);
        }
        return handler.drain(new FluidStack(fluid, maxAmount), !simulate);
    }
    
    public static int fillTile(final TileEntity te, final EnumFacing side, final FluidStack fs, final boolean simulate) {
        if (!te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
            return 0;
        }
        final IFluidHandler handler = (IFluidHandler)te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
        if (handler == null) {
            return 0;
        }
        return handler.fill(fs, !simulate);
    }
    
    public static List<AdjacentFluidHandler> getAdjacentHandlers(final TileEntity source) {
        final List<AdjacentFluidHandler> ret = new ArrayList<AdjacentFluidHandler>();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity te = source.getWorld().getTileEntity(source.getPos().offset(dir));
            if (isFluidTile(te, dir.getOpposite())) {
                ret.add(new AdjacentFluidHandler(te, dir));
            }
        }
        return ret;
    }
    
    public static AdjacentFluidHandler getAdjacentHandler(final TileEntity source, final EnumFacing dir) {
        final TileEntity te = source.getWorld().getTileEntity(source.getPos().offset(dir));
        if (!isFluidTile(te, dir.getOpposite())) {
            return null;
        }
        return new AdjacentFluidHandler(te, dir);
    }
    
    public static int distribute(final TileEntity source, final FluidStack stack, final boolean simulate) {
        int transferred = 0;
        for (final AdjacentFluidHandler handler : getAdjacentHandlers(source)) {
            final int amount = fillTile(handler.handler, handler.dir.getOpposite(), stack, simulate);
            transferred += amount;
            stack.amount -= amount;
            if (stack.amount <= 0) {
                break;
            }
        }
        stack.amount += transferred;
        return transferred;
    }
    
    public static int distributeAll(final TileEntity source, int amount) {
        if (source == null) {
            throw new IllegalArgumentException("source has to be a tile entity");
        }
        final TileEntity srcTe = source;
        int transferred = 0;
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity te = srcTe.getWorld().getTileEntity(srcTe.getPos().offset(dir));
            if (isFluidTile(te, dir.getOpposite())) {
                final FluidStack stack = transfer(source, dir, te, amount);
                if (stack != null) {
                    amount -= stack.amount;
                    transferred += stack.amount;
                    if (amount <= 0) {
                        break;
                    }
                }
            }
        }
        return transferred;
    }
    
    public static FluidStack transfer(final TileEntity source, final EnumFacing dir, final TileEntity target, int amount) {
        FluidStack ret;
        do {
            ret = drainTile(source, dir, amount, true);
            if (ret == null || ret.amount <= 0) {
                return null;
            }
            if (ret.amount > amount) {
                throw new IllegalStateException("The fluid handler " + source + " drained more than the requested amount.");
            }
            final int cAmount = fillTile(target, getOppositeDir(dir), ret.copy(), true);
            if (cAmount > amount) {
                throw new IllegalStateException("The fluid handler " + target + " filled more than the requested amount.");
            }
            amount = cAmount;
        } while (amount != ret.amount && amount > 0);
        if (amount <= 0) {
            return null;
        }
        ret = drainTile(source, dir, amount, false);
        if (ret == null) {
            throw new IllegalStateException("The fluid handler " + source + " drained inconsistently. Expected " + amount + ", couldn't find previous IFluidHandler facing " + dir + '.');
        }
        if (ret.amount != amount) {
            throw new IllegalStateException("The fluid handler " + source + " drained inconsistently. Expected " + amount + ", got " + ret.amount + '.');
        }
        amount = fillTile(target, getOppositeDir(dir), ret.copy(), false);
        if (amount != ret.amount) {
            throw new IllegalStateException("The fluid handler " + target + " filled inconsistently. Expected " + ret.amount + ", got " + amount + '.');
        }
        return ret;
    }
    
    private static EnumFacing getOppositeDir(final EnumFacing dir) {
        if (dir == null) {
            return null;
        }
        return dir.getOpposite();
    }
    
    public static boolean check(final FluidStack fs) {
        return fs.getFluid() != null;
    }
    
    public static FluidStack drainBlock(final World world, final BlockPos pos, final boolean simulate) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            final IFluidBlock liquid = (IFluidBlock)block;
            if (liquid.canDrain(world, pos)) {
                return liquid.drain(world, pos, !simulate);
            }
        }
        else if (block instanceof BlockLiquid && (int)state.getValue((IProperty)BlockLiquid.LEVEL) == 0) {
            FluidStack fluid = null;
            if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                fluid = new FluidStack(FluidRegistry.WATER, 1000);
            }
            else if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
                fluid = new FluidStack(FluidRegistry.LAVA, 1000);
            }
            if (fluid != null && !simulate) {
                world.setBlockToAir(pos);
            }
            return fluid;
        }
        return null;
    }
    
    public static boolean drainBlockToContainer(final World world, final BlockPos pos, final EntityPlayer player, final EnumHand hand) {
        FluidStack fs = drainBlock(world, pos, true);
        if (fs == null || fs.amount <= 0) {
            return false;
        }
        final int amount = fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, true);
        if (amount != fs.amount) {
            return false;
        }
        fs = drainBlock(world, pos, false);
        fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, false);
        return true;
    }
    
    public static boolean fillBlock(final FluidStack fs, final World world, final BlockPos pos, final boolean simulate) {
        if (fs == null || fs.amount < 1000) {
            return false;
        }
        final Fluid fluid = fs.getFluid();
        if (fluid == null || !fluid.canBePlacedInWorld() || fluid.getBlock() == null) {
            return false;
        }
        final IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (!block.isAir(state, (IBlockAccess)world, pos) && state.getMaterial().isSolid()) {
            return false;
        }
        if (block == fluid.getBlock() && isFullFluidBlock(world, pos, block, state)) {
            return false;
        }
        if (simulate) {
            return true;
        }
        final Block fluidBlock;
        if (world.provider.doesWaterVaporize() && (fluidBlock = fluid.getBlock()) != null && fluidBlock.getDefaultState().getMaterial() == Material.WATER) {
            world.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8f);
            for (int i = 0; i < 8; ++i) {
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0, 0.0, 0.0, new int[0]);
            }
        }
        else {
            if (!world.isRemote && !state.getMaterial().isSolid() && !state.getMaterial().isLiquid()) {
                world.destroyBlock(pos, true);
            }
            if (fluid == FluidRegistry.WATER) {
                block = (Block)Blocks.FLOWING_WATER;
            }
            else if (fluid == FluidRegistry.LAVA) {
                block = (Block)Blocks.FLOWING_LAVA;
            }
            else {
                block = fluid.getBlock();
            }
            if (!world.setBlockState(pos, block.getDefaultState())) {
                return false;
            }
        }
        fs.amount -= 1000;
        return true;
    }
    
    private static boolean isFullFluidBlock(final World world, final BlockPos pos, final Block block, final IBlockState state) {
        if (block instanceof IFluidBlock) {
            final IFluidBlock fBlock = (IFluidBlock)block;
            final FluidStack drained = fBlock.drain(world, pos, false);
            return drained != null && drained.amount >= 1000;
        }
        return state.getProperties().containsKey((Object)BlockLiquid.LEVEL) && (int)state.getValue((IProperty)BlockLiquid.LEVEL) == 0;
    }
    
    public static boolean fillBlockFromContainer(final World world, final BlockPos pos, final EntityPlayer player, final EnumHand hand) {
        final FluidStack fs = drainContainer(player, hand, null, 1000, FluidContainerOutputMode.InPlacePreferred, true);
        if (fs == null || fs.amount < 1000) {
            return false;
        }
        if (!fillBlock(fs, world, pos, false)) {
            return false;
        }
        drainContainer(player, hand, null, 1000, FluidContainerOutputMode.InPlacePreferred, false);
        return true;
    }
    
    public static boolean storeOutputContainer(final MutableObject<ItemStack> output, final EntityPlayer player) {
        return output.getValue() == null || StackUtil.storeInventoryItem((ItemStack)output.getValue(), player, false);
    }
    
    public static String toStringSafe(final FluidStack fluidStack) {
        if (fluidStack.getFluid() == null) {
            return fluidStack.amount + "(mb)x(null)@(unknown)";
        }
        return fluidStack.toString();
    }
    
    static {
        registeredFluids = FluidRegistry.getRegisteredFluids().values();
    }
    
    public static class LiquidData
    {
        public final Fluid liquid;
        public final boolean isSource;
        
        LiquidData(final Fluid liquid1, final boolean isSource1) {
            this.liquid = liquid1;
            this.isSource = isSource1;
        }
    }
    
    public static class FluidOperationResult
    {
        public final FluidStack fluidChange;
        public final ItemStack inPlaceOutput;
        public final ItemStack extraOutput;
        
        FluidOperationResult(final FluidStack fluidChange, final ItemStack inPlaceOutput, final ItemStack extraOutput) {
            if (fluidChange == null) {
                throw new NullPointerException("null fluid change");
            }
            this.fluidChange = fluidChange;
            this.inPlaceOutput = inPlaceOutput;
            this.extraOutput = extraOutput;
        }
    }
    
    public static class AdjacentFluidHandler
    {
        public final TileEntity handler;
        public final EnumFacing dir;
        
        AdjacentFluidHandler(final TileEntity handler, final EnumFacing dir) {
            this.handler = handler;
            this.dir = dir;
        }
    }
}
