package ic2.core.entity;

import ic2.core.IC2;
import ic2.core.Ic2Explosion;
import ic2.core.Ic2Player;
import ic2.core.ref.Ic2Entities;
import ic2.core.util.StackUtil;
import ic2.core.util.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LaserBulletEntity extends ThrowableProjectile
{
	public static final double laserSpeed = 1.0;
	public LivingEntity owner;
	public boolean isSmeltMode = false;
	public boolean removeBlock = false;
	public float range = 0.0F;
	public float power = 0.0F;
	public int blockBreaks = 0;
	public boolean isExplosiveMode = false;

	public LaserBulletEntity(Level world)
	{
		super(Ic2Entities.LASER_BULLET, world);
	}

	public LaserBulletEntity(EntityType<? extends LaserBulletEntity> arg, Level arg2)
	{
		super(arg, arg2);
	}

	public LaserBulletEntity(Level world, LivingEntity owner)
	{
		super(Ic2Entities.LASER_BULLET, owner, world);
	}

	public LaserBulletEntity(Level world, Vector3 start, Vector3 dir, LivingEntity owner, float range, float power, int blockBreaks, boolean isExplosiveMode)
	{
		this(world, owner);
		this.owner = owner;
		this.absMoveTo(start.x, start.y, start.z);
		this.range = range;
		this.power = power;
		this.blockBreaks = blockBreaks;
		this.isExplosiveMode = isExplosiveMode;
	}

	protected float getGravity()
	{
		return 0.0F;
	}

	protected void defineSynchedData()
	{
	}

	public void tick()
	{
		super.tick();
		if (IC2.sideProxy.isSimulating() && (this.range < 1.0F || this.power <= 0.0F || this.blockBreaks <= 0))
		{
			if (this.isExplosiveMode)
			{
				this.explode();
			}

			this.remove(RemovalReason.DISCARDED);
		} else
		{
			this.power -= 0.5F;
		}
	}

	protected void onHitBlock(BlockHitResult blockHitResult)
	{
		super.onHitBlock(blockHitResult);
		this.handleHit(blockHitResult);
	}

	protected void onHitEntity(EntityHitResult entityHitResult)
	{
		super.onHitEntity(entityHitResult);
		this.handleHit(entityHitResult);
	}

	protected void handleHit(HitResult hitResult)
	{
		if (this.isExplosiveMode)
		{
			this.explode();
			this.remove(RemovalReason.DISCARDED);
		} else
		{
			switch (hitResult.getType())
			{
				case ENTITY:
					if (this.hitEntity(((EntityHitResult) hitResult).getEntity()))
					{
						this.power -= 0.5F;
					} else
					{
						this.remove(RemovalReason.DISCARDED);
					}
					break;
				case BLOCK:
					assert hitResult instanceof BlockHitResult;
					BlockHitResult blockHitResult = (BlockHitResult) hitResult;
					if (!this.hitBlock(blockHitResult.getBlockPos(), blockHitResult.getDirection()))
					{
						this.power -= 0.5F;
					} else
					{
						this.remove(RemovalReason.DISCARDED);
					}
					break;
				default:
					throw new RuntimeException("invalid hit type: " + hitResult.getType());
			}
		}
	}

	private void explode()
	{
		Level world = this.getCommandSenderWorld();
		Ic2Explosion explosion = new Ic2Explosion(world, this, this.getX(), this.getY(), this.getZ(), 5.0F, 0.85F);
		explosion.doExplosion();
	}

	private boolean hitEntity(Entity entity)
	{
		int damage = (int) this.power;
		if (damage > 0)
		{
			entity.setSecondsOnFire(damage * (this.isSmeltMode ? 2 : 1));
			return entity.hurt(this.level().damageSources().mobProjectile(this, this.owner), damage);
		} else
		{
			return true;
		}
	}

	private boolean hitBlock(BlockPos pos, Direction side)
	{
		Level world = this.getCommandSenderWorld();
		Player playerOwner = this.owner instanceof Player ? (Player) this.owner : Ic2Player.get(world);
		if (playerOwner == null)
		{
			return false;
		}

		if (playerOwner.blockActionRestricted(world, pos, Objects.requireNonNull(playerOwner.getServer()).getDefaultGameType()))
		{
			return false;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		boolean dropBlock = true;
		if (world.getBlockState(pos).isAir()
			|| block == Blocks.GLASS
			|| block == Blocks.GLASS_PANE
			|| block instanceof StainedGlassPaneBlock
			|| block instanceof StainedGlassBlock)
		{
			return false;
		}

		if (world.isClientSide)
		{
			return true;
		}

		float hardness = state.getDestroySpeed(world, pos);
		if (hardness < 0.0F)
		{
			this.remove(RemovalReason.DISCARDED);
			return true;
		}

		this.power -= hardness / 1.5F;
		if (this.power < 0.0F)
		{
			return true;
		}

		List<ItemStack> replacements = new ArrayList<>();
		if (block == Blocks.TNT )
		{
			block.wasExploded(
				world, pos, new Explosion(world, this, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F, false, BlockInteraction.DESTROY)
			);
		} else if (this.isSmeltMode)
		{
			if (state.isFlammable(world, pos, side))
			{
				dropBlock = false;
			} else
			{
				for (ItemStack isa : StackUtil.getDrops(world, pos, state, block, 0))
				{
					this.appendSmeltItemStack(block, isa, replacements);
				}

				dropBlock = replacements.isEmpty();
			}
		}

		if (this.removeBlock)
		{
			if (dropBlock)
			{
				Block.dropResources(state, world, pos);
			}

			world.removeBlock(pos, false);

			for (ItemStack replacement : replacements)
			{
				if (!StackUtil.placeBlock(replacement, world, pos))
				{
					StackUtil.dropAsEntity(world, pos, replacement);
				}

				this.power = 0.0F;
			}

			if (world.random.nextInt(10) == 0 && state.isFlammable(world, pos, Direction.UP))
			{
				world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
			}
		}

		this.blockBreaks--;
		return true;
	}

	private void appendSmeltItemStack(Block targetBlock, ItemStack inputItemStack, List<ItemStack> replacementList)
	{
		if (inputItemStack.getItem() instanceof BlockItem && ((BlockItem) inputItemStack.getItem()).getBlock() != targetBlock)
		{
			inputItemStack = new ItemStack(targetBlock.asItem());
		}

		SmeltingRecipe recipe = (SmeltingRecipe) IC2.sideProxy
			.getRecipeManager()
			.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(new ItemStack[] { inputItemStack }), this.getCommandSenderWorld())
			.orElse(null);
		if (recipe != null)
		{
			ItemStack replacementStack = recipe.getResultItem(this.getCommandSenderWorld().registryAccess());
			if (!StackUtil.isEmpty(replacementStack))
			{
				replacementList.add(replacementStack);
			}
		}
	}

	public void init(LivingEntity owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, boolean removeBlock)
	{
		this.owner = owner;
		this.range = range;
		this.power = power;
		this.blockBreaks = blockBreaks;
		this.removeBlock = removeBlock;
		this.isExplosiveMode = explosive;
		this.isSmeltMode = smelt;
	}
}
