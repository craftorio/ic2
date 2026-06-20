package ic2.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.ICancellableEvent;

public class ExplosionEvent extends LevelEvent implements ICancellableEvent {

    public final Entity entity;

    public final Vec3 pos;

    public final double power;

    public final LivingEntity igniter;

    public final int radiationRange;

    public final double rangeLimit;

    public ExplosionEvent(Level world, Entity entity, Vec3 pos, double power, LivingEntity igniter, int radiationRange, double rangeLimit) {
        super(world);
        this.entity = entity;
        this.pos = pos;
        this.power = power;
        this.igniter = igniter;
        this.radiationRange = radiationRange;
        this.rangeLimit = rangeLimit;
    }
}
