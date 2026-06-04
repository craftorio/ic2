// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.audio.PositionSpec;
import net.minecraft.item.EnumRarity;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import ic2.api.event.LaserEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import ic2.core.util.Vector3;
import net.minecraft.entity.Entity;
import ic2.core.util.Util;
import ic2.core.network.NetworkManager;
import net.minecraft.entity.EntityLivingBase;
import ic2.api.item.ElectricItem;
import net.minecraft.util.EnumActionResult;
import ic2.core.IC2;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Collection;
import java.util.LinkedList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.init.Localization;
import ic2.core.util.StackUtil;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.network.INetworkItemEventListener;

public class ItemToolMiningLaser extends ItemElectricTool implements INetworkItemEventListener
{
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
    public void addInformation(final ItemStack stack, final World world, final List<String> list, final ITooltipFlag par4) {
        super.addInformation(stack, world, (List)list, par4);
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        String mode = null;
        switch (nbtData.getInteger("laserSetting")) {
            case 0: {
                mode = Localization.translate("ic2.tooltip.mode.mining");
                break;
            }
            case 1: {
                mode = Localization.translate("ic2.tooltip.mode.lowFocus");
                break;
            }
            case 2: {
                mode = Localization.translate("ic2.tooltip.mode.longRange");
                break;
            }
            case 3: {
                mode = Localization.translate("ic2.tooltip.mode.horizontal");
                break;
            }
            case 4: {
                mode = Localization.translate("ic2.tooltip.mode.superHeat");
                break;
            }
            case 5: {
                mode = Localization.translate("ic2.tooltip.mode.scatter");
                break;
            }
            case 6: {
                mode = Localization.translate("ic2.tooltip.mode.explosive");
                break;
            }
            case 7: {
                mode = Localization.translate("ic2.tooltip.mode.3x3");
                break;
            }
            default: {
                return;
            }
        }
        list.add(Localization.translate("ic2.tooltip.mode", mode));
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        final String mode = Localization.translate(getModeString(nbtData.getInteger("laserSetting")));
        info.addAll(super.getHudInfo(stack, advanced));
        info.add(Localization.translate("ic2.tooltip.mode", mode));
        return info;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!IC2.platform.isSimulating()) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        int laserSetting = nbtData.getInteger("laserSetting");
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            laserSetting = (laserSetting + 1) % 8;
            nbtData.setInteger("laserSetting", laserSetting);
            IC2.platform.messagePlayer(player, "ic2.tooltip.mode", getModeString(laserSetting));
        }
        else {
            final int consume = (new int[] { 1250, 100, 5000, 0, 2500, 10000, 5000, 7500 })[laserSetting];
            if (!ElectricItem.manager.use(stack, consume, (EntityLivingBase)player)) {
                return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.FAIL, (Object)stack);
            }
            switch (laserSetting) {
                case 0: {
                    if (this.shootLaser(stack, world, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false)) {
                        IC2.network.get(true).initiateItemEvent(player, stack, 0, true);
                        break;
                    }
                    break;
                }
                case 1: {
                    if (this.shootLaser(stack, world, (EntityLivingBase)player, 4.0f, 5.0f, 1, false, false)) {
                        IC2.network.get(true).initiateItemEvent(player, stack, 1, true);
                        break;
                    }
                    break;
                }
                case 2: {
                    if (this.shootLaser(stack, world, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 20.0f, Integer.MAX_VALUE, false, false)) {
                        IC2.network.get(true).initiateItemEvent(player, stack, 2, true);
                        break;
                    }
                    break;
                }
                case 4: {
                    if (this.shootLaser(stack, world, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 8.0f, Integer.MAX_VALUE, false, true)) {
                        IC2.network.get(true).initiateItemEvent(player, stack, 4, true);
                        break;
                    }
                    break;
                }
                case 5: {
                    final Vector3 look = Util.getLook((Entity)player);
                    final Vector3 right = look.copy().cross(Vector3.UP);
                    if (right.lengthSquared() < 1.0E-4) {
                        final double angle = Math.toRadians(player.rotationYaw) - 1.5707963267948966;
                        right.set(Math.sin(angle), 0.0, -Math.cos(angle));
                    }
                    else {
                        right.normalize();
                    }
                    final Vector3 up = right.copy().cross(look);
                    final int sideShots = 2;
                    final double unitDistance = 8.0;
                    look.scale(8.0);
                    for (int r = -2; r <= 2; ++r) {
                        for (int u = -2; u <= 2; ++u) {
                            final Vector3 dir = look.copy().addScaled(right, r).addScaled(up, u).normalize();
                            this.shootLaser(stack, world, dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 12.0f, Integer.MAX_VALUE, false, false);
                        }
                    }
                    IC2.network.get(true).initiateItemEvent(player, stack, 5, true);
                    break;
                }
                case 6: {
                    if (this.shootLaser(stack, world, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 12.0f, Integer.MAX_VALUE, true, false)) {
                        IC2.network.get(true).initiateItemEvent(player, stack, 6, true);
                        break;
                    }
                    break;
                }
            }
        }
        return super.onItemRightClick(world, player, hand);
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        final ItemStack stack = StackUtil.get(player, hand);
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        if (!IC2.keyboard.isModeSwitchKeyDown(player) && (nbtData.getInteger("laserSetting") == 3 || nbtData.getInteger("laserSetting") == 7)) {
            final Vector3 dir = Util.getLook((Entity)player);
            final double angle = dir.dot(Vector3.UP);
            if (Math.abs(angle) < 1.0 / Math.sqrt(2.0)) {
                if (ElectricItem.manager.use(stack, 3000.0, (EntityLivingBase)player)) {
                    dir.y = 0.0;
                    dir.normalize();
                    Vector3 start = Util.getEyePosition((Entity)player);
                    start.y = pos.getY() + 0.5;
                    start = adjustStartPos(start, dir);
                    if (nbtData.getInteger("laserSetting") == 3) {
                        if (this.shootLaser(stack, world, start, dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false)) {
                            IC2.network.get(true).initiateItemEvent(player, stack, 3, true);
                        }
                    }
                    else if (nbtData.getInteger("laserSetting") == 7 && this.shootLaser(stack, world, start, dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false)) {
                        this.shootLaser(stack, world, new Vector3(start.x, start.y - 1.0, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        this.shootLaser(stack, world, new Vector3(start.x, start.y + 1.0, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        if (player.getHorizontalFacing().equals((Object)EnumFacing.SOUTH) || player.getHorizontalFacing().equals((Object)EnumFacing.NORTH)) {
                            this.shootLaser(stack, world, new Vector3(start.x - 1.0, start.y, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x + 1.0, start.y, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x - 1.0, start.y - 1.0, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x + 1.0, start.y - 1.0, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x - 1.0, start.y + 1.0, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x + 1.0, start.y + 1.0, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        }
                        if (player.getHorizontalFacing().equals((Object)EnumFacing.EAST) || player.getHorizontalFacing().equals((Object)EnumFacing.WEST)) {
                            this.shootLaser(stack, world, new Vector3(start.x, start.y, start.z - 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x, start.y, start.z + 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x, start.y - 1.0, start.z - 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x, start.y - 1.0, start.z + 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x, start.y + 1.0, start.z - 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                            this.shootLaser(stack, world, new Vector3(start.x, start.y + 1.0, start.z + 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        }
                        IC2.network.get(true).initiateItemEvent(player, stack, 7, true);
                    }
                }
            }
            else if (nbtData.getInteger("laserSetting") == 7) {
                if (ElectricItem.manager.use(stack, 3000.0, (EntityLivingBase)player)) {
                    dir.x = 0.0;
                    dir.z = 0.0;
                    dir.normalize();
                    Vector3 start = Util.getEyePosition((Entity)player);
                    start.x = pos.getX() + 0.5;
                    start.z = pos.getZ() + 0.5;
                    start = adjustStartPos(start, dir);
                    if (this.shootLaser(stack, world, start, dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false)) {
                        this.shootLaser(stack, world, new Vector3(start.x + 1.0, start.y, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        this.shootLaser(stack, world, new Vector3(start.x - 1.0, start.y, start.z), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        this.shootLaser(stack, world, new Vector3(start.x + 1.0, start.y, start.z + 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        this.shootLaser(stack, world, new Vector3(start.x - 1.0, start.y, start.z - 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        this.shootLaser(stack, world, new Vector3(start.x + 1.0, start.y, start.z - 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        this.shootLaser(stack, world, new Vector3(start.x - 1.0, start.y, start.z + 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        this.shootLaser(stack, world, new Vector3(start.x, start.y, start.z + 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        this.shootLaser(stack, world, new Vector3(start.x, start.y, start.z - 1.0), dir, (EntityLivingBase)player, Float.POSITIVE_INFINITY, 5.0f, Integer.MAX_VALUE, false, false);
                        IC2.network.get(true).initiateItemEvent(player, stack, 7, true);
                    }
                }
            }
            else {
                IC2.platform.messagePlayer(player, "Mining laser aiming angle too steep", new Object[0]);
            }
        }
        return EnumActionResult.FAIL;
    }
    
    private static Vector3 adjustStartPos(final Vector3 pos, final Vector3 dir) {
        return pos.addScaled(dir, 0.2);
    }
    
    public boolean shootLaser(final ItemStack stack, final World world, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive, final boolean smelt) {
        final Vector3 dir = Util.getLook((Entity)owner);
        return this.shootLaser(stack, world, dir, owner, range, power, blockBreaks, explosive, smelt);
    }
    
    public boolean shootLaser(final ItemStack stack, final World world, final Vector3 dir, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive, final boolean smelt) {
        final Vector3 start = adjustStartPos(Util.getEyePosition((Entity)owner), dir);
        return this.shootLaser(stack, world, start, dir, owner, range, power, blockBreaks, explosive, smelt);
    }
    
    public boolean shootLaser(final ItemStack stack, final World world, final Vector3 start, final Vector3 dir, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive, final boolean smelt) {
        final EntityMiningLaser entity = new EntityMiningLaser(world, start, dir, owner, range, power, blockBreaks, explosive);
        final LaserEvent.LaserShootEvent event = new LaserEvent.LaserShootEvent(world, entity, owner, range, power, blockBreaks, explosive, smelt, stack);
        MinecraftForge.EVENT_BUS.post((Event)event);
        if (event.isCanceled()) {
            return false;
        }
        entity.copyDataFromEvent(event);
        world.spawnEntity((Entity)entity);
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }
    
    @Override
    public void onNetworkEvent(final ItemStack stack, final EntityPlayer player, final int event) {
        switch (event) {
            case 0: {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaser.ogg", true, IC2.audioManager.getDefaultVolume());
                break;
            }
            case 1: {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserLowFocus.ogg", true, IC2.audioManager.getDefaultVolume());
                break;
            }
            case 2: {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserLongRange.ogg", true, IC2.audioManager.getDefaultVolume());
                break;
            }
            case 3: {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaser.ogg", true, IC2.audioManager.getDefaultVolume());
                break;
            }
            case 4: {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaser.ogg", true, IC2.audioManager.getDefaultVolume());
                break;
            }
            case 5: {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserScatter.ogg", true, IC2.audioManager.getDefaultVolume());
                break;
            }
            case 6: {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserExplosive.ogg", true, IC2.audioManager.getDefaultVolume());
                break;
            }
            case 7: {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/MiningLaser/MiningLaserScatter.ogg", true, IC2.audioManager.getDefaultVolume());
                break;
            }
        }
    }
    
    private static String getModeString(final int mode) {
        switch (mode) {
            case 0: {
                return "ic2.tooltip.mode.mining";
            }
            case 1: {
                return "ic2.tooltip.mode.lowFocus";
            }
            case 2: {
                return "ic2.tooltip.mode.longRange";
            }
            case 3: {
                return "ic2.tooltip.mode.horizontal";
            }
            case 4: {
                return "ic2.tooltip.mode.superHeat";
            }
            case 5: {
                return "ic2.tooltip.mode.scatter";
            }
            case 6: {
                return "ic2.tooltip.mode.explosive";
            }
            case 7: {
                return "ic2.tooltip.mode.3x3";
            }
            default: {
                assert false;
                return "";
            }
        }
    }
}
