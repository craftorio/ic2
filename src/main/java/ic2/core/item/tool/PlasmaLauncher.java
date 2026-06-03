package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class PlasmaLauncher extends ItemElectricTool {
  public PlasmaLauncher() {
    super(ItemName.plasma_launcher, 100);
    this.maxCharge = 40000;
    this.transferLimit = 128;
    this.tier = 3;
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    if (!IC2.platform.isSimulating())
      return new ActionResult(EnumActionResult.PASS, StackUtil.get(player, hand)); 
    EntityParticle particle = new EntityParticle(world, (EntityLivingBase)player, 8.0F, 1.0D, 2.0D);
    world.func_72838_d(particle);
    return super.func_77659_a(world, player, hand);
  }
}
