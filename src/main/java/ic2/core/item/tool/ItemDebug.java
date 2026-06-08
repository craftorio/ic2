package ic2.core.item.tool;

import ic2.api.crops.CropCard;
import ic2.api.energy.EnergyNet;
import ic2.api.item.IBoxable;
import ic2.api.item.IDebuggable;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import ic2.api.reactor.IReactor;
import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.generator.tileentity.TileEntityBaseGenerator;
import ic2.core.block.personal.IPersonalBlock;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.crop.TileEntityCrop;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.item.InfiniteElectricItemManager;
import ic2.core.item.PriorityUsableItem;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class ItemDebug extends Item implements PriorityUsableItem, ISpecialElectricItem, IBoxable
{
	private static IElectricItemManager manager = null;

	public ItemDebug(Properties settings)
	{
		super(settings);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		ItemDebug.Mode mode = getMode(stack);
		if (IC2.keyboard.isModeSwitchKeyDown(player))
		{
			if (!world.isClientSide)
			{
				mode = ItemDebug.Mode.modes[(mode.ordinal() + 1) % ItemDebug.Mode.modes.length];
				setMode(stack, mode);
				IC2.sideProxy.messagePlayer(player, "Debug Item Mode: " + mode.getName());
				return InteractionResult.SUCCESS;
			} else
			{
				return InteractionResult.PASS;
			}
		} else
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof IDebuggable dbg)
			{
				if (world.isClientSide)
				{
					return InteractionResult.PASS;
				}

				if (dbg.isDebuggable())
				{
					IC2.sideProxy.messagePlayer(player, dbg.getDebugText());
				}

			} else
			{
				ItemDebug.Output output = new ItemDebug.Output();
				switch (mode)
				{
					case InterfacesFields:
					case InterfacesFieldsRetrace:
					{
						String plat = getPlatform(world);
						BlockState state = world.getBlockState(pos);
						Block block = state.getBlock();
						BlockEntity te = world.getBlockEntity(pos);
						output.both("[%s] block state: %s%nname: %s%ncls: %s%nbe: %s", plat, state, block.getDescriptionId(), block.getClass().getName(), te);
						if (te != null)
						{
							output.part("[%s] interfaces:", plat);
							Class<?> c = te.getClass();

							do
							{
								for (Class<?> i : c.getInterfaces())
								{
									output.part(' ').part(i.getName());
								}

								c = c.getSuperclass();
							} while (c != null);

							output.partToConsole();
						}

						output.console("block fields:");
						dumpObjectFields(block, output);
						if (te != null)
						{
							output.console("");
							output.console("tile entity fields:");
							dumpObjectFields(te, output);
						}
						break;
					}
					case TileData:
					{
						if (world.isClientSide)
						{
							return InteractionResult.PASS;
						}

						BlockEntity tileEntity = world.getBlockEntity(pos);
						if (tileEntity instanceof Ic2TileEntity te)
						{
							output.chat("Block: Active=%b Facing=%s", te.getActive(), te.getFacing());

							for (TileEntityComponent comp : te.getComponents())
							{
								if (comp instanceof Energy energy)
								{
									output.chat("Energy: %.2f / %.2f", energy.getEnergy(), energy.getCapacity());
								} else if (comp instanceof Redstone redstone)
								{
									output.chat("Redstone: %d", redstone.getRedstoneInput());
								}
							}
						}

						if (tileEntity instanceof TileEntityBaseGenerator te)
						{
							output.chat("BaseGen: Fuel=%d", te.fuel);
						}

						if (tileEntity instanceof IEnergyStorage te)
						{
							output.chat("EnergyStorage: Stored=%d", te.getStored());
						}

						if (tileEntity instanceof IReactor te)
						{
							output.chat(
								"Reactor: Heat=%d MaxHeat=%d HEM=%f Output=%f", te.getHeat(), te.getMaxHeat(), te.getHeatEffectModifier(), te.getReactorEnergyOutput()
							);
						}

						if (tileEntity instanceof IPersonalBlock te)
						{
							output.chat("PersonalBlock: CanAccess=%b", te.permitsAccess(player.getGameProfile()));
						}

						if (tileEntity instanceof TileEntityCrop te)
						{
							CropCard crop = te.getCrop();
							String id = crop != null ? crop.getOwner() + ":" + crop.getId() : "none";
							output.chat(
								"Crop: Crop=%s Size=%d Growth=%d Gain=%d Resistance=%d Nutrients=%d Water=%d GrowthPoints=%d%n Cross=%b",
								id,
								te.getCurrentAge(),
								te.getStatGrowth(),
								te.getStatGain(),
								te.getStatResistance(),
								te.getStorageNutrients(),
								te.getStorageWater(),
								te.getGrowthPoints(),
								te.isCrossingBase()
							);
						}
						break;
					}
					case EnergyNet:
					{
						if (world.isClientSide)
						{
							return InteractionResult.PASS;
						}

						ByteArrayOutputStream consoleBuffer = new ByteArrayOutputStream();
						PrintStream consoleStream = new PrintStream(consoleBuffer, false, StandardCharsets.UTF_8);
						ByteArrayOutputStream chatBuffer = new ByteArrayOutputStream();
						PrintStream chatStream = new PrintStream(consoleBuffer, false, StandardCharsets.UTF_8);
						if (!((EnergyNetGlobal) EnergyNet.instance).dumpDebugInfo(world, pos, consoleStream, chatStream))
						{
							return InteractionResult.PASS;
						}

						chatStream.flush();
						consoleStream.flush();
						if (consoleBuffer.size() > 0)
						{
							output.console(consoleBuffer.toString(StandardCharsets.UTF_8).stripTrailing());
						}

						if (chatBuffer.size() > 0)
						{
							output.chat(chatBuffer.toString(StandardCharsets.UTF_8).stripTrailing());
						}
						break;
					}
					case Accelerate:
					case AccelerateX100:
						if (world.isClientSide)
						{
							return InteractionResult.PASS;
						}

						accelerate(world, pos, mode == ItemDebug.Mode.Accelerate ? 1000 : 100000, output);
				}

				output.flush(player);
			}
			return world.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
		}
	}

	private static boolean accelerate(Level world, BlockPos pos, int count, ItemDebug.Output output)
	{
		BlockState state = world.getBlockState(pos);
		BlockEntity be;
		if (state.hasBlockEntity() && (be = world.getBlockEntity(pos)) != null)
		{
			BlockEntityTicker ticker = state.getTicker(world, be.getType());
			if (ticker == null)
			{
				return false;
			}

			output.chat("Running %s ticks on %s.", count, be);
			int changes = 0;
			int interruptCount = -1;

			for (int i = 0; i < count; i++)
			{
				if (be.isRemoved())
				{
					changes++;
					state = world.getBlockState(pos);
					if (!state.hasBlockEntity() || (be = world.getBlockEntity(pos)) == null || be.isRemoved() || (ticker = state.getTicker(world, be.getType())) == null)
					{
						interruptCount = i;
						break;
					}
				}

				ticker.tick(world, pos, state, be);
			}

			if (changes > 0)
			{
				if (interruptCount != -1)
				{
					output.chat("The tile entity changed %d time(s), interrupted after %d updates.", changes, interruptCount);
				} else
				{
					output.chat("The tile entity changed %d time(s).", changes);
				}
			}

		} else
		{
			if (!state.isRandomlyTicking())
			{
				return false;
			}

			output.chat("Running up to %d ticks on % (%s).", count, state.getBlock(), pos);

			for (int i = 0; i < count && world.getBlockState(pos) == state; i++)
			{
				state.randomTick((ServerLevel) world, pos, IC2.random);
				if (world.getBlockState(pos) != state)
				{
					output.chat("Ran %d ticks before a state change.", i);
					break;
				}
			}

		}
		return true;
	}

	public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand)
	{
		return handleEntity(stack, user, entity);
	}

	private static InteractionResult handleEntity(ItemStack stack, Player player, Entity entity)
	{
		ItemDebug.Mode mode = getMode(stack);
		if (mode != ItemDebug.Mode.InterfacesFieldsRetrace)
		{
			return InteractionResult.PASS;
		}

		Level world = player.getLevel();
		ItemDebug.Output output = new ItemDebug.Output();
		String plat = getPlatform(world);
		output.both("[%s] entity: %s", output, entity);
		if (entity instanceof ItemEntity)
		{
			ItemStack entStack = ((ItemEntity) entity).getItem();
			String name = Util.getName(entStack.getItem()).toString();
			output.both("[%s] item id: %s size: %s name: %s", plat, name, StackUtil.getSize(entStack), entStack.getDescriptionId());
			output.console("NBT: %s", entStack.getTag());
		}

		output.flush(player);
		return world.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
	}

	private static ItemDebug.Mode getMode(ItemStack stack)
	{
		CompoundTag nbt = stack.getTag();
		int modeIdx = nbt != null ? nbt.getInt("mode") : 0;
		if (modeIdx < 0 || modeIdx >= ItemDebug.Mode.modes.length)
		{
			modeIdx = 0;
		}

		return ItemDebug.Mode.modes[modeIdx];
	}

	private static void setMode(ItemStack stack, ItemDebug.Mode mode)
	{
		stack.getOrCreateTag().putInt("mode", mode.ordinal());
	}

	private static String getPlatform(Level world)
	{
		if (IC2.envProxy.isClientEnv())
		{
			if (!world.isClientSide)
			{
				return "sp server";
			} else
			{
				return world.getServer() == null ? "mp client" : "sp client";
			}
		} else
		{
			return "mp server";
		}
	}

	private static void dumpObjectFields(Object o, ItemDebug.Output output)
	{
		List<Class<?>> classes = new ArrayList<>();
		Class<?> cls = o.getClass();

		do
		{
			classes.add(cls);
		} while ((cls = cls.getSuperclass()) != null);

		for (int clsIdx = classes.size() - 1; clsIdx >= 0; clsIdx--)
		{
			Class<?> fieldDeclaringClass = classes.get(clsIdx);
			Field[] fields = fieldDeclaringClass.getDeclaredFields();
			boolean printedHeader = false;

			for (Field field : fields)
			{
				Class<?> type = field.getType();
				int modifiers = field.getModifiers();
				if (!Modifier.isStatic(modifiers)
					|| fieldDeclaringClass != Block.class
					&& fieldDeclaringClass != BlockEntity.class
					&& fieldDeclaringClass != Ic2TileEntity.class
					&& (!Modifier.isFinal(modifiers) || !type.isPrimitive() && type != String.class && !Property.class.isAssignableFrom(type)))
				{
					if (!printedHeader)
					{
						output.console(fieldDeclaringClass.getName());
						printedHeader = true;
					}

					Object value;
					try
					{
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						value = field.get(o);
						field.setAccessible(accessible);
					} catch (ReflectiveOperationException e)
					{
						value = "<can't access>";
					}

					output.console("  %s type: %s", field.getName(), type.getName());
					if (!isSelfDescribingClass(type))
					{
						output.part("    identity hash: %x hash: %x modifiers: %x", System.identityHashCode(value), value == null ? 0 : value.hashCode(), modifiers);
						if (value != null && value.getClass() != type)
						{
							output.part(" class: %s", value.getClass().getName());
						}

						output.partToConsole();
					}

					if (value != null && field.getType().isArray())
					{
						List<Object> array = new ArrayList<>();

						for (int i = 0; i < Array.getLength(value); i++)
						{
							array.add(Array.get(value, i));
						}

						value = array;
					}

					if (value instanceof Iterable)
					{
						output.part("    values (%s):", value instanceof Collection ? ((Collection<?>) value).size() : "?");
						int i = 0;

						for (Object o2 : (Iterable<?>) value)
						{
							output.part("      [%d] ", i++);
							dumpValueString(o2, field, "        ", output);
						}
					} else if (value instanceof Map)
					{
						output.console("    values (%s):", ((Map<?, ?>) value).size());

						for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet())
						{
							output.part("      %s: ", entry.getKey());
							dumpValueString(entry.getValue(), field, "        ", output);
						}
					} else
					{
						output.part("    value: ");
						dumpValueString(value, field, "      ", output);
					}
				}
			}
		}
	}

	private static void dumpValueString(Object o, Field parentField, String prefix, ItemDebug.Output output)
	{
		if (o == null)
		{
			output.part("<null>");
			output.partToConsole();
		} else
		{
			StringBuilder ret;
			if (o.getClass().isArray())
			{
				ret = new StringBuilder();

				for (int i = 0; i < Array.getLength(o); i++)
				{
					Object val = Array.get(o, i);
					String valStr;
					if (val == null)
					{
						valStr = "<null>";
					} else
					{
						valStr = val.toString();
						if (valStr.length() > 32)
						{
							valStr = valStr.substring(0, 20) + "... (" + (valStr.length() - 20) + " more)";
						}
					}

					ret.append(" [").append(i).append("] ").append(valStr);
				}
			} else
			{
				ret = new StringBuilder(o.toString());
			}

			if (ret.length() > 100)
			{
				ret = new StringBuilder(ret.substring(0, 90) + "... (" + (ret.length() - 90) + " more)");
			}

			output.part(ret.toString());
			output.partToConsole();
			if (!Modifier.isStatic(parentField.getModifiers())
				&& !parentField.isSynthetic()
				&& !o.getClass().isArray()
				&& !(o instanceof Iterable)
				&& !isSelfDescribingClass(o.getClass()))
			{
				if (o instanceof Level)
				{
					output.console("%s dim: %s", prefix, Util.getDimId((Level) o));
				} else if (!(o instanceof StateDefinition)
					&& !(o instanceof BlockEntity)
					&& !(o instanceof ItemStack)
					&& !o.getClass().getName().startsWith("java."))
				{
					for (Class<?> fieldDeclaringClass = o.getClass();
					     fieldDeclaringClass != null && fieldDeclaringClass != Object.class;
					     fieldDeclaringClass = fieldDeclaringClass.getSuperclass()
					)
					{
						for (Field field : fieldDeclaringClass.getDeclaredFields())
						{
							if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers()))
							{
								Object val;
								try
								{
									field.setAccessible(true);
									val = field.get(o);
								} catch (Exception e)
								{
									val = "<can't access>";
								}

								String valStr;
								if (val == o)
								{
									valStr = "<parent>";
								} else
								{
									valStr = toStringLimited(val, 100);
								}

								output.console("%s%s: %s", prefix, field.getName(), valStr);
							}
						}
					}
				}
			}
		}
	}

	private static boolean isSelfDescribingClass(Class<?> cls)
	{
		return cls.isPrimitive()
			|| cls.isEnum()
			|| cls == Class.class
			|| cls == String.class
			|| cls == BlockState.class
			|| cls == ResourceLocation.class
			|| Tag.class.isAssignableFrom(cls)
			|| Vec3i.class.isAssignableFrom(cls)
			|| Vec3.class.isAssignableFrom(cls)
			|| Block.class.isAssignableFrom(cls)
			|| Item.class.isAssignableFrom(cls)
			|| Fluid.class.isAssignableFrom(cls);
	}

	private static String toStringLimited(Object o, int limit)
	{
		if (o == null)
		{
			return "<null>";
		} else
		{
			int extra = 12;
			limit = Math.max(limit, 12);
			String ret = o.toString();
			if (ret.length() > limit)
			{
				int newLimit = limit - 12;
				return ret.substring(0, newLimit) + "... (" + (ret.length() - newLimit) + " more)";
			} else
			{
				return ret;
			}
		}
	}

	@Override
	public IElectricItemManager getManager(ItemStack stack)
	{
		if (manager == null)
		{
			manager = new InfiniteElectricItemManager();
		}

		return manager;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	private enum Mode
	{
		InterfacesFields("Interfaces and Fields"),
		InterfacesFieldsRetrace("Interfaces and Fields (liquid/entity)"),
		TileData("Tile Data"),
		EnergyNet("Energy Net"),
		Accelerate("Accelerate"),
		AccelerateX100("Accelerate x100");

		static final ItemDebug.Mode[] modes = values();
		private final String name;

		Mode(String name)
		{
			this.name = name;
		}

		String getName()
		{
			return this.name;
		}
	}

	private static class Output
	{
		private final StringBuilder chatSb = new StringBuilder();
		private final StringBuilder consoleSb = new StringBuilder();
		private final StringBuilder partSb = new StringBuilder();

		public void chat(CharSequence line)
		{
			if (!this.chatSb.isEmpty())
			{
				this.chatSb.append('\n');
			}

			this.chatSb.append(line);
		}

		public void chat(String format, Object... args)
		{
			this.chat(String.format(format, args));
		}

		public void console(CharSequence line)
		{
			if (!this.consoleSb.isEmpty())
			{
				this.consoleSb.append('\n');
			}

			this.consoleSb.append(line);
		}

		public void console(String format, Object... args)
		{
			this.console(String.format(format, args));
		}

		public void both(CharSequence line)
		{
			this.chat(line);
			this.console(line);
		}

		public void both(String format, Object... args)
		{
			this.both(String.format(format, args));
		}

		public ItemDebug.Output part(CharSequence line)
		{
			this.partSb.append(line);
			return this;
		}

		public ItemDebug.Output part(char c)
		{
			this.partSb.append(c);
			return this;
		}

		public ItemDebug.Output part(String format, Object... args)
		{
			return this.part(String.format(format, args));
		}

		public void partToChat()
		{
			this.chat(this.partSb);
			this.partSb.setLength(0);
		}

		public void partToConsole()
		{
			this.console(this.partSb);
			this.partSb.setLength(0);
		}

		public void partToBoth()
		{
			this.both(this.partSb);
			this.partSb.setLength(0);
		}

		void flush(Player player)
		{
			if (player.getLevel().isClientSide)
			{
				System.out.println(this.consoleSb);

				for (String line : this.chatSb.toString().split("[\\r\\n]+"))
				{
					IC2.sideProxy.messagePlayer(player, line);
				}
			} else if (player instanceof ServerPlayer)
			{
				IC2.network.get(true).sendConsole((ServerPlayer) player, this.consoleSb.toString());
				IC2.network.get(true).sendChat((ServerPlayer) player, this.chatSb.toString());
			}

			this.chatSb.setLength(0);
			this.consoleSb.setLength(0);
		}
	}
}
