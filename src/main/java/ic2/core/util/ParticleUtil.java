package ic2.core.util;

import ic2.core.block.ITeBlock;
import ic2.core.block.TeBlockRegistry;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.model.ISpecialParticleModel;
import ic2.core.model.ModelUtil;
import java.lang.reflect.Constructor;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ParticleUtil {
  public static void showFlames(World world, BlockPos pos, EnumFacing facing) {
    if (world.field_73012_v.nextInt(8) != 0)
      return; 
    double width = 0.625D;
    double height = 0.625D;
    double depthOffset = 0.02D;
    double x = pos.getX() + (facing.getFrontOffsetX() * 1.04D + 1.0D) / 2.0D;
    double y = pos.getY() + world.field_73012_v.nextFloat() * 0.625D;
    double z = pos.getZ() + (facing.getFrontOffsetZ() * 1.04D + 1.0D) / 2.0D;
    if (facing.func_176740_k() == EnumFacing.Axis.X) {
      z += world.field_73012_v.nextFloat() * 0.625D - 0.3125D;
    } else {
      x += world.field_73012_v.nextFloat() * 0.625D - 0.3125D;
    } 
    world.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
    world.func_175688_a(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
  }
  
  public static void spawnBlockLandParticles(World world, BlockPos pos, double x, double y, double z, int count, ITeBlock teBlock) {
    Minecraft mc = Minecraft.getMinecraft();
    Random rnd = world.field_73012_v;
    if (mc.field_71441_e != world || mc.player == null)
      return; 
    if (mc.field_71474_y.field_74362_aa > 1 || (mc.field_71474_y.field_74362_aa == 1 && rnd
      .nextInt(3) == 0))
      return; 
    if (mc.player.func_70092_e(x, y, z) > 1024.0D)
      return; 
    double speed = 0.15D;
    IBlockState state = TeBlockRegistry.get(teBlock.getIdentifier()).getState(teBlock);
    for (int i = 0; i < count; i++) {
      double mx = rnd.nextGaussian() * 0.15D;
      double my = rnd.nextGaussian() * 0.15D;
      double mz = rnd.nextGaussian() * 0.15D;
      ParticleBlockDust particleBlockDust = newParticleBlockDust(world, x, y, z, mx, my, mz, state);
      ensureTexture(world, pos, (Particle)particleBlockDust.func_174845_l(), state);
      mc.field_71452_i.func_78873_a((Particle)particleBlockDust);
    } 
  }
  
  public static void spawnBlockRunParticles(World world, BlockPos pos, double x, double y, double z, double xSpeed, double zSpeed, ITeBlock block) {
    IBlockState state = TeBlockRegistry.get(block.getIdentifier()).getState(block);
    ParticleDigging particle = newParticleDigging(world, x, y, z, xSpeed, 1.5D, zSpeed, state);
    ensureTexture(world, pos, (Particle)particle.func_174845_l(), state);
    (Minecraft.getMinecraft()).field_71452_i.func_78873_a((Particle)particle);
  }
  
  public static void spawnBlockHitParticles(TileEntityBlock te, EnumFacing side) {
    spawnBlockHitParticles(te, side, false);
  }
  
  public static void spawnBlockHitParticles(TileEntityBlock te, EnumFacing side, boolean checkTexture) {
    World world = te.getWorld();
    BlockPos pos = te.getPos();
    double offset = 0.1D;
    AxisAlignedBB aabb = te.getVisualBoundingBox();
    double x = pos.getX() + world.field_73012_v.nextDouble() * (aabb.field_72336_d - aabb.field_72340_a - offset * 2.0D) + offset + aabb.field_72340_a;
    double y = pos.getY() + world.field_73012_v.nextDouble() * (aabb.field_72337_e - aabb.field_72338_b - offset * 2.0D) + offset + aabb.field_72338_b;
    double z = pos.getZ() + world.field_73012_v.nextDouble() * (aabb.field_72334_f - aabb.field_72339_c - offset * 2.0D) + offset + aabb.field_72339_c;
    switch (side) {
      case DOWN:
        y = pos.getY() + aabb.field_72338_b - offset;
        break;
      case UP:
        y = pos.getY() + aabb.field_72337_e + offset;
        break;
      case NORTH:
        z = pos.getZ() + aabb.field_72339_c - offset;
        break;
      case SOUTH:
        z = pos.getZ() + aabb.field_72334_f + offset;
        break;
      case WEST:
        x = pos.getX() + aabb.field_72340_a - offset;
        break;
      case EAST:
        x = pos.getX() + aabb.field_72336_d + offset;
        break;
      default:
        throw new IllegalStateException("invalid facing: " + side);
    } 
    ParticleDigging particle = newParticleDigging(world, x, y, z, 0.0D, 0.0D, 0.0D, te.getBlockState());
    particle.func_174846_a(pos);
    particle.func_70543_e(0.2F);
    particle.func_70541_f(0.6F);
    if (checkTexture)
      ensureTexture(world, pos, (Particle)particle, te.getBlockState()); 
    (Minecraft.getMinecraft()).field_71452_i.func_78873_a((Particle)particle);
  }
  
  public static void spawnBlockBreakParticles(TileEntityBlock te) {
    World world = te.getWorld();
    BlockPos pos = te.getPos();
    IBlockState state = te.getBlockState();
    Minecraft mc = Minecraft.getMinecraft();
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 4; y++) {
        for (int z = 0; z < 4; z++) {
          double xOffset = (x + 0.5D) / 4.0D;
          double yOffset = (y + 0.5D) / 4.0D;
          double zOffset = (z + 0.5D) / 4.0D;
          ParticleDigging particle = newParticleDigging(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, xOffset - 0.5D, yOffset - 0.5D, zOffset - 0.5D, state);
          particle.func_174846_a(pos);
          ensureTexture(world, pos, (Particle)particle, state);
          mc.field_71452_i.func_78873_a((Particle)particle);
        } 
      } 
    } 
  }
  
  public static ParticleBlockDust newParticleBlockDust(World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, IBlockState state) {
    try {
      return particleBlockDust_ctor.newInstance(new Object[] { world, Double.valueOf(xCoord), Double.valueOf(yCoord), Double.valueOf(zCoord), Double.valueOf(xSpeed), Double.valueOf(ySpeed), Double.valueOf(zSpeed), state });
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static ParticleDigging newParticleDigging(World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, IBlockState state) {
    try {
      return particleDigging_ctor.newInstance(new Object[] { world, Double.valueOf(xCoord), Double.valueOf(yCoord), Double.valueOf(zCoord), Double.valueOf(xSpeed), Double.valueOf(ySpeed), Double.valueOf(zSpeed), state });
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static void ensureTexture(World world, BlockPos pos, Particle particle, IBlockState state) {
    if (pos == null)
      return; 
    IBakedModel model = ModelUtil.getBlockModel(state);
    if (model instanceof ISpecialParticleModel && ((ISpecialParticleModel)model).needsEnhancing(state)) {
      state = state.func_185899_b((IBlockAccess)world, pos);
      state = state.getBlock().getExtendedState(state, (IBlockAccess)world, pos);
      assert state instanceof Ic2BlockState.Ic2BlockStateInstance;
      ((ISpecialParticleModel)model).enhanceParticle(particle, (Ic2BlockState.Ic2BlockStateInstance)state);
    } 
  }
  
  private static Constructor<ParticleBlockDust> getParticleBlockDustCtor() {
    try {
      Constructor<ParticleBlockDust> ret = ParticleBlockDust.class.getDeclaredConstructor(new Class[] { World.class, double.class, double.class, double.class, double.class, double.class, double.class, IBlockState.class });
      ret.setAccessible(true);
      return ret;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static Constructor<ParticleDigging> getParticleDiggingCtor() {
    try {
      Constructor<ParticleDigging> ret = ParticleDigging.class.getDeclaredConstructor(new Class[] { World.class, double.class, double.class, double.class, double.class, double.class, double.class, IBlockState.class });
      ret.setAccessible(true);
      return ret;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static final Constructor<ParticleBlockDust> particleBlockDust_ctor = getParticleBlockDustCtor();
  
  private static final Constructor<ParticleDigging> particleDigging_ctor = getParticleDiggingCtor();
}
