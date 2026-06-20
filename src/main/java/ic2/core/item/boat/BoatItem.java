package ic2.core.item.boat;

import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;

public class BoatItem extends Item
{
	private static final Predicate<Entity> RIDERS = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
	private final Class<? extends Boat> boatEntityClass;
	private final EntityType<? extends Boat> boatEntityType;

	public BoatItem(Class<? extends Boat> boatEntityClass, EntityType<? extends Boat> boatEntityType, Properties settings)
	{
		super(settings);
		this.boatEntityClass = boatEntityClass;
		this.boatEntityType = boatEntityType;
	}

	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player user, @NotNull InteractionHand hand)
	{
		ItemStack itemStack = user.getMainHandItem();
		BlockHitResult hitResult = getPlayerPOVHitResult(world, user, Fluid.ANY);
		if (hitResult.getType() == Type.MISS)
		{
			return InteractionResultHolder.pass(itemStack);
		}

		Vec3 vec3d = user.getViewVector(1.0F);
		List<Entity> list = world.getEntities(user, user.getBoundingBox().expandTowards(vec3d.scale(5.0)).inflate(1.0), RIDERS);
		if (!list.isEmpty())
		{
			Vec3 vec3d2 = user.getEyePosition();

			for (Entity entity : list)
			{
				AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
				if (box.contains(vec3d2))
				{
					return InteractionResultHolder.pass(itemStack);
				}
			}
		}

		if (hitResult.getType() == Type.BLOCK)
		{
			Boat boatEntity = this.createEntity(world, hitResult);
			boatEntity.setYRot(user.getYRot());
			if (!world.noCollision(boatEntity, boatEntity.getBoundingBox()))
			{
				return InteractionResultHolder.fail(itemStack);
			}

			if (!world.isClientSide)
			{
				world.addFreshEntity(boatEntity);
				world.gameEvent(user, GameEvent.ENTITY_PLACE, hitResult.getLocation());
				if (!user.getAbilities().instabuild)
				{
					itemStack.shrink(1);
				}
			}

			user.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
		} else
		{
			return InteractionResultHolder.pass(itemStack);
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
			Constructor<? extends Boat> constructor = this.boatEntityClass.getConstructor(EntityType.class, Level.class, double.class, double.class, double.class);
			return constructor.newInstance(this.boatEntityType, world, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}
}
