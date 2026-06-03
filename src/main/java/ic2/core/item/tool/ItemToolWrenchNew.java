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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolWrenchNew extends ItemToolIC2 implements IEnhancedOverlayProvider, IHitSoundOverride {
  public ItemToolWrenchNew() {
    super(ItemName.wrench_new, HarvestLevel.Iron, EnumSet.of(ToolClass.Wrench));
    func_77656_e(120);
  }
  
  public boolean canTakeDamage(ItemStack stack, int amount) {
    return true;
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!canTakeDamage(stack, 1))
      return EnumActionResult.FAIL; 
    IBlockState state = Util.getBlockState((IBlockAccess)world, pos);
    Block block = state.func_177230_c();
    if (block.isAir(state, (IBlockAccess)world, pos))
      return EnumActionResult.FAIL; 
    if (world.func_175625_s(pos) instanceof IPipe) {
      IPipe target = (IPipe)world.func_175625_s(pos);
      EnumFacing newFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
      assert target != null;
      target.flipConnection(newFacing);
      if (world.func_175625_s(pos.func_177972_a(newFacing)) instanceof IPipe) {
        IPipe other = (IPipe)world.func_175625_s(pos.func_177972_a(newFacing));
        assert other != null;
        if (target.isConnected(newFacing) != other.isConnected(newFacing.func_176734_d()))
          other.flipConnection(newFacing.func_176734_d()); 
      } 
      if (world.field_72995_K)
        IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager
            .getDefaultVolume()); 
      return EnumActionResult.SUCCESS;
    } 
    if (block instanceof IWrenchable) {
      IWrenchable wrenchable = (IWrenchable)block;
      EnumFacing newFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
      wrenchable.setFacing(world, pos, newFacing, player);
      if (world.field_72995_K)
        IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager
            .getDefaultVolume()); 
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.FAIL;
  }
  
  public boolean func_82789_a(ItemStack toRepair, ItemStack repair) {
    return Util.matchesOD(repair, "ingotBronze");
  }
  
  public boolean func_77616_k(ItemStack stack) {
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, @Nullable World worldIn, List<String> info, ITooltipFlag flagIn) {
    info.add((Minecraft.func_71410_x()).field_71474_y.field_74312_F.getDisplayName() + ":");
    info.add(" Safely mine IC2 machines (Yes you will get the machine and not the machine block)");
    info.add("");
    info.add((Minecraft.func_71410_x()).field_71474_y.field_74313_G.getDisplayName() + ":");
    info.add(" Set the machine facing (rotate)");
    info.add(" Connect pipes together and to covers");
  }
  
  public boolean providesEnhancedOverlay(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
    Block block = world.func_180495_p(pos).func_177230_c();
    if (block instanceof IWrenchable) {
      TileEntity tileEntity = world.func_175625_s(pos);
      return (tileEntity instanceof IPipe || 
        Arrays.<EnumFacing>stream(EnumFacing.field_82609_l).anyMatch(face -> ((IWrenchable)block).canSetFacing(world, pos, face, player)));
    } 
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public String getHitSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
    return "";
  }
  
  @SideOnly(Side.CLIENT)
  public String getBreakSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
    if (player.field_71075_bZ.field_75098_d)
      return null; 
    IBlockState state = world.func_180495_p(pos);
    return (state.func_177230_c() instanceof IWrenchable) ? "Tools/wrench.ogg" : null;
  }
}
