package ic2.core.item.tool;

import com.google.common.base.Predicate;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.IC2;
import ic2.core.audio.AudioPosition;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemToolCutter extends ItemToolCrafting implements IEnhancedOverlayProvider {
  public ItemToolCutter() {
    super(ItemName.cutter, 60);
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileEntity te = world.func_175625_s(pos);
    if (te instanceof TileEntityCable) {
      TileEntityCable cable = (TileEntityCable)te;
      Predicate<ItemStack> request = StackUtil.sameStack(ItemName.crafting.getItemStack((Enum)CraftingItemType.rubber));
      if (StackUtil.consumeFromPlayerInventory(player, request, 1, true) && cable.tryAddInsulation()) {
        StackUtil.consumeFromPlayerInventory(player, request, 1, false);
        StackUtil.damageOrError(player, hand, 1);
        return EnumActionResult.SUCCESS;
      } 
    } 
    return EnumActionResult.PASS;
  }
  
  public boolean removeInsulation(EntityPlayer player, EnumHand hand, TileEntityCable cable) {
    if (cable.tryRemoveInsulation(true) && StackUtil.damage(player, hand, StackUtil.sameItem((Item)this), 3)) {
      cable.tryRemoveInsulation(false);
      if ((cable.getWorld()).isRemote) {
        IC2.audioManager.playOnce(new AudioPosition(cable.getWorld(), cable.getPos()), "Tools/InsulationCutters.ogg");
      } else {
        StackUtil.dropAsEntity(cable.getWorld(), cable.getPos(), ItemName.crafting.getItemStack((Enum)CraftingItemType.rubber));
      } 
      return true;
    } 
    return false;
  }
  
  public boolean providesEnhancedOverlay(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
    return false;
  }
}
