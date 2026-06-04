// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.util.DamageSource;

public class IC2DamageSource extends DamageSource
{
    public static IC2DamageSource electricity;
    public static IC2DamageSource nuke;
    public static IC2DamageSource radiation;
    
    public IC2DamageSource(final String s) {
        super(s);
    }
    
    public static DamageSource getNukeSource(final Explosion explosion) {
        return (explosion != null && explosion.getExplosivePlacedBy() != null) ? new EntityDamageSource("nuke.player", (Entity)explosion.getExplosivePlacedBy()).setExplosion() : IC2DamageSource.nuke;
    }
    
    static {
        IC2DamageSource.electricity = new IC2DamageSource("electricity");
        IC2DamageSource.nuke = (IC2DamageSource)new IC2DamageSource("nuke").setExplosion();
        IC2DamageSource.radiation = (IC2DamageSource)new IC2DamageSource("radiation").setDamageBypassesArmor();
    }
}
