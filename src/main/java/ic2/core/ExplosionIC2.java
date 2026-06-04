// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.init.Blocks;
import ic2.api.tile.ExplosionWhitelist;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.StackUtil;
import ic2.core.util.ItemComparableItemStack;
import java.util.Map;
import java.util.HashMap;
import ic2.core.network.NetworkManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import ic2.core.item.armor.ItemArmorHazmat;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Collections;
import java.util.Comparator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import ic2.api.event.ExplosionEvent;
import ic2.core.util.Util;
import java.util.ArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import java.util.List;
import net.minecraft.util.DamageSource;
import java.util.Random;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;

public class ExplosionIC2 extends Explosion
{
    private final World worldObj;
    private final Entity exploder;
    private final double explosionX;
    private final double explosionY;
    private final double explosionZ;
    private final int mapHeight;
    private final float power;
    private final float explosionDropRate;
    private final Type type;
    private final int radiationRange;
    private final EntityLivingBase igniter;
    private final Random rng;
    private final double maxDistance;
    private final int areaSize;
    private final int areaX;
    private final int areaZ;
    private final DamageSource damageSource;
    private final List<EntityDamage> entitiesInRange;
    private final long[][] destroyedBlockPositions;
    private ChunkCache chunkCache;
    private static final double dropPowerLimit = 8.0;
    private static final double damageAtDropPowerLimit = 32.0;
    private static final double accelerationAtDropPowerLimit = 0.7;
    private static final double motionLimit = 60.0;
    private static final int secondaryRayCount = 5;
    private static final int bitSetElementSize = 2;
    
    public ExplosionIC2(final World world, final Entity entity, final double x, final double y, final double z, final float power, final float drop) {
        this(world, entity, x, y, z, power, drop, Type.Normal);
    }
    
    public ExplosionIC2(final World world, final Entity entity, final double x, final double y, final double z, final float power, final float drop, final Type type) {
        this(world, entity, x, y, z, power, drop, type, null, 0);
    }
    
    public ExplosionIC2(final World world, final Entity entity, final BlockPos pos, final float power, final float drop, final Type type) {
        this(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, drop, type);
    }
    
    public ExplosionIC2(final World world, final Entity entity, final double x, final double y, final double z, final float power1, final float drop, final Type type1, final EntityLivingBase igniter1, final int radiationRange1) {
        super(world, entity, x, y, z, power1, false, false);
        this.rng = new Random();
        this.entitiesInRange = new ArrayList<EntityDamage>();
        this.worldObj = world;
        this.exploder = entity;
        this.explosionX = x;
        this.explosionY = y;
        this.explosionZ = z;
        this.mapHeight = IC2.getWorldHeight(world);
        this.power = power1;
        this.explosionDropRate = drop;
        this.type = type1;
        this.igniter = igniter1;
        this.radiationRange = radiationRange1;
        this.maxDistance = this.power / 0.4;
        final int maxDistanceInt = (int)Math.ceil(this.maxDistance);
        this.areaSize = maxDistanceInt * 2;
        this.areaX = Util.roundToNegInf(x) - maxDistanceInt;
        this.areaZ = Util.roundToNegInf(z) - maxDistanceInt;
        if (this.isNuclear()) {
            this.damageSource = IC2DamageSource.getNukeSource(this);
        }
        else {
            this.damageSource = DamageSource.causeExplosionDamage((Explosion)this);
        }
        this.destroyedBlockPositions = new long[this.mapHeight][];
    }
    
    public ExplosionIC2(final World world, final Entity entity, final BlockPos pos, final int i, final float f, final Type heat) {
        this(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, (float)i, f, heat);
    }
    
    public void doExplosion() {
        if (this.power <= 0.0f) {
            return;
        }
        final ExplosionEvent event = new ExplosionEvent(this.worldObj, this.exploder, this.getPosition(), this.power, this.igniter, this.radiationRange, this.maxDistance);
        if (MinecraftForge.EVENT_BUS.post((Event)event)) {
            return;
        }
        final int range = this.areaSize / 2;
        final BlockPos pos = new BlockPos(this.getPosition());
        final BlockPos start = pos.add(-range, -range, -range);
        final BlockPos end = pos.add(range, range, range);
        this.chunkCache = new ChunkCache(this.worldObj, start, end, 0);
        final List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(start, end));
        for (final Entity entity : entities) {
            if (entity instanceof EntityLivingBase || entity instanceof EntityItem) {
                final int distance = (int)(Util.square(entity.posX - this.explosionX) + Util.square(entity.posY - this.explosionY) + Util.square(entity.posZ - this.explosionZ));
                final double health = getEntityHealth(entity);
                this.entitiesInRange.add(new EntityDamage(entity, distance, health));
            }
        }
        final boolean entitiesAreInRange = !this.entitiesInRange.isEmpty();
        if (entitiesAreInRange) {
            Collections.sort(this.entitiesInRange, new Comparator<EntityDamage>() {
                @Override
                public int compare(final EntityDamage a, final EntityDamage b) {
                    return a.distance - b.distance;
                }
            });
        }
        final int steps = (int)Math.ceil(3.141592653589793 / Math.atan(1.0 / this.maxDistance));
        final BlockPos.MutableBlockPos tmpPos = new BlockPos.MutableBlockPos();
        for (int phi_n = 0; phi_n < 2 * steps; ++phi_n) {
            for (int theta_n = 0; theta_n < steps; ++theta_n) {
                final double phi = 6.283185307179586 / steps * phi_n;
                final double theta = 3.141592653589793 / steps * theta_n;
                this.shootRay(this.explosionX, this.explosionY, this.explosionZ, phi, theta, this.power, entitiesAreInRange && phi_n % 8 == 0 && theta_n % 8 == 0, tmpPos);
            }
        }
        for (final EntityDamage entry : this.entitiesInRange) {
            final Entity entity2 = entry.entity;
            entity2.attackEntityFrom(this.damageSource, (float)entry.damage);
            if (entity2 instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer)entity2;
                if (this.isNuclear() && this.igniter != null && player == this.igniter && player.getHealth() <= 0.0f) {
                    IC2.achievements.issueAchievement(player, "dieFromOwnNuke");
                }
            }
            final double motionSq = Util.square(entry.motionX) + Util.square(entity2.motionY) + Util.square(entity2.motionZ);
            final double reduction = (motionSq > 3600.0) ? Math.sqrt(3600.0 / motionSq) : 1.0;
            final Entity entity4 = entity2;
            entity4.motionX += entry.motionX * reduction;
            final Entity entity5 = entity2;
            entity5.motionY += entry.motionY * reduction;
            final Entity entity6 = entity2;
            entity6.motionZ += entry.motionZ * reduction;
        }
        if (this.isNuclear() && this.radiationRange >= 1) {
            final List<EntityLiving> entitiesInRange = this.worldObj.getEntitiesWithinAABB((Class)EntityLiving.class, new AxisAlignedBB(this.explosionX - this.radiationRange, this.explosionY - this.radiationRange, this.explosionZ - this.radiationRange, this.explosionX + this.radiationRange, this.explosionY + this.radiationRange, this.explosionZ + this.radiationRange));
            for (final EntityLiving entity3 : entitiesInRange) {
                if (ItemArmorHazmat.hasCompleteHazmat((EntityLivingBase)entity3)) {
                    continue;
                }
                final double distance2 = entity3.getDistance(this.explosionX, this.explosionY, this.explosionZ);
                final int hungerLength = (int)(120.0 * (this.radiationRange - distance2));
                final int poisonLength = (int)(80.0 * (this.radiationRange / 3 - distance2));
                if (hungerLength >= 0) {
                    entity3.addPotionEffect(new PotionEffect(MobEffects.HUNGER, hungerLength, 0));
                }
                if (poisonLength < 0) {
                    continue;
                }
                IC2Potion.radiation.applyTo((EntityLivingBase)entity3, poisonLength, 0);
            }
        }
        IC2.network.get(true).initiateExplosionEffect(this.worldObj, this.getPosition(), this.type);
        final Random rng = this.worldObj.rand;
        final boolean doDrops = this.worldObj.getGameRules().getBoolean("doTileDrops");
        final Map<XZposition, Map<ItemComparableItemStack, DropData>> blocksToDrop = new HashMap<XZposition, Map<ItemComparableItemStack, DropData>>();
        for (int y = 0; y < this.destroyedBlockPositions.length; ++y) {
            final long[] bitSet = this.destroyedBlockPositions[y];
            if (bitSet != null) {
                int index = -2;
                while ((index = nextSetIndex(index + 2, bitSet, 2)) != -1) {
                    final int realIndex = index / 2;
                    int z = realIndex / this.areaSize;
                    int x = realIndex - z * this.areaSize;
                    x += this.areaX;
                    z += this.areaZ;
                    tmpPos.setPos(x, y, z);
                    final IBlockState state = this.chunkCache.getBlockState((BlockPos)tmpPos);
                    final Block block = state.getBlock();
                    if (this.power < 20.0f) {}
                    if (doDrops && block.canDropFromExplosion((Explosion)this) && getAtIndex(index, bitSet, 2) == 1) {
                        for (final ItemStack stack : StackUtil.getDrops((IBlockAccess)this.worldObj, (BlockPos)tmpPos, state, block, 0)) {
                            if (rng.nextFloat() > this.explosionDropRate) {
                                continue;
                            }
                            final XZposition xZposition = new XZposition(x / 2, z / 2);
                            Map<ItemComparableItemStack, DropData> map = blocksToDrop.get(xZposition);
                            if (map == null) {
                                map = new HashMap<ItemComparableItemStack, DropData>();
                                blocksToDrop.put(xZposition, map);
                            }
                            final ItemComparableItemStack isw = new ItemComparableItemStack(stack, false);
                            DropData data = map.get(isw);
                            if (data == null) {
                                data = new DropData(StackUtil.getSize(stack), y);
                                map.put(isw.copy(), data);
                            }
                            else {
                                data.add(StackUtil.getSize(stack), y);
                            }
                        }
                    }
                    block.onBlockExploded(this.worldObj, (BlockPos)tmpPos, (Explosion)this);
                }
            }
        }
        for (final Map.Entry<XZposition, Map<ItemComparableItemStack, DropData>> entry2 : blocksToDrop.entrySet()) {
            final XZposition xZposition2 = entry2.getKey();
            for (final Map.Entry<ItemComparableItemStack, DropData> entry3 : entry2.getValue().entrySet()) {
                final ItemComparableItemStack isw2 = entry3.getKey();
                int stackSize;
                for (int count = entry3.getValue().n; count > 0; count -= stackSize) {
                    stackSize = Math.min(count, 64);
                    final EntityItem entityitem = new EntityItem(this.worldObj, (double)((xZposition2.x + this.worldObj.rand.nextFloat()) * 2.0f), entry3.getValue().maxY + 0.5, (double)((xZposition2.z + this.worldObj.rand.nextFloat()) * 2.0f), isw2.toStack(stackSize));
                    entityitem.setDefaultPickupDelay();
                    this.worldObj.spawnEntity((Entity)entityitem);
                }
            }
        }
    }
    
    public void destroy(final int x, final int y, final int z, final boolean noDrop) {
        this.destroyUnchecked(x, y, z, noDrop);
    }
    
    private void destroyUnchecked(final int x, final int y, final int z, final boolean noDrop) {
        int index = (z - this.areaZ) * this.areaSize + (x - this.areaX);
        index *= 2;
        long[] array = this.destroyedBlockPositions[y];
        if (array == null) {
            array = makeArray(Util.square(this.areaSize), 2);
            this.destroyedBlockPositions[y] = array;
        }
        if (noDrop) {
            setAtIndex(index, array, 3);
        }
        else {
            setAtIndex(index, array, 1);
        }
    }
    
    private void shootRay(double x, double y, double z, final double phi, final double theta, double power1, final boolean killEntities, final BlockPos.MutableBlockPos tmpPos) {
        final double deltaX = Math.sin(theta) * Math.cos(phi);
        final double deltaY = Math.cos(theta);
        final double deltaZ = Math.sin(theta) * Math.sin(phi);
        int step = 0;
        while (true) {
            final int blockY = Util.roundToNegInf(y);
            if (blockY < 0) {
                break;
            }
            if (blockY >= this.mapHeight) {
                break;
            }
            final int blockX = Util.roundToNegInf(x);
            final int blockZ = Util.roundToNegInf(z);
            tmpPos.setPos(blockX, blockY, blockZ);
            final IBlockState state = this.chunkCache.getBlockState((BlockPos)tmpPos);
            final Block block = state.getBlock();
            double absorption = this.getAbsorption(block, (BlockPos)tmpPos);
            if (absorption < 0.0) {
                break;
            }
            if (absorption > 1000.0 && !ExplosionWhitelist.isBlockWhitelisted(block)) {
                absorption = 0.5;
            }
            else {
                if (absorption > power1) {
                    break;
                }
                if (block == Blocks.STONE || (block != Blocks.AIR && !block.isAir(state, (IBlockAccess)this.worldObj, (BlockPos)tmpPos))) {
                    this.destroyUnchecked(blockX, blockY, blockZ, power1 > 8.0);
                }
            }
            if (killEntities && (step + 4) % 8 == 0 && !this.entitiesInRange.isEmpty() && power1 >= 0.25) {
                this.damageEntities(x, y, z, step, power1);
            }
            if (absorption > 10.0) {
                for (int i = 0; i < 5; ++i) {
                    this.shootRay(x, y, z, this.rng.nextDouble() * 2.0 * 3.141592653589793, this.rng.nextDouble() * 3.141592653589793, absorption * 0.4, false, tmpPos);
                }
            }
            power1 -= absorption;
            x += deltaX;
            y += deltaY;
            z += deltaZ;
            ++step;
        }
    }
    
    private double getAbsorption(final Block block, final BlockPos pos) {
        double ret = 0.5;
        if (block == Blocks.AIR || block.isAir(block.getDefaultState(), (IBlockAccess)this.worldObj, pos)) {
            return ret;
        }
        if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && this.type != Type.Normal) {
            ++ret;
        }
        else {
            final float resistance = block.getExplosionResistance(this.worldObj, pos, this.exploder, (Explosion)this);
            if (resistance < 0.0f) {
                return resistance;
            }
            final double extra = (resistance + 4.0f) * 0.3;
            if (this.type != Type.Heat) {
                ret += extra;
            }
            else {
                ret += extra * 6.0;
            }
        }
        return ret;
    }
    
    private void damageEntities(final double x, final double y, final double z, final int step, final double power) {
        int index;
        if (step != 4) {
            final int distanceMin = Util.square(step - 5);
            int indexStart = 0;
            int indexEnd = this.entitiesInRange.size() - 1;
            do {
                index = (indexStart + indexEnd) / 2;
                final int distance = this.entitiesInRange.get(index).distance;
                if (distance < distanceMin) {
                    indexStart = index + 1;
                }
                else if (distance > distanceMin) {
                    indexEnd = index - 1;
                }
                else {
                    indexEnd = index;
                }
            } while (indexStart < indexEnd);
        }
        else {
            index = 0;
        }
        final int distanceMax = Util.square(step + 5);
        for (int i = index; i < this.entitiesInRange.size(); ++i) {
            final EntityDamage entry = this.entitiesInRange.get(i);
            if (entry.distance >= distanceMax) {
                break;
            }
            final Entity entity = entry.entity;
            if (Util.square(entity.posX - x) + Util.square(entity.posY - y) + Util.square(entity.posZ - z) <= 25.0) {
                final double damage = 4.0 * power;
                final EntityDamage entityDamage = entry;
                entityDamage.damage += damage;
                final EntityDamage entityDamage2 = entry;
                entityDamage2.health -= damage;
                final double dx = entity.posX - this.explosionX;
                final double dy = entity.posY - this.explosionY;
                final double dz = entity.posZ - this.explosionZ;
                final double distance2 = Math.sqrt(dx * dx + dy * dy + dz * dz);
                final EntityDamage entityDamage3 = entry;
                entityDamage3.motionX += dx / distance2 * 0.0875 * power;
                final EntityDamage entityDamage4 = entry;
                entityDamage4.motionY += dy / distance2 * 0.0875 * power;
                final EntityDamage entityDamage5 = entry;
                entityDamage5.motionZ += dz / distance2 * 0.0875 * power;
                if (entry.health <= 0.0) {
                    entity.attackEntityFrom(this.damageSource, (float)entry.damage);
                    if (!entity.isEntityAlive()) {
                        this.entitiesInRange.remove(i);
                        --i;
                    }
                }
            }
        }
    }
    
    public EntityLivingBase getExplosivePlacedBy() {
        return this.igniter;
    }
    
    private boolean isNuclear() {
        return this.type == Type.Nuclear;
    }
    
    private static double getEntityHealth(final Entity entity) {
        if (entity instanceof EntityItem) {
            return 5.0;
        }
        return Double.POSITIVE_INFINITY;
    }
    
    private static long[] makeArray(final int size, final int step) {
        return new long[(size * step + 8 - step) / 8];
    }
    
    private static int nextSetIndex(final int start, final long[] array, final int step) {
        int offset = start % 8;
        for (int i = start / 8; i < array.length; ++i) {
            final long aval = array[i];
            for (int j = offset; j < 8; j += step) {
                final int val = (int)(aval >> j & (long)((1 << step) - 1));
                if (val != 0) {
                    return i * 8 + j;
                }
            }
            offset = 0;
        }
        return -1;
    }
    
    private static int getAtIndex(final int index, final long[] array, final int step) {
        return (int)(array[index / 8] >>> index % 8 & (long)((1 << step) - 1));
    }
    
    private static void setAtIndex(final int index, final long[] array, final int value) {
        final int n = index / 8;
        array[n] |= value << index % 8;
    }
    
    private static class XZposition
    {
        int x;
        int z;
        
        XZposition(final int x1, final int z1) {
            this.x = x1;
            this.z = z1;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof XZposition) {
                final XZposition xZposition = (XZposition)obj;
                return xZposition.x == this.x && xZposition.z == this.z;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return this.x * 31 ^ this.z;
        }
    }
    
    private static class DropData
    {
        int n;
        int maxY;
        
        DropData(final int n1, final int y) {
            this.n = n1;
            this.maxY = y;
        }
        
        public DropData add(final int n1, final int y) {
            this.n += n1;
            if (y > this.maxY) {
                this.maxY = y;
            }
            return this;
        }
    }
    
    public enum Type
    {
        Normal, 
        Heat, 
        Electrical, 
        Nuclear;
    }
    
    private static class EntityDamage
    {
        final Entity entity;
        final int distance;
        double health;
        double damage;
        double motionX;
        double motionY;
        double motionZ;
        
        EntityDamage(final Entity entity, final int distance, final double health) {
            this.entity = entity;
            this.distance = distance;
            this.health = health;
        }
    }
}
