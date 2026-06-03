package ic2.core.item.armor.jetpack;

import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class JetpackLogic {
  private static boolean lastJetpackUsed;
  
  private static AudioSource audioSource;
  
  public static boolean useJetpack(EntityPlayer player, boolean hoverMode, IJetpack jetpack, ItemStack stack) {
    if (jetpack.getChargeLevel(stack) <= 0.0D)
      return false; 
    IBoostingJetpack bjetpack = (jetpack instanceof IBoostingJetpack) ? (IBoostingJetpack)jetpack : null;
    float power = jetpack.getPower(stack);
    float dropPercentage = jetpack.getDropPercentage(stack);
    if (jetpack.getChargeLevel(stack) <= dropPercentage)
      power = (float)(power * jetpack.getChargeLevel(stack) / dropPercentage); 
    if (IC2.keyboard.isForwardKeyDown(player)) {
      float retruster, boost;
      if (bjetpack != null) {
        retruster = bjetpack.getBaseThrust(stack, hoverMode);
        boost = bjetpack.getBoostThrust(player, stack, hoverMode);
      } else {
        retruster = hoverMode ? 1.0F : 0.15F;
        boost = 0.0F;
      } 
      float forwardpower = power * retruster * 2.0F;
      if (forwardpower > 0.0F) {
        player.func_191958_b(0.0F, 0.0F, 0.4F * forwardpower + boost, 0.02F + boost);
        if (boost != 0.0F && !player.field_70122_E)
          bjetpack.useBoostPower(stack, boost); 
      } 
    } 
    int worldHeight = IC2.getWorldHeight(player.func_130014_f_());
    int maxFlightHeight = (int)(worldHeight / jetpack.getWorldHeightDivisor(stack));
    double y = player.field_70163_u;
    if (y > (maxFlightHeight - 25)) {
      if (y > maxFlightHeight)
        y = maxFlightHeight; 
      power = (float)(power * (maxFlightHeight - y) / 25.0D);
    } 
    double prevmotion = player.field_70181_x;
    player.field_70181_x = Math.min(player.field_70181_x + (power * 0.2F), 0.6000000238418579D);
    if (hoverMode) {
      float maxHoverY = 0.0F;
      if (IC2.keyboard.isJumpKeyDown(player)) {
        maxHoverY += jetpack.getHoverMultiplier(stack, true);
        if (bjetpack != null)
          maxHoverY *= bjetpack.getHoverBoost(player, stack, true); 
      } 
      if (IC2.keyboard.isSneakKeyDown(player)) {
        maxHoverY += -jetpack.getHoverMultiplier(stack, false);
        if (bjetpack != null)
          maxHoverY *= bjetpack.getHoverBoost(player, stack, false); 
      } 
      if (player.field_70181_x > maxHoverY) {
        player.field_70181_x = maxHoverY;
        if (prevmotion > player.field_70181_x)
          player.field_70181_x = prevmotion; 
      } 
    } 
    int consume = 2;
    if (hoverMode)
      consume = 1; 
    if (!player.field_70122_E)
      jetpack.drainEnergy(stack, consume); 
    player.field_70143_R = 0.0F;
    player.field_70140_Q = 0.0F;
    IC2.platform.resetPlayerInAirTime(player);
    return true;
  }
  
  public static void onArmorTick(World world, EntityPlayer player, ItemStack stack, IJetpack jetpack) {
    if (stack == null || !jetpack.isJetpackActive(stack))
      return; 
    NBTTagCompound nbtData = getJetpackCompound(stack);
    boolean hoverMode = getHoverMode(nbtData);
    byte toggleTimer = nbtData.func_74771_c("toggleTimer");
    boolean jetpackUsed = false;
    if (IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
      toggleTimer = 10;
      hoverMode = !hoverMode;
      if (IC2.platform.isSimulating()) {
        nbtData.func_74757_a("hoverMode", hoverMode);
        if (hoverMode) {
          IC2.platform.messagePlayer(player, "Hover Mode enabled.", new Object[0]);
        } else {
          IC2.platform.messagePlayer(player, "Hover Mode disabled.", new Object[0]);
        } 
      } 
    } 
    if (IC2.keyboard.isJumpKeyDown(player) || hoverMode) {
      jetpackUsed = useJetpack(player, hoverMode, jetpack, stack);
      if (player.field_70122_E && hoverMode && IC2.platform.isSimulating()) {
        setHoverMode(nbtData, false);
        IC2.platform.messagePlayer(player, "Hover Mode disabled.", new Object[0]);
      } 
    } 
    if (IC2.platform.isSimulating() && toggleTimer > 0) {
      toggleTimer = (byte)(toggleTimer - 1);
      nbtData.func_74774_a("toggleTimer", toggleTimer);
    } 
    if (IC2.platform.isRendering() && player == IC2.platform.getPlayerInstance()) {
      if (lastJetpackUsed != jetpackUsed) {
        if (jetpackUsed) {
          if (audioSource == null)
            audioSource = IC2.audioManager.createSource(player, PositionSpec.Backpack, "Tools/Jetpack/JetpackLoop.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
          if (audioSource != null)
            audioSource.play(); 
        } else if (audioSource != null) {
          audioSource.remove();
          audioSource = null;
        } 
        lastJetpackUsed = jetpackUsed;
      } 
      if (audioSource != null)
        audioSource.updatePosition(); 
    } 
    if (jetpackUsed)
      player.field_71069_bz.func_75142_b(); 
  }
  
  private static void setHoverMode(NBTTagCompound nbt, boolean value) {
    nbt.func_74757_a("hoverMode", value);
  }
  
  private static boolean getHoverMode(NBTTagCompound nbt) {
    return nbt.func_74767_n("hoverMode");
  }
  
  private static NBTTagCompound getJetpackCompound(ItemStack stack) {
    return StackUtil.getOrCreateNbtData(stack);
  }
}
