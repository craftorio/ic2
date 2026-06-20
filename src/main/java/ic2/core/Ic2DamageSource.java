package ic2.core;

import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.RegistryAccess;

public class Ic2DamageSource
{
	public static final ResourceKey<DamageType> ELECTRICITY = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ic2", "electricity"));
	public static final ResourceKey<DamageType> NUKE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ic2", "nuke"));
	public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ic2", "radiation"));
	public static final ResourceKey<DamageType> REACTOR_EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ic2", "reactor_explosion"));

	public static DamageSource electricity;
	public static DamageSource nuke;
	public static DamageSource radiation;
	public static DamageSource reactorExplosion;

	public static void init(RegistryAccess registryAccess)
	{
		Registry<DamageType> registry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
		electricity = new DamageSource(registry.getHolderOrThrow(ELECTRICITY));
		nuke = new DamageSource(registry.getHolderOrThrow(NUKE));
		radiation = new DamageSource(registry.getHolderOrThrow(RADIATION));
		reactorExplosion = new DamageSource(registry.getHolderOrThrow(REACTOR_EXPLOSION));
	}

	public static DamageSource getNukeSource(LivingEntity igniter, Level level)
	{
		if (igniter != null)
		{
			return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NUKE), igniter);
		}
		if (nuke == null)
		{
			init(level.registryAccess());
		}
		return nuke;
	}

	public static DamageSource create(Level level, String name)
	{
		ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ic2", name));
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
	}
}
