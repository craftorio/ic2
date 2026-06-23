package ic2.core.block.machine.tileentity;

import org.joml.Vector3f;
import ic2.core.IC2;
import ic2.core.Ic2DamageSource;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.ref.Ic2BlockEntities;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TileEntityTesla extends Ic2TileEntity
{
	private static final DustParticleOptions effect = new DustParticleOptions(new Vector3f(0.1F, 0.1F, 1.0F), 1.0F);
	protected final Redstone redstone;
	protected final Energy energy;
	private int ticker = IC2.random.nextInt(32);

	public TileEntityTesla(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.TESLA_COIL, pos, state);
		this.redstone = this.addComponent(new Redstone(this));
		this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 2));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.redstone.hasRedstoneInput())
		{
			if (this.energy.useEnergy(1.0) && ++this.ticker % 32 == 0)
			{
				int damage = (int) this.energy.getEnergy() / 400;
				if (damage > 0 && this.shock(damage))
				{
					System.out.println(damage);
					this.energy.useEnergy(damage * 400);
				}
			}
		}
	}

	protected boolean shock(int totalDamage)
	{
		int r = 4;
		Level world = this.getLevel();
		if (world == null)
		{
			return false;
		}

		List<LivingEntity> entities = world.getEntitiesOfClass(
			LivingEntity.class,
			new AABB(
				this.worldPosition.getX() - 4,
				this.worldPosition.getY() - 4,
				this.worldPosition.getZ() - 4,
				this.worldPosition.getX() + 4 + 1,
				this.worldPosition.getY() + 4 + 1,
				this.worldPosition.getZ() + 4 + 1
			),
			EntitySelector.NO_CREATIVE_OR_SPECTATOR
		);
		if (entities.size() == 0)
		{
			return false;
		}

		boolean isShocked = false;
		int damage = totalDamage / entities.size();

		for (LivingEntity entity : entities)
		{
			if (!ItemArmorHazmat.hasCompleteHazmat(entity) && entity.hurt(Ic2DamageSource.electricity, damage))
			{
				if (world instanceof ServerLevel worldServer)
				{
					RandomSource rnd = RandomSource.create();
					System.out.println(entity);

					for (int i = 0; i < damage; i++)
					{
						worldServer.addParticle(
							effect,
							entity.getX() + rnd.nextFloat() - 0.5,
							entity.getY() + rnd.nextFloat() * 2.0F - 1.0,
							entity.getZ() + rnd.nextFloat() - 0.5,
							0.0,
							0.0,
							0.0
						);
					}
				}

				isShocked = true;
			}
		}

		return isShocked;
	}
}
