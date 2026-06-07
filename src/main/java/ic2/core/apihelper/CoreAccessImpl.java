package ic2.core.apihelper;

import ic2.api.info.IInfoProvider;
import ic2.api.network.INetworkManager;
import ic2.api.tile.IRotorProvider;
import ic2.api.util.CoreAccess;
import ic2.api.util.CoreAccessRef;
import ic2.core.IC2;
import ic2.core.Ic2DamageSource;
import ic2.core.Ic2Potion;
import ic2.core.util.ItemInfo;
import ic2.core.util.Util;

import java.lang.reflect.Field;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class CoreAccessImpl implements CoreAccess
{
	public static void init()
	{
		try
		{
			Field field = CoreAccessRef.class.getDeclaredField("CORE_ACCESS");
			field.setAccessible(true);
			field.set(null, new CoreAccessImpl());
			field.setAccessible(false);
		} catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isCallingFromIc2()
	{
		return Util.isCallingFromIc2(1);
	}

	@Override
	public IInfoProvider getItemInfo()
	{
		return new ItemInfo();
	}

	@Override
	public INetworkManager getClientNetworkManager()
	{
		return IC2.network.get(false);
	}

	@Override
	public INetworkManager getServerNetworkManager()
	{
		return IC2.network.get(true);
	}

	@Override
	public DamageSource getElectricDamageSource()
	{
		return Ic2DamageSource.electricity;
	}

	@Override
	public DamageSource getNukeExplosionDamageSource()
	{
		return Ic2DamageSource.nuke;
	}

	@Override
	public DamageSource getRadiationDamageSource()
	{
		return Ic2DamageSource.radiation;
	}

	@Override
	public MobEffect getRadiationStatusEffect()
	{
		return Ic2Potion.radiation;
	}

	@Override
	public <T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> type)
	{
		IC2.sideProxy.registerRotorProvider(type);
	}
}
