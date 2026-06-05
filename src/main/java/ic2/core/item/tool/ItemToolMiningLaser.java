package ic2.core.item.tool;

import ic2.api.event.LaserEvent;
import ic2.api.item.ElectricItem;
import ic2.api.network.INetworkItemEventListener;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolMiningLaser extends ItemElectricTool implements INetworkItemEventListener {
   private static final int EventShotMining = 0;
   private static final int EventShotLowFocus = 1;
   private static final int EventShotLongRange = 2;
   private static final int EventShotHorizontal = 3;
   private static final int EventShotSuperHeat = 4;
   private static final int EventShotScatter = 5;
   private static final int EventShotExplosive = 6;
   private static final int EventShot3x3 = 7;

   public ItemToolMiningLaser() {
      super(ItemName.mining_laser, 100);
      this.maxCharge = 300000;
      this.transferLimit = 512;
      this.tier = 3;
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag par4) {
      super.addInformation(stack, world, list, par4);
      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      String mode;
      switch (nbtData.getInteger("laserSetting")) {
         case 0:
            mode = Localization.translate("ic2.tooltip.mode.mining");
            break;
         case 1:
            mode = Localization.translate("ic2.tooltip.mode.lowFocus");
            break;
         case 2:
            mode = Localization.translate("ic2.tooltip.mode.longRange");
            break;
         case 3:
            mode = Localization.translate("ic2.tooltip.mode.horizontal");
            break;
         case 4:
            mode = Localization.translate("ic2.tooltip.mode.superHeat");
            break;
         case 5:
            mode = Localization.translate("ic2.tooltip.mode.scatter");
            break;
         case 6:
            mode = Localization.translate("ic2.tooltip.mode.explosive");
            break;
         case 7:
            mode = Localization.translate("ic2.tooltip.mode.3x3");
            break;
         default:
            return;
      }

      list.add(Localization.translate("ic2.tooltip.mode", mode));
   }

   @Override
   public List<String> getHudInfo(ItemStack stack, boolean advanced) {
      List<String> info = new LinkedList<>();
      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      String mode = Localization.translate(getModeString(nbtData.getInteger("laserSetting")));
      info.addAll(super.getHudInfo(stack, advanced));
      info.add(Localization.translate("ic2.tooltip.mode", mode));
      return info;
   }

   @Override
   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (!IC2.platform.isSimulating()) {
         return new ActionResult(EnumActionResult.PASS, stack);
      }

      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      int laserSetting = nbtData.getInteger("laserSetting");
      if (IC2.keyboard.isModeSwitchKeyDown(player)) {
         laserSetting = (laserSetting + 1) % 8;
         nbtData.setInteger("laserSetting", laserSetting);
         IC2.platform.messagePlayer(player, "ic2.tooltip.mode", getModeString(laserSetting));
      } else {
         int consume = new int[]{1250, 100, 5000, 0, 2500, 10000, 5000, 7500}[laserSetting];
         if (!ElectricItem.manager.use(stack, consume, player)) {
            return new ActionResult(EnumActionResult.FAIL, stack);
         }

         switch (laserSetting) {
            case 0:
               if (this.shootLaser(stack, world, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false)) {
                  IC2.network.get(true).initiateItemEvent(player, stack, 0, true);
               }
               break;
            case 1:
               if (this.shootLaser(stack, world, player, 4.0F, 5.0F, 1, false, false)) {
                  IC2.network.get(true).initiateItemEvent(player, stack, 1, true);
               }
               break;
            case 2:
               if (this.shootLaser(stack, world, player, Float.POSITIVE_INFINITY, 20.0F, Integer.MAX_VALUE, false, false)) {
                  IC2.network.get(true).initiateItemEvent(player, stack, 2, true);
               }
            case 3:
            case 7:
            default:
               break;
            case 4:
               if (this.shootLaser(stack, world, player, Float.POSITIVE_INFINITY, 8.0F, Integer.MAX_VALUE, false, true)) {
                  IC2.network.get(true).initiateItemEvent(player, stack, 4, true);
               }
               break;
            case 5:
               Vector3 look = Util.getLook(player);
               Vector3 right = look.copy().cross(Vector3.UP);
               if (right.lengthSquared() < 1.0E-4) {
                  double angle = Math.toRadians(player.rotationYaw) - (Math.PI / 2);
                  right.set(Math.sin(angle), 0.0, -Math.cos(angle));
               } else {
                  right.normalize();
               }

               Vector3 up = right.copy().cross(look);
               int sideShots = 2;
               double unitDistance = 8.0;
               look.scale(8.0);

               for (int r = -2; r <= 2; r++) {
                  for (int u = -2; u <= 2; u++) {
                     Vector3 dir = look.copy().addScaled(right, r).addScaled(up, u).normalize();
                     this.shootLaser(stack, world, dir, player, Float.POSITIVE_INFINITY, 12.0F, Integer.MAX_VALUE, false, false);
                  }
               }

               IC2.network.get(true).initiateItemEvent(player, stack, 5, true);
               break;
            case 6:
               if (this.shootLaser(stack, world, player, Float.POSITIVE_INFINITY, 12.0F, Integer.MAX_VALUE, true, false)) {
                  IC2.network.get(true).initiateItemEvent(player, stack, 6, true);
               }
         }
      }

      return super.onItemRightClick(world, player, hand);
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      if (world.isRemote) {
         return EnumActionResult.PASS;
      }

      ItemStack stack = StackUtil.get(player, hand);
      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      if (!IC2.keyboard.isModeSwitchKeyDown(player) && (nbtData.getInteger("laserSetting") == 3 || nbtData.getInteger("laserSetting") == 7)) {
         Vector3 dir = Util.getLook(player);
         double angle = dir.dot(Vector3.UP);
         if (Math.abs(angle) < 1.0 / Math.sqrt(2.0)) {
            if (ElectricItem.manager.use(stack, 3000.0, player)) {
               dir.y = 0.0;
               dir.normalize();
               Vector3 start = Util.getEyePosition(player);
               start.y = pos.getY() + 0.5;
               start = adjustStartPos(start, dir);
               if (nbtData.getInteger("laserSetting") == 3) {
                  if (this.shootLaser(stack, world, start, dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false)) {
                     IC2.network.get(true).initiateItemEvent(player, stack, 3, true);
                  }
               } else if (nbtData.getInteger("laserSetting") == 7
                  && this.shootLaser(stack, world, start, dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false)) {
                  this.shootLaser(
                     stack, world, new Vector3(start.x, start.y - 1.0, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                  );
                  this.shootLaser(
                     stack, world, new Vector3(start.x, start.y + 1.0, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                  );
                  if (player.getHorizontalFacing().equals(EnumFacing.SOUTH) || player.getHorizontalFacing().equals(EnumFacing.NORTH)) {
                     this.shootLaser(
                        stack, world, new Vector3(start.x - 1.0, start.y, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                     );
                     this.shootLaser(
                        stack, world, new Vector3(start.x + 1.0, start.y, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                     );
                     this.shootLaser(
                        stack,
                        world,
                        new Vector3(start.x - 1.0, start.y - 1.0, start.z),
                        dir,
                        player,
                        Float.POSITIVE_INFINITY,
                        5.0F,
                        Integer.MAX_VALUE,
                        false,
                        false
                     );
                     this.shootLaser(
                        stack,
                        world,
                        new Vector3(start.x + 1.0, start.y - 1.0, start.z),
                        dir,
                        player,
                        Float.POSITIVE_INFINITY,
                        5.0F,
                        Integer.MAX_VALUE,
                        false,
                        false
                     );
                     this.shootLaser(
                        stack,
                        world,
                        new Vector3(start.x - 1.0, start.y + 1.0, start.z),
                        dir,
                        player,
                        Float.POSITIVE_INFINITY,
                        5.0F,
                        Integer.MAX_VALUE,
                        false,
                        false
                     );
                     this.shootLaser(
                        stack,
                        world,
                        new Vector3(start.x + 1.0, start.y + 1.0, start.z),
                        dir,
                        player,
                        Float.POSITIVE_INFINITY,
                        5.0F,
                        Integer.MAX_VALUE,
                        false,
                        false
                     );
                  }

                  if (player.getHorizontalFacing().equals(EnumFacing.EAST) || player.getHorizontalFacing().equals(EnumFacing.WEST)) {
                     this.shootLaser(
                        stack, world, new Vector3(start.x, start.y, start.z - 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                     );
                     this.shootLaser(
                        stack, world, new Vector3(start.x, start.y, start.z + 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                     );
                     this.shootLaser(
                        stack,
                        world,
                        new Vector3(start.x, start.y - 1.0, start.z - 1.0),
                        dir,
                        player,
                        Float.POSITIVE_INFINITY,
                        5.0F,
                        Integer.MAX_VALUE,
                        false,
                        false
                     );
                     this.shootLaser(
                        stack,
                        world,
                        new Vector3(start.x, start.y - 1.0, start.z + 1.0),
                        dir,
                        player,
                        Float.POSITIVE_INFINITY,
                        5.0F,
                        Integer.MAX_VALUE,
                        false,
                        false
                     );
                     this.shootLaser(
                        stack,
                        world,
                        new Vector3(start.x, start.y + 1.0, start.z - 1.0),
                        dir,
                        player,
                        Float.POSITIVE_INFINITY,
                        5.0F,
                        Integer.MAX_VALUE,
                        false,
                        false
                     );
                     this.shootLaser(
                        stack,
                        world,
                        new Vector3(start.x, start.y + 1.0, start.z + 1.0),
                        dir,
                        player,
                        Float.POSITIVE_INFINITY,
                        5.0F,
                        Integer.MAX_VALUE,
                        false,
                        false
                     );
                  }

                  IC2.network.get(true).initiateItemEvent(player, stack, 7, true);
               }
            }
         } else if (nbtData.getInteger("laserSetting") == 7) {
            if (ElectricItem.manager.use(stack, 3000.0, player)) {
               dir.x = 0.0;
               dir.z = 0.0;
               dir.normalize();
               Vector3 start = Util.getEyePosition(player);
               start.x = pos.getX() + 0.5;
               start.z = pos.getZ() + 0.5;
               start = adjustStartPos(start, dir);
               if (this.shootLaser(stack, world, start, dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false)) {
                  this.shootLaser(
                     stack, world, new Vector3(start.x + 1.0, start.y, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                  );
                  this.shootLaser(
                     stack, world, new Vector3(start.x - 1.0, start.y, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                  );
                  this.shootLaser(
                     stack,
                     world,
                     new Vector3(start.x + 1.0, start.y, start.z + 1.0),
                     dir,
                     player,
                     Float.POSITIVE_INFINITY,
                     5.0F,
                     Integer.MAX_VALUE,
                     false,
                     false
                  );
                  this.shootLaser(
                     stack,
                     world,
                     new Vector3(start.x - 1.0, start.y, start.z - 1.0),
                     dir,
                     player,
                     Float.POSITIVE_INFINITY,
                     5.0F,
                     Integer.MAX_VALUE,
                     false,
                     false
                  );
                  this.shootLaser(
                     stack,
                     world,
                     new Vector3(start.x + 1.0, start.y, start.z - 1.0),
                     dir,
                     player,
                     Float.POSITIVE_INFINITY,
                     5.0F,
                     Integer.MAX_VALUE,
                     false,
                     false
                  );
                  this.shootLaser(
                     stack,
                     world,
                     new Vector3(start.x - 1.0, start.y, start.z + 1.0),
                     dir,
                     player,
                     Float.POSITIVE_INFINITY,
                     5.0F,
                     Integer.MAX_VALUE,
                     false,
                     false
                  );
                  this.shootLaser(
                     stack, world, new Vector3(start.x, start.y, start.z + 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                  );
                  this.shootLaser(
                     stack, world, new Vector3(start.x, start.y, start.z - 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false
                  );
                  IC2.network.get(true).initiateItemEvent(player, stack, 7, true);
               }
            }
         } else {
            IC2.platform.messagePlayer(player, "Mining laser aiming angle too steep");
         }
      }

      return EnumActionResult.FAIL;
   }

   private static Vector3 adjustStartPos(Vector3 pos, Vector3 dir) {
      return pos.addScaled(dir, 0.2);
   }

   public boolean shootLaser(ItemStack stack, World world, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt) {
      Vector3 dir = Util.getLook(owner);
      return this.shootLaser(stack, world, dir, owner, range, power, blockBreaks, explosive, smelt);
   }

   public boolean shootLaser(
      ItemStack stack, World world, Vector3 dir, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt
   ) {
      Vector3 start = adjustStartPos(Util.getEyePosition(owner), dir);
      return this.shootLaser(stack, world, start, dir, owner, range, power, blockBreaks, explosive, smelt);
   }

   public boolean shootLaser(
      ItemStack stack,
      World world,
      Vector3 start,
      Vector3 dir,
      EntityLivingBase owner,
      float range,
      float power,
      int blockBreaks,
      boolean explosive,
      boolean smelt
   ) {
      EntityMiningLaser entity = new EntityMiningLaser(world, start, dir, owner, range, power, blockBreaks, explosive);
      LaserEvent.LaserShootEvent event = new LaserEvent.LaserShootEvent(world, entity, owner, range, power, blockBreaks, explosive, smelt, stack);
      MinecraftForge.EVENT_BUS.post(event);
      if (event.isCanceled()) {
         return false;
      }

      entity.copyDataFromEvent(event);
      world.spawnEntity(entity);
      return true;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public EnumRarity getRarity(ItemStack stack) {
      return EnumRarity.UNCOMMON;
   }

   @Override
   public void onNetworkEvent(ItemStack stack, EntityPlayer player, int event) {
      switch (event) {
         case 0:
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaser.ogg", true, IC2.audioManager.getDefaultVolume());
            break;
         case 1:
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserLowFocus.ogg", true, IC2.audioManager.getDefaultVolume());
            break;
         case 2:
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserLongRange.ogg", true, IC2.audioManager.getDefaultVolume());
            break;
         case 3:
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaser.ogg", true, IC2.audioManager.getDefaultVolume());
            break;
         case 4:
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaser.ogg", true, IC2.audioManager.getDefaultVolume());
            break;
         case 5:
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserScatter.ogg", true, IC2.audioManager.getDefaultVolume());
            break;
         case 6:
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserExplosive.ogg", true, IC2.audioManager.getDefaultVolume());
            break;
         case 7:
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserScatter.ogg", true, IC2.audioManager.getDefaultVolume());
      }
   }

   private static String getModeString(int mode) {
      switch (mode) {
         case 0:
            return "ic2.tooltip.mode.mining";
         case 1:
            return "ic2.tooltip.mode.lowFocus";
         case 2:
            return "ic2.tooltip.mode.longRange";
         case 3:
            return "ic2.tooltip.mode.horizontal";
         case 4:
            return "ic2.tooltip.mode.superHeat";
         case 5:
            return "ic2.tooltip.mode.scatter";
         case 6:
            return "ic2.tooltip.mode.explosive";
         case 7:
            return "ic2.tooltip.mode.3x3";
         default:
            assert false;
            return "";
      }
   }
}
