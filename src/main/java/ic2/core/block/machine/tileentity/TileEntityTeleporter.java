package ic2.core.block.machine.tileentity;

import org.joml.Vector3f;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import ic2.core.block.tileentity.TileEntityBase;
import ic2.core.init.MainConfig;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TileEntityTeleporter extends TileEntityBase implements INetworkTileEntityEventListener
{
	private BlockPos target;
	private int targetCheckTicker = IC2.random.nextInt(1024);
	private int cooldown = 0;

	public TileEntityTeleporter(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.TELEPORTER, pos, state);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		if (nbt.contains("targetX"))
		{
			this.target = new BlockPos(nbt.getInt("targetX"), nbt.getInt("targetY"), nbt.getInt("targetZ"));
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if (this.target != null)
		{
			nbt.putInt("targetX", this.target.getX());
			nbt.putInt("targetY", this.target.getY());
			nbt.putInt("targetZ", this.target.getZ());
		}
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateComparatorLevel();
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean coolingDown = this.cooldown > 0;
		if (coolingDown)
		{
			this.cooldown--;
			IC2.network.get(true).updateTileEntityField(this, "cooldown");
		}

		Level world = this.getLevel();
		if (world.hasNeighborSignal(this.worldPosition) && this.target != null)
		{
			this.activate(false);
			List<Entity> entitiesNearby;
			if (coolingDown)
			{
				entitiesNearby = Collections.emptyList();
			} else
			{
				entitiesNearby = world.getEntitiesOfClass(Entity.class, new AABB(this.worldPosition.getX() - 1, this.worldPosition.getY(), this.worldPosition.getZ() - 1, this.worldPosition.getX() + 2, this.worldPosition.getY() + 3, this.worldPosition.getZ() + 2), EntitySelector.ENTITY_STILL_ALIVE);
			}

			if (!entitiesNearby.isEmpty() && this.verifyTarget())
			{
				double minDistanceSquared = Double.MAX_VALUE;
				Entity closestEntity = null;

				for (Entity entity : entitiesNearby)
				{
					if (entity.getVehicle() == null)
					{
						double distSquared = this.worldPosition.distToLowCornerSqr(entity.getX(), entity.getY(), entity.getZ());
						if (distSquared < minDistanceSquared)
						{
							minDistanceSquared = distSquared;
							closestEntity = entity;
						}
					}
				}

				assert closestEntity != null;
				this.teleport(closestEntity, Math.sqrt(this.worldPosition.distSqr(this.target)));
			} else if (++this.targetCheckTicker % 1024 == 0)
			{
				this.verifyTarget();
			}
		} else
		{
			this.shutdown(false);
		}
	}

	private boolean verifyTarget()
	{
		if (this.getLevel().getBlockEntity(this.target) instanceof TileEntityTeleporter)
		{
			return true;
		}

		this.target = null;
		this.updateComparatorLevel();
		this.shutdown(false);
		return false;
	}

	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		if (this.getActive())
		{
			if (this.cooldown > 0)
			{
				this.spawnGreenParticles(2, this.worldPosition);
			} else
			{
				this.spawnBlueParticles(2, this.worldPosition);
			}
		}
	}

	private void updateComparatorLevel()
	{
		int targetLevel = this.target != null ? 15 : 0;
		this.comparator.setLevel(targetLevel);
	}

	public void teleport(Entity user, double distance)
	{
		int weight = this.getWeightOf(user);
		if (weight != 0)
		{
			int energyCost = (int) (weight * Math.pow(distance + 10.0, 0.7) * 5.0);
			if (energyCost <= this.getAvailableEnergy())
			{
				this.consumeEnergy(energyCost);
				if (user instanceof ServerPlayer)
				{
					user.teleportTo(this.target.getX() + 0.5, this.target.getY() + 1.5 + user.getMyRidingOffset(), this.target.getZ() + 0.5);
				} else
				{
					user.absMoveTo(this.target.getX() + 0.5, this.target.getY() + 1.5 + user.getMyRidingOffset(), this.target.getZ() + 0.5, user.getYRot(), user.getXRot());
				}

				BlockEntity te = this.getLevel().getBlockEntity(this.target);
				assert te instanceof TileEntityTeleporter;
				((TileEntityTeleporter) te).onTeleportTo();
				IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
				if (user instanceof Player && distance >= 1000.0)
				{
					IC2.grantAdvancement((Player) user, "ic2/build_generator/build_batbox/build_mfeu/build_mfsu/build_teleporter/teleport_far_away");
				}
			}
		}
	}

	public void spawnBlueParticles(int n, BlockPos pos)
	{
		this.spawnParticles(n, pos, 0, 1);
	}

	public void spawnGreenParticles(int n, BlockPos pos)
	{
		this.spawnParticles(n, pos, 1, 0);
	}

	private void spawnParticles(int n, BlockPos pos, int green, int blue)
	{
		Level world = this.getLevel();
		RandomSource rnd = world.random;
		DustParticleOptions effect = new DustParticleOptions(new Vector3f(-1 / 255.0F, green / 255.0F, blue / 255.0F), 1.0F);

		for (int i = 0; i < n; i++)
		{
			world.addParticle(effect, pos.getX() + rnd.nextFloat(), pos.getY() + 1 + rnd.nextFloat(), pos.getZ() + rnd.nextFloat(), 0.0, 0.0, 0.0);
			world.addParticle(effect, pos.getX() + rnd.nextFloat(), pos.getY() + 2 + rnd.nextFloat(), pos.getZ() + rnd.nextFloat(), 0.0, 0.0, 0.0);
		}
	}

	public void consumeEnergy(int energy)
	{
		Level world = this.getLevel();
		List<IEnergyStorage> energySources = new LinkedList<>();

		for (Direction dir : Util.ALL_DIRS)
		{
			if (world.getBlockEntity(this.worldPosition.relative(dir)) instanceof IEnergyStorage energySource && energySource.isTeleporterCompatible(dir.getOpposite()) && energySource.getStored() > 0)
			{
				energySources.add(energySource);
			}
		}

		while (energy > 0)
		{
			int drain = (energy + energySources.size() - 1) / energySources.size();
			Iterator<IEnergyStorage> it = energySources.iterator();

			while (it.hasNext())
			{
				IEnergyStorage energySource = it.next();
				if (drain > energy)
				{
					drain = energy;
				}

				if (energySource.getStored() <= drain)
				{
					energy -= energySource.getStored();
					energySource.setStored(0);
					it.remove();
				} else
				{
					energy -= drain;
					energySource.addEnergy(-drain);
				}
			}
		}
	}

	public int getAvailableEnergy()
	{
		Level world = this.getLevel();
		int energy = 0;

		for (Direction dir : Util.ALL_DIRS)
		{
			if (world.getBlockEntity(this.worldPosition.relative(dir)) instanceof IEnergyStorage storage && storage.isTeleporterCompatible(dir.getOpposite()))
			{
				energy += storage.getStored();
			}
		}

		return energy;
	}

	public int getWeightOf(Entity user)
	{
		boolean teleporterUseInventoryWeight = ConfigUtil.getBool(MainConfig.get(), "balance/teleporterUseInventoryWeight");
		int weight = 0;
		if (user instanceof ItemEntity)
		{
			ItemStack is = ((ItemEntity) user).getItem();
			weight += 100 * StackUtil.getSize(is) / is.getMaxStackSize();
		} else if (user instanceof Animal || user instanceof AbstractMinecart || user instanceof Boat)
		{
			weight += 100;
		} else if (user instanceof Player)
		{
			weight += 1000;
			if (teleporterUseInventoryWeight)
			{
				for (ItemStack stack : ((Player) user).getInventory().items)
				{
					weight += getStackCost(stack);
				}
			}
		} else if (user instanceof Ghast)
		{
			weight += 2500;
		} else if (user instanceof WitherBoss)
		{
			weight += 5000;
		} else if (user instanceof EnderDragon)
		{
			weight += 10000;
		} else if (user instanceof Mob)
		{
			weight += 500;
		}

		if (teleporterUseInventoryWeight && user instanceof LivingEntity living)
		{
			for (ItemStack stack : living.getAllSlots())
			{
				weight += getStackCost(stack);
			}

			if (user instanceof Player)
			{
				ItemStack stack = living.getMainHandItem();
				weight -= getStackCost(stack);
			}
		}

		for (Entity passenger : user.getPassengers())
		{
			weight += this.getWeightOf(passenger);
		}

		return weight;
	}

	private static int getStackCost(ItemStack stack)
	{
		return StackUtil.isEmpty(stack) ? 0 : 100 * StackUtil.getSize(stack) / stack.getMaxStackSize();
	}

	private void onTeleportTo()
	{
		this.cooldown = 20;
	}

	@Override
	protected boolean canEntityDestroy(Entity entity)
	{
		return !(entity instanceof EnderDragon) && !(entity instanceof WitherBoss);
	}

	public boolean hasTarget()
	{
		return this.target != null;
	}

	public BlockPos getTarget()
	{
		return this.target;
	}

	public void setTarget(BlockPos pos)
	{
		this.target = pos;
		this.updateComparatorLevel();
		IC2.network.get(true).updateTileEntityField(this, "target");
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_TELEPORTER_CHARGE;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("target");
		return ret;
	}

	@Override
	public void onNetworkEvent(int event)
	{
		if (event == 0)
		{
			if (this.level == null)
			{
				return;
			}

			this.level.playLocalSound(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), Ic2SoundEvents.MACHINE_TELEPORTER_USE, SoundSource.BLOCKS, 1.0F, 1.0F, true);
			this.spawnBlueParticles(20, this.worldPosition);
			this.spawnBlueParticles(20, this.target);
		} else
		{
			IC2.sideProxy.displayError("An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID " + event + ", tile entity below)\nT: " + this + " (" + this.worldPosition + ")");
		}
	}
}
