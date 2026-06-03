package ic2.core.crop;

import ic2.api.item.IBoxable;
import ic2.core.block.TileEntityBlock;
import ic2.core.item.ItemIC2;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import net.minecraft.block.SoundType;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ItemCrop extends ItemIC2 implements IBoxable {
  public ItemCrop() {
    super(ItemName.crop_stick);
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (!world.func_180495_p(pos).func_177230_c().func_176200_f((IBlockAccess)world, pos))
      pos = pos.func_177972_a(side); 
    ItemStack cropStickStack = StackUtil.get(player, hand);
    if (StackUtil.isEmpty(cropStickStack))
      return EnumActionResult.PASS; 
    if (world.func_180495_p(pos.func_177977_b()).func_177230_c() != Blocks.field_150458_ak)
      return EnumActionResult.PASS; 
    if (!player.func_175151_a(pos, side, cropStickStack))
      return EnumActionResult.PASS; 
    if (!world.func_190527_a(BlockName.te.getInstance(), pos, true, side, (Entity)player))
      return EnumActionResult.PASS; 
    TileEntityBlock tile = TileEntityBlock.instantiate(TeBlock.crop.getTeClass());
    if (ItemBlockTileEntity.placeTeBlock(cropStickStack, (EntityLivingBase)player, world, pos, side, tile)) {
      SoundType stepSound = SoundType.field_185850_c;
      world.func_184148_a(null, pos.func_177958_n() + 0.5D, pos.func_177956_o() + 0.5D, pos.func_177952_p() + 0.5D, stepSound
          .func_185841_e(), SoundCategory.BLOCKS, (stepSound
          
          .func_185843_a() + 1.0F) / 2.0F, stepSound
          .func_185847_b() * 0.8F);
      StackUtil.consumeOrError(player, hand, 1);
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.PASS;
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemStack) {
    return true;
  }
}
