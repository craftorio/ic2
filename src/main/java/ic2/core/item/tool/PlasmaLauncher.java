package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
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

   @Override
   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      if (!IC2.platform.isSimulating()) {
         return new ActionResult(EnumActionResult.PASS, StackUtil.get(player, hand));
      }

      EntityParticle particle = new EntityParticle(world, player, 8.0F, 1.0, 2.0);
      world.spawnEntity(particle);
      return super.onItemRightClick(world, player, hand);
   }
}
