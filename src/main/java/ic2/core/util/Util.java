package ic2.core.util;

import ic2.core.IC2;
import ic2.core.Ic2Player;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.oredict.OreDictionary;

public final class Util {
   private static final Map<Class<? extends IBlockAccess>, Field> worldFieldCache = new IdentityHashMap<>();
   public static Set<EnumFacing> noFacings = Collections.emptySet();
   public static Set<EnumFacing> onlyNorth = Collections.unmodifiableSet(EnumSet.of(EnumFacing.NORTH));
   public static Set<EnumFacing> horizontalFacings = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(EnumFacing.HORIZONTALS)));
   public static Set<EnumFacing> verticalFacings = Collections.unmodifiableSet(EnumSet.of(EnumFacing.DOWN, EnumFacing.UP));
   public static Set<EnumFacing> downSideFacings = Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(EnumFacing.UP)));
   public static Set<EnumFacing> allFacings = Collections.unmodifiableSet(EnumSet.allOf(EnumFacing.class));
   private static final boolean inDev = System.getProperty("INDEV") != null;
   private static final boolean includeWorldHash = System.getProperty("ic2.debug.includeworldhash") != null;
   private static final Map<Class<?>, Boolean> checkedClasses = new IdentityHashMap<>();

   public static int roundToNegInf(float x) {
      int ret = (int)x;
      if (ret > x) {
         ret--;
      }

      return ret;
   }

   public static int roundToNegInf(double x) {
      int ret = (int)x;
      if (ret > x) {
         ret--;
      }

      return ret;
   }

   public static int saturatedCast(double x) {
      if (x > 2.147483647E9) {
         return Integer.MAX_VALUE;
      } else {
         return x < -2.1474836E9F ? Integer.MIN_VALUE : (int)x;
      }
   }

   public static int limit(int value, int min, int max) {
      if (value <= min) {
         return min;
      } else {
         return value >= max ? max : value;
      }
   }

   public static float limit(float value, float min, float max) {
      if (Float.isNaN(value) || value <= min) {
         return min;
      } else {
         return value >= max ? max : value;
      }
   }

   public static double limit(double value, double min, double max) {
      if (Double.isNaN(value) || value <= min) {
         return min;
      } else {
         return value >= max ? max : value;
      }
   }

   public static double map(double value, double srcMax, double dstMax) {
      if (value < 0.0 || Double.isNaN(value)) {
         value = 0.0;
      }

      if (value > srcMax) {
         value = srcMax;
      }

      return value / srcMax * dstMax;
   }

   public static double lerp(double start, double end, double fraction) {
      assert fraction >= 0.0 && fraction <= 1.0;
      return start + (end - start) * fraction;
   }

   public static float lerp(float start, float end, float fraction) {
      assert fraction >= 0.0F && fraction <= 1.0F;
      return start + (end - start) * fraction;
   }

   public static int square(int x) {
      return x * x;
   }

   public static float square(float x) {
      return x * x;
   }

   public static double square(double x) {
      return x * x;
   }

   public static boolean isSimilar(float a, float b) {
      return Math.abs(a - b) < 1.0E-5F;
   }

   public static boolean isSimilar(double a, double b) {
      return Math.abs(a - b) < 1.0E-5;
   }

   public static int countInArray(Object[] oa, Class<?>... clsz) {
      int ret = 0;

      for (Object o : oa) {
         for (Class<?> cls : clsz) {
            if (cls.isAssignableFrom(o.getClass())) {
               ret++;
            }
         }
      }

      return ret;
   }

   public static int countInArray(Object[] oa, Class<?> cls) {
      int ret = 0;

      for (Object o : oa) {
         if (cls.isAssignableFrom(o.getClass())) {
            ret++;
         }
      }

      return ret;
   }

   public static boolean inDev() {
      return inDev;
   }

   public static boolean hasAssertions() {
      boolean ret = false;
      assert ret = true;
      return ret;
   }

   public static boolean matchesOD(ItemStack stack, Object match) {
      if (!(match instanceof ItemStack)) {
         if (match instanceof String) {
            if (StackUtil.isEmpty(stack)) {
               return false;
            }

            for (int oreId : OreDictionary.getOreIDs(stack)) {
               if (OreDictionary.getOreName(oreId).equals(match)) {
                  return true;
               }
            }

            return false;
         } else {
            return stack == match;
         }
      } else {
         return !StackUtil.isEmpty(stack) && stack.isItemEqual((ItemStack)match);
      }
   }

   public static String toString(TileEntity te) {
      return te == null ? "null" : toString(te, te.getWorld(), te.getPos());
   }

   public static String toString(Object o, IBlockAccess world, BlockPos pos) {
      return toString(o, world, pos.getX(), pos.getY(), pos.getZ());
   }

   public static String toString(Object o, IBlockAccess world, int x, int y, int z) {
      StringBuilder ret = new StringBuilder(64);
      if (o == null) {
         ret.append("null");
      } else {
         ret.append(o.getClass().getName());
         ret.append('@');
         ret.append(Integer.toHexString(System.identityHashCode(o)));
      }

      ret.append(" (");
      ret.append(formatPosition(world, x, y, z));
      ret.append(")");
      return ret.toString();
   }

   public static String formatPosition(TileEntity te) {
      return formatPosition(te.getWorld(), te.getPos());
   }

   public static String formatPosition(IBlockAccess world, BlockPos pos) {
      return formatPosition(world, pos.getX(), pos.getY(), pos.getZ());
   }

   public static String formatPosition(IBlockAccess world, int x, int y, int z) {
      int dimId;
      if (world instanceof World && ((World)world).provider != null) {
         dimId = ((World)world).provider.getDimension();
      } else {
         dimId = Integer.MIN_VALUE;
      }

      return !includeWorldHash ? formatPosition(dimId, x, y, z) : String.format("dim %d (@%x): %d/%d/%d", dimId, System.identityHashCode(world), x, y, z);
   }

   public static String formatPosition(int dimId, int x, int y, int z) {
      return "dim " + dimId + ": " + x + "/" + y + "/" + z;
   }

   public static String formatPosition(BlockPos pos) {
      return formatPosition(pos.getX(), pos.getY(), pos.getZ());
   }

   public static String formatPosition(int x, int y, int z) {
      return x + "/" + y + "/" + z;
   }

   public static String toSiString(double value, int digits) {
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
         return ret + "∞ ";
      }

      double log = Math.log10(value);
      double mul;
      String si;
      if (log >= 0.0) {
         int reduce = (int)Math.floor(log / 3.0);
         mul = 1.0 / Math.pow(10.0, reduce * 3);
         switch (reduce) {
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
      } else {
         int expand = (int)Math.ceil(-log / 3.0);
         mul = Math.pow(10.0, expand * 3);
         switch (expand) {
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
      int iVal = (int)Math.floor(value);
      value -= iVal;
      int iDigits = 1;
      if (iVal > 0) {
         iDigits = (int)(iDigits + Math.floor(Math.log10(iVal)));
      }

      mul = Math.pow(10.0, digits - iDigits);
      int dVal = (int)Math.round(value * mul);
      if (dVal >= mul) {
         iVal++;
         dVal = (int)(dVal - mul);
         iDigits = 1;
         if (iVal > 0) {
            iDigits = (int)(iDigits + Math.floor(Math.log10(iVal)));
         }
      }

      ret = ret + Integer.toString(iVal);
      if (digits > iDigits && dVal != 0) {
         ret = ret + String.format(".%0" + (digits - iDigits) + "d", dVal);
      }

      ret = ret.replaceFirst("(\\.\\d*?)0+$", "$1");
      return ret + " " + si;
   }

   public static void exit(int status) {
      Method exit = null;

      try {
         exit = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", int.class);
         exit.setAccessible(true);
      } catch (Exception e) {
         IC2.log.warn(LogCategory.General, e, "Method lookup failed.");

         try {
            Field security = System.class.getDeclaredField("security");
            security.setAccessible(true);
            security.set(null, null);
            exit = System.class.getMethod("exit", int.class);
         } catch (Exception f) {
            throw new Error(f);
         }
      }

      try {
         exit.invoke(null, status);
      } catch (Exception e) {
         throw new Error(e);
      }
   }

   public static Vector3 getEyePosition(Entity entity) {
      return new Vector3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
   }

   public static Vector3 getLook(Entity entity) {
      return new Vector3(entity.getLookVec());
   }

   public static Vector3 getLookScaled(Entity entity) {
      return getLook(entity).scale(getReachDistance(entity));
   }

   public static double getReachDistance(Entity entity) {
      return entity instanceof EntityPlayerMP ? ((EntityPlayerMP)entity).interactionManager.getBlockReachDistance() : 5.0;
   }

   public static RayTraceResult traceBlocks(EntityPlayer player, boolean liquid) {
      return traceBlocks(player, liquid, !liquid, false);
   }

   public static RayTraceResult traceBlocks(EntityPlayer player, boolean liquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
      Vector3 start = getEyePosition(player);
      Vector3 end = getLookScaled(player).add(start);
      return player.getEntityWorld().rayTraceBlocks(start.toVec3(), end.toVec3(), liquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
   }

   public static RayTraceResult traceEntities(EntityPlayer player, boolean alwaysCollide) {
      Vector3 start = getEyePosition(player);
      return traceEntities(player.getEntityWorld(), start.toVec3(), getLookScaled(player).add(start).toVec3(), player, alwaysCollide);
   }

   public static RayTraceResult traceEntities(EntityPlayer player, Vec3d end, boolean alwaysCollide) {
      return traceEntities(player.getEntityWorld(), getEyePosition(player).toVec3(), end, player, alwaysCollide);
   }

   public static RayTraceResult traceEntities(World world, Vec3d start, Vec3d end, Entity exclude, boolean alwaysCollide) {
      AxisAlignedBB aabb = new AxisAlignedBB(
         Math.min(start.x, end.x),
         Math.min(start.y, end.y),
         Math.min(start.z, end.z),
         Math.max(start.x, end.x),
         Math.max(start.y, end.y),
         Math.max(start.z, end.z)
      );
      List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(exclude, aabb);
      RayTraceResult closest = null;
      double minDist = Double.POSITIVE_INFINITY;

      for (Entity entity : entities) {
         if (alwaysCollide || entity.canBeCollidedWith()) {
            RayTraceResult pos = entity.getEntityBoundingBox().calculateIntercept(start, end);
            if (pos != null) {
               double distance = start.squareDistanceTo(pos.hitVec);
               if (distance < minDist) {
                  pos.entityHit = entity;
                  pos.typeOfHit = Type.ENTITY;
                  minDist = distance;
                  closest = pos;
               }
            }
         }
      }

      return closest;
   }

   public static boolean isFakePlayer(EntityPlayer entity, boolean fuzzy) {
      if (entity == null) {
         return false;
      } else if (!(entity instanceof EntityPlayerMP)) {
         return true;
      } else {
         return fuzzy ? entity instanceof FakePlayer : entity.getClass() != EntityPlayerMP.class;
      }
   }

   public static World getWorld(IBlockAccess world) {
      if (world == null) {
         return null;
      }

      if (world instanceof World) {
         return (World)world;
      }

      Class<? extends IBlockAccess> cls = (Class<? extends IBlockAccess>)world.getClass();
      Field field;
      synchronized (worldFieldCache) {
         field = worldFieldCache.get(cls);
         if (field == null && !worldFieldCache.containsKey(cls)) {
            field = ReflectionUtil.getFieldRecursive(world.getClass(), World.class, false);
            worldFieldCache.put(cls, field);
         }
      }

      if (field != null) {
         try {
            return (World)field.get(world);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      } else {
         return null;
      }
   }

   public static Chunk getLoadedChunk(World world, int chunkX, int chunkZ) {
      Chunk chunk = null;
      if (world.getChunkProvider() instanceof ChunkProviderServer) {
         ChunkProviderServer cps = (ChunkProviderServer)world.getChunkProvider();

         try {
            chunk = (Chunk)cps.id2ChunkMap.get(ChunkPos.asLong(chunkX, chunkZ));
         } catch (NoSuchFieldError e) {
            if (cps.chunkExists(chunkX, chunkZ)) {
               chunk = cps.provideChunk(chunkX, chunkZ);
            }
         }
      } else {
         chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
      }

      return chunk instanceof EmptyChunk ? null : chunk;
   }

   public static boolean checkMcCoordBounds(int x, int y, int z) {
      return checkMcCoordBounds(x, z) && y >= 0 && y < 256;
   }

   public static boolean checkMcCoordBounds(int x, int z) {
      return x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000;
   }

   public static boolean checkInterfaces(Class<?> cls) {
      Boolean cached = checkedClasses.get(cls);
      if (cached != null) {
         return cached;
      }

      Set<Class<?>> interfaces = Collections.newSetFromMap(new IdentityHashMap<>());
      Class<?> c = cls;

      do {
         for (Class<?> i : c.getInterfaces()) {
            interfaces.add(i);
         }

         c = c.getSuperclass();
      } while (c != null);

      boolean result = true;

      for (Class<?> iface : interfaces) {
         for (Method method : iface.getMethods()) {
            boolean found = false;
            c = cls;

            do {
               try {
                  Method match = c.getDeclaredMethod(method.getName(), method.getParameterTypes());
                  if (method.getReturnType().isAssignableFrom(match.getReturnType())) {
                     found = true;
                     break;
                  }
               } catch (NoSuchMethodException var13) {
               }

               c = c.getSuperclass();
            } while (c != null);

            if (!found) {
               IC2.log.info(LogCategory.General, "Can't find method %s.%s in %s.", method.getDeclaringClass().getName(), method.getName(), cls.getName());
               result = false;
            }
         }
      }

      checkedClasses.put(cls, result);
      return result;
   }

   public static IBlockState getBlockState(IBlockAccess world, BlockPos pos) {
      IBlockState state = world.getBlockState(pos);
      return state.getActualState(world, pos);
   }

   public static Block getBlock(String name) {
      if (name == null) {
         throw new NullPointerException("null name");
      } else {
         return getBlock(new ResourceLocation(name));
      }
   }

   public static Block getBlock(ResourceLocation loc) {
      Block ret = (Block)Block.REGISTRY.getObject(loc);
      if (ret != Blocks.AIR) {
         return ret;
      } else {
         return loc.getResourceDomain().equals("minecraft") && loc.getResourcePath().equals("air") ? ret : null;
      }
   }

   public static ResourceLocation getName(Block block) {
      return (ResourceLocation)Block.REGISTRY.getNameForObject(block);
   }

   public static Item getItem(String name) {
      if (name == null) {
         throw new NullPointerException("null name");
      } else {
         return getItem(new ResourceLocation(name));
      }
   }

   public static Item getItem(ResourceLocation loc) {
      return (Item)Item.REGISTRY.getObject(loc);
   }

   public static ResourceLocation getName(Item item) {
      return (ResourceLocation)Item.REGISTRY.getNameForObject(item);
   }

   public static boolean harvestBlock(World world, BlockPos pos) {
      if (world.isRemote) {
         return false;
      }

      IBlockState state = world.getBlockState(pos);
      Block block = state.getBlock();
      TileEntity te = world.getTileEntity(pos);
      EntityPlayer player = Ic2Player.get(world);
      boolean canHarvest = block.canHarvestBlock(world, pos, player);
      block.onBlockHarvested(world, pos, state, player);
      boolean removed = block.removedByPlayer(state, world, pos, player, canHarvest);
      if (canHarvest && removed) {
         block.harvestBlock(world, player, pos, state, te, new ItemStack(Items.DIAMOND_PICKAXE));
      }

      return removed;
   }
}
