package ic2.core.block.machine.tileentity;

import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.item.armor.ItemArmorHazmat;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class TileEntityTesla extends TileEntityBlock
{
	protected final Redstone redstone;
	protected final Energy energy;
	private int ticker = IC2.random.nextInt(32);

	public TileEntityTesla()
	{
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
					this.energy.useEnergy(damage * 400);
				}
			}
		}
	}

	protected boolean shock(int damage)
	{
		int r = 4;
		World world = this.getWorld();

		for (EntityLivingBase entity : world.getEntitiesWithinAABB(
			EntityLivingBase.class,
			new AxisAlignedBB(
				this.pos.getX() - 4,
				this.pos.getY() - 4,
				this.pos.getZ() - 4,
				this.pos.getX() + 4 + 1,
				this.pos.getY() + 4 + 1,
				this.pos.getZ() + 4 + 1
			)
		))
		{
			if (!ItemArmorHazmat.hasCompleteHazmat(entity) && entity.attackEntityFrom(IC2DamageSource.electricity, damage))
			{
				if (world instanceof WorldServer)
				{
					WorldServer worldServer = (WorldServer) world;
					Random rnd = world.rand;

					for (int i = 0; i < damage; i++)
					{
						worldServer.spawnParticle(
							EnumParticleTypes.REDSTONE,
							true,
							entity.posX + rnd.nextFloat() - 0.5,
							entity.posY + rnd.nextFloat() * 2.0F - 1.0,
							entity.posZ + rnd.nextFloat() - 0.5,
							0,
							0.1,
							0.1,
							1.0,
							1.0,
							new int[0]
						);
					}
				}

				return true;
			}
		}

		return false;
	}
}
