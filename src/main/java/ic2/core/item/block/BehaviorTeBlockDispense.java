package ic2.core.item.block;

import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.EntityItnt;
import ic2.core.ref.BlockName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BehaviorTeBlockDispense extends BehaviorDefaultDispenseItem {
   protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
      if (StackUtil.checkItemEquality(stack, BlockName.te.getItemStack(TeBlock.itnt))) {
         World world = source.getWorld();
         BlockPos pos = source.getBlockPos().offset((EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING));
         assert !world.isRemote;
         EntityIC2Explosive entity = new EntityItnt(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
         world.setBlockToAir(pos);
         world.spawnEntity(entity);
         world.playSound(
            null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F
         );
         return StackUtil.decSize(stack);
      } else {
         return stack;
      }
   }
}
