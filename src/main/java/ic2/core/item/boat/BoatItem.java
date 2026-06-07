package ic2.core.item.boat;

import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class BoatItem extends Item
{
	private static final Predicate<Entity> RIDERS = EntitySelector.f_20408_.and(Entity::m_6087_);
	private final Class<? extends Boat> boatEntityClass;
	private final EntityType<? extends Boat> boatEntityType;

	public BoatItem(Class<? extends Boat> boatEntityClass, EntityType<? extends Boat> boatEntityType, Properties settings)
	{
		super(settings);
		this.boatEntityClass = boatEntityClass;
		this.boatEntityType = boatEntityType;
	}

	public InteractionResultHolder<ItemStack> m_7203_(Level world, Player user, InteractionHand hand)
	{
		ItemStack itemStack = user.m_21120_(hand);
		BlockHitResult hitResult = m_41435_(world, user, Fluid.ANY);
		if (hitResult.m_6662_() == Type.MISS)
		{
			return InteractionResultHolder.m_19098_(itemStack);
		}

		Vec3 vec3d = user.m_20252_(1.0F);
		List<Entity> list = world.m_6249_(user, user.m_20191_().m_82369_(vec3d.m_82490_(5.0)).m_82400_(1.0), RIDERS);
		if (!list.isEmpty())
		{
			Vec3 vec3d2 = user.m_146892_();

			for (Entity entity : list)
			{
				AABB box = entity.m_20191_().m_82400_(entity.m_6143_());
				if (box.m_82390_(vec3d2))
				{
					return InteractionResultHolder.m_19098_(itemStack);
				}
			}
		}

		if (hitResult.m_6662_() == Type.BLOCK)
		{
			Boat boatEntity = this.createEntity(world, hitResult);
			boatEntity.m_146922_(user.m_146908_());
			if (!world.m_45756_(boatEntity, boatEntity.m_20191_()))
			{
				return InteractionResultHolder.m_19100_(itemStack);
			}

			if (!world.isClientSide)
			{
				world.addFreshEntity(boatEntity);
				world.m_220400_(user, GameEvent.f_157810_, hitResult.m_82450_());
				if (!user.m_150110_().f_35937_)
				{
					itemStack.m_41774_(1);
				}
			}

			user.m_36246_(Stats.f_12982_.m_12902_(this));
			return InteractionResultHolder.m_19092_(itemStack, world.m_5776_());
		} else
		{
			return InteractionResultHolder.m_19098_(itemStack);
		}
	}

	private Boat createEntity(Level world, HitResult hitResult)
	{
		if (this.boatEntityClass == null)
		{
			IC2.log.error(LogCategory.General, "Boat entity class doesn't exist");
			return null;
		}

		try
		{
			Constructor constructor = this.boatEntityClass.getConstructor(EntityType.class, Level.class, double.class, double.class, double.class);
			return (Boat) constructor.newInstance(
				this.boatEntityType, world, hitResult.m_82450_().f_82479_, hitResult.m_82450_().f_82480_, hitResult.m_82450_().f_82481_
			);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}
}
