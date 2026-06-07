package ic2.core;

import ic2.api.tile.ExplosionWhitelist;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ic2Explosion extends Explosion
{
	private final Level worldObj;
	private final Entity exploder;
	private final double explosionX;
	private final double explosionY;
	private final double explosionZ;
	private final int worldMaxHeight;
	private final int worldMinHeight;
	private final float power;
	private final float explosionDropRate;
	private final Ic2Explosion.Type type;
	private final int radiationRange;
	private final LivingEntity igniter;
	private final Random rng = new Random();
	private final double maxDistance;
	private final int areaSize;
	private final int areaX;
	private final int areaZ;
	private final DamageSource damageSource;
	private final List<Ic2Explosion.EntityDamage> entitiesInRange = new ArrayList<>();
	private final long[][] destroyedBlockPositions;
	private BlockGetter chunkCache;
	private static final double dropPowerLimit = 8.0;
	private static final double damageAtDropPowerLimit = 32.0;
	private static final double accelerationAtDropPowerLimit = 0.7;
	private static final double motionLimit = 60.0;
	private static final int secondaryRayCount = 5;
	private static final int bitSetElementSize = 2;

	public Ic2Explosion(Level world, Entity entity, double x, double y, double z, float power, float drop)
	{
		this(world, entity, x, y, z, power, drop, Ic2Explosion.Type.Normal);
	}

	public Ic2Explosion(Level world, Entity entity, double x, double y, double z, float power, float drop, Ic2Explosion.Type type)
	{
		this(world, entity, x, y, z, power, drop, type, null, 0);
	}

	public Ic2Explosion(Level world, Entity entity, BlockPos pos, float power, float drop, Ic2Explosion.Type type)
	{
		this(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, drop, type);
	}

	public Ic2Explosion(
		Level world, Entity entity, double x, double y, double z, float power, float drop, Ic2Explosion.Type type, LivingEntity igniter, int radiationRange
	)
	{
		super(world, entity, x, y, z, power, true, BlockInteraction.DESTROY);
		this.worldObj = world;
		this.exploder = entity;
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
		this.worldMaxHeight = IC2.getWorldMaxHeight(world);
		this.worldMinHeight = IC2.getWorldMinHeight(world);
		this.power = power;
		this.explosionDropRate = drop;
		this.type = type;
		this.igniter = igniter;
		this.radiationRange = radiationRange;
		this.maxDistance = power / 0.4;
		int maxDistanceInt = (int) Math.ceil(this.maxDistance);
		this.areaSize = maxDistanceInt * 2;
		this.areaX = Util.roundToNegInf(x) - maxDistanceInt;
		this.areaZ = Util.roundToNegInf(z) - maxDistanceInt;
		if (this.isNuclear())
		{
			this.damageSource = Ic2DamageSource.getNukeSource(igniter);
		} else
		{
			this.damageSource = DamageSource.m_19373_(igniter);
		}

		this.destroyedBlockPositions = new long[this.worldMaxHeight - this.worldMinHeight][];
	}

	public Ic2Explosion(Level world, Entity entity, BlockPos pos, int i, float f, Ic2Explosion.Type heat)
	{
		this(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, i, f, heat);
	}

	public void doExplosion()
	{
		if (!(this.power <= 0.0F))
		{
			Vec3 position = new Vec3(this.explosionX, this.explosionY, this.explosionZ);
			if (IC2.envProxy.announceExplosion(this.worldObj, this.exploder, position, this.power, this.igniter, this.radiationRange, this.maxDistance))
			{
				int range = this.areaSize / 2;
				BlockPos pos = new BlockPos(this.explosionX, this.explosionY, this.explosionZ);
				BlockPos start = pos.offset(-range, -range, -range);
				BlockPos end = pos.offset(range, range, range);
				this.chunkCache = new PathNavigationRegion(this.worldObj, start, end);

				for (Entity entity : this.worldObj.m_45933_(this.exploder, new AABB(start, end)))
				{
					if (entity instanceof LivingEntity || entity instanceof ItemEntity)
					{
						int distance = (int) (
							Util.square(entity.getX() - this.explosionX)
								+ Util.square(entity.getY() - this.explosionY)
								+ Util.square(entity.getZ() - this.explosionZ)
						);
						double health = getEntityHealth(entity);
						this.entitiesInRange.add(new Ic2Explosion.EntityDamage(entity, distance, health));
					}
				}

				boolean entitiesAreInRange = !this.entitiesInRange.isEmpty();
				if (entitiesAreInRange)
				{
					this.entitiesInRange.sort(Comparator.comparingInt(a -> a.distance));
				}

				int steps = (int) Math.ceil(Math.PI / Math.atan(1.0 / this.maxDistance));
				MutableBlockPos tmpPos = new MutableBlockPos();

				for (int phi_n = 0; phi_n < 2 * steps; phi_n++)
				{
					for (int theta_n = 0; theta_n < steps; theta_n++)
					{
						double phi = (Math.PI * 2) / steps * phi_n;
						double theta = Math.PI / steps * theta_n;
						this.shootRay(
							this.explosionX,
							this.explosionY,
							this.explosionZ,
							phi,
							theta,
							this.power,
							entitiesAreInRange && phi_n % 8 == 0 && theta_n % 8 == 0,
							tmpPos
						);
					}
				}

				for (Ic2Explosion.EntityDamage entry : this.entitiesInRange)
				{
					Entity entity = entry.entity;
					entity.hurt(this.damageSource, (float) entry.damage);
					if (entity instanceof Player player && this.isNuclear() && this.igniter != null && player == this.igniter && player.m_21223_() <= 0.0F)
					{
						IC2.achievements.issueAchievement(player, "dieFromOwnNuke");
					}

					double motionSq = Util.square(entry.motionX) + Util.square(entry.motionY) + Util.square(entry.motionZ);
					double reduction = motionSq > 3600.0 ? Math.sqrt(3600.0 / motionSq) : 1.0;
					entity.m_20256_(entity.m_20184_().m_82520_(entry.motionX * reduction, entry.motionY * reduction, entry.motionZ * reduction));
				}

				if (this.isNuclear() && this.radiationRange >= 1)
				{
					for (Mob entity : this.worldObj
						.getEntitiesOfClass(
							Mob.class,
							new AABB(
								this.explosionX - this.radiationRange,
								this.explosionY - this.radiationRange,
								this.explosionZ - this.radiationRange,
								this.explosionX + this.radiationRange,
								this.explosionY + this.radiationRange,
								this.explosionZ + this.radiationRange
							),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR
						))
					{
						if (!ItemArmorHazmat.hasCompleteHazmat(entity))
						{
							double distance = entity.m_20182_().m_82554_(position);
							int hungerLength = (int) (120.0 * (this.radiationRange - distance));
							int poisonLength = (int) (80.0 * (this.radiationRange / 3 - distance));
							if (hungerLength >= 0)
							{
								entity.m_7292_(new MobEffectInstance(MobEffects.f_19612_, hungerLength, 0));
							}

							if (poisonLength >= 0)
							{
								Ic2Potion.radiation.applyTo(entity, poisonLength, 0);
							}
						}
					}
				}

				IC2.network.get(true).initiateExplosionEffect(this.worldObj, position, this.type);
				RandomSource rng = this.worldObj.random;
				boolean doDrops = this.worldObj.m_46469_().m_46207_(GameRules.f_46136_);
				Map<Ic2Explosion.XZPosition, Map<ItemComparableItemStack, Ic2Explosion.DropData>> blocksToDrop = new HashMap<>();
				Builder builder = new Builder((ServerLevel) this.worldObj)
					.m_230911_(this.worldObj.random)
					.m_78972_(LootContextParams.f_81460_, new Vec3(this.explosionX, this.explosionY, this.explosionZ))
					.m_78972_(LootContextParams.f_81463_, ItemStack.EMPTY)
					.m_78984_(LootContextParams.f_81455_, this.exploder)
					.m_78972_(LootContextParams.f_81464_, this.power);

				for (int destroyedBlockPosIndex = 0; destroyedBlockPosIndex < this.destroyedBlockPositions.length; destroyedBlockPosIndex++)
				{
					int y = destroyedBlockPosIndex + this.worldMinHeight;
					long[] bitSet = this.destroyedBlockPositions[destroyedBlockPosIndex];
					if (bitSet != null)
					{
						int index = -2;

						while ((index = nextSetIndex(index + 2, bitSet, 2)) != -1)
						{
							int realIndex = index / 2;
							int z = realIndex / this.areaSize;
							int x = realIndex - z * this.areaSize;
							x += this.areaX;
							z += this.areaZ;
							tmpPos.set(x, y, z);
							BlockState state = this.chunkCache.getBlockState(tmpPos);
							Block block = state.getBlock();
							if (this.power < 20.0F)
							{
							}

							if (doDrops && block.m_6903_(this) && getAtIndex(index, bitSet, 2) == 1)
							{
								BlockEntity be = state.m_155947_() ? this.worldObj.getBlockEntity(tmpPos) : null;
								builder.m_78984_(LootContextParams.f_81462_, be);

								for (ItemStack stack : state.m_60724_(builder))
								{
									if (!(rng.nextFloat() > this.explosionDropRate))
									{
										Ic2Explosion.XZPosition xZposition = new Ic2Explosion.XZPosition(x / 2, z / 2);
										Map<ItemComparableItemStack, Ic2Explosion.DropData> map = blocksToDrop.get(xZposition);
										if (map == null)
										{
											map = new HashMap<>();
											blocksToDrop.put(xZposition, map);
										}

										ItemComparableItemStack isw = new ItemComparableItemStack(stack, false);
										Ic2Explosion.DropData data = map.get(isw);
										if (data == null)
										{
											data = new Ic2Explosion.DropData(StackUtil.getSize(stack), y);
											map.put(isw.copy(), data);
										} else
										{
											data.add(StackUtil.getSize(stack), y);
										}
									}
								}
							}

							this.worldObj.m_7731_(tmpPos, Blocks.f_50016_.defaultBlockState(), 3);
							block.m_7592_(this.worldObj, tmpPos, this);
						}
					}
				}

				for (Entry<Ic2Explosion.XZPosition, Map<ItemComparableItemStack, Ic2Explosion.DropData>> entry : blocksToDrop.entrySet())
				{
					Ic2Explosion.XZPosition xZposition = entry.getKey();

					for (Entry<ItemComparableItemStack, Ic2Explosion.DropData> entry2 : entry.getValue().entrySet())
					{
						ItemComparableItemStack isw = entry2.getKey();
						int count = entry2.getValue().n;

						while (count > 0)
						{
							int stackSize = Math.min(count, 64);
							ItemEntity entityitem = new ItemEntity(
								this.worldObj,
								(xZposition.x + this.worldObj.random.nextFloat()) * 2.0F,
								entry2.getValue().maxY + 0.5,
								(xZposition.z + this.worldObj.random.nextFloat()) * 2.0F,
								isw.toStack(stackSize)
							);
							entityitem.m_32060_();
							this.worldObj.addFreshEntity(entityitem);
							count -= stackSize;
						}
					}
				}
			}
		}
	}

	public void destroy(int x, int y, int z, boolean noDrop)
	{
		this.destroyUnchecked(x, y, z, noDrop);
	}

	private void destroyUnchecked(int x, int y, int z, boolean noDrop)
	{
		int index = (z - this.areaZ) * this.areaSize + (x - this.areaX);
		index *= 2;
		long[] array = this.destroyedBlockPositions[y - this.worldMinHeight];
		if (array == null)
		{
			array = makeArray(Util.square(this.areaSize), 2);
			this.destroyedBlockPositions[y - this.worldMinHeight] = array;
		}

		if (noDrop)
		{
			setAtIndex(index, array, 3);
		} else
		{
			setAtIndex(index, array, 1);
		}
	}

	private void shootRay(double x, double y, double z, double phi, double theta, double power1, boolean killEntities, MutableBlockPos tmpPos)
	{
		double deltaX = Math.sin(theta) * Math.cos(phi);
		double deltaY = Math.cos(theta);
		double deltaZ = Math.sin(theta) * Math.sin(phi);
		int step = 0;

		while (true)
		{
			int blockY = Util.roundToNegInf(y);
			if (blockY < this.worldMinHeight || blockY >= this.worldMaxHeight)
			{
				break;
			}

			int blockX = Util.roundToNegInf(x);
			int blockZ = Util.roundToNegInf(z);
			tmpPos.set(blockX, blockY, blockZ);
			BlockState state = this.chunkCache.getBlockState(tmpPos);
			Block block = state.getBlock();
			double absorption = this.getAbsorption(state, tmpPos);
			if (absorption < 0.0)
			{
				break;
			}

			if (absorption > 1000.0 && !ExplosionWhitelist.isBlockWhitelisted(block))
			{
				absorption = 0.5;
			} else
			{
				if (absorption > power1)
				{
					break;
				}

				if (block == Blocks.f_50069_ || block != Blocks.f_50016_ && !state.isAir())
				{
					this.destroyUnchecked(blockX, blockY, blockZ, power1 > 8.0);
				}
			}

			if (killEntities && (step + 4) % 8 == 0 && !this.entitiesInRange.isEmpty() && power1 >= 0.25)
			{
				this.damageEntities(x, y, z, step, power1);
			}

			if (absorption > 10.0)
			{
				for (int i = 0; i < 5; i++)
				{
					this.shootRay(x, y, z, this.rng.nextDouble() * 2.0 * Math.PI, this.rng.nextDouble() * Math.PI, absorption * 0.4, false, tmpPos);
				}
			}

			power1 -= absorption;
			x += deltaX;
			y += deltaY;
			z += deltaZ;
			step++;
		}
	}

	private double getAbsorption(BlockState state, BlockPos pos)
	{
		double ret = 0.5;
		Block block = state.getBlock();
		if (block != Blocks.f_50016_ && !state.isAir())
		{
			if (block == Blocks.f_49990_ && this.type != Ic2Explosion.Type.Normal)
			{
				ret++;
			} else
			{
				float resistance = IC2.envProxy.getBlastResistance(state, this.worldObj, pos, this);
				if (resistance < 0.0F)
				{
					return resistance;
				}

				double extra = (resistance + 4.0F) * 0.3;
				if (this.type != Ic2Explosion.Type.Heat)
				{
					ret += extra;
				} else
				{
					ret += extra * 6.0;
				}
			}

			return ret;
		} else
		{
			return ret;
		}
	}

	private void damageEntities(double x, double y, double z, int step, double power)
	{
		int index;
		if (step != 4)
		{
			int distanceMin = Util.square(step - 5);
			int indexStart = 0;
			int indexEnd = this.entitiesInRange.size() - 1;

			do
			{
				index = (indexStart + indexEnd) / 2;
				int distance = this.entitiesInRange.get(index).distance;
				if (distance < distanceMin)
				{
					indexStart = index + 1;
				} else if (distance > distanceMin)
				{
					indexEnd = index - 1;
				} else
				{
					indexEnd = index;
				}
			} while (indexStart < indexEnd);
		} else
		{
			index = 0;
		}

		int distanceMax = Util.square(step + 5);

		for (int i = index; i < this.entitiesInRange.size(); i++)
		{
			Ic2Explosion.EntityDamage entry = this.entitiesInRange.get(i);
			if (entry.distance >= distanceMax)
			{
				break;
			}

			Entity entity = entry.entity;
			if (Util.square(entity.getX() - x) + Util.square(entity.getY() - y) + Util.square(entity.getZ() - z) <= 25.0)
			{
				double damage = 4.0 * power;
				entry.damage += damage;
				entry.health -= damage;
				double dx = entity.getX() - this.explosionX;
				double dy = entity.getY() - this.explosionY;
				double dz = entity.getZ() - this.explosionZ;
				double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
				entry.motionX += dx / distance * 0.0875 * power;
				entry.motionY += dy / distance * 0.0875 * power;
				entry.motionZ += dz / distance * 0.0875 * power;
				if (entry.health <= 0.0)
				{
					entity.hurt(this.damageSource, (float) entry.damage);
					if (!entity.m_6084_())
					{
						this.entitiesInRange.remove(i);
						i--;
					}
				}
			}
		}
	}

	public LivingEntity m_46079_()
	{
		return this.igniter;
	}

	private boolean isNuclear()
	{
		return this.type == Ic2Explosion.Type.Nuclear;
	}

	private static double getEntityHealth(Entity entity)
	{
		return entity instanceof ItemEntity ? 5.0 : Double.POSITIVE_INFINITY;
	}

	private static long[] makeArray(int size, int step)
	{
		return new long[(size * step + 8 - step) / 8];
	}

	private static int nextSetIndex(int start, long[] array, int step)
	{
		int offset = start % 8;

		for (int i = start / 8; i < array.length; i++)
		{
			long aval = array[i];
			int j = offset;

			while (j < 8)
			{
				int val = (int) (aval >> j & (1 << step) - 1);
				if (val != 0)
				{
					return i * 8 + j;
				}

				j += step;
			}

			offset = 0;
		}

		return -1;
	}

	private static int getAtIndex(int index, long[] array, int step)
	{
		return (int) (array[index / 8] >>> index % 8 & (1 << step) - 1);
	}

	private static void setAtIndex(int index, long[] array, int value)
	{
		array[index / 8] = array[index / 8] | value << index % 8;
	}

	private static class DropData
	{
		int n;
		int maxY;

		DropData(int n1, int y)
		{
			this.n = n1;
			this.maxY = y;
		}

		public Ic2Explosion.DropData add(int n1, int y)
		{
			this.n += n1;
			if (y > this.maxY)
			{
				this.maxY = y;
			}

			return this;
		}
	}

	private static class EntityDamage
	{
		final Entity entity;
		final int distance;
		double health;
		double damage;
		double motionX;
		double motionY;
		double motionZ;

		EntityDamage(Entity entity, int distance, double health)
		{
			this.entity = entity;
			this.distance = distance;
			this.health = health;
		}
	}

	public enum Type
	{
		Normal,
		Heat,
		Electrical,
		Nuclear;
	}

	private static class XZPosition
	{
		int x;
		int z;

		XZPosition(int x1, int z1)
		{
			this.x = x1;
			this.z = z1;
		}

		@Override
		public boolean equals(Object obj)
		{
			return !(obj instanceof Ic2Explosion.XZPosition xzPosition) ? false : xzPosition.x == this.x && xzPosition.z == this.z;
		}

		@Override
		public int hashCode()
		{
			return this.x * 31 ^ this.z;
		}
	}
}
