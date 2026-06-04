// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor.jetpack;

import ic2.core.util.StackUtil;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.audio.PositionSpec;
import net.minecraft.world.World;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.audio.AudioSource;

public class JetpackLogic
{
    private static boolean lastJetpackUsed;
    private static AudioSource audioSource;
    
    public static boolean useJetpack(final EntityPlayer player, final boolean hoverMode, final IJetpack jetpack, final ItemStack stack) {
        if (jetpack.getChargeLevel(stack) <= 0.0) {
            return false;
        }
        final IBoostingJetpack bjetpack = (jetpack instanceof IBoostingJetpack) ? ((IBoostingJetpack)jetpack) : null;
        float power = jetpack.getPower(stack);
        final float dropPercentage = jetpack.getDropPercentage(stack);
        if (jetpack.getChargeLevel(stack) <= dropPercentage) {
            power *= (float)(jetpack.getChargeLevel(stack) / dropPercentage);
        }
        if (IC2.keyboard.isForwardKeyDown(player)) {
            float retruster;
            float boost;
            if (bjetpack != null) {
                retruster = bjetpack.getBaseThrust(stack, hoverMode);
                boost = bjetpack.getBoostThrust(player, stack, hoverMode);
            }
            else {
                retruster = (hoverMode ? 1.0f : 0.15f);
                boost = 0.0f;
            }
            final float forwardpower = power * retruster * 2.0f;
            if (forwardpower > 0.0f) {
                player.moveRelative(0.0f, 0.0f, 0.4f * forwardpower + boost, 0.02f + boost);
                if (boost != 0.0f && !player.onGround) {
                    bjetpack.useBoostPower(stack, boost);
                }
            }
        }
        final int worldHeight = IC2.getWorldHeight(player.getEntityWorld());
        final int maxFlightHeight = (int)(worldHeight / jetpack.getWorldHeightDivisor(stack));
        double y = player.posY;
        if (y > maxFlightHeight - 25) {
            if (y > maxFlightHeight) {
                y = maxFlightHeight;
            }
            power *= (float)((maxFlightHeight - y) / 25.0);
        }
        final double prevmotion = player.motionY;
        player.motionY = Math.min(player.motionY + power * 0.2f, 0.6000000238418579);
        if (hoverMode) {
            float maxHoverY = 0.0f;
            if (IC2.keyboard.isJumpKeyDown(player)) {
                maxHoverY += jetpack.getHoverMultiplier(stack, true);
                if (bjetpack != null) {
                    maxHoverY *= bjetpack.getHoverBoost(player, stack, true);
                }
            }
            if (IC2.keyboard.isSneakKeyDown(player)) {
                maxHoverY += -jetpack.getHoverMultiplier(stack, false);
                if (bjetpack != null) {
                    maxHoverY *= bjetpack.getHoverBoost(player, stack, false);
                }
            }
            if (player.motionY > maxHoverY) {
                player.motionY = maxHoverY;
                if (prevmotion > player.motionY) {
                    player.motionY = prevmotion;
                }
            }
        }
        int consume = 2;
        if (hoverMode) {
            consume = 1;
        }
        if (!player.onGround) {
            jetpack.drainEnergy(stack, consume);
        }
        player.fallDistance = 0.0f;
        player.distanceWalkedModified = 0.0f;
        IC2.platform.resetPlayerInAirTime(player);
        return true;
    }
    
    public static void onArmorTick(final World world, final EntityPlayer player, final ItemStack stack, final IJetpack jetpack) {
        if (stack == null || !jetpack.isJetpackActive(stack)) {
            return;
        }
        final NBTTagCompound nbtData = getJetpackCompound(stack);
        boolean hoverMode = getHoverMode(nbtData);
        byte toggleTimer = nbtData.getByte("toggleTimer");
        boolean jetpackUsed = false;
        if (IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
            toggleTimer = 10;
            hoverMode = !hoverMode;
            if (IC2.platform.isSimulating()) {
                nbtData.setBoolean("hoverMode", hoverMode);
                if (hoverMode) {
                    IC2.platform.messagePlayer(player, "Hover Mode enabled.", new Object[0]);
                }
                else {
                    IC2.platform.messagePlayer(player, "Hover Mode disabled.", new Object[0]);
                }
            }
        }
        if (IC2.keyboard.isJumpKeyDown(player) || hoverMode) {
            jetpackUsed = useJetpack(player, hoverMode, jetpack, stack);
            if (player.onGround && hoverMode && IC2.platform.isSimulating()) {
                setHoverMode(nbtData, false);
                IC2.platform.messagePlayer(player, "Hover Mode disabled.", new Object[0]);
            }
        }
        if (IC2.platform.isSimulating() && toggleTimer > 0) {
            --toggleTimer;
            nbtData.setByte("toggleTimer", toggleTimer);
        }
        if (IC2.platform.isRendering() && player == IC2.platform.getPlayerInstance()) {
            if (JetpackLogic.lastJetpackUsed != jetpackUsed) {
                if (jetpackUsed) {
                    if (JetpackLogic.audioSource == null) {
                        JetpackLogic.audioSource = IC2.audioManager.createSource(player, PositionSpec.Backpack, "Tools/Jetpack/JetpackLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
                    }
                    if (JetpackLogic.audioSource != null) {
                        JetpackLogic.audioSource.play();
                    }
                }
                else if (JetpackLogic.audioSource != null) {
                    JetpackLogic.audioSource.remove();
                    JetpackLogic.audioSource = null;
                }
                JetpackLogic.lastJetpackUsed = jetpackUsed;
            }
            if (JetpackLogic.audioSource != null) {
                JetpackLogic.audioSource.updatePosition();
            }
        }
        if (jetpackUsed) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }
    
    private static void setHoverMode(final NBTTagCompound nbt, final boolean value) {
        nbt.setBoolean("hoverMode", value);
    }
    
    private static boolean getHoverMode(final NBTTagCompound nbt) {
        return nbt.getBoolean("hoverMode");
    }
    
    private static NBTTagCompound getJetpackCompound(final ItemStack stack) {
        return StackUtil.getOrCreateNbtData(stack);
    }
}
