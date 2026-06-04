// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.EntityNuke;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.block.EntityIC2Explosive;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.Util;
import org.apache.logging.log4j.Level;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import ic2.core.ref.TeBlock;

@TeBlock.Delegated(current = TileEntityNuke.class, old = TileEntityClassicNuke.class)
public abstract class TileEntityBridgeNuke extends Explosive
{
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (placer instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer)placer;
            final String playerName = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
            IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Player %s placed a nuke at %s.", playerName, Util.formatPosition(this));
        }
    }
    
    public abstract float getNukeExplosivePower();
    
    public abstract int getRadiationRange();
    
    @Override
    protected EntityIC2Explosive getEntity(final EntityLivingBase igniter) {
        if (!ConfigUtil.getBool(MainConfig.get(), "protection/enableNuke")) {
            return null;
        }
        final float power = this.getNukeExplosivePower();
        if (power < 0.0f) {
            return null;
        }
        final int radiationRange = this.getRadiationRange();
        return new EntityNuke(this.getWorld(), this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, power, radiationRange);
    }
    
    @Override
    protected void onIgnite(final EntityLivingBase igniter) {
        final String cause = (igniter == null) ? "indirectly" : ("by " + igniter.getClass().getSimpleName() + " " + igniter.getName());
        IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Nuke at %s was ignited %s.", Util.formatPosition(this), cause);
    }
    
    public static class TileEntityClassicNuke extends TileEntityBridgeNuke
    {
        private static final float POWER = 35.0f;
        
        @Override
        public float getNukeExplosivePower() {
            return Math.min(35.0f, ConfigUtil.getFloat(MainConfig.get(), "protection/nukeExplosionPowerLimit"));
        }
        
        @Override
        public int getRadiationRange() {
            return 1;
        }
    }
}
