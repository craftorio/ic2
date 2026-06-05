package ic2.core.item;

import ic2.core.crop.TileEntityCrop;
import ic2.core.init.Localization;
import ic2.core.item.type.CellType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemClassicCell extends ItemMulti<CellType> {
   public ItemClassicCell() {
      super(ItemName.cell, CellType.class);
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      CellType type = this.getType(stack);
      if (type.hasCropAction()) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileEntityCrop) {
            return type.doCropAction(stack, result -> StackUtil.set(player, hand, result), (TileEntityCrop)te, true);
         }
      }

      return EnumActionResult.PASS;
   }

   public int getItemStackLimit(ItemStack stack) {
      CellType type = this.getType(stack);
      return type != null ? type.getStackSize() : 0;
   }

   public boolean showDurabilityBar(ItemStack stack) {
      return this.getType(stack).getUsage(stack) > 0;
   }

   public double getDurabilityForDisplay(ItemStack stack) {
      CellType type = this.getType(stack);
      return (double)type.getUsage(stack) / type.getMaximum(stack);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
      CellType type = this.getType(stack);
      if (type.getStackSize() == 1 && advanced.isAdvanced()) {
         int max = type.getMaximum(stack);
         tooltip.add(Localization.translate("item.durability", max - type.getUsage(stack), max));
      }
   }
}
