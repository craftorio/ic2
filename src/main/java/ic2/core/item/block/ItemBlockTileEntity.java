package ic2.core.item.block;

import ic2.core.IC2;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.ITeBlock;
import ic2.core.block.TeBlockRegistry;
import ic2.core.block.TileEntityBlock;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockTileEntity extends ItemBlockIC2 {
   public final ResourceLocation identifier;

   public ItemBlockTileEntity(Block block, ResourceLocation identifier) {
      super(block);
      this.setHasSubtypes(true);
      this.identifier = identifier;
   }

   @Override
   public String getUnlocalizedName(ItemStack stack) {
      ITeBlock teBlock = this.getTeBlock(stack);
      String name = teBlock == null ? "invalid" : teBlock.getName();
      return super.getUnlocalizedName() + "." + name;
   }

   public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
      this.block.getSubBlocks(tab, items);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
      ITeBlock block = this.getTeBlock(stack);
      if (block != null && block.getDummyTe() != null) {
         block.getDummyTe().addInformation(stack, tooltip, advanced);
      }
   }

   public boolean placeBlockAt(
      ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState
   ) {
      assert newState.getBlock() == this.block;
      if (!((BlockTileEntity)this.block).canReplace(world, pos, side, stack)) {
         return false;
      }

      ITeBlock teBlock = this.getTeBlock(stack);
      if (teBlock == null) {
         return false;
      }

      Class<? extends TileEntityBlock> teClass = teBlock.getTeClass();
      if (teClass == null) {
         return false;
      }

      TileEntityBlock te = TileEntityBlock.instantiate(teClass);
      return placeTeBlock(stack, player, world, pos, side, te);
   }

   public static boolean placeTeBlock(ItemStack stack, EntityLivingBase placer, World world, BlockPos pos, EnumFacing side, TileEntityBlock te) {
      IBlockState oldState = world.getBlockState(pos);
      IBlockState newState = te.getBlockState();
      if (!world.setBlockState(pos, newState, 0)) {
         return false;
      }

      world.setTileEntity(pos, te);
      te.onPlaced(stack, placer, side);
      world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), oldState, newState, 3);
      if (!world.isRemote) {
         IC2.network.get(true).sendInitialData(te);
      }

      return true;
   }

   @Override
   public EnumRarity getRarity(ItemStack stack) {
      ITeBlock teblock = this.getTeBlock(stack);
      return teblock != null ? teblock.getRarity() : EnumRarity.COMMON;
   }

   private ITeBlock getTeBlock(ItemStack stack) {
      return stack == null ? null : TeBlockRegistry.get(this.identifier, stack.getItemDamage());
   }
}
