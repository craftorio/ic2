// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.event;

import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.event.world.WorldEvent;

@Cancelable
public class ExplosionEvent extends WorldEvent
{
    public final Entity entity;
    public final Vec3d pos;
    public final double power;
    public final EntityLivingBase igniter;
    public final int radiationRange;
    public final double rangeLimit;
    
    public ExplosionEvent(final World world, final Entity entity, final Vec3d pos, final double power, final EntityLivingBase igniter, final int radiationRange, final double rangeLimit) {
        super(world);
        this.entity = entity;
        this.pos = pos;
        this.power = power;
        this.igniter = igniter;
        this.radiationRange = radiationRange;
        this.rangeLimit = rangeLimit;
    }
}
