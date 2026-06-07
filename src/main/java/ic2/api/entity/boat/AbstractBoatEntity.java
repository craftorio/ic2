package ic2.api.entity.boat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Boat.Status;
import net.minecraft.world.entity.vehicle.Boat.Type;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractBoatEntity extends Boat
{
	protected boolean isExtraItemDropped = false;

	public AbstractBoatEntity(EntityType<? extends Boat> entityType, Level world)
	{
		super(entityType, world);
	}

	public AbstractBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world, double x, double y, double z)
	{
		this(entityType, world);
		this.m_6034_(x, y, z);
		this.f_19854_ = x;
		this.f_19855_ = y;
		this.f_19856_ = z;
	}

	public void m_38332_(Type type)
	{
		super.m_38332_(Type.OAK);
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
		return fluidState.m_205070_(FluidTags.f_13131_);
	}

	protected SoundEvent m_38370_()
	{
		switch (this.checkLocation())
		{
			case IN_WATER:
			case UNDER_WATER:
			case UNDER_FLOWING_WATER:
				return SoundEvents.f_11707_;
			case ON_LAND:
				return SoundEvents.f_11706_;
			default:
				return null;
		}
	}

	private Status checkLocation()
	{
		Class<Boat> boatEntityClass = Boat.class;

		try
		{
			Field waterLevelField = boatEntityClass.getDeclaredField("waterLevel");
			Field nearbySlipperinessField = boatEntityClass.getDeclaredField("nearbySlipperiness");
			waterLevelField.setAccessible(true);
			nearbySlipperinessField.setAccessible(true);
			Status location = this.getUnderWaterLocation();
			if (location != null)
			{
				waterLevelField.set(this, this.m_20191_().maxY);
				return location;
			} else if (this.checkBoatInWater())
			{
				return Status.IN_WATER;
			} else
			{
				float f = this.m_38377_();
				if (f > 0.0F)
				{
					nearbySlipperinessField.set(this, f);
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
			Field waterLevelField = boatEntityClass.getDeclaredField("waterLevel");
			waterLevelField.setAccessible(true);
			AABB box = this.m_20191_();
			int i = Mth.m_14107_(box.minX);
			int j = Mth.m_14165_(box.maxX);
			int k = Mth.m_14107_(box.minY);
			int l = Mth.m_14165_(box.minY + 0.001);
			int m = Mth.m_14107_(box.minZ);
			int n = Mth.m_14165_(box.maxZ);
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
						FluidState fluidState = this.f_19853_.m_6425_(mutable);
						if (this.canFloatOn(fluidState))
						{
							float f = p + fluidState.m_76155_(this.f_19853_, mutable);
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
		AABB box = this.m_20191_();
		double d = box.maxY + 0.001;
		int i = Mth.m_14107_(box.minX);
		int j = Mth.m_14165_(box.maxX);
		int k = Mth.m_14107_(box.maxY);
		int l = Mth.m_14165_(d);
		int m = Mth.m_14107_(box.minZ);
		int n = Mth.m_14165_(box.maxZ);
		boolean bl = false;
		MutableBlockPos mutable = new MutableBlockPos();

		for (int o = i; o < j; o++)
		{
			for (int p = k; p < l; p++)
			{
				for (int q = m; q < n; q++)
				{
					mutable.set(o, p, q);
					FluidState fluidState = this.f_19853_.m_6425_(mutable);
					if (this.canFloatOn(fluidState) && d < mutable.getY() + fluidState.m_76155_(this.f_19853_, mutable))
					{
						if (!fluidState.m_76170_())
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

	public float m_38371_()
	{
		Class<Boat> boatEntityClass = Boat.class;

		try
		{
			Field fallVelocityField = boatEntityClass.getDeclaredField("fallVelocity");
			fallVelocityField.setAccessible(true);
			AABB box = this.m_20191_();
			int i = Mth.m_14107_(box.minX);
			int j = Mth.m_14165_(box.maxX);
			int k = Mth.m_14107_(box.maxY);
			int l = Mth.m_14165_(box.maxY - (Double) fallVelocityField.get(this));
			int m = Mth.m_14107_(box.minZ);
			int n = Mth.m_14165_(box.maxZ);
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
						FluidState fluidState = this.f_19853_.m_6425_(mutable);
						if (this.canFloatOn(fluidState))
						{
							f = Math.max(f, fluidState.m_76155_(this.f_19853_, mutable));
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

	private void invokePrivateMethod(Class<Boat> boatEntityClass, String methodName, Object... parameters)
	{
		try
		{
			Class<?>[] parameterTypes = new Class[parameters.length];

			for (int i = 0; i < parameters.length; i++)
			{
				parameterTypes[i] = parameters[i].getClass();
			}

			Method method = boatEntityClass.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			method.invoke(this, parameters);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void m_8119_()
	{
		Class<Boat> boatEntityClass = Boat.class;

		try
		{
			Field lastLocationField = boatEntityClass.getDeclaredField("lastLocation");
			Field locationField = boatEntityClass.getDeclaredField("location");
			Field ticksUnderwaterField = boatEntityClass.getDeclaredField("ticksUnderwater");
			Field paddlePhasesField = boatEntityClass.getDeclaredField("paddlePhases");
			lastLocationField.setAccessible(true);
			locationField.setAccessible(true);
			ticksUnderwaterField.setAccessible(true);
			paddlePhasesField.setAccessible(true);
			lastLocationField.set(this, locationField.get(this));
			locationField.set(this, this.checkLocation());
			Status location = (Status) locationField.get(this);
			float newTicksUnderWater = (Float) ticksUnderwaterField.get(this) + 1.0F;
			float[] paddlePhases = (float[]) paddlePhasesField.get(this);
			ticksUnderwaterField.set(this, location != Status.UNDER_WATER && location != Status.UNDER_FLOWING_WATER ? 0.0F : newTicksUnderWater);
			float ticksUnderWater = (Float) ticksUnderwaterField.get(this);
			if (!this.f_19853_.isClientSide && ticksUnderWater >= 60.0F)
			{
				this.m_20153_();
			}

			if (this.m_38385_() > 0)
			{
				this.m_38354_(this.m_38385_() - 1);
			}

			if (this.m_38384_() > 0.0F)
			{
				this.m_38311_(this.m_38384_() - 1.0F);
			}

			this.m_6075_();
			this.invokePrivateMethod(boatEntityClass, "updatePositionAndRotation");
			if (this.m_6109_())
			{
				if (!(this.m_146895_() instanceof Player))
				{
					this.m_38339_(false, false);
				}

				this.invokePrivateMethod(boatEntityClass, "updateVelocity");
				if (this.f_19853_.isClientSide)
				{
					this.invokePrivateMethod(boatEntityClass, "updatePaddles");
					this.f_19853_.m_5503_(new ServerboundPaddleBoatPacket(this.m_38313_(0), this.m_38313_(1)));
				}

				this.m_6478_(MoverType.SELF, this.m_20184_());
			} else
			{
				this.m_20256_(Vec3.f_82478_);
			}

			this.invokePrivateMethod(boatEntityClass, "handleBubbleColumn");

			for (int i = 0; i <= 1; i++)
			{
				if (this.m_38313_(i))
				{
					SoundEvent soundEvent;
					if (!this.m_20067_()
						&& paddlePhases[i] % (float) (Math.PI * 2) <= (float) (Math.PI / 4)
						&& (paddlePhases[i] + (float) (Math.PI / 8)) % (float) (Math.PI * 2) >= (float) (Math.PI / 4)
						&& (soundEvent = this.m_38370_()) != null)
					{
						Vec3 vec3d = this.m_20252_(1.0F);
						double d = i == 1 ? -vec3d.f_82481_ : vec3d.f_82481_;
						double e = i == 1 ? vec3d.f_82479_ : -vec3d.f_82479_;
						this.f_19853_
							.m_6263_(
								null,
								this.getX() + d,
								this.getY(),
								this.getZ() + e,
								soundEvent,
								this.m_5720_(),
								1.0F,
								0.8F + 0.4F * this.f_19796_.nextFloat()
							);
					}

					paddlePhases[i] += (float) (Math.PI / 8);
				} else
				{
					paddlePhases[i] = 0.0F;
				}
			}

			paddlePhasesField.set(this, paddlePhases);
			this.m_20101_();
			List<Entity> list = this.f_19853_.m_6249_(this, this.m_20191_().m_82377_(0.2F, -0.01F, 0.2F), EntitySelector.m_20421_(this));
			if (!list.isEmpty())
			{
				boolean bl = !this.f_19853_.isClientSide && !(this.m_6688_() instanceof Player);

				for (Entity entity : list)
				{
					if (!entity.m_20363_(this))
					{
						if (bl
							&& this.m_20197_().size() < this.m_213801_()
							&& !entity.m_20159_()
							&& entity.m_20205_() < this.m_20205_()
							&& entity instanceof LivingEntity
							&& !(entity instanceof WaterAnimal)
							&& !(entity instanceof Player))
						{
							entity.m_20329_(this);
						} else
						{
							this.m_7334_(entity);
						}
					}
				}
			}
		} catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void m_7840_(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition)
	{
		if (onGround && this.brokenByFalling())
		{
			super.m_7840_(heightDifference, true, state, landedPosition);
		} else
		{
			if (!onGround)
			{
				super.m_7840_(heightDifference, false, state, landedPosition);
			}
		}
	}

	public ItemEntity m_19998_(ItemLike item)
	{
		if (item == this.m_38387_().m_38434_())
		{
			return this.m_19983_(new ItemStack(this.getOverrideBoatType().getBaseItem()));
		} else if (item == Items.f_42398_ && !this.isExtraItemDropped)
		{
			this.isExtraItemDropped = true;
			return this.m_19983_(this.getExtraDropItemStack());
		} else
		{
			return super.m_19998_(item);
		}
	}

	public void m_183634_()
	{
		super.m_183634_();
		this.isExtraItemDropped = false;
	}
}
