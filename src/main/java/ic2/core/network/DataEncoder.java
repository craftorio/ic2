package ic2.core.network;

import com.mojang.authlib.GameProfile;
import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.network.IGrowingBuffer;
import ic2.api.network.INetworkCustomEncoder;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.core.IC2;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import ic2.core.util.Util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public final class DataEncoder
{
	private static final Map<Class<?>, DataEncoder.EncodedType> classToTypeCache = Collections.synchronizedMap(new IdentityHashMap<>());
	private static final Map<Class<?>, INetworkCustomEncoder> classToAddonType = Collections.synchronizedMap(new IdentityHashMap<>());

	public static void encode(GrowingBuffer os, Object o) throws IOException
	{
		try
		{
			encode(os, o, true);
		} catch (IllegalArgumentException e)
		{
			IC2.sideProxy.displayError(e, "An unknown data type was attempted to be encoded for sending through\nmultiplayer.\nThis could happen due to a bug.");
		}
	}

	public static void encode(IGrowingBuffer os, Object o, boolean withType) throws IOException
	{
		DataEncoder.EncodedType type = typeFromObject(o);
		if (withType)
		{
			os.writeByte(idFromType(type));
		}

		switch (type)
		{
			case Addon:
			case UnSafeAddon:
				assert o != null;
				INetworkCustomEncoder ince = classToAddonType.get(o.getClass());
				if (ince == null)
				{
					throw new IllegalStateException("Cannot encode an object without an encoder! Type was " + o.getClass());
				}

				os.writeString(o.getClass().getName());
				ince.encode(os, o);
				break;
			case Array:
				Class<?> componentClass = o.getClass().getComponentType();
				int len = Array.getLength(o);
				if (componentClass == Object.class && len > 0)
				{
					boolean isEnum = false;
					Class<?> target = null;

					label200:
					for (int i = 0; i < len; i++)
					{
						Object value = Array.get(o, i);
						if (target == null)
						{
							if (value instanceof Enum)
							{
								target = ((Enum) value).getDeclaringClass();
								isEnum = true;
							} else if (value != null)
							{
								target = value.getClass();
								assert target != Object.class;
							}
						} else if (value != null)
						{
							Class<?> valueClass = value.getClass();
							if (valueClass != target && !target.isAssignableFrom(valueClass))
							{
								if (isEnum || value instanceof Enum)
								{
									throw new IllegalArgumentException("Array of mixed enum entries");
								}

								while ((target = target.getSuperclass()) != Object.class)
								{
									if (target.isAssignableFrom(valueClass))
									{
										continue label200;
									}
								}

								i++;

								while (i < len)
								{
									if (Array.get(o, i) instanceof Enum)
									{
										throw new IllegalArgumentException("Array of mixed enum entries");
									}

									i++;
								}

								target = Object.class;
								break;
							} else
							{
								assert isEnum == (value instanceof Enum);
							}
						} else if (isEnum)
						{
							throw new IllegalArgumentException("Enum array with null entry");
						}
					}

					componentClass = target;
				}

				DataEncoder.EncodedType componentType = typeFromClass(componentClass);
				os.writeByte(idFromType(componentType));
				os.writeBoolean(componentClass.isPrimitive());
				if (componentType == DataEncoder.EncodedType.Addon
					|| componentType == DataEncoder.EncodedType.UnSafeAddon
					|| componentType == DataEncoder.EncodedType.Enum)
				{
					os.writeString(componentClass.getName());
				}

				os.writeVarInt(len);
				boolean anyTypeMismatch = false;

				for (int i = 0; i < len; i++)
				{
					Object value = Array.get(o, i);
					if (value == null || typeFromClass(value.getClass()) != componentType)
					{
						anyTypeMismatch = true;
						break;
					}
				}

				os.writeBoolean(anyTypeMismatch);

				for (int i = 0; i < len; i++)
				{
					encode(os, Array.get(o, i), anyTypeMismatch);
				}
				break;
			case Block:
				encode(os, Util.getName((Block) o), false);
				break;
			case BlockPos:
			{
				BlockPos pos = (BlockPos) o;
				os.writeInt(pos.getX());
				os.writeInt(pos.getY());
				os.writeInt(pos.getZ());
				break;
			}
			case Boolean:
				os.writeBoolean((Boolean) o);
				break;
			case Byte:
				os.writeByte((Byte) o);
				break;
			case Character:
				os.writeChar((Character) o);
				break;
			case ChunkPos:
			{
				ColumnPos pos = (ColumnPos) o;
				os.writeInt(pos.f_140723_());
				os.writeInt(pos.f_140724_());
				break;
			}
			case Collection:
				encode(os, ((Collection) o).toArray(), false);
				break;
			case Component:
				CompoundTag nbt = ((TileEntityComponent) o).writeToNbt();
				encode(os, nbt == null ? new CompoundTag() : nbt, false);
				break;
			case CropCard:
				CropCard cropCard = (CropCard) o;
				os.writeString(cropCard.getOwner());
				os.writeString(cropCard.getId());
				break;
			case Double:
				os.writeDouble((Double) o);
				break;
			case ElectrolyzerRecipe:
				IElectrolyzerRecipeManager.ElectrolyzerRecipe recipe = (IElectrolyzerRecipeManager.ElectrolyzerRecipe) o;
				os.writeInt(recipe.inputAmount);
				os.writeInt(recipe.EUaTick);
				os.writeInt(recipe.ticksNeeded);
				IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs = recipe.outputs;
				os.writeByte(outputs.length);

				for (IElectrolyzerRecipeManager.ElectrolyzerOutput output : outputs)
				{
					encode(os, output.fluid, false);
					os.writeInt(output.fluidAmount);
					os.writeByte(output.tankDirection.m_122411_());
				}
				break;
			case Enchantment:
				encode(os, Registry.f_122825_.getKey((Enchantment) o), false);
				break;
			case Enum:
				os.writeVarInt(((Enum) o).ordinal());
				break;
			case Float:
				os.writeFloat((Float) o);
				break;
			case Fluid:
				encode(os, Registry.f_122822_.getKey((Fluid) o), false);
				break;
			case FluidStack:
				Ic2FluidStack fs = (Ic2FluidStack) o;
				encode(os, fs.getFluid(), false);
				os.writeInt(fs.getAmountMb());
				encode(os, FluidHandler.getFluidStackNbt(fs), true);
				break;
			case FluidTank:
				Ic2FluidTank tank = (Ic2FluidTank) o;
				encode(os, tank.getFluidStack(), true);
				os.writeInt(tank.getCapacity());
				break;
			case GameProfile:
				GameProfile gp = (GameProfile) o;
				encode(os, gp.getId(), true);
				os.writeString(gp.getName());
				break;
			case Integer:
				os.writeInt((Integer) o);
				break;
			case InvSlot:
				InvSlot slot = (InvSlot) o;
				ItemStack[] contents = new ItemStack[slot.size()];

				for (int i = 0; i < slot.size(); i++)
				{
					contents[i] = slot.get(i);
				}

				encode(os, contents, false);
				break;
			case Item:
				encode(os, Util.getName((Item) o), false);
				break;
			case ItemStack:
				ItemStack stack = (ItemStack) o;
				if (StackUtil.isEmpty(stack))
				{
					os.writeByte(0);
				} else
				{
					os.writeByte(StackUtil.getSize(stack));
					encode(os, stack.getItem(), false);
					encode(os, stack.getTag(), true);
				}
				break;
			case Long:
				os.writeLong((Long) o);
				break;
			case NBTTagCompound:
				NbtIo.m_128941_((CompoundTag) o, os);
				break;
			case Null:
				if (!withType)
				{
					throw new IllegalArgumentException("o has to be non-null without types");
				}
				break;
			case Object:
				throw new IllegalArgumentException("unhandled class: " + o.getClass());
			case Potion:
				encode(os, Registry.f_122823_.getKey((MobEffect) o), false);
				break;
			case ResourceLocation:
				ResourceLocation loc = (ResourceLocation) o;
				os.writeString(loc.m_135827_());
				os.writeString(loc.m_135815_());
				break;
			case Short:
				os.writeShort((Short) o);
				break;
			case String:
				os.writeString((String) o);
				break;
			case TileEntity:
				BlockEntity te = (BlockEntity) o;
				encode(os, te.getLevel(), false);
				encode(os, te.getBlockPos(), false);
				break;
			case TupleT2:
			{
				Tuple.T2<?, ?> t = (Tuple.T2<?, ?>) o;
				encode(os, t.a, true);
				encode(os, t.b, true);
				break;
			}
			case TupleT3:
			{
				Tuple.T3<?, ?, ?> t = (Tuple.T3<?, ?, ?>) o;
				encode(os, t.a, true);
				encode(os, t.b, true);
				encode(os, t.c, true);
				break;
			}
			case UUID:
				UUID uuid = (UUID) o;
				os.writeLong(uuid.getMostSignificantBits());
				os.writeLong(uuid.getLeastSignificantBits());
				break;
			case Vec3:
				Vec3 v = (Vec3) o;
				os.writeDouble(v.f_82479_);
				os.writeDouble(v.f_82480_);
				os.writeDouble(v.f_82481_);
				break;
			case World:
				encode(os, Util.getDimId((Level) o), false);
				break;
			default:
				throw new IllegalArgumentException("unhandled type: " + type);
		}
	}

	public static Object decode(IGrowingBuffer is) throws IOException
	{
		try
		{
			return decode(is, typeFromId(is.readUnsignedByte()));
		} catch (IllegalArgumentException e)
		{
			String msg = "An unknown data type was received over multiplayer to be decoded.\nThis could happen due to corrupted data or a bug.";
			IC2.sideProxy.displayError(e, msg);
			return null;
		}
	}

	public static <T> T decode(IGrowingBuffer is, Class<T> clazz) throws IOException
	{
		DataEncoder.EncodedType type = typeFromClass(clazz);
		if (type.threadSafe)
		{
			return (T) decode(is, type);
		} else
		{
			throw new IllegalArgumentException("requesting decode for non thread safe type");
		}
	}

	public static <T extends Enum<T>> T decodeEnum(IGrowingBuffer is, Class<T> clazz) throws IOException
	{
		int ordinal = (Integer) decode(is, DataEncoder.EncodedType.Enum);
		T[] values = (T[]) clazz.getEnumConstants();
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
	}

	public static Object decodeDeferred(GrowingBuffer is, Class<?> clazz) throws IOException
	{
		DataEncoder.EncodedType type = typeFromClass(clazz);
		return decode(is, type);
	}

	public static Object decode(IGrowingBuffer is, DataEncoder.EncodedType type) throws IOException
	{
		switch (type)
		{
			case Addon:
			case UnSafeAddon:
				String aimTypeName = is.readString();
				final INetworkCustomEncoder ince = classToAddonType.get(getClass(aimTypeName));
				if (ince == null)
				{
					throw new IllegalStateException("Cannot decode an object without a decoder! Type was " + aimTypeName);
				} else
				{
					if (ince.isThreadSafe())
					{
						return ince.decode(is);
					}

					return new DataEncoder.IResolvableValue<Object>()
					{
						@Override
						public Object get(MinecraftServer server)
						{
							try
							{
								return ince.decode(is);
							} catch (IOException e)
							{
								throw new RuntimeException("Unexpected error", e);
							}
						}
					};
				}
			case Array:
				DataEncoder.EncodedType componentType = typeFromId(is.readUnsignedByte());
				boolean primitive = is.readBoolean();
				boolean isEnum = componentType == DataEncoder.EncodedType.Enum;
				Class<?> component = primitive ? unbox(componentType.cls) : componentType.cls;
				if (component == null || isEnum)
				{
					assert componentType == DataEncoder.EncodedType.Addon || componentType == DataEncoder.EncodedType.UnSafeAddon || isEnum;
					component = getClass(is.readString());
				}

				final Class<?> componentClass = component;
				final int len = is.readVarInt();
				boolean anyTypeMismatch = is.readBoolean();
				boolean needsResolving = !componentType.threadSafe;
				Object array;
				if (!needsResolving)
				{
					array = Array.newInstance(componentClass, len);
				} else
				{
					array = new Object[len];
				}

				if (!anyTypeMismatch)
				{
					if (isEnum)
					{
						Object[] constants = componentClass.getEnumConstants();
						assert constants != null;

						for (int i = 0; i < len; i++)
						{
							Array.set(array, i, constants[decode(is, componentType)]);
						}
					} else
					{
						for (int i = 0; i < len; i++)
						{
							Array.set(array, i, decode(is, componentType));
						}
					}
				} else
				{
					for (int i = 0; i < len; i++)
					{
						DataEncoder.EncodedType cType = typeFromId(is.readUnsignedByte());
						if (!cType.threadSafe && !needsResolving)
						{
							needsResolving = true;
							if (componentClass != Object.class)
							{
								Object newArray = new Object[len];
								System.arraycopy(array, 0, newArray, 0, i);
								array = newArray;
							}
						}

						Array.set(array, i, decode(is, cType));
					}
				}

				if (!needsResolving)
				{
					return array;
				}

				final Object tmpArray = array;
				return new DataEncoder.IResolvableValue<Object>()
				{
					@Override
					public Object get(MinecraftServer server)
					{
						Object ret = Array.newInstance(componentClass, len);

						for (int i = 0; i < len; i++)
						{
							Array.set(ret, i, DataEncoder.getValue(Array.get(tmpArray, i), server));
						}

						return ret;
					}
				};
			case Block:
				return Util.getBlock((ResourceLocation) decode(is, DataEncoder.EncodedType.ResourceLocation));
			case BlockPos:
				return new BlockPos(is.readInt(), is.readInt(), is.readInt());
			case Boolean:
				return is.readBoolean();
			case Byte:
				return is.readByte();
			case Character:
				return is.readChar();
			case ChunkPos:
				return new ColumnPos(is.readInt(), is.readInt());
			case Collection:
			{
				final Object ret = decode(is, DataEncoder.EncodedType.Array);
				if (ret instanceof DataEncoder.IResolvableValue)
				{
					return new DataEncoder.IResolvableValue<List<Object>>()
					{
						public List<Object> get(MinecraftServer server)
						{
							return Arrays.asList((Object[]) ((DataEncoder.IResolvableValue) ret).get(server));
						}
					};
				}

				return Arrays.asList((Object[]) ret);
			}
			case Component:
				return decode(is, DataEncoder.EncodedType.NBTTagCompound);
			case CropCard:
				return Crops.instance.getCropCard(is.readString(), is.readString());
			case Double:
				return is.readDouble();
			case ElectrolyzerRecipe:
				int inputAmount = is.readInt();
				int EUaTick = is.readInt();
				int ticksNeeded = is.readInt();
				byte max = is.readByte();
				IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs = new IElectrolyzerRecipeManager.ElectrolyzerOutput[max];

				for (byte i = 0; i < max; i++)
				{
					outputs[i] = new IElectrolyzerRecipeManager.ElectrolyzerOutput(
						(Fluid) decode(is, DataEncoder.EncodedType.Fluid), is.readInt(), Direction.m_122376_(is.readByte())
					);
				}

				return new IElectrolyzerRecipeManager.ElectrolyzerRecipe(inputAmount, EUaTick, ticksNeeded, outputs);
			case Enchantment:
				return Registry.f_122825_.m_7745_((ResourceLocation) decode(is, DataEncoder.EncodedType.ResourceLocation));
			case Enum:
				return is.readVarInt();
			case Float:
				return is.readFloat();
			case Fluid:
				return Registry.f_122822_.m_7745_((ResourceLocation) decode(is, DataEncoder.EncodedType.ResourceLocation));
			case FluidStack:
			{
				Fluid fluid = (Fluid) decode(is, DataEncoder.EncodedType.Fluid);
				int amount = is.readInt();
				CompoundTag nbt = (CompoundTag) decode(is);
				return FluidHandler.createFluidStackMb(fluid, amount, nbt);
			}
			case FluidTank:
			{
				Ic2FluidStack fs = (Ic2FluidStack) decode(is);
				Ic2FluidTank ret = new Ic2FluidTank(is.readInt());
				ret.setFluidStack(fs);
				return ret;
			}
			case GameProfile:
				return new GameProfile((UUID) decode(is), is.readString());
			case Integer:
				return is.readInt();
			case InvSlot:
			{
				ItemStack[] contents = (ItemStack[]) decode(is, DataEncoder.EncodedType.Array);
				InvSlot ret = new InvSlot(contents.length);

				for (int i = 0; i < contents.length; i++)
				{
					ret.put(i, contents[i]);
				}

				return ret;
			}
			case Item:
				return Util.getItem((ResourceLocation) decode(is, DataEncoder.EncodedType.ResourceLocation));
			case ItemStack:
			{
				int size = is.readByte();
				if (size == 0)
				{
					return StackUtil.emptyStack;
				}

				Item item = decode(is, Item.class);
				CompoundTag nbt = (CompoundTag) decode(is);
				ItemStack ret = new ItemStack(item, size);
				ret.m_41751_(nbt);
				return ret;
			}
			case Long:
				return is.readLong();
			case NBTTagCompound:
				return NbtIo.m_128934_(is, NbtAccounter.f_128917_);
			case Null:
				return null;
			case Object:
				return new Object();
			case Potion:
				return Registry.f_122823_.m_7745_((ResourceLocation) decode(is, DataEncoder.EncodedType.ResourceLocation));
			case ResourceLocation:
				return ResourceLocation.fromNamespaceAndPath(is.readString(), is.readString());
			case Short:
				return is.readShort();
			case String:
				return is.readString();
			case TileEntity:
				final DataEncoder.IResolvableValue<Level> deferredWorld = (DataEncoder.IResolvableValue<Level>) decode(is, DataEncoder.EncodedType.World);
				final BlockPos pos = (BlockPos) decode(is, DataEncoder.EncodedType.BlockPos);
				return new DataEncoder.IResolvableValue<BlockEntity>()
				{
					public BlockEntity get(MinecraftServer server)
					{
						Level world = deferredWorld.get(server);
						return world == null ? null : world.getBlockEntity(pos);
					}
				};
			case TupleT2:
				return new Tuple.T2<>(decode(is), decode(is));
			case TupleT3:
				return new Tuple.T3<>(decode(is), decode(is), decode(is));
			case UUID:
				return new UUID(is.readLong(), is.readLong());
			case Vec3:
				return new Vec3(is.readDouble(), is.readDouble(), is.readDouble());
			case World:
				final ResourceLocation dimensionId = (ResourceLocation) decode(is, DataEncoder.EncodedType.ResourceLocation);
				return new DataEncoder.IResolvableValue<Level>()
				{
					public Level get(MinecraftServer server)
					{
						return IC2.sideProxy.getWorld(server, dimensionId);
					}
				};
			default:
				throw new IllegalArgumentException("unhandled type: " + type);
		}
	}

	public static <T> T getValue(Object decoded, MinecraftServer server)
	{
		return (T) (decoded instanceof DataEncoder.IResolvableValue ? ((DataEncoder.IResolvableValue) decoded).get(server) : decoded);
	}

	public static <T> boolean copyValue(T src, T dst)
	{
		if (src == null || dst == null)
		{
			return false;
		}

		if (dst instanceof ItemStack)
		{
			ItemStack srcT = (ItemStack) src;
			ItemStack dstT = (ItemStack) dst;
			if (srcT.getItem() == dstT.getItem())
			{
				dstT.m_41764_(srcT.m_41613_());
				dstT.m_41751_(srcT.getTag());
				return true;
			} else
			{
				return false;
			}
		} else
		{
			if (dst instanceof Ic2FluidTank)
			{
				Ic2FluidTank srcT = (Ic2FluidTank) src;
				Ic2FluidTank dstT = (Ic2FluidTank) dst;
				dstT.setFluidStack(srcT.getFluidStack());
				dstT.setCapacity(srcT.getCapacity());
			} else if (dst instanceof InvSlot)
			{
				InvSlot srcT = (InvSlot) src;
				InvSlot dstT = (InvSlot) dst;
				if (srcT.size() != dstT.size())
				{
					throw new RuntimeException("Can't sync InvSlots with mismatched sizes.");
				}

				for (int i = 0; i < srcT.size(); i++)
				{
					if (!copyValue(srcT.get(i), dstT.get(i)))
					{
						dstT.put(i, srcT.get(i));
					}
				}
			} else if (dst instanceof TileEntityComponent)
			{
				CompoundTag nbt = (CompoundTag) src;
				((TileEntityComponent) dst).readFromNbt(nbt);
			} else
			{
				if (!(dst instanceof Collection))
				{
					return false;
				}

				Collection<Object> srcT = (Collection<Object>) src;
				Collection<Object> dstT = (Collection<Object>) dst;
				dstT.clear();
				dstT.addAll(srcT);
			}

			return true;
		}
	}

	private static Class<?> box(Class<?> clazz)
	{
		if (clazz == byte.class)
		{
			return Byte.class;
		} else if (clazz == short.class)
		{
			return Short.class;
		} else if (clazz == int.class)
		{
			return Integer.class;
		} else if (clazz == long.class)
		{
			return Long.class;
		} else if (clazz == float.class)
		{
			return Float.class;
		} else if (clazz == double.class)
		{
			return Double.class;
		} else if (clazz == boolean.class)
		{
			return Boolean.class;
		} else
		{
			return clazz == char.class ? Character.class : clazz;
		}
	}

	private static Class<?> unbox(Class<?> clazz)
	{
		if (clazz == Byte.class)
		{
			return byte.class;
		} else if (clazz == Short.class)
		{
			return short.class;
		} else if (clazz == Integer.class)
		{
			return int.class;
		} else if (clazz == Long.class)
		{
			return long.class;
		} else if (clazz == Float.class)
		{
			return float.class;
		} else if (clazz == Double.class)
		{
			return double.class;
		} else if (clazz == Boolean.class)
		{
			return boolean.class;
		} else
		{
			return clazz == Character.class ? char.class : clazz;
		}
	}

	private static Class<?> getClass(String type)
	{
		try
		{
			return Class.forName(type);
		} catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Missing type from the class path expected by network: " + type, e);
		}
	}

	private static int idFromType(DataEncoder.EncodedType type)
	{
		return type.ordinal();
	}

	private static DataEncoder.EncodedType typeFromId(int id)
	{
		if (id >= 0 && id < DataEncoder.EncodedType.types.length)
		{
			return DataEncoder.EncodedType.types[id];
		} else
		{
			throw new IllegalArgumentException("invalid type id: " + id);
		}
	}

	private static DataEncoder.EncodedType typeFromObject(Object o)
	{
		return o == null ? DataEncoder.EncodedType.Null : typeFromClass(o.getClass());
	}

	private static DataEncoder.EncodedType typeFromClass(Class<?> cls)
	{
		if (cls == null)
		{
			return DataEncoder.EncodedType.Null;
		}

		if (cls.isArray())
		{
			return DataEncoder.EncodedType.Array;
		}

		if (cls.isPrimitive())
		{
			cls = box(cls);
		}

		DataEncoder.EncodedType ret = DataEncoder.EncodedType.classToTypeMap.get(cls);
		if (ret != null)
		{
			return ret;
		}

		ret = classToTypeCache.get(cls);
		if (ret != null)
		{
			return ret;
		}

		INetworkCustomEncoder ince = classToAddonType.get(cls);
		if (ince != null)
		{
			ret = ince.isThreadSafe() ? DataEncoder.EncodedType.Addon : DataEncoder.EncodedType.UnSafeAddon;
			classToTypeCache.put(cls, ret);
			return ret;
		}

		for (DataEncoder.EncodedType type : DataEncoder.EncodedType.types)
		{
			if (type.cls != null && type.cls.isAssignableFrom(cls))
			{
				classToTypeCache.put(cls, type);
				return type;
			}
		}

		throw new IllegalStateException("unmatched " + cls);
	}

	public static void addNetworkEncoder(Class<?> typeBeingEncoded, INetworkCustomEncoder customEncoder)
	{
		assert typeBeingEncoded != null && customEncoder != null;
		INetworkCustomEncoder previous = classToAddonType.put(typeBeingEncoded, customEncoder);
		if (previous != null)
		{
			throw new IllegalStateException(
				"Duplicate mapping for class! "
					+ previous.getClass().getName()
					+ " and "
					+ customEncoder.getClass().getName()
					+ " both map for "
					+ typeBeingEncoded.getName()
					+ "."
			);
		}
	}

	public enum EncodedType
	{
		Null(null),
		Array(null),
		Byte(Byte.class),
		Short(Short.class),
		Integer(Integer.class),
		Long(Long.class),
		Float(Float.class),
		Double(Double.class),
		Boolean(Boolean.class),
		Character(Character.class),
		String(String.class),
		Enum(Enum.class),
		UUID(UUID.class),
		Block(Block.class),
		Item(Item.class),
		TileEntity(BlockEntity.class, false),
		ItemStack(ItemStack.class),
		World(Level.class, false),
		NBTTagCompound(CompoundTag.class),
		ResourceLocation(ResourceLocation.class),
		GameProfile(GameProfile.class),
		Potion(MobEffect.class),
		Enchantment(Enchantment.class),
		BlockPos(BlockPos.class),
		ChunkPos(ColumnPos.class),
		Vec3(Vec3.class),
		Fluid(Fluid.class),
		FluidStack(Ic2FluidStack.class),
		FluidTank(Ic2FluidTank.class),
		InvSlot(InvSlot.class),
		Component(TileEntityComponent.class, false),
		CropCard(CropCard.class),
		ElectrolyzerRecipe(IElectrolyzerRecipeManager.ElectrolyzerRecipe.class),
		TupleT2(Tuple.T2.class),
		TupleT3(Tuple.T3.class),
		Addon(null),
		UnSafeAddon(null, false),
		Collection(Collection.class),
		Object(Object.class);

		final Class<?> cls;
		final boolean threadSafe;
		static final DataEncoder.EncodedType[] types = values();
		static final Map<Class<?>, DataEncoder.EncodedType> classToTypeMap = new IdentityHashMap<>(types.length - 2);

		EncodedType(Class<?> cls)
		{
			this(cls, true);
		}

		EncodedType(Class<?> cls, boolean threadSafe)
		{
			this.cls = cls;
			this.threadSafe = threadSafe;
		}

		static
		{
			for (DataEncoder.EncodedType type : types)
			{
				if (type.cls != null)
				{
					classToTypeMap.put(type.cls, type);
				}
			}

			if (types.length > 255)
			{
				throw new RuntimeException("too many types");
			}
		}
	}

	private interface IResolvableValue<T>
	{
		T get(MinecraftServer var1);
	}
}
