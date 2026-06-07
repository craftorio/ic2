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
	public static final EntityType<ITntEntity> ITNT = register(
		"itnt", Builder.m_20704_(ITntEntity::new, MobCategory.MISC).m_20719_().m_20699_(0.98F, 0.98F).m_20702_(10).m_20717_(10)
	);
	public static final EntityType<NukeEntity> NUKE = register(
		"nuke", Builder.m_20704_(NukeEntity::new, MobCategory.MISC).m_20719_().m_20699_(0.98F, 0.98F).m_20702_(10).m_20717_(10)
	);
	public static final EntityType<LaserBulletEntity> LASER_BULLET = register(
		"laser_bullet", Builder.m_20704_(LaserBulletEntity::new, MobCategory.MISC).m_20719_().m_20699_(0.8F, 0.8F).m_20702_(8).m_20717_(8)
	);
	public static final EntityType<RubberBoatEntity> RUBBER_BOAT = register(
		"rubber_boat", Builder.m_20704_(RubberBoatEntity::new, MobCategory.MISC).m_20699_(1.375F, 0.5625F).m_20702_(10)
	);
	public static final EntityType<ElectricBoatEntity> ELECTRIC_BOAT = register(
		"electric_boat", Builder.m_20704_(ElectricBoatEntity::new, MobCategory.MISC).m_20699_(1.375F, 0.5625F).m_20702_(10)
	);
	public static final EntityType<CarbonBoatEntity> CARBON_BOAT = register(
		"carbon_boat", Builder.m_20704_(CarbonBoatEntity::new, MobCategory.MISC).m_20699_(1.375F, 0.5625F).m_20702_(10)
	);

	public static void init()
	{
	}

	private static <T extends Entity> EntityType<T> register(String name, Builder<T> builder)
	{
		EntityType<T> ret = builder.m_20712_(IC2.getIdentifier(name).toString());
		IC2.envProxy.registerEntity(IC2.getIdentifier(name), ret);
		return ret;
	}
}
