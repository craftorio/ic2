package ic2.core.block.machine.tileentity;

import ic2.core.IC2;
import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.EntityNuke;
import ic2.core.init.MainConfig;
import ic2.core.ref.TeBlock.Delegated;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.apache.logging.log4j.Level;

@Delegated(current = TileEntityNuke.class, old = TileEntityBridgeNuke.TileEntityClassicNuke.class)
public abstract class TileEntityBridgeNuke extends Explosive {
  public static class TileEntityClassicNuke extends TileEntityBridgeNuke {
    private static final float POWER = 35.0F;
    
    public float getNukeExplosivePower() {
      return Math.min(35.0F, ConfigUtil.getFloat(MainConfig.get(), "protection/nukeExplosionPowerLimit"));
    }
    
    public int getRadiationRange() {
      return 1;
    }
  }
  
  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    super.onPlaced(stack, placer, facing);
    if (placer instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer)placer;
      String playerName = player.func_146103_bH().getName() + "/" + player.func_146103_bH().getId();
      IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Player %s placed a nuke at %s.", new Object[] { playerName, Util.formatPosition((TileEntity)this) });
    } 
  }
  
  public abstract float getNukeExplosivePower();
  
  public abstract int getRadiationRange();
  
  protected EntityIC2Explosive getEntity(EntityLivingBase igniter) {
    if (!ConfigUtil.getBool(MainConfig.get(), "protection/enableNuke"))
      return null; 
    float power = getNukeExplosivePower();
    if (power < 0.0F)
      return null; 
    int radiationRange = getRadiationRange();
    return (EntityIC2Explosive)new EntityNuke(func_145831_w(), this.field_174879_c.func_177958_n() + 0.5D, this.field_174879_c.func_177956_o() + 0.5D, this.field_174879_c.func_177952_p() + 0.5D, power, radiationRange);
  }
  
  protected void onIgnite(EntityLivingBase igniter) {
    String cause = (igniter == null) ? "indirectly" : ("by " + igniter.getClass().getSimpleName() + " " + igniter.func_70005_c_());
    IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Nuke at %s was ignited %s.", new Object[] { Util.formatPosition((TileEntity)this), cause });
  }
}
