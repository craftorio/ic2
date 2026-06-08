package ic2.core.util;

import ic2.core.IC2;
import ic2.core.Ic2Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public final class Util
{
	public static final Direction[] ALL_DIRS = Direction.values();
	public static final Direction[] HORIZONTAL_DIRS = Arrays.copyOfRange(ALL_DIRS, 2, 6);
	public static final Set<Direction> noFacings = Collections.emptySet();
	public static final Set<Direction> onlyNorth = Collections.unmodifiableSet(EnumSet.of(Direction.NORTH));
	public static final Set<Direction> horizontalFacings = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(HORIZONTAL_DIRS)));
	public static final Set<Direction> verticalFacings = Collections.unmodifiableSet(EnumSet.of(Direction.DOWN, Direction.UP));
	public static final Set<Direction> downSideFacings = Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(Direction.UP)));
	public static final Set<Direction> allFacings = Collections.unmodifiableSet(EnumSet.allOf(Direction.class));
	public static final InteractionHand[] HANDS = InteractionHand.values();
	private static final boolean inDev = System.getProperty("INDEV") != null;
	private static final boolean includeWorldHash = System.getProperty("ic2.debug.includeworldhash") != null;
	private static final Map<Class<?>, Boolean> checkedClasses = new IdentityHashMap<>();

	public static int roundToNegInf(float x)
	{
		int ret = (int) x;
		if (ret > x)
		{
			ret--;
		}

		return ret;
	}

	public static int roundToNegInf(double x)
	{
		int ret = (int) x;
		if (ret > x)
		{
			ret--;
		}

		return ret;
	}

	public static int saturatedCast(double x)
	{
		if (x > 2.147483647E9)
		{
			return Integer.MAX_VALUE;
		} else
		{
			return x < -2.1474836E9F ? Integer.MIN_VALUE : (int) x;
		}
	}

	public static int limit(int value, int min, int max)
	{
		if (value <= min)
		{
			return min;
		} else
		{
			return value >= max ? max : value;
		}
	}

	public static float limit(float value, float min, float max)
	{
		if (Float.isNaN(value) || value <= min)
		{
			return min;
		} else
		{
			return value >= max ? max : value;
		}
	}

	public static double limit(double value, double min, double max)
	{
		if (Double.isNaN(value) || value <= min)
		{
			return min;
		} else
		{
			return value >= max ? max : value;
		}
	}

	public static double map(double value, double srcMax, double dstMax)
	{
		if (value < 0.0 || Double.isNaN(value))
		{
			value = 0.0;
		}

		if (value > srcMax)
		{
			value = srcMax;
		}

		return value / srcMax * dstMax;
	}

	public static double lerp(double start, double end, double fraction)
	{
		assert fraction >= 0.0 && fraction <= 1.0;
		return start + (end - start) * fraction;
	}

	public static float lerp(float start, float end, float fraction)
	{
		assert fraction >= 0.0F && fraction <= 1.0F;
		return start + (end - start) * fraction;
	}

	public static int square(int x)
	{
		return x * x;
	}

	public static float square(float x)
	{
		return x * x;
	}

	public static double square(double x)
	{
		return x * x;
	}

	public static boolean isSimilar(float a, float b)
	{
		return Math.abs(a - b) < 1.0E-5F;
	}

	public static boolean isSimilar(double a, double b)
	{
		return Math.abs(a - b) < 1.0E-5;
	}

	public static int countInArray(Object[] oa, Class<?>... clsz)
	{
		int ret = 0;

		for (Object o : oa)
		{
			for (Class<?> cls : clsz)
			{
				if (cls.isAssignableFrom(o.getClass()))
				{
					ret++;
				}
			}
		}

		return ret;
	}

	public static int countInArray(Object[] oa, Class<?> cls)
	{
		int ret = 0;

		for (Object o : oa)
		{
			if (cls.isAssignableFrom(o.getClass()))
			{
				ret++;
			}
		}

		return ret;
	}

	public static boolean checkInterfaces(Class<?> cls)
	{
		Boolean cached = checkedClasses.get(cls);
		if (cached != null)
		{
			return cached;
		}

		Set<Class<?>> interfaces = Collections.newSetFromMap(new IdentityHashMap<>());
		Class<?> c = cls;

		do
		{
			Collections.addAll(interfaces, c.getInterfaces());

			c = c.getSuperclass();
		} while (c != null);

		boolean result = true;

		for (Class<?> iface : interfaces)
		{
			for (Method method : iface.getMethods())
			{
				boolean found = false;
				c = cls;

				do
				{
					try
					{
						Method match = c.getDeclaredMethod(method.getName(), method.getParameterTypes());
						if (method.getReturnType().isAssignableFrom(match.getReturnType()))
						{
							found = true;
							break;
						}
					} catch (NoSuchMethodException var13)
					{
					}

					c = c.getSuperclass();
				} while (c != null);

				if (!found)
				{
					IC2.log.info(LogCategory.General, "Can't find method %s.%s in %s.", method.getDeclaringClass().getName(), method.getName(), cls.getName());
					result = false;
				}
			}
		}

		checkedClasses.put(cls, result);
		return result;
	}

	public static boolean inDev()
	{
		return inDev;
	}

	public static boolean hasAssertions()
	{
		boolean ret = false;
		assert ret = true;
		return ret;
	}

	public static boolean isCallingFromIc2()
	{
		return isCallingFromIc2(1);
	}

	public static boolean isCallingFromIc2(int extraFrames)
	{
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		int idx = 2 + extraFrames;
		return st.length <= idx || st[idx].getClassName().startsWith("ic2.");
	}

	public static Block getBlock(String name)
	{
		if (name == null)
		{
			throw new NullPointerException("null name");
		} else
		{
			// TODO
			return getBlock(ResourceLocation.withDefaultNamespace(name));
		}
	}

	public static Block getBlock(ResourceLocation loc)
	{
		Block ret = BuiltInRegistries.BLOCK.get(loc);
		if (ret != Blocks.AIR)
		{
			return ret;
		} else
		{
			return loc.getNamespace().equals("minecraft") && loc.getPath().equals("air") ? ret : null;
		}
	}

	public static boolean canShear(BlockState state)
	{
		return state.is(BlockTags.LEAVES)
			|| state.is(Blocks.COBWEB)
			|| state.is(Blocks.GRASS)
			|| state.is(Blocks.FERN)
			|| state.is(Blocks.DEAD_BUSH)
			|| state.is(Blocks.HANGING_ROOTS)
			|| state.is(Blocks.VINE)
			|| state.is(Blocks.TRIPWIRE)
			|| state.is(BlockTags.WOOL);
	}

	public static Vector3 getEyePosition(Entity entity)
	{
		return new Vector3(entity.getX(), entity.getEyeY(), entity.getZ());
	}

	public static ResourceLocation getName(Block block)
	{
		return ForgeRegistries.BLOCKS.getKey(block);
	}

	public static Item getItem(String name)
	{
		if (name == null)
		{
			throw new NullPointerException("null name");
		} else
		{
			return getItem(ResourceLocation.withDefaultNamespace(name));
		}
	}

	public static Item getItem(ResourceLocation loc)
	{
		return BuiltInRegistries.ITEM.get(loc);
	}

	public static Vector3 getLook(Entity entity)
	{
		return new Vector3(entity.getLookAngle());
	}

	public static ResourceLocation getName(Item item)
	{
		return BuiltInRegistries.ITEM.getKey(item);
	}

	public static Fluid getFluid(ResourceLocation loc)
	{
		return BuiltInRegistries.FLUID.get(loc);
	}

	public static ResourceLocation getName(Fluid fluid)
	{
		return BuiltInRegistries.FLUID.getKey(fluid);
	}

	public static ResourceLocation getDimId(Level world)
	{
		return world.dimension().location();
	}

	public static String toString(BlockEntity te)
	{
		return te == null ? "null" : toString(te, te.getLevel(), te.getBlockPos());
	}

	public static String toString(Object o, BlockGetter world, BlockPos pos)
	{
		return toString(o, world, pos.getX(), pos.getY(), pos.getZ());
	}

	public static String toString(Object o, BlockGetter world, int x, int y, int z)
	{
		StringBuilder ret = new StringBuilder(64);
		if (o == null)
		{
			ret.append("null");
		} else
		{
			ret.append(o.getClass().getName());
			ret.append('@');
			ret.append(Integer.toHexString(System.identityHashCode(o)));
		}

		ret.append(" (");
		ret.append(formatPosition(world, x, y, z));
		ret.append(")");
		return ret.toString();
	}

	public static String toCamel(String snakeStr)
	{
		String[] snakeStrSplit = snakeStr.split("_");
		StringBuilder retBuilder = new StringBuilder();

		for (String str : snakeStrSplit)
		{
			if (retBuilder.isEmpty())
			{
				retBuilder.append(str);
			} else
			{
				retBuilder.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
			}
		}

		return retBuilder.toString();
	}

	public static String formatPosition(BlockEntity te)
	{
		return te != null ? formatPosition(te.getLevel(), te.getBlockPos()) : "(null)";
	}

	public static String formatPosition(BlockGetter world, BlockPos pos)
	{
		return formatPosition(world, pos.getX(), pos.getY(), pos.getZ());
	}

	public static String formatPosition(BlockGetter world, int x, int y, int z)
	{
		ResourceLocation dimId;
		if (world instanceof Level)
		{
			dimId = getDimId((Level) world);
		} else
		{
			dimId = null;
		}

		return !includeWorldHash ? formatPosition(dimId, x, y, z) : String.format("dim %s (@%x): %d/%d/%d", dimId, System.identityHashCode(world), x, y, z);
	}

	public static String formatPosition(ResourceLocation dimId, int x, int y, int z)
	{
		return "dim " + dimId + ": " + x + "/" + y + "/" + z;
	}

	public static String formatPosition(BlockPos pos)
	{
		return formatPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public static String formatPosition(int x, int y, int z)
	{
		return x + "/" + y + "/" + z;
	}

	public static String toSiString(double value, int digits)
	{
		if (value == 0.0)
		{
			return "0 ";
		}

		if (Double.isNaN(value))
		{
			return "NaN ";
		}

		String ret = "";
		if (value < 0.0)
		{
			ret = "-";
			value = -value;
		}

		if (Double.isInfinite(value))
		{
			return ret + "∞ ";
		}

		double log = Math.log10(value);
		double mul;
		String si;
		if (log >= 0.0)
		{
			int reduce = (int) Math.floor(log / 3.0);
			mul = 1.0 / Math.pow(10.0, reduce * 3);
			switch (reduce)
			{
				case 0:
					si = "";
					break;
				case 1:
					si = "k";
					break;
				case 2:
					si = "M";
					break;
				case 3:
					si = "G";
					break;
				case 4:
					si = "T";
					break;
				case 5:
					si = "P";
					break;
				case 6:
					si = "E";
					break;
				case 7:
					si = "Z";
					break;
				case 8:
					si = "Y";
					break;
				default:
					si = "E" + reduce * 3;
			}
		} else
		{
			int expand = (int) Math.ceil(-log / 3.0);
			mul = Math.pow(10.0, expand * 3);
			switch (expand)
			{
				case 0:
					si = "";
					break;
				case 1:
					si = "m";
					break;
				case 2:
					si = "µ";
					break;
				case 3:
					si = "n";
					break;
				case 4:
					si = "p";
					break;
				case 5:
					si = "f";
					break;
				case 6:
					si = "a";
					break;
				case 7:
					si = "z";
					break;
				case 8:
					si = "y";
					break;
				default:
					si = "E-" + expand * 3;
			}
		}

		value *= mul;
		int iVal = (int) Math.floor(value);
		value -= iVal;
		int iDigits = 1;
		if (iVal > 0)
		{
			iDigits = (int) (iDigits + Math.floor(Math.log10(iVal)));
		}

		mul = Math.pow(10.0, digits - iDigits);
		int dVal = (int) Math.round(value * mul);
		if (dVal >= mul)
		{
			iVal++;
			dVal = (int) (dVal - mul);
			iDigits = 1;
			if (iVal > 0)
			{
				iDigits = (int) (iDigits + Math.floor(Math.log10(iVal)));
			}
		}

		ret = ret + iVal;
		if (digits > iDigits && dVal != 0)
		{
			ret = ret + String.format(".%0" + (digits - iDigits) + "d", dVal);
		}

		ret = ret.replaceFirst("(\\.\\d*?)0+$", "$1");
		return ret + " " + si;
	}

	public static void exit(int status)
	{
		Method exit = null;

		try
		{
			exit = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", int.class);
			exit.setAccessible(true);
		} catch (Exception e)
		{
			IC2.log.warn(LogCategory.General, e, "Method lookup failed.");

			try
			{
				Field security = System.class.getDeclaredField("security");
				security.setAccessible(true);
				security.set(null, null);
				exit = System.class.getMethod("exit", int.class);
			} catch (Exception f)
			{
				throw new Error(f);
			}
		}

		try
		{
			exit.invoke(null, status);
		} catch (Exception e)
		{
			throw new Error(e);
		}
	}

	public static boolean isFakePlayer(Player entity, boolean fuzzy)
	{
		if (entity == null)
		{
			return false;
		} else if (!(entity instanceof ServerPlayer))
		{
			return true;
		} else
		{
			return fuzzy ? IC2.envProxy.isFakePlayer(entity) : entity.getClass() != ServerPlayer.class;
		}
	}

	public static boolean isAreaLoaded(LevelReader world, BlockPos center, int dist)
	{
		return world.hasChunksAt(center.getX() - dist, center.getZ() - dist, center.getX() + dist, center.getZ() + dist);
	}

	public static boolean harvestBlock(Level world, BlockPos pos)
	{
		if (world.isClientSide)
		{
			return false;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		BlockEntity be = world.getBlockEntity(pos);
		Player player = Ic2Player.get(world);
		block.playerWillDestroy(world, pos, state, player);
		if (!world.removeBlock(pos, false))
		{
			return false;
		}

		block.destroy(world, pos, state);
		block.playerDestroy(world, player, pos, state, be, new ItemStack(Items.DIAMOND_PICKAXE));
		return true;
	}

	public static boolean matchesOD(ItemStack stack, Object match)
	{
		if (!(match instanceof ItemStack))
		{
			if (!(match instanceof TagKey<?> tagKey && tagKey.isFor(BuiltInRegistries.ITEM.key())))
			{
				return stack == match;
			} else
			{
				if (StackUtil.isEmpty(stack))
				{
					return false;
				}

				Optional<TagKey<Item>> itemTagKeyOpt = tagKey.cast(BuiltInRegistries.ITEM.key());
				if (itemTagKeyOpt.isEmpty())
				{
					return false;
				}

				TagKey<Item> itemTagKey = itemTagKeyOpt.get();
				return stack.is(itemTagKey);
			}
		} else
		{
			return !StackUtil.isEmpty(stack) && ItemStack.isSameItem(stack, (ItemStack) match);
		}
	}
}
