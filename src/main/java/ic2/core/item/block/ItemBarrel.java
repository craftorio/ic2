package ic2.core.item.block;

import ic2.core.block.BlockScaffold;
import ic2.core.block.TileEntityBarrel;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.state.IIdProvider;
import ic2.core.init.Localization;
import ic2.core.item.ItemBooze;
import ic2.core.item.ItemIC2;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
    func_77625_d(1);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(final ItemName name) {
    ModelLoader.setCustomMeshDefinition((Item)this, new ItemMeshDefinition() {
          public ModelResourceLocation func_178113_a(ItemStack stack) {
            return ItemIC2.getModelLocation(name, null);
          }
        });
    ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(name, null) });
  }
  
  public String func_77653_i(ItemStack itemstack) {
    int v = ItemBooze.getAmountOfValue(itemstack.getItemDamage());
    if (v > 0)
      return "" + v + Localization.translate("ic2.item.LBoozeBarrel"); 
    return Localization.translate("ic2.item.EmptyBoozeBarrel");
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float a, float b, float c) {
    ItemStack stack = StackUtil.get(player, hand);
    if (world.getBlockState(pos) == BlockName.scaffold.getBlockState((IIdProvider)BlockScaffold.ScaffoldType.wood) && 
      ItemBlockTileEntity.placeTeBlock(stack, (EntityLivingBase)player, world, pos, side, (TileEntityBlock)new TileEntityBarrel(stack.getItemDamage()))) {
      StackUtil.consumeOrError(player, hand, 1);
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.PASS;
  }
}
