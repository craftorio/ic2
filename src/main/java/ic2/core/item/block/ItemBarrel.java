package ic2.core.item.block;

import ic2.core.block.BlockScaffold;
import ic2.core.block.TileEntityBarrel;
import ic2.core.init.Localization;
import ic2.core.item.ItemBooze;
import ic2.core.item.ItemIC2;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBarrel extends ItemIC2 {
   public ItemBarrel() {
      super(ItemName.barrel);
      this.setMaxStackSize(1);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerModels(final ItemName name) {
      ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
         public ModelResourceLocation getModelLocation(ItemStack stack) {
            return ItemIC2.getModelLocation(name, null);
         }
      });
      ModelBakery.registerItemVariants(this, new ResourceLocation[]{getModelLocation(name, null)});
   }

   @Override
   public String getItemStackDisplayName(ItemStack itemstack) {
      int v = ItemBooze.getAmountOfValue(itemstack.getItemDamage());
      return v > 0 ? "" + v + Localization.translate("ic2.item.LBoozeBarrel") : Localization.translate("ic2.item.EmptyBoozeBarrel");
   }

   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float a, float b, float c) {
      ItemStack stack = StackUtil.get(player, hand);
      if (world.getBlockState(pos) == BlockName.scaffold.getBlockState(BlockScaffold.ScaffoldType.wood)
         && ItemBlockTileEntity.placeTeBlock(stack, player, world, pos, side, new TileEntityBarrel(stack.getItemDamage()))) {
         StackUtil.consumeOrError(player, hand, 1);
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.PASS;
      }
   }
}
