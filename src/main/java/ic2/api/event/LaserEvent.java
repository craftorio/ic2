// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.event;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.event.world.WorldEvent;

@Cancelable
public class LaserEvent extends WorldEvent
{
    public final Entity lasershot;
    public EntityLivingBase owner;
    public float range;
    public float power;
    public int blockBreaks;
    public boolean explosive;
    public boolean smelt;
    
    public LaserEvent(final World world, final Entity lasershot, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive, final boolean smelt) {
        super(world);
        this.lasershot = lasershot;
        this.owner = owner;
        this.range = range;
        this.power = power;
        this.blockBreaks = blockBreaks;
        this.explosive = explosive;
        this.smelt = smelt;
    }
    
    public static class LaserShootEvent extends LaserEvent
    {
        public final ItemStack laserItem;
        
        public LaserShootEvent(final World world, final Entity lasershot, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive, final boolean smelt, final ItemStack laseritem) {
            super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
            this.laserItem = laseritem;
        }
    }
    
    public static class LaserExplodesEvent extends LaserEvent
    {
        public float explosionPower;
        public float explosionDropRate;
        public float explosionEntityDamage;
        
        public LaserExplodesEvent(final World world, final Entity lasershot, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive, final boolean smelt, final float explosionpower1, final float explosiondroprate1, final float explosionentitydamage1) {
            super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
            this.explosionPower = explosionpower1;
            this.explosionDropRate = explosiondroprate1;
            this.explosionEntityDamage = explosionentitydamage1;
        }
    }
    
    public static class LaserHitsBlockEvent extends LaserEvent
    {
        public BlockPos pos;
        public final EnumFacing side;
        public boolean removeBlock;
        public boolean dropBlock;
        public float dropChance;
        
        public LaserHitsBlockEvent(final World world, final Entity lasershot, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive1, final boolean smelt1, final BlockPos pos, final EnumFacing side, final float dropChance, final boolean removeBlock, final boolean dropBlock) {
            super(world, lasershot, owner, range, power, blockBreaks, explosive1, smelt1);
            this.pos = pos;
            this.side = side;
            this.removeBlock = removeBlock;
            this.dropBlock = dropBlock;
            this.dropChance = dropChance;
        }
    }
    
    public static class LaserHitsEntityEvent extends LaserEvent
    {
        public Entity hitEntity;
        public boolean passThrough;
        
        public LaserHitsEntityEvent(final World world, final Entity lasershot, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive, final boolean smelt, final Entity hitentity) {
            super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
            this.hitEntity = hitentity;
        }
    }
}
