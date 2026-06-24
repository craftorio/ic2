package ic2.core.block.machine.tileentity;

import ic2.api.entity.block.ExplosiveEntity;
import ic2.core.IC2;
import ic2.core.entity.block.NukeEntity;
import ic2.core.init.IC2Config;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.Level;

public abstract class TileEntityBridgeNuke extends TileEntityExplosive
{
	protected TileEntityBridgeNuke(BlockPos pos, BlockState state)
	{
		super(ic2.core.ref.Ic2BlockEntities.NUKE, pos, state);
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		if (placer instanceof Player player)
		{
			String playerName = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
			IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Player %s placed a nuke at %s.", playerName, Util.formatPosition(this));
		}
	}

	public abstract float getNukeExplosivePower();

	public abstract int getRadiationRange();

	@Override
	protected ExplosiveEntity getEntity(LivingEntity igniter)
	{
		if (!IC2Config.protection.enableNuke.get())
		{
			return null;
		}

		float power = this.getNukeExplosivePower();
		if (power < 0.0F)
		{
			return null;
		}

		int radiationRange = this.getRadiationRange();
		return new NukeEntity(
			this.getLevel(), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, power, radiationRange
		);
	}

	@Override
	protected void onIgnite(LivingEntity igniter)
	{
		String cause = igniter == null ? "indirectly" : "by " + igniter.getClass().getSimpleName() + " " + igniter.getName();
		IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Nuke at %s was ignited %s.", Util.formatPosition(this), cause);
	}

}
