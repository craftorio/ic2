// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.nbt.NBTBase;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.list.array.TIntArrayList;
import java.util.LinkedList;
import gnu.trove.TIntCollection;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.TIntSet;
import net.minecraft.util.EnumActionResult;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemBlock;
import ic2.core.Ic2Player;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.block.state.IBlockState;
import java.util.HashSet;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Collection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.NonNullList;
import ic2.core.IC2;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Arrays;
import java.util.ListIterator;
import net.minecraft.inventory.ISidedInventory;
import com.google.common.base.Predicates;
import java.util.Iterator;
import net.minecraftforge.items.IItemHandler;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import com.mojang.authlib.GameProfile;
import ic2.core.block.personal.IPersonalBlock;
import net.minecraft.world.ILockableContainer;
import net.minecraft.block.BlockChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraft.tileentity.TileEntity;
import java.util.Set;
import net.minecraft.item.ItemStack;
import com.google.common.base.Predicate;

public final class StackUtil
{
    public static final Predicate<ItemStack> anyStack;
    static final Set<String> ignoredNbtKeys;
    public static final ItemStack emptyStack;
    private static final int[] emptySlotArray;
    
    public static boolean isInventoryTile(final TileEntity te, final EnumFacing side) {
        return te instanceof IInventory || (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side));
    }
    
    public static IInventory findDoubleChest(final TileEntityChest chest) {
        final World world = chest.getWorld();
        final BlockPos pos = chest.getPos();
        if (world == null || pos == null || !world.isBlockLoaded(pos)) {
            return null;
        }
        final BlockChest.Type type = chest.getChestType();
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te instanceof TileEntityChest && ((TileEntityChest)te).getChestType() == type) {
                ILockableContainer left;
                ILockableContainer right;
                if (facing == EnumFacing.WEST || facing == EnumFacing.NORTH) {
                    left = (ILockableContainer)te;
                    right = (ILockableContainer)chest;
                }
                else {
                    left = (ILockableContainer)chest;
                    right = (ILockableContainer)te;
                }
                return (IInventory)new InventoryLargeChest("container.chestDouble", left, right);
            }
        }
        return (IInventory)chest;
    }
    
    public static AdjacentInv getAdjacentInventory(final TileEntity source, final EnumFacing dir) {
        final TileEntity target = source.getWorld().getTileEntity(source.getPos().offset(dir));
        if (!isInventoryTile(target, dir)) {
            return null;
        }
        final GameProfile srcOwner;
        if (target instanceof IPersonalBlock && source instanceof IPersonalBlock && (srcOwner = ((IPersonalBlock)source).getOwner()) != null) {
            return new PersonalAdjacentInv(target, dir, srcOwner);
        }
        if (target instanceof TileEntityChest && findDoubleChest((TileEntityChest)target) == null) {
            return null;
        }
        return new AdjacentInv(target, dir);
    }
    
    public static List<AdjacentInv> getAdjacentInventories(final TileEntity source) {
        final List<AdjacentInv> inventories = new ArrayList<AdjacentInv>();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final AdjacentInv inventory = getAdjacentInventory(source, dir);
            if (inventory != null) {
                inventories.add(inventory);
            }
        }
        Collections.sort(inventories, new Comparator<AdjacentInv>() {
            @Override
            public int compare(final AdjacentInv a, final AdjacentInv b) {
                if (a.te instanceof IPersonalBlock || !(b.te instanceof IPersonalBlock)) {
                    return -1;
                }
                if (b.te instanceof IPersonalBlock || !(a.te instanceof IPersonalBlock)) {
                    return 1;
                }
                return StackUtil.getInventorySize(b.te, b.dir.getOpposite(), b.getAccessor()) - StackUtil.getInventorySize(a.te, a.dir.getOpposite(), a.getAccessor());
            }
        });
        return inventories;
    }
    
    public static GameProfile getOwner(final TileEntity te) {
        if (te instanceof IPersonalBlock) {
            return ((IPersonalBlock)te).getOwner();
        }
        return null;
    }
    
    public static int getInventorySize(final TileEntity te, final EnumFacing side, final GameProfile accessor) {
        if (te instanceof IInventory) {
            final IInventory inv = getInventory(te, accessor);
            return (inv == null) ? 0 : inv.getSizeInventory();
        }
        if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            return 0;
        }
        final IItemHandler handler = (IItemHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        if (handler == null) {
            return 0;
        }
        return handler.getSlots();
    }
    
    private static IInventory getInventory(final TileEntity te, final GameProfile accessor) {
        if (te instanceof TileEntityChest) {
            return findDoubleChest((TileEntityChest)te);
        }
        if (te instanceof IPersonalBlock) {
            return ((IPersonalBlock)te).getPrivilegedInventory(accessor);
        }
        if (te instanceof IInventory) {
            return (IInventory)te;
        }
        return null;
    }
    
    public static int distribute(final TileEntity source, final ItemStack stack, final boolean simulate) {
        ItemStack remaining = copy(stack);
        for (final AdjacentInv inventory : getAdjacentInventories(source)) {
            final int amount = putInInventory(source, inventory, remaining, simulate);
            remaining = decSize(remaining, amount);
            if (isEmpty(remaining)) {
                break;
            }
        }
        return getSize(stack) - getSize(remaining);
    }
    
    public static int fetch(final TileEntity source, final ItemStack stack, final boolean simulate) {
        ItemStack remaining = copy(stack);
        for (final AdjacentInv inventory : getAdjacentInventories(source)) {
            final ItemStack transferred = getFromInventory(source, inventory, remaining, true, simulate);
            if (isEmpty(transferred)) {
                continue;
            }
            remaining = decSize(remaining, getSize(transferred));
            if (isEmpty(remaining)) {
                break;
            }
        }
        return getSize(stack) - getSize(remaining);
    }
    
    public static int transfer(final TileEntity src, final TileEntity dst, final EnumFacing dir, final int amount) {
        return transfer(src, dst, dir, amount, (Predicate<ItemStack>)Predicates.alwaysTrue(), true);
    }
    
    public static int transfer(final TileEntity src, final TileEntity dst, final EnumFacing dir, final int amount, final Predicate<ItemStack> checker) {
        return transfer(src, dst, dir, amount, checker, checker == null || Predicates.alwaysTrue().equals((Object)checker));
    }
    
    private static int transfer(final TileEntity src, final TileEntity dst, final EnumFacing dir, final int amount, final Predicate<ItemStack> checker, final boolean skipChecker) {
        if (amount <= 0) {
            return 0;
        }
        final GameProfile srcOwner = getOwner(src);
        final GameProfile dstOwner = getOwner(dst);
        final EnumFacing reverseDir = dir.getOpposite();
        final int[] srcSlots = getInventorySlots(src, dir, false, true, dstOwner);
        if (srcSlots.length == 0) {
            return 0;
        }
        final int[] dstSlots = getInventorySlots(dst, reverseDir, true, false, srcOwner);
        if (dstSlots.length == 0) {
            return 0;
        }
        if (src instanceof IInventory) {
            final IInventory srcInv = getInventory(src, dstOwner);
            if (srcInv == null) {
                return 0;
            }
            if (dst instanceof IInventory) {
                final IInventory dstInv = getInventory(dst, srcOwner);
                if (dstInv == null) {
                    return 0;
                }
                return transfer(srcInv, srcSlots, dstInv, dstSlots, dir, reverseDir, amount, checker, skipChecker);
            }
            else {
                if (!dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite())) {
                    return 0;
                }
                final IItemHandler dstHandler = (IItemHandler)dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
                if (dstHandler == null) {
                    return 0;
                }
                return transfer(srcInv, srcSlots, dstHandler, dstSlots, reverseDir, amount, checker, skipChecker);
            }
        }
        else {
            if (!src.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir)) {
                return 0;
            }
            final IItemHandler srcHandler = (IItemHandler)src.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
            if (srcHandler == null) {
                return 0;
            }
            if (dst instanceof IInventory) {
                final IInventory dstInv = getInventory(dst, srcOwner);
                if (dstInv == null) {
                    return 0;
                }
                return transfer(srcHandler, srcSlots, dstInv, dstSlots, reverseDir, amount, checker, skipChecker);
            }
            else {
                if (!dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite())) {
                    return 0;
                }
                final IItemHandler dstHandler = (IItemHandler)dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
                if (dstHandler == null) {
                    return 0;
                }
                return transfer(srcHandler, srcSlots, dstHandler, dstSlots, amount, checker, skipChecker);
            }
        }
    }
    
    private static int transfer(final IInventory src, final int[] srcSlots, final IInventory dst, final int[] dstSlots, final EnumFacing dir, final EnumFacing reverseDir, int amount, final Predicate<ItemStack> checker, final boolean skipChecker) {
        final ISidedInventory dstSided = (dst instanceof ISidedInventory) ? dst : null;
        final int total = amount;
        for (final int srcSlot : srcSlots) {
            final ItemStack srcStack = src.getStackInSlot(srcSlot);
            if (!isEmpty(srcStack)) {
                if (skipChecker || checker.apply((Object)srcStack)) {
                    final int transferred = insert(srcStack, amount, dst, dstSided, reverseDir, dstSlots);
                    if (transferred > 0) {
                        amount -= transferred;
                        src.setInventorySlotContents(srcSlot, decSize(srcStack, transferred));
                        if (amount <= 0) {
                            break;
                        }
                    }
                }
            }
        }
        amount = total - amount;
        assert amount >= 0;
        if (amount > 0) {
            src.markDirty();
            dst.markDirty();
        }
        return amount;
    }
    
    private static int transfer(final IItemHandler src, final int[] srcSlots, final IInventory dst, final int[] dstSlots, final EnumFacing reverseDir, int amount, final Predicate<ItemStack> checker, final boolean skipChecker) {
        final ISidedInventory dstSided = (dst instanceof ISidedInventory) ? dst : null;
        final int total = amount;
        for (final int srcSlot : srcSlots) {
            final ItemStack srcStack = src.extractItem(srcSlot, amount, true);
            if (!isEmpty(srcStack)) {
                if (skipChecker || checker.apply((Object)srcStack)) {
                    final int transferred = insert(srcStack, amount, dst, dstSided, reverseDir, dstSlots);
                    if (transferred > 0) {
                        amount -= transferred;
                        src.extractItem(srcSlot, transferred, false);
                        if (amount <= 0) {
                            break;
                        }
                    }
                }
            }
        }
        amount = total - amount;
        assert amount >= 0;
        if (amount > 0) {
            dst.markDirty();
        }
        return amount;
    }
    
    private static int insert(final ItemStack stack, final int maxAmount, final IInventory dst, final ISidedInventory dstSided, final EnumFacing side, final int[] dstSlots) {
        final int sizeLimit = Math.min(stack.getMaxStackSize(), dst.getInventoryStackLimit());
        int remaining;
        final int total = remaining = Math.min(maxAmount, getSize(stack));
        for (int pass = 0; pass < 2; ++pass) {
            for (int i = 0; i < dstSlots.length; ++i) {
                final int dstSlot = dstSlots[i];
                if (dstSlot >= 0) {
                    final ItemStack dstStack = dst.getStackInSlot(dstSlot);
                    if (pass == 0) {
                        if (isEmpty(dstStack)) {
                            continue;
                        }
                        if (!checkItemEqualityStrict(stack, dstStack)) {
                            continue;
                        }
                    }
                    if (pass != 1 || isEmpty(dstStack)) {
                        if (dst.isItemValidForSlot(dstSlot, stack)) {
                            if (dstSided == null || dstSided.canInsertItem(dstSlot, stack, side)) {
                                final int amount = Math.min(remaining, sizeLimit - getSize(dstStack));
                                if (isEmpty(dstStack)) {
                                    dst.setInventorySlotContents(dstSlot, copyWithSize(stack, amount));
                                }
                                else {
                                    if (amount <= 0) {
                                        dstSlots[i] = -1;
                                        continue;
                                    }
                                    dst.setInventorySlotContents(dstSlot, incSize(dstStack, amount));
                                }
                                assert amount > 0;
                                remaining -= amount;
                                if (remaining <= 0) {
                                    return total;
                                }
                            }
                        }
                    }
                }
            }
        }
        return total - remaining;
    }
    
    private static int transfer(final IItemHandler src, final int[] srcSlots, final IItemHandler dst, final int[] dstSlots, int amount, final Predicate<ItemStack> checker, final boolean skipChecker) {
        final int total = amount;
        for (final int srcSlot : srcSlots) {
            final ItemStack srcStack = src.extractItem(srcSlot, amount, true);
            if (!isEmpty(srcStack)) {
                if (skipChecker || checker.apply((Object)srcStack)) {
                    final int transferred = insert(srcStack, Integer.MAX_VALUE, dst, dstSlots);
                    if (transferred > 0) {
                        amount -= transferred;
                        src.extractItem(srcSlot, transferred, false);
                        if (amount <= 0) {
                            break;
                        }
                    }
                }
            }
        }
        amount = total - amount;
        assert amount >= 0;
        return amount;
    }
    
    private static int transfer(final IInventory src, final int[] srcSlots, final IItemHandler dst, final int[] dstSlots, final EnumFacing dir, int amount, final Predicate<ItemStack> checker, final boolean skipChecker) {
        final int total = amount;
        for (final int srcSlot : srcSlots) {
            final ItemStack srcStack = src.getStackInSlot(srcSlot);
            if (!isEmpty(srcStack)) {
                if (skipChecker || checker.apply((Object)srcStack)) {
                    final int transferred = insert(srcStack, amount, dst, dstSlots);
                    if (transferred > 0) {
                        amount -= transferred;
                        src.setInventorySlotContents(srcSlot, decSize(srcStack, transferred));
                        if (amount <= 0) {
                            break;
                        }
                    }
                }
            }
        }
        amount = total - amount;
        assert amount >= 0;
        if (amount > 0) {
            src.markDirty();
        }
        return amount;
    }
    
    private static int insert(final ItemStack stack, final int maxAmount, final IItemHandler dst, final int[] dstSlots) {
        int remaining;
        final int total = remaining = Math.min(maxAmount, getSize(stack));
        assert !isEmpty(stack);
        for (int pass = 0; pass < 2; ++pass) {
            for (final int dstSlot : dstSlots) {
                Label_0171: {
                    if (dstSlot >= 0) {
                        final ItemStack dstStack = dst.getStackInSlot(dstSlot);
                        if (pass == 0) {
                            if (isEmpty(dstStack)) {
                                break Label_0171;
                            }
                            if (!checkItemEqualityStrict(stack, dstStack)) {
                                break Label_0171;
                            }
                        }
                        if (pass != 1 || isEmpty(dstStack)) {
                            final ItemStack leftOver = dst.insertItem(dstSlot, copyWithSize(stack, remaining), false);
                            final int transferred = remaining - getSize(leftOver);
                            remaining -= transferred;
                            if (remaining <= 0) {
                                return total;
                            }
                        }
                    }
                }
            }
        }
        return total - remaining;
    }
    
    public static void distributeDrops(final TileEntity source, final List<ItemStack> stacks) {
        final ListIterator<ItemStack> it = stacks.listIterator();
        while (it.hasNext()) {
            final ItemStack stack = it.next();
            final int amount = distribute(source, stack, false);
            if (amount == getSize(stack)) {
                it.remove();
            }
            else {
                it.set(decSize(stack, amount));
            }
        }
        final Iterator<ItemStack> iterator = stacks.iterator();
        while (iterator.hasNext()) {
            final ItemStack stack = iterator.next();
            dropAsEntity(source.getWorld(), source.getPos(), stack);
        }
        stacks.clear();
    }
    
    private static ItemStack getFromInventory(final TileEntity source, final AdjacentInv inventory, final ItemStack stack, final boolean ignoreMaxStackSize, final boolean simulate) {
        return getFromInventory(inventory.te, inventory.dir.getOpposite(), stack, getSize(stack), ignoreMaxStackSize, inventory.getAccessor(), simulate);
    }
    
    public static ItemStack getFromInventory(final TileEntity te, final EnumFacing side, final ItemStack stackDestination, final int max, final boolean ignoreMaxStackSize, final boolean simulate) {
        return getFromInventory(te, side, stackDestination, max, ignoreMaxStackSize, null, simulate);
    }
    
    public static ItemStack getFromInventory(final TileEntity te, final EnumFacing side, ItemStack stackDestination, int max, final boolean ignoreMaxStackSize, final GameProfile accessor, final boolean simulate) {
        if (!isEmpty(stackDestination) && !ignoreMaxStackSize) {
            max = Math.min(max, stackDestination.getMaxStackSize() - getSize(stackDestination));
        }
        final int[] slots = getInventorySlots(te, side, false, true, accessor);
        if (slots.length == 0) {
            return StackUtil.emptyStack;
        }
        ItemStack ret = StackUtil.emptyStack;
        if (te instanceof IInventory) {
            final IInventory inv = getInventory(te, accessor);
            if (inv == null) {
                return StackUtil.emptyStack;
            }
            for (final int slot : slots) {
                if (max <= 0) {
                    break;
                }
                ItemStack stack = inv.getStackInSlot(slot);
                if (!isEmpty(stack)) {
                    if (isEmpty(stackDestination) || checkItemEqualityStrict(stack, stackDestination)) {
                        final boolean extra = isEmpty(ret);
                        if (extra) {
                            ret = copyWithSize(stack, 1);
                            if (isEmpty(stackDestination)) {
                                if (!ignoreMaxStackSize) {
                                    max = Math.min(max, ret.getMaxStackSize());
                                }
                                stackDestination = ret;
                            }
                        }
                        final int transfer = Math.min(max, getSize(stack));
                        if (!simulate) {
                            stack = decSize(stack, transfer);
                            inv.setInventorySlotContents(slot, stack);
                        }
                        max -= transfer;
                        ret = incSize(ret, extra ? (transfer - 1) : transfer);
                    }
                }
            }
            if (!simulate && !isEmpty(ret)) {
                inv.markDirty();
            }
        }
        else if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            final IItemHandler handler = (IItemHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if (handler == null) {
                return StackUtil.emptyStack;
            }
            for (final int slot : slots) {
                if (max <= 0) {
                    break;
                }
                Label_0514: {
                    if (!isEmpty(stackDestination)) {
                        final ItemStack stack = handler.getStackInSlot(slot);
                        if (isEmpty(stack)) {
                            break Label_0514;
                        }
                        if (!checkItemEqualityStrict(stack, stackDestination)) {
                            break Label_0514;
                        }
                    }
                    final ItemStack stack = handler.extractItem(slot, max, simulate);
                    if (!isEmpty(stack)) {
                        final boolean extra = isEmpty(ret);
                        if (extra) {
                            ret = copyWithSize(stack, 1);
                            if (isEmpty(stackDestination)) {
                                if (!ignoreMaxStackSize) {
                                    max = Math.min(max, ret.getMaxStackSize());
                                }
                                stackDestination = ret;
                            }
                        }
                        else {
                            assert checkItemEqualityStrict(stack, ret);
                        }
                        final int transfer = getSize(stack);
                        max -= transfer;
                        ret = incSize(ret, extra ? (transfer - 1) : transfer);
                    }
                }
            }
        }
        return ret;
    }
    
    private static int putInInventory(final TileEntity source, final AdjacentInv inventory, final ItemStack stackSource, final boolean simulate) {
        return putInInventory(inventory.te, inventory.dir.getOpposite(), stackSource, inventory.getAccessor(), simulate);
    }
    
    public static int putInInventory(final TileEntity te, final EnumFacing side, final ItemStack stackSource, final boolean simulate) {
        return putInInventory(te, side, stackSource, null, simulate);
    }
    
    public static int putInInventory(final TileEntity te, final EnumFacing side, final ItemStack stackSource, final GameProfile accessor, final boolean simulate) {
        if (isEmpty(stackSource)) {
            return 0;
        }
        final int[] slots = getInventorySlots(te, side, true, false, accessor);
        if (slots.length == 0) {
            return 0;
        }
        if (te instanceof IInventory) {
            final IInventory inv = getInventory(te, accessor);
            if (inv == null) {
                return 0;
            }
            int toTransfer = getSize(stackSource);
            for (final int slot : slots) {
                if (toTransfer <= 0) {
                    break;
                }
                if (inv.isItemValidForSlot(slot, stackSource)) {
                    if (!(inv instanceof ISidedInventory) || ((ISidedInventory)inv).canInsertItem(slot, stackSource, side)) {
                        final ItemStack stack = inv.getStackInSlot(slot);
                        if (!isEmpty(stack) && checkItemEqualityStrict(stack, stackSource)) {
                            final int transfer = Math.min(toTransfer, Math.min(inv.getInventoryStackLimit(), stack.getMaxStackSize()) - getSize(stack));
                            if (!simulate) {
                                inv.setInventorySlotContents(slot, incSize(stack, transfer));
                            }
                            toTransfer -= transfer;
                        }
                    }
                }
            }
            for (final int slot : slots) {
                if (toTransfer <= 0) {
                    break;
                }
                if (inv.isItemValidForSlot(slot, stackSource)) {
                    if (!(inv instanceof ISidedInventory) || ((ISidedInventory)inv).canInsertItem(slot, stackSource, side)) {
                        final ItemStack stack = inv.getStackInSlot(slot);
                        if (isEmpty(stack)) {
                            final int transfer = Math.min(toTransfer, Math.min(inv.getInventoryStackLimit(), stackSource.getMaxStackSize()));
                            if (!simulate) {
                                final ItemStack dest = copyWithSize(stackSource, transfer);
                                inv.setInventorySlotContents(slot, dest);
                            }
                            toTransfer -= transfer;
                        }
                    }
                }
            }
            if (!simulate && toTransfer != getSize(stackSource)) {
                inv.markDirty();
            }
            return getSize(stackSource) - toTransfer;
        }
        else {
            if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
                return 0;
            }
            final IItemHandler handler = (IItemHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if (handler == null) {
                return 0;
            }
            ItemStack src = stackSource.copy();
            for (final int slot : slots) {
                if (isEmpty(src)) {
                    break;
                }
                final ItemStack stack = handler.getStackInSlot(slot);
                if (!isEmpty(stack)) {
                    final ItemStack remaining = handler.insertItem(slot, src.copy(), simulate);
                    if (isEmpty(remaining)) {
                        src = StackUtil.emptyStack;
                    }
                    else if (getSize(remaining) < getSize(src)) {
                        src = setSize(src, getSize(remaining));
                    }
                }
            }
            for (final int slot : slots) {
                if (isEmpty(src)) {
                    break;
                }
                final ItemStack stack = handler.getStackInSlot(slot);
                if (isEmpty(stack)) {
                    final ItemStack remaining = handler.insertItem(slot, src.copy(), simulate);
                    if (isEmpty(remaining)) {
                        src = StackUtil.emptyStack;
                    }
                    else if (getSize(remaining) < getSize(src)) {
                        src = setSize(src, getSize(remaining));
                    }
                }
            }
            return getSize(stackSource) - getSize(src);
        }
    }
    
    private static int[] getInventorySlots(final TileEntity te, final EnumFacing side, final boolean checkInsert, final boolean checkExtract, final GameProfile accessor) {
        if (te instanceof IInventory) {
            final IInventory inv = getInventory(te, accessor);
            if (inv == null || inv.getInventoryStackLimit() <= 0) {
                return StackUtil.emptySlotArray;
            }
            ISidedInventory sidedInv;
            int[] ret;
            if (inv instanceof ISidedInventory) {
                sidedInv = (ISidedInventory)inv;
                ret = sidedInv.getSlotsForFace(side);
                if (ret.length == 0) {
                    return StackUtil.emptySlotArray;
                }
                ret = Arrays.copyOf(ret, ret.length);
            }
            else {
                final int size = inv.getSizeInventory();
                if (size <= 0) {
                    return StackUtil.emptySlotArray;
                }
                sidedInv = null;
                ret = new int[size];
                for (int i = 0; i < ret.length; ++i) {
                    ret[i] = i;
                }
            }
            if (checkInsert || checkExtract) {
                int writeIdx = 0;
                for (int readIdx = 0; readIdx < ret.length; ++readIdx) {
                    final int slot = ret[readIdx];
                    final ItemStack stack = inv.getStackInSlot(slot);
                    if ((!checkExtract || (!isEmpty(stack) && (sidedInv == null || sidedInv.canExtractItem(slot, stack, side)))) && (!checkInsert || isEmpty(stack) || (getSize(stack) < stack.getMaxStackSize() && getSize(stack) < inv.getInventoryStackLimit() && (sidedInv == null || sidedInv.canInsertItem(slot, stack, side))))) {
                        ret[writeIdx] = slot;
                        ++writeIdx;
                    }
                }
                if (writeIdx != ret.length) {
                    ret = Arrays.copyOf(ret, writeIdx);
                }
            }
            return ret;
        }
        else {
            if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
                return StackUtil.emptySlotArray;
            }
            final IItemHandler handler = (IItemHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if (handler == null) {
                return StackUtil.emptySlotArray;
            }
            final int size2 = handler.getSlots();
            if (size2 <= 0) {
                return StackUtil.emptySlotArray;
            }
            int[] ret = new int[size2];
            for (int j = 0; j < ret.length; ++j) {
                ret[j] = j;
            }
            if (checkInsert || checkExtract) {
                int writeIdx = 0;
                for (int readIdx = 0; readIdx < ret.length; ++readIdx) {
                    final int slot = ret[readIdx];
                    final ItemStack stack = handler.getStackInSlot(slot);
                    if ((!checkExtract || (!isEmpty(stack) && !isEmpty(handler.extractItem(slot, Integer.MAX_VALUE, true)))) && (!checkInsert || checkInsert(handler, slot, stack))) {
                        ret[writeIdx] = slot;
                        ++writeIdx;
                    }
                }
                if (writeIdx != ret.length) {
                    ret = Arrays.copyOf(ret, writeIdx);
                }
            }
            return ret;
        }
    }
    
    private static boolean checkInsert(final IItemHandler handler, final int slot, final ItemStack stack) {
        if (isEmpty(stack) || getSize(stack) >= stack.getMaxStackSize()) {
            return true;
        }
        final int testSize = Integer.MAX_VALUE;
        final ItemStack result = handler.insertItem(slot, copyWithSize(stack, Integer.MAX_VALUE), true);
        return isEmpty(result) || getSize(result) < Integer.MAX_VALUE;
    }
    
    public static boolean consumeFromPlayerInventory(final EntityPlayer player, final Predicate<ItemStack> request, final int amount, final boolean simulate) {
        final NonNullList<ItemStack> contents = (NonNullList<ItemStack>)player.inventory.mainInventory;
        for (int pass = 0; pass < 2; ++pass) {
            int amountNeeded = amount;
            for (int i = 0; i < contents.size(); ++i) {
                final ItemStack stack = (ItemStack)contents.get(i);
                if (request.apply((Object)stack)) {
                    if (player.capabilities.isCreativeMode) {
                        return true;
                    }
                    final int cAmount = Math.min(getSize(stack), amountNeeded);
                    amountNeeded -= cAmount;
                    if (pass == 1) {
                        contents.set(i, (Object)decSize(stack, cAmount));
                    }
                    if (amountNeeded <= 0) {
                        break;
                    }
                }
            }
            if (amountNeeded > 0) {
                if (pass == 1) {
                    IC2.log.warn(LogCategory.General, "Inconsistent inventory transaction for player %s, request %s: %d missing", player, request, amountNeeded);
                }
                return false;
            }
            if (simulate) {
                return true;
            }
        }
        return true;
    }
    
    public static Predicate<ItemStack> sameStack(final ItemStack stack) {
        if (isEmpty(stack)) {
            throw new IllegalArgumentException("empty stack");
        }
        return (Predicate<ItemStack>)new Predicate<ItemStack>() {
            public boolean apply(final ItemStack input) {
                return StackUtil.checkItemEquality(input, stack);
            }
            
            @Override
            public String toString() {
                return "stack==" + stack;
            }
        };
    }
    
    public static Predicate<ItemStack> sameItem(final Item item) {
        if (item == null) {
            throw new NullPointerException("null item");
        }
        return (Predicate<ItemStack>)new Predicate<ItemStack>() {
            public boolean apply(final ItemStack input) {
                return input.getItem() == item;
            }
            
            @Override
            public String toString() {
                return "item==" + item;
            }
        };
    }
    
    public static Predicate<ItemStack> sameItem(final Block block) {
        if (block == null) {
            throw new NullPointerException("null block");
        }
        final Item item = Item.getItemFromBlock(block);
        if (item == null || (item == Items.AIR && block != Blocks.AIR)) {
            throw new IllegalArgumentException("block " + block + " doesn't have an associated item");
        }
        return sameItem(item);
    }
    
    public static Predicate<ItemStack> oreDict(final String name) {
        return recipeInput(Recipes.inputFactory.forOreDict(name));
    }
    
    public static Predicate<ItemStack> recipeInput(final IRecipeInput item) {
        return (Predicate<ItemStack>)new Predicate<ItemStack>() {
            public boolean apply(final ItemStack input) {
                return item.matches(input);
            }
            
            @Override
            public String toString() {
                return item.toString();
            }
        };
    }
    
    public static boolean consume(final EntityPlayer player, final EnumHand hand, final Predicate<ItemStack> request, final int amount) {
        return consume0(player, hand, request, amount, false) != StackUtil.emptyStack;
    }
    
    public static ItemStack consumeAndGet(final EntityPlayer player, final Predicate<ItemStack> request, final int amount) {
        return consumeAndGet(player, EnumHand.MAIN_HAND, request, amount);
    }
    
    public static ItemStack consumeAndGet(final EntityPlayer player, final EnumHand hand, final Predicate<ItemStack> request, final int amount) {
        return consume0(player, hand, request, amount, true);
    }
    
    public static void consumeOrError(final EntityPlayer player, final EnumHand hand, final int amount) {
        consumeOrError(player, hand, StackUtil.anyStack, amount);
    }
    
    public static void consumeOrError(final EntityPlayer player, final EnumHand hand, final Predicate<ItemStack> request, final int amount) {
        if (!consume(player, hand, request, amount)) {
            throw new IllegalStateException("consume failed");
        }
    }
    
    private static ItemStack consume0(final EntityPlayer player, final EnumHand hand, final Predicate<ItemStack> request, final int amount, final boolean copyOutput) {
        if (amount <= 0) {
            throw new IllegalArgumentException("negative/zero amount");
        }
        final ItemStack stack = get(player, hand);
        if (isEmpty(stack)) {
            return StackUtil.emptyStack;
        }
        if (!request.apply((Object)stack)) {
            return StackUtil.emptyStack;
        }
        if (player.capabilities.isCreativeMode) {
            return copyOutput ? copyWithSize(stack, amount) : stack;
        }
        if (getSize(stack) < amount) {
            return StackUtil.emptyStack;
        }
        ItemStack ret;
        if (getSize(stack) == amount) {
            ret = stack;
            clear(player, hand);
        }
        else {
            ret = (copyOutput ? copyWithSize(stack, amount) : stack);
            set(player, hand, decSize(stack, amount));
        }
        return ret;
    }
    
    public static boolean damage(final EntityPlayer player, final EnumHand hand, final Predicate<ItemStack> request, final int amount) {
        return damage0(player, hand, request, amount, false) != StackUtil.emptyStack;
    }
    
    public static void damageOrError(final EntityPlayer player, final EnumHand hand, final int amount) {
        damageOrError(player, hand, StackUtil.anyStack, amount);
    }
    
    public static void damageOrError(final EntityPlayer player, final EnumHand hand, final Predicate<ItemStack> request, final int amount) {
        if (!damage(player, hand, request, amount)) {
            throw new IllegalStateException("damage failed");
        }
    }
    
    private static ItemStack damage0(final EntityPlayer player, final EnumHand hand, final Predicate<ItemStack> request, final int amount, final boolean copyOutput) {
        if (amount <= 0) {
            throw new IllegalArgumentException("negative/zero amount");
        }
        final ItemStack stack = get(player, hand);
        if (isEmpty(stack)) {
            return StackUtil.emptyStack;
        }
        final int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) {
            return StackUtil.emptyStack;
        }
        if (!request.apply((Object)stack)) {
            return StackUtil.emptyStack;
        }
        if (player.capabilities.isCreativeMode || !stack.isItemStackDamageable()) {
            return copyOutput ? copy(stack) : stack;
        }
        stack.damageItem(amount, (EntityLivingBase)player);
        ItemStack ret;
        if (isEmpty(stack)) {
            ret = stack;
            clear(player, hand);
        }
        else {
            ret = (copyOutput ? copy(stack) : stack);
            set(player, hand, stack);
        }
        return ret;
    }
    
    public static ItemStack get(final EntityPlayer player, final EnumHand hand) {
        return player.getHeldItem(hand);
    }
    
    public static void set(final EntityPlayer player, final EnumHand hand, ItemStack stack) {
        if (isEmpty(stack)) {
            stack = StackUtil.emptyStack;
        }
        final InventoryPlayer inv = player.inventory;
        if (hand == EnumHand.MAIN_HAND) {
            inv.mainInventory.set(inv.currentItem, (Object)stack);
        }
        else {
            if (hand != EnumHand.OFF_HAND) {
                throw new IllegalArgumentException("invalid hand: " + hand);
            }
            inv.offHandInventory.set(0, (Object)stack);
        }
    }
    
    public static void clear(final EntityPlayer player, final EnumHand hand) {
        set(player, hand, StackUtil.emptyStack);
    }
    
    public static void clearEmpty(final EntityPlayer player, final EnumHand hand) {
        if (isEmpty(player, hand)) {
            clear(player, hand);
        }
    }
    
    public static void dropAsEntity(final World world, final BlockPos pos, final ItemStack stack) {
        if (isEmpty(stack)) {
            return;
        }
        final double f = 0.7;
        final double dx = world.rand.nextFloat() * f + (1.0 - f) * 0.5;
        final double dy = world.rand.nextFloat() * f + (1.0 - f) * 0.5;
        final double dz = world.rand.nextFloat() * f + (1.0 - f) * 0.5;
        final EntityItem entityItem = new EntityItem(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack.copy());
        entityItem.setDefaultPickupDelay();
        world.spawnEntity((Entity)entityItem);
    }
    
    public static ItemStack copy(final ItemStack stack) {
        return stack.copy();
    }
    
    public static ItemStack copyWithSize(final ItemStack stack, final int newSize) {
        if (isEmpty(stack)) {
            throw new IllegalArgumentException("empty stack: " + toStringSafe(stack));
        }
        return setSize(copy(stack), newSize);
    }
    
    public static ItemStack copyShrunk(final ItemStack stack, final int amount) {
        if (isEmpty(stack)) {
            throw new IllegalArgumentException("empty stack: " + toStringSafe(stack));
        }
        return setSize(copy(stack), getSize(stack) - amount);
    }
    
    public static ItemStack copyWithWildCard(final ItemStack stack) {
        final ItemStack ret = copy(stack);
        setRawMeta(ret, 32767);
        return ret;
    }
    
    public static Collection<ItemStack> copy(final Collection<ItemStack> c) {
        final List<ItemStack> ret = new ArrayList<ItemStack>(c.size());
        for (final ItemStack stack : c) {
            ret.add(copy(stack));
        }
        return ret;
    }
    
    public static NBTTagCompound getOrCreateNbtData(final ItemStack stack) {
        NBTTagCompound ret = stack.getTagCompound();
        if (ret == null) {
            ret = new NBTTagCompound();
            stack.setTagCompound(ret);
        }
        return ret;
    }
    
    public static boolean checkItemEquality(final ItemStack a, final ItemStack b) {
        return (isEmpty(a) && isEmpty(b)) || (!isEmpty(a) && !isEmpty(b) && a.getItem() == b.getItem() && (!a.getHasSubtypes() || a.getMetadata() == b.getMetadata()) && checkNbtEquality(a, b));
    }
    
    public static boolean checkItemEquality(final ItemStack a, final Item b) {
        return (isEmpty(a) && b == null) || (!isEmpty(a) && b != null && a.getItem() == b);
    }
    
    public static boolean checkItemEqualityStrict(final ItemStack a, final ItemStack b) {
        return (isEmpty(a) && isEmpty(b)) || (!isEmpty(a) && !isEmpty(b) && a.isItemEqual(b) && checkNbtEqualityStrict(a, b));
    }
    
    private static boolean checkNbtEquality(final ItemStack a, final ItemStack b) {
        return checkNbtEquality(a.getTagCompound(), b.getTagCompound());
    }
    
    public static boolean checkNbtEquality(final NBTTagCompound a, final NBTTagCompound b) {
        if (a == b) {
            return true;
        }
        final Set<String> keysA = (a != null) ? a.getKeySet() : Collections.emptySet();
        final Set<String> keysB = (b != null) ? b.getKeySet() : Collections.emptySet();
        final Set<String> toCheck = new HashSet<String>(Math.max(keysA.size(), keysB.size()));
        for (final String key : keysA) {
            if (StackUtil.ignoredNbtKeys.contains(key)) {
                continue;
            }
            if (!keysB.contains(key)) {
                return false;
            }
            toCheck.add(key);
        }
        for (final String key : keysB) {
            if (StackUtil.ignoredNbtKeys.contains(key)) {
                continue;
            }
            if (!keysA.contains(key)) {
                return false;
            }
            toCheck.add(key);
        }
        for (final String key : toCheck) {
            if (!a.getTag(key).equals((Object)b.getTag(key))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean checkNbtEqualityStrict(final ItemStack a, final ItemStack b) {
        final NBTTagCompound nbtA = a.getTagCompound();
        final NBTTagCompound nbtB = b.getTagCompound();
        return nbtA == nbtB || (nbtA != null && nbtB != null && nbtA.equals((Object)nbtB));
    }
    
    public static ItemStack getPickStack(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player) {
        final RayTraceResult target = new RayTraceResult(RayTraceResult.Type.BLOCK, new Vec3d((Vec3i)pos), EnumFacing.DOWN, pos);
        final ItemStack ret = state.getBlock().getPickBlock(state, target, world, pos, player);
        if (isEmpty(ret)) {
            return StackUtil.emptyStack;
        }
        return ret;
    }
    
    public static List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
        return getDrops(world, pos, state, state.getBlock(), fortune);
    }
    
    public static List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final Block block, final int fortune) {
        final NonNullList<ItemStack> drops = (NonNullList<ItemStack>)NonNullList.create();
        assert world.getBlockState(pos).getBlock() == block;
        block.getDrops((NonNullList)drops, world, pos, state, fortune);
        return (List<ItemStack>)drops;
    }
    
    public static List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, EntityPlayer player, final int fortune, final boolean silkTouch) {
        final Block block = state.getBlock();
        if (block.isAir(state, world, pos)) {
            return Collections.emptyList();
        }
        World rawWorld = null;
        if (silkTouch) {
            rawWorld = Util.getWorld(world);
            if (rawWorld == null) {
                throw new IllegalArgumentException("invalid world for silk touch: " + world);
            }
            if (player == null) {
                player = Ic2Player.get(rawWorld);
            }
        }
        final ItemStack drop;
        if (silkTouch && block.canSilkHarvest(rawWorld, pos, state, player) && !isEmpty(drop = getPickStack(rawWorld, pos, state, player))) {
            return Collections.singletonList(drop);
        }
        return getDrops(world, pos, state, block, fortune);
    }
    
    public static boolean placeBlock(ItemStack stack, final World world, final BlockPos pos) {
        if (isEmpty(stack)) {
            return false;
        }
        final Item item = stack.getItem();
        if (item instanceof ItemBlock || item instanceof ItemBlockSpecial) {
            final int oldSize = getSize(stack);
            final EntityPlayer player = Ic2Player.get(world);
            final EnumHand hand = EnumHand.MAIN_HAND;
            final ItemStack prev = player.getHeldItem(hand);
            player.setHeldItem(hand, stack);
            final EnumActionResult result = item.onItemUse(player, world, pos, hand, EnumFacing.DOWN, 0.0f, 0.0f, 0.0f);
            player.setHeldItem(hand, prev);
            stack = setSize(stack, oldSize);
            return result == EnumActionResult.SUCCESS;
        }
        return false;
    }
    
    public static boolean isEmpty(final ItemStack stack) {
        return stack == StackUtil.emptyStack || stack == null || stack.getItem() == null || stack.getCount() <= 0;
    }
    
    public static boolean isEmpty(final EntityPlayer player, final EnumHand hand) {
        return isEmpty(player.getHeldItem(hand));
    }
    
    public static int getSize(final ItemStack stack) {
        if (isEmpty(stack)) {
            return 0;
        }
        return stack.getCount();
    }
    
    public static ItemStack setSize(final ItemStack stack, final int size) {
        if (size <= 0) {
            return StackUtil.emptyStack;
        }
        stack.setCount(size);
        return stack;
    }
    
    public static ItemStack incSize(final ItemStack stack) {
        return incSize(stack, 1);
    }
    
    public static ItemStack incSize(final ItemStack stack, final int amount) {
        return setSize(stack, getSize(stack) + amount);
    }
    
    public static ItemStack decSize(final ItemStack stack) {
        return decSize(stack, 1);
    }
    
    public static ItemStack decSize(final ItemStack stack, final int amount) {
        return incSize(stack, -amount);
    }
    
    public static boolean check2(final Iterable<List<ItemStack>> list) {
        for (final List<ItemStack> list2 : list) {
            if (!check(list2)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean check(final ItemStack[] array) {
        return check(Arrays.asList(array));
    }
    
    public static boolean check(final Iterable<ItemStack> list) {
        for (final ItemStack stack : list) {
            if (!check(stack)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean check(final ItemStack stack) {
        return stack.getItem() != null;
    }
    
    public static String toStringSafe2(final Iterable<List<ItemStack>> list) {
        final StringBuilder ret = new StringBuilder("[");
        for (final List<ItemStack> list2 : list) {
            if (ret.length() > 1) {
                ret.append(", ");
            }
            ret.append(toStringSafe(list2));
        }
        return ret.append(']').toString();
    }
    
    public static String toStringSafe(final ItemStack[] array) {
        return toStringSafe(Arrays.asList(array));
    }
    
    public static String toStringSafe(final Iterable<ItemStack> list) {
        final StringBuilder ret = new StringBuilder("[");
        for (final ItemStack stack : list) {
            if (ret.length() > 1) {
                ret.append(", ");
            }
            ret.append(toStringSafe(stack));
        }
        return ret.append(']').toString();
    }
    
    public static String toStringSafe(final ItemStack stack) {
        if (stack == null) {
            return "(null)";
        }
        if (stack.getItem() == null) {
            return getSize(stack) + "x(null)@(unknown)";
        }
        return stack.toString();
    }
    
    public static boolean storeInventoryItem(final ItemStack stack, final EntityPlayer player, final boolean simulate) {
        if (simulate) {
            int sizeLeft = getSize(stack);
            final int maxStackSize = Math.min(player.inventory.getInventoryStackLimit(), stack.getMaxStackSize());
            for (int i = 0; i < player.inventory.mainInventory.size() && sizeLeft > 0; ++i) {
                final ItemStack invStack = (ItemStack)player.inventory.mainInventory.get(i);
                if (isEmpty(invStack)) {
                    sizeLeft -= maxStackSize;
                }
                else if (checkItemEqualityStrict(stack, invStack) && getSize(invStack) < maxStackSize) {
                    sizeLeft -= maxStackSize - getSize(invStack);
                }
            }
            return sizeLeft <= 0;
        }
        return player.inventory.addItemStackToInventory(stack);
    }
    
    public static int getRawMeta(final ItemStack stack) {
        return Items.DYE.getDamage(stack);
    }
    
    public static void setRawMeta(final ItemStack stack, final int meta) {
        if (meta < 0) {
            throw new IllegalArgumentException("negative meta");
        }
        Items.DYE.setDamage(stack, meta);
    }
    
    public static TIntSet getSlotsFromInv(final IInventory inv) {
        final TIntSet set = (TIntSet)new TIntHashSet();
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            set.add(i);
        }
        return set;
    }
    
    public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(final IInventory craftMatrix) {
        return balanceStacks(craftMatrix, (Collection<ItemStack>)Collections.emptySet());
    }
    
    public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(final IInventory craftMatrix, final ItemStack sourceItemStack) {
        return balanceStacks(craftMatrix, Collections.singleton(sourceItemStack));
    }
    
    public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(final IInventory inv, final Collection<ItemStack> additionalItems) {
        return balanceStacks(inv, (Predicate<Tuple.T2<ItemStack, Integer>>)new Predicate<Tuple.T2<ItemStack, Integer>>() {
            public boolean apply(final Tuple.T2<ItemStack, Integer> input) {
                return !StackUtil.isEmpty(inv.getStackInSlot((int)input.b));
            }
        }, getSlotsFromInv(inv), additionalItems);
    }
    
    public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(final IInventory inv, final Predicate<Tuple.T2<ItemStack, Integer>> canInsert) {
        return balanceStacks(inv, canInsert, getSlotsFromInv(inv), (Collection<ItemStack>)Collections.emptySet());
    }
    
    public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(final IInventory inv, final Predicate<Tuple.T2<ItemStack, Integer>> canInsert, final TIntSet originalAvailableSlots, final Collection<ItemStack> additionalStacksOriginal) {
        final List<ItemStack> additionalStacks = new LinkedList<ItemStack>(additionalStacksOriginal);
        final TIntSet availableSlots = (TIntSet)new TIntHashSet((TIntCollection)originalAvailableSlots);
        final List<ItemStack> leftOvers = new ArrayList<ItemStack>();
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            if (availableSlots.contains(i)) {
                final ItemStack stack = inv.getStackInSlot(i);
                if (!isEmpty(stack)) {
                    int amount = 0;
                    final ListIterator<ItemStack> iter = additionalStacks.listIterator();
                    while (iter.hasNext()) {
                        final ItemStack currentStack = iter.next();
                        if (checkItemEqualityStrict(currentStack, stack)) {
                            iter.remove();
                            amount += getSize(currentStack);
                        }
                    }
                    amount = distributeStackToSlots(inv, stack, availableSlots, canInsert, amount);
                    while (amount > 0) {
                        final int size = Math.min(stack.getMaxStackSize(), amount);
                        amount -= size;
                        leftOvers.add(copyWithSize(stack, size));
                    }
                }
            }
        }
        final Iterator<ItemStack> iterator = additionalStacks.iterator();
        while (iterator.hasNext()) {
            final ItemStack stack = iterator.next();
            final int amount = distributeStackToSlots(inv, stack, availableSlots, canInsert, getSize(stack));
            if (amount > 0) {
                leftOvers.add(copyWithSize(stack, amount));
            }
        }
        originalAvailableSlots.removeAll((TIntCollection)availableSlots);
        return new Tuple.T2<List<ItemStack>, TIntCollection>(leftOvers, (TIntCollection)originalAvailableSlots);
    }
    
    private static int distributeStackToSlots(final IInventory inv, final ItemStack stack, final TIntSet availableSlots, final Predicate<Tuple.T2<ItemStack, Integer>> canInsert, int amount) {
        final TIntList currentWorkingSet = (TIntList)new TIntArrayList();
        final TIntIterator iter = availableSlots.iterator();
        while (iter.hasNext()) {
            final int currentSlot = iter.next();
            final ItemStack currentStack = inv.getStackInSlot(currentSlot);
            if ((checkItemEqualityStrict(stack, currentStack) || isEmpty(currentStack)) && canInsert.apply((Object)new Tuple.T2(stack, currentSlot))) {
                amount += getSize(currentStack);
                currentWorkingSet.add(currentSlot);
                iter.remove();
            }
        }
        currentWorkingSet.sort();
        final int maxStackSize = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
        int slotsLeft = currentWorkingSet.size();
        final TIntIterator iter2 = currentWorkingSet.iterator();
        while (iter2.hasNext() && amount > 0) {
            final int currentSlot2 = iter2.next();
            int itemsToPut = amount / slotsLeft;
            if (amount % slotsLeft > 0) {
                ++itemsToPut;
            }
            itemsToPut = Math.min(itemsToPut, maxStackSize);
            inv.setInventorySlotContents(currentSlot2, copyWithSize(stack, itemsToPut));
            amount -= itemsToPut;
            --slotsLeft;
            iter2.remove();
        }
        if (!currentWorkingSet.isEmpty()) {
            assert amount <= 0;
            currentWorkingSet.forEach((TIntProcedure)new TIntProcedure() {
                public boolean execute(final int currentSlot) {
                    inv.setInventorySlotContents(currentSlot, StackUtil.emptyStack);
                    return true;
                }
            });
        }
        assert slotsLeft == 0;
        return amount;
    }
    
    public static ItemStack setImmutableSize(ItemStack stack, final int size) {
        if (getSize(stack) != size) {
            stack = copyWithSize(stack, size);
        }
        return stack;
    }
    
    public static boolean matchesNBT(final NBTTagCompound subject, final NBTTagCompound target) {
        if (subject == null) {
            return target == null || target.hasNoTags();
        }
        if (target == null) {
            return true;
        }
        for (final String key : target.getKeySet()) {
            final NBTBase targetNBT = target.getTag(key);
            if (!subject.hasKey(key) || targetNBT.getId() != subject.getTagId(key)) {
                return false;
            }
            final NBTBase subjectNBT = subject.getTag(key);
            if (!targetNBT.equals((Object)subjectNBT)) {
                return false;
            }
        }
        return true;
    }
    
    public static ItemStack wrapEmpty(final ItemStack stack) {
        return (stack == null) ? StackUtil.emptyStack : stack;
    }
    
    static {
        anyStack = Predicates.alwaysTrue();
        ignoredNbtKeys = new HashSet<String>(Arrays.asList("damage", "charge", "energy", "advDmg"));
        emptyStack = ItemStack.EMPTY;
        emptySlotArray = new int[0];
    }
    
    public static class AdjacentInv
    {
        public final TileEntity te;
        public final EnumFacing dir;
        
        AdjacentInv(final TileEntity te, final EnumFacing dir) {
            this.te = te;
            this.dir = dir;
        }
        
        public GameProfile getAccessor() {
            return null;
        }
    }
    
    public static class PersonalAdjacentInv extends AdjacentInv
    {
        public final GameProfile accessor;
        
        PersonalAdjacentInv(final TileEntity te, final EnumFacing dir, final GameProfile accessor) {
            super(te, dir);
            this.accessor = accessor;
        }
        
        @Override
        public GameProfile getAccessor() {
            return this.accessor;
        }
    }
}
