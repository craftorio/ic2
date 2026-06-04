// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import ic2.core.block.EntityIC2Explosive;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.Entity;
import ic2.core.block.EntityItnt;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockDispenser;
import net.minecraft.util.EnumFacing;
import ic2.core.util.StackUtil;
import ic2.core.ref.TeBlock;
import ic2.core.ref.BlockName;
import net.minecraft.item.ItemStack;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;

public class BehaviorTeBlockDispense extends BehaviorDefaultDispenseItem
{
    protected ItemStack dispenseStack(final IBlockSource source, final ItemStack stack) {
        if (!StackUtil.checkItemEquality(stack, BlockName.te.getItemStack(TeBlock.itnt))) {
            return stack;
        }
        final World world = source.getWorld();
        final BlockPos pos = source.getBlockPos().offset((EnumFacing)source.getBlockState().getValue((IProperty)BlockDispenser.FACING));
        assert !world.isRemote;
        final EntityIC2Explosive entity = new EntityItnt(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.setBlockToAir(pos);
        world.spawnEntity((Entity)entity);
        world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return StackUtil.decSize(stack);
    }
}
