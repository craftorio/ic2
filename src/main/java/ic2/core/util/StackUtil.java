package ic2.core.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.mojang.authlib.GameProfile;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
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
import net.minecraft.block.BlockChest.Type;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class StackUtil
{
	public static final Predicate<ItemStack> anyStack = Predicates.alwaysTrue();
	static final Set<String> ignoredNbtKeys = new HashSet<>(Arrays.asList("damage", "charge", "energy", "advDmg"));
	public static final ItemStack emptyStack = ItemStack.EMPTY;
	private static final int[] emptySlotArray = new int[0];

	public static boolean isInventoryTile(TileEntity te, EnumFacing side)
	{
		return te instanceof IInventory || te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
	}

	public static IInventory findDoubleChest(TileEntityChest chest)
	{
		World world = chest.getWorld();
		BlockPos pos = chest.getPos();
		if (world != null && pos != null && world.isBlockLoaded(pos))
		{
			Type type = chest.getChestType();

			for (EnumFacing facing : EnumFacing.HORIZONTALS)
			{
				TileEntity te = world.getTileEntity(pos.offset(facing));
				if (te instanceof TileEntityChest && ((TileEntityChest) te).getChestType() == type)
				{
					ILockableContainer left;
					ILockableContainer right;
					if (facing != EnumFacing.WEST && facing != EnumFacing.NORTH)
					{
						left = chest;
						right = (TileEntityChest) te;
					} else
					{
						left = (TileEntityChest) te;
						right = chest;
					}

					return new InventoryLargeChest("container.chestDouble", left, right);
				}
			}

			return chest;
		} else
		{
			return null;
		}
	}

	public static StackUtil.AdjacentInv getAdjacentInventory(TileEntity source, EnumFacing dir)
	{
		TileEntity target = source.getWorld().getTileEntity(source.getPos().offset(dir));
		if (!isInventoryTile(target, dir))
		{
			return null;
		} else
		{
			GameProfile srcOwner;
			if (target instanceof IPersonalBlock && source instanceof IPersonalBlock && (srcOwner = ((IPersonalBlock) source).getOwner()) != null)
			{
				return new StackUtil.PersonalAdjacentInv(target, dir, srcOwner);
			} else
			{
				return target instanceof TileEntityChest && findDoubleChest((TileEntityChest) target) == null ? null : new StackUtil.AdjacentInv(target, dir);
			}
		}
	}

	public static List<StackUtil.AdjacentInv> getAdjacentInventories(TileEntity source)
	{
		List<StackUtil.AdjacentInv> inventories = new ArrayList<>();

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			StackUtil.AdjacentInv inventory = getAdjacentInventory(source, dir);
			if (inventory != null)
			{
				inventories.add(inventory);
			}
		}

		Collections.sort(
			inventories,
			new Comparator<StackUtil.AdjacentInv>()
			{
				public int compare(StackUtil.AdjacentInv a, StackUtil.AdjacentInv b)
				{
					if (a.te instanceof IPersonalBlock || !(b.te instanceof IPersonalBlock))
					{
						return -1;
					} else
					{
						return !(b.te instanceof IPersonalBlock) && a.te instanceof IPersonalBlock
							? StackUtil.getInventorySize(b.te, b.dir.getOpposite(), b.getAccessor())
							  - StackUtil.getInventorySize(a.te, a.dir.getOpposite(), a.getAccessor())
							: 1;
					}
				}
			}
		);
		return inventories;
	}

	public static GameProfile getOwner(TileEntity te)
	{
		return te instanceof IPersonalBlock ? ((IPersonalBlock) te).getOwner() : null;
	}

	public static int getInventorySize(TileEntity te, EnumFacing side, GameProfile accessor)
	{
		if (te instanceof IInventory)
		{
			IInventory inv = getInventory(te, accessor);
			return inv == null ? 0 : inv.getSizeInventory();
		} else if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = (IItemHandler) te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			return handler == null ? 0 : handler.getSlots();
		} else
		{
			return 0;
		}
	}

	private static IInventory getInventory(TileEntity te, GameProfile accessor)
	{
		if (te instanceof TileEntityChest)
		{
			return findDoubleChest((TileEntityChest) te);
		} else if (te instanceof IPersonalBlock)
		{
			return ((IPersonalBlock) te).getPrivilegedInventory(accessor);
		} else
		{
			return te instanceof IInventory ? (IInventory) te : null;
		}
	}

	public static int distribute(TileEntity source, ItemStack stack, boolean simulate)
	{
		ItemStack remaining = copy(stack);

		for (StackUtil.AdjacentInv inventory : getAdjacentInventories(source))
		{
			int amount = putInInventory(source, inventory, remaining, simulate);
			remaining = decSize(remaining, amount);
			if (isEmpty(remaining))
			{
				break;
			}
		}

		return getSize(stack) - getSize(remaining);
	}

	public static int fetch(TileEntity source, ItemStack stack, boolean simulate)
	{
		ItemStack remaining = copy(stack);

		for (StackUtil.AdjacentInv inventory : getAdjacentInventories(source))
		{
			ItemStack transferred = getFromInventory(source, inventory, remaining, true, simulate);
			if (!isEmpty(transferred))
			{
				remaining = decSize(remaining, getSize(transferred));
				if (isEmpty(remaining))
				{
					break;
				}
			}
		}

		return getSize(stack) - getSize(remaining);
	}

	public static int transfer(TileEntity src, TileEntity dst, EnumFacing dir, int amount)
	{
		return transfer(src, dst, dir, amount, Predicates.alwaysTrue(), true);
	}

	public static int transfer(TileEntity src, TileEntity dst, EnumFacing dir, int amount, Predicate<ItemStack> checker)
	{
		return transfer(src, dst, dir, amount, checker, checker == null || Predicates.alwaysTrue().equals(checker));
	}

	private static int transfer(TileEntity src, TileEntity dst, EnumFacing dir, int amount, Predicate<ItemStack> checker, boolean skipChecker)
	{
		if (amount <= 0)
		{
			return 0;
		}

		GameProfile srcOwner = getOwner(src);
		GameProfile dstOwner = getOwner(dst);
		EnumFacing reverseDir = dir.getOpposite();
		int[] srcSlots = getInventorySlots(src, dir, false, true, dstOwner);
		if (srcSlots.length == 0)
		{
			return 0;
		}

		int[] dstSlots = getInventorySlots(dst, reverseDir, true, false, srcOwner);
		if (dstSlots.length == 0)
		{
			return 0;
		}

		if (src instanceof IInventory)
		{
			IInventory srcInv = getInventory(src, dstOwner);
			if (srcInv == null)
			{
				return 0;
			} else if (dst instanceof IInventory)
			{
				IInventory dstInv = getInventory(dst, srcOwner);
				return dstInv == null ? 0 : transfer(srcInv, srcSlots, dstInv, dstSlots, dir, reverseDir, amount, checker, skipChecker);
			} else if (dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()))
			{
				IItemHandler dstHandler = (IItemHandler) dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
				return dstHandler == null ? 0 : transfer(srcInv, srcSlots, dstHandler, dstSlots, reverseDir, amount, checker, skipChecker);
			} else
			{
				return 0;
			}
		} else if (src.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir))
		{
			IItemHandler srcHandler = (IItemHandler) src.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
			if (srcHandler == null)
			{
				return 0;
			} else if (dst instanceof IInventory)
			{
				IInventory dstInv = getInventory(dst, srcOwner);
				return dstInv == null ? 0 : transfer(srcHandler, srcSlots, dstInv, dstSlots, reverseDir, amount, checker, skipChecker);
			} else if (dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()))
			{
				IItemHandler dstHandler = (IItemHandler) dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
				return dstHandler == null ? 0 : transfer(srcHandler, srcSlots, dstHandler, dstSlots, amount, checker, skipChecker);
			} else
			{
				return 0;
			}
		} else
		{
			return 0;
		}
	}

	private static int transfer(
		IInventory src,
		int[] srcSlots,
		IInventory dst,
		int[] dstSlots,
		EnumFacing dir,
		EnumFacing reverseDir,
		int amount,
		Predicate<ItemStack> checker,
		boolean skipChecker
	)
	{
		ISidedInventory dstSided = dst instanceof ISidedInventory ? (ISidedInventory) dst : null;
		int total = amount;

		for (int srcSlot : srcSlots)
		{
			ItemStack srcStack = src.getStackInSlot(srcSlot);
			if (!isEmpty(srcStack) && (skipChecker || checker.apply(srcStack)))
			{
				int transferred = insert(srcStack, amount, dst, dstSided, reverseDir, dstSlots);
				if (transferred > 0)
				{
					amount -= transferred;
					src.setInventorySlotContents(srcSlot, decSize(srcStack, transferred));
					if (amount <= 0)
					{
						break;
					}
				}
			}
		}

		amount = total - amount;
		assert amount >= 0;
		if (amount > 0)
		{
			src.markDirty();
			dst.markDirty();
		}

		return amount;
	}

	private static int transfer(
		IItemHandler src, int[] srcSlots, IInventory dst, int[] dstSlots, EnumFacing reverseDir, int amount, Predicate<ItemStack> checker, boolean skipChecker
	)
	{
		ISidedInventory dstSided = dst instanceof ISidedInventory ? (ISidedInventory) dst : null;
		int total = amount;

		for (int srcSlot : srcSlots)
		{
			ItemStack srcStack = src.extractItem(srcSlot, amount, true);
			if (!isEmpty(srcStack) && (skipChecker || checker.apply(srcStack)))
			{
				int transferred = insert(srcStack, amount, dst, dstSided, reverseDir, dstSlots);
				if (transferred > 0)
				{
					amount -= transferred;
					src.extractItem(srcSlot, transferred, false);
					if (amount <= 0)
					{
						break;
					}
				}
			}
		}

		amount = total - amount;
		assert amount >= 0;
		if (amount > 0)
		{
			dst.markDirty();
		}

		return amount;
	}

	private static int insert(ItemStack stack, int maxAmount, IInventory dst, ISidedInventory dstSided, EnumFacing side, int[] dstSlots)
	{
		int sizeLimit = Math.min(stack.getMaxStackSize(), dst.getInventoryStackLimit());
		int total = Math.min(maxAmount, getSize(stack));
		int remaining = total;

		for (int pass = 0; pass < 2; pass++)
		{
			for (int i = 0; i < dstSlots.length; i++)
			{
				int dstSlot = dstSlots[i];
				if (dstSlot >= 0)
				{
					ItemStack dstStack = dst.getStackInSlot(dstSlot);
					if ((pass != 0 || !isEmpty(dstStack) && checkItemEqualityStrict(stack, dstStack))
						&& (pass != 1 || isEmpty(dstStack))
						&& dst.isItemValidForSlot(dstSlot, stack)
						&& (dstSided == null || dstSided.canInsertItem(dstSlot, stack, side)))
					{
						int amount = Math.min(remaining, sizeLimit - getSize(dstStack));
						if (isEmpty(dstStack))
						{
							dst.setInventorySlotContents(dstSlot, copyWithSize(stack, amount));
						} else
						{
							if (amount <= 0)
							{
								dstSlots[i] = -1;
								continue;
							}

							dst.setInventorySlotContents(dstSlot, incSize(dstStack, amount));
						}

						assert amount > 0;
						remaining -= amount;
						if (remaining <= 0)
						{
							return total;
						}
					}
				}
			}
		}

		return total - remaining;
	}

	private static int transfer(
		IItemHandler src, int[] srcSlots, IItemHandler dst, int[] dstSlots, int amount, Predicate<ItemStack> checker, boolean skipChecker
	)
	{
		int total = amount;

		for (int srcSlot : srcSlots)
		{
			ItemStack srcStack = src.extractItem(srcSlot, amount, true);
			if (!isEmpty(srcStack) && (skipChecker || checker.apply(srcStack)))
			{
				int transferred = insert(srcStack, Integer.MAX_VALUE, dst, dstSlots);
				if (transferred > 0)
				{
					amount -= transferred;
					src.extractItem(srcSlot, transferred, false);
					if (amount <= 0)
					{
						break;
					}
				}
			}
		}

		amount = total - amount;
		assert amount >= 0;
		return amount;
	}

	private static int transfer(
		IInventory src, int[] srcSlots, IItemHandler dst, int[] dstSlots, EnumFacing dir, int amount, Predicate<ItemStack> checker, boolean skipChecker
	)
	{
		int total = amount;

		for (int srcSlot : srcSlots)
		{
			ItemStack srcStack = src.getStackInSlot(srcSlot);
			if (!isEmpty(srcStack) && (skipChecker || checker.apply(srcStack)))
			{
				int transferred = insert(srcStack, amount, dst, dstSlots);
				if (transferred > 0)
				{
					amount -= transferred;
					src.setInventorySlotContents(srcSlot, decSize(srcStack, transferred));
					if (amount <= 0)
					{
						break;
					}
				}
			}
		}

		amount = total - amount;
		assert amount >= 0;
		if (amount > 0)
		{
			src.markDirty();
		}

		return amount;
	}

	private static int insert(ItemStack stack, int maxAmount, IItemHandler dst, int[] dstSlots)
	{
		int total = Math.min(maxAmount, getSize(stack));
		int remaining = total;
		assert !isEmpty(stack);

		for (int pass = 0; pass < 2; pass++)
		{
			for (int dstSlot : dstSlots)
			{
				if (dstSlot >= 0)
				{
					ItemStack dstStack = dst.getStackInSlot(dstSlot);
					if ((pass != 0 || !isEmpty(dstStack) && checkItemEqualityStrict(stack, dstStack)) && (pass != 1 || isEmpty(dstStack)))
					{
						ItemStack leftOver = dst.insertItem(dstSlot, copyWithSize(stack, remaining), false);
						int transferred = remaining - getSize(leftOver);
						remaining -= transferred;
						if (remaining <= 0)
						{
							return total;
						}
					}
				}
			}
		}

		return total - remaining;
	}

	public static void distributeDrops(TileEntity source, List<ItemStack> stacks)
	{
		ListIterator<ItemStack> it = stacks.listIterator();

		while (it.hasNext())
		{
			ItemStack stack = it.next();
			int amount = distribute(source, stack, false);
			if (amount == getSize(stack))
			{
				it.remove();
			} else
			{
				it.set(decSize(stack, amount));
			}
		}

		for (ItemStack stack : stacks)
		{
			dropAsEntity(source.getWorld(), source.getPos(), stack);
		}

		stacks.clear();
	}

	private static ItemStack getFromInventory(TileEntity source, StackUtil.AdjacentInv inventory, ItemStack stack, boolean ignoreMaxStackSize, boolean simulate)
	{
		return getFromInventory(inventory.te, inventory.dir.getOpposite(), stack, getSize(stack), ignoreMaxStackSize, inventory.getAccessor(), simulate);
	}

	public static ItemStack getFromInventory(TileEntity te, EnumFacing side, ItemStack stackDestination, int max, boolean ignoreMaxStackSize, boolean simulate)
	{
		return getFromInventory(te, side, stackDestination, max, ignoreMaxStackSize, null, simulate);
	}

	public static ItemStack getFromInventory(
		TileEntity te, EnumFacing side, ItemStack stackDestination, int max, boolean ignoreMaxStackSize, GameProfile accessor, boolean simulate
	)
	{
		if (!isEmpty(stackDestination) && !ignoreMaxStackSize)
		{
			max = Math.min(max, stackDestination.getMaxStackSize() - getSize(stackDestination));
		}

		int[] slots = getInventorySlots(te, side, false, true, accessor);
		if (slots.length == 0)
		{
			return emptyStack;
		}

		ItemStack ret = emptyStack;
		if (te instanceof IInventory)
		{
			IInventory inv = getInventory(te, accessor);
			if (inv == null)
			{
				return emptyStack;
			}

			for (int slot : slots)
			{
				if (max <= 0)
				{
					break;
				}

				ItemStack stack = inv.getStackInSlot(slot);
				if (!isEmpty(stack) && (isEmpty(stackDestination) || checkItemEqualityStrict(stack, stackDestination)))
				{
					boolean extra = isEmpty(ret);
					if (extra)
					{
						ret = copyWithSize(stack, 1);
						if (isEmpty(stackDestination))
						{
							if (!ignoreMaxStackSize)
							{
								max = Math.min(max, ret.getMaxStackSize());
							}

							stackDestination = ret;
						}
					}

					int transfer = Math.min(max, getSize(stack));
					if (!simulate)
					{
						stack = decSize(stack, transfer);
						inv.setInventorySlotContents(slot, stack);
					}

					max -= transfer;
					ret = incSize(ret, extra ? transfer - 1 : transfer);
				}
			}

			if (!simulate && !isEmpty(ret))
			{
				inv.markDirty();
			}
		} else if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = (IItemHandler) te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			if (handler == null)
			{
				return emptyStack;
			}

			for (int slot : slots)
			{
				if (max <= 0)
				{
					break;
				}

				if (!isEmpty(stackDestination))
				{
					ItemStack stack = handler.getStackInSlot(slot);
					if (isEmpty(stack) || !checkItemEqualityStrict(stack, stackDestination))
					{
						continue;
					}
				}

				ItemStack stack = handler.extractItem(slot, max, simulate);
				if (!isEmpty(stack))
				{
					boolean extra = isEmpty(ret);
					if (extra)
					{
						ret = copyWithSize(stack, 1);
						if (isEmpty(stackDestination))
						{
							if (!ignoreMaxStackSize)
							{
								max = Math.min(max, ret.getMaxStackSize());
							}

							stackDestination = ret;
						}
					} else
					{
						assert checkItemEqualityStrict(stack, ret);
					}

					int transfer = getSize(stack);
					max -= transfer;
					ret = incSize(ret, extra ? transfer - 1 : transfer);
				}
			}
		}

		return ret;
	}

	private static int putInInventory(TileEntity source, StackUtil.AdjacentInv inventory, ItemStack stackSource, boolean simulate)
	{
		return putInInventory(inventory.te, inventory.dir.getOpposite(), stackSource, inventory.getAccessor(), simulate);
	}

	public static int putInInventory(TileEntity te, EnumFacing side, ItemStack stackSource, boolean simulate)
	{
		return putInInventory(te, side, stackSource, null, simulate);
	}

	public static int putInInventory(TileEntity te, EnumFacing side, ItemStack stackSource, GameProfile accessor, boolean simulate)
	{
		if (isEmpty(stackSource))
		{
			return 0;
		}

		int[] slots = getInventorySlots(te, side, true, false, accessor);
		if (slots.length == 0)
		{
			return 0;
		}

		if (te instanceof IInventory)
		{
			IInventory inv = getInventory(te, accessor);
			if (inv == null)
			{
				return 0;
			}

			int toTransfer = getSize(stackSource);

			for (int slot : slots)
			{
				if (toTransfer <= 0)
				{
					break;
				}

				if (inv.isItemValidForSlot(slot, stackSource) && (!(inv instanceof ISidedInventory) || ((ISidedInventory) inv).canInsertItem(slot, stackSource, side)))
				{
					ItemStack stack = inv.getStackInSlot(slot);
					if (!isEmpty(stack) && checkItemEqualityStrict(stack, stackSource))
					{
						int transfer = Math.min(toTransfer, Math.min(inv.getInventoryStackLimit(), stack.getMaxStackSize()) - getSize(stack));
						if (!simulate)
						{
							inv.setInventorySlotContents(slot, incSize(stack, transfer));
						}

						toTransfer -= transfer;
					}
				}
			}

			for (int slot : slots)
			{
				if (toTransfer <= 0)
				{
					break;
				}

				if (inv.isItemValidForSlot(slot, stackSource) && (!(inv instanceof ISidedInventory) || ((ISidedInventory) inv).canInsertItem(slot, stackSource, side)))
				{
					ItemStack stack = inv.getStackInSlot(slot);
					if (isEmpty(stack))
					{
						int transfer = Math.min(toTransfer, Math.min(inv.getInventoryStackLimit(), stackSource.getMaxStackSize()));
						if (!simulate)
						{
							ItemStack dest = copyWithSize(stackSource, transfer);
							inv.setInventorySlotContents(slot, dest);
						}

						toTransfer -= transfer;
					}
				}
			}

			if (!simulate && toTransfer != getSize(stackSource))
			{
				inv.markDirty();
			}

			return getSize(stackSource) - toTransfer;
		} else
		{
			if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
			{
				return 0;
			}

			IItemHandler handler = (IItemHandler) te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			if (handler == null)
			{
				return 0;
			}

			ItemStack src = stackSource.copy();

			for (int slot : slots)
			{
				if (isEmpty(src))
				{
					break;
				}

				ItemStack stack = handler.getStackInSlot(slot);
				if (!isEmpty(stack))
				{
					ItemStack remaining = handler.insertItem(slot, src.copy(), simulate);
					if (isEmpty(remaining))
					{
						src = emptyStack;
					} else if (getSize(remaining) < getSize(src))
					{
						src = setSize(src, getSize(remaining));
					}
				}
			}

			for (int slot : slots)
			{
				if (isEmpty(src))
				{
					break;
				}

				ItemStack stack = handler.getStackInSlot(slot);
				if (isEmpty(stack))
				{
					ItemStack remaining = handler.insertItem(slot, src.copy(), simulate);
					if (isEmpty(remaining))
					{
						src = emptyStack;
					} else if (getSize(remaining) < getSize(src))
					{
						src = setSize(src, getSize(remaining));
					}
				}
			}

			return getSize(stackSource) - getSize(src);
		}
	}

	private static int[] getInventorySlots(TileEntity te, EnumFacing side, boolean checkInsert, boolean checkExtract, GameProfile accessor)
	{
		if (te instanceof IInventory)
		{
			IInventory inv = getInventory(te, accessor);
			if (inv != null && inv.getInventoryStackLimit() > 0)
			{
				ISidedInventory sidedInv;
				int[] ret;
				if (inv instanceof ISidedInventory)
				{
					sidedInv = (ISidedInventory) inv;
					ret = sidedInv.getSlotsForFace(side);
					if (ret.length == 0)
					{
						return emptySlotArray;
					}

					ret = Arrays.copyOf(ret, ret.length);
				} else
				{
					int size = inv.getSizeInventory();
					if (size <= 0)
					{
						return emptySlotArray;
					}

					sidedInv = null;
					ret = new int[size];
					int i = 0;

					while (i < ret.length)
					{
						ret[i] = i++;
					}
				}

				if (checkInsert || checkExtract)
				{
					int writeIdx = 0;

					for (int readIdx = 0; readIdx < ret.length; readIdx++)
					{
						int slot = ret[readIdx];
						ItemStack stack = inv.getStackInSlot(slot);
						if ((!checkExtract || !isEmpty(stack) && (sidedInv == null || sidedInv.canExtractItem(slot, stack, side)))
							&& (
							!checkInsert
								|| isEmpty(stack)
								|| getSize(stack) < stack.getMaxStackSize()
								&& getSize(stack) < inv.getInventoryStackLimit()
								&& (sidedInv == null || sidedInv.canInsertItem(slot, stack, side))
						))
						{
							ret[writeIdx] = slot;
							writeIdx++;
						}
					}

					if (writeIdx != ret.length)
					{
						ret = Arrays.copyOf(ret, writeIdx);
					}
				}

				return ret;
			} else
			{
				return emptySlotArray;
			}
		} else
		{
			if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
			{
				return emptySlotArray;
			}

			IItemHandler handler = (IItemHandler) te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			if (handler == null)
			{
				return emptySlotArray;
			}

			int size = handler.getSlots();
			if (size <= 0)
			{
				return emptySlotArray;
			}

			int[] ret = new int[size];
			int i = 0;

			while (i < ret.length)
			{
				ret[i] = i++;
			}

			if (checkInsert || checkExtract)
			{
				i = 0;

				for (int readIdx = 0; readIdx < ret.length; readIdx++)
				{
					int slot = ret[readIdx];
					ItemStack stack = handler.getStackInSlot(slot);
					if ((!checkExtract || !isEmpty(stack) && !isEmpty(handler.extractItem(slot, Integer.MAX_VALUE, true)))
						&& (!checkInsert || checkInsert(handler, slot, stack)))
					{
						ret[i] = slot;
						i++;
					}
				}

				if (i != ret.length)
				{
					ret = Arrays.copyOf(ret, i);
				}
			}

			return ret;
		}
	}

	private static boolean checkInsert(IItemHandler handler, int slot, ItemStack stack)
	{
		if (!isEmpty(stack) && getSize(stack) < stack.getMaxStackSize())
		{
			int testSize = Integer.MAX_VALUE;
			ItemStack result = handler.insertItem(slot, copyWithSize(stack, Integer.MAX_VALUE), true);
			return isEmpty(result) || getSize(result) < Integer.MAX_VALUE;
		} else
		{
			return true;
		}
	}

	public static boolean consumeFromPlayerInventory(EntityPlayer player, Predicate<ItemStack> request, int amount, boolean simulate)
	{
		NonNullList<ItemStack> contents = player.inventory.mainInventory;
		int pass = 0;

		label47:
		while (pass < 2)
		{
			int amountNeeded = amount;

			for (int i = 0; i < contents.size(); i++)
			{
				ItemStack stack = (ItemStack) contents.get(i);
				if (request.apply(stack))
				{
					if (player.capabilities.isCreativeMode)
					{
						return true;
					}

					int cAmount = Math.min(getSize(stack), amountNeeded);
					amountNeeded -= cAmount;
					if (pass == 1)
					{
						contents.set(i, decSize(stack, cAmount));
					}

					if (amountNeeded <= 0)
					{
						if (amountNeeded > 0)
						{
							if (pass == 1)
							{
								IC2.log
									.warn(LogCategory.General, "Inconsistent inventory transaction for player %s, request %s: %d missing", player, request, amountNeeded);
							}

							return false;
						}

						if (simulate)
						{
							return true;
						}

						pass++;
						continue label47;
					}
				}
			}

			return true;
		}

		return true;
	}

	public static Predicate<ItemStack> sameStack(final ItemStack stack)
	{
		if (isEmpty(stack))
		{
			throw new IllegalArgumentException("empty stack");
		} else
		{
			return new Predicate<ItemStack>()
			{
				public boolean apply(ItemStack input)
				{
					return StackUtil.checkItemEquality(input, stack);
				}

				@Override
				public String toString()
				{
					return "stack==" + stack;
				}
			};
		}
	}

	public static Predicate<ItemStack> sameItem(final Item item)
	{
		if (item == null)
		{
			throw new NullPointerException("null item");
		} else
		{
			return new Predicate<ItemStack>()
			{
				public boolean apply(ItemStack input)
				{
					return input.getItem() == item;
				}

				@Override
				public String toString()
				{
					return "item==" + item;
				}
			};
		}
	}

	public static Predicate<ItemStack> sameItem(Block block)
	{
		if (block == null)
		{
			throw new NullPointerException("null block");
		} else
		{
			Item item = Item.getItemFromBlock(block);
			if (item != null && (item != Items.AIR || block == Blocks.AIR))
			{
				return sameItem(item);
			} else
			{
				throw new IllegalArgumentException("block " + block + " doesn't have an associated item");
			}
		}
	}

	public static Predicate<ItemStack> oreDict(String name)
	{
		return recipeInput(Recipes.inputFactory.forOreDict(name));
	}

	public static Predicate<ItemStack> recipeInput(final IRecipeInput item)
	{
		return new Predicate<ItemStack>()
		{
			public boolean apply(ItemStack input)
			{
				return item.matches(input);
			}

			@Override
			public String toString()
			{
				return item.toString();
			}
		};
	}

	public static boolean consume(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount)
	{
		return consume0(player, hand, request, amount, false) != emptyStack;
	}

	public static ItemStack consumeAndGet(EntityPlayer player, Predicate<ItemStack> request, int amount)
	{
		return consumeAndGet(player, EnumHand.MAIN_HAND, request, amount);
	}

	public static ItemStack consumeAndGet(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount)
	{
		return consume0(player, hand, request, amount, true);
	}

	public static void consumeOrError(EntityPlayer player, EnumHand hand, int amount)
	{
		consumeOrError(player, hand, anyStack, amount);
	}

	public static void consumeOrError(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount)
	{
		if (!consume(player, hand, request, amount))
		{
			throw new IllegalStateException("consume failed");
		}
	}

	private static ItemStack consume0(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount, boolean copyOutput)
	{
		if (amount <= 0)
		{
			throw new IllegalArgumentException("negative/zero amount");
		}

		ItemStack stack = get(player, hand);
		if (isEmpty(stack))
		{
			return emptyStack;
		}

		if (!request.apply(stack))
		{
			return emptyStack;
		}

		if (player.capabilities.isCreativeMode)
		{
			return copyOutput ? copyWithSize(stack, amount) : stack;
		}

		if (getSize(stack) < amount)
		{
			return emptyStack;
		}

		ItemStack ret;
		if (getSize(stack) == amount)
		{
			ret = stack;
			clear(player, hand);
		} else
		{
			ret = copyOutput ? copyWithSize(stack, amount) : stack;
			set(player, hand, decSize(stack, amount));
		}

		return ret;
	}

	public static boolean damage(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount)
	{
		return damage0(player, hand, request, amount, false) != emptyStack;
	}

	public static void damageOrError(EntityPlayer player, EnumHand hand, int amount)
	{
		damageOrError(player, hand, anyStack, amount);
	}

	public static void damageOrError(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount)
	{
		if (!damage(player, hand, request, amount))
		{
			throw new IllegalStateException("damage failed");
		}
	}

	private static ItemStack damage0(EntityPlayer player, EnumHand hand, Predicate<ItemStack> request, int amount, boolean copyOutput)
	{
		if (amount <= 0)
		{
			throw new IllegalArgumentException("negative/zero amount");
		}

		ItemStack stack = get(player, hand);
		if (isEmpty(stack))
		{
			return emptyStack;
		}

		int maxDamage = stack.getMaxDamage();
		if (maxDamage <= 0)
		{
			return emptyStack;
		}

		if (!request.apply(stack))
		{
			return emptyStack;
		}

		if (!player.capabilities.isCreativeMode && stack.isItemStackDamageable())
		{
			stack.damageItem(amount, player);
			ItemStack ret;
			if (isEmpty(stack))
			{
				ret = stack;
				clear(player, hand);
			} else
			{
				ret = copyOutput ? copy(stack) : stack;
				set(player, hand, stack);
			}

			return ret;
		} else
		{
			return copyOutput ? copy(stack) : stack;
		}
	}

	public static ItemStack get(EntityPlayer player, EnumHand hand)
	{
		return player.getHeldItem(hand);
	}

	public static void set(EntityPlayer player, EnumHand hand, ItemStack stack)
	{
		if (isEmpty(stack))
		{
			stack = emptyStack;
		}

		InventoryPlayer inv = player.inventory;
		if (hand == EnumHand.MAIN_HAND)
		{
			inv.mainInventory.set(inv.currentItem, stack);
		} else
		{
			if (hand != EnumHand.OFF_HAND)
			{
				throw new IllegalArgumentException("invalid hand: " + hand);
			}

			inv.offHandInventory.set(0, stack);
		}
	}

	public static void clear(EntityPlayer player, EnumHand hand)
	{
		set(player, hand, emptyStack);
	}

	public static void clearEmpty(EntityPlayer player, EnumHand hand)
	{
		if (isEmpty(player, hand))
		{
			clear(player, hand);
		}
	}

	public static void dropAsEntity(World world, BlockPos pos, ItemStack stack)
	{
		if (!isEmpty(stack))
		{
			double f = 0.7;
			double dx = world.rand.nextFloat() * f + (1.0 - f) * 0.5;
			double dy = world.rand.nextFloat() * f + (1.0 - f) * 0.5;
			double dz = world.rand.nextFloat() * f + (1.0 - f) * 0.5;
			EntityItem entityItem = new EntityItem(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack.copy());
			entityItem.setDefaultPickupDelay();
			world.spawnEntity(entityItem);
		}
	}

	public static ItemStack copy(ItemStack stack)
	{
		return stack.copy();
	}

	public static ItemStack copyWithSize(ItemStack stack, int newSize)
	{
		if (isEmpty(stack))
		{
			throw new IllegalArgumentException("empty stack: " + toStringSafe(stack));
		} else
		{
			return setSize(copy(stack), newSize);
		}
	}

	public static ItemStack copyShrunk(ItemStack stack, int amount)
	{
		if (isEmpty(stack))
		{
			throw new IllegalArgumentException("empty stack: " + toStringSafe(stack));
		} else
		{
			return setSize(copy(stack), getSize(stack) - amount);
		}
	}

	public static ItemStack copyWithWildCard(ItemStack stack)
	{
		ItemStack ret = copy(stack);
		setRawMeta(ret, 32767);
		return ret;
	}

	public static Collection<ItemStack> copy(Collection<ItemStack> c)
	{
		List<ItemStack> ret = new ArrayList<>(c.size());

		for (ItemStack stack : c)
		{
			ret.add(copy(stack));
		}

		return ret;
	}

	public static NBTTagCompound getOrCreateNbtData(ItemStack stack)
	{
		NBTTagCompound ret = stack.getTagCompound();
		if (ret == null)
		{
			ret = new NBTTagCompound();
			stack.setTagCompound(ret);
		}

		return ret;
	}

	public static boolean checkItemEquality(ItemStack a, ItemStack b)
	{
		return isEmpty(a) && isEmpty(b)
			|| !isEmpty(a)
			&& !isEmpty(b)
			&& a.getItem() == b.getItem()
			&& (!a.getHasSubtypes() || a.getMetadata() == b.getMetadata())
			&& checkNbtEquality(a, b);
	}

	public static boolean checkItemEquality(ItemStack a, Item b)
	{
		return isEmpty(a) && b == null || !isEmpty(a) && b != null && a.getItem() == b;
	}

	public static boolean checkItemEqualityStrict(ItemStack a, ItemStack b)
	{
		return isEmpty(a) && isEmpty(b) || !isEmpty(a) && !isEmpty(b) && a.isItemEqual(b) && checkNbtEqualityStrict(a, b);
	}

	private static boolean checkNbtEquality(ItemStack a, ItemStack b)
	{
		return checkNbtEquality(a.getTagCompound(), b.getTagCompound());
	}

	public static boolean checkNbtEquality(NBTTagCompound a, NBTTagCompound b)
	{
		if (a == b)
		{
			return true;
		}

		Set<String> keysA = a != null ? a.getKeySet() : Collections.emptySet();
		Set<String> keysB = b != null ? b.getKeySet() : Collections.emptySet();
		Set<String> toCheck = new HashSet<>(Math.max(keysA.size(), keysB.size()));

		for (String key : keysA)
		{
			if (!ignoredNbtKeys.contains(key))
			{
				if (!keysB.contains(key))
				{
					return false;
				}

				toCheck.add(key);
			}
		}

		for (String key : keysB)
		{
			if (!ignoredNbtKeys.contains(key))
			{
				if (!keysA.contains(key))
				{
					return false;
				}

				toCheck.add(key);
			}
		}

		for (String key : toCheck)
		{
			if (!a.getTag(key).equals(b.getTag(key)))
			{
				return false;
			}
		}

		return true;
	}

	public static boolean checkNbtEqualityStrict(ItemStack a, ItemStack b)
	{
		NBTTagCompound nbtA = a.getTagCompound();
		NBTTagCompound nbtB = b.getTagCompound();
		return nbtA == nbtB ? true : nbtA != null && nbtB != null && nbtA.equals(nbtB);
	}

	public static ItemStack getPickStack(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		RayTraceResult target = new RayTraceResult(net.minecraft.util.math.RayTraceResult.Type.BLOCK, new Vec3d(pos), EnumFacing.DOWN, pos);
		ItemStack ret = state.getBlock().getPickBlock(state, target, world, pos, player);
		return isEmpty(ret) ? emptyStack : ret;
	}

	public static List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return getDrops(world, pos, state, state.getBlock(), fortune);
	}

	public static List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, Block block, int fortune)
	{
		NonNullList<ItemStack> drops = NonNullList.create();
		assert world.getBlockState(pos).getBlock() == block;
		block.getDrops(drops, world, pos, state, fortune);
		return drops;
	}

	public static List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, EntityPlayer player, int fortune, boolean silkTouch)
	{
		Block block = state.getBlock();
		if (block.isAir(state, world, pos))
		{
			return Collections.emptyList();
		}

		World rawWorld = null;
		if (silkTouch)
		{
			rawWorld = Util.getWorld(world);
			if (rawWorld == null)
			{
				throw new IllegalArgumentException("invalid world for silk touch: " + world);
			}

			if (player == null)
			{
				player = Ic2Player.get(rawWorld);
			}
		}

		ItemStack drop;
		return silkTouch && block.canSilkHarvest(rawWorld, pos, state, player) && !isEmpty(drop = getPickStack(rawWorld, pos, state, player))
			? Collections.singletonList(drop)
			: getDrops(world, pos, state, block, fortune);
	}

	public static boolean placeBlock(ItemStack stack, World world, BlockPos pos)
	{
		if (isEmpty(stack))
		{
			return false;
		}

		Item item = stack.getItem();
		if (!(item instanceof ItemBlock) && !(item instanceof ItemBlockSpecial))
		{
			return false;
		}

		int oldSize = getSize(stack);
		EntityPlayer player = Ic2Player.get(world);
		EnumHand hand = EnumHand.MAIN_HAND;
		ItemStack prev = player.getHeldItem(hand);
		player.setHeldItem(hand, stack);
		EnumActionResult result = item.onItemUse(player, world, pos, hand, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
		player.setHeldItem(hand, prev);
		stack = setSize(stack, oldSize);
		return result == EnumActionResult.SUCCESS;
	}

	public static boolean isEmpty(ItemStack stack)
	{
		return stack == emptyStack || stack == null || stack.getItem() == null || stack.getCount() <= 0;
	}

	public static boolean isEmpty(EntityPlayer player, EnumHand hand)
	{
		return isEmpty(player.getHeldItem(hand));
	}

	public static int getSize(ItemStack stack)
	{
		return isEmpty(stack) ? 0 : stack.getCount();
	}

	public static ItemStack setSize(ItemStack stack, int size)
	{
		if (size <= 0)
		{
			return emptyStack;
		}

		stack.setCount(size);
		return stack;
	}

	public static ItemStack incSize(ItemStack stack)
	{
		return incSize(stack, 1);
	}

	public static ItemStack incSize(ItemStack stack, int amount)
	{
		return setSize(stack, getSize(stack) + amount);
	}

	public static ItemStack decSize(ItemStack stack)
	{
		return decSize(stack, 1);
	}

	public static ItemStack decSize(ItemStack stack, int amount)
	{
		return incSize(stack, -amount);
	}

	public static boolean check2(Iterable<List<ItemStack>> list)
	{
		for (List<ItemStack> list2 : list)
		{
			if (!check(list2))
			{
				return false;
			}
		}

		return true;
	}

	public static boolean check(ItemStack[] array)
	{
		return check(Arrays.asList(array));
	}

	public static boolean check(Iterable<ItemStack> list)
	{
		for (ItemStack stack : list)
		{
			if (!check(stack))
			{
				return false;
			}
		}

		return true;
	}

	public static boolean check(ItemStack stack)
	{
		return stack.getItem() != null;
	}

	public static String toStringSafe2(Iterable<List<ItemStack>> list)
	{
		StringBuilder ret = new StringBuilder("[");

		for (List<ItemStack> list2 : list)
		{
			if (ret.length() > 1)
			{
				ret.append(", ");
			}

			ret.append(toStringSafe(list2));
		}

		return ret.append(']').toString();
	}

	public static String toStringSafe(ItemStack[] array)
	{
		return toStringSafe(Arrays.asList(array));
	}

	public static String toStringSafe(Iterable<ItemStack> list)
	{
		StringBuilder ret = new StringBuilder("[");

		for (ItemStack stack : list)
		{
			if (ret.length() > 1)
			{
				ret.append(", ");
			}

			ret.append(toStringSafe(stack));
		}

		return ret.append(']').toString();
	}

	public static String toStringSafe(ItemStack stack)
	{
		if (stack == null)
		{
			return "(null)";
		} else
		{
			return stack.getItem() == null ? getSize(stack) + "x(null)@(unknown)" : stack.toString();
		}
	}

	public static boolean storeInventoryItem(ItemStack stack, EntityPlayer player, boolean simulate)
	{
		if (!simulate)
		{
			return player.inventory.addItemStackToInventory(stack);
		}

		int sizeLeft = getSize(stack);
		int maxStackSize = Math.min(player.inventory.getInventoryStackLimit(), stack.getMaxStackSize());

		for (int i = 0; i < player.inventory.mainInventory.size() && sizeLeft > 0; i++)
		{
			ItemStack invStack = (ItemStack) player.inventory.mainInventory.get(i);
			if (isEmpty(invStack))
			{
				sizeLeft -= maxStackSize;
			} else if (checkItemEqualityStrict(stack, invStack) && getSize(invStack) < maxStackSize)
			{
				sizeLeft -= maxStackSize - getSize(invStack);
			}
		}

		return sizeLeft <= 0;
	}

	public static int getRawMeta(ItemStack stack)
	{
		return Items.DYE.getDamage(stack);
	}

	public static void setRawMeta(ItemStack stack, int meta)
	{
		if (meta < 0)
		{
			throw new IllegalArgumentException("negative meta");
		}

		Items.DYE.setDamage(stack, meta);
	}

	public static TIntSet getSlotsFromInv(IInventory inv)
	{
		TIntSet set = new TIntHashSet();

		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			set.add(i);
		}

		return set;
	}

	public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(IInventory craftMatrix)
	{
		return balanceStacks(craftMatrix, Collections.emptySet());
	}

	public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(IInventory craftMatrix, ItemStack sourceItemStack)
	{
		return balanceStacks(craftMatrix, Collections.singleton(sourceItemStack));
	}

	public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(final IInventory inv, Collection<ItemStack> additionalItems)
	{
		return balanceStacks(inv, new Predicate<Tuple.T2<ItemStack, Integer>>()
		{
			public boolean apply(Tuple.T2<ItemStack, Integer> input)
			{
				return !StackUtil.isEmpty(inv.getStackInSlot(input.b));
			}
		}, getSlotsFromInv(inv), additionalItems);
	}

	public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(IInventory inv, Predicate<Tuple.T2<ItemStack, Integer>> canInsert)
	{
		return balanceStacks(inv, canInsert, getSlotsFromInv(inv), Collections.emptySet());
	}

	public static Tuple.T2<List<ItemStack>, ? extends TIntCollection> balanceStacks(
		IInventory inv, Predicate<Tuple.T2<ItemStack, Integer>> canInsert, TIntSet originalAvailableSlots, Collection<ItemStack> additionalStacksOriginal
	)
	{
		List<ItemStack> additionalStacks = new LinkedList<>(additionalStacksOriginal);
		TIntSet availableSlots = new TIntHashSet(originalAvailableSlots);
		List<ItemStack> leftOvers = new ArrayList<>();

		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			if (availableSlots.contains(i))
			{
				ItemStack stack = inv.getStackInSlot(i);
				if (!isEmpty(stack))
				{
					int amount = 0;
					ListIterator<ItemStack> iter = additionalStacks.listIterator();

					while (iter.hasNext())
					{
						ItemStack currentStack = iter.next();
						if (checkItemEqualityStrict(currentStack, stack))
						{
							iter.remove();
							amount += getSize(currentStack);
						}
					}

					amount = distributeStackToSlots(inv, stack, availableSlots, canInsert, amount);

					while (amount > 0)
					{
						int size = Math.min(stack.getMaxStackSize(), amount);
						amount -= size;
						leftOvers.add(copyWithSize(stack, size));
					}
				}
			}
		}

		for (ItemStack stack : additionalStacks)
		{
			int amount = distributeStackToSlots(inv, stack, availableSlots, canInsert, getSize(stack));
			if (amount > 0)
			{
				leftOvers.add(copyWithSize(stack, amount));
			}
		}

		originalAvailableSlots.removeAll(availableSlots);
		return new Tuple.T2<>(leftOvers, originalAvailableSlots);
	}

	private static int distributeStackToSlots(
		final IInventory inv, ItemStack stack, TIntSet availableSlots, Predicate<Tuple.T2<ItemStack, Integer>> canInsert, int amount
	)
	{
		TIntList currentWorkingSet = new TIntArrayList();
		TIntIterator iter = availableSlots.iterator();

		while (iter.hasNext())
		{
			int currentSlot = iter.next();
			ItemStack currentStack = inv.getStackInSlot(currentSlot);
			if ((checkItemEqualityStrict(stack, currentStack) || isEmpty(currentStack)) && canInsert.apply(new Tuple.T2<>(stack, currentSlot)))
			{
				amount += getSize(currentStack);
				currentWorkingSet.add(currentSlot);
				iter.remove();
			}
		}

		currentWorkingSet.sort();
		int maxStackSize = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
		int slotsLeft = currentWorkingSet.size();
		TIntIterator iterx = currentWorkingSet.iterator();

		while (iterx.hasNext() && amount > 0)
		{
			int currentSlot = iterx.next();
			int itemsToPut = amount / slotsLeft;
			if (amount % slotsLeft > 0)
			{
				itemsToPut++;
			}

			itemsToPut = Math.min(itemsToPut, maxStackSize);
			inv.setInventorySlotContents(currentSlot, copyWithSize(stack, itemsToPut));
			amount -= itemsToPut;
			slotsLeft--;
			iterx.remove();
		}

		if (!currentWorkingSet.isEmpty())
		{
			assert amount <= 0;
			currentWorkingSet.forEach(new TIntProcedure()
			{
				public boolean execute(int currentSlot)
				{
					inv.setInventorySlotContents(currentSlot, StackUtil.emptyStack);
					return true;
				}
			});
		}

		assert amount <= 0 || slotsLeft == 0;
		return amount;
	}

	public static ItemStack setImmutableSize(ItemStack stack, int size)
	{
		if (getSize(stack) != size)
		{
			stack = copyWithSize(stack, size);
		}

		return stack;
	}

	public static boolean matchesNBT(NBTTagCompound subject, NBTTagCompound target)
	{
		if (subject == null)
		{
			return target == null || target.hasNoTags();
		}

		if (target == null)
		{
			return true;
		}

		for (String key : target.getKeySet())
		{
			NBTBase targetNBT = target.getTag(key);
			if (!subject.hasKey(key) || targetNBT.getId() != subject.getTagId(key))
			{
				return false;
			}

			NBTBase subjectNBT = subject.getTag(key);
			if (!targetNBT.equals(subjectNBT))
			{
				return false;
			}
		}

		return true;
	}

	public static ItemStack wrapEmpty(ItemStack stack)
	{
		return stack == null ? emptyStack : stack;
	}

	public static class AdjacentInv
	{
		public final TileEntity te;
		public final EnumFacing dir;

		AdjacentInv(TileEntity te, EnumFacing dir)
		{
			this.te = te;
			this.dir = dir;
		}

		public GameProfile getAccessor()
		{
			return null;
		}
	}

	public static class PersonalAdjacentInv extends StackUtil.AdjacentInv
	{
		public final GameProfile accessor;

		PersonalAdjacentInv(TileEntity te, EnumFacing dir, GameProfile accessor)
		{
			super(te, dir);
			this.accessor = accessor;
		}

		@Override
		public GameProfile getAccessor()
		{
			return this.accessor;
		}
	}
}
