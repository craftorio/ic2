package ic2.core.item.tool;

import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.core.init.Localization;
import ic2.core.item.ItemToolIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.RotationUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolCrowbar extends ItemToolIC2 implements IEnhancedOverlayProvider {
  public ItemToolCrowbar() {
    super(ItemName.crowbar, HarvestLevel.Iron, EnumSet.of(ToolClass.Crowbar));
    setMaxDamage(250);
  }
  
  public boolean canTakeDamage(ItemStack stack, int amount) {
    return true;
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!canTakeDamage(stack, 1))
      return EnumActionResult.FAIL; 
    IBlockState state = Util.getBlockState((IBlockAccess)world, pos);
    Block block = state.getBlock();
    if (block.isAir(state, (IBlockAccess)world, pos))
      return EnumActionResult.FAIL; 
    if (world.getTileEntity(pos) instanceof ICoverHolder) {
      ICoverHolder target = (ICoverHolder)world.getTileEntity(pos);
      EnumFacing selectedFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
      if (target.canRemoveCover(world, pos, selectedFacing))
        if (!world.isRemote) {
          target.removeCover(world, pos, selectedFacing);
          stack.damageItem(1, (EntityLivingBase)player);
        } else {
          IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Crowbar.ogg", true, IC2.audioManager.getDefaultVolume());
          IC2.platform.messagePlayer(player, Localization.translate("Attachment removed"), new Object[0]);
        }  
      return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.FAIL;
  }
  
  public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
    return (repair != null && Util.matchesOD(repair, "ingotBronze"));
  }
  
  public boolean isEnchantable(ItemStack stack) {
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> info, ITooltipFlag flagIn) {
    info.add((Minecraft.getMinecraft()).gameSettings.keyBindUseItem.getDisplayName() + ":");
    info.add(" Remove attachments from blocks");
  }
  
  public boolean providesEnhancedOverlay(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
    TileEntity tileEntity = world.getTileEntity(pos);
    if (tileEntity instanceof ICoverHolder)
      return true; 
    return false;
  }
}
