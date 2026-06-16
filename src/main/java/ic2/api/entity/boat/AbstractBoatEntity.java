package ic2.api.entity.boat;

import java.lang.reflect.Field;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBoatEntity extends Boat
{
	protected boolean isExtraItemDropped = false;

	private static Field getFieldSafe(Class<?> clazz, String srgName, String mcpName) throws NoSuchFieldException
	{
		try
		{
			return clazz.getDeclaredField(srgName);
		} catch (NoSuchFieldException e)
		{
			return clazz.getDeclaredField(mcpName);
		}
	}

	public AbstractBoatEntity(EntityType<? extends Boat> entityType, Level world)
	{
		super(entityType, world);
	}

	public AbstractBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world, double x, double y, double z)
	{
		this(entityType, world);
		this.setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	public ItemStack getExtraDropItemStack()
	{
		return ItemStack.EMPTY;
	}

	public abstract BoatType getOverrideBoatType();

	public boolean brokenByFalling()
	{
		return true;
	}

	public boolean canFloatOn(FluidState fluidState)
	{
		return fluidState.is(FluidTags.WATER);
	}

	protected SoundEvent getPaddleSound()
	{
		return switch (this.checkLocation())
		{
			case IN_WATER, UNDER_WATER, UNDER_FLOWING_WATER -> SoundEvents.BOAT_PADDLE_WATER;
			case ON_LAND -> SoundEvents.BOAT_PADDLE_LAND;
			default -> null;
		};
	}

	private Status checkLocation()
	{
		Class<Boat> boatEntityClass = Boat.class;

		try
		{
			Field waterLevelField = getFieldSafe(boatEntityClass, "f_38277_", "waterLevel");
			Field landFrictionField = getFieldSafe(boatEntityClass, "f_38278_", "landFriction");
			waterLevelField.setAccessible(true);
			landFrictionField.setAccessible(true);
			Status location = this.getUnderWaterLocation();
			if (location != null)
			{
				waterLevelField.set(this, this.getBoundingBox().maxY);
				return location;
			} else if (this.checkBoatInWater())
			{
				return Status.IN_WATER;
			} else
			{
				float f = this.getGroundFriction();
				if (f > 0.0F)
				{
					landFrictionField.set(this, f);
					return Status.ON_LAND;
				} else
				{
					return Status.IN_AIR;
				}
			}
		} catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	private boolean checkBoatInWater()
	{
		Class<Boat> boatEntityClass = Boat.class;

		try
		{
			Field waterLevelField = getFieldSafe(boatEntityClass, "f_38277_", "waterLevel");
			waterLevelField.setAccessible(true);
			AABB box = this.getBoundingBox();
			int i = Mth.floor(box.minX);
			int j = Mth.ceil(box.maxX);
			int k = Mth.floor(box.minY);
			int l = Mth.ceil(box.minY + 0.001);
			int m = Mth.floor(box.minZ);
			int n = Mth.ceil(box.maxZ);
			boolean bl = false;
			waterLevelField.set(this, -Double.MAX_VALUE);
			MutableBlockPos mutable = new MutableBlockPos();

			for (int o = i; o < j; o++)
			{
				for (int p = k; p < l; p++)
				{
					for (int q = m; q < n; q++)
					{
						mutable.set(o, p, q);
						FluidState fluidState = this.level().getFluidState(mutable);
						if (this.canFloatOn(fluidState))
						{
							float f = p + fluidState.getHeight(this.level(), mutable);
							waterLevelField.set(this, Math.max(f, (Double) waterLevelField.get(this)));
							bl |= box.minY < f;
						}
					}
				}
			}

			return bl;
		} catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Status getUnderWaterLocation()
	{
		AABB box = this.getBoundingBox();
		double d = box.maxY + 0.001;
		int i = Mth.floor(box.minX);
		int j = Mth.ceil(box.maxX);
		int k = Mth.floor(box.maxY);
		int l = Mth.ceil(d);
		int m = Mth.floor(box.minZ);
		int n = Mth.ceil(box.maxZ);
		boolean bl = false;
		MutableBlockPos mutable = new MutableBlockPos();

		for (int o = i; o < j; o++)
		{
			for (int p = k; p < l; p++)
			{
				for (int q = m; q < n; q++)
				{
					mutable.set(o, p, q);
					FluidState fluidState = this.level().getFluidState(mutable);
					if (this.canFloatOn(fluidState) && d < mutable.getY() + fluidState.getHeight(this.level(), mutable))
					{
						if (!fluidState.isSource())
						{
							return Status.UNDER_FLOWING_WATER;
						}

						bl = true;
					}
				}
			}
		}

		return bl ? Status.UNDER_WATER : null;
	}

	public float getWaterLevelAbove()
	{
		Class<Boat> boatEntityClass = Boat.class;

		try
		{
			Field lastYdField = getFieldSafe(boatEntityClass, "f_38281_", "lastYd");
			lastYdField.setAccessible(true);
			AABB box = this.getBoundingBox();
			int i = Mth.floor(box.minX);
			int j = Mth.ceil(box.maxX);
			int k = Mth.floor(box.maxY);
			int l = Mth.ceil(box.maxY - (Double) lastYdField.get(this));
			int m = Mth.floor(box.minZ);
			int n = Mth.ceil(box.maxZ);
			MutableBlockPos mutable = new MutableBlockPos();

			label47:
			for (int o = k; o < l; o++)
			{
				float f = 0.0F;

				for (int p = i; p < j; p++)
				{
					for (int q = m; q < n; q++)
					{
						mutable.set(p, o, q);
						FluidState fluidState = this.level().getFluidState(mutable);
						if (this.canFloatOn(fluidState))
						{
							f = Math.max(f, fluidState.getHeight(this.level(), mutable));
						}

						if (f >= 1.0F)
						{
							continue label47;
						}
					}
				}

				if (f < 1.0F)
				{
					return mutable.getY() + f;
				}
			}

			return l + 1;
		} catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void tick()
	{
		super.tick();
	}

	protected void checkFallDamage(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition)
	{
		if (onGround && this.brokenByFalling())
		{
			super.checkFallDamage(heightDifference, true, state, landedPosition);
		} else
		{
			if (!onGround)
			{
				super.checkFallDamage(heightDifference, false, state, landedPosition);
			}
		}
	}

	public ItemEntity spawnAtLocation(@NotNull ItemLike item)
	{
		if (item == this.getOverrideBoatType().getBaseItem())
		{
			return this.spawnAtLocation(new ItemStack(this.getOverrideBoatType().getBaseItem()));
		} else if (item == Items.STICK && !this.isExtraItemDropped)
		{
			this.isExtraItemDropped = true;
			return this.spawnAtLocation(this.getExtraDropItemStack());
		} else
		{
			return super.spawnAtLocation(item);
		}
	}

	public void resetFallDistance()
	{
		super.resetFallDistance();
		this.isExtraItemDropped = false;
	}
}
