package ic2.core.util;

import com.mojang.authlib.GameProfile;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.item.EnvItemHandler;
import ic2.core.ref.Ic2ItemTags;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

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
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class StackUtil
{
	private static final List<TagKey<Item>> oreTags = List.of(
		Ic2ItemTags.ORES,
		ItemTags.COAL_ORES,
		ItemTags.COPPER_ORES,
		ItemTags.DIAMOND_ORES,
		ItemTags.GOLD_ORES,
		ItemTags.IRON_ORES,
		ItemTags.EMERALD_ORES,
		ItemTags.LAPIS_ORES,
		ItemTags.REDSTONE_ORES
	);
	static final Set<String> ignoredNbtKeys = new HashSet<>(Arrays.asList("damage", "charge", "energy", "advDmg"));
	public static final Predicate<ItemStack> anyStack = stack -> true;
	public static final ItemStack emptyStack = ItemStack.EMPTY;
	private static final int[] emptySlotArray = new int[0];
	public static final EnvItemHandler ENV = IC2.envProxy.createItemHandler();

	public static boolean isEmpty(ItemStack stack)
	{
		return stack == emptyStack || stack == null || stack.getItem() == null || stack.getCount() <= 0;
	}

	public static boolean isEmpty(Player player, InteractionHand hand)
	{
		return isEmpty(player.getItemInHand(hand));
	}

	public static int getSize(ItemStack stack)
	{
		return isEmpty(stack) ? 0 : stack.getCount();
	}

	public static boolean isOreStack(ItemStack stack)
	{
		for (TagKey<Item> oreTag : oreTags)
		{
			if (stack.is(oreTag))
			{
				return true;
			}
		}

		return false;
	}

	public static ItemStack setSize(ItemStack stack, int size)
	{
		stack.setCount(size);
		return size <= 0 ? emptyStack : stack;
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

	public static ItemStack wrapEmpty(ItemStack stack)
	{
		return stack == null ? emptyStack : stack;
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

	public static Collection<ItemStack> copy(Collection<ItemStack> c)
	{
		List<ItemStack> ret = new ArrayList<>(c.size());

		for (ItemStack stack : c)
		{
			ret.add(copy(stack));
		}

		return ret;
	}

	public static CompoundTag getOrCreateNbtData(ItemStack stack)
	{
		CompoundTag ret = stack.getTag();
		if (ret == null)
		{
			ret = new CompoundTag();
			stack.setTag(ret);
		}

		return ret;
	}

	public static boolean checkItemEquality(ItemStack a, ItemStack b)
	{
		return isEmpty(a) && isEmpty(b) || !isEmpty(a) && !isEmpty(b) && a.getItem() == b.getItem() && checkNbtEquality(a, b);
	}

	public static boolean checkItemEquality(ItemStack a, Item b)
	{
		return isEmpty(a) && b == null || !isEmpty(a) && b != null && a.getItem() == b;
	}

	public static boolean checkItemEqualityStrict(ItemStack a, ItemStack b)
	{
		return isEmpty(a) && isEmpty(b) || !isEmpty(a) && !isEmpty(b) && ItemStack.isSameItem(a, b) && checkNbtEqualityStrict(a, b);
	}

	private static boolean checkNbtEquality(ItemStack a, ItemStack b)
	{
		return checkNbtEquality(a.getTag(), b.getTag());
	}

	public static boolean checkNbtEquality(CompoundTag a, CompoundTag b)
	{
		if (a == b)
		{
			return true;
		}

		Set<String> keysA = a != null ? a.getAllKeys() : Collections.emptySet();
		Set<String> keysB = b != null ? b.getAllKeys() : Collections.emptySet();
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
			if (!a.get(key).equals(b.get(key)))
			{
				return false;
			}
		}

		return true;
	}

	public static boolean checkNbtEqualityStrict(ItemStack a, ItemStack b)
	{
		CompoundTag nbtA = a.getTag();
		CompoundTag nbtB = b.getTag();
		return nbtA == nbtB ? true : nbtA != null && nbtB != null && nbtA.equals(nbtB);
	}

	public static Predicate<ItemStack> sameStack(ItemStack stack)
	{
		if (isEmpty(stack))
		{
			throw new IllegalArgumentException("empty stack");
		} else
		{
			return new Predicate<ItemStack>()
			{
				public boolean test(ItemStack input)
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

	public static Predicate<ItemStack> sameItem(Item item)
	{
		if (item == null)
		{
			throw new NullPointerException("null item");
		} else
		{
			return new Predicate<ItemStack>()
			{
				public boolean test(ItemStack input)
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

	public static Predicate<ItemStack> sameItem(ItemLike block)
	{
		if (block == null)
		{
			throw new NullPointerException("null block");
		} else
		{
			Item item = block.asItem();
			if (item != null && (item != Items.AIR || block == Blocks.AIR))
			{
				return sameItem(item);
			} else
			{
				throw new IllegalArgumentException("block " + block + " doesn't have an associated item");
			}
		}
	}

	public static Predicate<ItemStack> recipeInput(IRecipeInput item)
	{
		return new Predicate<ItemStack>()
		{
			public boolean test(ItemStack input)
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

	public static boolean consume(Player player, InteractionHand hand, Predicate<ItemStack> request, int amount)
	{
		return consume0(player, hand, request, amount, false) != emptyStack;
	}

	public static ItemStack consumeAndGet(Player player, Predicate<ItemStack> request, int amount)
	{
		return consumeAndGet(player, InteractionHand.MAIN_HAND, request, amount);
	}

	public static ItemStack consumeAndGet(Player player, InteractionHand hand, Predicate<ItemStack> request, int amount)
	{
		return consume0(player, hand, request, amount, true);
	}

	public static void consumeOrError(Player player, InteractionHand hand, int amount)
	{
		consumeOrError(player, hand, anyStack, amount);
	}

	public static void consumeOrError(Player player, InteractionHand hand, Predicate<ItemStack> request, int amount)
	{
		if (!consume(player, hand, request, amount))
		{
			throw new IllegalStateException("consume failed");
		}
	}

	private static ItemStack consume0(Player player, InteractionHand hand, Predicate<ItemStack> request, int amount, boolean copyOutput)
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

		if (!request.test(stack))
		{
			return emptyStack;
		}

		if (player.getAbilities().instabuild)
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

	public static ItemStack consumeFromPlayerInventoryAndGet(Player player, Predicate<ItemStack> request, int amount)
	{
		return consumeFromPlayerInventoryAndGet(player, request, amount, false);
	}

	public static ItemStack consumeFromPlayerInventoryAndGet(Player player, Predicate<ItemStack> request, int amount, boolean excludeSelectedSlot)
	{
		Inventory inventory = player.getInventory();
		NonNullList<ItemStack> contents = inventory.items;
		int amountNeeded = amount;

		for (int i = 0; i < contents.size(); i++)
		{
			ItemStack stack = (ItemStack) contents.get(i);
			if (request.test(stack))
			{
				if (player.getAbilities().instabuild)
				{
					return copyWithSize(stack, amount);
				}

				if (!excludeSelectedSlot || i != inventory.selected)
				{
					int cAmount = Math.min(getSize(stack), amountNeeded);
					amountNeeded -= cAmount;
					if (amountNeeded > 0)
					{
						IC2.log.warn(LogCategory.General, "Inconsistent inventory transaction for player %s, request %s: %d missing", player, request, amountNeeded);
						return emptyStack;
					} else
					{
						ItemStack ret = copyWithSize(stack, amount);
						contents.set(i, decSize(stack, cAmount));
						return ret;
					}
				}
			}
		}

		return emptyStack;
	}

	public static boolean addToPlayerInventory(Player player, ItemStack stack)
	{
		return player.getInventory().add(stack);
	}

	public static boolean consumeFromPlayerInventory(Player player, Predicate<ItemStack> request, int amount, boolean simulate)
	{
		NonNullList<ItemStack> contents = player.getInventory().items;
		int pass = 0;

		label47:
		while (pass < 2)
		{
			int amountNeeded = amount;

			for (int i = 0; i < contents.size(); i++)
			{
				ItemStack stack = (ItemStack) contents.get(i);
				if (request.test(stack))
				{
					if (player.getAbilities().instabuild)
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

			return false;
		}

		return true;
	}

	public static boolean damage(Player player, InteractionHand hand, Predicate<ItemStack> request, int amount)
	{
		return damage0(player, hand, request, amount, false) != emptyStack;
	}

	public static void damageOrError(Player player, InteractionHand hand, int amount)
	{
		damageOrError(player, hand, anyStack, amount);
	}

	public static void damageOrError(Player player, InteractionHand hand, Predicate<ItemStack> request, int amount)
	{
		if (!damage(player, hand, request, amount))
		{
			throw new IllegalStateException("damage failed");
		}
	}

	private static ItemStack damage0(Player player, InteractionHand hand, Predicate<ItemStack> request, int amount, boolean copyOutput)
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

		if (!request.test(stack))
		{
			return emptyStack;
		}

		if (!player.getAbilities().instabuild && stack.isDamageableItem())
		{
			stack.hurtAndBreak(amount, player, p -> p.broadcastBreakEvent(hand));
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

	public static ItemStack get(Player player, InteractionHand hand)
	{
		return player.getItemInHand(hand);
	}

	public static void set(Player player, InteractionHand hand, ItemStack stack)
	{
		if (isEmpty(stack))
		{
			stack = emptyStack;
		}

		Inventory inv = player.getInventory();
		if (hand == InteractionHand.MAIN_HAND)
		{
			inv.items.set(inv.selected, stack);
		} else
		{
			if (hand != InteractionHand.OFF_HAND)
			{
				throw new IllegalArgumentException("invalid hand: " + hand);
			}

			inv.offhand.set(0, stack);
		}
	}

	public static void clear(Player player, InteractionHand hand)
	{
		set(player, hand, emptyStack);
	}

	public static void clearEmpty(Player player, InteractionHand hand)
	{
		if (isEmpty(player, hand))
		{
			clear(player, hand);
		}
	}

	public static boolean storeInventoryItem(ItemStack stack, Player player, boolean simulate)
	{
		if (!simulate)
		{
			return player.getInventory().add(stack);
		}

		int sizeLeft = getSize(stack);
		int maxStackSize = Math.min(player.getInventory().getMaxStackSize(), stack.getMaxStackSize());

		for (int i = 0; i < player.getInventory().items.size() && sizeLeft > 0; i++)
		{
			ItemStack invStack = (ItemStack) player.getInventory().items.get(i);
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

	public static void dropAsEntity(Level world, BlockPos pos, ItemStack stack)
	{
		if (!isEmpty(stack))
		{
			double f = 0.7;
			double dx = world.random.nextFloat() * f + (1.0 - f) * 0.5;
			double dy = world.random.nextFloat() * f + (1.0 - f) * 0.5;
			double dz = world.random.nextFloat() * f + (1.0 - f) * 0.5;
			ItemEntity entityItem = new ItemEntity(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack.copy());
			entityItem.setDefaultPickUpDelay();
			world.addFreshEntity(entityItem);
		}
	}

	public static ItemStack setImmutableSize(ItemStack stack, int size)
	{
		if (getSize(stack) != size)
		{
			stack = copyWithSize(stack, size);
		}

		return stack;
	}

	public static int fetch(BlockEntity source, ItemStack stack, boolean simulate)
	{
		return ENV.fetch(source, stack, simulate);
	}

	public static int distribute(BlockEntity source, ItemStack stack, boolean simulate)
	{
		return ENV.distribute(source, stack, simulate);
	}

	public static void distributeDrops(BlockEntity source, List<ItemStack> stacks)
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
			dropAsEntity(source.getLevel(), source.getBlockPos(), stack);
		}

		stacks.clear();
	}

	public static boolean placeBlock(ItemStack stack, Level world, BlockPos pos)
	{
		if (isEmpty(stack))
		{
			return false;
		}

		Item item = stack.getItem();
		if (!(item instanceof BlockItem))
		{
			return false;
		}

		int oldSize = getSize(stack);
		Player player = Ic2Player.get(world);
		InteractionHand hand = InteractionHand.MAIN_HAND;
		ItemStack prev = player.getItemInHand(hand);
		player.setItemInHand(hand, stack);
		InteractionResult result = item.useOn(
			new UseOnContext(player, hand, new BlockHitResult(Vec3.atLowerCornerOf(pos).add(0.5, 1.0, 0.5), Direction.DOWN, pos, false))
		);
		player.setItemInHand(hand, prev);
		stack = setSize(stack, oldSize);
		return result == InteractionResult.SUCCESS || result == InteractionResult.CONSUME;
	}

	public static int putInInventory(BlockEntity te, Direction side, ItemStack stackSource, boolean simulate)
	{
		return putInInventory(te, side, stackSource, null, simulate);
	}

	public static int putInInventory(BlockEntity te, Direction side, ItemStack stack, GameProfile accessor, boolean simulate)
	{
		return ENV.deposit(te, side, stack, accessor, simulate);
	}

	public static ItemStack getPickStack(Level world, BlockPos pos, BlockState state, Player player)
	{
		ItemStack ret = state.getBlock().getCloneItemStack(world, pos, state);
		return isEmpty(ret) ? emptyStack : ret;
	}

	public static List<ItemStack> getDrops(BlockGetter world, BlockPos pos, BlockState state, int fortune)
	{
		return getDrops(world, pos, state, state.getBlock(), fortune);
	}

	public static List<ItemStack> getDrops(BlockGetter world, BlockPos pos, BlockState state, Block block, int fortune)
	{
		return getDrops(world, pos, state, null, fortune, false);
	}

	public static List<ItemStack> getDrops(BlockGetter world, BlockPos pos, BlockState state, Player player, int fortune, boolean silkTouch)
	{
		if (state.isAir())
		{
			return Collections.emptyList();
		}

		ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
		if (silkTouch)
		{
			EnchantmentHelper.setEnchantments(Collections.singletonMap(Enchantments.SILK_TOUCH, fortune), stack);
		} else if (fortune > 0)
		{
			EnchantmentHelper.setEnchantments(Collections.singletonMap(Enchantments.BLOCK_FORTUNE, fortune), stack);
		}

		return Block.getDrops(state, (ServerLevel) world, pos, world.getBlockEntity(pos), player, stack);
	}

	public static IntSet getSlotsFromInv(Container inv)
	{
		IntSet set = new IntOpenHashSet();

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			set.add(i);
		}

		return set;
	}

	public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(Container craftMatrix)
	{
		return balanceStacks(craftMatrix, Collections.emptySet());
	}

	public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(Container craftMatrix, ItemStack sourceItemStack)
	{
		return balanceStacks(craftMatrix, Collections.singleton(sourceItemStack));
	}

	public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(Container inv, Collection<ItemStack> additionalItems)
	{
		return balanceStacks(inv, new Predicate<Tuple.T2<ItemStack, Integer>>()
		{
			public boolean test(Tuple.T2<ItemStack, Integer> input)
			{
				return !StackUtil.isEmpty(inv.getItem(input.b));
			}
		}, getSlotsFromInv(inv), additionalItems);
	}

	public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(Container inv, Predicate<Tuple.T2<ItemStack, Integer>> canInsert)
	{
		return balanceStacks(inv, canInsert, getSlotsFromInv(inv), Collections.emptySet());
	}

	public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(
		Container inv, Predicate<Tuple.T2<ItemStack, Integer>> canInsert, IntSet originalAvailableSlots, Collection<ItemStack> additionalStacksOriginal
	)
	{
		List<ItemStack> additionalStacks = new LinkedList<>(additionalStacksOriginal);
		IntSet availableSlots = new IntOpenHashSet(originalAvailableSlots);
		List<ItemStack> leftOvers = new ArrayList<>();

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			if (availableSlots.contains(i))
			{
				ItemStack stack = inv.getItem(i);
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
		Container inv, ItemStack stack, IntSet availableSlots, Predicate<Tuple.T2<ItemStack, Integer>> canInsert, int amount
	)
	{
		IntList currentWorkingSet = new IntArrayList();
		IntIterator iter = availableSlots.iterator();

		while (iter.hasNext())
		{
			int currentSlot = iter.nextInt();
			ItemStack currentStack = inv.getItem(currentSlot);
			if ((checkItemEqualityStrict(stack, currentStack) || isEmpty(currentStack)) && canInsert.test(new Tuple.T2<>(stack, currentSlot)))
			{
				amount += getSize(currentStack);
				currentWorkingSet.add(currentSlot);
				iter.remove();
			}
		}

		currentWorkingSet.sort(Comparator.naturalOrder());
		int maxStackSize = Math.min(stack.getMaxStackSize(), inv.getMaxStackSize());
		int slotsLeft = currentWorkingSet.size();
		IntIterator iterx = currentWorkingSet.iterator();

		while (iterx.hasNext() && amount > 0)
		{
			int currentSlot = iterx.nextInt();
			int itemsToPut = amount / slotsLeft;
			if (amount % slotsLeft > 0)
			{
				itemsToPut++;
			}

			itemsToPut = Math.min(itemsToPut, maxStackSize);
			inv.setItem(currentSlot, copyWithSize(stack, itemsToPut));
			amount -= itemsToPut;
			slotsLeft--;
			iterx.remove();
		}

		if (!currentWorkingSet.isEmpty())
		{
			assert amount <= 0;
			IntListIterator var14 = currentWorkingSet.iterator();

			while (var14.hasNext())
			{
				int currentSlot = (Integer) var14.next();
				inv.setItem(currentSlot, emptyStack);
			}
		}

		assert amount <= 0 || slotsLeft == 0;
		return amount;
	}
}
