package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.init.MainConfig;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class ItemToolWrench extends ItemIC2 implements IBoxable {
  public ItemToolWrench() {
    this(ItemName.wrench);
  }
  
  protected ItemToolWrench(ItemName name) {
    super(name);
    func_77656_e(120);
    func_77625_d(1);
  }
  
  public boolean canTakeDamage(ItemStack stack, int amount) {
    return true;
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!canTakeDamage(stack, 1))
      return EnumActionResult.FAIL; 
    WrenchResult result = wrenchBlock(world, pos, side, player, canTakeDamage(stack, 10));
    if (result != WrenchResult.Nothing) {
      if (!world.field_72995_K) {
        damage(stack, (result == WrenchResult.Rotated) ? 1 : 10, player);
      } else {
        IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
      } 
      return world.field_72995_K ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.FAIL;
  }
  
  public static WrenchResult wrenchBlock(World world, BlockPos pos, EnumFacing side, EntityPlayer player, boolean remove) {
    IBlockState state = Util.getBlockState((IBlockAccess)world, pos);
    Block block = state.func_177230_c();
    if (block.isAir(state, (IBlockAccess)world, pos))
      return WrenchResult.Nothing; 
    if (block instanceof IWrenchable) {
      IWrenchable wrenchable = (IWrenchable)block;
      EnumFacing currentFacing = wrenchable.getFacing(world, pos);
      EnumFacing newFacing = currentFacing;
      if (IC2.keyboard.isAltKeyDown(player)) {
        EnumFacing.Axis axis = side.func_176740_k();
        if ((side.func_176743_c() == EnumFacing.AxisDirection.POSITIVE && !player.func_70093_af()) || (side
          .func_176743_c() == EnumFacing.AxisDirection.NEGATIVE && player.func_70093_af())) {
          newFacing = newFacing.func_176732_a(axis);
        } else {
          for (int i = 0; i < 3; i++)
            newFacing = newFacing.func_176732_a(axis); 
        } 
      } else if (player.func_70093_af()) {
        newFacing = side.func_176734_d();
      } else {
        newFacing = side;
      } 
      if (newFacing != currentFacing && wrenchable.setFacing(world, pos, newFacing, player))
        return WrenchResult.Rotated; 
      if (remove && wrenchable.wrenchCanRemove(world, pos, player)) {
        if (!world.field_72995_K) {
          int experience;
          TileEntity te = world.func_175625_s(pos);
          if (ConfigUtil.getBool(MainConfig.get(), "protection/wrenchLogging")) {
            String playerName = player.func_146103_bH().getName() + "/" + player.func_146103_bH().getId();
            IC2.log.info(LogCategory.PlayerActivity, "Player %s used a wrench to remove the block %s (te %s) at %s.", new Object[] { playerName, state, 
                  getTeName(te), Util.formatPosition((IBlockAccess)world, pos) });
          } 
          if (player instanceof EntityPlayerMP) {
            experience = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP)player).field_71134_c.func_73081_b(), (EntityPlayerMP)player, pos);
            if (experience < 0)
              return WrenchResult.Nothing; 
          } else {
            experience = 0;
          } 
          block.func_176208_a(world, pos, state, player);
          if (block.removedByPlayer(state, world, pos, player, true)) {
            block.func_176206_d(world, pos, state);
          } else {
            return WrenchResult.Nothing;
          } 
          List<ItemStack> drops = wrenchable.getWrenchDrops(world, pos, state, te, player, 0);
          if (drops == null || drops.isEmpty()) {
            if (logEmptyWrenchDrops)
              IC2.log.warn(LogCategory.General, "The block %s (te %s) at %s didn't yield any wrench drops.", new Object[] { state, getTeName(te), Util.formatPosition((IBlockAccess)world, pos) }); 
          } else {
            for (ItemStack stack : drops)
              StackUtil.dropAsEntity(world, pos, stack); 
          } 
          if (!player.field_71075_bZ.field_75098_d && experience > 0)
            block.func_180637_b(world, pos, experience); 
        } 
        return WrenchResult.Removed;
      } 
    } else if (block.rotateBlock(world, pos, side)) {
      return WrenchResult.Rotated;
    } 
    return WrenchResult.Nothing;
  }
  
  private static String getTeName(Object te) {
    return (te != null) ? te.getClass().getSimpleName().replace("TileEntity", "") : "none";
  }
  
  private enum WrenchResult {
    Rotated, Removed, Nothing;
  }
  
  public void damage(ItemStack is, int damage, EntityPlayer player) {
    is.func_77972_a(damage, (EntityLivingBase)player);
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
  
  public boolean func_82789_a(ItemStack toRepair, ItemStack repair) {
    return (repair != null && Util.matchesOD(repair, "ingotBronze"));
  }
  
  private static final boolean logEmptyWrenchDrops = ConfigUtil.getBool(MainConfig.get(), "debug/logEmptyWrenchDrops");
}
