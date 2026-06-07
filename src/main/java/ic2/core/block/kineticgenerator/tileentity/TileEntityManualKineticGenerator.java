package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@NotClassic
public class TileEntityManualKineticGenerator extends Ic2TileEntity implements IKineticSource
{
	public int clicks;
	public static final int maxClicksPerTick = 10;
	public final int maxKU = 1000;
	public int currentKU;
	private static final float outputModifier = Math.round(ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/manual"));

	public TileEntityManualKineticGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.MANUAL_KINETIC_GENERATOR, pos, state);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.clicks = 0;
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		this.playerClicked(player);
		return InteractionResult.SUCCESS;
	}

	private void playerClicked(Player player)
	{
		if (player.m_36324_().m_38702_() > 6)
		{
			if (player instanceof ServerPlayer)
			{
				if (this.clicks < 10)
				{
					int ku;
					if (!Util.isFakePlayer(player, false))
					{
						ku = 400;
					} else
					{
						ku = 20;
					}

					ku = (int) (ku * outputModifier);
					this.currentKU = Math.min(this.currentKU + ku, 1000);
					player.m_36399_(0.25F);
					this.clicks++;
				}
			}
		}
	}

	@Override
	public int maxrequestkineticenergyTick(Direction directionFrom)
	{
		return this.drawKineticEnergy(directionFrom, Integer.MAX_VALUE, true);
	}

	@Override
	public int getConnectionBandwidth(Direction side)
	{
		return 1000;
	}

	@Override
	public int requestkineticenergy(Direction directionFrom, int requestkineticenergy)
	{
		return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
	}

	@Override
	public int drawKineticEnergy(Direction side, int request, boolean simulate)
	{
		int max = Math.min(this.currentKU, request);
		if (!simulate)
		{
			this.currentKU -= max;
		}

		return max;
	}
}
