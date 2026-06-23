package ic2.core.block.machine.tileentity;

import ic2.api.entity.block.ExplosiveEntity;
import ic2.core.block.comp.Redstone;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;

public abstract class TileEntityExplosive extends TileEntityInventory implements Redstone.IRedstoneChangeHandler
{
	protected final Redstone redstone = this.addComponent(new Redstone(this));
	private boolean exploded;

	protected TileEntityExplosive(BlockEntityType<? extends TileEntityExplosive> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.redstone.subscribe(this);
	}

	@Override
	public void onRedstoneChange(int newLevel)
	{
		if (newLevel > 0)
		{
			this.explode(null, false);
		}
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (!StackUtil.consume(player, hand, StackUtil.sameItem(Items.FIRE_CHARGE), 1) && !StackUtil.damage(player, hand, StackUtil.sameItem(Items.FLINT_AND_STEEL), 1))
		{
			return super.onActivated(player, hand, side, hit);
		}

		this.explode(player, false);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void onExploded(Explosion explosion)
	{
		super.onExploded(explosion);
		Entity source = explosion.getDamageSource().getEntity();
		this.explode(source instanceof LivingEntity living ? living : null, true);
	}

	@Override
	protected boolean onRemovedByPlayer(Player player, boolean willHarvest)
	{
		if (this.explodeOnRemoval())
		{
			this.explode(player, false);
			return true;
		} else
		{
			return super.onRemovedByPlayer(player, willHarvest);
		}
	}

	@Override
	protected void onEntityCollision(Entity entity)
	{
		if (!this.getLevel().isClientSide && entity instanceof Projectile && entity.isOnFire())
		{
			Projectile arrow = (Projectile) entity;
			Entity owner = arrow.getOwner();
			this.explode(owner instanceof LivingEntity ? (LivingEntity) owner : null, false);
		}
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		return this.exploded ? null : super.adjustDrop(drop, wrench);
	}

	protected boolean explode(LivingEntity igniter, boolean shortFuse)
	{
     RandomSource rng = RandomSource.create();
		ExplosiveEntity entity = this.getEntity(igniter);
		if (entity == null)
		{
			return false;
		}

		Level world = this.getLevel();
		if (world.isClientSide)
		{
			return true;
		}

		entity.setCausingEntity(igniter);
		this.onIgnite(igniter);
		world.removeBlock(this.worldPosition, false);
		if (shortFuse)
		{
			entity.setFuse(rng.nextInt(Math.max(1, entity.getFuse() / 4)) + entity.getFuse() / 8);
		}

		world.addFreshEntity(entity);
		world.playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
		this.exploded = true;
		return true;
	}

	protected boolean explodeOnRemoval()
	{
		return false;
	}

	protected abstract ExplosiveEntity getEntity(LivingEntity var1);

	protected void onIgnite(LivingEntity igniter)
	{
	}
}
