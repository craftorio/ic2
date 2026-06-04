// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.client.renderer.block.model.IBakedModel;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.world.IBlockAccess;
import ic2.core.model.ISpecialParticleModel;
import ic2.core.model.ModelUtil;
import net.minecraft.util.math.AxisAlignedBB;
import ic2.core.block.TileEntityBlock;
import net.minecraft.block.state.IBlockState;
import java.util.Random;
import net.minecraft.client.particle.Particle;
import ic2.core.block.TeBlockRegistry;
import net.minecraft.client.Minecraft;
import ic2.core.block.ITeBlock;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleBlockDust;
import java.lang.reflect.Constructor;

public class ParticleUtil
{
    private static final Constructor<ParticleBlockDust> particleBlockDust_ctor;
    private static final Constructor<ParticleDigging> particleDigging_ctor;
    
    public static void showFlames(final World world, final BlockPos pos, final EnumFacing facing) {
        if (world.rand.nextInt(8) != 0) {
            return;
        }
        final double width = 0.625;
        final double height = 0.625;
        final double depthOffset = 0.02;
        double x = pos.getX() + (facing.getFrontOffsetX() * 1.04 + 1.0) / 2.0;
        final double y = pos.getY() + world.rand.nextFloat() * 0.625;
        double z = pos.getZ() + (facing.getFrontOffsetZ() * 1.04 + 1.0) / 2.0;
        if (facing.getAxis() == EnumFacing.Axis.X) {
            z += world.rand.nextFloat() * 0.625 - 0.3125;
        }
        else {
            x += world.rand.nextFloat() * 0.625 - 0.3125;
        }
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0, 0.0, 0.0, new int[0]);
        world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0, new int[0]);
    }
    
    public static void spawnBlockLandParticles(final World world, final BlockPos pos, final double x, final double y, final double z, final int count, final ITeBlock teBlock) {
        final Minecraft mc = Minecraft.getMinecraft();
        final Random rnd = world.rand;
        if (mc.world != world || mc.player == null) {
            return;
        }
        if (mc.gameSettings.particleSetting > 1 || (mc.gameSettings.particleSetting == 1 && rnd.nextInt(3) == 0)) {
            return;
        }
        if (mc.player.getDistanceSq(x, y, z) > 1024.0) {
            return;
        }
        final double speed = 0.15;
        final IBlockState state = TeBlockRegistry.get(teBlock.getIdentifier()).getState(teBlock);
        for (int i = 0; i < count; ++i) {
            final double mx = rnd.nextGaussian() * 0.15;
            final double my = rnd.nextGaussian() * 0.15;
            final double mz = rnd.nextGaussian() * 0.15;
            final ParticleDigging particle = (ParticleDigging)newParticleBlockDust(world, x, y, z, mx, my, mz, state);
            ensureTexture(world, pos, (Particle)particle.init(), state);
            mc.effectRenderer.addEffect((Particle)particle);
        }
    }
    
    public static void spawnBlockRunParticles(final World world, final BlockPos pos, final double x, final double y, final double z, final double xSpeed, final double zSpeed, final ITeBlock block) {
        final IBlockState state = TeBlockRegistry.get(block.getIdentifier()).getState(block);
        final ParticleDigging particle = newParticleDigging(world, x, y, z, xSpeed, 1.5, zSpeed, state);
        ensureTexture(world, pos, (Particle)particle.init(), state);
        Minecraft.getMinecraft().effectRenderer.addEffect((Particle)particle);
    }
    
    public static void spawnBlockHitParticles(final TileEntityBlock te, final EnumFacing side) {
        spawnBlockHitParticles(te, side, false);
    }
    
    public static void spawnBlockHitParticles(final TileEntityBlock te, final EnumFacing side, final boolean checkTexture) {
        final World world = te.getWorld();
        final BlockPos pos = te.getPos();
        final double offset = 0.1;
        final AxisAlignedBB aabb = te.getVisualBoundingBox();
        double x = pos.getX() + world.rand.nextDouble() * (aabb.maxX - aabb.minX - offset * 2.0) + offset + aabb.minX;
        double y = pos.getY() + world.rand.nextDouble() * (aabb.maxY - aabb.minY - offset * 2.0) + offset + aabb.minY;
        double z = pos.getZ() + world.rand.nextDouble() * (aabb.maxZ - aabb.minZ - offset * 2.0) + offset + aabb.minZ;
        switch (side) {
            case DOWN: {
                y = pos.getY() + aabb.minY - offset;
                break;
            }
            case UP: {
                y = pos.getY() + aabb.maxY + offset;
                break;
            }
            case NORTH: {
                z = pos.getZ() + aabb.minZ - offset;
                break;
            }
            case SOUTH: {
                z = pos.getZ() + aabb.maxZ + offset;
                break;
            }
            case WEST: {
                x = pos.getX() + aabb.minX - offset;
                break;
            }
            case EAST: {
                x = pos.getX() + aabb.maxX + offset;
                break;
            }
            default: {
                throw new IllegalStateException("invalid facing: " + side);
            }
        }
        final ParticleDigging particle = newParticleDigging(world, x, y, z, 0.0, 0.0, 0.0, te.getBlockState());
        particle.setBlockPos(pos);
        particle.multiplyVelocity(0.2f);
        particle.multipleParticleScaleBy(0.6f);
        if (checkTexture) {
            ensureTexture(world, pos, (Particle)particle, te.getBlockState());
        }
        Minecraft.getMinecraft().effectRenderer.addEffect((Particle)particle);
    }
    
    public static void spawnBlockBreakParticles(final TileEntityBlock te) {
        final World world = te.getWorld();
        final BlockPos pos = te.getPos();
        final IBlockState state = te.getBlockState();
        final Minecraft mc = Minecraft.getMinecraft();
        for (int x = 0; x < 4; ++x) {
            for (int y = 0; y < 4; ++y) {
                for (int z = 0; z < 4; ++z) {
                    final double xOffset = (x + 0.5) / 4.0;
                    final double yOffset = (y + 0.5) / 4.0;
                    final double zOffset = (z + 0.5) / 4.0;
                    final ParticleDigging particle = newParticleDigging(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, xOffset - 0.5, yOffset - 0.5, zOffset - 0.5, state);
                    particle.setBlockPos(pos);
                    ensureTexture(world, pos, (Particle)particle, state);
                    mc.effectRenderer.addEffect((Particle)particle);
                }
            }
        }
    }
    
    public static ParticleBlockDust newParticleBlockDust(final World world, final double xCoord, final double yCoord, final double zCoord, final double xSpeed, final double ySpeed, final double zSpeed, final IBlockState state) {
        try {
            return ParticleUtil.particleBlockDust_ctor.newInstance(world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, state);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ParticleDigging newParticleDigging(final World world, final double xCoord, final double yCoord, final double zCoord, final double xSpeed, final double ySpeed, final double zSpeed, final IBlockState state) {
        try {
            return ParticleUtil.particleDigging_ctor.newInstance(world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, state);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void ensureTexture(final World world, final BlockPos pos, final Particle particle, IBlockState state) {
        if (pos == null) {
            return;
        }
        final IBakedModel model = ModelUtil.getBlockModel(state);
        if (model instanceof ISpecialParticleModel && ((ISpecialParticleModel)model).needsEnhancing(state)) {
            state = state.getActualState((IBlockAccess)world, pos);
            state = state.getBlock().getExtendedState(state, (IBlockAccess)world, pos);
            assert state instanceof Ic2BlockState.Ic2BlockStateInstance;
            ((ISpecialParticleModel)model).enhanceParticle(particle, (Ic2BlockState.Ic2BlockStateInstance)state);
        }
    }
    
    private static Constructor<ParticleBlockDust> getParticleBlockDustCtor() {
        try {
            final Constructor<ParticleBlockDust> ret = ParticleBlockDust.class.getDeclaredConstructor(World.class, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, IBlockState.class);
            ret.setAccessible(true);
            return ret;
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Constructor<ParticleDigging> getParticleDiggingCtor() {
        try {
            final Constructor<ParticleDigging> ret = ParticleDigging.class.getDeclaredConstructor(World.class, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, IBlockState.class);
            ret.setAccessible(true);
            return ret;
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static {
        particleBlockDust_ctor = getParticleBlockDustCtor();
        particleDigging_ctor = getParticleDiggingCtor();
    }
}
