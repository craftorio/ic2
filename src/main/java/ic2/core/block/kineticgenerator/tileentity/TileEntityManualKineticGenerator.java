// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.util.Util;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.profile.NotClassic;
import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.TileEntityBlock;

@NotClassic
public class TileEntityManualKineticGenerator extends TileEntityBlock implements IKineticSource
{
    public int clicks;
    public static final int maxClicksPerTick = 10;
    public final int maxKU = 1000;
    public int currentKU;
    private static final float outputModifier;
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.clicks = 0;
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        this.playerClicked(player);
        return true;
    }
    
    private void playerClicked(final EntityPlayer player) {
        if (player.getFoodStats().getFoodLevel() <= 6) {
            return;
        }
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        if (this.clicks >= 10) {
            return;
        }
        int ku;
        if (!Util.isFakePlayer(player, false)) {
            ku = 400;
        }
        else {
            ku = 20;
        }
        ku *= (int)TileEntityManualKineticGenerator.outputModifier;
        this.currentKU = Math.min(this.currentKU + ku, 1000);
        player.addExhaustion(0.25f);
        ++this.clicks;
    }
    
    @Override
    public int maxrequestkineticenergyTick(final EnumFacing directionFrom) {
        return this.drawKineticEnergy(directionFrom, Integer.MAX_VALUE, true);
    }
    
    @Override
    public int getConnectionBandwidth(final EnumFacing side) {
        return 1000;
    }
    
    @Override
    public int requestkineticenergy(final EnumFacing directionFrom, final int requestkineticenergy) {
        return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
    }
    
    @Override
    public int drawKineticEnergy(final EnumFacing side, final int request, final boolean simulate) {
        final int max = Math.min(this.currentKU, request);
        if (!simulate) {
            this.currentKU -= max;
        }
        return max;
    }
    
    static {
        outputModifier = (float)Math.round(ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/manual"));
    }
}
