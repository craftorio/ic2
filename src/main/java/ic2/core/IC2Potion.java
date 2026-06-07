package ic2.core;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Ic2Potion extends MobEffect
{
	public static Ic2Potion radiation;

	public Ic2Potion(MobEffectCategory type, int liquidColor)
	{
		super(type, liquidColor);
	}

	public void m_6742_(LivingEntity entity, int amplifier)
	{
		if (this == radiation)
		{
			entity.hurt(Ic2DamageSource.radiation, amplifier / 100 + 0.5F);
		}
	}

	public boolean m_6584_(int duration, int amplifier)
	{
		if (this == radiation)
		{
			int rate = 25 >> amplifier;
			return rate > 0 ? duration % rate == 0 : true;
		} else
		{
			return false;
		}
	}

	public void applyTo(LivingEntity entity, int duration, int amplifier)
	{
		MobEffectInstance effect = new MobEffectInstance(radiation, duration, amplifier);
		entity.m_7292_(effect);
	}
}
