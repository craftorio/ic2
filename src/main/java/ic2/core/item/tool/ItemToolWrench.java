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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class ItemToolWrench extends ItemIC2 implements IBoxable {
   private static final boolean logEmptyWrenchDrops = ConfigUtil.getBool(MainConfig.get(), "debug/logEmptyWrenchDrops");

   public ItemToolWrench() {
      this(ItemName.wrench);
   }

   protected ItemToolWrench(ItemName name) {
      super(name);
      this.setMaxDamage(120);
      this.setMaxStackSize(1);
   }

   public boolean canTakeDamage(ItemStack stack, int amount) {
      return true;
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (!this.canTakeDamage(stack, 1)) {
         return EnumActionResult.FAIL;
      }

      ItemToolWrench.WrenchResult result = wrenchBlock(world, pos, side, player, this.canTakeDamage(stack, 10));
      if (result != ItemToolWrench.WrenchResult.Nothing) {
         if (!world.isRemote) {
            this.damage(stack, result == ItemToolWrench.WrenchResult.Rotated ? 1 : 10, player);
         } else {
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
         }

         return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public static ItemToolWrench.WrenchResult wrenchBlock(World world, BlockPos pos, EnumFacing side, EntityPlayer player, boolean remove) {
      IBlockState state = Util.getBlockState(world, pos);
      Block block = state.getBlock();
      if (block.isAir(state, world, pos)) {
         return ItemToolWrench.WrenchResult.Nothing;
      }

      if (block instanceof IWrenchable) {
         IWrenchable wrenchable = (IWrenchable)block;
         EnumFacing currentFacing = wrenchable.getFacing(world, pos);
         EnumFacing newFacing = currentFacing;
         if (!IC2.keyboard.isAltKeyDown(player)) {
            if (player.isSneaking()) {
               newFacing = side.getOpposite();
            } else {
               newFacing = side;
            }
         } else {
            Axis axis = side.getAxis();
            if ((side.getAxisDirection() != AxisDirection.POSITIVE || player.isSneaking())
               && (side.getAxisDirection() != AxisDirection.NEGATIVE || !player.isSneaking())) {
               for (int i = 0; i < 3; i++) {
                  newFacing = newFacing.rotateAround(axis);
               }
            } else {
               newFacing = newFacing.rotateAround(axis);
            }
         }

         if (newFacing != currentFacing && wrenchable.setFacing(world, pos, newFacing, player)) {
            return ItemToolWrench.WrenchResult.Rotated;
         }

         if (remove && wrenchable.wrenchCanRemove(world, pos, player)) {
            if (!world.isRemote) {
               TileEntity te = world.getTileEntity(pos);
               if (ConfigUtil.getBool(MainConfig.get(), "protection/wrenchLogging")) {
                  String playerName = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
                  IC2.log
                     .info(
                        LogCategory.PlayerActivity,
                        "Player %s used a wrench to remove the block %s (te %s) at %s.",
                        playerName,
                        state,
                        getTeName(te),
                        Util.formatPosition(world, pos)
                     );
               }

               int experience;
               if (player instanceof EntityPlayerMP) {
                  experience = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP)player).interactionManager.getGameType(), (EntityPlayerMP)player, pos);
                  if (experience < 0) {
                     return ItemToolWrench.WrenchResult.Nothing;
                  }
               } else {
                  experience = 0;
               }

               block.onBlockHarvested(world, pos, state, player);
               if (!block.removedByPlayer(state, world, pos, player, true)) {
                  return ItemToolWrench.WrenchResult.Nothing;
               }

               block.onBlockDestroyedByPlayer(world, pos, state);
               List<ItemStack> drops = wrenchable.getWrenchDrops(world, pos, state, te, player, 0);
               if (drops != null && !drops.isEmpty()) {
                  for (ItemStack stack : drops) {
                     StackUtil.dropAsEntity(world, pos, stack);
                  }
               } else if (logEmptyWrenchDrops) {
                  IC2.log
                     .warn(
                        LogCategory.General, "The block %s (te %s) at %s didn't yield any wrench drops.", state, getTeName(te), Util.formatPosition(world, pos)
                     );
               }

               if (!player.capabilities.isCreativeMode && experience > 0) {
                  block.dropXpOnBlockBreak(world, pos, experience);
               }
            }

            return ItemToolWrench.WrenchResult.Removed;
         }
      } else if (block.rotateBlock(world, pos, side)) {
         return ItemToolWrench.WrenchResult.Rotated;
      }

      return ItemToolWrench.WrenchResult.Nothing;
   }

   private static String getTeName(Object te) {
      return te != null ? te.getClass().getSimpleName().replace("TileEntity", "") : "none";
   }

   public void damage(ItemStack is, int damage, EntityPlayer player) {
      is.damageItem(damage, player);
   }

   @Override
   public boolean canBeStoredInToolbox(ItemStack itemstack) {
      return true;
   }

   public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
      return repair != null && Util.matchesOD(repair, "ingotBronze");
   }

   private enum WrenchResult {
      Rotated,
      Removed,
      Nothing;
   }
}
