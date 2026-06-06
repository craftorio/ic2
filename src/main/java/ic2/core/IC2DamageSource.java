package ic2.core;

import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.Explosion;

public class IC2DamageSource extends DamageSource
{
	public static final IC2DamageSource electricity = new IC2DamageSource("electricity");
	public static final IC2DamageSource nuke = (IC2DamageSource) new IC2DamageSource("nuke").setExplosion();
	public static final IC2DamageSource radiation = (IC2DamageSource) new IC2DamageSource("radiation").setDamageBypassesArmor();

	public IC2DamageSource(String s)
	{
		super(s);
	}

	public static DamageSource getNukeSource(Explosion explosion)
	{
		return explosion != null && explosion.getExplosivePlacedBy() != null ? new EntityDamageSource("nuke.player", explosion.getExplosivePlacedBy()).setExplosion() : nuke;
	}
}
