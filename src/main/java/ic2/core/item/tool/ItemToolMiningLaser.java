package ic2.core.item.tool;

import ic2.api.event.LaserEvent;
import ic2.api.item.ElectricItem;
import ic2.api.network.INetworkItemEventListener;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.init.Localization;
import ic2.core.network.NetworkManager;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.fml.common.eventhandler.Event;
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
  public void func_77624_a(ItemStack stack, World world, List<String> list, ITooltipFlag par4) {
    String mode;
    super.func_77624_a(stack, world, list, par4);
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    switch (nbtData.func_74762_e("laserSetting")) {
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
    list.add(Localization.translate("ic2.tooltip.mode", new Object[] { mode }));
  }
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    String mode = Localization.translate(getModeString(nbtData.func_74762_e("laserSetting")));
    info.addAll(super.getHudInfo(stack, advanced));
    info.add(Localization.translate("ic2.tooltip.mode", new Object[] { mode }));
    return info;
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!IC2.platform.isSimulating())
      return new ActionResult(EnumActionResult.PASS, stack); 
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    int laserSetting = nbtData.func_74762_e("laserSetting");
    if (IC2.keyboard.isModeSwitchKeyDown(player)) {
      laserSetting = (laserSetting + 1) % 8;
      nbtData.func_74768_a("laserSetting", laserSetting);
      IC2.platform.messagePlayer(player, "ic2.tooltip.mode", new Object[] { getModeString(laserSetting) });
    } else {
      Vector3 look, right, up;
      int sideShots;
      double unitDistance;
      int r;
      (new int[8])[0] = 1250;
      (new int[8])[1] = 100;
      (new int[8])[2] = 5000;
      (new int[8])[3] = 0;
      (new int[8])[4] = 2500;
      (new int[8])[5] = 10000;
      (new int[8])[6] = 5000;
      (new int[8])[7] = 7500;
      int consume = (new int[8])[laserSetting];
      if (!ElectricItem.manager.use(stack, consume, (EntityLivingBase)player))
        return new ActionResult(EnumActionResult.FAIL, stack); 
      switch (laserSetting) {
        case 0:
          if (shootLaser(stack, world, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false))
            ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 0, true); 
          break;
        case 1:
          if (shootLaser(stack, world, (EntityLivingBase)player, 4.0F, 5.0F, 1, false, false))
            ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 1, true); 
          break;
        case 2:
          if (shootLaser(stack, world, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 20.0F, 2147483647, false, false))
            ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 2, true); 
          break;
        case 4:
          if (shootLaser(stack, world, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 8.0F, 2147483647, false, true))
            ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 4, true); 
          break;
        case 5:
          look = Util.getLook((Entity)player);
          right = look.copy().cross(Vector3.UP);
          if (right.lengthSquared() < 1.0E-4D) {
            double angle = Math.toRadians(player.field_70177_z) - 1.5707963267948966D;
            right.set(Math.sin(angle), 0.0D, -Math.cos(angle));
          } else {
            right.normalize();
          } 
          up = right.copy().cross(look);
          sideShots = 2;
          unitDistance = 8.0D;
          look.scale(8.0D);
          for (r = -2; r <= 2; r++) {
            for (int u = -2; u <= 2; u++) {
              Vector3 dir = look.copy().addScaled(right, r).addScaled(up, u).normalize();
              shootLaser(stack, world, dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 12.0F, 2147483647, false, false);
            } 
          } 
          ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 5, true);
          break;
        case 6:
          if (shootLaser(stack, world, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 12.0F, 2147483647, true, false))
            ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 6, true); 
          break;
      } 
    } 
    return super.func_77659_a(world, player, hand);
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    if (world.isRemote)
      return EnumActionResult.PASS; 
    ItemStack stack = StackUtil.get(player, hand);
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    if (!IC2.keyboard.isModeSwitchKeyDown(player) && (nbtData
      .func_74762_e("laserSetting") == 3 || nbtData.func_74762_e("laserSetting") == 7)) {
      Vector3 dir = Util.getLook((Entity)player);
      double angle = dir.dot(Vector3.UP);
      if (Math.abs(angle) < 1.0D / Math.sqrt(2.0D)) {
        if (ElectricItem.manager.use(stack, 3000.0D, (EntityLivingBase)player)) {
          dir.y = 0.0D;
          dir.normalize();
          Vector3 start = Util.getEyePosition((Entity)player);
          start.y = pos.func_177956_o() + 0.5D;
          start = adjustStartPos(start, dir);
          if (nbtData.func_74762_e("laserSetting") == 3) {
            if (shootLaser(stack, world, start, dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false))
              ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 3, true); 
          } else if (nbtData.func_74762_e("laserSetting") == 7 && 
            shootLaser(stack, world, start, dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false)) {
            shootLaser(stack, world, new Vector3(start.x, start.y - 1.0D, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            shootLaser(stack, world, new Vector3(start.x, start.y + 1.0D, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            if (player.func_174811_aO().equals(EnumFacing.SOUTH) || player.func_174811_aO().equals(EnumFacing.NORTH)) {
              shootLaser(stack, world, new Vector3(start.x - 1.0D, start.y, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x + 1.0D, start.y, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x - 1.0D, start.y - 1.0D, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x + 1.0D, start.y - 1.0D, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x - 1.0D, start.y + 1.0D, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x + 1.0D, start.y + 1.0D, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            } 
            if (player.func_174811_aO().equals(EnumFacing.EAST) || player.func_174811_aO().equals(EnumFacing.WEST)) {
              shootLaser(stack, world, new Vector3(start.x, start.y, start.z - 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x, start.y, start.z + 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x, start.y - 1.0D, start.z - 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x, start.y - 1.0D, start.z + 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x, start.y + 1.0D, start.z - 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
              shootLaser(stack, world, new Vector3(start.x, start.y + 1.0D, start.z + 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            } 
            ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 7, true);
          } 
        } 
      } else if (nbtData.func_74762_e("laserSetting") == 7) {
        if (ElectricItem.manager.use(stack, 3000.0D, (EntityLivingBase)player)) {
          dir.x = 0.0D;
          dir.z = 0.0D;
          dir.normalize();
          Vector3 start = Util.getEyePosition((Entity)player);
          start.x = pos.func_177958_n() + 0.5D;
          start.z = pos.func_177952_p() + 0.5D;
          start = adjustStartPos(start, dir);
          if (shootLaser(stack, world, start, dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false)) {
            shootLaser(stack, world, new Vector3(start.x + 1.0D, start.y, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            shootLaser(stack, world, new Vector3(start.x - 1.0D, start.y, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            shootLaser(stack, world, new Vector3(start.x + 1.0D, start.y, start.z + 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            shootLaser(stack, world, new Vector3(start.x - 1.0D, start.y, start.z - 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            shootLaser(stack, world, new Vector3(start.x + 1.0D, start.y, start.z - 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            shootLaser(stack, world, new Vector3(start.x - 1.0D, start.y, start.z + 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            shootLaser(stack, world, new Vector3(start.x, start.y, start.z + 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            shootLaser(stack, world, new Vector3(start.x, start.y, start.z - 1.0D), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0F, 2147483647, false, false);
            ((NetworkManager)IC2.network.get(true)).initiateItemEvent(player, stack, 7, true);
          } 
        } 
      } else {
        IC2.platform.messagePlayer(player, "Mining laser aiming angle too steep", new Object[0]);
      } 
    } 
    return EnumActionResult.FAIL;
  }
  
  private static Vector3 adjustStartPos(Vector3 pos, Vector3 dir) {
    return pos.addScaled(dir, 0.2D);
  }
  
  public boolean shootLaser(ItemStack stack, World world, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt) {
    Vector3 dir = Util.getLook((Entity)owner);
    return shootLaser(stack, world, dir, owner, range, power, blockBreaks, explosive, smelt);
  }
  
  public boolean shootLaser(ItemStack stack, World world, Vector3 dir, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt) {
    Vector3 start = adjustStartPos(Util.getEyePosition((Entity)owner), dir);
    return shootLaser(stack, world, start, dir, owner, range, power, blockBreaks, explosive, smelt);
  }
  
  public boolean shootLaser(ItemStack stack, World world, Vector3 start, Vector3 dir, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt) {
    EntityMiningLaser entity = new EntityMiningLaser(world, start, dir, owner, range, power, blockBreaks, explosive);
    LaserEvent.LaserShootEvent event = new LaserEvent.LaserShootEvent(world, entity, owner, range, power, blockBreaks, explosive, smelt, stack);
    MinecraftForge.EVENT_BUS.post((Event)event);
    if (event.isCanceled())
      return false; 
    entity.copyDataFromEvent((LaserEvent)event);
    world.func_72838_d(entity);
    return true;
  }
  
  @SideOnly(Side.CLIENT)
  public EnumRarity func_77613_e(ItemStack stack) {
    return EnumRarity.UNCOMMON;
  }
  
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
        break;
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
    } 
    assert false;
    return "";
  }
}
