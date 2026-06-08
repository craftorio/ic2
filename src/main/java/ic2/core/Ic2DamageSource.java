package ic2.core;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;

public class Ic2DamageSource extends DamageSource
{
	public static Ic2DamageSource electricity = new Ic2DamageSource("electricity");
	public static Ic2DamageSource nuke = (Ic2DamageSource) new Ic2DamageSource("nuke").setExplosion();
	public static Ic2DamageSource radiation = (Ic2DamageSource) new Ic2DamageSource("radiation").bypassArmor();

	public Ic2DamageSource(String s)
	{
		super(s);
	}

	public static DamageSource getNukeSource(LivingEntity igniter)
	{
		return igniter != null ? new EntityDamageSource("nuke.player", igniter).setExplosion() : nuke;
	}
}
