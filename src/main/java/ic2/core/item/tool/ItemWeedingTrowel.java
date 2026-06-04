// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.tileentity.TileEntity;
import ic2.core.util.StackUtil;
import ic2.core.item.type.CropResItemType;
import ic2.core.crop.IC2Crops;
import ic2.core.crop.TileEntityCrop;
import ic2.core.IC2;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;
import ic2.core.item.ItemIC2;

@NotClassic
public class ItemWeedingTrowel extends ItemIC2
{
    public ItemWeedingTrowel() {
        super(ItemName.weeding_trowel);
        this.setMaxStackSize(1);
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (!IC2.platform.isSimulating()) {
            return EnumActionResult.PASS;
        }
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCrop) {
            final TileEntityCrop tileEntityCrop = (TileEntityCrop)tileEntity;
            if (tileEntityCrop.getCrop() == IC2Crops.weed) {
                StackUtil.dropAsEntity(world, pos, StackUtil.copyWithSize(ItemName.crop_res.getItemStack(CropResItemType.weed), tileEntityCrop.getCurrentSize()));
                tileEntityCrop.reset();
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
}
