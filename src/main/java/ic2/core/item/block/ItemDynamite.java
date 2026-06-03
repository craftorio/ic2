package ic2.core.item.block;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.BehaviorDynamiteDispense;
import ic2.core.block.EntityDynamite;
import ic2.core.block.EntityStickyDynamite;
import ic2.core.item.ItemIC2;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ItemDynamite extends ItemIC2 implements IBoxable {
  public boolean sticky;
  
  public ItemDynamite(ItemName name) {
    super(name);
    this.sticky = (name == ItemName.dynamite_sticky);
    func_77625_d(16);
    BlockDispenser.field_149943_a.func_82595_a(this, new BehaviorDynamiteDispense(this.sticky));
  }
  
  public int func_77647_b(int i) {
    return i;
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float a, float b, float c) {
    if (this.sticky)
      return EnumActionResult.PASS; 
    pos = pos.func_177972_a(side);
    IBlockState state = world.func_180495_p(pos);
    Block dynamite = BlockName.dynamite.getInstance();
    if (state.func_177230_c().isAir(state, (IBlockAccess)world, pos) && dynamite.func_176198_a(world, pos, side) && dynamite.func_176196_c(world, pos)) {
      world.func_180501_a(pos, dynamite.getStateForPlacement(world, pos, side, a, b, c, 0, (EntityLivingBase)player, hand), 3);
      StackUtil.consumeOrError(player, hand, 1);
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.FAIL;
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!player.field_71075_bZ.field_75098_d)
      stack = StackUtil.decSize(stack); 
    world.func_184133_a(player, player.func_180425_c(), SoundEvents.field_187737_v, SoundCategory.PLAYERS, 0.5F, 0.4F / (field_77697_d.nextFloat() * 0.4F + 0.8F));
    if (IC2.platform.isSimulating())
      if (this.sticky) {
        world.func_72838_d((Entity)new EntityStickyDynamite(world, (EntityLivingBase)player));
      } else {
        world.func_72838_d((Entity)new EntityDynamite(world, (EntityLivingBase)player));
      }  
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
}
