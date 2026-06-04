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
    setMaxStackSize(16);
    BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new BehaviorDynamiteDispense(this.sticky));
  }
  
  public int getMetadata(int i) {
    return i;
  }
  
  public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float a, float b, float c) {
    if (this.sticky)
      return EnumActionResult.PASS; 
    pos = pos.offset(side);
    IBlockState state = world.getBlockState(pos);
    Block dynamite = BlockName.dynamite.getInstance();
    if (state.getBlock().isAir(state, (IBlockAccess)world, pos) && dynamite.canPlaceBlockOnSide(world, pos, side) && dynamite.canPlaceBlockAt(world, pos)) {
      world.setBlockState(pos, dynamite.getStateForPlacement(world, pos, side, a, b, c, 0, (EntityLivingBase)player, hand), 3);
      StackUtil.consumeOrError(player, hand, 1);
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.FAIL;
  }
  
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!player.capabilities.isCreativeMode)
      stack = StackUtil.decSize(stack); 
    world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
    if (IC2.platform.isSimulating())
      if (this.sticky) {
        world.spawnEntity((Entity)new EntityStickyDynamite(world, (EntityLivingBase)player));
      } else {
        world.spawnEntity((Entity)new EntityDynamite(world, (EntityLivingBase)player));
      }  
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
}
