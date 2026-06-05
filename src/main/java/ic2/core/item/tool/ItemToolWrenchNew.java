package ic2.core.item.tool;

import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.tile.IWrenchable;
import ic2.api.transport.IPipe;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.audio.PositionSpec;
import ic2.core.item.ItemToolIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.RotationUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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

public class ItemToolWrenchNew extends ItemToolIC2 implements IEnhancedOverlayProvider, IHitSoundOverride {
   public ItemToolWrenchNew() {
      super(ItemName.wrench_new, HarvestLevel.Iron, EnumSet.of(ToolClass.Wrench));
      this.setMaxDamage(120);
   }

   public boolean canTakeDamage(ItemStack stack, int amount) {
      return true;
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (!this.canTakeDamage(stack, 1)) {
         return EnumActionResult.FAIL;
      }

      IBlockState state = Util.getBlockState(world, pos);
      Block block = state.getBlock();
      if (block.isAir(state, world, pos)) {
         return EnumActionResult.FAIL;
      }

      if (world.getTileEntity(pos) instanceof IPipe) {
         IPipe target = (IPipe)world.getTileEntity(pos);
         EnumFacing newFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
         assert target != null;
         target.flipConnection(newFacing);
         if (world.getTileEntity(pos.offset(newFacing)) instanceof IPipe) {
            IPipe other = (IPipe)world.getTileEntity(pos.offset(newFacing));
            assert other != null;
            if (target.isConnected(newFacing) != other.isConnected(newFacing.getOpposite())) {
               other.flipConnection(newFacing.getOpposite());
            }
         }

         if (world.isRemote) {
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
         }

         return EnumActionResult.SUCCESS;
      } else if (block instanceof IWrenchable) {
         IWrenchable wrenchable = (IWrenchable)block;
         EnumFacing newFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
         wrenchable.setFacing(world, pos, newFacing, player);
         if (world.isRemote) {
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
      return Util.matchesOD(repair, "ingotBronze");
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> info, ITooltipFlag flagIn) {
      info.add(Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName() + ":");
      info.add(" Safely mine IC2 machines (Yes you will get the machine and not the machine block)");
      info.add("");
      info.add(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName() + ":");
      info.add(" Set the machine facing (rotate)");
      info.add(" Connect pipes together and to covers");
   }

   @Override
   public boolean providesEnhancedOverlay(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
      Block block = world.getBlockState(pos).getBlock();
      if (!(block instanceof IWrenchable)) {
         return false;
      }

      TileEntity tileEntity = world.getTileEntity(pos);
      return tileEntity instanceof IPipe
         || Arrays.stream(EnumFacing.VALUES).anyMatch(face -> ((IWrenchable)block).canSetFacing(world, pos, face, player));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public String getHitSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
      return "";
   }

   @SideOnly(Side.CLIENT)
   @Override
   public String getBreakSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
      if (player.capabilities.isCreativeMode) {
         return null;
      }

      IBlockState state = world.getBlockState(pos);
      return state.getBlock() instanceof IWrenchable ? "Tools/wrench.ogg" : null;
   }
}
