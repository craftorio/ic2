// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop;

import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.TeBlock;
import net.minecraft.entity.Entity;
import ic2.core.ref.BlockName;
import net.minecraft.init.Blocks;
import ic2.core.util.StackUtil;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.api.item.IBoxable;
import ic2.core.item.ItemIC2;

public class ItemCrop extends ItemIC2 implements IBoxable
{
    public ItemCrop() {
        super(ItemName.crop_stick);
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (!world.getBlockState(pos).getBlock().isReplaceable((IBlockAccess)world, pos)) {
            pos = pos.offset(side);
        }
        final ItemStack cropStickStack = StackUtil.get(player, hand);
        if (StackUtil.isEmpty(cropStickStack)) {
            return EnumActionResult.PASS;
        }
        if (world.getBlockState(pos.down()).getBlock() != Blocks.FARMLAND) {
            return EnumActionResult.PASS;
        }
        if (!player.canPlayerEdit(pos, side, cropStickStack)) {
            return EnumActionResult.PASS;
        }
        if (!world.mayPlace(BlockName.te.getInstance(), pos, true, side, (Entity)player)) {
            return EnumActionResult.PASS;
        }
        final TileEntityBlock tile = TileEntityBlock.instantiate(TeBlock.crop.getTeClass());
        if (ItemBlockTileEntity.placeTeBlock(cropStickStack, (EntityLivingBase)player, world, pos, side, tile)) {
            final SoundType stepSound = SoundType.PLANT;
            world.playSound((EntityPlayer)null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stepSound.getPlaceSound(), SoundCategory.BLOCKS, (stepSound.getVolume() + 1.0f) / 2.0f, stepSound.getPitch() * 0.8f);
            StackUtil.consumeOrError(player, hand, 1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemStack) {
        return true;
    }
}
