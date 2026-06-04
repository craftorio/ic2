// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.util.Collection;
import java.util.Arrays;
import java.util.EnumSet;
import net.minecraft.init.Items;
import ic2.core.Ic2Player;
import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import java.util.Collections;
import java.util.IdentityHashMap;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.FakePlayer;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import java.lang.reflect.Method;
import ic2.core.IC2;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import java.util.Set;
import java.lang.reflect.Field;
import net.minecraft.world.IBlockAccess;
import java.util.Map;

public final class Util
{
    private static final Map<Class<? extends IBlockAccess>, Field> worldFieldCache;
    public static Set<EnumFacing> noFacings;
    public static Set<EnumFacing> onlyNorth;
    public static Set<EnumFacing> horizontalFacings;
    public static Set<EnumFacing> verticalFacings;
    public static Set<EnumFacing> downSideFacings;
    public static Set<EnumFacing> allFacings;
    private static final boolean inDev;
    private static final boolean includeWorldHash;
    private static final Map<Class<?>, Boolean> checkedClasses;
    
    public static int roundToNegInf(final float x) {
        int ret = (int)x;
        if (ret > x) {
            --ret;
        }
        return ret;
    }
    
    public static int roundToNegInf(final double x) {
        int ret = (int)x;
        if (ret > x) {
            --ret;
        }
        return ret;
    }
    
    public static int saturatedCast(final double x) {
        if (x > 2.147483647E9) {
            return Integer.MAX_VALUE;
        }
        if (x < -2.147483648E9) {
            return Integer.MIN_VALUE;
        }
        return (int)x;
    }
    
    public static int limit(final int value, final int min, final int max) {
        if (value <= min) {
            return min;
        }
        if (value >= max) {
            return max;
        }
        return value;
    }
    
    public static float limit(final float value, final float min, final float max) {
        if (Float.isNaN(value) || value <= min) {
            return min;
        }
        if (value >= max) {
            return max;
        }
        return value;
    }
    
    public static double limit(final double value, final double min, final double max) {
        if (Double.isNaN(value) || value <= min) {
            return min;
        }
        if (value >= max) {
            return max;
        }
        return value;
    }
    
    public static double map(double value, final double srcMax, final double dstMax) {
        if (value < 0.0 || Double.isNaN(value)) {
            value = 0.0;
        }
        if (value > srcMax) {
            value = srcMax;
        }
        return value / srcMax * dstMax;
    }
    
    public static double lerp(final double start, final double end, final double fraction) {
        assert fraction >= 0.0 && fraction <= 1.0;
        return start + (end - start) * fraction;
    }
    
    public static float lerp(final float start, final float end, final float fraction) {
        assert fraction >= 0.0f && fraction <= 1.0f;
        return start + (end - start) * fraction;
    }
    
    public static int square(final int x) {
        return x * x;
    }
    
    public static float square(final float x) {
        return x * x;
    }
    
    public static double square(final double x) {
        return x * x;
    }
    
    public static boolean isSimilar(final float a, final float b) {
        return Math.abs(a - b) < 1.0E-5f;
    }
    
    public static boolean isSimilar(final double a, final double b) {
        return Math.abs(a - b) < 1.0E-5;
    }
    
    public static int countInArray(final Object[] oa, final Class<?>... clsz) {
        int ret = 0;
        for (final Object o : oa) {
            for (final Class<?> cls : clsz) {
                if (cls.isAssignableFrom(o.getClass())) {
                    ++ret;
                }
            }
        }
        return ret;
    }
    
    public static int countInArray(final Object[] oa, final Class<?> cls) {
        int ret = 0;
        for (final Object o : oa) {
            if (cls.isAssignableFrom(o.getClass())) {
                ++ret;
            }
        }
        return ret;
    }
    
    public static boolean inDev() {
        return Util.inDev;
    }
    
    public static boolean hasAssertions() {
        boolean ret = false;
        assert ret = true;
        return ret;
    }
    
    public static boolean matchesOD(final ItemStack stack, final Object match) {
        if (match instanceof ItemStack) {
            return !StackUtil.isEmpty(stack) && stack.isItemEqual((ItemStack)match);
        }
        if (!(match instanceof String)) {
            return stack == match;
        }
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        for (final int oreId : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(oreId).equals(match)) {
                return true;
            }
        }
        return false;
    }
    
    public static String toString(final TileEntity te) {
        if (te == null) {
            return "null";
        }
        return toString(te, (IBlockAccess)te.getWorld(), te.getPos());
    }
    
    public static String toString(final Object o, final IBlockAccess world, final BlockPos pos) {
        return toString(o, world, pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static String toString(final Object o, final IBlockAccess world, final int x, final int y, final int z) {
        final StringBuilder ret = new StringBuilder(64);
        if (o == null) {
            ret.append("null");
        }
        else {
            ret.append(o.getClass().getName());
            ret.append('@');
            ret.append(Integer.toHexString(System.identityHashCode(o)));
        }
        ret.append(" (");
        ret.append(formatPosition(world, x, y, z));
        ret.append(")");
        return ret.toString();
    }
    
    public static String formatPosition(final TileEntity te) {
        return formatPosition((IBlockAccess)te.getWorld(), te.getPos());
    }
    
    public static String formatPosition(final IBlockAccess world, final BlockPos pos) {
        return formatPosition(world, pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static String formatPosition(final IBlockAccess world, final int x, final int y, final int z) {
        int dimId;
        if (world instanceof World && ((World)world).provider != null) {
            dimId = ((World)world).provider.getDimension();
        }
        else {
            dimId = Integer.MIN_VALUE;
        }
        if (!Util.includeWorldHash) {
            return formatPosition(dimId, x, y, z);
        }
        return String.format("dim %d (@%x): %d/%d/%d", dimId, System.identityHashCode(world), x, y, z);
    }
    
    public static String formatPosition(final int dimId, final int x, final int y, final int z) {
        return "dim " + dimId + ": " + x + "/" + y + "/" + z;
    }
    
    public static String formatPosition(final BlockPos pos) {
        return formatPosition(pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static String formatPosition(final int x, final int y, final int z) {
        return x + "/" + y + "/" + z;
    }
    
    public static String toSiString(double value, final int digits) {
        if (value == 0.0) {
            return "0 ";
        }
        if (Double.isNaN(value)) {
            return "NaN ";
        }
        String ret = "";
        if (value < 0.0) {
            ret = "-";
            value = -value;
        }
        if (Double.isInfinite(value)) {
            return ret + "\u221e ";
        }
        final double log = Math.log10(value);
        double mul;
        String si = null;
        if (log >= 0.0) {
            final int reduce = (int)Math.floor(log / 3.0);
            mul = 1.0 / Math.pow(10.0, reduce * 3);
            switch (reduce) {
                case 0: {
                    si = "";
                    break;
                }
                case 1: {
                    si = "k";
                    break;
                }
                case 2: {
                    si = "M";
                    break;
                }
                case 3: {
                    si = "G";
                    break;
                }
                case 4: {
                    si = "T";
                    break;
                }
                case 5: {
                    si = "P";
                    break;
                }
                case 6: {
                    si = "E";
                    break;
                }
                case 7: {
                    si = "Z";
                    break;
                }
                case 8: {
                    si = "Y";
                    break;
                }
                default: {
                    si = "E" + reduce * 3;
                    break;
                }
            }
        }
        else {
            final int expand = (int)Math.ceil(-log / 3.0);
            mul = Math.pow(10.0, expand * 3);
            switch (expand) {
                case 0: {
                    si = "";
                    break;
                }
                case 1: {
                    si = "m";
                    break;
                }
                case 2: {
                    si = "?";
                    break;
                }
                case 3: {
                    si = "n";
                    break;
                }
                case 4: {
                    si = "p";
                    break;
                }
                case 5: {
                    si = "f";
                    break;
                }
                case 6: {
                    si = "a";
                    break;
                }
                case 7: {
                    si = "z";
                    break;
                }
                case 8: {
                    si = "y";
                    break;
                }
                default: {
                    si = "E-" + expand * 3;
                    break;
                }
            }
        }
        value *= mul;
        int iVal = (int)Math.floor(value);
        value -= iVal;
        int iDigits = 1;
        if (iVal > 0) {
            iDigits += (int)Math.floor(Math.log10(iVal));
        }
        mul = Math.pow(10.0, digits - iDigits);
        int dVal = (int)Math.round(value * mul);
        if (dVal >= mul) {
            ++iVal;
            dVal -= (int)mul;
            iDigits = 1;
            if (iVal > 0) {
                iDigits += (int)Math.floor(Math.log10(iVal));
            }
        }
        ret += Integer.toString(iVal);
        if (digits > iDigits && dVal != 0) {
            ret += String.format(".%0" + (digits - iDigits) + "d", dVal);
        }
        ret = ret.replaceFirst("(\\.\\d*?)0+$", "$1");
        return ret + " " + si;
    }
    
    public static void exit(final int status) {
        Method exit = null;
        try {
            exit = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", Integer.TYPE);
            exit.setAccessible(true);
        }
        catch (final Exception e) {
            IC2.log.warn(LogCategory.General, e, "Method lookup failed.");
            try {
                final Field security = System.class.getDeclaredField("security");
                security.setAccessible(true);
                security.set(null, null);
                exit = System.class.getMethod("exit", Integer.TYPE);
            }
            catch (final Exception f) {
                throw new Error(f);
            }
        }
        try {
            exit.invoke(null, status);
        }
        catch (final Exception e) {
            throw new Error(e);
        }
    }
    
    public static Vector3 getEyePosition(final Entity entity) {
        return new Vector3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
    }
    
    public static Vector3 getLook(final Entity entity) {
        return new Vector3(entity.getLookVec());
    }
    
    public static Vector3 getLookScaled(final Entity entity) {
        return getLook(entity).scale(getReachDistance(entity));
    }
    
    public static double getReachDistance(final Entity entity) {
        if (entity instanceof EntityPlayerMP) {
            return ((EntityPlayerMP)entity).interactionManager.getBlockReachDistance();
        }
        return 5.0;
    }
    
    public static RayTraceResult traceBlocks(final EntityPlayer player, final boolean liquid) {
        return traceBlocks(player, liquid, !liquid, false);
    }
    
    public static RayTraceResult traceBlocks(final EntityPlayer player, final boolean liquid, final boolean ignoreBlockWithoutBoundingBox, final boolean returnLastUncollidableBlock) {
        final Vector3 start = getEyePosition((Entity)player);
        final Vector3 end = getLookScaled((Entity)player).add(start);
        return player.getEntityWorld().rayTraceBlocks(start.toVec3(), end.toVec3(), liquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
    }
    
    public static RayTraceResult traceEntities(final EntityPlayer player, final boolean alwaysCollide) {
        final Vector3 start = getEyePosition((Entity)player);
        return traceEntities(player.getEntityWorld(), start.toVec3(), getLookScaled((Entity)player).add(start).toVec3(), (Entity)player, alwaysCollide);
    }
    
    public static RayTraceResult traceEntities(final EntityPlayer player, final Vec3d end, final boolean alwaysCollide) {
        return traceEntities(player.getEntityWorld(), getEyePosition((Entity)player).toVec3(), end, (Entity)player, alwaysCollide);
    }
    
    public static RayTraceResult traceEntities(final World world, final Vec3d start, final Vec3d end, final Entity exclude, final boolean alwaysCollide) {
        final AxisAlignedBB aabb = new AxisAlignedBB(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.min(start.z, end.z), Math.max(start.x, end.x), Math.max(start.y, end.y), Math.max(start.z, end.z));
        final List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(exclude, aabb);
        RayTraceResult closest = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (final Entity entity : entities) {
            if (!alwaysCollide && !entity.canBeCollidedWith()) {
                continue;
            }
            final RayTraceResult pos = entity.getEntityBoundingBox().calculateIntercept(start, end);
            if (pos == null) {
                continue;
            }
            final double distance = start.squareDistanceTo(pos.hitVec);
            if (distance >= minDist) {
                continue;
            }
            pos.entityHit = entity;
            pos.typeOfHit = RayTraceResult.Type.ENTITY;
            minDist = distance;
            closest = pos;
        }
        return closest;
    }
    
    public static boolean isFakePlayer(final EntityPlayer entity, final boolean fuzzy) {
        if (entity == null) {
            return false;
        }
        if (!(entity instanceof EntityPlayerMP)) {
            return true;
        }
        if (fuzzy) {
            return entity instanceof FakePlayer;
        }
        return entity.getClass() != EntityPlayerMP.class;
    }
    
    public static World getWorld(final IBlockAccess world) {
        if (world == null) {
            return null;
        }
        if (world instanceof World) {
            return (World)world;
        }
        final Class<? extends IBlockAccess> cls = world.getClass();
        Field field;
        synchronized (Util.worldFieldCache) {
            field = Util.worldFieldCache.get(cls);
            if (field == null && !Util.worldFieldCache.containsKey(cls)) {
                field = ReflectionUtil.getFieldRecursive(world.getClass(), World.class, false);
                Util.worldFieldCache.put(cls, field);
            }
        }
        if (field != null) {
            try {
                return (World)field.get(world);
            }
            catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    
    public static Chunk getLoadedChunk(final World world, final int chunkX, final int chunkZ) {
        Chunk chunk = null;
        if (world.getChunkProvider() instanceof ChunkProviderServer) {
            final ChunkProviderServer cps = (ChunkProviderServer)world.getChunkProvider();
            try {
                chunk = (Chunk)cps.id2ChunkMap.get(ChunkPos.asLong(chunkX, chunkZ));
            }
            catch (final NoSuchFieldError e) {
                if (cps.chunkExists(chunkX, chunkZ)) {
                    chunk = cps.provideChunk(chunkX, chunkZ);
                }
            }
        }
        else {
            chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        }
        if (chunk instanceof EmptyChunk) {
            return null;
        }
        return chunk;
    }
    
    public static boolean checkMcCoordBounds(final int x, final int y, final int z) {
        return checkMcCoordBounds(x, z) && y >= 0 && y < 256;
    }
    
    public static boolean checkMcCoordBounds(final int x, final int z) {
        return x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000;
    }
    
    public static boolean checkInterfaces(final Class<?> cls) {
        final Boolean cached = Util.checkedClasses.get(cls);
        if (cached != null) {
            return cached;
        }
        final Set<Class<?>> interfaces = Collections.newSetFromMap(new IdentityHashMap<Class<?>, Boolean>());
        Class<?> c = cls;
        do {
            for (final Class<?> i : c.getInterfaces()) {
                interfaces.add(i);
            }
            c = c.getSuperclass();
        } while (c != null);
        boolean result = true;
        for (final Class<?> iface : interfaces) {
            for (final Method method : iface.getMethods()) {
                boolean found = false;
                c = cls;
                do {
                    try {
                        final Method match = c.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        if (method.getReturnType().isAssignableFrom(match.getReturnType())) {
                            found = true;
                            break;
                        }
                    }
                    catch (final NoSuchMethodException ex) {}
                    c = c.getSuperclass();
                } while (c != null);
                if (!found) {
                    IC2.log.info(LogCategory.General, "Can't find method %s.%s in %s.", method.getDeclaringClass().getName(), method.getName(), cls.getName());
                    result = false;
                }
            }
        }
        Util.checkedClasses.put(cls, result);
        return result;
    }
    
    public static IBlockState getBlockState(final IBlockAccess world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        return state.getActualState(world, pos);
    }
    
    public static Block getBlock(final String name) {
        if (name == null) {
            throw new NullPointerException("null name");
        }
        return getBlock(new ResourceLocation(name));
    }
    
    public static Block getBlock(final ResourceLocation loc) {
        final Block ret = (Block)Block.REGISTRY.getObject((Object)loc);
        if (ret != Blocks.AIR) {
            return ret;
        }
        if (loc.getResourceDomain().equals("minecraft") && loc.getResourcePath().equals("air")) {
            return ret;
        }
        return null;
    }
    
    public static ResourceLocation getName(final Block block) {
        return (ResourceLocation)Block.REGISTRY.getNameForObject((Object)block);
    }
    
    public static Item getItem(final String name) {
        if (name == null) {
            throw new NullPointerException("null name");
        }
        return getItem(new ResourceLocation(name));
    }
    
    public static Item getItem(final ResourceLocation loc) {
        return (Item)Item.REGISTRY.getObject((Object)loc);
    }
    
    public static ResourceLocation getName(final Item item) {
        return (ResourceLocation)Item.REGISTRY.getNameForObject((Object)item);
    }
    
    public static boolean harvestBlock(final World world, final BlockPos pos) {
        if (world.isRemote) {
            return false;
        }
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        final TileEntity te = world.getTileEntity(pos);
        final EntityPlayer player = Ic2Player.get(world);
        final boolean canHarvest = block.canHarvestBlock((IBlockAccess)world, pos, player);
        block.onBlockHarvested(world, pos, state, player);
        final boolean removed = block.removedByPlayer(state, world, pos, player, canHarvest);
        if (canHarvest && removed) {
            block.harvestBlock(world, player, pos, state, te, new ItemStack(Items.DIAMOND_PICKAXE));
        }
        return removed;
    }
    
    static {
        worldFieldCache = new IdentityHashMap<Class<? extends IBlockAccess>, Field>();
        Util.noFacings = Collections.emptySet();
        Util.onlyNorth = Collections.unmodifiableSet((Set<? extends EnumFacing>)EnumSet.of(EnumFacing.NORTH));
        Util.horizontalFacings = Collections.unmodifiableSet((Set<? extends EnumFacing>)EnumSet.copyOf(Arrays.asList(EnumFacing.HORIZONTALS)));
        Util.verticalFacings = Collections.unmodifiableSet((Set<? extends EnumFacing>)EnumSet.of(EnumFacing.DOWN, EnumFacing.UP));
        Util.downSideFacings = Collections.unmodifiableSet((Set<? extends EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)EnumFacing.UP)));
        Util.allFacings = Collections.unmodifiableSet((Set<? extends EnumFacing>)EnumSet.allOf(EnumFacing.class));
        inDev = (System.getProperty("INDEV") != null);
        includeWorldHash = (System.getProperty("ic2.debug.includeworldhash") != null);
        checkedClasses = new IdentityHashMap<Class<?>, Boolean>();
    }
}
