package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTreetapElectric extends ItemElectricTool {
  public ItemTreetapElectric() {
    super(ItemName.electric_treetap, 50);
    this.maxCharge = 10000;
    this.transferLimit = 100;
    this.tier = 1;
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    IBlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    ItemStack stack = StackUtil.get(player, hand);
    if (block != BlockName.rubber_wood.getInstance() || 
      !ElectricItem.manager.canUse(stack, this.operationEnergyCost))
      return EnumActionResult.PASS; 
    if (ItemTreetap.attemptExtract(player, world, pos, side, state, null)) {
      ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
      return EnumActionResult.SUCCESS;
    } 
    return super.func_180614_a(player, world, pos, hand, side, hitX, hitY, hitZ);
  }
}
