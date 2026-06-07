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
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
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
import net.minecraft.world.level.material.Material;
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
		this.m_20248_(start.x, start.y, start.z);
		this.range = range;
		this.power = power;
		this.blockBreaks = blockBreaks;
		this.isExplosiveMode = isExplosiveMode;
	}

	protected float m_7139_()
	{
		return 0.0F;
	}

	protected void m_8097_()
	{
	}

	public void m_8119_()
	{
		super.m_8119_();
		if (IC2.sideProxy.isSimulating() && (this.range < 1.0F || this.power <= 0.0F || this.blockBreaks <= 0))
		{
			if (this.isExplosiveMode)
			{
				this.explode();
			}

			this.m_142687_(RemovalReason.DISCARDED);
		} else
		{
			this.power -= 0.5F;
		}
	}

	protected void m_8060_(BlockHitResult blockHitResult)
	{
		super.m_8060_(blockHitResult);
		this.handleHit(blockHitResult);
	}

	protected void m_5790_(EntityHitResult entityHitResult)
	{
		super.m_5790_(entityHitResult);
		this.handleHit(entityHitResult);
	}

	protected void handleHit(HitResult hitResult)
	{
		if (this.isExplosiveMode)
		{
			this.explode();
			this.m_142687_(RemovalReason.DISCARDED);
		} else
		{
			switch (hitResult.m_6662_())
			{
				case ENTITY:
					if (this.hitEntity(((EntityHitResult) hitResult).m_82443_()))
					{
						this.power -= 0.5F;
					} else
					{
						this.m_142687_(RemovalReason.DISCARDED);
					}
					break;
				case BLOCK:
					assert hitResult instanceof BlockHitResult;
					BlockHitResult blockHitResult = (BlockHitResult) hitResult;
					if (!this.hitBlock(blockHitResult.m_82425_(), blockHitResult.m_82434_()))
					{
						this.power -= 0.5F;
					} else
					{
						this.m_142687_(RemovalReason.DISCARDED);
					}
					break;
				default:
					throw new RuntimeException("invalid hit type: " + hitResult.m_6662_());
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
			entity.m_20254_(damage * (this.isSmeltMode ? 2 : 1));
			return entity.hurt(new IndirectEntityDamageSource("laser", this, this.owner).m_19366_(), damage);
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

		if (playerOwner.m_36187_(world, pos, Objects.requireNonNull(playerOwner.m_20194_()).m_130008_()))
		{
			return false;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		boolean dropBlock = true;
		if (world.getBlockState(pos).isAir()
			|| block == Blocks.f_50058_
			|| block == Blocks.f_50185_
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
			this.m_142687_(RemovalReason.DISCARDED);
			return true;
		}

		this.power -= hardness / 1.5F;
		if (this.power < 0.0F)
		{
			return true;
		}

		List<ItemStack> replacements = new ArrayList<>();
		if (state.getMaterial() == Material.f_76273_)
		{
			block.m_7592_(
				world, pos, new Explosion(world, this, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F, false, BlockInteraction.BREAK)
			);
		} else if (this.isSmeltMode)
		{
			if (state.getMaterial() == Material.WOOD)
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
				Block.m_49950_(state, world, pos);
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

			if (world.random.nextInt(10) == 0 && state.getMaterial().m_76335_())
			{
				world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
			}
		}

		this.blockBreaks--;
		return true;
	}

	private void appendSmeltItemStack(Block targetBlock, ItemStack inputItemStack, List<ItemStack> replacementList)
	{
		if (inputItemStack.getItem() instanceof BlockItem && ((BlockItem) inputItemStack.getItem()).m_40614_() != targetBlock)
		{
			inputItemStack = new ItemStack(targetBlock.m_5456_());
		}

		SmeltingRecipe recipe = (SmeltingRecipe) IC2.sideProxy
			.getRecipeManager()
			.m_44015_(RecipeType.f_44108_, new SimpleContainer(new ItemStack[] { inputItemStack }), null)
			.orElse(null);
		if (recipe != null)
		{
			ItemStack replacementStack = recipe.m_8043_();
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
