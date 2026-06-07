package ic2.api.info;

import ic2.api.util.CoreAccessRef;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;

public final class Info
{
	public static final String MOD_ID = "ic2";
	private static IInfoProvider itemInfo;
	private static DamageSource DMG_ELECTRIC;
	private static DamageSource DMG_NUKE_EXPLOSION;
	private static DamageSource DMG_RADIATION;
	private static MobEffect POTION_RADIATION;

	public static boolean isIc2Available()
	{
		return CoreAccessRef.exists();
	}

	public static IInfoProvider getItemInfo()
	{
		IInfoProvider ret = itemInfo;
		if (ret == null)
		{
			itemInfo = ret = CoreAccessRef.get().getItemInfo();
		}

		return ret;
	}

	public static DamageSource getElectricDamageSource()
	{
		DamageSource ret = DMG_ELECTRIC;
		if (ret == null)
		{
			DMG_ELECTRIC = ret = CoreAccessRef.get().getElectricDamageSource();
		}

		return ret;
	}

	public static DamageSource getNukeExplosionDamageSource()
	{
		DamageSource ret = DMG_NUKE_EXPLOSION;
		if (ret == null)
		{
			DMG_NUKE_EXPLOSION = ret = CoreAccessRef.get().getNukeExplosionDamageSource();
		}

		return ret;
	}

	public static DamageSource getRadiationDamageSource()
	{
		DamageSource ret = DMG_RADIATION;
		if (ret == null)
		{
			DMG_RADIATION = ret = CoreAccessRef.get().getRadiationDamageSource();
		}

		return ret;
	}

	public static MobEffect getRadiationStatusEffect()
	{
		MobEffect ret = POTION_RADIATION;
		if (ret == null)
		{
			POTION_RADIATION = ret = CoreAccessRef.get().getRadiationStatusEffect();
		}

		return ret;
	}
}
