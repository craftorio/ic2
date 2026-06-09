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

	public void applyEffectTick(LivingEntity entity, int amplifier)
	{
		if (this == radiation)
		{
			if (Ic2DamageSource.radiation == null)
			{
				Ic2DamageSource.init(entity.level().registryAccess());
			}
			entity.hurt(Ic2DamageSource.radiation, (float) amplifier / 100 + 0.5F);
		}
	}

	public boolean isDurationEffectTick(int duration, int amplifier)
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
		entity.addEffect(effect);
	}
}
