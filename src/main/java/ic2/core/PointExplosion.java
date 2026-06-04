// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import ic2.core.util.Util;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import ic2.api.event.ExplosionEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;

public class PointExplosion extends Explosion
{
    private final World world;
    private final Entity entity;
    private final float dropRate;
    private final int entityDamage;
    private float explosionSize;
    
    public PointExplosion(final World world1, final Entity entity, final EntityLivingBase exploder, final double x, final double y, final double z, final float power, final float dropRate1, final int entityDamage1) {
        super(world1, (Entity)exploder, x, y, z, power, true, true);
        this.world = world1;
        this.entity = entity;
        this.dropRate = dropRate1;
        this.entityDamage = entityDamage1;
        this.explosionSize = power;
    }
    
    public void doExplosionA() {
        final double explosionX = this.getPosition().x;
        final double explosionY = this.getPosition().y;
        final double explosionZ = this.getPosition().z;
        final ExplosionEvent event = new ExplosionEvent(this.world, this.entity, this.getPosition(), this.explosionSize, this.getExplosivePlacedBy(), 0, 1.0);
        if (MinecraftForge.EVENT_BUS.post((Event)event)) {
            return;
        }
        for (int x = Util.roundToNegInf(explosionX) - 1; x <= Util.roundToNegInf(explosionX) + 1; ++x) {
            for (int y = Util.roundToNegInf(explosionY) - 1; y <= Util.roundToNegInf(explosionY) + 1; ++y) {
                for (int z = Util.roundToNegInf(explosionZ) - 1; z <= Util.roundToNegInf(explosionZ) + 1; ++z) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    final IBlockState block = this.world.getBlockState(pos);
                    if (block.getBlock().getExplosionResistance(this.world, pos, (Entity)this.getExplosivePlacedBy(), (Explosion)this) < this.explosionSize * 10.0f) {
                        this.getAffectedBlockPositions().add(pos);
                    }
                }
            }
        }
        final List<Entity> entitiesInRange = this.world.getEntitiesWithinAABBExcludingEntity((Entity)this.getExplosivePlacedBy(), new AxisAlignedBB(explosionX - 2.0, explosionY - 2.0, explosionZ - 2.0, explosionX + 2.0, explosionY + 2.0, explosionZ + 2.0));
        for (final Entity entity : entitiesInRange) {
            entity.attackEntityFrom(DamageSource.causeExplosionDamage((Explosion)this), (float)this.entityDamage);
        }
        this.explosionSize = 1.0f / this.dropRate;
    }
}
