package ic2.core.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.mojang.authlib.GameProfile;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.block.personal.IPersonalBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class StackUtil {
  public static boolean isInventoryTile(TileEntity te, EnumFacing side) {
    return (te instanceof IInventory || (te != null && te
      .hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)));
  }
  
  public static class AdjacentInv {
    public final TileEntity te;
    
    public final EnumFacing dir;
    
    AdjacentInv(TileEntity te, EnumFacing dir) {
      this.te = te;
      this.dir = dir;
    }
    
    public GameProfile getAccessor() {
      return null;
    }
  }
  
  public static class PersonalAdjacentInv extends AdjacentInv {
    public final GameProfile accessor;
    
    PersonalAdjacentInv(TileEntity te, EnumFacing dir, GameProfile accessor) {
      super(te, dir);
      this.accessor = accessor;
    }
    
    public GameProfile getAccessor() {
      return this.accessor;
    }
  }
  
  public static IInventory findDoubleChest(TileEntityChest chest) {
    World world = chest.func_145831_w();
    BlockPos pos = chest.func_174877_v();
    if (world == null || pos == null || !world.func_175667_e(pos))
      return null; 
    BlockChest.Type type = chest.func_145980_j();
    for (EnumFacing facing : EnumFacing.field_176754_o) {
      TileEntity te = world.func_175625_s(pos.func_177972_a(facing));
      if (te instanceof TileEntityChest && ((TileEntityChest)te).func_145980_j() == type) {
        TileEntityChest tileEntityChest1;
        TileEntityChest tileEntityChest2;
        if (facing == EnumFacing.WEST || facing == EnumFacing.NORTH) {
          tileEntityChest1 = (TileEntityChest)te;
          tileEntityChest2 = chest;
        } else {
          tileEntityChest1 = chest;
          tileEntityChest2 = (TileEntityChest)te;
        } 
        return (IInventory)new InventoryLargeChest("container.chestDouble", (ILockableContainer)tileEntityChest1, (ILockableContainer)tileEntityChest2);
      } 
    } 
    return (IInventory)chest;
  }
  
  public static AdjacentInv getAdjacentInventory(TileEntity source, EnumFacing dir) {
    TileEntity target = source.func_145831_w().func_175625_s(source.func_174877_v().func_177972_a(dir));
    if (!isInventoryTile(target, dir))
      return null; 
    GameProfile srcOwner;
    if (target instanceof IPersonalBlock && source instanceof IPersonalBlock && (
      
      srcOwner = ((IPersonalBlock)source).getOwner()) != null)
      return new PersonalAdjacentInv(target, dir, srcOwner); 
    if (target instanceof TileEntityChest && findDoubleChest((TileEntityChest)target) == null)
      return null; 
    return new AdjacentInv(target, dir);
  }
  
  public static List<AdjacentInv> getAdjacentInventories(TileEntity source) {
    List<AdjacentInv> inventories = new ArrayList<>();
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      AdjacentInv inventory = getAdjacentInventory(source, dir);
      if (inventory != null)
        inventories.add(inventory); 
    } 
    Collections.sort(inventories, new Comparator<AdjacentInv>() {
          public int compare(StackUtil.AdjacentInv a, StackUtil.AdjacentInv b) {
            if (a.te instanceof IPersonalBlock || !(b.te instanceof IPersonalBlock))
              return -1; 
            if (b.te instanceof IPersonalBlock || !(a.te instanceof IPersonalBlock))
              return 1; 
            return StackUtil.getInventorySize(b.te, b.dir.func_176734_d(), b.getAccessor()) - StackUtil.getInventorySize(a.te, a.dir.func_176734_d(), a.getAccessor());
          }
        });
    return inventories;
  }
  
  public static GameProfile getOwner(TileEntity te) {
    if (te instanceof IPersonalBlock)
      return ((IPersonalBlock)te).getOwner(); 
    return null;
  }
  
  public static int getInventorySize(TileEntity te, EnumFacing side, GameProfile accessor) {
    if (te instanceof IInventory) {
      IInventory inv = getInventory(te, accessor);
      return (inv == null) ? 0 : inv.func_70302_i_();
    } 
    if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
      IItemHandler handler = (IItemHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
      if (handler == null)
        return 0; 
      return handler.getSlots();
    } 
    return 0;
  }
  
  private static IInventory getInventory(TileEntity te, GameProfile accessor) {
    if (te instanceof TileEntityChest)
      return findDoubleChest((TileEntityChest)te); 
    if (te instanceof IPersonalBlock)
      return ((IPersonalBlock)te).getPrivilegedInventory(accessor); 
    if (te instanceof IInventory)
      return (IInventory)te; 
    return null;
  }
  
  public static int distribute(TileEntity source, ItemStack stack, boolean simulate) {
    ItemStack remaining = copy(stack);
    for (AdjacentInv inventory : getAdjacentInventories(source)) {
      int amount = putInInventory(source, inventory, remaining, simulate);
      remaining = decSize(remaining, amount);
      if (isEmpty(remaining))
        break; 
    } 
    return getSize(stack) - getSize(remaining);
  }
  
  public static int fetch(TileEntity source, ItemStack stack, boolean simulate) {
    ItemStack remaining = copy(stack);
    for (AdjacentInv inventory : getAdjacentInventories(source)) {
      ItemStack transferred = getFromInventory(source, inventory, remaining, true, simulate);
      if (isEmpty(transferred))
        continue; 
      remaining = decSize(remaining, getSize(transferred));
      if (isEmpty(remaining))
        break; 
    } 
    return getSize(stack) - getSize(remaining);
  }
  
  public static int transfer(TileEntity src, TileEntity dst, EnumFacing dir, int amount) {
    return transfer(src, dst, dir, amount, Predicates.alwaysTrue(), true);
  }
  
  public static int transfer(TileEntity src, TileEntity dst, EnumFacing dir, int amount, Predicate<ItemStack> checker) {
    return transfer(src, dst, dir, amount, checker, (checker == null || Predicates.alwaysTrue().equals(checker)));
  }
  
  private static int transfer(TileEntity src, TileEntity dst, EnumFacing dir, int amount, Predicate<ItemStack> checker, boolean skipChecker) {
    if (amount <= 0)
      return 0; 
    GameProfile srcOwner = getOwner(src);
    GameProfile dstOwner = getOwner(dst);
    EnumFacing reverseDir = dir.func_176734_d();
    int[] srcSlots = getInventorySlots(src, dir, false, true, dstOwner);
    if (srcSlots.length == 0)
      return 0; 
    int[] dstSlots = getInventorySlots(dst, reverseDir, true, false, srcOwner);
    if (dstSlots.length == 0)
      return 0; 
    if (src instanceof IInventory) {
      IInventory srcInv = getInventory(src, dstOwner);
      if (srcInv == null)
        return 0; 
      if (dst instanceof IInventory) {
        IInventory dstInv = getInventory(dst, srcOwner);
        if (dstInv == null)
          return 0; 
        return transfer(srcInv, srcSlots, dstInv, dstSlots, dir, reverseDir, amount, checker, skipChecker);
      } 
      if (dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.func_176734_d())) {
        IItemHandler dstHandler = (IItemHandler)dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.func_176734_d());
        if (dstHandler == null)
          return 0; 
        return transfer(srcInv, srcSlots, dstHandler, dstSlots, reverseDir, amount, checker, skipChecker);
      } 
      return 0;
    } 
    if (src.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir)) {
      IItemHandler srcHandler = (IItemHandler)src.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
      if (srcHandler == null)
        return 0; 
      if (dst instanceof IInventory) {
        IInventory dstInv = getInventory(dst, srcOwner);
        if (dstInv == null)
          return 0; 
        return transfer(srcHandler, srcSlots, dstInv, dstSlots, reverseDir, amount, checker, skipChecker);
      } 
      if (dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.func_176734_d())) {
        IItemHandler dstHandler = (IItemHandler)dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.func_176734_d());
        if (dstHandler == null)
          return 0; 
        return transfer(srcHandler, srcSlots, dstHandler, dstSlots, amount, checker, skipChecker);
      } 
      return 0;
    } 
    return 0;
  }
  
  private static int transfer(IInventory src, int[] srcSlots, IInventory dst, int[] dstSlots, EnumFacing dir, EnumFacing reverseDir, int amount, Predicate<ItemStack> checker, boolean skipChecker) {
    ISidedInventory dstSided = (dst instanceof ISidedInventory) ? (ISidedInventory)dst : null;
    int total = amount;
    for (int srcSlot : srcSlots) {
      ItemStack srcStack = src.func_70301_a(srcSlot);
      if (!isEmpty(srcStack) && (skipChecker || checker.apply(srcStack))) {
        int transferred = insert(srcStack, amount, dst, dstSided, reverseDir, dstSlots);
        if (transferred > 0) {
          amount -= transferred;
          src.func_70299_a(srcSlot, decSize(srcStack, transferred));
          if (amount <= 0)
            break; 
        } 
      } 
    } 
    amount = total - amount;
    assert amount >= 0;
    if (amount > 0) {
      src.func_70296_d();
      dst.func_70296_d();
    } 
    return amount;
  }
  
  private static int transfer(IItemHandler src, int[] srcSlots, IInventory dst, int[] dstSlots, EnumFacing reverseDir, int amount, Predicate<ItemStack> checker, boolean skipChecker) {
    ISidedInventory dstSided = (dst instanceof ISidedInventory) ? (ISidedInventory)dst : null;
    int total = amount;
    for (int srcSlot : srcSlots) {
      ItemStack srcStack = src.extractItem(srcSlot, amount, true);
      if (!isEmpty(srcStack) && (skipChecker || checker.apply(srcStack))) {
        int transferred = insert(srcStack, amount, dst, dstSided, reverseDir, dstSlots);
        if (transferred > 0) {
          amount -= transferred;
          src.extractItem(srcSlot, transferred, false);
          if (amount <= 0)
            break; 
        } 
      } 
    } 
    amount = total - amount;
    assert amount >= 0;
    if (amount > 0)
      dst.func_70296_d(); 
    return amount;
  }
  
  private static int insert(ItemStack stack, int maxAmount, IInventory dst, ISidedInventory dstSided, EnumFacing side, int[] dstSlots) {
    int sizeLimit = Math.min(stack.func_77976_d(), dst.func_70297_j_());
    int total = Math.min(maxAmount, getSize(stack));
    int remaining = total;
    for (int pass = 0; pass < 2; pass++) {
      int i = 0;
      while (true) {
        int amount;
        if (i < dstSlots.length) {
          int dstSlot = dstSlots[i];
          if (dstSlot < 0)
            continue; 
          ItemStack dstStack = dst.func_70301_a(dstSlot);
          if ((pass == 0 && (isEmpty(dstStack) || !checkItemEqualityStrict(stack, dstStack))) || (
            pass == 1 && !isEmpty(dstStack)) || 
            !dst.func_94041_b(dstSlot, stack) || (
            dstSided != null && !dstSided.func_180462_a(dstSlot, stack, side)))
            continue; 
          amount = Math.min(remaining, sizeLimit - getSize(dstStack));
          if (isEmpty(dstStack)) {
            dst.func_70299_a(dstSlot, copyWithSize(stack, amount));
          } else {
            if (amount <= 0) {
              dstSlots[i] = -1;
            } else {
              dst.func_70299_a(dstSlot, incSize(dstStack, amount));
              assert amount > 0;
            } 
            continue;
          } 
        } else {
          break;
        } 
        assert amount <= 0;
        i++;
      } 
    } 
    return total - remaining;
  }
  
  private static int transfer(IItemHandler src, int[] srcSlots, IItemHandler dst, int[] dstSlots, int amount, Predicate<ItemStack> checker, boolean skipChecker) {
    int total = amount;
    for (int srcSlot : srcSlots) {
      ItemStack srcStack = src.extractItem(srcSlot, amount, true);
      if (!isEmpty(srcStack) && (skipChecker || checker.apply(srcStack))) {
        int transferred = insert(srcStack, 2147483647, dst, dstSlots);
        if (transferred > 0) {
          amount -= transferred;
          src.extractItem(srcSlot, transferred, false);
          if (amount <= 0)
            break; 
        } 
      } 
    } 
    amount = total - amount;
    assert amount >= 0;
    return amount;
  }
  
  private static int transfer(IInventory src, int[] srcSlots, IItemHandler dst, int[] dstSlots, EnumFacing dir, int amount, Predicate<ItemStack> checker, boolean skipChecker) {
    int total = amount;
    for (int srcSlot : srcSlots) {
      ItemStack srcStack = src.func_70301_a(srcSlot);
      if (!isEmpty(srcStack) && (skipChecker || checker.apply(srcStack))) {
        int transferred = insert(srcStack, amount, dst, dstSlots);
        if (transferred > 0) {
          amount -= transferred;
          src.func_70299_a(srcSlot, decSize(srcStack, transferred));
          if (amount <= 0)
            break; 
        } 
      } 
    } 
    amount = total - amount;
    assert amount >= 0;
    if (amount > 0)
      src.func_70296_d(); 
    return amount;
  }
  
  private static int insert(ItemStack stack, int maxAmount, IItemHandler dst, int[] dstSlots) {
    int total = Math.min(maxAmount, getSize(stack));
    int remaining = total;
    assert !isEmpty(stack);
    for (int pass = 0; pass < 2; pass++) {
      for (int dstSlot : dstSlots) {
        if (dstSlot >= 0) {
          ItemStack dstStack = dst.getStackInSlot(dstSlot);
          if ((pass != 0 || (!isEmpty(dstStack) && checkItemEqualityStrict(stack, dstStack))) && (
            pass != 1 || isEmpty(dstStack))) {
            ItemStack leftOver = dst.insertItem(dstSlot, copyWithSize(stack, remaining), false);
            int transferred = remaining - getSize(leftOver);
            remaining -= transferred;
            if (remaining <= 0)
              return total; 
          } 
        } 
      } 
    } 
    return total - remaining;
  }
  
  public static void distributeDrops(TileEntity source, List<ItemStack> stacks) {
    for (ListIterator<ItemStack> it = stacks.listIterator(); it.hasNext(); ) {
      ItemStack stack = it.next();
      int amount = distribute(source, stack, false);
      if (amount == getSize(stack)) {
        it.remove();
        continue;
      } 
      it.set(decSize(stack, amount));
    } 
    for (ItemStack stack : stacks)
      dropAsEntity(source.func_145831_w(), source.func_174877_v(), stack); 
    stacks.clear();
  }
  
  private static ItemStack getFromInventory(TileEntity source, AdjacentInv inventory, ItemStack stack, boolean ignoreMaxStackSize, boolean simulate) {
    return getFromInventory(inventory.te, inventory.dir.func_176734_d(), stack, getSize(stack), ignoreMaxStackSize, inventory.getAccessor(), simulate);
  }
  
  public static ItemStack getFromInventory(TileEntity te, EnumFacing side, ItemStack stackDestination, int max, boolean ignoreMaxStackSize, boolean simulate) {
    return getFromInventory(te, side, stackDestination, max, ignoreMaxStackSize, null, simulate);
  }
  
  public static ItemStack getFromInventory(TileEntity te, EnumFacing side, ItemStack stackDestination, int max, boolean ignoreMaxStackSize, GameProfile accessor, boolean simulate) {
    if (!isEmpty(stackDestination) && !ignoreMaxStackSize)
      max = Math.min(max, stackDestination.func_77976_d() - getSize(stackDestination)); 
    int[] slots = getInventorySlots(te, side, false, true, accessor);
    if (slots.length == 0)
      return emptyStack; 
    ItemStack ret = emptyStack;
    if (te instanceof IInventory) {
      IInventory inv = getInventory(te, accessor);
      if (inv == null)
        return emptyStack; 
      for (int slot : slots) {
        if (max <= 0)
          break; 
        ItemStack stack = inv.func_70301_a(slot);
        if (!isEmpty(stack))
          if (isEmpty(stackDestination) || checkItemEqualityStrict(stack, stackDestination)) {
            boolean extra = isEmpty(ret);
            if (extra) {
              ret = copyWithSize(stack, 1);
              if (isEmpty(stackDestination)) {
                if (!ignoreMaxStackSize)
                  max = Math.min(max, ret.func_77976_d()); 
                stackDestination = ret;
              } 
            } 
            int transfer = Math.min(max, getSize(stack));
            if (!simulate) {
              stack = decSize(stack, transfer);
              inv.func_70299_a(slot, stack);
            } 
            max -= transfer;
            ret = incSize(ret, extra ? (transfer - 1) : transfer);
          }  
      } 
      if (!simulate && !isEmpty(ret))
        inv.func_70296_d(); 
    } else if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
      IItemHandler handler = (IItemHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
      if (handler == null)
        return emptyStack; 
      for (int slot : slots) {
        if (max <= 0)
          break; 
        if (!isEmpty(stackDestination)) {
          ItemStack itemStack = handler.getStackInSlot(slot);
          if (isEmpty(itemStack) || !checkItemEqualityStrict(itemStack, stackDestination))
            continue; 
        } 
        ItemStack stack = handler.extractItem(slot, max, simulate);
        if (!isEmpty(stack)) {
          boolean extra = isEmpty(ret);
          if (extra) {
            ret = copyWithSize(stack, 1);
            if (isEmpty(stackDestination)) {
              if (!ignoreMaxStackSize)
                max = Math.min(max, ret.func_77976_d()); 
              stackDestination = ret;
            } 
          } else {
            assert checkItemEqualityStrict(stack, ret);
          } 
          int transfer = getSize(stack);
          max -= transfer;
          ret = incSize(ret, extra ? (transfer - 1) : transfer);
        } 
        continue;
      } 
    } 
    return ret;
  }
  
  private static int putInInventory(TileEntity source, AdjacentInv inventory, ItemStack stackSource, boolean simulate) {
    return putInInventory(inventory.te, inventory.dir.func_176734_d(), stackSource, inventory.getAccessor(), simulate);
  }
  
  public static int putInInventory(TileEntity te, EnumFacing side, ItemStack stackSource, boolean simulate) {
    return putInInventory(te, side, stackSource, null, simulate);
  }
  
  public static int putInInventory(TileEntity te, EnumFacing side, ItemStack stackSource, GameProfile accessor, boolean simulate) {
    if (isEmpty(stackSource))
      return 0; 
    int[] slots = getInventorySlots(te, side, true, false, accessor);
    if (slots.length == 0)
      return 0; 
    if (te instanceof IInventory) {
      IInventory inv = getInventory(te, accessor);
      if (inv == null)
        return 0; 
      int toTransfer = getSize(stackSource);
      for (int slot : slots) {
        if (toTransfer <= 0)
          break; 
        if (inv.func_94041_b(slot, stackSource) && (
          !(inv instanceof ISidedInventory) || ((ISidedInventory)inv).func_180462_a(slot, stackSource, side))) {
          ItemStack stack = inv.func_70301_a(slot);
          if (!isEmpty(stack) && checkItemEqualityStrict(stack, stackSource)) {
            int transfer = Math.min(toTransfer, Math.min(inv.func_70297_j_(), stack.func_77976_d()) - getSize(stack));
            if (!simulate)
              inv.func_70299_a(slot, incSize(stack, transfer)); 
            toTransfer -= transfer;
          } 
        } 
      } 
      for (int slot : slots) {
        if (toTransfer <= 0)
          break; 
        if (inv.func_94041_b(slot, stackSource) && (
          !(inv instanceof ISidedInventory) || ((ISidedInventory)inv).func_180462_a(slot, stackSource, side))) {
          ItemStack stack = inv.func_70301_a(slot);
          if (isEmpty(stack)) {
            int transfer = Math.min(toTransfer, Math.min(inv.func_70297_j_(), stackSource.func_77976_d()));
            if (!simulate) {
              ItemStack dest = copyWithSize(stackSource, transfer);
              inv.func_70299_a(slot, dest);
            } 
            toTransfer -= transfer;
          } 
        } 
      } 
      if (!simulate && toTransfer != getSize(stackSource))
        inv.func_70296_d(); 
      return getSize(stackSource) - toTransfer;
    } 
    if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
      IItemHandler handler = (IItemHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
      if (handler == null)
        return 0; 
      ItemStack src = stackSource.func_77946_l();
      for (int slot : slots) {
        if (isEmpty(src))
          break; 
        ItemStack stack = handler.getStackInSlot(slot);
        if (!isEmpty(stack)) {
          ItemStack remaining = handler.insertItem(slot, src.func_77946_l(), simulate);
          if (isEmpty(remaining)) {
            src = emptyStack;
          } else if (getSize(remaining) < getSize(src)) {
            src = setSize(src, getSize(remaining));
          } 
        } 
      } 
      for (int slot : slots) {
        if (isEmpty(src))
          break; 
        ItemStack stack = handler.getStackInSlot(slot);
        if (isEmpty(stack)) {
          ItemStack remaining = handler.insertItem(slot, src.func_77946_l(), simulate);
          if (isEmpty(remaining)) {
            src = emptyStack;
          } else if (getSize(remaining) < getSize(src)) {
            src = setSize(src, getSize(remaining));
          } 
        } 
      } 
      return getSize(stackSource) - getSize(src);
    } 
    return 0;
  }
  
  private static int[] getInventorySlots(TileEntity te, EnumFacing side, boolean checkInsert, boolean checkExtract, GameProfile accessor) {
    if (te instanceof IInventory) {
      ISidedInventory sidedInv;
      int[] ret;
      IInventory inv = getInventory(te, accessor);
      if (inv == null || inv
        .func_70297_j_() <= 0)
        return emptySlotArray; 
      if (inv instanceof ISidedInventory) {
        sidedInv = (ISidedInventory)inv;
        ret = sidedInv.func_180463_a(side);
        if (ret.length == 0)
          return emptySlotArray; 
        ret = Arrays.copyOf(ret, ret.length);
      } else {
        int size = inv.func_70302_i_();
        if (size <= 0)
          return emptySlotArray; 
        sidedInv = null;
        ret = new int[size];
        for (int i = 0; i < ret.length; i++)
          ret[i] = i; 
      } 
      if (checkInsert || checkExtract) {
        int writeIdx = 0;
        for (int readIdx = 0; readIdx < ret.length; readIdx++) {
          int slot = ret[readIdx];
          ItemStack stack = inv.func_70301_a(slot);
          if ((!checkExtract || (!isEmpty(stack) && (sidedInv == null || sidedInv
            .func_180461_b(slot, stack, side)))) && (!checkInsert || 
            isEmpty(stack) || (getSize(stack) < stack.func_77976_d() && getSize(stack) < inv.func_70297_j_() && (sidedInv == null || sidedInv
            .func_180462_a(slot, stack, side))))) {
            ret[writeIdx] = slot;
            writeIdx++;
          } 
        } 
        if (writeIdx != ret.length)
          ret = Arrays.copyOf(ret, writeIdx); 
      } 
      return ret;
    } 
    if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
      IItemHandler handler = (IItemHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
      if (handler == null)
        return emptySlotArray; 
      int size = handler.getSlots();
      if (size <= 0)
        return emptySlotArray; 
      int[] ret = new int[size];
      for (int i = 0; i < ret.length; i++)
        ret[i] = i; 
      if (checkInsert || checkExtract) {
        int writeIdx = 0;
        for (int readIdx = 0; readIdx < ret.length; readIdx++) {
          int slot = ret[readIdx];
          ItemStack stack = handler.getStackInSlot(slot);
          if ((!checkExtract || (!isEmpty(stack) && 
            !isEmpty(handler.extractItem(slot, 2147483647, true)))) && (!checkInsert || 
            checkInsert(handler, slot, stack))) {
            ret[writeIdx] = slot;
            writeIdx++;
          } 
        } 
        if (writeIdx != ret.length)
          ret = Arrays.copyOf(ret, writeIdx); 
      } 
      return ret;
    } 
    return emptySlotArray;
  }
  
  private static boolean checkInsert(IItemHandler handler, int slot, ItemStack stack) {
    if (isEmpty(stack) || getSize(stack) >= stack.func_77976_d())
      return true; 
    int testSize = Integer.MAX_VALUE;
    ItemStack result = handler.insertItem(slot, copyWithSize(stack, 2147483647), true);
    return (isEmpty(result) || getSize(result) < Integer.MAX_VALUE);
  }
  
  public static boolean consumeFromPlayerInventory(EntityPlayer player, Predicate<ItemStack> request, int amount, boolean simulate) {
    NonNullList<ItemStack> contents = player.field_71071_by.field_70462_a;
    for (int pass = 0; pass < 2; pass++) {
      int amountNeeded = amount;
      for (int i = 0; i < contents.size(); i++) {
        ItemStack stack = (ItemStack)contents.get(i);
        if (request.apply(stack)) {
          if (player.field_71075_bZ.field_75098_d)
            return true; 
          int cAmount = Math.min(getSize(stack), amountNeeded);
          amountNeeded -= cAmount;
          if (pass == 1)
            contents.set(i, decSize(stack, cAmount)); 
          if (amountNeeded <= 0)
            break; 
        } 
      } 
      if (amountNeeded > 0) {
        if (pass == 1)
          IC2.log.warn(LogCategory.General, "Inconsistent inventory transaction for player %s, request %s: %d missing", new Object[] { player, request, Integer.valueOf(amountNeeded) }); 
        return false;
      } 
      if (simulate)
        return true; 
    } 
    return true;
  }
  
  public static Predicate<ItemStack> sameStack(final ItemStack stack) {
    if (isEmpty(stack))
      throw new IllegalArgumentException("empty stack"); 
    return new Predicate<ItemStack>() {
        public boolean apply(ItemStack input) {
          return StackUtil.checkItemEquality(input, stack);
        }
        
        public String toString() {
          return "stack==" + stack;
        }
      };
  }
  
  public static Predicate<ItemStack> sameItem(final Item item) {
    if (item == null)
      throw new NullPointerException("null item"); 
    return new Predicate<ItemStack>() {
        public boolean apply(ItemStack input) {
          return (input.func_77973_b() == item);
        }
        
        public String toString() {
          return "item==" + item;
        }
      };
  }
  
  public static Predicate<ItemStack> sameItem(Block block) {
    if (block == null)
      throw new NullPointerException("null block"); 
    Item item = Item.func_150898_a(block);
    if (item == null || (item == Items.field_190931_a && block != Blocks.field_150350_a))
      throw new IllegalArgumentException("block " + block + " doesn't have an associated item"); 
    return sameItem(item);
  }
  
  public static Predicate<ItemStack> oreDict(String name) {
    return recipeInput(Recipes.inputFactory.forOreDict(name));
  }
  
  public static Predicate<ItemStack> recipeInput(final IRecipeInput item) {
    return new Predicate<ItemStack>() {
        public boolean apply(ItemStack input) {
          return item.matches(input);
        }
        
        public String toString() {
          return item.toString();
        }
      };
  }
  
  public static final Predicate<ItemStack> anyStack = Predicates.alwaysTrue();
  
  public static boolean consume(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount) {
    return (consume0(player, hand, request, amount, false) != emptyStack);
  }
  
  public static ItemStack consumeAndGet(EntityPlayer player, Predicate<ItemStack> request, int amount) {
    return consumeAndGet(player, EnumHand.MAIN_HAND, request, amount);
  }
  
  public static ItemStack consumeAndGet(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount) {
    return consume0(player, hand, request, amount, true);
  }
  
  public static void consumeOrError(EntityPlayer player, EnumHand hand, int amount) {
    consumeOrError(player, hand, anyStack, amount);
  }
  
  public static void consumeOrError(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount) {
    if (!consume(player, hand, request, amount))
      throw new IllegalStateException("consume failed"); 
  }
  
  private static ItemStack consume0(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount, boolean copyOutput) {
    ItemStack ret;
    if (amount <= 0)
      throw new IllegalArgumentException("negative/zero amount"); 
    ItemStack stack = get(player, hand);
    if (isEmpty(stack))
      return emptyStack; 
    if (!request.apply(stack))
      return emptyStack; 
    if (player.field_71075_bZ.field_75098_d)
      return copyOutput ? copyWithSize(stack, amount) : stack; 
    if (getSize(stack) < amount)
      return emptyStack; 
    if (getSize(stack) == amount) {
      ret = stack;
      clear(player, hand);
    } else {
      ret = copyOutput ? copyWithSize(stack, amount) : stack;
      set(player, hand, decSize(stack, amount));
    } 
    return ret;
  }
  
  public static boolean damage(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount) {
    return (damage0(player, hand, request, amount, false) != emptyStack);
  }
  
  public static void damageOrError(EntityPlayer player, EnumHand hand, int amount) {
    damageOrError(player, hand, anyStack, amount);
  }
  
  public static void damageOrError(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount) {
    if (!damage(player, hand, request, amount))
      throw new IllegalStateException("damage failed"); 
  }
  
  private static ItemStack damage0(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount, boolean copyOutput) {
    ItemStack ret;
    if (amount <= 0)
      throw new IllegalArgumentException("negative/zero amount"); 
    ItemStack stack = get(player, hand);
    if (isEmpty(stack))
      return emptyStack; 
    int maxDamage = stack.func_77958_k();
    if (maxDamage <= 0)
      return emptyStack; 
    if (!request.apply(stack))
      return emptyStack; 
    if (player.field_71075_bZ.field_75098_d || !stack.func_77984_f())
      return copyOutput ? copy(stack) : stack; 
    stack.func_77972_a(amount, (EntityLivingBase)player);
    if (isEmpty(stack)) {
      ret = stack;
      clear(player, hand);
    } else {
      ret = copyOutput ? copy(stack) : stack;
      set(player, hand, stack);
    } 
    return ret;
  }
  
  public static ItemStack get(EntityPlayer player, EnumHand hand) {
    return player.func_184586_b(hand);
  }
  
  public static void set(EntityPlayer player, EnumHand hand, ItemStack stack) {
    if (isEmpty(stack))
      stack = emptyStack; 
    InventoryPlayer inv = player.field_71071_by;
    if (hand == EnumHand.MAIN_HAND) {
      inv.field_70462_a.set(inv.field_70461_c, stack);
    } else if (hand == EnumHand.OFF_HAND) {
      inv.field_184439_c.set(0, stack);
    } else {
      throw new IllegalArgumentException("invalid hand: " + hand);
    } 
  }
  
  public static void clear(EntityPlayer player, EnumHand hand) {
    set(player, hand, emptyStack);
  }
  
  public static void clearEmpty(EntityPlayer player, EnumHand hand) {
    if (isEmpty(player, hand))
      clear(player, hand); 
  }
  
  public static void dropAsEntity(World world, BlockPos pos, ItemStack stack) {
    if (isEmpty(stack))
      return; 
    double f = 0.7D;
    double dx = world.field_73012_v.nextFloat() * f + (1.0D - f) * 0.5D;
    double dy = world.field_73012_v.nextFloat() * f + (1.0D - f) * 0.5D;
    double dz = world.field_73012_v.nextFloat() * f + (1.0D - f) * 0.5D;
    EntityItem entityItem = new EntityItem(world, pos.func_177958_n() + dx, pos.func_177956_o() + dy, pos.func_177952_p() + dz, stack.func_77946_l());
    entityItem.func_174869_p();
    world.func_72838_d((Entity)entityItem);
  }
  
  public static ItemStack copy(ItemStack stack) {
    return stack.func_77946_l();
  }
  
  public static ItemStack copyWithSize(ItemStack stack, int newSize) {
    if (isEmpty(stack))
      throw new IllegalArgumentException("empty stack: " + toStringSafe(stack)); 
    return setSize(copy(stack), newSize);
  }
  
  public static ItemStack copyShrunk(ItemStack stack, int amount) {
    if (isEmpty(stack))
      throw new IllegalArgumentException("empty stack: " + toStringSafe(stack)); 
    return setSize(copy(stack), getSize(stack) - amount);
  }
  
  public static ItemStack copyWithWildCard(ItemStack stack) {
    ItemStack ret = copy(stack);
    setRawMeta(ret, 32767);
    return ret;
  }
  
  public static Collection<ItemStack> copy(Collection<ItemStack> c) {
    List<ItemStack> ret = new ArrayList<>(c.size());
    for (ItemStack stack : c)
      ret.add(copy(stack)); 
    return ret;
  }
  
  public static NBTTagCompound getOrCreateNbtData(ItemStack stack) {
    NBTTagCompound ret = stack.func_77978_p();
    if (ret == null) {
      ret = new NBTTagCompound();
      stack.func_77982_d(ret);
    } 
    return ret;
  }
  
  public static boolean checkItemEquality(ItemStack a, ItemStack b) {
    return ((isEmpty(a) && isEmpty(b)) || (
      !isEmpty(a) && !isEmpty(b) && a
      .func_77973_b() == b.func_77973_b() && (
      !a.func_77981_g() || a.func_77960_j() == b.func_77960_j()) && 
      checkNbtEquality(a, b)));
  }
  
  public static boolean checkItemEquality(ItemStack a, Item b) {
    return ((isEmpty(a) && b == null) || (
      !isEmpty(a) && b != null && a
      .func_77973_b() == b));
  }
  
  public static boolean checkItemEqualityStrict(ItemStack a, ItemStack b) {
    return ((isEmpty(a) && isEmpty(b)) || (
      !isEmpty(a) && !isEmpty(b) && a
      .func_77969_a(b) && 
      checkNbtEqualityStrict(a, b)));
  }
  
  private static boolean checkNbtEquality(ItemStack a, ItemStack b) {
    return checkNbtEquality(a.func_77978_p(), b.func_77978_p());
  }
  
  public static boolean checkNbtEquality(NBTTagCompound a, NBTTagCompound b) {
    if (a == b)
      return true; 
    Set<String> keysA = (a != null) ? a.func_150296_c() : Collections.<String>emptySet();
    Set<String> keysB = (b != null) ? b.func_150296_c() : Collections.<String>emptySet();
    Set<String> toCheck = new HashSet<>(Math.max(keysA.size(), keysB.size()));
    for (String key : keysA) {
      if (ignoredNbtKeys.contains(key))
        continue; 
      if (!keysB.contains(key))
        return false; 
      toCheck.add(key);
    } 
    for (String key : keysB) {
      if (ignoredNbtKeys.contains(key))
        continue; 
      if (!keysA.contains(key))
        return false; 
      toCheck.add(key);
    } 
    for (String key : toCheck) {
      if (!a.func_74781_a(key).equals(b.func_74781_a(key)))
        return false; 
    } 
    return true;
  }
  
  static final Set<String> ignoredNbtKeys = new HashSet<>(Arrays.asList(new String[] { "damage", "charge", "energy", "advDmg" }));
  
  public static boolean checkNbtEqualityStrict(ItemStack a, ItemStack b) {
    NBTTagCompound nbtA = a.func_77978_p();
    NBTTagCompound nbtB = b.func_77978_p();
    if (nbtA == nbtB)
      return true; 
    return (nbtA != null && nbtB != null && nbtA.equals(nbtB));
  }
  
  public static ItemStack getPickStack(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
    RayTraceResult target = new RayTraceResult(RayTraceResult.Type.BLOCK, new Vec3d((Vec3i)pos), EnumFacing.DOWN, pos);
    ItemStack ret = state.func_177230_c().getPickBlock(state, target, world, pos, player);
    if (isEmpty(ret))
      return emptyStack; 
    return ret;
  }
  
  public static List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    return getDrops(world, pos, state, state.func_177230_c(), fortune);
  }
  
  public static List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, Block block, int fortune) {
    NonNullList<ItemStack> drops = NonNullList.func_191196_a();
    assert world.func_180495_p(pos).func_177230_c() == block;
    block.getDrops(drops, world, pos, state, fortune);
    return (List<ItemStack>)drops;
  }
  
  public static List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, EntityPlayer player, int fortune, boolean silkTouch) {
    Block block = state.func_177230_c();
    if (block.isAir(state, world, pos))
      return Collections.emptyList(); 
    World rawWorld = null;
    if (silkTouch) {
      rawWorld = Util.getWorld(world);
      if (rawWorld == null)
        throw new IllegalArgumentException("invalid world for silk touch: " + world); 
      if (player == null)
        player = Ic2Player.get(rawWorld); 
    } 
    ItemStack drop;
    if (silkTouch && block
      .canSilkHarvest(rawWorld, pos, state, player) && 
      !isEmpty(drop = getPickStack(rawWorld, pos, state, player)))
      return Collections.singletonList(drop); 
    return getDrops(world, pos, state, block, fortune);
  }
  
  public static boolean placeBlock(ItemStack stack, World world, BlockPos pos) {
    if (isEmpty(stack))
      return false; 
    Item item = stack.func_77973_b();
    if (item instanceof net.minecraft.item.ItemBlock || item instanceof net.minecraft.item.ItemBlockSpecial) {
      int oldSize = getSize(stack);
      EntityPlayer player = Ic2Player.get(world);
      EnumHand hand = EnumHand.MAIN_HAND;
      ItemStack prev = player.func_184586_b(hand);
      player.func_184611_a(hand, stack);
      EnumActionResult result = item.func_180614_a(player, world, pos, hand, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
      player.func_184611_a(hand, prev);
      stack = setSize(stack, oldSize);
      return (result == EnumActionResult.SUCCESS);
    } 
    return false;
  }
  
  public static boolean isEmpty(ItemStack stack) {
    return (stack == emptyStack || stack == null || stack.func_77973_b() == null || stack.func_190916_E() <= 0);
  }
  
  public static boolean isEmpty(EntityPlayer player, EnumHand hand) {
    return isEmpty(player.func_184586_b(hand));
  }
  
  public static int getSize(ItemStack stack) {
    if (isEmpty(stack))
      return 0; 
    return stack.func_190916_E();
  }
  
  public static ItemStack setSize(ItemStack stack, int size) {
    if (size <= 0)
      return emptyStack; 
    stack.func_190920_e(size);
    return stack;
  }
  
  public static ItemStack incSize(ItemStack stack) {
    return incSize(stack, 1);
  }
  
  public static ItemStack incSize(ItemStack stack, int amount) {
    return setSize(stack, getSize(stack) + amount);
  }
  
  public static ItemStack decSize(ItemStack stack) {
    return decSize(stack, 1);
  }
  
  public static ItemStack decSize(ItemStack stack, int amount) {
    return incSize(stack, -amount);
  }
  
  public static boolean check2(Iterable<List<ItemStack>> list) {
    for (List<ItemStack> list2 : list) {
      if (!check(list2))
        return false; 
    } 
    return true;
  }
  
  public static boolean check(ItemStack[] array) {
    return check(Arrays.asList(array));
  }
  
  public static boolean check(Iterable<ItemStack> list) {
    for (ItemStack stack : list) {
      if (!check(stack))
        return false; 
    } 
    return true;
  }
  
  public static boolean check(ItemStack stack) {
    return (stack.func_77973_b() != null);
  }
  
  public static String toStringSafe2(Iterable<List<ItemStack>> list) {
    StringBuilder ret = new StringBuilder("[");
    for (List<ItemStack> list2 : list) {
      if (ret.length() > 1)
        ret.append(", "); 
      ret.append(toStringSafe(list2));
    } 
    return ret.append(']').toString();
  }
  
  public static String toStringSafe(ItemStack[] array) {
    return toStringSafe(Arrays.asList(array));
  }
  
  public static String toStringSafe(Iterable<ItemStack> list) {
    StringBuilder ret = new StringBuilder("[");
    for (ItemStack stack : list) {
      if (ret.length() > 1)
        ret.append(", "); 
      ret.append(toStringSafe(stack));
    } 
    return ret.append(']').toString();
  }
  
  public static String toStringSafe(ItemStack stack) {
    if (stack == null)
      return "(null)"; 
    if (stack.func_77973_b() == null)
      return getSize(stack) + "x(null)@(unknown)"; 
    return stack.toString();
  }
  
  public static boolean storeInventoryItem(ItemStack stack, EntityPlayer player, boolean simulate) {
    if (simulate) {
      int sizeLeft = getSize(stack);
      int maxStackSize = Math.min(player.field_71071_by.func_70297_j_(), stack.func_77976_d());
      for (int i = 0; i < player.field_71071_by.field_70462_a.size() && sizeLeft > 0; i++) {
        ItemStack invStack = (ItemStack)player.field_71071_by.field_70462_a.get(i);
        if (isEmpty(invStack)) {
          sizeLeft -= maxStackSize;
        } else if (checkItemEqualityStrict(stack, invStack) && getSize(invStack) < maxStackSize) {
          sizeLeft -= maxStackSize - getSize(invStack);
        } 
      } 
      return (sizeLeft <= 0);
    } 
    return player.field_71071_by.func_70441_a(stack);
  }
  
  public static int getRawMeta(ItemStack stack) {
    return Items.field_151100_aR.getDamage(stack);
  }
  
  public static void setRawMeta(ItemStack stack, int meta) {
    if (meta < 0)
      throw new IllegalArgumentException("negative meta"); 
    Items.field_151100_aR.setDamage(stack, meta);
  }
  
  public static TIntSet getSlotsFromInv(IInventory inv) {
    TIntHashSet tIntHashSet = new TIntHashSet();
    for (int i = 0; i < inv.func_70302_i_(); i++)
      tIntHashSet.add(i); 
    return (TIntSet)tIntHashSet;
  }
  
  public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(IInventory craftMatrix) {
    return balanceStacks(craftMatrix, Collections.emptySet());
  }
  
  public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(IInventory craftMatrix, ItemStack sourceItemStack) {
    return balanceStacks(craftMatrix, Collections.singleton(sourceItemStack));
  }
  
  public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(final IInventory inv, Collection<ItemStack> additionalItems) {
    return balanceStacks(inv, new Predicate<Tuple.T2<ItemStack, Integer>>() {
          public boolean apply(Tuple.T2<ItemStack, Integer> input) {
            return !StackUtil.isEmpty(inv.func_70301_a(((Integer)input.b).intValue()));
          }
        },  getSlotsFromInv(inv), additionalItems);
  }
  
  public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(IInventory inv, Predicate<Tuple.T2<ItemStack, Integer>> canInsert) {
    return balanceStacks(inv, canInsert, getSlotsFromInv(inv), Collections.emptySet());
  }
  
  public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(IInventory inv, Predicate<Tuple.T2<ItemStack, Integer>> canInsert, TIntSet originalAvailableSlots, Collection<ItemStack> additionalStacksOriginal) {
    List<ItemStack> additionalStacks = new LinkedList<>(additionalStacksOriginal);
    TIntHashSet tIntHashSet = new TIntHashSet((TIntCollection)originalAvailableSlots);
    List<ItemStack> leftOvers = new ArrayList<>();
    for (int i = 0; i < inv.func_70302_i_(); i++) {
      if (tIntHashSet.contains(i)) {
        ItemStack stack = inv.func_70301_a(i);
        if (!isEmpty(stack)) {
          int amount = 0;
          for (ListIterator<ItemStack> iter = additionalStacks.listIterator(); iter.hasNext(); ) {
            ItemStack currentStack = iter.next();
            if (checkItemEqualityStrict(currentStack, stack)) {
              iter.remove();
              amount += getSize(currentStack);
            } 
          } 
          amount = distributeStackToSlots(inv, stack, (TIntSet)tIntHashSet, canInsert, amount);
          while (amount > 0) {
            int size = Math.min(stack.func_77976_d(), amount);
            amount -= size;
            leftOvers.add(copyWithSize(stack, size));
          } 
        } 
      } 
    } 
    for (ItemStack stack : additionalStacks) {
      int amount = distributeStackToSlots(inv, stack, (TIntSet)tIntHashSet, canInsert, getSize(stack));
      if (amount > 0)
        leftOvers.add(copyWithSize(stack, amount)); 
    } 
    originalAvailableSlots.removeAll((TIntCollection)tIntHashSet);
    return (Tuple.T2)new Tuple.T2<>(leftOvers, originalAvailableSlots);
  }
  
  private static int distributeStackToSlots(final IInventory inv, ItemStack stack, TIntSet availableSlots, Predicate<Tuple.T2<ItemStack, Integer>> canInsert, int amount) {
    TIntArrayList tIntArrayList = new TIntArrayList();
    for (TIntIterator iter = availableSlots.iterator(); iter.hasNext(); ) {
      int currentSlot = iter.next();
      ItemStack currentStack = inv.func_70301_a(currentSlot);
      if ((checkItemEqualityStrict(stack, currentStack) || isEmpty(currentStack)) && canInsert.apply(new Tuple.T2<>(stack, Integer.valueOf(currentSlot)))) {
        amount += getSize(currentStack);
        tIntArrayList.add(currentSlot);
        iter.remove();
      } 
    } 
    tIntArrayList.sort();
    int maxStackSize = Math.min(stack.func_77976_d(), inv.func_70297_j_());
    int slotsLeft = tIntArrayList.size();
    for (TIntIterator tIntIterator1 = tIntArrayList.iterator(); tIntIterator1.hasNext() && amount > 0; slotsLeft--, tIntIterator1.remove()) {
      int currentSlot = tIntIterator1.next();
      int itemsToPut = amount / slotsLeft;
      if (amount % slotsLeft > 0)
        itemsToPut++; 
      itemsToPut = Math.min(itemsToPut, maxStackSize);
      inv.func_70299_a(currentSlot, copyWithSize(stack, itemsToPut));
      amount -= itemsToPut;
    } 
    if (!tIntArrayList.isEmpty()) {
      assert amount <= 0;
      tIntArrayList.forEach(new TIntProcedure() {
            public boolean execute(int currentSlot) {
              inv.func_70299_a(currentSlot, StackUtil.emptyStack);
              return true;
            }
          });
    } 
    assert amount <= 0 || slotsLeft == 0;
    return amount;
  }
  
  public static ItemStack setImmutableSize(ItemStack stack, int size) {
    if (getSize(stack) != size)
      stack = copyWithSize(stack, size); 
    return stack;
  }
  
  public static boolean matchesNBT(NBTTagCompound subject, NBTTagCompound target) {
    if (subject == null)
      return (target == null || target.func_82582_d()); 
    if (target == null)
      return true; 
    for (String key : target.func_150296_c()) {
      NBTBase targetNBT = target.func_74781_a(key);
      if (!subject.func_74764_b(key) || targetNBT.func_74732_a() != subject.func_150299_b(key))
        return false; 
      NBTBase subjectNBT = subject.func_74781_a(key);
      if (!targetNBT.equals(subjectNBT))
        return false; 
    } 
    return true;
  }
  
  public static ItemStack wrapEmpty(ItemStack stack) {
    return (stack == null) ? emptyStack : stack;
  }
  
  public static final ItemStack emptyStack = ItemStack.field_190927_a;
  
  private static final int[] emptySlotArray = new int[0];
}
