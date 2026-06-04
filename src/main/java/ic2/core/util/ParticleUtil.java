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
    if (world.rand.nextInt(8) != 0)
      return; 
    double width = 0.625D;
    double height = 0.625D;
    double depthOffset = 0.02D;
    double x = pos.getX() + (facing.getFrontOffsetX() * 1.04D + 1.0D) / 2.0D;
    double y = pos.getY() + world.rand.nextFloat() * 0.625D;
    double z = pos.getZ() + (facing.getFrontOffsetZ() * 1.04D + 1.0D) / 2.0D;
    if (facing.getAxis() == EnumFacing.Axis.X) {
      z += world.rand.nextFloat() * 0.625D - 0.3125D;
    } else {
      x += world.rand.nextFloat() * 0.625D - 0.3125D;
    } 
    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
    world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
  }
  
  public static void spawnBlockLandParticles(World world, BlockPos pos, double x, double y, double z, int count, ITeBlock teBlock) {
    Minecraft mc = Minecraft.getMinecraft();
    Random rnd = world.rand;
    if (mc.world != world || mc.player == null)
      return; 
    if (mc.gameSettings.particleSetting > 1 || (mc.gameSettings.particleSetting == 1 && rnd
      .nextInt(3) == 0))
      return; 
    if (mc.player.getDistanceSq(x, y, z) > 1024.0D)
      return; 
    double speed = 0.15D;
    IBlockState state = TeBlockRegistry.get(teBlock.getIdentifier()).getState(teBlock);
    for (int i = 0; i < count; i++) {
      double mx = rnd.nextGaussian() * 0.15D;
      double my = rnd.nextGaussian() * 0.15D;
      double mz = rnd.nextGaussian() * 0.15D;
      ParticleBlockDust particleBlockDust = newParticleBlockDust(world, x, y, z, mx, my, mz, state);
      ensureTexture(world, pos, (Particle)particleBlockDust.init(), state);
      mc.effectRenderer.addEffect((Particle)particleBlockDust);
    } 
  }
  
  public static void spawnBlockRunParticles(World world, BlockPos pos, double x, double y, double z, double xSpeed, double zSpeed, ITeBlock block) {
    IBlockState state = TeBlockRegistry.get(block.getIdentifier()).getState(block);
    ParticleDigging particle = newParticleDigging(world, x, y, z, xSpeed, 1.5D, zSpeed, state);
    ensureTexture(world, pos, (Particle)particle.init(), state);
    (Minecraft.getMinecraft()).effectRenderer.addEffect((Particle)particle);
  }
  
  public static void spawnBlockHitParticles(TileEntityBlock te, EnumFacing side) {
    spawnBlockHitParticles(te, side, false);
  }
  
  public static void spawnBlockHitParticles(TileEntityBlock te, EnumFacing side, boolean checkTexture) {
    World world = te.getWorld();
    BlockPos pos = te.getPos();
    double offset = 0.1D;
    AxisAlignedBB aabb = te.getVisualBoundingBox();
    double x = pos.getX() + world.rand.nextDouble() * (aabb.maxX - aabb.minX - offset * 2.0D) + offset + aabb.minX;
    double y = pos.getY() + world.rand.nextDouble() * (aabb.maxY - aabb.minY - offset * 2.0D) + offset + aabb.minY;
    double z = pos.getZ() + world.rand.nextDouble() * (aabb.maxZ - aabb.minZ - offset * 2.0D) + offset + aabb.minZ;
    switch (side) {
      case DOWN:
        y = pos.getY() + aabb.minY - offset;
        break;
      case UP:
        y = pos.getY() + aabb.maxY + offset;
        break;
      case NORTH:
        z = pos.getZ() + aabb.minZ - offset;
        break;
      case SOUTH:
        z = pos.getZ() + aabb.maxZ + offset;
        break;
      case WEST:
        x = pos.getX() + aabb.minX - offset;
        break;
      case EAST:
        x = pos.getX() + aabb.maxX + offset;
        break;
      default:
        throw new IllegalStateException("invalid facing: " + side);
    } 
    ParticleDigging particle = newParticleDigging(world, x, y, z, 0.0D, 0.0D, 0.0D, te.getBlockState());
    particle.setBlockPos(pos);
    particle.multiplyVelocity(0.2F);
    particle.multipleParticleScaleBy(0.6F);
    if (checkTexture)
      ensureTexture(world, pos, (Particle)particle, te.getBlockState()); 
    (Minecraft.getMinecraft()).effectRenderer.addEffect((Particle)particle);
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
          particle.setBlockPos(pos);
          ensureTexture(world, pos, (Particle)particle, state);
          mc.effectRenderer.addEffect((Particle)particle);
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
      state = state.getActualState((IBlockAccess)world, pos);
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
