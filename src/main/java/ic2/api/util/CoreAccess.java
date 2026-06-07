package ic2.api.util;

import ic2.api.info.IInfoProvider;
import ic2.api.network.INetworkManager;
import ic2.api.tile.IRotorProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface CoreAccess
{
	boolean isCallingFromIc2();

	IInfoProvider getItemInfo();

	INetworkManager getClientNetworkManager();

	INetworkManager getServerNetworkManager();

	DamageSource getElectricDamageSource();

	DamageSource getNukeExplosionDamageSource();

	DamageSource getRadiationDamageSource();

	MobEffect getRadiationStatusEffect();

	<T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> var1);
}
