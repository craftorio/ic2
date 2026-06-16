package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.entity.LaserBulletEntity;
import ic2.core.entity.block.ITntEntity;
import ic2.core.entity.block.NukeEntity;
import ic2.core.entity.boat.CarbonBoatEntity;
import ic2.core.entity.boat.ElectricBoatEntity;
import ic2.core.entity.boat.RubberBoatEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class Ic2Entities
{
	public static final EntityType<ITntEntity> ITNT = register("itnt", Builder.<ITntEntity>of(ITntEntity::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10));
	public static final EntityType<NukeEntity> NUKE = register("nuke", Builder.<NukeEntity>of(NukeEntity::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10));
	public static final EntityType<LaserBulletEntity> LASER_BULLET = register("laser_bullet", Builder.<LaserBulletEntity>of(LaserBulletEntity::new, MobCategory.MISC).fireImmune().sized(0.8F, 0.8F).clientTrackingRange(8).updateInterval(8));
	public static final EntityType<RubberBoatEntity> RUBBER_BOAT = register("rubber_boat", Builder.<RubberBoatEntity>of(RubberBoatEntity::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10));
	public static final EntityType<ElectricBoatEntity> ELECTRIC_BOAT = register("electric_boat", Builder.<ElectricBoatEntity>of(ElectricBoatEntity::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10));
	public static final EntityType<CarbonBoatEntity> CARBON_BOAT = register("carbon_boat", Builder.<CarbonBoatEntity>of(CarbonBoatEntity::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10));

	public static void init()
	{
	}

	private static <T extends Entity> EntityType<T> register(String name, Builder<T> builder)
	{
		EntityType<T> ret = builder.build(IC2.getIdentifier(name).toString());
		IC2.envProxy.registerEntity(IC2.getIdentifier(name), ret);
		return ret;
	}
}
