package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ItemElectricToolHoe extends ItemElectricTool {
  public ItemElectricToolHoe() {
    super(ItemName.electric_hoe, 50, HarvestLevel.Iron, EnumSet.of(ToolClass.Hoe));
    this.maxCharge = 10000;
    this.transferLimit = 100;
    this.tier = 1;
    this.field_77864_a = 16.0F;
  }
  
  public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
    ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
    return false;
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!player.func_175151_a(pos, side, stack))
      return EnumActionResult.PASS; 
    if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost))
      return EnumActionResult.PASS; 
    if (MinecraftForge.EVENT_BUS.post((Event)new UseHoeEvent(player, stack, world, pos)))
      return EnumActionResult.PASS; 
    IBlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    if (side != EnumFacing.DOWN && world
      .isAirBlock(pos.up()) && (block == Blocks.field_150391_bh || block == Blocks.field_150349_c || block == Blocks.field_150346_d)) {
      block = Blocks.FARMLAND;
      SoundType stepSound = block.getSoundType(state, world, pos, (Entity)player);
      world.func_184148_a(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stepSound.func_185844_d(), SoundCategory.BLOCKS, (stepSound.func_185843_a() + 1.0F) / 2.0F, stepSound.func_185847_b() * 0.8F);
      if (IC2.platform.isSimulating()) {
        world.func_175656_a(pos, block.getDefaultState());
        ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
      } 
      return EnumActionResult.SUCCESS;
    } 
    return super.func_180614_a(player, world, pos, hand, side, hitX, hitY, hitZ);
  }
}
