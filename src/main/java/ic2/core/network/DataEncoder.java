// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.io.DataInput;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraft.util.EnumFacing;
import ic2.api.crops.Crops;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;
import ic2.core.util.Tuple;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.potion.Potion;
import java.io.DataOutput;
import net.minecraft.nbt.CompressedStreamTools;
import ic2.core.util.StackUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ic2.core.block.invslot.InvSlot;
import com.mojang.authlib.GameProfile;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraft.enchantment.Enchantment;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.api.crops.CropCard;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.comp.TileEntityComponent;
import java.util.Collection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos;
import ic2.core.util.Util;
import net.minecraft.block.Block;
import java.lang.reflect.Array;
import java.io.IOException;
import ic2.core.IC2;
import ic2.api.network.IGrowingBuffer;
import ic2.api.network.INetworkCustomEncoder;
import java.util.Map;

public final class DataEncoder
{
    private static final Map<Class<?>, EncodedType> classToTypeCache;
    private static final Map<Class<?>, INetworkCustomEncoder> classToAddonType;
    
    public static void encode(final GrowingBuffer os, final Object o) throws IOException {
        try {
            encode(os, o, true);
        }
        catch (final IllegalArgumentException e) {
            IC2.platform.displayError(e, "An unknown data type was attempted to be encoded for sending through\nmultiplayer.\nThis could happen due to a bug.", new Object[0]);
        }
    }
    
    public static void encode(final IGrowingBuffer os, final Object o, final boolean withType) throws IOException {
        final EncodedType type = typeFromObject(o);
        if (withType) {
            os.writeByte(idFromType(type));
        }
        switch (type) {
            case Addon:
            case UnSafeAddon: {
                assert o != null;
                final INetworkCustomEncoder ince = DataEncoder.classToAddonType.get(o.getClass());
                if (ince == null) {
                    throw new IllegalStateException("Cannot encode an object without an encoder! Type was " + o.getClass());
                }
                os.writeString(o.getClass().getName());
                ince.encode(os, o);
                break;
            }
            case Array: {
                Class<?> componentClass = o.getClass().getComponentType();
                final int len = Array.getLength(o);
                if (componentClass == Object.class && len > 0) {
                    boolean isEnum = false;
                    Class<?> target = null;
                Label_0569:
                    for (int i = 0; i < len; ++i) {
                        final Object value = Array.get(o, i);
                        if (target == null) {
                            if (value instanceof Enum) {
                                target = ((Enum)value).getDeclaringClass();
                                isEnum = true;
                            }
                            else if (value != null) {
                                target = value.getClass();
                                assert target != Object.class;
                            }
                        }
                        else if (value != null) {
                            final Class<?> valueClass = value.getClass();
                            if (valueClass != target && !target.isAssignableFrom(valueClass)) {
                                if (isEnum || value instanceof Enum) {
                                    throw new IllegalArgumentException("Array of mixed enum entries");
                                }
                                while ((target = target.getSuperclass()) != Object.class) {
                                    if (target.isAssignableFrom(valueClass)) {
                                        continue Label_0569;
                                    }
                                }
                                ++i;
                                while (i < len) {
                                    if (Array.get(o, i) instanceof Enum) {
                                        throw new IllegalArgumentException("Array of mixed enum entries");
                                    }
                                    ++i;
                                }
                                target = Object.class;
                                break;
                            }
                            else {
                                assert isEnum == value instanceof Enum;
                            }
                        }
                        else if (isEnum) {
                            throw new IllegalArgumentException("Enum array with null entry");
                        }
                    }
                    componentClass = target;
                }
                final EncodedType componentType = typeFromClass(componentClass);
                os.writeByte(idFromType(componentType));
                os.writeBoolean(componentClass.isPrimitive());
                if (componentType == EncodedType.Addon || componentType == EncodedType.UnSafeAddon || componentType == EncodedType.Enum) {
                    os.writeString(componentClass.getName());
                }
                os.writeVarInt(len);
                boolean anyTypeMismatch = false;
                for (int i = 0; i < len; ++i) {
                    final Object value = Array.get(o, i);
                    if (value == null || typeFromClass(value.getClass()) != componentType) {
                        anyTypeMismatch = true;
                        break;
                    }
                }
                os.writeBoolean(anyTypeMismatch);
                for (int i = 0; i < len; ++i) {
                    encode(os, Array.get(o, i), anyTypeMismatch);
                }
                break;
            }
            case Block: {
                encode(os, Util.getName((Block)o), false);
                break;
            }
            case BlockPos: {
                final BlockPos pos = (BlockPos)o;
                os.writeInt(pos.getX());
                os.writeInt(pos.getY());
                os.writeInt(pos.getZ());
                break;
            }
            case Boolean: {
                os.writeBoolean((boolean)o);
                break;
            }
            case Byte: {
                os.writeByte((byte)o);
                break;
            }
            case Character: {
                os.writeChar((char)o);
                break;
            }
            case ChunkPos: {
                final ChunkPos pos2 = (ChunkPos)o;
                os.writeInt(pos2.x);
                os.writeInt(pos2.z);
                break;
            }
            case Collection: {
                encode(os, ((Collection)o).toArray(), false);
                break;
            }
            case Component: {
                final NBTTagCompound nbt = ((TileEntityComponent)o).writeToNbt();
                encode(os, (nbt == null) ? new NBTTagCompound() : nbt, false);
                break;
            }
            case CropCard: {
                final CropCard cropCard = (CropCard)o;
                os.writeString(cropCard.getOwner());
                os.writeString(cropCard.getId());
                break;
            }
            case Double: {
                os.writeDouble((double)o);
                break;
            }
            case ElectrolyzerRecipe: {
                final IElectrolyzerRecipeManager.ElectrolyzerRecipe recipe = (IElectrolyzerRecipeManager.ElectrolyzerRecipe)o;
                os.writeInt(recipe.inputAmount);
                os.writeInt(recipe.EUaTick);
                os.writeInt(recipe.ticksNeeded);
                final IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs = recipe.outputs;
                os.writeByte(outputs.length);
                for (final IElectrolyzerRecipeManager.ElectrolyzerOutput output : outputs) {
                    os.writeString(output.fluidName);
                    os.writeInt(output.fluidAmount);
                    os.writeByte(output.tankDirection.getIndex());
                }
                break;
            }
            case Enchantment: {
                encode(os, Enchantment.REGISTRY.getNameForObject((Object)o), false);
                break;
            }
            case Enum: {
                os.writeVarInt(((Enum)o).ordinal());
                break;
            }
            case Float: {
                os.writeFloat((float)o);
                break;
            }
            case Fluid: {
                os.writeString(((Fluid)o).getName());
                break;
            }
            case FluidStack: {
                final FluidStack fs = (FluidStack)o;
                encode(os, fs.getFluid(), false);
                os.writeInt(fs.amount);
                encode(os, fs.tag, true);
                break;
            }
            case FluidTank: {
                final FluidTank tank = (FluidTank)o;
                encode(os, tank.getFluid(), true);
                os.writeInt(tank.getCapacity());
                break;
            }
            case GameProfile: {
                final GameProfile gp = (GameProfile)o;
                encode(os, gp.getId(), true);
                os.writeString(gp.getName());
                break;
            }
            case Integer: {
                os.writeInt((int)o);
                break;
            }
            case InvSlot: {
                final InvSlot slot = (InvSlot)o;
                final ItemStack[] contents = new ItemStack[slot.size()];
                for (int j = 0; j < slot.size(); ++j) {
                    contents[j] = slot.get(j);
                }
                encode(os, contents, false);
                break;
            }
            case Item: {
                encode(os, Util.getName((Item)o), false);
                break;
            }
            case ItemStack: {
                final ItemStack stack = (ItemStack)o;
                if (StackUtil.isEmpty(stack)) {
                    os.writeByte(0);
                    break;
                }
                os.writeByte(StackUtil.getSize(stack));
                encode(os, stack.getItem(), false);
                os.writeShort(stack.getItemDamage());
                encode(os, stack.getTagCompound(), true);
                break;
            }
            case Long: {
                os.writeLong((long)o);
                break;
            }
            case NBTTagCompound: {
                CompressedStreamTools.write((NBTTagCompound)o, (DataOutput)os);
                break;
            }
            case Null: {
                if (!withType) {
                    throw new IllegalArgumentException("o has to be non-null without types");
                }
                break;
            }
            case Object: {
                throw new IllegalArgumentException("unhandled class: " + o.getClass());
            }
            case Potion: {
                encode(os, Potion.REGISTRY.getNameForObject((Object)o), false);
                break;
            }
            case ResourceLocation: {
                final ResourceLocation loc = (ResourceLocation)o;
                os.writeString(loc.getResourceDomain());
                os.writeString(loc.getResourcePath());
                break;
            }
            case Short: {
                os.writeShort((short)o);
                break;
            }
            case String: {
                os.writeString((String)o);
                break;
            }
            case TileEntity: {
                final TileEntity te = (TileEntity)o;
                encode(os, te.getWorld(), false);
                encode(os, te.getPos(), false);
                break;
            }
            case TupleT2: {
                final Tuple.T2<?, ?> t = (Tuple.T2<?, ?>)o;
                encode(os, t.a, true);
                encode(os, t.b, true);
                break;
            }
            case TupleT3: {
                final Tuple.T3<?, ?, ?> t2 = (Tuple.T3<?, ?, ?>)o;
                encode(os, t2.a, true);
                encode(os, t2.b, true);
                encode(os, t2.c, true);
                break;
            }
            case UUID: {
                final UUID uuid = (UUID)o;
                os.writeLong(uuid.getMostSignificantBits());
                os.writeLong(uuid.getLeastSignificantBits());
                break;
            }
            case Vec3: {
                final Vec3d v = (Vec3d)o;
                os.writeDouble(v.x);
                os.writeDouble(v.y);
                os.writeDouble(v.z);
                break;
            }
            case World: {
                os.writeInt(((World)o).provider.getDimension());
                break;
            }
            default: {
                throw new IllegalArgumentException("unhandled type: " + type);
            }
        }
    }
    
    public static Object decode(final IGrowingBuffer is) throws IOException {
        try {
            return decode(is, typeFromId(is.readUnsignedByte()));
        }
        catch (final IllegalArgumentException e) {
            final String msg = "An unknown data type was received over multiplayer to be decoded.\nThis could happen due to corrupted data or a bug.";
            IC2.platform.displayError(e, msg, new Object[0]);
            return null;
        }
    }
    
    public static <T> T decode(final IGrowingBuffer is, final Class<T> clazz) throws IOException {
        final EncodedType type = typeFromClass(clazz);
        if (type.threadSafe) {
            return (T)decode(is, type);
        }
        throw new IllegalArgumentException("requesting decode for non thread safe type");
    }
    
    public static <T extends Enum<T>> T decodeEnum(final IGrowingBuffer is, final Class<T> clazz) throws IOException {
        final int ordinal = (int)decode(is, EncodedType.Enum);
        final T[] values = clazz.getEnumConstants();
        return (T)((ordinal >= 0 && ordinal < values.length) ? values[ordinal] : null);
    }
    
    public static Object decodeDeferred(final GrowingBuffer is, final Class<?> clazz) throws IOException {
        final EncodedType type = typeFromClass(clazz);
        return decode(is, type);
    }
    
    public static Object decode(final IGrowingBuffer is, final EncodedType type) throws IOException {
        switch (type) {
            case Addon:
            case UnSafeAddon: {
                final String aimTypeName = is.readString();
                final INetworkCustomEncoder ince = DataEncoder.classToAddonType.get(getClass(aimTypeName));
                if (ince == null) {
                    throw new IllegalStateException("Cannot decode an object without a decoder! Type was " + aimTypeName);
                }
                if (ince.isThreadSafe()) {
                    return ince.decode(is);
                }
                return new IResolvableValue<Object>() {
                    @Override
                    public Object get() {
                        try {
                            return ince.decode(is);
                        }
                        catch (final IOException e) {
                            throw new RuntimeException("Unexpected error", e);
                        }
                    }
                };
            }
            case Array: {
                final EncodedType componentType = typeFromId(is.readUnsignedByte());
                final boolean primitive = is.readBoolean();
                final boolean isEnum = componentType == EncodedType.Enum;
                Class<?> component = primitive ? unbox(componentType.cls) : componentType.cls;
                if (component == null || isEnum) {
                    assert !(!isEnum);
                    component = getClass(is.readString());
                }
                final Class<?> componentClass = component;
                final int len = is.readVarInt();
                final boolean anyTypeMismatch = is.readBoolean();
                boolean needsResolving = !componentType.threadSafe;
                Object array;
                if (!needsResolving) {
                    array = Array.newInstance(componentClass, len);
                }
                else {
                    array = new Object[len];
                }
                if (!anyTypeMismatch) {
                    if (isEnum) {
                        final Object[] constants = (Object[])componentClass.getEnumConstants();
                        assert constants != null;
                        for (int i = 0; i < len; ++i) {
                            Array.set(array, i, constants[(int)decode(is, componentType)]);
                        }
                    }
                    else {
                        for (int j = 0; j < len; ++j) {
                            Array.set(array, j, decode(is, componentType));
                        }
                    }
                }
                else {
                    for (int j = 0; j < len; ++j) {
                        final EncodedType cType = typeFromId(is.readUnsignedByte());
                        if (!cType.threadSafe && !needsResolving) {
                            needsResolving = true;
                            if (componentClass != Object.class) {
                                final Object newArray = new Object[len];
                                System.arraycopy(array, 0, newArray, 0, j);
                                array = newArray;
                            }
                        }
                        Array.set(array, j, decode(is, cType));
                    }
                }
                if (!needsResolving) {
                    return array;
                }
                final Object tmpArray = array;
                return new IResolvableValue<Object>() {
                    @Override
                    public Object get() {
                        final Object ret = Array.newInstance(componentClass, len);
                        for (int i = 0; i < len; ++i) {
                            Array.set(ret, i, DataEncoder.getValue(Array.get(tmpArray, i)));
                        }
                        return ret;
                    }
                };
            }
            case Block: {
                return Util.getBlock((ResourceLocation)decode(is, EncodedType.ResourceLocation));
            }
            case BlockPos: {
                return new BlockPos(is.readInt(), is.readInt(), is.readInt());
            }
            case Boolean: {
                return is.readBoolean();
            }
            case Byte: {
                return is.readByte();
            }
            case Character: {
                return is.readChar();
            }
            case ChunkPos: {
                return new ChunkPos(is.readInt(), is.readInt());
            }
            case Collection: {
                final Object ret = decode(is, EncodedType.Array);
                if (ret instanceof IResolvableValue) {
                    return new IResolvableValue<List<Object>>() {
                        @Override
                        public List<Object> get() {
                            return Arrays.asList((Object[])((IResolvableValue)ret).get());
                        }
                    };
                }
                return Arrays.asList((Object[])ret);
            }
            case Component: {
                return decode(is, EncodedType.NBTTagCompound);
            }
            case CropCard: {
                return Crops.instance.getCropCard(is.readString(), is.readString());
            }
            case Double: {
                return is.readDouble();
            }
            case ElectrolyzerRecipe: {
                final int inputAmount = is.readInt();
                final int EUaTick = is.readInt();
                final int ticksNeeded = is.readInt();
                final byte max = is.readByte();
                final IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs = new IElectrolyzerRecipeManager.ElectrolyzerOutput[max];
                for (byte k = 0; k < max; ++k) {
                    outputs[k] = new IElectrolyzerRecipeManager.ElectrolyzerOutput(is.readString(), is.readInt(), EnumFacing.getFront((int)is.readByte()));
                }
                return new IElectrolyzerRecipeManager.ElectrolyzerRecipe(inputAmount, EUaTick, ticksNeeded, outputs);
            }
            case Enchantment: {
                return Enchantment.REGISTRY.getObject((Object)decode(is, EncodedType.ResourceLocation));
            }
            case Enum: {
                return is.readVarInt();
            }
            case Float: {
                return is.readFloat();
            }
            case Fluid: {
                return FluidRegistry.getFluid(is.readString());
            }
            case FluidStack: {
                final FluidStack ret2 = new FluidStack((Fluid)decode(is, EncodedType.Fluid), is.readInt());
                ret2.tag = (NBTTagCompound)decode(is);
                return ret2;
            }
            case FluidTank: {
                return new FluidTank((FluidStack)decode(is), is.readInt());
            }
            case GameProfile: {
                return new GameProfile((UUID)decode(is), is.readString());
            }
            case Integer: {
                return is.readInt();
            }
            case InvSlot: {
                final ItemStack[] contents = (ItemStack[])decode(is, EncodedType.Array);
                final InvSlot ret3 = new InvSlot(contents.length);
                for (int l = 0; l < contents.length; ++l) {
                    ret3.put(l, contents[l]);
                }
                return ret3;
            }
            case Item: {
                return Util.getItem((ResourceLocation)decode(is, EncodedType.ResourceLocation));
            }
            case ItemStack: {
                final int size = is.readByte();
                if (size == 0) {
                    return StackUtil.emptyStack;
                }
                final Item item = decode(is, Item.class);
                final int meta = is.readShort();
                final NBTTagCompound nbt = (NBTTagCompound)decode(is);
                final ItemStack ret4 = new ItemStack(item, size, meta);
                ret4.setTagCompound(nbt);
                return ret4;
            }
            case Long: {
                return is.readLong();
            }
            case NBTTagCompound: {
                return CompressedStreamTools.read((DataInput)is, NBTSizeTracker.INFINITE);
            }
            case Null: {
                return null;
            }
            case Object: {
                return new Object();
            }
            case Potion: {
                return Potion.REGISTRY.getObject((Object)decode(is, EncodedType.ResourceLocation));
            }
            case ResourceLocation: {
                return new ResourceLocation(is.readString(), is.readString());
            }
            case Short: {
                return is.readShort();
            }
            case String: {
                return is.readString();
            }
            case TileEntity: {
                final IResolvableValue<World> deferredWorld = (IResolvableValue<World>)decode(is, EncodedType.World);
                final BlockPos pos = (BlockPos)decode(is, EncodedType.BlockPos);
                return new IResolvableValue<TileEntity>() {
                    @Override
                    public TileEntity get() {
                        final World world = deferredWorld.get();
                        if (world == null) {
                            return null;
                        }
                        return world.getTileEntity(pos);
                    }
                };
            }
            case TupleT2: {
                return new Tuple.T2(decode(is), decode(is));
            }
            case TupleT3: {
                return new Tuple.T3(decode(is), decode(is), decode(is));
            }
            case UUID: {
                return new UUID(is.readLong(), is.readLong());
            }
            case Vec3: {
                return new Vec3d(is.readDouble(), is.readDouble(), is.readDouble());
            }
            case World: {
                final int dimensionId = is.readInt();
                return new IResolvableValue<World>() {
                    @Override
                    public World get() {
                        return IC2.platform.getWorld(dimensionId);
                    }
                };
            }
            default: {
                throw new IllegalArgumentException("unhandled type: " + type);
            }
        }
    }
    
    public static <T> T getValue(final Object decoded) {
        if (decoded instanceof IResolvableValue) {
            return ((IResolvableValue)decoded).get();
        }
        return (T)decoded;
    }
    
    public static <T> boolean copyValue(final T src, final T dst) {
        if (src == null || dst == null) {
            return false;
        }
        if (!(dst instanceof ItemStack)) {
            if (dst instanceof FluidTank) {
                final FluidTank srcT = (FluidTank)src;
                final FluidTank dstT = (FluidTank)dst;
                dstT.setFluid(srcT.getFluid());
                dstT.setCapacity(srcT.getCapacity());
            }
            else if (dst instanceof InvSlot) {
                final InvSlot srcT2 = (InvSlot)src;
                final InvSlot dstT2 = (InvSlot)dst;
                if (srcT2.size() != dstT2.size()) {
                    throw new RuntimeException("Can't sync InvSlots with mismatched sizes.");
                }
                for (int i = 0; i < srcT2.size(); ++i) {
                    if (!copyValue(srcT2.get(i), dstT2.get(i))) {
                        dstT2.put(i, srcT2.get(i));
                    }
                }
            }
            else if (dst instanceof TileEntityComponent) {
                final NBTTagCompound nbt = (NBTTagCompound)src;
                ((TileEntityComponent)dst).readFromNbt(nbt);
            }
            else {
                if (!(dst instanceof Collection)) {
                    return false;
                }
                final Collection<Object> srcT3 = (Collection<Object>)src;
                final Collection<Object> dstT3 = (Collection<Object>)dst;
                dstT3.clear();
                dstT3.addAll(srcT3);
            }
            return true;
        }
        final ItemStack srcT4 = (ItemStack)src;
        final ItemStack dstT4 = (ItemStack)dst;
        if (srcT4.getItem() == dstT4.getItem()) {
            dstT4.setCount(srcT4.getCount());
            StackUtil.setRawMeta(dstT4, StackUtil.getRawMeta(srcT4));
            dstT4.setTagCompound(srcT4.getTagCompound());
            return true;
        }
        return false;
    }
    
    private static Class<?> box(final Class<?> clazz) {
        if (clazz == Byte.TYPE) {
            return Byte.class;
        }
        if (clazz == Short.TYPE) {
            return Short.class;
        }
        if (clazz == Integer.TYPE) {
            return Integer.class;
        }
        if (clazz == Long.TYPE) {
            return Long.class;
        }
        if (clazz == Float.TYPE) {
            return Float.class;
        }
        if (clazz == Double.TYPE) {
            return Double.class;
        }
        if (clazz == Boolean.TYPE) {
            return Boolean.class;
        }
        if (clazz == Character.TYPE) {
            return Character.class;
        }
        return clazz;
    }
    
    private static Class<?> unbox(final Class<?> clazz) {
        if (clazz == Byte.class) {
            return Byte.TYPE;
        }
        if (clazz == Short.class) {
            return Short.TYPE;
        }
        if (clazz == Integer.class) {
            return Integer.TYPE;
        }
        if (clazz == Long.class) {
            return Long.TYPE;
        }
        if (clazz == Float.class) {
            return Float.TYPE;
        }
        if (clazz == Double.class) {
            return Double.TYPE;
        }
        if (clazz == Boolean.class) {
            return Boolean.TYPE;
        }
        if (clazz == Character.class) {
            return Character.TYPE;
        }
        return clazz;
    }
    
    private static Class<?> getClass(final String type) {
        try {
            return Class.forName(type);
        }
        catch (final ClassNotFoundException e) {
            throw new RuntimeException("Missing type from the class path expected by network: " + type, e);
        }
    }
    
    private static int idFromType(final EncodedType type) {
        return type.ordinal();
    }
    
    private static EncodedType typeFromId(final int id) {
        if (id < 0 || id >= EncodedType.types.length) {
            throw new IllegalArgumentException("invalid type id: " + id);
        }
        return EncodedType.types[id];
    }
    
    private static EncodedType typeFromObject(final Object o) {
        if (o == null) {
            return EncodedType.Null;
        }
        return typeFromClass(o.getClass());
    }
    
    private static EncodedType typeFromClass(Class<?> cls) {
        if (cls == null) {
            return EncodedType.Null;
        }
        if (cls.isArray()) {
            return EncodedType.Array;
        }
        if (cls.isPrimitive()) {
            cls = box(cls);
        }
        EncodedType ret = EncodedType.classToTypeMap.get(cls);
        if (ret != null) {
            return ret;
        }
        ret = DataEncoder.classToTypeCache.get(cls);
        if (ret != null) {
            return ret;
        }
        final INetworkCustomEncoder ince = DataEncoder.classToAddonType.get(cls);
        if (ince != null) {
            ret = (ince.isThreadSafe() ? EncodedType.Addon : EncodedType.UnSafeAddon);
            DataEncoder.classToTypeCache.put(cls, ret);
            return ret;
        }
        for (final EncodedType type : EncodedType.types) {
            if (type.cls != null && type.cls.isAssignableFrom(cls)) {
                DataEncoder.classToTypeCache.put(cls, type);
                return type;
            }
        }
        throw new IllegalStateException("unmatched " + cls);
    }
    
    public static void addNetworkEncoder(final Class<?> typeBeingEncoded, final INetworkCustomEncoder customEncoder) {
        assert typeBeingEncoded != null && customEncoder != null;
        final INetworkCustomEncoder previous = DataEncoder.classToAddonType.put(typeBeingEncoded, customEncoder);
        if (previous != null) {
            throw new IllegalStateException("Duplicate mapping for class! " + previous.getClass().getName() + " and " + customEncoder.getClass().getName() + " both map for " + typeBeingEncoded.getName() + '.');
        }
    }
    
    static {
        classToTypeCache = Collections.synchronizedMap(new IdentityHashMap<Class<?>, EncodedType>());
        classToAddonType = Collections.synchronizedMap(new IdentityHashMap<Class<?>, INetworkCustomEncoder>());
    }
    
    public enum EncodedType
    {
        Null((Class<?>)null), 
        Array((Class<?>)null), 
        Byte((Class<?>)Byte.class), 
        Short((Class<?>)Short.class), 
        Integer((Class<?>)Integer.class), 
        Long((Class<?>)Long.class), 
        Float((Class<?>)Float.class), 
        Double((Class<?>)Double.class), 
        Boolean((Class<?>)Boolean.class), 
        Character((Class<?>)Character.class), 
        String((Class<?>)String.class), 
        Enum((Class<?>)Enum.class), 
        UUID((Class<?>)UUID.class), 
        Block((Class<?>)Block.class), 
        Item((Class<?>)Item.class), 
        TileEntity((Class<?>)TileEntity.class, false), 
        ItemStack((Class<?>)ItemStack.class), 
        World((Class<?>)World.class, false), 
        NBTTagCompound((Class<?>)NBTTagCompound.class), 
        ResourceLocation((Class<?>)ResourceLocation.class), 
        GameProfile((Class<?>)GameProfile.class), 
        Potion((Class<?>)Potion.class), 
        Enchantment((Class<?>)Enchantment.class), 
        BlockPos((Class<?>)BlockPos.class), 
        ChunkPos((Class<?>)ChunkPos.class), 
        Vec3((Class<?>)Vec3d.class), 
        Fluid((Class<?>)Fluid.class), 
        FluidStack((Class<?>)FluidStack.class), 
        FluidTank((Class<?>)FluidTank.class), 
        InvSlot((Class<?>)InvSlot.class), 
        Component((Class<?>)TileEntityComponent.class, false), 
        CropCard((Class<?>)CropCard.class), 
        ElectrolyzerRecipe((Class<?>)IElectrolyzerRecipeManager.ElectrolyzerRecipe.class), 
        TupleT2((Class<?>)Tuple.T2.class), 
        TupleT3((Class<?>)Tuple.T3.class), 
        Addon((Class<?>)null), 
        UnSafeAddon((Class<?>)null, false), 
        Collection((Class<?>)Collection.class), 
        Object((Class<?>)Object.class);
        
        final Class<?> cls;
        final boolean threadSafe;
        static final EncodedType[] types;
        static final Map<Class<?>, EncodedType> classToTypeMap;
        
        private EncodedType(final Class<?> cls) {
            this(cls, true);
        }
        
        private EncodedType(final Class<?> cls, final boolean threadSafe) {
            this.cls = cls;
            this.threadSafe = threadSafe;
        }
        
        static {
            types = values();
            classToTypeMap = new IdentityHashMap<Class<?>, EncodedType>(EncodedType.types.length - 2);
            for (final EncodedType type : EncodedType.types) {
                if (type.cls != null) {
                    EncodedType.classToTypeMap.put(type.cls, type);
                }
            }
            if (EncodedType.types.length > 255) {
                throw new RuntimeException("too many types");
            }
        }
    }
    
    private interface IResolvableValue<T>
    {
        T get();
    }
}
