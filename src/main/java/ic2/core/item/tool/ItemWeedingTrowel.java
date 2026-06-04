package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.crop.IC2Crops;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.ItemIC2;
import ic2.core.item.type.CropResItemType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@NotClassic
public class ItemWeedingTrowel extends ItemIC2 {
  public ItemWeedingTrowel() {
    super(ItemName.weeding_trowel);
    setMaxStackSize(1);
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    if (!IC2.platform.isSimulating())
      return EnumActionResult.PASS; 
    TileEntity tileEntity = world.getTileEntity(pos);
    if (tileEntity instanceof TileEntityCrop) {
      TileEntityCrop tileEntityCrop = (TileEntityCrop)tileEntity;
      if (tileEntityCrop.getCrop() == IC2Crops.weed) {
        StackUtil.dropAsEntity(world, pos, StackUtil.copyWithSize(ItemName.crop_res.getItemStack((Enum)CropResItemType.weed), tileEntityCrop.getCurrentSize()));
        tileEntityCrop.reset();
        return EnumActionResult.SUCCESS;
      } 
    } 
    return EnumActionResult.PASS;
  }
}
