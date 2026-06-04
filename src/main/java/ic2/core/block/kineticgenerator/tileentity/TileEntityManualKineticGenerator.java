package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.TileEntityBlock;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

@NotClassic
public class TileEntityManualKineticGenerator extends TileEntityBlock implements IKineticSource
{
	public int clicks;

	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.clicks = 0;
	}

	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		playerClicked(player);
		return true;
	}

	private void playerClicked(EntityPlayer player)
	{
		if (player.getFoodStats().getFoodLevel() <= 6)
			return;
		if (!(player instanceof net.minecraft.entity.player.EntityPlayerMP))
			return;
		if (this.clicks >= 10)
			return;
		
		float ku;
		if (!Util.isFakePlayer(player, false))
		{
			ku = 400;
		} else
		{
			ku = 20;
		}
		ku = (ku * outputModifier);
		this.currentKU = (int) Math.min(this.currentKU + ku, 1000);
		player.addExhaustion(0.25F);
		this.clicks++;
	}

	public int currentKU;

	private static final float outputModifier = Math.round(ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/manual"));

	public int maxrequestKineticEnergyTick(EnumFacing directionFrom)
	{
		return drawKineticEnergy(directionFrom, 2147483647, true);
	}

	public int getConnectionBandwidth(EnumFacing side)
	{
		return 1000;
	}

	public int requestKineticEnergy(EnumFacing directionFrom, int requestKineticEnergy)
	{
		return drawKineticEnergy(directionFrom, requestKineticEnergy, false);
	}

	public int drawKineticEnergy(EnumFacing side, int request, boolean simulate)
	{
		int max = Math.min(this.currentKU, request);
		if (!simulate)
			this.currentKU -= max;
		return max;
	}
}
