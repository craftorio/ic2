package ic2.core.block;

import ic2.core.IC2;
import ic2.core.block.state.IIdProvider;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.ref.BlockName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityNuke extends EntityIC2Explosive {
  public EntityNuke(World world, double x, double y, double z, float power, int radiationRange) {
    super(world, x, y, z, 300, power, 0.05F, 1.5F, BlockName.te.getBlockState((IIdProvider)TeBlock.nuke), radiationRange);
  }
  
  public EntityNuke(World world) {
    this(world, 0.0D, 0.0D, 0.0D, 0.0F, 0);
  }
  
  public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (IC2.platform.isSimulating() && !StackUtil.isEmpty(stack) && stack.getItem() instanceof ItemToolWrench) {
      ItemToolWrench wrench = (ItemToolWrench)stack.getItem();
      if (wrench.canTakeDamage(stack, 1)) {
        wrench.damage(stack, 1, player);
        setDead();
      } 
    } 
    return false;
  }
}
